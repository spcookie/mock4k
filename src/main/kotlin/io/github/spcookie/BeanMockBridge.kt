package io.github.spcookie

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.starProjectedType

/**
 * 用于生成模拟对象的Bean模拟引擎
 * 重新设计以充分利用MockEngine的功能
 *
 * @author spcookie
 * @since 1.2.0
 */
internal class BeanMockBridge(
    private val mockEngine: MockEngine,
    typeAdapter: TypeAdapter,
    private val containerAdapter: ContainerAdapter
) {

    private val beanIntrospect = BeanIntrospect(containerAdapter)

    private val beanMockMapper = BeanMockMapper(typeAdapter, containerAdapter)

    /**
     * 使用可选配置模拟Bean对象
     */
    fun <T : Any> mockBean(
        clazz: KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return when {
            isSingleType(clazz, containerAdapter) -> {
                mockType(recursiveToKType(clazz))
            }

            else -> {
                val mockBeanAnnotation = clazz.findAnnotation<Mock.Bean>()
                val config = BeanMockConfig(
                    // 方法参数优先于注解值
                    // 如果方法参数为null，则使用注解值；否则使用方法参数
                    includePrivate = includePrivate ?: (mockBeanAnnotation?.includePrivate ?: false),
                    includeStatic = includeStatic ?: (mockBeanAnnotation?.includeStatic ?: false),
                    includeTransient = includeTransient ?: (mockBeanAnnotation?.includeTransient ?: false),
                    depth = depth ?: (mockBeanAnnotation?.depth ?: 3)
                )

                mockBeanInternal(clazz, config)
            }
        }
    }

    /**
     * 使用模拟单个类型
     */
    fun <T : Any> mockType(kType: KType): T {
        val clazz = kType.classifier as? KClass<*> ?: throw IllegalArgumentException("Type classifier is not KClass")
        return if (isSingleType(clazz, containerAdapter)) {
            // 处理单个类型
            val gen = beanIntrospect.analyzePropertyType(kType, null)
                ?.let { mockEngine.generate(it) }
            @Suppress("UNCHECKED_CAST")
            beanMockMapper.convertValue(gen, kType, BeanMockConfig()) as T
        } else {
            throw IllegalArgumentException("Cannot generate single type for ${kType.classifier}")
        }
    }

    /**
     * 使用新架构的内部Bean模拟实现：
     * 1. 分析Bean属性并转换为Map结构
     * 2. 使用MockEngine生成数据
     * 3. 将生成的数据映射回Bean对象
     */
    private fun <T : Any> mockBeanInternal(clazz: KClass<T>, config: BeanMockConfig): T {
        try {
            // 步骤1：分析Bean属性并转换为Map结构
            val propertyMap = beanIntrospect.analyzeBean(clazz, config)

            // 步骤2：使用MockEngine生成数据
            @Suppress("UNCHECKED_CAST")
            val generatedData = mockEngine.generate(propertyMap) as Map<String, Any?>

            // 步骤3：将生成的数据映射回Bean对象
            val result = beanMockMapper.mapToBean(clazz, generatedData, config)

            return result
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot generate mock bean for ${clazz.simpleName}", e)
        }
    }

    fun recursiveToKType(clazz: KClass<*>): KType {
        if (clazz.typeParameters.isEmpty()) {
            return clazz.createType()
        }
        val typeArgs = clazz.typeParameters.map { param ->
            val upper = param.upperBounds.firstOrNull() ?: Any::class.starProjectedType
            val upperClassifier = upper.classifier as? KClass<*> ?: Any::class
            val recursiveType = recursiveToKType(upperClassifier)
            KTypeProjection.invariant(recursiveType)
        }
        return clazz.createType(typeArgs)
    }
}