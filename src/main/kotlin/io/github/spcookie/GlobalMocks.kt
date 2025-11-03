/**
 * Mock4K - 用于生成模拟数据的Kotlin库
 * <p>
 * 这是Mock4K库的主要入口点，提供用于生成模拟数据的工具，
 * 包括随机值、特定区域设置的数据和具有可自定义配置的复杂Bean对象。
 * </p>
 *
 * @author spcookie
 * @since 1.0.0
 */
@file:JvmName("GlobalMocks")
@file:JvmMultifileClass
@file:Suppress("unused")

package io.github.spcookie

import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance


/**
 * 核心模拟引擎的抽象基类。
 * 集中了所有内部委托方法和公共 API 的实现逻辑。
 */
abstract class BaseMock(
    /** 用于生成随机值的Random工具实例 */
    val random: MockRandom = MockRandom(),
    /** 用于自定义类型转换的类型适配器管理器 */
    typeAdapter: TypeAdapter = TypeAdapter(),
    /** 用于自定义容器类型处理的容器适配器管理器 */
    containerAdapter: ContainerAdapter = ContainerAdapter()
) {
    /** 用于特定区域设置数据生成的Locale工具实例 */
    val locale = LocaleManager

    /** 单例MockEngine实例以在调用间维护状态 */
    private val mockEngine = MockEngine(random)

    /** 用于处理Bean对象生成的BeanMockBridge实例 */
    private val typeMockBridge = TypeMockBridge(mockEngine, typeAdapter, containerAdapter)

    // --- 内部核心委托方法 ---

    @JvmSynthetic
    internal fun g(template: Any): Any {
        return mockEngine.generate(template)
    }

    @JvmSynthetic
    internal fun <T : Any> bg(
        clazz: KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return typeMockBridge.mockClass(clazz, includePrivate, includeStatic, includeTransient, depth)
    }

    @JvmSynthetic
    internal fun <T : Any> stg(
        type: KType,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return typeMockBridge.mockType(type, includePrivate, includeStatic, includeTransient, depth)
    }

    @JvmSynthetic
    internal fun <T : Any> s(clazz: KClass<T>, config: BeanMockConfig? = null): T {
        // 创建MockStub对象
        return MethodMockStub.make(clazz, config).createInstance()
    }

    // --- 公共 API 实现逻辑 ---

    /** 基于模板映射生成模拟数据 */
    @Suppress("UNCHECKED_CAST")
    fun mock(template: Map<String, *>): Map<String, *> {
        return g(template as Any) as Map<String, *>
    }

    /** 基于模板列表生成模拟数据 */
    @Suppress("UNCHECKED_CAST")
    fun mock(template: List<*>): List<*> {
        return g(template as Any) as List<*>
    }

    /** 基于JSON模板字符串生成模拟数据 */
    fun mock(template: String): String {
        val parse = Mson.parse(template)
        return parse?.let { Mson.stringify(g(parse)) }
            ?: throw IllegalArgumentException("Invalid json template")
    }

    /** 生成模拟Bean对象 */
    @JvmSynthetic
    fun <T : Any> mock(
        clazz: KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return bg(clazz, includePrivate, includeStatic, includeTransient, depth)
    }

    /** 使用具体化类型生成模拟Bean对象 */
    @JvmSynthetic
    inline fun <reified T : Any> mock(
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return mock(T::class, includePrivate, includeStatic, includeTransient, depth)
    }

    /** 使用配置对象生成模拟Bean对象 */
    @JvmSynthetic
    fun <T : Any> mock(
        clazz: KClass<T>,
        config: BeanMockConfig? = null
    ): T {
        val includePrivate = config?.includePrivate
        val includeStatic = config?.includeStatic
        val includeTransient = config?.includeTransient
        val depth = config?.depth
        return mock(clazz, includePrivate, includeStatic, includeTransient, depth)
    }

    /** 使用配置对象和具体化类型生成模拟Bean对象 */
    @JvmSynthetic
    inline fun <reified T : Any> mock(
        config: BeanMockConfig? = null
    ): T {
        val includePrivate = config?.includePrivate
        val includeStatic = config?.includeStatic
        val includeTransient = config?.includeTransient
        val depth = config?.depth
        return mock(T::class, includePrivate, includeStatic, includeTransient, depth)
    }

    /** 生成模拟Bean对象（Java版本） */
    @JvmOverloads
    fun <T : Any> mock(
        clazz: Class<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return mock(clazz.kotlin, includePrivate, includeStatic, includeTransient, depth)
    }

    /** 基于KType生成模拟对象 */
    @JvmSynthetic
    fun <T : Any> mock(
        type: KType,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return stg(type, includePrivate, includeStatic, includeTransient, depth)
    }

    /** 基于Java Type生成模拟对象 */
    @JvmOverloads
    fun <T : Any> mock(
        type: Type,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return mock(type.toKType(), includePrivate, includeStatic, includeTransient, depth)
    }

    /** 基于 TypeRef 生成模拟对象 */
    fun <T : Any> mock(
        typeRef: TypeRef<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return mock(typeRef.type, includePrivate, includeStatic, includeTransient, depth)
    }

    /** 使用Java类和配置对象生成模拟Bean对象 */
    fun <T : Any> mock(
        clazz: Class<T>,
        config: BeanMockConfig? = null
    ): T {
        val includePrivate = config?.includePrivate
        val includeStatic = config?.includeStatic
        val includeTransient = config?.includeTransient
        val depth = config?.depth
        return mock(clazz, includePrivate, includeStatic, includeTransient, depth)
    }

    /** 创建MockStub对象 */
    @JvmSynthetic
    fun <T : Any> load(clazz: KClass<T>, config: BeanMockConfig? = null): T {
        return s(clazz, config)
    }

    /** 使用具体化类型和创建创建MockStub对象 */
    @JvmSynthetic
    inline fun <reified T : Any> load(config: BeanMockConfig? = null): T {
        return load(T::class, config)
    }

    /** 创建MockStub对象（Java版本） */
    @JvmOverloads
    fun <T : Any> load(clazz: Class<T>, config: BeanMockConfig? = null): T {
        return load(clazz.kotlin, config)
    }
}

// --- 全局配置对象 ---

object GlobalMockConf {
    /** 用于生成随机值的Random工具实例 */
    @JvmField
    val Random = MockRandom()

    /** 用于特定区域设置数据生成的Locale工具实例 */
    @JvmField
    val Locale = LocaleManager

    /** 用于自定义类型转换的类型适配器管理器 */
    @JvmField
    val TypeAdapter: TypeAdapter = TypeAdapter()

    /** 用于自定义容器类型处理的容器适配器管理器 */
    @JvmField
    val ContainerAdapter: ContainerAdapter = ContainerAdapter()

    /**
     * 全局单例 MockEngine 实例。
     * 使用匿名对象继承 BaseMock，确保所有全局调用都委托给此单例。
     */
    @JvmField
    val INSTANCE = object : BaseMock(Random, TypeAdapter, ContainerAdapter) {}

}

// --- 顶层全局函数 (委托给 GlobalMockConf.INSTANCE) ---

@JvmOverloads
fun inherit(
    random: MockRandom? = null,
    typeAdapter: TypeAdapter? = null,
    containerAdapter: ContainerAdapter? = null
): ScopeMock {
    return ScopeMock.create(
        random ?: GlobalMockConf.Random,
        typeAdapter ?: GlobalMockConf.TypeAdapter,
        containerAdapter ?: GlobalMockConf.ContainerAdapter
    )
}

/** 基于模板映射生成模拟数据 */
fun mock(template: Map<String, *>): Map<String, *> {
    return GlobalMockConf.INSTANCE.mock(template)
}

/** 基于模板列表生成模拟数据 */
fun mock(template: List<*>): List<*> {
    return GlobalMockConf.INSTANCE.mock(template)
}

/** 基于JSON模板字符串生成模拟数据 */
fun mock(template: String): String {
    return GlobalMockConf.INSTANCE.mock(template)
}

/** 生成模拟Bean对象 */
@JvmSynthetic
fun <T : Any> mock(
    clazz: KClass<T>,
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null,
    depth: Int? = null
): T {
    return GlobalMockConf.INSTANCE.mock(clazz, includePrivate, includeStatic, includeTransient, depth)
}

/** 使用具体化类型生成模拟Bean对象 */
@JvmSynthetic
inline fun <reified T : Any> mock(
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null,
    depth: Int? = null
): T {
    return GlobalMockConf.INSTANCE.mock(T::class, includePrivate, includeStatic, includeTransient, depth)
}

/** 使用配置对象生成模拟Bean对象 */
@JvmSynthetic
fun <T : Any> mock(
    clazz: KClass<T>,
    config: BeanMockConfig? = null
): T {
    return GlobalMockConf.INSTANCE.mock(clazz, config)
}

/** 使用配置对象和具体化类型生成模拟Bean对象 */
@JvmSynthetic
inline fun <reified T : Any> mock(
    config: BeanMockConfig? = null
): T {
    return GlobalMockConf.INSTANCE.mock(T::class, config)
}

/** 生成模拟Bean对象（Java版本） */
@JvmOverloads
fun <T : Any> mock(
    clazz: Class<T>,
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null,
    depth: Int? = null
): T {
    return GlobalMockConf.INSTANCE.mock(clazz, includePrivate, includeStatic, includeTransient, depth)
}

/** 基于KType生成模拟对象 */
@JvmSynthetic
fun <T : Any> mock(
    type: KType,
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null,
    depth: Int? = null
): T {
    return GlobalMockConf.INSTANCE.mock(type, includePrivate, includeStatic, includeTransient, depth)
}

/** 基于Java Type生成模拟对象 */
@JvmOverloads
fun <T : Any> mock(
    type: Type,
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null,
    depth: Int? = null
): T {
    return GlobalMockConf.INSTANCE.mock(type, includePrivate, includeStatic, includeTransient, depth)
}

/** 基于 TypeRef 生成模拟对象 */
@JvmOverloads
fun <T : Any> mock(
    typeRef: TypeRef<T>,
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null,
    depth: Int? = null
): T {
    return GlobalMockConf.INSTANCE.mock(typeRef, includePrivate, includeStatic, includeTransient, depth)
}

/** 使用Java类和配置对象生成模拟Bean对象 */
fun <T : Any> mock(
    clazz: Class<T>,
    config: BeanMockConfig? = null
): T {
    return GlobalMockConf.INSTANCE.mock(clazz, config)
}

/** 创建MockStub对象 */
@JvmSynthetic
fun <T : Any> load(clazz: KClass<T>, config: BeanMockConfig? = null): T {
    return GlobalMockConf.INSTANCE.load(clazz, config)
}

/** 使用具体化类型和创建创建MockStub对象 */
@JvmSynthetic
inline fun <reified T : Any> load(config: BeanMockConfig? = null): T {
    return GlobalMockConf.INSTANCE.load(T::class, config)
}

/** 创建MockStub对象（Java版本） */
@JvmOverloads
fun <T : Any> load(clazz: Class<T>, config: BeanMockConfig? = null): T {
    return GlobalMockConf.INSTANCE.load(clazz, config)
}

// ---  作用域模拟对象 ---

/**
 * 带有独立配置的局部作用域模拟对象。
 * 继承 BaseMock 以自动获得所有 mock 和 load 方法。
 */
class ScopeMock private constructor(
    random: MockRandom,
    typeAdapter: TypeAdapter = TypeAdapter(),
    containerAdapter: ContainerAdapter = ContainerAdapter()
) : BaseMock(random, typeAdapter, containerAdapter) {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun create(
            random: MockRandom = MockRandom(),
            typeAdapter: TypeAdapter = TypeAdapter(),
            containerAdapter: ContainerAdapter = ContainerAdapter()
        ): ScopeMock {
            return ScopeMock(random, typeAdapter, containerAdapter)
        }
    }

}