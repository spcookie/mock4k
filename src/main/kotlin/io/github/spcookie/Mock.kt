package io.github.spcookie

/**
 * Bean和属性配置的Mock注解容器
 *
 * @author spcookie
 * @since 1.2.0
 */
@Target()
@Retention(AnnotationRetention.RUNTIME)
annotation class Mock {

    /**
     * 集合生成的填充策略枚举
     *
     * @author spcookie
     * @since 1.2.0
     */
    enum class Fill {
        /**
         * 为所有位置重复相同的元素
         */
        REPEAT,

        /**
         * 为每个位置生成不同的随机元素
         */
        RANDOM
    }

    /**
     * Bean模拟生成的配置
     *
     * @param includePrivate 是否在模拟生成中包含私有属性
     * @param includeStatic 是否在模拟生成中包含静态属性
     * @param includeTransient 是否在模拟生成中包含瞬态属性
     * @param depth 递归Bean生成的最大深度以避免无限递归 (默认: 3)
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
     * Bean属性的Mock注解
     *
     * @param rule 用于模拟生成的规则
     * @param placeholder 用于模拟生成的占位符
     * @param enabled 是否为此属性启用模拟 (默认: true)
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
     * 用于指定占位符的占位符注解
     *
     * @param value 要使用的占位符表达式
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
     * 用于指定生成规则的规则注解
     *
     * @param count 生成的固定计数
     * @param min 范围生成的最小值
     * @param max 范围生成的最大值
     * @param step 递增生成的步长值
     * @param dmin 最小小数位数
     * @param dmax 最大小数位数
     * @param dcount 固定小数位数
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
     * 用于指定集合大小和填充策略的长度注解
     *
     * @param value 集合的大小 (List或Map)
     * @param fill 填充策略: Fill.REPEAT重复第一个元素，Fill.RANDOM生成随机元素
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