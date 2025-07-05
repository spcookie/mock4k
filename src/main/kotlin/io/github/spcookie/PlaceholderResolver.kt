package io.github.spcookie

import kotlin.reflect.full.memberFunctions

/**
 * Resolver for @placeholder syntax
 *
 * @author spcookie
 * @since 1.0.0
 */
internal class PlaceholderResolver {

    companion object {
        /**
         * Pattern for matching placeholder syntax: @methodName or @methodName(params)
         * Groups: 1 = method name, 2 = parameters (optional)
         */
        private val PLACEHOLDER_PATTERN = Regex("@([a-zA-Z][a-zA-Z0-9]*(?:\\.[a-zA-Z0-9]+)*)(?:\\(([^)]*)\\))?")

        /**
         * Pattern for matching a single placeholder that spans the entire string
         * Used to determine if we should preserve the original type or convert to string
         */
        private val SINGLE_PLACEHOLDER_PATTERN =
            Regex("^@([a-zA-Z][a-zA-Z0-9]*(?:\\.[a-zA-Z0-9]+)*)(?:\\(([^)]*)\\))?$")
    }

    private val random = MockRandom

    /**
     * Resolve placeholders in a string
     */
    fun resolve(template: String, context: ExecutionContext? = null): String {
        // Handle @placeholder syntax
        return PLACEHOLDER_PATTERN.findAll(template).fold(template) { acc, match ->
            val fullMatch = match.value
            val methodName = match.groupValues[1]
            val params = match.groupValues.getOrNull(2) ?: ""

            val resolved = resolvePlaceholder(methodName, params, fullMatch, context)
            acc.replace(fullMatch, resolved.toString())
        }
    }

    /**
     * Resolve string template with smart type detection
     * Determines whether to return original type (for single placeholder) or string (for multiple parts)
     */
    fun resolveStringTemplate(template: String, context: ExecutionContext? = null): Any {
        // Check if the template is a single placeholder
        val match = SINGLE_PLACEHOLDER_PATTERN.matchEntire(template.trim())

        return if (match != null) {
            // Single placeholder - preserve original type
            resolveSinglePlaceholder(template, context)
        } else {
            // Template with multiple parts - return as string
            resolve(template, context)
        }
    }

    /**
     * Resolve a single placeholder while preserving its original type
     */
    fun resolveSinglePlaceholder(template: String, context: ExecutionContext? = null): Any {
        val match = PLACEHOLDER_PATTERN.find(template)
        if (match != null) {
            val methodName = match.groupValues[1]
            val params = match.groupValues.getOrNull(2) ?: ""
            return resolvePlaceholder(methodName, params, match.value, context)
        }
        return template
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

            // Second, try custom placeholders
            val lowercaseMethodName = methodName.lowercase()
            val customResult = resolveExtendedPlaceholder(lowercaseMethodName, params, placeholder)
            if (customResult != placeholder) {
                return customResult
            }

            // Finally, try built-in placeholders
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

    /**
     * Resolve custom placeholder
     */
    private fun resolveExtendedPlaceholder(methodName: String, params: String, placeholder: String): Any {
        return try {
            val lowerMethodName = methodName.lowercase()
            if (params.isNotEmpty()) {
                // Try custom placeholder with parameters
                val customGenerator = random.getExtendedWithParams(lowerMethodName)
                if (customGenerator != null) {
                    val paramList = parseParams(params)
                    return customGenerator(paramList)
                }
            } else {
                // Try custom placeholder without parameters
                val customGenerator = random.getExtended(lowerMethodName)
                if (customGenerator != null) {
                    return customGenerator()
                }
            }
            placeholder
        } catch (_: Exception) {
            placeholder
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