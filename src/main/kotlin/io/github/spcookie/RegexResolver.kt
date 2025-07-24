package io.github.spcookie

/**
 * 用于解析 /pattern/ 格式的正则表达式模式的解析器
 *
 * @author spcookie
 * @since 1.0.0
 */
internal class RegexResolver(
    private val random: MockRandom
) {

    /**
     * 解析 /pattern/ 格式的正则表达式模式
     */
    fun resolveRegexPatterns(template: String): String {
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
                // 如果正则表达式无效，则保留原始字符串
                // 结果保持不变
            }
        }

        return result
    }

    /**
     * 用于常见模式的简单正则表达式字符串生成器
     */
    private fun generateStringFromRegex(regex: Regex): String {
        val pattern = regex.pattern

        // 处理简单的字符类和量词
        return when {
            // 电子邮件模式
            pattern.contains("@") && pattern.contains("\\.") -> {
                val username = generateRandomString(5, 10, "abcdefghijklmnopqrstuvwxyz0123456789")
                val domain = generateRandomString(3, 8, "abcdefghijklmnopqrstuvwxyz")
                val tld = listOf("com", "org", "net", "edu").random()
                "$username@$domain.$tld"
            }

            // 具有多个部分的复杂模式（例如，[A-Z][a-z]{4,9}）
            pattern.contains("[A-Z]") && pattern.contains("[a-z]") -> {
                generateComplexPattern(pattern)
            }

            // 电话号码模式
            pattern.contains("\\d") -> {
                val length = extractQuantifier(pattern, "\\d")
                generateRandomString(length.first, length.second, "0123456789")
            }

            // 单词模式
            pattern.contains("\\w") -> {
                val length = extractQuantifier(pattern, "\\w")
                generateRandomString(
                    length.first,
                    length.second,
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_"
                )
            }

            // 字符类
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

            // 默认：生成一个简单的字母数字字符串
            else -> generateRandomString(5, 10, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
        }
    }

    /**
     * 为具有多个字符类的复杂模式生成字符串
     */
    private fun generateComplexPattern(pattern: String): String {
        val result = StringBuilder()

        // 解析模式部分：[A-Z]、[a-z]{4,9} 等。
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

        return if (result.isNotEmpty()) result.toString() else generateRandomString(
            5,
            10,
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        )
    }

    /**
     * 从模式中提取量词并返回最小/最大长度
     */
    private fun extractQuantifier(pattern: String, elementPattern: String): Pair<Int, Int> {
        // 在元素模式后查找量词
        // 处理转义和未转义的模式
        val escapedPattern = elementPattern.replace("\\", "\\\\")
        val quantifierRegex = Regex("$escapedPattern\\{(\\d+)(?:,(\\d+))?\\}")
        val match = quantifierRegex.find(pattern)

        return if (match != null) {
            val min = match.groupValues[1].toInt()
            val max = match.groupValues.getOrNull(2)?.toIntOrNull() ?: min
            Pair(min, max)
        } else {
            // 尝试在字符串中查找任何 {n} 或 {n,m} 模式
            val anyQuantifierRegex = Regex("\\{(\\d+)(?:,(\\d+))?\\}")
            val anyMatch = anyQuantifierRegex.find(pattern)
            if (anyMatch != null) {
                val min = anyMatch.groupValues[1].toInt()
                val max = anyMatch.groupValues.getOrNull(2)?.toIntOrNull() ?: min
                Pair(min, max)
            } else {
                // 如果未找到量词，则为默认长度
                Pair(5, 10)
            }
        }
    }

    /**
     * 生成具有指定长度范围和字符集的随机字符串
     */
    private fun generateRandomString(minLength: Int, maxLength: Int, charset: String): String {
        val length = random.integer(minLength, maxLength)
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
}