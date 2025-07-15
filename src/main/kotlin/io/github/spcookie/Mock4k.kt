package io.github.spcookie

/**
 * Mock4k注解容器
 *
 * 标记注解
 *
 * @author spcookie
 * @since 1.2.0
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mock4k()
