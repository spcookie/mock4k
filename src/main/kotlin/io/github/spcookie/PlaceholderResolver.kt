package io.github.spcookie

import kotlin.reflect.full.memberFunctions

/**
 * @placeholder语法解析器
 *
 * @author spcookie
 * @since 1.0.0
 */
internal class PlaceholderResolver(
    private val random: MockRandom
) {

    companion object {
        /**
         * 匹配占位符语法的模式: @methodName 或 @methodName(params)
         * 分组: 1 = 方法名, 2 = 参数 (可选)
         */
        private val PLACEHOLDER_PATTERN = Regex("@([a-zA-Z][a-zA-Z0-9]*(?:\\.[a-zA-Z0-9]+)*)(?:\\(([^)]*)\\))?")

        /**
         * 匹配跨越整个字符串的单个占位符的模式
         * 用于确定是否应保留原始类型或转换为字符串
         */
        private val SINGLE_PLACEHOLDER_PATTERN =
            Regex("^@([a-zA-Z][a-zA-Z0-9]*(?:\\.[a-zA-Z0-9]+)*)(?:\\(([^)]*)\\))?$")
    }

    /**
     * 解析字符串中的占位符
     */
    fun resolve(template: String, context: ExecutionContext? = null): String {
        // 处理@placeholder语法
        return PLACEHOLDER_PATTERN.findAll(template).fold(template) { acc, match ->
            val fullMatch = match.value
            val methodName = match.groupValues[1]
            val params = match.groupValues.getOrNull(2) ?: ""

            val resolved = resolvePlaceholder(methodName, params, fullMatch, context)
            acc.replace(fullMatch, resolved.toString())
        }
    }

    /**
     * 使用智能类型检测解析字符串模板
     * 确定是返回原始类型(单个占位符)还是字符串(多个部分)
     */
    fun resolveStringTemplate(template: String, context: ExecutionContext? = null): Any {
        // 检查模板是否为单个占位符
        val match = SINGLE_PLACEHOLDER_PATTERN.matchEntire(template.trim())

        return if (match != null) {
            // 单个占位符 - 保留原始类型
            resolveSinglePlaceholder(template, context)
        } else {
            // 包含多个部分的模板 - 返回字符串
            resolve(template, context)
        }
    }

    /**
     * 解析单个占位符同时保留其原始类型
     */
    fun resolveSinglePlaceholder(template: String, context: ExecutionContext? = null): Any {
        val match = PLACEHOLDER_PATTERN.find(template)
        if (match != null) {
            val methodName = match.groupValues[1]
            val params = match.groupValues.getOrNull(2) ?: ""
            return resolvePlaceholder(methodName, params, match.value, context)
        }
        return template
    }


    private fun resolvePlaceholder(
        methodName: String,
        params: String,
        placeholder: String,
        context: ExecutionContext?
    ): Any {
        return try {
            // 首先，尝试解析为属性引用
            if (context != null) {
                val propertyValue = resolvePropertyReference(methodName, context)
                if (propertyValue != null) {
                    return propertyValue
                }
            }

            // 其次，尝试自定义占位符
            val lowercaseMethodName = methodName.lowercase()
            val customResult = resolveExtendedPlaceholder(lowercaseMethodName, params, placeholder)
            if (customResult != placeholder) {
                return customResult
            }

            // 最后，尝试内置占位符
            val result = if (params.isNotEmpty()) {
                val paramList = parseParams(params)
                callMethodWithParams(lowercaseMethodName, paramList, placeholder)
            } else {
                callMethod(lowercaseMethodName, placeholder)
            }
            result
        } catch (_: Exception) {
            placeholder
        }
    }

    /**
     * 解析属性引用(相对或绝对路径)
     */
    private fun resolvePropertyReference(propertyPath: String, context: ExecutionContext): Any? {
        // 检查是否为绝对路径(包含点)
        return if (propertyPath.contains(".")) {
            // 绝对路径引用
            context.getResolvedValueByAbsolutePath(propertyPath)
        } else {
            // 相对路径引用(在当前上下文内)
            context.getResolvedValue(propertyPath)
        }
    }

    /**
     * 解析自定义占位符
     */
    private fun resolveExtendedPlaceholder(methodName: String, params: String, placeholder: String): Any {
        return try {
            val lowerMethodName = methodName.lowercase()
            if (params.isNotEmpty()) {
                // 尝试带参数的自定义占位符
                val customGenerator = random.getExtendedWithParams(lowerMethodName)
                if (customGenerator != null) {
                    val paramList = parseParams(params)
                    return customGenerator(paramList)
                }
            } else {
                // 尝试不带参数的自定义占位符
                val customGenerator = random.getExtended(lowerMethodName)
                if (customGenerator != null) {
                    return customGenerator()
                }
            }
            placeholder
        } catch (_: Exception) {
            placeholder
        }
    }

    private fun parseParams(params: String): List<Any> {
        if (params.isBlank()) return emptyList()

        return params.split(",").map { param ->
            val trimmed = param.trim()
            when {
                trimmed.startsWith('"') && trimmed.endsWith('"') -> trimmed.substring(1, trimmed.length - 1)
                trimmed.startsWith('\'') && trimmed.endsWith('\'') -> trimmed.substring(1, trimmed.length - 1)
                trimmed.toIntOrNull() != null -> trimmed.toInt()
                trimmed.toDoubleOrNull() != null -> trimmed.toDouble()
                trimmed == "true" -> true
                trimmed == "false" -> false
                else -> trimmed
            }
        }
    }


    private fun callMethod(methodName: String, placeholder: String): Any {
        val method = random::class.memberFunctions.find { it.name.lowercase() == methodName.lowercase() }
        return if (method != null && method.parameters.size == 1) {
            method.call(random) ?: placeholder
        } else {
            placeholder
        }
    }

    private fun callMethodWithParams(methodName: String, params: List<Any>, placeholder: String): Any {
        val methods = random::class.memberFunctions.filter { it.name.lowercase() == methodName.lowercase() }

        for (method in methods) {
            if (method.parameters.size == params.size + 1) {
                return try {
                    method.call(random, *params.toTypedArray()) ?: placeholder
                } catch (_: Exception) {
                    continue
                }
            }
        }

        return placeholder
    }
}