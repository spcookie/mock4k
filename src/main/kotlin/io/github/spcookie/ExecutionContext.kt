package io.github.spcookie

/**
 * Execution context for tracking instance, path information, increment counters and resolved data
 *
 * @author spcookie
 * @since 1.0.0
 */
internal data class ExecutionContext(
    val path: String = "",
    val incrementCounters: MutableMap<String, Int> = mutableMapOf(),
    val dataContext: MutableMap<String, Any?> = mutableMapOf(),
    val rootDataContext: MutableMap<String, Any?> = mutableMapOf()
) {
    fun createChildContext(propertyName: String): ExecutionContext {
        val newPath = if (path.isEmpty()) propertyName else "$path.$propertyName"
        return ExecutionContext(newPath, incrementCounters, dataContext, rootDataContext)
    }

    fun createCounterKey(propertyName: String): String {
        // Include path to ensure independent counters at different levels
        return if (path.isEmpty()) propertyName else "$path.$propertyName"
    }

    fun getOrPutCounter(key: String, defaultValue: Int): Int {
        return incrementCounters.getOrPut(key) { defaultValue }
    }

    /**
     * Store resolved value in both local and root context
     */
    fun storeResolvedValue(key: String, value: Any?) {
        val fullPath = if (path.isEmpty()) key else "$path.$key"
        dataContext[key] = value
        rootDataContext[fullPath] = value
    }

    /**
     * Get resolved value by relative path (within current context)
     */
    fun getResolvedValue(key: String): Any? {
        return dataContext[key]
    }

    /**
     * Get resolved value by absolute path (from root)
     */
    fun getResolvedValueByAbsolutePath(absolutePath: String): Any? {
        return rootDataContext[absolutePath]
    }

    /**
     * Get all available keys for debugging
     */
    fun getAllKeys(): Set<String> {
        return rootDataContext.keys
    }
}