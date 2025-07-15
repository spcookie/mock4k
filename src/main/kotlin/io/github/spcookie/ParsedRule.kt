package io.github.spcookie

/**
 * 表示带有属性名称的已解析规则
 *
 * @author spcookie
 * @since 1.0.0
 */
internal data class ParsedRule(val name: String, val rule: Rule?)