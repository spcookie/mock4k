package io.github.spcookie

import kotlin.reflect.KClass

/**
 * Type adapter manager for custom type conversion
 *
 * @author spcookie
 * @since 1.2.0
 */
class TypeAdapter {

    private val adapters = mutableMapOf<KClass<*>, (Any?) -> Any?>()

    /**
     * Register a custom type adapter
     */
    fun <T : Any> register(type: KClass<T>, adapter: (Any?) -> T?) {
        adapters[type] = adapter as (Any?) -> Any?
    }

    /**
     * Get type adapter for a specific type
     */
    internal fun get(type: KClass<*>): ((Any?) -> Any?)? {
        return adapters[type]
    }

    /**
     * Get all registered adapters (for internal use)
     */
    internal fun getAll(): Map<KClass<*>, (Any?) -> Any?> {
        return adapters.toMap()
    }
}