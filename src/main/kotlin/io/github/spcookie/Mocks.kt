package io.github.spcookie


/**
 * Mock4K - A Kotlin library for generating mock data
 *
 * @author spcookie
 * @since 1.0.0
 */
object Mocks {

    /**
     * The Random utility instance
     */
    @JvmField
    val Random = MockRandom

    /**
     * The Locale utility instance
     */
    @JvmField
    val Locale = LocaleManager

    /**
     * Type adapter manager for custom type conversion
     */
    @JvmField
    val TypeAdapter = TypeAdapter()

    /**
     * Container adapter manager for custom container type handling
     */
    @JvmField
    val ContainerAdapter = ContainerAdapter()

    /**
     * Singleton MockEngine instance to maintain state across calls
     */
    private val mockEngine = MockEngine()

    /**
     * BeanMockBridge instance
     */
    private val beanMockEngine = BeanMockBridge(mockEngine, TypeAdapter, ContainerAdapter)

    /**
     * Generate mock data based on template
     *
     * @param template The data template
     * @return Generated mock data
     */
    @JvmSynthetic
    internal fun g(template: Any): Any {
        return mockEngine.generate(template)!!
    }

    /**
     * Generate mock bean object
     *
     * @param clazz The class to mock
     * @param includePrivate Whether to mock private properties
     * @param includeStatic Whether to mock static properties
     * @param includeTransient Whether to mock transient properties
     * @param depth Maximum depth for recursive bean generation
     * @return Generated mock bean object
     */
    @JvmSynthetic
    internal fun <T : Any> bg(
        clazz: kotlin.reflect.KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return beanMockEngine.mockBean(clazz, includePrivate, includeStatic, includeTransient, depth)
    }

}

/**
 * Generate mock data based on template map
 *
 * @param template The data template as map
 * @return Generated mock data
 */
@Suppress("UNCHECKED_CAST")
fun mock(template: Map<String, *>): Map<String, *> {
    return Mocks.g(template as Any) as Map<String, *>
}

/**
 * Generate mock data based on template list
 *
 * @param template The data template as list
 * @return Generated mock data
 */
@Suppress("UNCHECKED_CAST")
fun mock(template: List<*>): List<*> {
    return Mocks.g(template as Any) as List<*>
}

/**
 * Generate mock data based on template string
 *
 * @param template The data template as string
 * @return Generated mock data
 */
fun mock(template: String): String {
    return Mocks.g(template as Any) as String
}

/**
 * Generate mock bean object
 *
 * @param clazz The class to mock
 * @param includePrivate Whether to mock private properties (default: null, uses annotation value)
 * @param includeStatic Whether to mock static properties (default: null, uses annotation value)
 * @param includeTransient Whether to mock transient properties (default: null, uses annotation value)
 * @param depth Maximum depth for recursive bean generation (default: null, uses annotation value or 3)
 * @return Generated mock bean object
 */
@JvmSynthetic
fun <T : Any> mock(
    clazz: kotlin.reflect.KClass<T>,
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null,
    depth: Int? = null
): T {
    return Mocks.bg(clazz, includePrivate, includeStatic, includeTransient, depth)
}

/**
 * Generate mock bean object with reified type
 *
 * @return Generated mock bean object
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

@JvmSynthetic
fun <T : Any> mock(
    clazz: kotlin.reflect.KClass<T>,
    config: BeanMockConfig? = null
): T {
    val includePrivate = config?.includePrivate
    val includeStatic = config?.includeStatic
    val includeTransient = config?.includeTransient
    val depth = config?.depth
    return mock(clazz, includePrivate, includeStatic, includeTransient, depth)
}

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
 * Generate mock bean object (Java-friendly version)
 *
 * @param clazz The Java class to mock
 * @param includePrivate Whether to mock private properties (default: null, uses annotation value)
 * @param includeStatic Whether to mock static properties (default: null, uses annotation value)
 * @param includeTransient Whether to mock transient properties (default: null, uses annotation value)
 * @param depth Maximum depth for recursive bean generation (default: null, uses annotation value or 3)
 * @return Generated mock bean object
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