package io.github.spcookie

/**
 * Configuration for bean mocking
 */
data class BeanMockConfig(
    val includePrivate: Boolean = false,
    val includeStatic: Boolean = false,
    val includeTransient: Boolean = false,
    val depth: Int = 3
)