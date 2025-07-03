package io.github.spcookie

/**
 * Core engine for mock data generation
 */
internal class MockEngine() {

    private val ruleExecutor = RuleExecutor()

    companion object {
        private val ruleParser = RuleParser()
        private val placeholderResolver = PlaceholderResolver()
    }

    /**
     * Generate mock data based on template
     */
    @Suppress("UNCHECKED_CAST")
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