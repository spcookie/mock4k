@file:JvmName("MockUtils")

package io.github.spcookie

import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance


/**
 * Mock4K - 用于生成模拟数据的Kotlin库
 *
 * 这是Mock4K库的主要入口点，提供用于生成模拟数据的工具，
 * 包括随机值、特定区域设置的数据和具有可自定义配置的复杂Bean对象。
 *
 * @author spcookie
 * @since 1.0.0
 */
object Mocks {

    /**
     * 用于生成随机值的Random工具实例
     *
     * 提供对各种随机数据生成方法的访问，包括
     * 数字、字符串、日期和其他基本类型。
     */
    @JvmField
    val Random = MockRandom

    /**
     * 用于特定区域设置数据生成的Locale工具实例
     *
     * 管理区域设置并提供区域感知的数据生成，
     * 如姓名、地址和其他特定区域的信息。
     */
    @JvmField
    val Locale = LocaleManager

    /**
     * 用于自定义类型转换的类型适配器管理器
     *
     * 允许注册自定义类型适配器来处理
     * 模拟数据生成过程中的特定数据类型。
     */
    @JvmField
    val TypeAdapter = TypeAdapter()

    /**
     * 用于自定义容器类型处理的容器适配器管理器
     *
     * 管理List、Set、Map等容器类型的适配器，
     * 以自定义集合如何填充模拟数据。
     */
    @JvmField
    val ContainerAdapter = ContainerAdapter()

    /**
     * 单例MockEngine实例以在调用间维护状态
     *
     * 负责处理模板并基于各种输入格式和配置
     * 生成模拟数据的核心引擎。
     */
    private val mockEngine = MockEngine()

    /**
     * 用于处理Bean对象生成的BeanMockBridge实例
     *
     * 在核心模拟引擎和Bean特定生成逻辑之间架起桥梁，
     * 集成类型和容器适配器。
     */
    private val beanMockBridge = BeanMockBridge(mockEngine, TypeAdapter, ContainerAdapter)

    /**
     * 基于模板生成模拟数据
     *
     * 这是一个内部工具方法，委托给模拟引擎
     * 基于提供的模板生成数据。
     *
     * @param template 数据模板（可以是Map、List或其他支持的类型）
     * @return 匹配模板结构的生成模拟数据
     * @throws IllegalArgumentException 如果模板格式不受支持
     */
    @JvmSynthetic
    internal fun g(template: Any): Any {
        return mockEngine.generate(template)!!
    }

    /**
     * 生成模拟Bean对象
     *
     * 这是一个内部工具方法，委托给Bean模拟引擎
     * 生成具有可配置属性包含的复杂对象实例。
     *
     * @param clazz 要模拟的Kotlin类
     * @param includePrivate 是否模拟私有属性（null使用注解/默认值）
     * @param includeStatic 是否模拟静态属性（null使用注解/默认值）
     * @param includeTransient 是否模拟瞬态属性（null使用注解/默认值）
     * @param depth 递归Bean生成的最大深度（null使用注解/默认值3）
     * @return 生成的具有填充属性的模拟Bean对象
     * @throws IllegalArgumentException 如果类无法实例化
     */
    @JvmSynthetic
    internal fun <T : Any> bg(
        clazz: KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return beanMockBridge.mockBean(clazz, includePrivate, includeStatic, includeTransient, depth)
    }

    /**
     * 基于KType生成模拟对象
     *
     * 使用Kotlin类型信息生成模拟对象，支持复杂的泛型类型。
     *
     * @param type Kotlin类型信息
     * @return 生成的指定类型的模拟对象
     * @throws IllegalArgumentException 如果类型无法实例化
     */
    fun <T : Any> stg(type: KType): T {
        return beanMockBridge.mockSingleType(type)
    }

    /**
     * 创建MockStub对象
     *
     * 此方法创建给定类的动态子类并拦截
     * 所有公共非void/非Unit方法以返回模拟值。
     *
     * @param clazz 要为其创建Stub的Kotlin类
     * @return 公共方法返回模拟值的Stub实例
     * @throws IllegalArgumentException 如果类无法被子类化
     */
    @JvmSynthetic
    internal fun <T : Any> s(clazz: KClass<T>): T {
        return MethodMockStub.make(clazz).createInstance()
    }

}

/**
 * 基于模板映射生成模拟数据
 *
 * 通过处理映射模板创建模拟数据，其中键表示
 * 字段名称，值表示数据生成规则或模式。
 *
 * @param template 作为映射的数据模板，包含字符串键和值模式
 * @return 生成的具有相同结构的映射模拟数据
 * @throws IllegalArgumentException 如果模板包含不支持的值类型
 */
@Suppress("UNCHECKED_CAST")
fun mock(template: Map<String, *>): Map<String, *> {
    return Mocks.g(template as Any) as Map<String, *>
}

/**
 * 基于模板列表生成模拟数据
 *
 * 通过处理列表模板创建模拟数据，其中每个元素
 * 表示要应用的数据生成规则或模式。
 *
 * @param template 作为列表的数据模板，包含生成模式
 * @return 生成的具有相应模拟值的列表模拟数据
 * @throws IllegalArgumentException 如果模板包含不支持的元素类型
 */
@Suppress("UNCHECKED_CAST")
fun mock(template: List<*>): List<*> {
    return Mocks.g(template as Any) as List<*>
}

/**
 * 基于JSON模板字符串生成模拟数据
 *
 * 解析JSON模板字符串并根据模板结构生成模拟数据，
 * 然后将结果作为JSON字符串返回。
 *
 * @param template 包含生成模式的JSON模板字符串
 * @return 生成的JSON字符串格式的模拟数据
 * @throws IllegalArgumentException 如果模板不是有效的JSON
 */
fun mock(template: String): String {
    val parse = Mson.parse(template)
    return parse?.let { Mson.stringify(Mocks.g(parse)) }
        ?: throw IllegalArgumentException("Invalid json template")
}

/**
 * 生成模拟Bean对象
 *
 * @param clazz 要模拟的类
 * @param includePrivate 是否模拟私有属性（默认：null，使用注解值）
 * @param includeStatic 是否模拟静态属性（默认：null，使用注解值）
 * @param includeTransient 是否模拟瞬态属性（默认：null，使用注解值）
 * @param depth 递归Bean生成的最大深度（默认：null，使用注解值或3）
 * @return 生成的模拟Bean对象
 */
@JvmSynthetic
fun <T : Any> mock(
    clazz: KClass<T>,
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null,
    depth: Int? = null
): T {
    return Mocks.bg(clazz, includePrivate, includeStatic, includeTransient, depth)
}

/**
 * 使用具体化类型生成模拟Bean对象
 *
 * 便利方法，使用Kotlin的具体化泛型自动
 * 确定目标类类型，无需显式传递类。
 *
 * @param T 要生成的类型（自动推断）
 * @param includePrivate 是否模拟私有属性（默认：null，使用注解值）
 * @param includeStatic 是否模拟静态属性（默认：null，使用注解值）
 * @param includeTransient 是否模拟瞬态属性（默认：null，使用注解值）
 * @param depth 递归Bean生成的最大深度（默认：null，使用注解值或3）
 * @return 生成的T类型模拟Bean对象
 * @throws IllegalArgumentException 如果类型T无法实例化
 */
@JvmSynthetic
inline fun <reified T : Any> mock(
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null,
    depth: Int? = null
): T {
    return mock(T::class, includePrivate, includeStatic, includeTransient, depth)
}

/**
 * 使用配置对象生成模拟Bean对象
 *
 * 接受配置对象而不是单个参数的替代方法，
 * 为Bean生成设置提供更结构化的方法。
 *
 * @param clazz 要模拟的Kotlin类
 * @param config 包含所有生成设置的配置对象（可为空）
 * @return 生成的模拟Bean对象
 * @throws IllegalArgumentException 如果类无法实例化
 */
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

/**
 * 使用配置对象和具体化类型生成模拟Bean对象
 *
 * 结合具体化泛型的便利性和配置对象方法，
 * 提供生成模拟Bean最灵活和类型安全的方式。
 *
 * @param T 要生成的类型（自动推断）
 * @param config 包含所有生成设置的配置对象（可为空）
 * @return 生成的T类型模拟Bean对象
 * @throws IllegalArgumentException 如果类型T无法实例化
 */
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

/**
 * 生成模拟Bean对象（Java版本）
 *
 * @param clazz 要模拟的Java类
 * @param includePrivate 是否模拟私有属性（默认：null，使用注解值）
 * @param includeStatic 是否模拟静态属性（默认：null，使用注解值）
 * @param includeTransient 是否模拟瞬态属性（默认：null，使用注解值）
 * @param depth 递归Bean生成的最大深度（默认：null，使用注解值或3）
 * @return 生成的模拟Bean对象
 */
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

/**
 * 基于KType生成模拟对象
 *
 * 使用Kotlin类型信息生成模拟对象，支持复杂的泛型类型。
 *
 * @param type Kotlin类型信息
 * @return 生成的指定类型的模拟对象
 * @throws IllegalArgumentException 如果类型无法实例化
 */
fun <T : Any> mock(type: KType): T {
    return Mocks.stg(type)
}

/**
 * 基于Java Type生成模拟对象
 *
 * 使用Java反射类型信息生成模拟对象，会自动转换为Kotlin类型。
 *
 * @param type Java反射类型信息
 * @return 生成的指定类型的模拟对象
 * @throws IllegalArgumentException 如果类型无法实例化
 */
fun <T : Any> mock(type: Type): T {
    return mock(type.toKType())
}

/**
 * 使用Java类和配置对象生成模拟Bean对象
 *
 * 接受配置对象的Java版本，提供配置Bean生成的结构化方法。
 *
 * @param clazz 要模拟的Java类
 * @param config 包含所有生成设置的配置对象（可为空）
 * @return 生成的模拟Bean对象
 * @throws IllegalArgumentException 如果类无法实例化
 */
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

/**
 * 创建MockStub对象
 *
 * 创建给定类的动态子类并拦截所有公共
 * 非void/Unit方法以返回模拟值。
 *
 * @param clazz 要为其创建Stub的Kotlin类
 * @return 公共方法返回模拟值的Stub实例
 * @throws IllegalArgumentException 如果类无法被子类化
 */
@JvmSynthetic
fun <T : Any> load(clazz: KClass<T>): T {
    return Mocks.s(clazz)
}

/**
 * 使用具体化类型和创建创建MockStub对象
 *
 * 便利方法，使用Kotlin的具体化泛型自动
 * 确定Stub创建的目标类类型。
 *
 * @param T 要为其创建Stub的类型（自动推断）
 * @return 公共方法返回模拟值的Stub实例
 * @throws IllegalArgumentException 如果类无法被子类化
 */
@JvmSynthetic
inline fun <reified T : Any> load(): T {
    return load(T::class)
}

/**
 * 创建MockStub对象（Java版本）
 *
 * 接受Java类对象并创建具有拦截公共方法的
 * Stub实例的Java兼容版本。
 *
 * @param clazz 要为其创建Stub的Java类
 * @return 公共方法返回模拟值的Stub实例
 * @throws IllegalArgumentException 如果类无法被子类化
 */
fun <T : Any> load(clazz: Class<T>): T {
    return load(clazz.kotlin)
}
