package io.github.spcookie

import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.function.Supplier
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * 用于处理第三方容器类型的容器适配器管理器
 * 为常见的容器类型提供默认适配器，并允许自定义注册
 *
 * @author spcookie
 * @since 1.2.0
 */
class ContainerAdapter {

    /**
     * 容器类型行为枚举
     */
    enum class ContainerBehavior {
        SINGLE_VALUE,    // 容器返回单个值
        STREAM_VALUES    // 容器返回多个值
    }

    /**
     * 容器处理器定义
     */
    data class ContainerHandler(
        val behavior: ContainerBehavior,
        val analyzer: ((List<KType>, Any?, BeanMockConfig, Int) -> Any?)? = null,
        val mapper: ((Any?, List<KType>, BeanMockConfig) -> Any?)? = null
    )

    private val handlers = mutableMapOf<String, ContainerHandler>()

    /**
     * 按限定名称前缀注册容器处理器
     */
    fun register(qualifiedNamePrefix: String, behavior: ContainerBehavior) {
        handlers[qualifiedNamePrefix] = ContainerHandler(behavior)
    }

    /**
     * 注册具有自定义分析器和映射器的容器处理器
     */
    fun register(
        qualifiedNamePrefix: String,
        behavior: ContainerBehavior,
        analyzer: ((List<KType>, Any?, BeanMockConfig, Int) -> Any?)? = null,
        mapper: ((Any?, List<KType>, BeanMockConfig) -> Any?)? = null
    ) {
        handlers[qualifiedNamePrefix] = ContainerHandler(behavior, analyzer, mapper)
    }

    /**
     * 获取所有已注册的容器类型前缀
     */
    fun getRegisteredPrefixes(): Set<String> {
        return handlers.keys.toSet()
    }


    /**
     * 获取类的容器行为
     */
    fun getContainerBehavior(kClass: KClass<*>): ContainerBehavior {
        // 首先检查Java标准类型
        when {
            Optional::class.java.isAssignableFrom(kClass.java) ||
                    CompletableFuture::class.java.isAssignableFrom(kClass.java) ||
                    Future::class.java.isAssignableFrom(kClass.java) ||
                    Callable::class.java.isAssignableFrom(kClass.java) ||
                    Supplier::class.java.isAssignableFrom(kClass.java) ||
                    Lazy::class.java.isAssignableFrom(kClass.java) -> return ContainerBehavior.SINGLE_VALUE
        }

        // 然后检查已注册的第三方类型
        val qualifiedName = kClass.qualifiedName ?: return ContainerBehavior.SINGLE_VALUE
        val handler = handlers.entries.find { qualifiedName.startsWith(it.key) }?.value
        return handler?.behavior ?: ContainerBehavior.SINGLE_VALUE
    }

    /**
     * 获取类的容器处理器
     */
    fun getContainerHandler(kClass: KClass<*>): ContainerHandler? {
        val qualifiedName = kClass.qualifiedName ?: return null
        return handlers.entries.find { qualifiedName.startsWith(it.key) }?.value
    }

    /**
     * 为BeanIntrospect分析容器类型
     */
    fun analyzeContainerType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean?,
        config: BeanMockConfig,
        currentDepth: Int,
        analyzeWrappedType: (KType?, Mock.Property?, Mock.Bean?, BeanMockConfig, Int) -> Any?
    ): Any? {
        val kClass = type.classifier as KClass<*>
        val handler = getContainerHandler(kClass)
        val typeArguments = type.arguments.mapNotNull { it.type }

        // 如果有自定义分析器，则使用它
        if (handler?.analyzer != null) {
            return handler.analyzer.invoke(typeArguments, null, config, currentDepth)
        }

        val behavior = getContainerBehavior(kClass)
        return when (behavior) {
            ContainerBehavior.SINGLE_VALUE -> {
                val wrappedType = type.arguments.firstOrNull()?.type
                analyzeWrappedType(wrappedType, annotation, propertyBeanAnnotation, config, currentDepth)
            }

            ContainerBehavior.STREAM_VALUES -> {
                val elementType = type.arguments.firstOrNull()?.type
                val elementValue =
                    analyzeWrappedType(elementType, annotation, propertyBeanAnnotation, config, currentDepth)
                listOf(elementValue)
            }
        }
    }

    /**
     * 为BeanMockMapper转换容器值
     */
    fun convertContainerValue(
        value: Any?,
        targetType: KType,
        config: BeanMockConfig,
        typeAdapter: TypeAdapter,
        convertWrappedValue: (Any?, KType?, BeanMockConfig) -> Any?
    ): Any? {
        if (value == null) return null

        val targetClass = targetType.classifier as KClass<*>
        val handler = getContainerHandler(targetClass)
        val typeArguments = targetType.arguments.mapNotNull { it.type }

        // 如果有自定义映射器，则使用它
        if (handler?.mapper != null) {
            return handler.mapper.invoke(value, typeArguments, config)
        }

        // 首先尝试为第三方类型使用TypeAdapter
        val adapter = typeAdapter.get(targetClass)
        if (adapter != null) {
            return adapter.invoke(value)
        }

        // 回退到标准容器处理
        val wrappedType = targetType.arguments.firstOrNull()?.type
        val behavior = getContainerBehavior(targetClass)

        return when (behavior) {
            ContainerBehavior.SINGLE_VALUE -> {
                when {
                    Optional::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        Optional.ofNullable(wrappedValue)
                    }

                    Stream::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        Stream.ofNullable(wrappedValue)
                    }

                    Future::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        CompletableFuture.completedFuture(wrappedValue)
                    }

                    Callable::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        Callable { wrappedValue }
                    }

                    Supplier::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        Supplier { wrappedValue }
                    }

                    Lazy::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        lazy { wrappedValue }
                    }

                    else -> convertWrappedValue(value, wrappedType, config)
                }
            }

            ContainerBehavior.STREAM_VALUES -> {
                when (value) {
                    is List<*> -> value.map { convertWrappedValue(it, wrappedType, config) }
                    else -> listOf(convertWrappedValue(value, wrappedType, config))
                }
            }
        }
    }
}