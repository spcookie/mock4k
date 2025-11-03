package io.github.spcookie

import Mock
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 测试规则优先级功能
 * 规则优先级：step > count > range and decimal > range > decimal
 */
class RulePriorityTest {

    private val logger = LoggerFactory.getLogger(RulePriorityTest::class.java)

    data class TestRulePriority(
        @Mock.Property(rule = Mock.Rule(step = 5, count = 10, min = 1, max = 100))
        val stepPriority: Int = 0,

        @Mock.Property(rule = Mock.Rule(count = 10, min = 1, max = 100, dmin = 2, dmax = 4))
        val countWithDecimal: Double = 0.0,

        @Mock.Property(rule = Mock.Rule(min = 1, max = 100, dmin = 2, dmax = 4))
        val rangeWithDecimal: Double = 0.0,

        @Mock.Property(rule = Mock.Rule(min = 1, max = 100))
        val rangeOnly: Int = 0,

        @Mock.Property(rule = Mock.Rule(dmin = 2, dmax = 4))
        val decimalOnly: Double = 0.0
    )

    @Test
    fun testRulePriority() {
        logger.info("测试规则优先级...")

        val typeIntrospect = TypeIntrospect(ContainerAdapter())
        val config = BeanMockConfig(includePrivate = true)
        val template = typeIntrospect.analyzeBean(TestRulePriority::class, config)

        logger.info("生成的模板: $template")

        // 验证step规则优先级最高
        assertTrue(template.containsKey("stepPriority|+5"), "step规则应该有最高优先级")

        // 验证count规则优先于range
        assertTrue(template.containsKey("countWithDecimal|10.2-4"), "count规则应该优先于range规则")

        // 验证range和decimal组合
        assertTrue(template.containsKey("rangeWithDecimal|1-100.2-4"), "range和decimal应该正确组合")

        // 验证仅range规则
        assertTrue(template.containsKey("rangeOnly|1-100"), "仅range规则应该正确生成")

        // 验证仅decimal规则
        assertTrue(template.containsKey("decimalOnly|1.2-4"), "仅decimal规则应该使用默认值1")

        logger.info("规则优先级测试通过！")
    }

    @Test
    fun testStepRuleHighestPriority() {
        logger.info("测试step规则最高优先级...")

        data class StepTest(
            @Mock.Property(rule = Mock.Rule(step = 3, count = 5, min = 10, max = 20, dmin = 1, dmax = 2))
            val value: Int = 0
        )

        val typeIntrospect = TypeIntrospect(ContainerAdapter())
        val template = typeIntrospect.analyzeBean(StepTest::class, BeanMockConfig(includePrivate = true))

        logger.info("Step规则测试模板: $template")

        // step规则应该忽略所有其他规则
        assertEquals("@integer", template["value|+3"], "step规则应该忽略其他所有规则")

        logger.info("Step规则优先级测试通过！")
    }

    @Test
    fun testCountRuleSecondPriority() {
        logger.info("测试count规则第二优先级...")

        data class CountTest(
            @Mock.Property(rule = Mock.Rule(count = 7, min = 10, max = 20, dmin = 1, dmax = 3))
            val value: Double = 0.0
        )

        val typeIntrospect = TypeIntrospect(ContainerAdapter())
        val template = typeIntrospect.analyzeBean(CountTest::class, BeanMockConfig(includePrivate = true))

        logger.info("Count规则测试模板: $template")

        // count规则应该包含decimal但忽略range
        assertEquals("@float", template["value|7.1-3"], "count规则应该包含decimal但忽略range")

        logger.info("Count规则优先级测试通过！")
    }

    @Test
    fun testDecimalOnlyLowestPriority() {
        logger.info("测试decimal规则最低优先级...")

        data class DecimalTest(
            @Mock.Property(rule = Mock.Rule(dmin = 2, dmax = 5))
            val value1: Double = 0.0,

            @Mock.Property(rule = Mock.Rule(dcount = 3))
            val value2: Double = 0.0
        )

        val typeIntrospect = TypeIntrospect(ContainerAdapter())
        val template = typeIntrospect.analyzeBean(DecimalTest::class, BeanMockConfig(includePrivate = true))

        logger.info("Decimal规则测试模板: $template")

        // decimal规则应该使用默认值1
        assertEquals("@float", template["value1|1.2-5"], "dmin-dmax应该使用默认值1")
        assertEquals("@float", template["value2|1.3"], "dcount应该使用默认值1")

        logger.info("Decimal规则优先级测试通过！")
    }
}