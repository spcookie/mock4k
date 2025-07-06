package io.github.spcookie

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * 测试验证递增计数器在不同模拟实例之间是否正确隔离
 */
class CounterIsolationTest {

    @Test
    @DisplayName("不同的模拟调用应该有隔离的计数器")
    fun testCounterIsolation() {
        val template1 = mapOf("id|+1" to 100)
        val template2 = mapOf("id|+1" to 200)

        // 第一次调用 template1
        val result1a = mock(template1) as Map<String, Any>
        assertEquals(100, result1a["id"])

        // 第一次调用 template2
        val result2a = mock(template2) as Map<String, Any>
        assertEquals(200, result2a["id"])

        // 第二次调用 template1 - 应该重新开始
        val result1b = mock(template1) as Map<String, Any>
        assertEquals(100, result1b["id"])

        // 第二次调用 template2 - 应该重新开始
        val result2b = mock(template2) as Map<String, Any>
        assertEquals(200, result2b["id"])
    }

    @Test
    @DisplayName("在单个模板内，计数器应该正常工作")
    fun testWithinTemplateCounters() {
        val template = mapOf(
            "user" to mapOf(
                "id|+1" to 1000,
                "score|+5" to 0
            ),
            "admin" to mapOf(
                "id|+1" to 2000,
                "score|+10" to 100
            )
        )

        val result = mock(template) as Map<String, Any>
        val user = result["user"] as Map<String, Any>
        val admin = result["admin"] as Map<String, Any>

        // 在同一个模板生成中，计数器应该正常工作
        assertEquals(1000, user["id"])
        assertEquals(0, user["score"])
        assertEquals(2000, admin["id"])
        assertEquals(100, admin["score"])
    }

    @Test
    @DisplayName("数组元素在同一次调用中应该有独立的计数器")
    fun testArrayCounters() {
        val template = listOf(
            mapOf("id|+1" to 100),
            mapOf("id|+1" to 100),
            mapOf("id|+1" to 100)
        )

        val result = mock(template) as List<Map<String, Any>>

        // 在同一次调用中，每个数组元素应该有独立的计数器
        assertEquals(100, result[0]["id"])
        assertEquals(101, result[1]["id"])
        assertEquals(102, result[2]["id"])
    }
}