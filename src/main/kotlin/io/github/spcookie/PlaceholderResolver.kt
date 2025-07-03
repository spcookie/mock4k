package io.github.spcookie

import kotlin.reflect.full.memberFunctions

/**
 * Resolver for @placeholder syntax
 */
internal class PlaceholderResolver {

    private val random = MockRandom

    private val placeholderPattern = Regex("@([a-z|A-Z]+)(?:\\(([^)]*)\\))?")

    /**
     * Resolve placeholders in a string
     */
    fun resolve(template: String): String {
        var result = template

        placeholderPattern.findAll(template).forEach { match ->
            val placeholder = match.value
            val methodName = match.groupValues[1].lowercase()
            val params = match.groupValues[2]

            val resolvedValue = resolvePlaceholder(methodName, params, placeholder)
            result = result.replace(placeholder, resolvedValue.toString())
        }

        return result
    }

    private fun resolvePlaceholder(methodName: String, params: String, placeholder: String): Any {
        return try {
            if (params.isNotEmpty()) {
                val paramList = parseParams(params)
                callMethodWithParams(methodName, paramList, placeholder)
            } else {
                callMethod(methodName, placeholder)
            }
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
                trimmed.startsWith("PT.") -> parsePhoneType(trimmed) ?: trimmed
                else -> trimmed
            }
        }
    }

    private fun parsePhoneType(value: String): MockRandom.PhoneType? {
        return when (value) {
            "PT.M", "PT.MOBILE" -> MockRandom.PhoneType.MOBILE
            "PT.L", "PT.LANDLINE" -> MockRandom.PhoneType.LANDLINE
            "PT.TF", "PT.TOLL_FREE" -> MockRandom.PhoneType.TOLL_FREE
            "PT.P", "PT.PREMIUM" -> MockRandom.PhoneType.PREMIUM
            else -> null
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