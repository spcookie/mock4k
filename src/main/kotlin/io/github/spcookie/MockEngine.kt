package io.github.spcookie

import org.slf4j.LoggerFactory

/**
 * 模拟数据生成的核心引擎
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
     * 首先处理正则表达式模式，然后处理占位符来解析字符串模板
     */
    private fun resolveString(template: String, context: ExecutionContext): Any {
        // 首先，处理正则表达式模式
        val regexResult = regexResolver.resolveRegexPatterns(template)

        // 使用 PlaceholderResolver 处理单个和多个占位符解析
        val result = placeholderResolver.resolveStringTemplate(regexResult, context)
        return result
    }

    /**
     * 根据模板生成模拟数据
     */
    @Suppress("UNCHECKED_CAST")
    fun generate(template: Any, context: ExecutionContext? = null): Any? {
        // 对于顶级调用，使用唯一的实例 ID 创建新的执行上下文
        // 对于嵌套调用，使用提供的上下文
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
            // 直接处理 null 值，不进行处理
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

            // 使用上下文感知解析以更好地确定规则
            val valueType = determineValueType(value)
            val parsedRule = if (key.contains("|")) {
                ruleParser.parse(key, valueType)
            } else {
                ParsedRule(key, null)
            }
            val childContext = context.createChildContext(parsedRule.name)
            val generatedValue = ruleExecutor.execute(parsedRule, value, this, childContext)
            result[parsedRule.name] = generatedValue

            // 将解析后的值存储在上下文中以供将来参考
            context.storeResolvedValue(parsedRule.name, generatedValue)
        }

        return result
    }

    /**
     * 确定上下文感知规则解析的值类型
     */
    private fun determineValueType(value: Any): RuleParser.ValueType {
        return when (value) {
            is String -> RuleParser.ValueType.STRING
            is Number -> RuleParser.ValueType.NUMBER
            is Boolean -> RuleParser.ValueType.BOOLEAN
            is Map<*, *> -> RuleParser.ValueType.OBJECT
            is List<*> -> RuleParser.ValueType.ARRAY
            else -> RuleParser.ValueType.STRING // 回退
        }
    }

    private fun generateFromList(template: List<*>, context: ExecutionContext): List<Any?> {
        return template.mapIndexed { index, item ->
            generate(item ?: "", context)
        }
    }
}