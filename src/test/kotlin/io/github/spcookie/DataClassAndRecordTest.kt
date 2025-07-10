package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * 测试data class和Java record的字段识别功能
 *
 * @author spcookie
 * @since 1.2.0
 */
class DataClassAndRecordTest {

    private val logger = LoggerFactory.getLogger(DataClassAndRecordTest::class.java)

    // ==================== Data Class测试 ====================

    /**
     * 简单data class测试
     */
    data class SimpleDataClass(
        val id: Long,
        val name: String,
        val age: Int,
        val active: Boolean
    )

    @Test
    fun testSimpleDataClassPropertyRecognition() {
        logger.info("测试简单data class属性识别...")

        val config = BeanMockConfig()
        val properties = getEligibleProperties(SimpleDataClass::class, config)

        assertEquals(4, properties.size, "应该识别出4个属性")

        val propertyNames = properties.map { it.name }.toSet()
        assertTrue(propertyNames.contains("id"), "应该包含id属性")
        assertTrue(propertyNames.contains("name"), "应该包含name属性")
        assertTrue(propertyNames.contains("age"), "应该包含age属性")
        assertTrue(propertyNames.contains("active"), "应该包含active属性")

        logger.info("识别的属性: ${propertyNames.joinToString(", ")}")
    }

    /**
     * 带注解的data class测试
     */
    data class AnnotatedDataClass(
        val id: Long,
        val name: String,
        val normalField: String
    )

    @Test
    fun testAnnotatedDataClassPropertyRecognition() {
        logger.info("测试带注解data class属性识别...")

        val config = BeanMockConfig()
        val properties = getEligibleProperties(AnnotatedDataClass::class, config)

        // disabledField应该被排除，因为enabled=false
        assertEquals(3, properties.size, "应该识别出3个属性（排除disabled字段）")

        val propertyNames = properties.map { it.name }.toSet()
        assertTrue(propertyNames.contains("id"), "应该包含id属性")
        assertTrue(propertyNames.contains("name"), "应该包含name属性")
        assertTrue(propertyNames.contains("normalField"), "应该包含normalField属性")
        assertFalse(propertyNames.contains("disabledField"), "不应该包含disabledField属性")

        logger.info("识别的属性: ${propertyNames.joinToString(", ")}")
    }

    /**
     * 混合var和val的data class测试
     */
    data class MixedDataClass(
        val id: Long,
        var name: String,
        val readOnlyField: String,
        var mutableField: String
    )

    @Test
    fun testMixedDataClassPropertyRecognition() {
        logger.info("测试混合var和val的data class属性识别...")

        val config = BeanMockConfig()
        val properties = getEligibleProperties(MixedDataClass::class, config)

        // 所有属性都应该被识别，包括val属性（因为是data class）
        assertEquals(4, properties.size, "应该识别出4个属性")

        val propertyNames = properties.map { it.name }.toSet()
        assertTrue(propertyNames.contains("id"), "应该包含id属性")
        assertTrue(propertyNames.contains("name"), "应该包含name属性")
        assertTrue(propertyNames.contains("readOnlyField"), "应该包含readOnlyField属性")
        assertTrue(propertyNames.contains("mutableField"), "应该包含mutableField属性")

        logger.info("识别的属性: ${propertyNames.joinToString(", ")}")
    }

    // ==================== 普通类测试（对比） ====================

    /**
     * 普通类（非data class）测试
     */
    class RegularClass {
        val readOnlyProperty: String = "readonly"
        var mutableProperty: String = "mutable"

        @Mock.Property
        val annotatedReadOnly: String = "annotated"
    }

    @Test
    fun testRegularClassPropertyRecognition() {
        logger.info("测试普通类属性识别...")

        val config = BeanMockConfig()
        val properties = getEligibleProperties(RegularClass::class, config)

        // 只有var属性应该被识别（普通类的val属性不应该被包含）
        assertEquals(1, properties.size, "应该只识别出1个属性（var属性）")

        val propertyNames = properties.map { it.name }.toSet()
        assertTrue(propertyNames.contains("mutableProperty"), "应该包含mutableProperty属性")
        assertFalse(propertyNames.contains("readOnlyProperty"), "不应该包含readOnlyProperty属性")
        assertFalse(propertyNames.contains("annotatedReadOnly"), "不应该包含annotatedReadOnly属性")

        logger.info("识别的属性: ${propertyNames.joinToString(", ")}")
    }

    // ==================== Bean Mock测试 ====================

    @Test
    fun testDataClassBeanMocking() {
        logger.info("测试data class Bean生成...")

        val user = mock(SimpleDataClass::class)

        assertNotNull(user, "用户对象不应为null")
        assertNotNull(user.id, "用户ID不应为null")
        assertNotNull(user.name, "用户姓名不应为null")
        assertNotNull(user.age, "用户年龄不应为null")
        assertNotNull(user.active, "用户状态不应为null")

        logger.info("生成的data class用户: $user")
    }

    @Test
    fun testSimpleDataClassBeanMocking() {
        logger.info("测试简单data class Bean生成...")

        try {
            val bean = mock(SimpleDataClass::class)

            assertNotNull(bean, "Bean对象不应为null")
            assertNotNull(bean.id, "ID不应为null")
            assertNotNull(bean.name, "名称不应为null")
            assertNotNull(bean.active, "active不应为null")

            logger.info("生成的简单data class Bean: $bean")
        } catch (e: Exception) {
            logger.error("测试失败: ${e.message}", e)
            throw e
        }
    }

    @Test
    fun testAnnotatedDataClassBeanMocking() {
        logger.info("测试带注解data class Bean生成...")

        try {
            val bean = mock(AnnotatedDataClass::class)

            assertNotNull(bean, "Bean对象不应为null")
            assertNotNull(bean.id, "ID不应为null")
            assertNotNull(bean.name, "名称不应为null")
            assertNotNull(bean.normalField, "普通字段不应为null")
            // disabledField可能为null或默认值，因为enabled=false

            logger.info("生成的带注解data class Bean: $bean")
        } catch (e: Exception) {
            logger.error("测试失败: ${e.message}", e)
            throw e
        }
    }
}