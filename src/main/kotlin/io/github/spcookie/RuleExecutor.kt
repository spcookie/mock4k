package io.github.spcookie

import kotlin.math.pow

/**
 * Executor for generation rules
 */
internal class RuleExecutor {

    private val random = MockRandom

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
                        item?.let { template -> result.add(engine.generate(template)) }
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