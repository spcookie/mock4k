package com.mock4k

/**
 * Mock4K - A Kotlin library for generating mock data
 * Inspired by Mock.js
 */
object Mock {
    
    /**
     * The Random utility instance
     */
    val Random = MockRandom
    
    private val engine = MockEngine()
    
    /**
     * Generate mock data based on template
     * 
     * @param template The data template
     * @return Generated mock data
     */
    fun mock(template: Any): Any {
        return engine.generate(template)
    }
    
    /**
     * Generate mock data based on template map
     * 
     * @param template The data template as map
     * @return Generated mock data
     */
    fun mock(template: Map<String, Any>): Map<String, Any> {
        return engine.generate(template) as Map<String, Any>
    }
    
    /**
     * Generate mock data based on template with custom random instance
     * 
     * @param template The data template
     * @param random Custom random instance
     * @return Generated mock data
     */
    fun mock(template: Any, random: MockRandom): Any {
        val customEngine = MockEngine(random)
        return customEngine.generate(template)
    }
}