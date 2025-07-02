package com.mock4k

import kotlin.math.pow
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions

/**
 * Mock4K - A Kotlin library for generating mock data
 * Inspired by Mock.js
 */
object Mock {
    
    /**
     * The Random utility instance
     */
    val Random = MockRandom
    
    private val engine = MockEngine()
    
    /**
     * Generate mock data based on template
     * 
     * @param template The data template
     * @return Generated mock data
     */
    fun mock(template: Any): Any {
        return engine.generate(template)
    }
    
    /**
     * Generate mock data based on template map
     * 
     * @param template The data template as map
     * @return Generated mock data
     */
    fun mock(template: Map<String, Any>): Map<String, Any> {
        return engine.generate(template) as Map<String, Any>
    }
    
    /**
     * Generate mock data based on template with custom random instance
     * 
     * @param template The data template
     * @param random Custom random instance
     * @return Generated mock data
     */
    fun mock(template: Any, random: MockRandom): Any {
        val customEngine = MockEngine(random)
        return customEngine.generate(template)
    }
}

/**
 * Represents different types of generation rules
 */
sealed class Rule {
    
    /**
     * Range rule: min-max
     */
    data class Range(val min: Int, val max: Int) : Rule()
    
    /**
     * Count rule: count
     */
    data class Count(val count: Int) : Rule()
    
    /**
     * Increment rule: +step
     */
    data class Increment(val step: Int) : Rule()
    
    /**
     * Float range rule: min-max.dmin-dmax
     */
    data class FloatRange(val min: Int, val max: Int, val dmin: Int, val dmax: Int) : Rule()
    
    /**
     * Float count rule: count.dmin-dmax
     */
    data class FloatCount(val count: Int, val dmin: Int, val dmax: Int) : Rule()
    
    /**
     * Float range with fixed decimal places: min-max.dcount
     */
    data class FloatRangeFixed(val min: Int, val max: Int, val dcount: Int) : Rule()
    
    /**
     * Float with fixed decimal places: count.dcount
     */
    data class FloatFixed(val count: Int, val dcount: Int) : Rule()
}

/**
 * Represents a parsed rule with property name
 */
data class ParsedRule(val name: String, val rule: Rule?)

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
        return executeCount(Rule.Count(count), value, engine)
    }
    
    private fun executeCount(rule: Rule.Count, value: Any, engine: MockEngine): Any {
        return when (value) {
            is String -> value.repeat(rule.count)
            is Number -> value
            is Boolean -> value
            is List<*> -> {
                if (value.isEmpty()) {
                    emptyList<Any>()
                } else {
                    val result = mutableListOf<Any>()
                    repeat(rule.count) {
                        val item = value[random.integer(0, value.size - 1)]
                        result.add(engine.generate(item))
                    }
                    result
                }
            }
            is Map<*, *> -> {
                val result = mutableListOf<Any>()
                repeat(rule.count) {
                    result.add(engine.generate(value))
                }
                result
            }
            else -> value
        }
    }
    
    private fun executeIncrement(rule: Rule.Increment, value: Any, propertyName: String): Any {
        return when (value) {
            is Number -> {
                val currentCount = incrementCounters.getOrPut(propertyName) { value.toInt() }
                val result = currentCount
                incrementCounters[propertyName] = currentCount + rule.step
                result
            }
            else -> value
        }
    }
    
    private fun executeFloatRange(rule: Rule.FloatRange, value: Any): Any {
        val integerPart = random.integer(rule.min, rule.max)
        val decimalPlaces = random.integer(rule.dmin, rule.dmax)
        val decimalPart = random.integer(0, (10.0.pow(decimalPlaces) - 1).toInt())
        val divisor = 10.0.pow(decimalPlaces)
        return integerPart + decimalPart / divisor
    }
    
    private fun executeFloatCount(rule: Rule.FloatCount, value: Any): Any {
        val decimalPlaces = random.integer(rule.dmin, rule.dmax)
        val decimalPart = random.integer(0, (10.0.pow(decimalPlaces) - 1).toInt())
        val divisor = 10.0.pow(decimalPlaces)
        return rule.count + decimalPart / divisor
    }
    
    private fun executeFloatRangeFixed(rule: Rule.FloatRangeFixed, value: Any): Any {
        val integerPart = random.integer(rule.min, rule.max)
        val decimalPart = random.integer(0, (10.0.pow(rule.dcount) - 1).toInt())
        val divisor = 10.0.pow(rule.dcount)
        return String.format("%.${rule.dcount}f", integerPart + decimalPart / divisor).toDouble()
    }
    
    private fun executeFloatFixed(rule: Rule.FloatFixed, value: Any): Any {
        val decimalPart = random.integer(0, (10.0.pow(rule.dcount) - 1).toInt())
        val divisor = 10.0.pow(rule.dcount)
        return String.format("%.${rule.dcount}f", rule.count + decimalPart / divisor).toDouble()
    }
}

/**
 * Core engine for mock data generation
 */
class MockEngine(private val random: MockRandom = MockRandom) {
    
    private val ruleParser = RuleParser()
    private val ruleExecutor = RuleExecutor(random)
    private val placeholderResolver = PlaceholderResolver(random)
    
    /**
     * Generate mock data based on template
     */
    fun generate(template: Any): Any {
        return when (template) {
            is Map<*, *> -> generateFromMap(template as Map<String, Any>)
            is List<*> -> generateFromList(template)
            is String -> placeholderResolver.resolve(template)
            else -> template
        }
    }
    
    private fun generateFromMap(template: Map<String, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        template.forEach { (key, value) ->
            val parsedRule = ruleParser.parse(key)
            val generatedValue = ruleExecutor.execute(parsedRule, value, this)
            result[parsedRule.name] = generatedValue
        }
        
        return result
    }
    
    private fun generateFromList(template: List<*>): List<Any> {
        return template.map { generate(it ?: "") }
    }
}

/**
 * Resolver for @placeholder syntax
 */
class PlaceholderResolver(private val random: MockRandom) {
    
    private val placeholderPattern = Regex("@([A-Z_]+)(?:\\(([^)]*)\\))?")
    
    /**
     * Resolve placeholders in a string
     */
    fun resolve(template: String): String {
        var result = template
        
        placeholderPattern.findAll(template).forEach { match ->
            val placeholder = match.value
            val methodName = match.groupValues[1].lowercase()
            val params = match.groupValues[2]
            
            val resolvedValue = resolvePlaceholder(methodName, params)
            result = result.replace(placeholder, resolvedValue.toString())
        }
        
        return result
    }
    
    private fun resolvePlaceholder(methodName: String, params: String): Any {
        return try {
            if (params.isNotEmpty()) {
                val paramList = parseParams(params)
                callMethodWithParams(methodName, paramList)
            } else {
                callMethod(methodName)
            }
        } catch (e: Exception) {
            "@$methodName"
        }
    }
    
    private fun parseParams(params: String): List<Any> {
        if (params.isBlank()) return emptyList()
        
        return params.split(",").map { param ->
            val trimmed = param.trim()
            when {
                trimmed.startsWith('"') && trimmed.endsWith('"') -> trimmed.substring(1, trimmed.length - 1)
                trimmed.startsWith("'") && trimmed.endsWith("'") -> trimmed.substring(1, trimmed.length - 1)
                trimmed.toIntOrNull() != null -> trimmed.toInt()
                trimmed.toDoubleOrNull() != null -> trimmed.toDouble()
                trimmed == "true" -> true
                trimmed == "false" -> false
                else -> trimmed
            }
        }
    }
    
    private fun callMethod(methodName: String): Any {
        val method = random::class.memberFunctions.find { it.name == methodName }
        return if (method != null && method.parameters.size == 1) {
            method.call(random) ?: "@$methodName"
        } else {
            "@$methodName"
        }
    }
    
    private fun callMethodWithParams(methodName: String, params: List<Any>): Any {
        val methods = random::class.memberFunctions.filter { it.name == methodName }
        
        for (method in methods) {
            if (method.parameters.size == params.size + 1) {
                return try {
                    method.call(random, *params.toTypedArray()) ?: "@$methodName"
                } catch (e: Exception) {
                    continue
                }
            }
        }
        
        return "@$methodName"
    }
}