package io.github.spcookie

import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


/**
 * Mock4K - A Kotlin library for generating mock data
 *
 * This is the main entry point for the Mock4K library, providing utilities
 * for generating mock data including random values, locale-specific data,
 * and complex bean objects with customizable configurations.
 *
 * @author spcookie
 * @since 1.0.0
 */
object Mocks {

    /**
     * The Random utility instance for generating random values
     *
     * Provides access to various random data generation methods including
     * numbers, strings, dates, and other primitive types.
     */
    @JvmField
    val Random = MockRandom

    /**
     * The Locale utility instance for locale-specific data generation
     *
     * Manages locale settings and provides locale-aware data generation
     * such as names, addresses, and other region-specific information.
     */
    @JvmField
    val Locale = LocaleManager

    /**
     * Type adapter manager for custom type conversion
     *
     * Allows registration of custom type adapters to handle specific
     * data types during mock data generation process.
     */
    @JvmField
    val TypeAdapter = TypeAdapter()

    /**
     * Container adapter manager for custom container type handling
     *
     * Manages adapters for container types like List, Set, Map, etc.
     * to customize how collections are populated with mock data.
     */
    @JvmField
    val ContainerAdapter = ContainerAdapter()

    /**
     * Singleton MockEngine instance to maintain state across calls
     *
     * The core engine responsible for processing templates and generating
     * mock data based on various input formats and configurations.
     */
    private val mockEngine = MockEngine()

    /**
     * BeanMockBridge instance for handling bean object generation
     *
     * Bridges the gap between the core mock engine and bean-specific
     * generation logic, integrating type and container adapters.
     */
    private val beanMockEngine = BeanMockBridge(mockEngine, TypeAdapter, ContainerAdapter)

    /**
     * Generate mock data based on template (internal shorthand method)
     *
     * This is an internal utility method that delegates to the mock engine
     * for generating data based on the provided template.
     *
     * @param template The data template (can be Map, List, or other supported types)
     * @return Generated mock data matching the template structure
     * @throws IllegalArgumentException if template format is not supported
     */
    @JvmSynthetic
    internal fun g(template: Any): Any {
        return mockEngine.generate(template)!!
    }

    /**
     * Generate mock bean object (internal shorthand method)
     *
     * This is an internal utility method that delegates to the bean mock engine
     * for generating complex object instances with configurable property inclusion.
     *
     * @param clazz The Kotlin class to mock
     * @param includePrivate Whether to mock private properties (null uses annotation/default)
     * @param includeStatic Whether to mock static properties (null uses annotation/default)
     * @param includeTransient Whether to mock transient properties (null uses annotation/default)
     * @param depth Maximum depth for recursive bean generation (null uses annotation/default of 3)
     * @return Generated mock bean object with populated properties
     * @throws IllegalArgumentException if class cannot be instantiated
     */
    @JvmSynthetic
    internal fun <T : Any> bg(
        clazz: KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        return beanMockEngine.mockBean(clazz, includePrivate, includeStatic, includeTransient, depth)
    }


    /**
     * Create a mock stub object using ByteBuddy instrumentation (Kotlin version)
     *
     * This method creates a dynamic subclass of the given class and intercepts
     * all public non-void/non-Unit methods to return mock values.
     *
     * @param clazz The Kotlin class to create a stub for
     * @return A stubbed instance where public methods return mock values
     * @throws IllegalArgumentException if the class cannot be subclassed
     */
    internal fun <T : Any> s(clazz: KClass<T>): T {
        return MethodMockStub.make(clazz).createInstance()
    }

}

/**
 * Generate mock data based on template map
 *
 * Creates mock data by processing a map template where keys represent
 * field names and values represent the data generation rules or patterns.
 *
 * @param template The data template as map with string keys and value patterns
 * @return Generated mock data as a map with the same structure
 * @throws IllegalArgumentException if template contains unsupported value types
 */
@Suppress("UNCHECKED_CAST")
fun mock(template: Map<String, *>): Map<String, *> {
    return Mocks.g(template as Any) as Map<String, *>
}

/**
 * Generate mock data based on template list
 *
 * Creates mock data by processing a list template where each element
 * represents a data generation rule or pattern to be applied.
 *
 * @param template The data template as list containing generation patterns
 * @return Generated mock data as a list with corresponding mock values
 * @throws IllegalArgumentException if template contains unsupported element types
 */
@Suppress("UNCHECKED_CAST")
fun mock(template: List<*>): List<*> {
    return Mocks.g(template as Any) as List<*>
}

/**
 * Generate mock data based on JSON template string
 *
 * Parses a JSON template string and generates mock data according to the
 * template structure, then returns the result as a JSON string.
 *
 * @param template The JSON template string containing generation patterns
 * @return Generated mock data as a JSON string
 * @throws IllegalArgumentException if the template is not valid JSON
 */
fun mock(template: String): String {
    val parse = Mson.parse(template)
    return parse?.let { Mson.stringify(Mocks.g(parse)) }
        ?: throw IllegalArgumentException("Invalid json template")
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
    clazz: KClass<T>,
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
 * Convenience method that uses Kotlin's reified generics to automatically
 * determine the target class type, eliminating the need to pass the class explicitly.
 *
 * @param T The type to generate (automatically inferred)
 * @param includePrivate Whether to mock private properties (default: null, uses annotation value)
 * @param includeStatic Whether to mock static properties (default: null, uses annotation value)
 * @param includeTransient Whether to mock transient properties (default: null, uses annotation value)
 * @param depth Maximum depth for recursive bean generation (default: null, uses annotation value or 3)
 * @return Generated mock bean object of type T
 * @throws IllegalArgumentException if type T cannot be instantiated
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
 * Generate mock bean object using configuration object
 *
 * Alternative method that accepts a configuration object instead of individual
 * parameters, providing a more structured approach to bean generation settings.
 *
 * @param clazz The Kotlin class to mock
 * @param config Configuration object containing all generation settings (nullable)
 * @return Generated mock bean object
 * @throws IllegalArgumentException if class cannot be instantiated
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
 * Generate mock bean object with reified type using configuration object
 *
 * Combines the convenience of reified generics with configuration object approach,
 * providing the most flexible and type-safe way to generate mock beans.
 *
 * @param T The type to generate (automatically inferred)
 * @param config Configuration object containing all generation settings (nullable)
 * @return Generated mock bean object of type T
 * @throws IllegalArgumentException if type T cannot be instantiated
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

/**
 * Generate mock bean object using Java Class and configuration object
 *
 * Java-friendly version that accepts a configuration object, providing
 * a structured approach for Java developers to configure bean generation.
 *
 * @param clazz The Java class to mock
 * @param config Configuration object containing all generation settings (nullable)
 * @return Generated mock bean object
 * @throws IllegalArgumentException if class cannot be instantiated
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
 * Create a mock stub object using ByteBuddy instrumentation
 *
 * Creates a dynamic subclass of the given class and intercepts all public
 * non-void/non-Unit methods to return mock values. This is useful for creating
 * test doubles where you need actual method calls to return mock data.
 *
 * @param clazz The Kotlin class to create a stub for
 * @return A stubbed instance where public methods return mock values
 * @throws IllegalArgumentException if the class cannot be subclassed
 */
@JvmSynthetic
fun <T : Any> load(clazz: KClass<T>): T {
    return Mocks.s(clazz)
}

/**
 * Create a mock stub object using ByteBuddy instrumentation with reified type
 *
 * Convenience method that uses Kotlin's reified generics to automatically
 * determine the target class type for stub creation.
 *
 * @param T The type to create a stub for (automatically inferred)
 * @return A stubbed instance where public methods return mock values
 * @throws IllegalArgumentException if the class cannot be subclassed
 */
@JvmSynthetic
inline fun <reified T : Any> load(): T {
    return load(T::class)
}

/**
 * Create a mock stub object using ByteBuddy instrumentation (Java-friendly version)
 *
 * Java-compatible version that accepts a Java Class object and creates
 * a stubbed instance with intercepted public methods.
 *
 * @param clazz The Java class to create a stub for
 * @return A stubbed instance where public methods return mock values
 * @throws IllegalArgumentException if the class cannot be subclassed
 */
fun <T : Any> load(clazz: Class<T>): T {
    return load(clazz.kotlin)
}
