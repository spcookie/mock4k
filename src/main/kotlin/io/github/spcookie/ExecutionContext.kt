package io.github.spcookie

/**
 * 用于跟踪实例、路径信息、递增计数器和已解析数据的执行上下文
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
        // 包含路径以确保不同级别的独立计数器
        return if (path.isEmpty()) propertyName else "$path.$propertyName"
    }

    fun getOrPutCounter(key: String, defaultValue: Int): Int {
        return incrementCounters.getOrPut(key) { defaultValue }
    }

    /**
     * 在本地和根上下文中存储已解析的值
     */
    fun storeResolvedValue(key: String, value: Any?) {
        val fullPath = if (path.isEmpty()) key else "$path.$key"
        dataContext[key] = value
        rootDataContext[fullPath] = value
    }

    /**
     * 通过相对路径获取已解析的值(在当前上下文内)
     */
    fun getResolvedValue(key: String): Any? {
        return dataContext[key]
    }

    /**
     * 通过绝对路径获取已解析的值(从根开始)
     */
    fun getResolvedValueByAbsolutePath(absolutePath: String): Any? {
        return rootDataContext[absolutePath]
    }

    /**
     * 获取所有可用的键用于调试
     */
    fun getAllKeys(): Set<String> {
        return rootDataContext.keys
    }
}