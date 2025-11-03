package io.github.spcookie

class MockRandomExtend(val source: MockRandom) {

    /**
     * 扩展占位符生成器的注册表
     */
    private val extended = mutableMapOf<String, () -> Any>()

    /**
     * 带参数的扩展占位符生成器的注册表
     */
    private val extendedWithParams = mutableMapOf<String, (List<Any>) -> Any>()

    // Extension methods

    /**
     * Extend MockRandom with custom placeholder generators
     *
     * @param placeholders Map of placeholder name to generator function
     * @return MockRandom instance for chaining
     */
    fun extend(placeholders: Map<String, () -> Any>): MockRandom {
        synchronized(this) {
            placeholders.forEach { (key, value) ->
                extended[key.lowercase()] = value
            }
        }
        return source
    }

    /**
     * Extend MockRandom with custom placeholder generators that accept parameters
     *
     * @param placeholders Map of placeholder name to generator function with parameters
     * @return MockRandom instance for chaining
     */
    fun extendWithParams(placeholders: Map<String, (List<Any>) -> Any>): MockRandom {
        synchronized(this) {
            placeholders.forEach { (key, value) ->
                extendedWithParams[key.lowercase()] = value
            }
        }
        return source
    }

    /**
     * Register a single custom placeholder generator
     *
     * @param name Placeholder name
     * @param generator Generator function
     * @return MockRandom instance for chaining
     */
    fun extend(name: String, generator: () -> Any): MockRandom {
        synchronized(this) {
            extended[name.lowercase()] = generator
        }
        return source
    }

    /**
     * Register a single custom placeholder generator with parameters
     *
     * @param name Placeholder name
     * @param generator Generator function that accepts parameters
     * @return MockRandom instance for chaining
     */
    fun extendWithParams(name: String, generator: (List<Any>) -> Any): MockRandom {
        synchronized(this) {
            extendedWithParams[name.lowercase()] = generator
        }
        return source
    }

    /**
     * Get extended placeholder generator by name
     *
     * @param name Placeholder name
     * @return Generator function or null if not found
     */
    internal fun getExtended(name: String): (() -> Any)? {
        return synchronized(this) {
            extended[name.lowercase()]
        }
    }

    /**
     * Get extended placeholder generator with parameters by name
     *
     * @param name Placeholder name
     * @return Generator function or null if not found
     */
    internal fun getExtendedWithParams(name: String): ((List<Any>) -> Any)? {
        return synchronized(this) {
            extendedWithParams[name.lowercase()]
        }
    }

    /**
     * Check if an extended placeholder exists
     *
     * @param name Placeholder name
     * @return true if placeholder exists, false otherwise
     */
    fun hasExtended(name: String): Boolean {
        return synchronized(this) {
            val lowerName = name.lowercase()
            extended.containsKey(lowerName) || extendedWithParams.containsKey(lowerName)
        }
    }

    /**
     * Remove an extended placeholder
     *
     * @param name Placeholder name
     * @return MockRandom instance for chaining
     */
    fun removeExtended(name: String): MockRandom {
        synchronized(this) {
            val lowerName = name.lowercase()
            extended.remove(lowerName)
            extendedWithParams.remove(lowerName)
        }
        return source
    }

    /**
     * Clear all extended placeholders
     *
     * @return MockRandom instance for chaining
     */
    fun clearExtended(): MockRandom {
        synchronized(this) {
            extended.clear()
            extendedWithParams.clear()
        }
        return source
    }

    /**
     * Get all registered extended placeholder names
     *
     * @return Set of placeholder names
     */
    fun getExtendedNames(): Set<String> {
        return synchronized(this) {
            extended.keys + extendedWithParams.keys
        }
    }

}