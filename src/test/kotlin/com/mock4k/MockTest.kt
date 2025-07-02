package com.mock4k

import com.mock4k.Mock
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class MockTest {
    
    @Test
    fun testBasicMock() {
        val template = mapOf(
            "list|1-10" to listOf(
                mapOf(
                    "id|+1" to 1
                )
            )
        )
        
        val result = Mock.mock(template)
        assertNotNull(result)
        println("Basic mock result: $result")
    }
    
    @Test
    fun testStringRule() {
        val template = mapOf(
            "name|3" to "Hello"
        )
        
        val result = Mock.mock(template) as Map<String, Any>
        val name = result["name"] as String
        assertTrue(name == "HelloHelloHello")
        println("String rule result: $result")
    }
    
    @Test
    fun testNumberRule() {
        val template = mapOf(
            "age|18-65" to 25,
            "score|1-100.1-2" to 85.5
        )
        
        val result = Mock.mock(template) as Map<String, Any>
        assertNotNull(result["age"])
        assertNotNull(result["score"])
        println("Number rule result: $result")
    }
    
    @Test
    fun testArrayRule() {
        val template = mapOf(
            "items|3-5" to listOf("apple", "banana", "orange")
        )
        
        val result = Mock.mock(template) as Map<String, Any>
        val items = result["items"] as List<*>
        assertTrue(items.isNotEmpty())
        println("Array rule result: $result")
    }
    
    @Test
    fun testPlaceholder() {
        val template = mapOf(
            "name" to "@NAME",
            "email" to "@EMAIL",
            "date" to "@DATE",
            "id" to "@GUID"
        )
        
        val result = Mock.mock(template) as Map<String, Any>
        assertNotNull(result["name"])
        assertNotNull(result["email"])
        assertNotNull(result["date"])
        assertNotNull(result["id"])
        println("Placeholder result: $result")
    }
    
    @Test
    fun testComplexTemplate() {
        val template = mapOf(
            "users|5-10" to listOf(
                mapOf(
                    "id|+1" to 1,
                    "name" to "@NAME",
                    "email" to "@EMAIL",
                    "age|18-65" to 25,
                    "address" to mapOf(
                        "city" to "@WORD",
                        "street" to "@SENTENCE"
                    ),
                    "tags|1-3" to listOf("tag1", "tag2", "tag3", "tag4")
                )
            )
        )
        
        val result = Mock.mock(template) as Map<String, Any>
        val users = result["users"] as List<*>
        assertTrue(users.isNotEmpty())
        println("Complex template result: $result")
    }
    
    @Test
    fun testIncrementRule() {
        val template = mapOf(
            "list|3" to listOf(
                mapOf(
                    "id|+1" to 100,
                    "name" to "Item"
                )
            )
        )
        
        val result = Mock.mock(template) as Map<String, Any>
        val list = result["list"] as List<*>
        assertTrue(list.size == 3)
        println("Increment rule result: $result")
    }
    
    @Test
    fun testBooleanRule() {
        val template = mapOf(
            "isActive|1" to true,
            "isEnabled|3-7" to false
        )
        
        val result = Mock.mock(template) as Map<String, Any>
        assertNotNull(result["isActive"])
        assertNotNull(result["isEnabled"])
        println("Boolean rule result: $result")
    }
    
    @Test
    fun testObjectRule() {
        val template = mapOf(
            "config|2-3" to mapOf(
                "debug" to true,
                "timeout" to 5000,
                "retries" to 3,
                "cache" to false,
                "logging" to true
            )
        )
        
        val result = Mock.mock(template) as Map<String, Any>
        val config = result["config"] as Map<*, *>
        assertTrue(config.size in 2..3)
        println("Object rule result: $result")
    }
    
    @Test
    fun testRandomMethods() {
        println("Random boolean: ${Mock.Random.boolean()}")
        println("Random integer: ${Mock.Random.integer(1, 100)}")
        println("Random string: ${Mock.Random.string(10)}")
        println("Random name: ${Mock.Random.name()}")
        println("Random email: ${Mock.Random.email()}")
        println("Random date: ${Mock.Random.date()}")
        println("Random color: ${Mock.Random.color()}")
        println("Random GUID: ${Mock.Random.guid()}")
    }
}