package io.github.spcookie

/**
 * Bean模拟配置
 */
data class BeanMockConfig(
    val includePrivate: Boolean = false,
    val includeStatic: Boolean = false,
    val includeTransient: Boolean = false,
    val depth: Int = 3
)