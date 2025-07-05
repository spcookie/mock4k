package io.github.spcookie

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * 测试验证不同模拟调用之间递增计数器的隔离性
 */
class IncrementIsolationTest {

    @Test
    @DisplayName("不同属性名称每次都应该从初始值开始")
    fun testDifferentPropertyCounters() {
        val template1 = mapOf("id1|+1" to 100)
        val template2 = mapOf("id2|+1" to 200)

        // Generate from both templates multiple times
        val results1 = mutableListOf<Int>()
        val results2 = mutableListOf<Int>()

        repeat(3) {
            val result1 = Mock.mock(template1) as Map<String, Any>
            val result2 = Mock.mock(template2) as Map<String, Any>
            results1.add(result1["id1"] as Int)
            results2.add(result2["id2"] as Int)
        }

        // 每次调用都应该从初始值开始，因为计数器在每次模拟调用后都会被清除
        assertEquals(listOf(100, 100, 100), results1)
        assertEquals(listOf(200, 200, 200), results2)
    }

    @Test
    @DisplayName("相同属性名称每次都应该从初始值开始")
    fun testSamePropertyNameIndependentCounter() {
        val template1 = mapOf("id|+1" to 100)
        val template2 = mapOf("id|+1" to 200)  // 相同属性名称，不同初始值

        // Generate from both templates alternately
        val results1 = mutableListOf<Int>()
        val results2 = mutableListOf<Int>()

        repeat(3) {
            val result1 = Mock.mock(template1) as Map<String, Any>
            val result2 = Mock.mock(template2) as Map<String, Any>
            results1.add(result1["id"] as Int)
            results2.add(result2["id"] as Int)
        }

        // 每次调用都应该从初始值开始，因为计数器在每次模拟调用后都会被清除
        assertEquals(listOf(100, 100, 100), results1)
        assertEquals(listOf(200, 200, 200), results2)
    }

    @Test
    @DisplayName("同一模板中的多个属性应该在单次调用内递增")
    fun testMultiplePropertiesInSameTemplate() {
        val template = listOf(
            mapOf("id|+1" to 100),
            mapOf("id|+1" to 100),
            mapOf("id|+1" to 100)
        )

        // 生成一次并检查相同属性在同一次调用内递增
        val result = Mock.mock(template) as List<Map<String, Any>>

        // 在单次模板生成中，相同属性应该在数组元素间递增
        assertEquals(100, result[0]["id"])
        assertEquals(101, result[1]["id"])
        assertEquals(102, result[2]["id"])
    }
}