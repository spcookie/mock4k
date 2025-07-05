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

        // First, handle regex patterns
        result = resolveRegexPatterns(result)

        // Then handle @placeholder syntax
        placeholderPattern.findAll(result).forEach { match ->
            val placeholder = match.value
            val methodName = match.groupValues[1].lowercase()
            val params = match.groupValues[2]

            val resolvedValue = resolvePlaceholder(methodName, params, placeholder)
            result = result.replace(placeholder, resolvedValue.toString())
        }

        return result
    }
    
    /**
     * Resolve regex patterns in format /pattern/
     */
    private fun resolveRegexPatterns(template: String): String {
        val regexPattern = Regex("/(.+?)/")
        var result = template
        
        regexPattern.findAll(template).forEach { match ->
            val fullMatch = match.value
            val pattern = match.groupValues[1]
            
            try {
                val regex = Regex(pattern)
                val generatedValue = generateStringFromRegex(regex)
                result = result.replace(fullMatch, generatedValue)
            } catch (_: Exception) {
                // If regex is invalid, keep the original string
                // result remains unchanged
            }
        }
        
        return result
     }
     
    /**
     * Simple regex string generator for common patterns
     */
    private fun generateStringFromRegex(regex: Regex): String {
        val pattern = regex.pattern
        
        // Handle simple character classes and quantifiers
        return when {
            // Email pattern
            pattern.contains("@") && pattern.contains("\\.") -> {
                val username = generateRandomString(5, 10, "abcdefghijklmnopqrstuvwxyz0123456789")
                val domain = generateRandomString(3, 8, "abcdefghijklmnopqrstuvwxyz")
                val tld = listOf("com", "org", "net", "edu").random()
                "$username@$domain.$tld"
            }
            
            // Complex patterns with multiple parts (e.g., [A-Z][a-z]{4,9})
            pattern.contains("[A-Z]") && pattern.contains("[a-z]") -> {
                generateComplexPattern(pattern)
            }
            
            // Phone number patterns
            pattern.contains("\\d") -> {
                val length = extractQuantifier(pattern, "\\d")
                generateRandomString(length.first, length.second, "0123456789")
            }
            
            // Word patterns
            pattern.contains("\\w") -> {
                val length = extractQuantifier(pattern, "\\w")
                generateRandomString(length.first, length.second, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_")
            }
            
            // Character classes
            pattern.contains("[a-z]") -> {
                val length = extractQuantifier(pattern, "\\[a-z\\]")
                generateRandomString(length.first, length.second, "abcdefghijklmnopqrstuvwxyz")
            }
            pattern.contains("[A-Z]") -> {
                val length = extractQuantifier(pattern, "\\[A-Z\\]")
                generateRandomString(length.first, length.second, "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            }
            pattern.contains("[0-9]") -> {
                val length = extractQuantifier(pattern, "\\[0-9\\]")
                generateRandomString(length.first, length.second, "0123456789")
            }
            
            // Default: generate a simple alphanumeric string
            else -> generateRandomString(5, 10, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
        }
    }
    
    /**
     * Generate string for complex patterns with multiple character classes
     */
    private fun generateComplexPattern(pattern: String): String {
        val result = StringBuilder()
        
        // Parse pattern parts: [A-Z], [a-z]{4,9}, etc.
        val partRegex = Regex("(\\[A-Z\\]|\\[a-z\\]|\\[0-9\\]|\\\\d|\\\\w)(?:\\{(\\d+)(?:,(\\d+))?\\})?")
        val matches = partRegex.findAll(pattern)
        
        for (match in matches) {
            val element = match.groupValues[1]
            val minStr = match.groupValues.getOrNull(2)
            val maxStr = match.groupValues.getOrNull(3)
            
            val min = minStr?.toIntOrNull() ?: 1
            val max = maxStr?.toIntOrNull() ?: min
            
            val charset = when (element) {
                "[A-Z]" -> "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                "[a-z]" -> "abcdefghijklmnopqrstuvwxyz"
                "[0-9]", "\\d" -> "0123456789"
                "\\w" -> "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_"
                else -> "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            }
            
            result.append(generateRandomString(min, max, charset))
        }
        
        return if (result.isNotEmpty()) result.toString() else generateRandomString(5, 10, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
    }
    
    /**
     * Extract quantifier from pattern and return min/max length
     */
    private fun extractQuantifier(pattern: String, elementPattern: String): Pair<Int, Int> {
        // Look for quantifiers after the element pattern
        // Handle both escaped and unescaped patterns
        val escapedPattern = elementPattern.replace("\\", "\\\\")
        val quantifierRegex = Regex("$escapedPattern\\{(\\d+)(?:,(\\d+))?\\}")
        val match = quantifierRegex.find(pattern)
        
        return if (match != null) {
            val min = match.groupValues[1].toInt()
            val max = match.groupValues.getOrNull(2)?.toIntOrNull() ?: min
            Pair(min, max)
        } else {
            // Try to find any {n} or {n,m} pattern in the string
            val anyQuantifierRegex = Regex("\\{(\\d+)(?:,(\\d+))?\\}")
            val anyMatch = anyQuantifierRegex.find(pattern)
            if (anyMatch != null) {
                val min = anyMatch.groupValues[1].toInt()
                val max = anyMatch.groupValues.getOrNull(2)?.toIntOrNull() ?: min
                Pair(min, max)
            } else {
                // Default length if no quantifier found
                Pair(5, 10)
            }
        }
    }
    
    /**
     * Generate a random string with specified length range and character set
     */
    private fun generateRandomString(minLength: Int, maxLength: Int, charset: String): String {
        val length = random.integer(minLength, maxLength)
        return (1..length)
            .map { charset.random() }
            .joinToString("")
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