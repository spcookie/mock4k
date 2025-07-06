package io.github.spcookie

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test for optimized BeanPropertyAnalyzer
 */
class BeanPropertyAnalyzerOptimizedTest {

    private val logger = LoggerFactory.getLogger(BeanPropertyAnalyzerOptimizedTest::class.java)
    private val analyzer = BeanPropertyAnalyzer()
    private val config = BeanPropertyAnalyzer.BeanMockConfig()
    private val mockEngine = MockEngine()
    private val typeAdapter = TypeAdapter()
    private val beanMockEngine = BeanMockEngine(mockEngine, typeAdapter)

    @Test
    fun `test rule-based annotations`() {
        logger.info("=== Testing Rule-based Annotations ===")
        
        val template = analyzer.analyzeBean(TestBeanWithRules::class, config)
        logger.info("Generated template: $template")
        
        // Verify rule-based properties
        assertTrue(template.containsKey("id"))
        assertTrue(template.containsKey("age"))
        assertTrue(template.containsKey("score"))
        assertTrue(template.containsKey("price"))
        
        // Generate mock data
        val mockData = mockEngine.generate(template)
        logger.info("Generated mock data: $mockData")
        
        // Map to bean
        val bean = beanMockEngine.mockBean(TestBeanWithRules::class)
        logger.info("Final bean: $bean")
        
        // Verify constraints
        assertTrue(bean.id in 1000..9999, "ID should be in range 1000-9999, got ${bean.id}")
        assertTrue(bean.age in 18..65, "Age should be in range 18-65, got ${bean.age}")
        assertTrue(bean.score >= 0.0 && bean.score <= 100.0, "Score should be in range 0.0-100.0, got ${bean.score}")
        assertNotNull(bean.price)
    }

    @Test
    fun `test placeholder-based annotations`() {
        logger.info("=== Testing Placeholder-based Annotations ===")
        
        val template = analyzer.analyzeBean(TestBeanWithPlaceholders::class, config)
        logger.info("Generated template: $template")
        
        // Verify placeholder-based properties
        assertTrue(template.containsKey("name"))
        assertTrue(template.containsKey("email"))
        assertTrue(template.containsKey("phone"))
        
        // Generate mock data
        val mockData = mockEngine.generate(template)
        logger.info("Generated mock data: $mockData")
        
        // Map to bean
        val bean = beanMockEngine.mockBean(TestBeanWithPlaceholders::class)
        logger.info("Final bean: $bean")
        
        // Verify placeholder results
        assertTrue(bean.name.contains(" "), "Name should contain space from @FIRST @LAST, got '${bean.name}'")
        assertTrue(bean.email.contains("@"), "Email should contain @, got '${bean.email}'")
        assertNotNull(bean.phone)
    }

    @Test
    fun `test collection with rules`() {
        logger.info("=== Testing Collection with Rules ===")
        
        val template = analyzer.analyzeBean(TestBeanWithCollections::class, config)
        logger.info("Generated template: $template")
        
        // Verify collection properties (keys include rule information)
        val tagsKey = template.keys.find { it.startsWith("tags") }
        val scoresKey = template.keys.find { it.startsWith("scores") }
        val metadataKey = template.keys.find { it.startsWith("metadata") }
        
        assertTrue(tagsKey != null, "Tags key not found in template")
        assertTrue(scoresKey != null, "Scores key not found in template")
        assertTrue(metadataKey != null, "Metadata key not found in template")
        
        // Log template details
        logger.info("Tags template: ${template[tagsKey]}")
        logger.info("Scores template: ${template[scoresKey]}")
        logger.info("Metadata template: ${template[metadataKey]}")
        
        // Generate mock data
        val mockData = mockEngine.generate(template)
        logger.info("Generated mock data: $mockData")
        
        // Log mock data details
        if (mockData is Map<*, *>) {
            logger.info("Tags mock data: ${mockData["tags"]}")
            logger.info("Scores mock data: ${mockData["scores"]}")
            logger.info("Metadata mock data: ${mockData["metadata"]}")
        } else {
            logger.info("Mock data is not a Map, type: ${mockData?.javaClass}")
        }
        
        // Map to bean
        val bean = beanMockEngine.mockBean(TestBeanWithCollections::class)
        logger.info("Final bean: $bean")
        
        // Log bean details
        logger.info("Bean tags: ${bean.tags} (size: ${bean.tags.size})")
        logger.info("Bean scores: ${bean.scores} (size: ${bean.scores.size})")
        logger.info("Bean metadata: ${bean.metadata} (size: ${bean.metadata.size})")
        
        // Verify collections
        assertTrue(bean.tags.size in 2..5, "Tags size should be 2-5, got ${bean.tags.size}")
        assertTrue(bean.scores.size in 1..3, "Scores size should be 1-3, got ${bean.scores.size}")
        assertTrue(bean.metadata.size in 1..4, "Metadata size should be 1-4, got ${bean.metadata.size}")
    }

    @Test
    fun `test nested objects`() {
        logger.info("=== Testing Nested Objects ===")
        
        val template = analyzer.analyzeBean(TestBeanWithNested::class, config)
        logger.info("Generated template: $template")
        
        // Verify nested object properties
        assertTrue(template.containsKey("user"))
        assertTrue(template.containsKey("address"))
        
        // Generate mock data
        val mockData = mockEngine.generate(template)
        logger.info("Generated mock data: $mockData")
        
        // Map to bean
        val bean = beanMockEngine.mockBean(TestBeanWithNested::class)
        logger.info("Final bean: $bean")
        
        // Verify nested objects
        assertNotNull(bean.user)
        assertNotNull(bean.address)
        assertTrue(bean.user.id in 1000..9999, "User ID should be in range 1000-9999, got ${bean.user.id}")
        assertNotNull(bean.address.city)
    }

    @Test
    fun `test disabled properties`() {
        logger.info("=== Testing Disabled Properties ===")
        
        val template = analyzer.analyzeBean(TestBeanWithDisabled::class, config)
        logger.info("Generated template: $template")
        
        // Verify disabled property is not included
        assertTrue(template.containsKey("name"))
        assertTrue(!template.containsKey("secret") || template["secret"] == null)
        
        // Map to bean
        val bean = beanMockEngine.mockBean(TestBeanWithDisabled::class)
        logger.info("Final bean: $bean")
        
        // Verify disabled property remains default
        assertNotNull(bean.name)
        assertEquals(null, bean.secret) // Should remain default value
    }
}

// Test data classes
data class TestBeanWithRules(
    @Mock.Property(rule = Mock.Property.Rule(min = 1000, max = 9999))
    var id: Long = 0L,
    
    @Mock.Property(rule = Mock.Property.Rule(min = 18, max = 65))
    var age: Int = 0,
    
    @Mock.Property(rule = Mock.Property.Rule(min = 0, max = 100, dcount = 2))
    var score: Double = 0.0,
    
    @Mock.Property(rule = Mock.Property.Rule(min = 10, max = 1000))
    var price: java.math.BigDecimal = java.math.BigDecimal.ZERO
)

data class TestBeanWithPlaceholders(
    @Mock.Property(placeholder = Mock.Property.Placeholder(value = "@FIRST @LAST"))
    var name: String = "",
    
    @Mock.Property(placeholder = Mock.Property.Placeholder(value = "@EMAIL"))
    var email: String = "",
    
    @Mock.Property(placeholder = Mock.Property.Placeholder(value = "@natural(10000000000,19999999999)"))
    var phone: String = ""
)

data class TestBeanWithCollections(
    @Mock.Property(rule = Mock.Property.Rule(min = 2, max = 5))
    var tags: List<String> = emptyList(),
    
    @Mock.Property(rule = Mock.Property.Rule(min = 1, max = 3))
    var scores: Set<Int> = emptySet(),
    
    @Mock.Property(rule = Mock.Property.Rule(min = 1, max = 4))
    var metadata: Map<String, String> = emptyMap()
)

data class TestBeanWithNested(
    var user: UserWithMockParamTest = UserWithMockParamTest(),
    var address: Address = Address()
)

data class Address(
    @Mock.Property(placeholder = Mock.Property.Placeholder(value = "@CITY"))
    var city: String = "",
    
    @Mock.Property(placeholder = Mock.Property.Placeholder(value = "@STATE"))
    var state: String = "",
    
    @Mock.Property(rule = Mock.Property.Rule(min = 10000, max = 99999))
    var zipCode: Int = 0
)

data class TestBeanWithDisabled(
    @Mock.Property(placeholder = Mock.Property.Placeholder(value = "@FIRST @LAST"))
    var name: String = "",
    
    @Mock.Property(enabled = false)
    var secret: String? = null
)