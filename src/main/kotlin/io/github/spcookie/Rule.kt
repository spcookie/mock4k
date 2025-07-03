package io.github.spcookie

/**
 * Represents different types of generation rules
 * Based on Mock.js syntax specification
 */
internal sealed interface Rule {

    // ==================== String Rules ====================
    /**
     * String repeat range rule: 'name|min-max': string
     * Generate a string by repeating the string value, with repeat count between min and max (inclusive)
     */
    data class StringRange(val min: Int, val max: Int) : Rule

    /**
     * String repeat count rule: 'name|count': string
     * Generate a string by repeating the string value exactly count times
     */
    data class StringCount(val count: Int) : Rule

    // ==================== Number Rules ====================
    /**
     * Number increment rule: 'name|+step': number
     * Auto-increment the property value by step, with initial value as number
     */
    data class NumberIncrement(val step: Int) : Rule

    /**
     * Number range rule: 'name|min-max': number
     * Generate an integer between min and max (inclusive)
     */
    data class NumberRange(val min: Int, val max: Int) : Rule

    /**
     * Float range rule: 'name|min-max.dmin-dmax': number
     * Generate a float number with integer part between min and max, and decimal places between dmin and dmax
     */
    data class FloatRange(val min: Int, val max: Int, val dmin: Int, val dmax: Int) : Rule

    /**
     * Float with fixed integer and variable decimal: 'name|count.dmin-dmax': number
     */
    data class FloatCount(val count: Int, val dmin: Int, val dmax: Int) : Rule

    /**
     * Float range with fixed decimal places: 'name|min-max.dcount': number
     */
    data class FloatRangeFixed(val min: Int, val max: Int, val dcount: Int) : Rule

    /**
     * Float with fixed decimal places: 'name|count.dcount': number
     */
    data class FloatFixed(val count: Int, val dcount: Int) : Rule

    // ==================== Boolean Rules ====================
    /**
     * Boolean random rule: 'name|1': boolean
     * Randomly generate a boolean value with 50% probability for true and 50% for false
     */
    data class BooleanRandom(val probability: Int = 1) : Rule

    /**
     * Boolean weighted rule: 'name|min-max': value
     * Randomly generate a boolean value with probability min/(min+max) for value and max/(min+max) for !value
     */
    data class BooleanWeighted(val min: Int, val max: Int) : Rule

    // ==================== Object Rules ====================
    /**
     * Object property count rule: 'name|count': object
     * Randomly select count properties from the object value
     */
    data class ObjectCount(val count: Int) : Rule

    /**
     * Object property range rule: 'name|min-max': object
     * Randomly select between min and max properties from the object value
     */
    data class ObjectRange(val min: Int, val max: Int) : Rule

    // ==================== Array Rules ====================
    /**
     * Array pick one rule: 'name|1': array
     * Randomly pick one element from the array value as the final value
     */
    data class ArrayPickOne(val index: Int = 1) : Rule

    /**
     * Array pick sequential rule: 'name|+1': array
     * Sequentially pick one element from the array value as the final value
     */
    data class ArrayPickSequential(val step: Int = 1) : Rule

    /**
     * Array repeat range rule: 'name|min-max': array
     * Generate a new array by repeating the array value, with repeat count between min and max
     */
    data class ArrayRepeatRange(val min: Int, val max: Int) : Rule

    /**
     * Array repeat count rule: 'name|count': array
     * Generate a new array by repeating the array value exactly count times
     */
    data class ArrayRepeatCount(val count: Int) : Rule

    // ==================== Legacy Compatibility Aliases ====================
    /**
     * Legacy Range rule for backward compatibility
     */
    @Deprecated("Use specific type rules instead", ReplaceWith("NumberRange(min, max)"))
    data class Range(val min: Int, val max: Int) : Rule

    /**
     * Legacy Count rule for backward compatibility
     */
    @Deprecated("Use specific type rules instead", ReplaceWith("StringCount(count) or ArrayRepeatCount(count)"))
    data class Count(val count: Int) : Rule

    /**
     * Legacy Increment rule for backward compatibility
     */
    @Deprecated("Use NumberIncrement instead", ReplaceWith("NumberIncrement(step)"))
    data class Increment(val step: Int) : Rule
}