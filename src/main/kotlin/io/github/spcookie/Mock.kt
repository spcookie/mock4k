package io.github.spcookie

/**
 * Mock annotation container for bean and property configurations
 *
 * @author spcookie
 * @since 1.2.0
 */
@Target()
@Retention(AnnotationRetention.RUNTIME)
annotation class Mock {

    /**
     * Fill strategy enumeration for collection generation
     *
     * @author spcookie
     * @since 1.2.0
     */
    enum class Fill {
        /**
         * Repeat the same element for all positions
         */
        REPEAT,

        /**
         * Generate different random elements for each position
         */
        RANDOM
    }

    /**
     * Configuration for bean mock generation
     *
     * @param includePrivate Whether to include private properties in mock generation
     * @param includeStatic Whether to include static properties in mock generation
     * @param includeTransient Whether to include transient properties in mock generation
     * @param depth Maximum depth for recursive bean generation to avoid infinite recursion (default: 3)
     *
     * @author spcookie
     * @since 1.2.0
     */
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Bean(
        val includePrivate: Boolean = false,
        val includeStatic: Boolean = false,
        val includeTransient: Boolean = false,
        val depth: Int = 6
    )

    /**
     * Mock annotation for bean properties
     *
     * @param rule The rule to apply for mock generation
     * @param placeholder The placeholder to use for mock generation
     * @param enabled Whether to enable mock for this property (default: true)
     *
     * @author spcookie
     * @since 1.2.0
     */
    @Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Property(
        val rule: Rule = Rule(),
        val placeholder: Placeholder = Placeholder(),
        val length: Length = Length(),
        val enabled: Boolean = true
    )

    /**
     * Placeholder annotation for specifying placeholders
     *
     * @param value The placeholder expression to use
     *
     * @author spcookie
     * @since 1.2.0
     */
    @Target()
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Placeholder(
        val value: String = "",
        val regex: String = ""
    )

    /**
     * Rule annotation for specifying generation rules
     *
     * @param count Fixed count for generation
     * @param min Minimum value for range generation
     * @param max Maximum value for range generation
     * @param step Step value for increment generation
     * @param dmin Minimum decimal places
     * @param dmax Maximum decimal places
     * @param dcount Fixed decimal places
     *
     * @author spcookie
     * @since 1.2.0
     */
    @Target()
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Rule(
        val count: Int = -1,
        val min: Int = -1,
        val max: Int = -1,
        val step: Int = -1,
        val dmin: Int = -1,
        val dmax: Int = -1,
        val dcount: Int = -1
    )

    /**
     * Length annotation for specifying collection size and fill strategy
     *
     * @param value The size of the collection (List or Map)
     * @param fill The fill strategy: Fill.REPEAT to repeat the first element, Fill.RANDOM to generate random elements
     *
     * @author spcookie
     * @since 1.2.0
     */
    @Target()
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Length(
        val value: Int = 1,
        val fill: Fill = Fill.RANDOM
    )
}