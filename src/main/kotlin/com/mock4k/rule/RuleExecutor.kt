package com.mock4k.rule

import com.mock4k.core.MockEngine
import com.mock4k.random.MockRandom
import kotlin.math.pow
import kotlin.random.Random

/**
 * Executor for generation rules
 */
class RuleExecutor(private val random: MockRandom) {
    
    private val incrementCounters = mutableMapOf<String, Int>()
    
    /**
     * Execute a rule with given value
     */
    fun execute(parsedRule: ParsedRule, value: Any, engine: MockEngine): Any {
        val rule = parsedRule.rule ?: return engine.generate(value)
        
        return when (rule) {
            is Rule.Range -> executeRange(rule, value, engine)
            is Rule.Count -> executeCount(rule, value, engine)
            is Rule.Increment -> executeIncrement(rule, value, parsedRule.name)
            is Rule.FloatRange -> executeFloatRange(rule, value)
            is Rule.FloatCount -> executeFloatCount(rule, value)
            is Rule.FloatRangeFixed -> executeFloatRangeFixed(rule, value)
            is Rule.FloatFixed -> executeFloatFixed(rule, value)
        }
    }
    
    private fun executeRange(rule: Rule.Range, value: Any, engine: MockEngine): Any {
        val count = random.integer(rule.min, rule.max)
        return executeWithCount(count, value, engine)
    }
    
    private fun executeCount(rule: Rule.Count, value: Any, engine: MockEngine): Any {
        return executeWithCount(rule.count, value, engine)
    }
    
    private fun executeWithCount(count: Int, value: Any, engine: MockEngine): Any {
        return when (value) {
            is String -> {
                // Repeat string
                value.repeat(count)
            }
            is Number -> {
                // Generate random number in range
                when (value) {
                    is Int -> random.integer(0, count)
                    is Long -> random.integer(0, count).toLong()
                    is Float -> random.float(0f, count.toFloat())
                    is Double -> random.float(0.0, count.toDouble())
                    else -> random.integer(0, count)
                }
            }
            is Boolean -> {
                // Generate boolean with probability
                if (count == 1) {
                    random.boolean()
                } else {
                    // For range, use min/(min+max) probability
                    random.boolean()
                }
            }
            is List<*> -> {
                // Repeat array or pick from array
                if (count == 1) {
                    // Pick one element
                    if (value.isNotEmpty()) {
                        engine.generate(value[random.integer(0, value.size - 1)]!!)
                    } else {
                        emptyList<Any>()
                    }
                } else {
                    // Repeat array
                    val result = mutableListOf<Any>()
                    repeat(count) {
                        value.forEach { item ->
                            result.add(engine.generate(item ?: ""))
                        }
                    }
                    result
                }
            }
            is Map<*, *> -> {
                // Pick properties from object
                val keys = value.keys.toList()
                val selectedKeys = if (count >= keys.size) {
                    keys
                } else {
                    keys.shuffled(Random.Default).take(count)
                }
                
                val result = mutableMapOf<String, Any>()
                selectedKeys.forEach { key ->
                    val keyStr = key.toString()
                    result[keyStr] = engine.generate(value[key] ?: "")
                }
                result
            }
            else -> engine.generate(value)
        }
    }
    
    private fun executeIncrement(rule: Rule.Increment, value: Any, propertyName: String): Any {
        val currentValue = incrementCounters.getOrPut(propertyName) {
            when (value) {
                is Number -> value.toInt()
                else -> 0
            }
        }
        
        incrementCounters[propertyName] = currentValue + rule.step
        return currentValue
    }
    
    private fun executeFloatRange(rule: Rule.FloatRange, value: Any): Double {
        val integerPart = random.integer(rule.min, rule.max)
        val decimalPlaces = random.integer(rule.dmin, rule.dmax)
        val decimalPart = random.float(0.0, 1.0)
        
        val factor = 10.0.pow(decimalPlaces)
        val roundedDecimal = kotlin.math.round(decimalPart * factor) / factor
        
        return integerPart + roundedDecimal
    }
    
    private fun executeFloatCount(rule: Rule.FloatCount, value: Any): Double {
        val decimalPlaces = random.integer(rule.dmin, rule.dmax)
        val decimalPart = random.float(0.0, 1.0)
        
        val factor = 10.0.pow(decimalPlaces)
        val roundedDecimal = kotlin.math.round(decimalPart * factor) / factor
        
        return rule.count + roundedDecimal
    }
    
    private fun executeFloatRangeFixed(rule: Rule.FloatRangeFixed, value: Any): Double {
        val integerPart = random.integer(rule.min, rule.max)
        val decimalPart = random.float(0.0, 1.0)
        
        val factor = 10.0.pow(rule.dcount)
        val roundedDecimal = kotlin.math.round(decimalPart * factor) / factor
        
        return integerPart + roundedDecimal
    }
    
    private fun executeFloatFixed(rule: Rule.FloatFixed, value: Any): Double {
        val decimalPart = random.float(0.0, 1.0)
        
        val factor = 10.0.pow(rule.dcount)
        val roundedDecimal = kotlin.math.round(decimalPart * factor) / factor
        
        return rule.count + roundedDecimal
    }
}