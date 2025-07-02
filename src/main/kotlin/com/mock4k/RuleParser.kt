package com.mock4k

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
            
            // Float patterns with decimal places
            ruleString.contains(".") -> {
                parseFloatRule(ruleString)
            }
            
            // Range pattern: name|1-10
            ruleString.contains("-") -> {
                val parts = ruleString.split("-")
                if (parts.size == 2) {
                    val min = parts[0].toIntOrNull()
                    val max = parts[1].toIntOrNull()
                    if (min != null && max != null) {
                        Rule.Range(min, max)
                    } else null
                } else null
            }
            
            // Count pattern: name|5
            else -> {
                val count = ruleString.toIntOrNull()
                if (count != null) {
                    Rule.Count(count)
                } else null
            }
        }
    }
    
    private fun parseFloatRule(ruleString: String): Rule? {
        val parts = ruleString.split(".")
        if (parts.size != 2) return null
        
        val integerPart = parts[0]
        val decimalPart = parts[1]
        
        return when {
            // Range with decimal range: 1-10.1-3
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
            
            // Count with decimal range: 5.1-3
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
            
            // Range with fixed decimal places: 1-10.2
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
            
            // Count with fixed decimal places: 5.2
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