package io.github.spcookie

import org.slf4j.LoggerFactory

/**
 * Core engine for mock data generation
 *
 * @author spcookie
 * @since 1.0.0
 */
internal class MockEngine() {

    private val logger = LoggerFactory.getLogger(MockEngine::class.java)
    private val ruleExecutor = RuleExecutor()

    companion object {
        private val ruleParser = RuleParser()
        private val placeholderResolver = PlaceholderResolver()
        private val regexResolver = RegexResolver()
    }

    /**
     * Resolve string template by first handling regex patterns, then placeholders
     */
    private fun resolveString(template: String, context: ExecutionContext): Any {
        // First, handle regex patterns
        val regexResult = regexResolver.resolveRegexPatterns(template)

        // Use PlaceholderResolver to handle both single and multiple placeholder resolution
        return placeholderResolver.resolveStringTemplate(regexResult, context)
    }

    /**
     * Generate mock data based on template
     */
    @Suppress("UNCHECKED_CAST")
    fun generate(template: Any, context: ExecutionContext? = null): Any? {
        // For top-level calls, create a new execution context with unique instance ID
        // For nested calls, use the provided context
        val executionContext = context ?: ExecutionContext()

        val result = when (template) {
            is Map<*, *> -> generateFromMap(template as Map<String, Any?>, executionContext)
            is List<*> -> generateFromList(template, executionContext)
            is String -> resolveString(template, executionContext)
            else -> template
        }
        
        return result
    }

    private fun generateFromMap(template: Map<String, Any?>, context: ExecutionContext): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        template.forEach { (key, value) ->
            // Handle null values directly without processing
            if (value == null) {
                val parsedRule = if (key.contains("|")) {
                    ruleParser.parse(key, RuleParser.ValueType.STRING)
                } else {
                    ParsedRule(key, null)
                }
                result[parsedRule.name] = null
                context.storeResolvedValue(parsedRule.name, null)
                return@forEach
            }
            
            // Use context-aware parsing for better rule determination
            val valueType = determineValueType(value)
            val parsedRule = if (key.contains("|")) {
                ruleParser.parse(key, valueType)
            } else {
                ParsedRule(key, null)
            }
            val childContext = context.createChildContext(parsedRule.name)
            val generatedValue = ruleExecutor.execute(parsedRule, value, this, childContext)
            result[parsedRule.name] = generatedValue

            // Store the resolved value in context for future reference
            context.storeResolvedValue(parsedRule.name, generatedValue)
        }

        return result
    }

    /**
     * Determine the value type for context-aware rule parsing
     */
    private fun determineValueType(value: Any): RuleParser.ValueType {
        return when (value) {
            is String -> RuleParser.ValueType.STRING
            is Number -> RuleParser.ValueType.NUMBER
            is Boolean -> RuleParser.ValueType.BOOLEAN
            is Map<*, *> -> RuleParser.ValueType.OBJECT
            is List<*> -> RuleParser.ValueType.ARRAY
            else -> RuleParser.ValueType.STRING // fallback
        }
    }

    private fun generateFromList(template: List<*>, context: ExecutionContext): List<Any?> {
        return template.mapIndexed { index, item ->
            generate(item ?: "", context)
        }
    }
}