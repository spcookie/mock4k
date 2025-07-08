package io.github.spcookie

import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Container adapter manager for handling third-party container types
 * Provides default adapters for common container types and allows custom registration
 *
 * @author spcookie
 * @since 1.2.0
 */
class ContainerAdapter {

    /**
     * Container type behavior enumeration
     */
    enum class ContainerBehavior {
        SINGLE_VALUE,    // Returns single wrapped value
        STREAM_VALUES,   // Returns list of values (for streams like Flux, Observable)
        RIGHT_TYPE,      // Uses second type parameter (for Either, Validation)
        NO_VALUE         // Returns null (for Completable)
    }

    /**
     * Container handler definition
     */
    data class ContainerHandler(
        val behavior: ContainerBehavior,
        val analyzer: ((KType, Any?, BeanMockConfig, Int) -> Any?)? = null,
        val mapper: ((Any?, KType, BeanMockConfig) -> Any?)? = null
    )

    private val handlers = mutableMapOf<String, ContainerHandler>()

    init {
        registerDefaultHandlers()
    }

    /**
     * Register default container handlers
     */
    private fun registerDefaultHandlers() {
        // Java standard container types
        register("java.util.Optional", ContainerBehavior.SINGLE_VALUE)
        register("java.util.concurrent.CompletableFuture", ContainerBehavior.SINGLE_VALUE)
        register("java.util.concurrent.Future", ContainerBehavior.SINGLE_VALUE)
        register("java.util.concurrent.Callable", ContainerBehavior.SINGLE_VALUE)
        register("java.util.function.Supplier", ContainerBehavior.SINGLE_VALUE)
        register("kotlin.Lazy", ContainerBehavior.SINGLE_VALUE)

        // Kotlin Coroutines
        register("kotlinx.coroutines.Deferred", ContainerBehavior.SINGLE_VALUE)

        // Project Reactor
        register("reactor.core.publisher.Mono", ContainerBehavior.SINGLE_VALUE)
        register("reactor.core.publisher.Flux", ContainerBehavior.STREAM_VALUES)

        // RxJava 2/3
        register("io.reactivex.Single", ContainerBehavior.SINGLE_VALUE)
        register("io.reactivex.Maybe", ContainerBehavior.SINGLE_VALUE)
        register("io.reactivex.Observable", ContainerBehavior.STREAM_VALUES)
        register("io.reactivex.Flowable", ContainerBehavior.STREAM_VALUES)
        register("io.reactivex.Completable", ContainerBehavior.NO_VALUE)
        register("io.reactivex.rxjava3.core.Single", ContainerBehavior.SINGLE_VALUE)
        register("io.reactivex.rxjava3.core.Maybe", ContainerBehavior.SINGLE_VALUE)
        register("io.reactivex.rxjava3.core.Observable", ContainerBehavior.STREAM_VALUES)
        register("io.reactivex.rxjava3.core.Flowable", ContainerBehavior.STREAM_VALUES)
        register("io.reactivex.rxjava3.core.Completable", ContainerBehavior.NO_VALUE)

        // Vavr (formerly Javaslang)
        register("io.vavr.control.Option", ContainerBehavior.SINGLE_VALUE)
        register("io.vavr.control.Try", ContainerBehavior.SINGLE_VALUE)
        register("io.vavr.control.Either", ContainerBehavior.RIGHT_TYPE)
        register("io.vavr.control.Validation", ContainerBehavior.RIGHT_TYPE)
        register("io.vavr.Lazy", ContainerBehavior.SINGLE_VALUE)
        register("io.vavr.concurrent.Future", ContainerBehavior.SINGLE_VALUE)

        // Arrow (Kotlin functional programming)
        register("arrow.core.Option", ContainerBehavior.SINGLE_VALUE)
        register("arrow.core.Either", ContainerBehavior.RIGHT_TYPE)
        register("arrow.core.Try", ContainerBehavior.SINGLE_VALUE)
        register("arrow.core.Validated", ContainerBehavior.RIGHT_TYPE)
        register("arrow.fx.coroutines.Resource", ContainerBehavior.SINGLE_VALUE)
    }

    /**
     * Register a container handler by qualified name prefix
     */
    fun register(qualifiedNamePrefix: String, behavior: ContainerBehavior) {
        handlers[qualifiedNamePrefix] = ContainerHandler(behavior)
    }

    /**
     * Register a container handler with custom analyzer and mapper
     */
    fun register(
        qualifiedNamePrefix: String,
        behavior: ContainerBehavior,
        analyzer: ((KType, Any?, BeanMockConfig, Int) -> Any?)? = null,
        mapper: ((Any?, KType, BeanMockConfig) -> Any?)? = null
    ) {
        handlers[qualifiedNamePrefix] = ContainerHandler(behavior, analyzer, mapper)
    }

    /**
     * Get all registered container type prefixes
     */
    fun getRegisteredPrefixes(): Set<String> {
        return handlers.keys.toSet()
    }


    /**
     * Get container behavior for a class
     */
    fun getContainerBehavior(kClass: KClass<*>): ContainerBehavior {
        // First check Java standard types
        when {
            Optional::class.java.isAssignableFrom(kClass.java) ||
                    CompletableFuture::class.java.isAssignableFrom(kClass.java) ||
                    java.util.concurrent.Future::class.java.isAssignableFrom(kClass.java) ||
                    java.util.concurrent.Callable::class.java.isAssignableFrom(kClass.java) ||
                    java.util.function.Supplier::class.java.isAssignableFrom(kClass.java) ||
                    kotlin.Lazy::class.java.isAssignableFrom(kClass.java) -> return ContainerBehavior.SINGLE_VALUE
        }

        // Then check registered third-party types
        val qualifiedName = kClass.qualifiedName ?: return ContainerBehavior.SINGLE_VALUE
        val handler = handlers.entries.find { qualifiedName.startsWith(it.key) }?.value
        return handler?.behavior ?: ContainerBehavior.SINGLE_VALUE
    }

    /**
     * Get container handler for a class
     */
    fun getContainerHandler(kClass: KClass<*>): ContainerHandler? {
        val qualifiedName = kClass.qualifiedName ?: return null
        return handlers.entries.find { qualifiedName.startsWith(it.key) }?.value
    }

    /**
     * Analyze container type for BeanIntrospect
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

        // Use custom analyzer if available
        if (handler?.analyzer != null) {
            return handler.analyzer.invoke(type, null, config, currentDepth)
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

            ContainerBehavior.RIGHT_TYPE -> {
                val rightType = type.arguments.getOrNull(1)?.type
                analyzeWrappedType(rightType, annotation, propertyBeanAnnotation, config, currentDepth)
            }

            ContainerBehavior.NO_VALUE -> null
        }
    }

    /**
     * Convert container value for BeanMockMapper
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

        // Use custom mapper if available
        if (handler?.mapper != null) {
            return handler.mapper.invoke(value, targetType, config)
        }

        // Try to use TypeAdapter first for third-party types
        val adapter = typeAdapter.get(targetClass)
        if (adapter != null) {
            return adapter.invoke(value)
        }

        // Fallback to standard container handling
        val wrappedType = targetType.arguments.firstOrNull()?.type
        val behavior = getContainerBehavior(targetClass)

        return when (behavior) {
            ContainerBehavior.SINGLE_VALUE -> {
                when {
                    Optional::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        Optional.ofNullable(wrappedValue)
                    }

                    CompletableFuture::class.java.isAssignableFrom(targetClass.java) ||
                            java.util.concurrent.Future::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        CompletableFuture.completedFuture(wrappedValue)
                    }

                    java.util.concurrent.Callable::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        java.util.concurrent.Callable { wrappedValue }
                    }

                    java.util.function.Supplier::class.java.isAssignableFrom(targetClass.java) -> {
                        val wrappedValue = convertWrappedValue(value, wrappedType, config)
                        java.util.function.Supplier { wrappedValue }
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

            ContainerBehavior.RIGHT_TYPE -> {
                val rightType = targetType.arguments.getOrNull(1)?.type
                convertWrappedValue(value, rightType, config)
            }

            ContainerBehavior.NO_VALUE -> null
        }
    }
}