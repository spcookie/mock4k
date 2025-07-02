package com.mock4k.rule

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
     * Float range with fixed decimal rule: min-max.dcount
     */
    data class FloatRangeFixed(val min: Int, val max: Int, val dcount: Int) : Rule()
    
    /**
     * Float fixed rule: count.dcount
     */
    data class FloatFixed(val count: Int, val dcount: Int) : Rule()
}

/**
 * Represents a parsed rule with property name
 */
data class ParsedRule(
    val name: String,
    val rule: Rule?
) {
    fun hasRule(): Boolean = rule != null
}