package io.github.spcookie

import kotlin.math.pow

/**
 * Execution context for tracking instance, path information and increment counters
 */
data class ExecutionContext(
    val instanceId: String = "default",
    val path: String = "",
    val incrementCounters: MutableMap<String, Int> = mutableMapOf()
) {
    fun createChildContext(propertyName: String): ExecutionContext {
        val newPath = if (path.isEmpty()) propertyName else "$path.$propertyName"
        return ExecutionContext(instanceId, newPath, incrementCounters)
    }

    fun createCounterKey(propertyName: String): String {
        // Include path to ensure independent counters at different levels
        return if (path.isEmpty()) propertyName else "$path.$propertyName"
    }

    fun getOrPutCounter(key: String, defaultValue: Int): Int {
        return incrementCounters.getOrPut(key, { defaultValue })
    }


}

/**
 * Executor for generation rules
 */
internal class RuleExecutor {

    private val random = MockRandom
    private var instanceCounter = 0

    /**
     * Generate a unique instance ID for each mock generation call
     */
    fun generateInstanceId(): String {
        return "instance_${++instanceCounter}"
    }

    /**
     * Execute a rule with given value
     */
    fun execute(
        parsedRule: ParsedRule,
        value: Any,
        engine: MockEngine,
        context: ExecutionContext = ExecutionContext()
    ): Any {
        val rule = parsedRule.rule ?: return engine.generate(value, context.createChildContext(parsedRule.name))

        return when (rule) {
            // String rules
            is Rule.StringRange -> executeStringRange(rule, value, engine, context)
            is Rule.StringCount -> executeStringCount(rule, value, engine, context)

            // Number rules
            is Rule.NumberIncrement -> executeNumberIncrement(rule, value, parsedRule.name, context)
            is Rule.NumberRange -> executeNumberRange(rule, value, engine)
            is Rule.FloatRange -> executeFloatRange(rule, value)
            is Rule.FloatCount -> executeFloatCount(rule, value)
            is Rule.FloatRangeFixed -> executeFloatRangeFixed(rule, value)
            is Rule.FloatFixed -> executeFloatFixed(rule, value)

            // Boolean rules
            is Rule.BooleanRandom -> executeBooleanRandom(rule, value)
            is Rule.BooleanWeighted -> executeBooleanWeighted(rule, value)

            // Object rules
            is Rule.ObjectCount -> executeObjectCount(rule, value, engine, context)
            is Rule.ObjectRange -> executeObjectRange(rule, value, engine, context)

            // Array rules
            is Rule.ArrayPickOne -> executeArrayPickOne(rule, value, engine, context)
            is Rule.ArrayPickSequential -> executeArrayPickSequential(rule, value, engine, parsedRule.name, context)
            is Rule.ArrayRepeatRange -> executeArrayRepeatRange(rule, value, engine, context)
            is Rule.ArrayRepeatCount -> executeArrayRepeatCount(rule, value, engine, context)

            // Legacy rules for backward compatibility
            is Rule.Range -> executeNumberRange(Rule.NumberRange(rule.min, rule.max), value, engine)
            is Rule.Count -> executeStringCount(Rule.StringCount(rule.count), value, engine, context)
            is Rule.Increment -> executeNumberIncrement(
                Rule.NumberIncrement(rule.step),
                value,
                parsedRule.name,
                context
            )
        }
    }

    // ==================== String Rules ====================

    private fun executeStringRange(
        rule: Rule.StringRange,
        value: Any,
        engine: MockEngine,
        context: ExecutionContext
    ): Any {
        val count = random.integer(rule.min, rule.max)
        return executeStringCount(Rule.StringCount(count), value, engine, context)
    }

    private fun executeStringCount(
        rule: Rule.StringCount,
        value: Any,
        engine: MockEngine,
        context: ExecutionContext
    ): Any {
        return when (value) {
            is String -> value.repeat(rule.count)
            else -> value.toString().repeat(rule.count)
        }
    }

    // ==================== Number Rules ====================

    private fun executeNumberRange(rule: Rule.NumberRange, value: Any, engine: MockEngine): Any {
        return random.integer(rule.min, rule.max)
    }

    private fun executeNumberIncrement(
        rule: Rule.NumberIncrement,
        value: Any,
        propertyName: String,
        context: ExecutionContext
    ): Any {
        return when (value) {
            is Number -> {
                val counterKey = context.createCounterKey(propertyName)
                val defaultValue = value.toInt()
                val currentValue = context.getOrPutCounter(counterKey, defaultValue)
                val newValue = currentValue + rule.step
                context.incrementCounters[counterKey] = newValue
                newValue
            }

            else -> {
                val counterKey = context.createCounterKey(propertyName)
                val currentValue = context.getOrPutCounter(counterKey, 0)
                val newValue = currentValue + rule.step
                context.incrementCounters[counterKey] = newValue
                newValue
            }
        }
    }

    // ==================== Boolean Rules ====================

    private fun executeBooleanRandom(rule: Rule.BooleanRandom, value: Any): Any {
        // 随机生成布尔值，概率为 1/2
        return random.boolean()
    }

    private fun executeBooleanWeighted(rule: Rule.BooleanWeighted, value: Any): Any {
        // 根据权重生成布尔值
        val total = rule.min + rule.max
        val threshold = rule.min.toDouble() / total
        return when (value) {
            is Boolean -> random.float() < threshold == value
            else -> random.float() < threshold
        }
    }

    // ==================== Object Rules ====================

    private fun executeObjectCount(
        rule: Rule.ObjectCount,
        value: Any,
        engine: MockEngine,
        context: ExecutionContext
    ): Any {
        return when (value) {
            is Map<*, *> -> {
                val keys = value.keys.toList()
                if (keys.isEmpty()) return emptyMap<String, Any>()

                val selectedKeys = keys.shuffled().take(rule.count)
                val result = mutableMapOf<String, Any>()
                selectedKeys.forEach { key ->
                    val keyStr = key.toString()
                    val originalValue = value[key]
                    val childContext = context.createChildContext(keyStr)
                    result[keyStr] = originalValue?.let { engine.generate(it, childContext) } ?: ""
                }
                result
            }

            else -> value
        }
    }

    private fun executeObjectRange(
        rule: Rule.ObjectRange,
        value: Any,
        engine: MockEngine,
        context: ExecutionContext
    ): Any {
        val count = random.integer(rule.min, rule.max)
        return executeObjectCount(Rule.ObjectCount(count), value, engine, context)
    }

    // ==================== Array Rules ====================

    private fun executeArrayPickOne(
        rule: Rule.ArrayPickOne,
        value: Any,
        engine: MockEngine,
        context: ExecutionContext
    ): Any {
        return when (value) {
            is List<*> -> {
                if (value.isEmpty()) return ""
                val item = value[random.integer(0, value.size - 1)]
                item?.let { engine.generate(it, context) } ?: ""
            }

            else -> value
        }
    }

    private fun executeArrayPickSequential(
        rule: Rule.ArrayPickSequential,
        value: Any,
        engine: MockEngine,
        propertyName: String,
        context: ExecutionContext
    ): Any {
        return when (value) {
            is List<*> -> {
                if (value.isEmpty()) return ""
                val counterKey = context.createCounterKey("${propertyName}_array_index")
                val currentIndex = context.getOrPutCounter(counterKey, 0)
                val newIndex = currentIndex + rule.step
                context.incrementCounters[counterKey] = newIndex
                val item = value[newIndex % value.size]
                item?.let { engine.generate(it, context) } ?: ""
            }

            else -> engine.generate(value, context)
        }
    }

    private fun executeArrayRepeatRange(
        rule: Rule.ArrayRepeatRange,
        value: Any,
        engine: MockEngine,
        context: ExecutionContext
    ): Any {
        val count = random.integer(rule.min, rule.max)
        return executeArrayRepeatCount(Rule.ArrayRepeatCount(count), value, engine, context)
    }

    private fun executeArrayRepeatCount(
        rule: Rule.ArrayRepeatCount,
        value: Any,
        engine: MockEngine,
        context: ExecutionContext
    ): Any {
        return when (value) {
            is List<*> -> {
                val result = mutableListOf<Any>()
                repeat(rule.count) { index ->
                    val childContext = context.createChildContext("[$index]")
                    result.addAll(value.mapNotNull { item ->
                        item?.let { engine.generate(it, childContext) }
                    })
                }
                result
            }

            else -> {
                val result = mutableListOf<Any>()
                repeat(rule.count) { index ->
                    val childContext = context.createChildContext("[$index]")
                    result.add(engine.generate(value, childContext))
                }
                result
            }
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