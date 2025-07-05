package io.github.spcookie

import kotlin.reflect.full.memberFunctions

/**
 * Resolver for @placeholder syntax
 *
 * @author spcookie
 * @since 1.0.0
 */
internal class PlaceholderResolver {

    private val random = MockRandom

    private val placeholderPattern = Regex("@([a-zA-Z][a-zA-Z0-9]*(?:\\.[a-zA-Z0-9]+)*)(?:\\(([^)]*)\\))?")

    /**
     * Resolve placeholders in a string
     */
    fun resolve(template: String, context: ExecutionContext? = null): String {
        // Handle @placeholder syntax
        return placeholderPattern.findAll(template).fold(template) { acc, match ->
            val fullMatch = match.value
            val methodName = match.groupValues[1]
            val params = match.groupValues.getOrNull(2) ?: ""

            val resolved = resolvePlaceholder(methodName, params, fullMatch, context)
            acc.replace(fullMatch, resolved.toString())
        }
    }


    private fun resolvePlaceholder(
        methodName: String,
        params: String,
        placeholder: String,
        context: ExecutionContext?
    ): Any {
        return try {
            // First, try to resolve as property reference
            if (context != null) {
                val propertyValue = resolvePropertyReference(methodName, context)
                if (propertyValue != null) {
                    return propertyValue
                }
            }

            // If not a property reference, try built-in placeholders
            val lowercaseMethodName = methodName.lowercase()
            val result = if (params.isNotEmpty()) {
                val paramList = parseParams(params)
                callMethodWithParams(lowercaseMethodName, paramList, placeholder)
            } else {
                callMethod(lowercaseMethodName, placeholder)
            }
            result
        } catch (_: Exception) {
            placeholder
        }
    }

    /**
     * Resolve property reference (relative or absolute path)
     */
    private fun resolvePropertyReference(propertyPath: String, context: ExecutionContext): Any? {
        // Check if it's an absolute path (contains dots)
        return if (propertyPath.contains(".")) {
            // Absolute path reference
            context.getResolvedValueByAbsolutePath(propertyPath)
        } else {
            // Relative path reference (within current context)
            context.getResolvedValue(propertyPath)
        }
    }

    private fun parseParams(params: String): List<Any> {
        if (params.isBlank()) return emptyList()

        return params.split(",").map { param ->
            val trimmed = param.trim()
            when {
                trimmed.startsWith('"') && trimmed.endsWith('"') -> trimmed.substring(1, trimmed.length - 1)
                trimmed.startsWith('\'') && trimmed.endsWith('\'') -> trimmed.substring(1, trimmed.length - 1)
                trimmed.toIntOrNull() != null -> trimmed.toInt()
                trimmed.toDoubleOrNull() != null -> trimmed.toDouble()
                trimmed == "true" -> true
                trimmed == "false" -> false
                else -> trimmed
            }
        }
    }


    private fun callMethod(methodName: String, placeholder: String): Any {
        val method = random::class.memberFunctions.find { it.name.lowercase() == methodName.lowercase() }
        return if (method != null && method.parameters.size == 1) {
            method.call(random) ?: placeholder
        } else {
            placeholder
        }
    }

    private fun callMethodWithParams(methodName: String, params: List<Any>, placeholder: String): Any {
        val methods = random::class.memberFunctions.filter { it.name.lowercase() == methodName.lowercase() }

        for (method in methods) {
            if (method.parameters.size == params.size + 1) {
                return try {
                    method.call(random, *params.toTypedArray()) ?: placeholder
                } catch (_: Exception) {
                    continue
                }
            }
        }

        return placeholder
    }
}