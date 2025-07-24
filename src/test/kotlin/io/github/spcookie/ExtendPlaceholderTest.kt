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
        GlobalMockConf.Random.clearExtended()
    }

    @Test
    fun `test extend with single placeholder without parameters`() {
        // 注册一个自定义占位符
        GlobalMockConf.Random.extend("customGreeting") { "Hello, World!" }

        // 测试自定义占位符
        val result = mock(mapOf("a" to "@customGreeting"))
        assertEquals("Hello, World!", result["a"])
    }

    @Test
    fun `test extend with single placeholder with parameters`() {
        // 注册一个带参数的自定义占位符
        GlobalMockConf.Random.extendWithParams("repeat") { params ->
            val text = params.getOrNull(0)?.toString() ?: "default"
            val count = params.getOrNull(1) as? Int ?: 1
            (1..count).joinToString("") { text }
        }

        // 测试带参数的自定义占位符
        val result = mock(mapOf("a" to "@repeat('Hi', 3)"))
        assertEquals("HiHiHi", result["a"])
    }

    @Test
    fun `test extend with map of placeholders`() {
        // 注册多个自定义占位符
        GlobalMockConf.Random.extend(
            mapOf(
            "customName" to { "John Doe" },
            "customAge" to { 25 },
            "customCity" to { "New York" }
        ))

        // 测试自定义占位符
        assertEquals("John Doe", mock(mapOf("a" to "@customName"))["a"])
        assertEquals(25, mock(mapOf("a" to "@customAge"))["a"])
        assertEquals("New York", mock(mapOf("a" to "@customCity"))["a"])
    }

    @Test
    fun `test hasExtended`() {
        GlobalMockConf.Random.extend("testPlaceholder") { "test" }

        assertTrue(GlobalMockConf.Random.hasExtended("testPlaceholder"))
        assertTrue(GlobalMockConf.Random.hasExtended("TESTPLACEHOLDER")) // 不区分大小写
        assertFalse(GlobalMockConf.Random.hasExtended("nonExistent"))
    }

    @Test
    fun `test removeExtended`() {
        GlobalMockConf.Random.extend("toRemove") { "test" }
        assertTrue(GlobalMockConf.Random.hasExtended("toRemove"))

        GlobalMockConf.Random.removeExtended("toRemove")
        assertFalse(GlobalMockConf.Random.hasExtended("toRemove"))
    }

    @Test
    fun `test getExtendedNames`() {
        GlobalMockConf.Random.extend(
            mapOf(
            "placeholder1" to { "test1" },
            "placeholder2" to { "test2" }
        ))

        GlobalMockConf.Random.extendWithParams("placeholder3") { _ -> "test3" }

        val names = GlobalMockConf.Random.getExtendedNames()
        assertEquals(3, names.size)
        assertTrue(names.contains("placeholder1"))
        assertTrue(names.contains("placeholder2"))
        assertTrue(names.contains("placeholder3"))
    }

    @Test
    fun `test clearExtendedPlaceholders`() {
        GlobalMockConf.Random.extend(
            mapOf(
            "placeholder1" to { "test1" },
            "placeholder2" to { "test2" }
        ))

        assertTrue(GlobalMockConf.Random.getExtendedNames().isNotEmpty())

        GlobalMockConf.Random.clearExtended()
        assertTrue(GlobalMockConf.Random.getExtendedNames().isEmpty())
    }

    @Test
    fun `test method chaining`() {
        // 测试扩展方法返回 MockRandom 以支持链式调用
        val result = GlobalMockConf.Random
            .extend("test1") { "value1" }
            .extend(mapOf("test2" to { "value2" }))
            .extendWithParams("test3") { _ -> "value3" }

        assertSame(GlobalMockConf.Random, result)
        assertTrue(GlobalMockConf.Random.hasExtended("test1"))
        assertTrue(GlobalMockConf.Random.hasExtended("test2"))
        assertTrue(GlobalMockConf.Random.hasExtended("test3"))
    }

    @Test
    fun `test error handling in custom placeholders`() {
        // 注册一个抛出异常的自定义占位符
        GlobalMockConf.Random.extend("errorPlaceholder") {
            throw RuntimeException("Test error")
        }

        // 当发生错误时应该回退到原始占位符字符串
        val result = mock(mapOf("a" to "@errorPlaceholder"))
        assertEquals("@errorPlaceholder", result["a"])
    }

    @Test
    fun `test fallback to built-in when custom placeholder not found`() {
        // 测试当没有注册自定义占位符时内置占位符仍然有效
        val result = mock(mapOf("a" to "@name"))
        assertNotNull(result)
        assertTrue(result["a"] is String)
        assertTrue((result["a"] as String).isNotEmpty())
    }

    @Test
    fun `demonstrate extended placeholder usage`() {
        // 使用新的命名约定注册简单占位符
        GlobalMockConf.Random.extend("company") { "TechCorp" }
        GlobalMockConf.Random.extend("department") { "Engineering" }

        // 注册带参数的占位符
        GlobalMockConf.Random.extendWithParams("greeting") { params ->
            val name = params.getOrNull(0) ?: "World"
            "Hello, $name!"
        }

        // 使用映射批量注册
        GlobalMockConf.Random.extend(
            mapOf(
            "status" to { "Active" },
            "priority" to { "High" }
        ))

        // 测试简单占位符
        val template1 = "@company - @department"
        val result1 = mock(mapOf("a" to template1))
        assertEquals("TechCorp - Engineering", result1["a"])

        // 测试参数化占位符
        val template2 = "@greeting(Alice)"
        val result2 = mock(mapOf("a" to template2))
        assertEquals("Hello, Alice!", result2["a"])

        // 测试复杂模板
        val complexTemplate = "Company: @company, Department: @department, Status: @status, Priority: @priority"
        val complexResult = mock(mapOf("a" to complexTemplate))
        assertEquals("Company: TechCorp, Department: Engineering, Status: Active, Priority: High", complexResult["a"])

        // 演示实用方法
        assertTrue(GlobalMockConf.Random.hasExtended("company"))
        assertTrue(GlobalMockConf.Random.hasExtended("greeting"))
        assertFalse(GlobalMockConf.Random.hasExtended("nonexistent"))

        val placeholderNames = GlobalMockConf.Random.getExtendedNames()
        assertTrue(placeholderNames.contains("company"))
        assertTrue(placeholderNames.contains("greeting"))
        assertTrue(placeholderNames.contains("status"))
        assertTrue(placeholderNames.contains("priority"))

        // 测试移除
        GlobalMockConf.Random.removeExtended("status")
        assertFalse(GlobalMockConf.Random.hasExtended("status"))

        // 测试方法链式调用
        GlobalMockConf.Random.extend("temp") { "temporary" }
        assertTrue(GlobalMockConf.Random.hasExtended("temp"))

        // 测试错误处理
        val invalidTemplate = "@nonexistent"
        val invalidResult = mock(mapOf("a" to invalidTemplate))
        assertEquals("@nonexistent", invalidResult["a"]) // 应该返回原始占位符

        // 测试清理
        GlobalMockConf.Random.clearExtended()
        assertFalse(GlobalMockConf.Random.hasExtended("company"))
        assertTrue(GlobalMockConf.Random.getExtendedNames().isEmpty())

        println("Extended placeholder example completed successfully!")
    }
}