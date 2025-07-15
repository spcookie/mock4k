package io.github.spcookie

/**
 * 表示不同类型的生成规则
 * 基于Mock.js语法规范
 *
 * @author spcookie
 * @since 1.0.0
 */
internal sealed interface Rule {

    /**
     * 验证规则参数的正确性
     * @return 如果所有参数都有效则返回true，否则返回false
     */
    fun validate(): Boolean

    // ==================== 字符串规则 ====================
    /**
     * 字符串重复范围规则: 'name|min-max': string
     * 通过重复字符串值生成字符串，重复次数在min和max之间（包含边界）
     */
    data class StringRange(val min: Int, val max: Int) : Rule {
        override fun validate(): Boolean = min >= 0 && max >= 0 && min <= max
    }

    /**
     * 字符串重复计数规则: 'name|count': string
     * 通过精确重复字符串值count次来生成字符串
     */
    data class StringCount(val count: Int) : Rule {
        override fun validate(): Boolean = count >= 0
    }

    // ==================== 数字规则 ====================
    /**
     * 数字递增规则: 'name|+step': number
     * 以number作为初始值，按step自动递增属性值
     */
    data class NumberIncrement(val step: Int) : Rule {
        override fun validate(): Boolean = step != 0
    }

    /**
     * 数字范围规则: 'name|min-max': number
     * 生成min和max之间的整数（包含边界）
     */
    data class NumberRange(val min: Int, val max: Int) : Rule {
        override fun validate(): Boolean = min <= max
    }

    /**
     * 浮点数范围规则: 'name|min-max.dmin-dmax': number
     * 生成整数部分在min和max之间，小数位数在dmin和dmax之间的浮点数
     */
    data class FloatRange(val min: Int, val max: Int, val dmin: Int, val dmax: Int) : Rule {
        override fun validate(): Boolean = min <= max && dmin >= 0 && dmax >= 0 && dmin <= dmax && dmax <= 10
    }

    /**
     * 固定整数部分和可变小数位数的浮点数: 'name|count.dmin-dmax': number
     */
    data class FloatCount(val count: Int, val dmin: Int, val dmax: Int) : Rule {
        override fun validate(): Boolean = dmin >= 0 && dmax >= 0 && dmin <= dmax && dmax <= 10
    }

    /**
     * 固定小数位数的浮点数范围: 'name|min-max.dcount': number
     */
    data class FloatRangeFixed(val min: Int, val max: Int, val dcount: Int) : Rule {
        override fun validate(): Boolean = min <= max && dcount >= 0 && dcount <= 10
    }

    /**
     * 固定小数位数的浮点数: 'name|count.dcount': number
     */
    data class FloatFixed(val count: Int, val dcount: Int) : Rule {
        override fun validate(): Boolean = dcount >= 0 && dcount <= 10
    }

    // ==================== 布尔规则 ====================
    /**
     * 布尔随机规则: 'name|1': boolean
     * 随机生成布尔值，true和false各有50%的概率
     */
    data class BooleanRandom(val probability: Int = 1) : Rule {
        override fun validate(): Boolean = probability == 1
    }

    /**
     * 布尔加权规则: 'name|min-max': value
     * 随机生成布尔值，value的概率为min/(min+max)，!value的概率为max/(min+max)
     */
    data class BooleanWeighted(val min: Int, val max: Int) : Rule {
        override fun validate(): Boolean = min >= 0 && max >= 0 && (min + max) > 0
    }

    // ==================== 对象规则 ====================
    /**
     * 对象属性计数规则: 'name|count': object
     * 从对象值中随机选择count个属性
     */
    data class ObjectCount(val count: Int) : Rule {
        override fun validate(): Boolean = count >= 0
    }

    /**
     * 对象属性范围规则: 'name|min-max': object
     * 从对象值中随机选择min到max个属性
     */
    data class ObjectRange(val min: Int, val max: Int) : Rule {
        override fun validate(): Boolean = min >= 0 && max >= 0 && min <= max
    }

    // ==================== 数组规则 ====================
    /**
     * 数组选择一个规则: 'name|1': array
     * 从数组值中随机选择一个元素作为最终值
     */
    data class ArrayPickOne(val index: Int = 1) : Rule {
        override fun validate(): Boolean = index == 1
    }

    /**
     * 数组顺序选择规则: 'name|+1': array
     * 从数组值中顺序选择一个元素作为最终值
     */
    data class ArrayPickSequential(val step: Int = 1) : Rule {
        override fun validate(): Boolean = step != 0
    }

    /**
     * 数组重复范围规则: 'name|min-max': array
     * 通过重复数组值生成新数组，重复次数在min和max之间
     */
    data class ArrayRepeatRange(val min: Int, val max: Int) : Rule {
        override fun validate(): Boolean = min >= 0 && max >= 0 && min <= max
    }

    /**
     * 数组重复计数规则: 'name|count': array
     * 通过精确重复数组值count次来生成新数组
     */
    data class ArrayRepeatCount(val count: Int) : Rule {
        override fun validate(): Boolean = count >= 0
    }

}