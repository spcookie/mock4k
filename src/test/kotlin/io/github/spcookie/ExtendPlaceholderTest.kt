package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * 自定义占位符扩展功能的测试用例
 *
 * @author spcookie
 * @since 1.0.0
 * @date 2024-12-19
 */
class ExtendPlaceholderTest {

    @BeforeEach
    fun setUp() {
        // 在每个测试前清除任何现有的自定义占位符
        Mock.Random.clearExtended()
    }

    @Test
    fun `test extend with single placeholder without parameters`() {
        // 注册一个自定义占位符
        Mock.Random.extend("customGreeting") { "Hello, World!" }

        // 测试自定义占位符
        val result = Mock.mock("@customGreeting")
        assertEquals("Hello, World!", result)
    }

    @Test
    fun `test extend with single placeholder with parameters`() {
        // 注册一个带参数的自定义占位符
        Mock.Random.extendWithParams("repeat") { params ->
            val text = params.getOrNull(0)?.toString() ?: "default"
            val count = params.getOrNull(1) as? Int ?: 1
            (1..count).joinToString("") { text }
        }

        // 测试带参数的自定义占位符
        val result = Mock.mock("@repeat('Hi', 3)")
        assertEquals("HiHiHi", result)
    }

    @Test
    fun `test extend with map of placeholders`() {
        // 注册多个自定义占位符
        Mock.Random.extend(
            mapOf(
            "customName" to { "John Doe" },
            "customAge" to { 25 },
            "customCity" to { "New York" }
        ))

        // 测试自定义占位符
        assertEquals("John Doe", Mock.mock("@customName"))
        assertEquals(25, Mock.mock("@customAge"))
        assertEquals("New York", Mock.mock("@customCity"))
    }

    @Test
    fun `test hasExtended`() {
        Mock.Random.extend("testPlaceholder") { "test" }

        assertTrue(Mock.Random.hasExtended("testPlaceholder"))
        assertTrue(Mock.Random.hasExtended("TESTPLACEHOLDER")) // 不区分大小写
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
        // 测试扩展方法返回 MockRandom 以支持链式调用
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
        // 注册一个抛出异常的自定义占位符
        Mock.Random.extend("errorPlaceholder") {
            throw RuntimeException("Test error")
        }

        // 当发生错误时应该回退到原始占位符字符串
        val result = Mock.mock("@errorPlaceholder")
        assertEquals("@errorPlaceholder", result)
    }

    @Test
    fun `test fallback to built-in when custom placeholder not found`() {
        // 测试当没有注册自定义占位符时内置占位符仍然有效
        val result = Mock.mock("@name")
        assertNotNull(result)
        assertTrue(result is String)
        assertTrue((result as String).isNotEmpty())
    }

    @Test
    fun `demonstrate extended placeholder usage`() {
        // 使用新的命名约定注册简单占位符
        MockRandom.extend("company") { "TechCorp" }
        MockRandom.extend("department") { "Engineering" }

        // 注册带参数的占位符
        MockRandom.extendWithParams("greeting") { params ->
            val name = params.getOrNull(0) ?: "World"
            "Hello, $name!"
        }

        // 使用映射批量注册
        MockRandom.extend(
            mapOf(
            "status" to { "Active" },
            "priority" to { "High" }
        ))

        // 测试简单占位符
        val template1 = "@company - @department"
        val result1 = Mock.mock(template1)
        assertEquals("TechCorp - Engineering", result1)

        // 测试参数化占位符
        val template2 = "@greeting(Alice)"
        val result2 = Mock.mock(template2)
        assertEquals("Hello, Alice!", result2)

        // 测试复杂模板
        val complexTemplate = "Company: @company, Department: @department, Status: @status, Priority: @priority"
        val complexResult = Mock.mock(complexTemplate)
        assertEquals("Company: TechCorp, Department: Engineering, Status: Active, Priority: High", complexResult)

        // 演示实用方法
        assertTrue(MockRandom.hasExtended("company"))
        assertTrue(MockRandom.hasExtended("greeting"))
        assertFalse(MockRandom.hasExtended("nonexistent"))

        val placeholderNames = MockRandom.getExtendedNames()
        assertTrue(placeholderNames.contains("company"))
        assertTrue(placeholderNames.contains("greeting"))
        assertTrue(placeholderNames.contains("status"))
        assertTrue(placeholderNames.contains("priority"))

        // 测试移除
        MockRandom.removeExtended("status")
        assertFalse(MockRandom.hasExtended("status"))

        // 测试方法链式调用
        MockRandom.extend("temp") { "temporary" }
        assertTrue(MockRandom.hasExtended("temp"))

        // 测试错误处理
        val invalidTemplate = "@nonexistent"
        val invalidResult = Mock.mock(invalidTemplate)
        assertEquals("@nonexistent", invalidResult) // 应该返回原始占位符

        // 测试清理
        MockRandom.clearExtended()
        assertFalse(MockRandom.hasExtended("company"))
        assertTrue(MockRandom.getExtendedNames().isEmpty())

        println("Extended placeholder example completed successfully!")
    }
}