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
internal class TypeMockBridge(
    private val mockEngine: MockEngine,
    typeAdapter: TypeAdapter,
    containerAdapter: ContainerAdapter,
) {

    private val typeIntrospect = TypeIntrospect(containerAdapter, mockEngine.random)

    private val typeMockMapper = TypeMockMapper(typeAdapter, containerAdapter)

    /**
     * 使用可选配置模拟 Class 对象
     */
    fun <T : Any> mockClass(
        clazz: KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null,
    ): T {
        return mockType(recursiveToKType(clazz), includePrivate, includeStatic, includeTransient, depth)
    }

    /**
     * 使用模拟单个类型
     */
    fun <T : Any> mockType(
        kType: KType,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        val clazz = kType.classifier as? KClass<*> ?: throw IllegalArgumentException("Type classifier is not KClass")
        val mockBeanAnnotation = clazz.findAnnotation<Mock.Bean>()
        val config = BeanMockConfig(
            // 方法参数优先于注解值
            // 如果方法参数为null，则使用注解值；否则使用方法参数
            includePrivate = includePrivate ?: (mockBeanAnnotation?.includePrivate ?: true),
            includeStatic = includeStatic ?: (mockBeanAnnotation?.includeStatic ?: false),
            includeTransient = includeTransient ?: (mockBeanAnnotation?.includeTransient ?: false),
            depth = depth ?: (mockBeanAnnotation?.depth ?: 3)
        )
        val mockPropertyAnnotation = clazz.findAnnotation<Mock.Property>()
        // 处理单个类型
        val gen = typeIntrospect.analyzePropertyType(kType, mockPropertyAnnotation, mockBeanAnnotation, config, 0)
            ?.let { mockEngine.generate(it) }
        @Suppress("UNCHECKED_CAST")
        return typeMockMapper.convertValue(gen, kType, config) as T
    }

    /**
     * 使用可选配置模拟 TypeRef 对象
     */
    fun <T : Any> mockTypeRef(
        typeRef: TypeRef<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null,
    ): T {
        return mockType(typeRef.type, includePrivate, includeStatic, includeTransient, depth)
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