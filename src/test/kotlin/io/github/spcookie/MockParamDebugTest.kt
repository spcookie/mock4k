package io.github.spcookie

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory

class MockParamDebugTest {

    private val logger = LoggerFactory.getLogger(MockParamDebugTest::class.java)

    @Test
    fun testMockParamDebug() {
        logger.info("=== MockParam Debug Test ===")
        
        // 1. 测试 BeanPropertyAnalyzer
        val analyzer = BeanPropertyAnalyzer()
        val config = BeanPropertyAnalyzer.BeanMockConfig()
        val template = analyzer.analyzeBean(UserWithMockParamTest::class, config)
        
        logger.info("Generated template: $template")
        
        // 2. 测试 MockEngine
        val mockEngine = MockEngine()
        val result = mockEngine.generate(template)
        
        logger.info("MockEngine result: $result")
        
        // 3. 测试完整的 Bean mock
        val user = MockObject.beanMockEngine.mockBean(UserWithMockParamTest::class)
        
        logger.info("Final bean result: $user")
        logger.info("ID: ${user.id} (should be 1000-9999)")
        logger.info("Name: '${user.name}' (should contain space for @FIRST @LAST)")
        logger.info("Age: ${user.age} (should be 18-65)")
        
        // 验证结果
        assertNotNull(user)
        assertTrue(user.id in 1000..9999, "ID should be in range 1000-9999, actual: ${user.id}")
        assertTrue(user.age in 18..65, "Age should be in range 18-65, actual: ${user.age}")
        
        // 对于姓名，我们需要检查是否正确处理了占位符
        logger.info("Name analysis: '${user.name}'")
        // 如果占位符没有正确处理，可能会直接返回 "@FIRST @LAST" 字符串
        if (user.name == "@FIRST @LAST") {
            logger.warn("WARNING: Placeholder not processed correctly!")
        } else if (user.name.contains(" ")) {
            logger.info("SUCCESS: Name contains space, likely processed correctly")
        } else {
            logger.info("INFO: Name doesn't contain space, might be single word")
        }
    }
    
    @Test
    fun testPlaceholderProcessing() {
        logger.info("=== Placeholder Processing Test ===")
        
        // 直接测试占位符处理
        val template = "@FIRST @LAST"
        val mockEngine = MockEngine()
        val result = mockEngine.generate(template)
        
        logger.info("Direct placeholder test:")
        logger.info("Template: '$template'")
        logger.info("Result: '$result'")
        
        // 测试单个占位符
        val firstResult = mockEngine.generate("@FIRST")
        val lastResult = mockEngine.generate("@LAST")
        
        logger.info("@FIRST result: '$firstResult'")
        logger.info("@LAST result: '$lastResult'")
    }
}