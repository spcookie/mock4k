package io.github.spcookie

/**
 * Configuration for bean mocking
 */
internal data class BeanMockConfig(
    val includePrivate: Boolean = false,
    val includeStatic: Boolean = false,
    val includeTransient: Boolean = false
)