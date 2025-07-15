package io.github.spcookie

/**
 * 模板规则解析器
 *
 * @author spcookie
 * @since 1.0.0
 */
internal class RuleParser {

    private val rulePattern = Regex("^(.+?)\\|(.+)$")

    /**
     * 使用上下文信息解析规则字符串以进行更准确的规则确定
     */
    fun parse(key: String, valueType: ValueType): ParsedRule {
        val match = rulePattern.find(key)

        return if (match != null) {
            val name = match.groupValues[1]
            val ruleString = match.groupValues[2]
            ParsedRule(name, parseRuleString(ruleString, valueType))
        } else {
            ParsedRule(key, null)
        }
    }

    /**
     * 使用值类型上下文解析规则字符串以进行准确的规则确定
     */
    private fun parseRuleString(ruleString: String, valueType: ValueType): Rule? {
        return when {
            // 递增模式: +step
            ruleString.startsWith("+") -> {
                val step = ruleString.substring(1).toIntOrNull() ?: 1
                when (valueType) {
                    ValueType.NUMBER -> Rule.NumberIncrement(step)
                    ValueType.ARRAY -> Rule.ArrayPickSequential(step)
                    else -> Rule.NumberIncrement(step) // 回退
                }
            }

            // 带小数位的浮点数模式
            ruleString.contains(".") -> {
                parseFloatRule(ruleString)
            }

            // 范围模式: min-max
            ruleString.contains("-") -> {
                val parts = ruleString.split("-")
                if (parts.size == 2) {
                    val min = parts[0].toIntOrNull()
                    val max = parts[1].toIntOrNull()
                    if (min != null && max != null) {
                        when (valueType) {
                            ValueType.STRING -> Rule.StringRange(min, max)
                            ValueType.NUMBER -> Rule.NumberRange(min, max)
                            ValueType.BOOLEAN -> Rule.BooleanWeighted(min, max)
                            ValueType.OBJECT -> Rule.ObjectRange(min, max)
                            ValueType.ARRAY -> Rule.ArrayRepeatRange(min, max)
                        }
                    } else null
                } else null
            }

            // 计数模式: count
            else -> {
                val count = ruleString.toIntOrNull()
                if (count != null) {
                    when (valueType) {
                        ValueType.STRING -> Rule.StringCount(count)
                        ValueType.NUMBER -> Rule.NumberRange(count, count) // 单个值
                        ValueType.BOOLEAN -> Rule.BooleanRandom(count)
                        ValueType.OBJECT -> Rule.ObjectCount(count)
                        ValueType.ARRAY -> {
                            if (count == 1) Rule.ArrayPickOne(count)
                            else Rule.ArrayRepeatCount(count)
                        }
                    }
                } else null
            }
        }
    }

    /**
     * 用于上下文感知解析的值类型
     */
    enum class ValueType {
        STRING, NUMBER, BOOLEAN, OBJECT, ARRAY
    }

    private fun parseFloatRule(ruleString: String): Rule? {
        val parts = ruleString.split(".")
        if (parts.size != 2) return null

        val integerPart = parts[0]
        val decimalPart = parts[1]

        return when {
            // 带小数范围的范围: 1-10.1-3
            integerPart.contains("-") && decimalPart.contains("-") -> {
                val intParts = integerPart.split("-")
                val decParts = decimalPart.split("-")
                if (intParts.size == 2 && decParts.size == 2) {
                    val min = intParts[0].toIntOrNull()
                    val max = intParts[1].toIntOrNull()
                    val dmin = decParts[0].toIntOrNull()
                    val dmax = decParts[1].toIntOrNull()
                    if (min != null && max != null && dmin != null && dmax != null) {
                        Rule.FloatRange(min, max, dmin, dmax)
                    } else null
                } else null
            }

            // 带小数范围的计数: 5.1-3
            decimalPart.contains("-") -> {
                val count = integerPart.toIntOrNull()
                val decParts = decimalPart.split("-")
                if (count != null && decParts.size == 2) {
                    val dmin = decParts[0].toIntOrNull()
                    val dmax = decParts[1].toIntOrNull()
                    if (dmin != null && dmax != null) {
                        Rule.FloatCount(count, dmin, dmax)
                    } else null
                } else null
            }

            // 带固定小数位的范围: 1-10.2
            integerPart.contains("-") -> {
                val intParts = integerPart.split("-")
                val dcount = decimalPart.toIntOrNull()
                if (intParts.size == 2 && dcount != null) {
                    val min = intParts[0].toIntOrNull()
                    val max = intParts[1].toIntOrNull()
                    if (min != null && max != null) {
                        Rule.FloatRangeFixed(min, max, dcount)
                    } else null
                } else null
            }

            // 带固定小数位的计数: 5.2
            else -> {
                val count = integerPart.toIntOrNull()
                val dcount = decimalPart.toIntOrNull()
                if (count != null && dcount != null) {
                    Rule.FloatFixed(count, dcount)
                } else null
            }
        }
    }
}