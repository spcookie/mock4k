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
    val Random = MockRandom

    /**
     * The Locale utility instance
     */
    val Locale = LocaleManager

    /**
     * Type adapter manager for custom type conversion
     */
    val TypeAdapter = TypeAdapter()

    /**
     * Singleton MockEngine instance to maintain state across calls
     */
    internal val mockEngine = MockEngine()

    /**
     * BeanMockBridge instance
     */
    internal val beanMockEngine = BeanMockBridge(mockEngine, TypeAdapter)

    /**
     * Generate mock data based on template
     *
     * @param template The data template
     * @return Generated mock data
     */
    fun g(template: Any): Any {
        return mockEngine.generate(template)!!
    }

    /**
     * Generate mock bean object
     *
     * @param clazz The class to mock
     * @param includePrivate Whether to mock private properties
     * @param includeStatic Whether to mock static properties
     * @param includeTransient Whether to mock transient properties
     * @return Generated mock bean object
     */
    fun <T : Any> bg(
        clazz: kotlin.reflect.KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null
    ): T {
        return beanMockEngine.mockBean(clazz, includePrivate, includeStatic, includeTransient)
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
fun mock(template: String): Any {
    return Mocks.g(template as Any)
}

/**
 * Generate mock bean object
 *
 * @param clazz The class to mock
 * @param includePrivate Whether to mock private properties (default: null, uses annotation value)
 * @param includeStatic Whether to mock static properties (default: null, uses annotation value)
 * @param includeTransient Whether to mock transient properties (default: null, uses annotation value)
 * @return Generated mock bean object
 */
fun <T : Any> mock(
    clazz: kotlin.reflect.KClass<T>,
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null
): T {
    return Mocks.bg(clazz, includePrivate, includeStatic, includeTransient)
}

/**
 * Generate mock bean object (Java-friendly version)
 *
 * @param clazz The Java class to mock
 * @param includePrivate Whether to mock private properties (default: null, uses annotation value)
 * @param includeStatic Whether to mock static properties (default: null, uses annotation value)
 * @param includeTransient Whether to mock transient properties (default: null, uses annotation value)
 * @return Generated mock bean object
 */
fun <T : Any> mock(
    clazz: Class<T>,
    includePrivate: Boolean? = null,
    includeStatic: Boolean? = null,
    includeTransient: Boolean? = null
): T {
    return mock(clazz.kotlin, includePrivate, includeStatic, includeTransient)
}