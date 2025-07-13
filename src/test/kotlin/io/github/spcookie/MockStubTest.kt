package io.github.spcookie

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Test class for mockStub functionality
 */
class MockStubTest {

    // Test class with various method types
    open class TestService {
        open fun getString(): String = "original"
        open fun getInt(): Int = 42
        open fun getBoolean(): Boolean = false
        open fun getList(): List<String> = listOf("original")
        open fun voidMethod(): Unit = Unit
        open fun getComplexObject(): TestData = TestData("original", 0)
    }

    data class TestData(val name: String, val value: Int)

    @Test
    fun `test mockStub with reified type`() {
        val stub = load<TestService>()

        Assertions.assertNotNull(stub)
        assertTrue(stub is TestService)

        // Test that methods return mock values instead of original values
        val stringResult = stub.getString()
        Assertions.assertNotNull(stringResult)
        Assertions.assertNotEquals("original", stringResult)

        val intResult = stub.getInt()
        Assertions.assertNotEquals(42, intResult)

        val booleanResult = stub.getBoolean()
        // Boolean can be true or false, so we just check it's not null
        Assertions.assertNotNull(booleanResult)

        val listResult = stub.getList()
        Assertions.assertNotNull(listResult)
        // Should return empty list for collections
        assertTrue(listResult.isEmpty())

        // Void method should work without issues
        Assertions.assertDoesNotThrow { stub.voidMethod() }

        val complexResult = stub.getComplexObject()
        Assertions.assertNotNull(complexResult)
        // Should be a mock object, not the original
        Assertions.assertNotEquals(TestData("original", 0), complexResult)
    }

    @Test
    fun `test mockStub with KClass`() {
        val stub = load(TestService::class)

        Assertions.assertNotNull(stub)
        assertTrue(stub is TestService)

        val result = stub.getString()
        Assertions.assertNotNull(result)
        Assertions.assertNotEquals("original", result)
    }

    @Test
    fun `test mockStub with Java Class`() {
        val stub = load(TestService::class.java)

        Assertions.assertNotNull(stub)
        assertTrue(stub is TestService)

        val result = stub.getString()
        Assertions.assertNotNull(result)
        Assertions.assertNotEquals("original", result)
    }

    @Test
    fun `test mockStub with final class should throw exception`() {
        // String is a final class, should not be able to subclass
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            load(String::class.java)
        }
    }
}