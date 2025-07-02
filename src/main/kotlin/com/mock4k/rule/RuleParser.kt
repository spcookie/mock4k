package com.mock4k.rule

/**
 * Parser for template rules
 */
class RuleParser {
    
    private val rulePattern = Regex("^(.+?)\\|(.+)$")
    
    /**
     * Parse a property key to extract name and rule
     */
    fun parse(key: String): ParsedRule {
        val match = rulePattern.find(key)
        
        return if (match != null) {
            val name = match.groupValues[1]
            val ruleString = match.groupValues[2]
            ParsedRule(name, parseRuleString(ruleString))
        } else {
            ParsedRule(key, null)
        }
    }
    
    private fun parseRuleString(ruleString: String): Rule? {
        return when {
            // +step pattern: name|+1
            ruleString.startsWith("+") -> {
                val step = ruleString.substring(1).toIntOrNull() ?: 1
                Rule.Increment(step)
            }
            
            // min-max.dmin-dmax pattern: name|1-10.1-3
            ruleString.contains(".") -> {
                val parts = ruleString.split(".")
                if (parts.size == 2) {
                    val integerPart = parseRange(parts[0])
                    val decimalPart = parseRange(parts[1])
                    
                    when {
                        integerPart is Range && decimalPart is Range -> 
                            Rule.FloatRange(integerPart.min, integerPart.max, decimalPart.min, decimalPart.max)
                        integerPart is Count && decimalPart is Range -> 
                            Rule.FloatCount(integerPart.value, decimalPart.min, decimalPart.max)
                        integerPart is Range && decimalPart is Count -> 
                            Rule.FloatRangeFixed(integerPart.min, integerPart.max, decimalPart.value)
                        integerPart is Count && decimalPart is Count -> 
                            Rule.FloatFixed(integerPart.value, decimalPart.value)
                        else -> null
                    }
                } else null
            }
            
            // min-max pattern: name|1-10
            ruleString.contains("-") -> {
                val range = parseRange(ruleString)
                if (range is Range) Rule.Range(range.min, range.max) else null
            }
            
            // count pattern: name|5
            else -> {
                val count = ruleString.toIntOrNull()
                if (count != null) Rule.Count(count) else null
            }
        }
    }
    
    private fun parseRange(rangeString: String): RangeOrCount? {
        return if (rangeString.contains("-")) {
            val parts = rangeString.split("-")
            if (parts.size == 2) {
                val min = parts[0].toIntOrNull()
                val max = parts[1].toIntOrNull()
                if (min != null && max != null) {
                    Range(min, max)
                } else null
            } else null
        } else {
            val count = rangeString.toIntOrNull()
            if (count != null) Count(count) else null
        }
    }
    
    private sealed class RangeOrCount
    private data class Range(val min: Int, val max: Int) : RangeOrCount()
    private data class Count(val value: Int) : RangeOrCount()
}