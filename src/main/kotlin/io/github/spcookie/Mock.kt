package io.github.spcookie

/**
 * Mock4K - A Kotlin library for generating mock data
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
    private val mockEngine = MockEngine()

    /**
     * Generate mock data based on template
     *
     * @param template The data template
     * @return Generated mock data
     */
    fun mock(template: Any): Any {
        return mockEngine.generate(template)
    }

    /**
     * Generate mock data based on template map
     *
     * @param template The data template as map
     * @return Generated mock data
     */
    @Suppress("UNCHECKED_CAST")
    fun mock(template: Map<String, Any>): Map<String, Any> {
        return mockEngine.generate(template) as Map<String, Any>
    }

}