package io.github.spcookie

/**
 * Mock annotation container for bean and property configurations
 *
 * @author spcookie
 * @since 1.2.0
 */
@Target()
@Retention(AnnotationRetention.RUNTIME)
annotation class Mock {
    
    /**
     * Configuration for bean mock generation
     * 
     * @param includePrivate Whether to include private properties in mock generation
     * @param includeStatic Whether to include static properties in mock generation
     * @param includeTransient Whether to include transient properties in mock generation
     * 
     * @author spcookie
     * @since 1.2.0
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Bean(
        val includePrivate: Boolean = false,
        val includeStatic: Boolean = false,
        val includeTransient: Boolean = false
    )
    
    /**
     * Mock annotation for bean properties
     * 
     * @param rule The rule to apply for mock generation
     * @param placeholder The placeholder to use for mock generation
     * @param enabled Whether to enable mock for this property (default: true)
     * 
     * @author spcookie
     * @since 1.2.0
     */
    @Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Property(
        val rule: Rule = Rule(),
        val placeholder: Placeholder = Placeholder(),
        val enabled: Boolean = true
    ) {
        /**
         * Placeholder annotation for specifying placeholders
         *
         * @param value The placeholder expression to use
         *
         * @author spcookie
         * @since 1.2.0
         */
        @Target()
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Placeholder(
            val value: String = ""
        )

        /**
         * Rule annotation for specifying generation rules
         *
         * @param count Fixed count for generation
         * @param min Minimum value for range generation
         * @param max Maximum value for range generation
         * @param step Step value for increment generation
         * @param dmin Minimum decimal places
         * @param dmax Maximum decimal places
         * @param dcount Fixed decimal places
         *
         * @author spcookie
         * @since 1.2.0
         */
        @Target()
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Rule(
            val count: Int = -1,
            val min: Int = -1,
            val max: Int = -1,
            val step: Int = -1,
            val dmin: Int = -1,
            val dmax: Int = -1,
            val dcount: Int = -1,
        )
    }
}

/**
 * Mock4K - A Kotlin library for generating mock data
 *
 * @author spcookie
 * @since 1.0.0
 */
object MockObject {

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
     * BeanMockEngine instance
     */
    internal val beanMockEngine = BeanMockEngine(mockEngine, TypeAdapter)

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
    return MockObject.g(template as Any) as Map<String, *>
}

/**
 * Generate mock data based on template list
 *
 * @param template The data template as list
 * @return Generated mock data
 */
@Suppress("UNCHECKED_CAST")
fun mock(template: List<*>): List<*> {
    return MockObject.g(template as Any) as List<*>
}

/**
 * Generate mock data based on template string
 *
 * @param template The data template as string
 * @return Generated mock data
 */
fun mock(template: String): Any {
    return MockObject.g(template as Any)
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
    return MockObject.bg(clazz, includePrivate, includeStatic, includeTransient)
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