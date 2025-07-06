package io.github.spcookie

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.test.assertTrue

class CollectionRuleDebugTest {
    
    private val logger = LoggerFactory.getLogger(CollectionRuleDebugTest::class.java)
    private val mockEngine = MockEngine()
    
    @Test
    fun `debug collection rule processing`() {
        logger.info("=== Debug Collection Rule Processing ===")
        
        // Test the template that BeanPropertyAnalyzer generates
        val template = mapOf(
            "tags|2-5" to listOf("@word")
        )
        
        logger.info("Input template: $template")
        
        val result = mockEngine.generate(template)
        logger.info("Generated result: $result")
        
        if (result is Map<*, *>) {
            val tags = result["tags"]
            logger.info("Tags result: $tags (type: ${tags?.javaClass})")
            
            if (tags is List<*>) {
                logger.info("Tags size: ${tags.size}")
                logger.info("Tags content: $tags")
                assertTrue(tags.size in 2..5, "Expected tags size to be 2-5, got ${tags.size}")
            } else {
                logger.error("Tags is not a List, got: ${tags?.javaClass}")
            }
        }
    }
    
    @Test
    fun `test different collection templates`() {
        logger.info("=== Testing Different Collection Templates ===")
        
        // Test 1: Array repeat template (current behavior)
        val template1 = mapOf(
            "items|3" to listOf("@word")
        )
        logger.info("Template 1 (repeat): $template1")
        val result1 = mockEngine.generate(template1)
        logger.info("Result 1: $result1")
        
        // Test 2: What we actually need for collection size control
        val template2 = mapOf(
            "items" to "@range(2,5)".let { range ->
                // This should generate a list with 2-5 elements
                listOf("@word") // But this is still wrong approach
            }
        )
        logger.info("Template 2 (size control attempt): $template2")
        val result2 = mockEngine.generate(template2)
        logger.info("Result 2: $result2")
    }
}