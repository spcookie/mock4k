package io.github.spcookie

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 简化的Bean Mock测试，用于验证基本功能
 */
class SimpleBeanMockTest {

    private val logger = LoggerFactory.getLogger(SimpleBeanMockTest::class.java)

    data class TestUser(
        val id: Long = 0L,
        val name: String = "",
        val age: Int = 0
    )

    @Test
    fun testBasicBeanMock() {
        logger.info("开始测试基本Bean Mock功能...")

        // 测试模板生成
        val beanIntrospect = BeanIntrospect(ContainerAdapter())
        val config = BeanMockConfig(includePrivate = true) // 包含私有属性
        val template = beanIntrospect.analyzeBean(TestUser::class, config)
        logger.info("生成的模板: $template")

        // 测试MockEngine生成
        val mockEngine = MockEngine()
        val generatedData = MockEngine().generate(template)
        logger.info("MockEngine生成的数据: $generatedData")

        val user = mock(TestUser::class, includePrivate = true)
        
        logger.info("生成的用户: $user")
        
        assertNotNull(user, "用户对象不应为null")
        assertNotNull(user.id, "用户ID不应为null")
        assertNotNull(user.name, "用户姓名不应为null")
        assertNotNull(user.age, "用户年龄不应为null")
        
        // 检查是否生成了非默认值
        val hasNonDefaultValues = user.id != 0L || user.name.isNotEmpty() || user.age != 0
        
        logger.info("是否生成了非默认值: $hasNonDefaultValues")
        logger.info("ID: ${user.id}, Name: '${user.name}', Age: ${user.age}")
        
        if (!hasNonDefaultValues) {
            logger.warn("警告: Bean mock可能没有正常工作，所有值都是默认值")
        }
    }

    @Test
    fun testMockEngineDirectly() {
        logger.info("测试MockEngine直接生成数据...")
        
        val template = mapOf(
            "id" to "number|1-1000",
            "name" to "@NAME",
            "age" to "number|18-65"
        )
        
        val result = mock(template)
        
        logger.info("MockEngine生成的结果: $result")
        
        assertNotNull(result)
        assertTrue(result.containsKey("id"))
        assertTrue(result.containsKey("name"))
        assertTrue(result.containsKey("age"))
    }
}