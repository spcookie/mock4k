package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Test cases for custom placeholder extension functionality
 *
 * @author spcookie
 * @since 1.0.0
 * @date 2024-12-19
 */
class ExtendPlaceholderTest {

    @BeforeEach
    fun setUp() {
        // Clear any existing custom placeholders before each test
        Mock.Random.clearExtended()
    }

    @Test
    fun `test extend with single placeholder without parameters`() {
        // Register a custom placeholder
        Mock.Random.extend("customGreeting") { "Hello, World!" }

        // Test the custom placeholder
        val result = Mock.mock("@customGreeting")
        assertEquals("Hello, World!", result)
    }

    @Test
    fun `test extend with single placeholder with parameters`() {
        // Register a custom placeholder with parameters
        Mock.Random.extendWithParams("repeat") { params ->
            val text = params.getOrNull(0)?.toString() ?: "default"
            val count = params.getOrNull(1) as? Int ?: 1
            (1..count).joinToString("") { text }
        }

        // Test the custom placeholder with parameters
        val result = Mock.mock("@repeat('Hi', 3)")
        assertEquals("HiHiHi", result)
    }

    @Test
    fun `test extend with map of placeholders`() {
        // Register multiple custom placeholders
        Mock.Random.extend(
            mapOf(
            "customName" to { "John Doe" },
            "customAge" to { 25 },
            "customCity" to { "New York" }
        ))

        // Test the custom placeholders
        assertEquals("John Doe", Mock.mock("@customName"))
        assertEquals(25, Mock.mock("@customAge"))
        assertEquals("New York", Mock.mock("@customCity"))
    }

    @Test
    fun `test hasExtended`() {
        Mock.Random.extend("testPlaceholder") { "test" }

        assertTrue(Mock.Random.hasExtended("testPlaceholder"))
        assertTrue(Mock.Random.hasExtended("TESTPLACEHOLDER")) // case insensitive
        assertFalse(Mock.Random.hasExtended("nonExistent"))
    }

    @Test
    fun `test removeExtended`() {
        Mock.Random.extend("toRemove") { "test" }
        assertTrue(Mock.Random.hasExtended("toRemove"))

        Mock.Random.removeExtended("toRemove")
        assertFalse(Mock.Random.hasExtended("toRemove"))
    }

    @Test
    fun `test getExtendedNames`() {
        Mock.Random.extend(
            mapOf(
            "placeholder1" to { "test1" },
            "placeholder2" to { "test2" }
        ))

        Mock.Random.extendWithParams("placeholder3") { _ -> "test3" }

        val names = Mock.Random.getExtendedNames()
        assertEquals(3, names.size)
        assertTrue(names.contains("placeholder1"))
        assertTrue(names.contains("placeholder2"))
        assertTrue(names.contains("placeholder3"))
    }

    @Test
    fun `test clearExtendedPlaceholders`() {
        Mock.Random.extend(
            mapOf(
            "placeholder1" to { "test1" },
            "placeholder2" to { "test2" }
        ))

        assertTrue(Mock.Random.getExtendedNames().isNotEmpty())

        Mock.Random.clearExtended()
        assertTrue(Mock.Random.getExtendedNames().isEmpty())
    }

    @Test
    fun `test method chaining`() {
        // Test that extend methods return MockRandom for chaining
        val result = Mock.Random
            .extend("test1") { "value1" }
            .extend(mapOf("test2" to { "value2" }))
            .extendWithParams("test3") { _ -> "value3" }

        assertSame(Mock.Random, result)
        assertTrue(Mock.Random.hasExtended("test1"))
        assertTrue(Mock.Random.hasExtended("test2"))
        assertTrue(Mock.Random.hasExtended("test3"))
    }

    @Test
    fun `test error handling in custom placeholders`() {
        // Register a custom placeholder that throws an exception
        Mock.Random.extend("errorPlaceholder") {
            throw RuntimeException("Test error")
        }

        // Should fallback to the original placeholder string when error occurs
        val result = Mock.mock("@errorPlaceholder")
        assertEquals("@errorPlaceholder", result)
    }

    @Test
    fun `test fallback to built-in when custom placeholder not found`() {
        // Test that built-in placeholders still work when no custom placeholder is registered
        val result = Mock.mock("@name")
        assertNotNull(result)
        assertTrue(result is String)
        assertTrue((result as String).isNotEmpty())
    }

    @Test
    fun `demonstrate extended placeholder usage`() {
        // Register simple placeholders using the new naming convention
        MockRandom.extend("company") { "TechCorp" }
        MockRandom.extend("department") { "Engineering" }

        // Register placeholders with parameters
        MockRandom.extendWithParams("greeting") { params ->
            val name = params.getOrNull(0) ?: "World"
            "Hello, $name!"
        }

        // Batch registration using map
        MockRandom.extend(
            mapOf(
            "status" to { "Active" },
            "priority" to { "High" }
        ))

        // Test simple placeholders
        val template1 = "@company - @department"
        val result1 = Mock.mock(template1)
        assertEquals("TechCorp - Engineering", result1)

        // Test parameterized placeholders
        val template2 = "@greeting(Alice)"
        val result2 = Mock.mock(template2)
        assertEquals("Hello, Alice!", result2)

        // Test complex template
        val complexTemplate = "Company: @company, Department: @department, Status: @status, Priority: @priority"
        val complexResult = Mock.mock(complexTemplate)
        assertEquals("Company: TechCorp, Department: Engineering, Status: Active, Priority: High", complexResult)

        // Demonstrate utility methods
        assertTrue(MockRandom.hasExtended("company"))
        assertTrue(MockRandom.hasExtended("greeting"))
        assertFalse(MockRandom.hasExtended("nonexistent"))

        val placeholderNames = MockRandom.getExtendedNames()
        assertTrue(placeholderNames.contains("company"))
        assertTrue(placeholderNames.contains("greeting"))
        assertTrue(placeholderNames.contains("status"))
        assertTrue(placeholderNames.contains("priority"))

        // Test removal
        MockRandom.removeExtended("status")
        assertFalse(MockRandom.hasExtended("status"))

        // Test method chaining
        MockRandom.extend("temp") { "temporary" }
        assertTrue(MockRandom.hasExtended("temp"))

        // Test error handling
        val invalidTemplate = "@nonexistent"
        val invalidResult = Mock.mock(invalidTemplate)
        assertEquals("@nonexistent", invalidResult) // Should return original placeholder

        // Test cleanup
        MockRandom.clearExtended()
        assertFalse(MockRandom.hasExtended("company"))
        assertTrue(MockRandom.getExtendedNames().isEmpty())

        println("Extended placeholder example completed successfully!")
    }
}