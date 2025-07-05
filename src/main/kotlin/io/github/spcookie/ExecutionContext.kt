package io.github.spcookie

/**
 * Execution context for tracking instance, path information and increment counters
 */
internal data class ExecutionContext(
    val path: String = "",
    val incrementCounters: MutableMap<String, Int> = mutableMapOf()
) {
    fun createChildContext(propertyName: String): ExecutionContext {
        val newPath = if (path.isEmpty()) propertyName else "$path.$propertyName"
        return ExecutionContext(newPath, incrementCounters)
    }

    fun createCounterKey(propertyName: String): String {
        // Include path to ensure independent counters at different levels
        return if (path.isEmpty()) propertyName else "$path.$propertyName"
    }

    fun getOrPutCounter(key: String, defaultValue: Int): Int {
        return incrementCounters.getOrPut(key) { defaultValue }
    }


}