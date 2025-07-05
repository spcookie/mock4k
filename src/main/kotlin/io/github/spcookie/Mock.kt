package io.github.spcookie

/**
 * Mock4K - A Kotlin library for generating mock data
 *
 * @author spcookie
 * @since 1.0.0
 */
object Mock {

    /**
     * The Random utility instance
     */
    val Random = MockRandom

    /**
     * The Locale utility instance
     */
    val Locale = LocaleManager

    /**
     * Singleton MockEngine instance to maintain state across calls
     */
    internal val mockEngine = MockEngine()

    /**
     * Generate mock data based on template
     *
     * @param template The data template
     * @return Generated mock data
     */
    fun g(template: Any): Any {
        return mockEngine.generate(template)!!
    }

}

/**
 * Generate mock data based on template map
 *
 * @param template The data template as map
 * @return Generated mock data
 */
@Suppress("UNCHECKED_CAST")
fun Mock.mock(template: Map<String, *>): Map<String, *> {
    return g(template as Any) as Map<String, *>
}

/**
 * Generate mock data based on template list
 *
 * @param template The data template as map
 * @return Generated mock data
 */
@Suppress("UNCHECKED_CAST")
fun Mock.mock(template: List<*>): List<*> {
    return g(template as Any) as List<*>
}

/**
 * Generate mock data based on template string
 *
 * @param template The data template as map
 * @return Generated mock data
 */
@Suppress("UNCHECKED_CAST")
fun Mock.mock(template: String): String {
    return g(template as Any) as String
}