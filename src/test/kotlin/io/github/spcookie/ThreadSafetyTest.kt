package io.github.spcookie

import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

/**
 * Test thread safety of MockRandom
 */
class ThreadSafetyTest {

    @Test
    fun `test MockRandom thread safety with basic operations`() {
        val threadCount = 10
        val operationsPerThread = 100
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = mutableListOf<Any>()
        val exceptions = mutableListOf<Exception>()

        repeat(threadCount) {
            executor.submit {
                try {
                    repeat(operationsPerThread) {
                        // Test various MockRandom operations
                        val randomInt = MockRandom.integer(1, 100)
                        val randomString = MockRandom.string(10)
                        val randomBoolean = MockRandom.boolean()
                        val randomFloat = MockRandom.float(0.0, 1.0)
                        val randomWord = MockRandom.word()

                        synchronized(results) {
                            results.add(randomInt)
                            results.add(randomString)
                            results.add(randomBoolean)
                            results.add(randomFloat)
                            results.add(randomWord)
                        }
                    }
                } catch (e: Exception) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        // Wait for all threads to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Threads did not complete in time")
        executor.shutdown()

        // Check that no exceptions occurred
        assertTrue(
            exceptions.isEmpty(),
            "Exceptions occurred during concurrent execution: ${exceptions.joinToString()}"
        )

        // Check that we got the expected number of results
        val expectedResults = threadCount * operationsPerThread * 5 // 5 operations per iteration
        assertTrue(results.size == expectedResults, "Expected $expectedResults results, got ${results.size}")
    }

    @Test
    fun `test MockRandom thread safety with custom placeholders`() {
        val threadCount = 5
        val operationsPerThread = 50
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val exceptions = mutableListOf<Exception>()

        // Register some custom placeholders
        MockRandom.extend("threadTest") { "Thread-${Thread.currentThread().id}" }
        MockRandom.extendWithParams("threadTestWithParam") { params ->
            "Thread-${Thread.currentThread().id}-${params.firstOrNull() ?: "default"}"
        }

        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    repeat(operationsPerThread) {
                        // Test custom placeholder operations
                        val hasExtended = MockRandom.hasExtended("threadTest")
                        val customResult = MockRandom.getExtended("threadTest")?.invoke()
                        val customWithParamResult =
                            MockRandom.getExtendedWithParams("threadTestWithParam")?.invoke(listOf(threadIndex))

                        assertTrue(hasExtended, "Custom placeholder should exist")
                        assertTrue(customResult != null, "Custom placeholder should return a result")
                        assertTrue(
                            customWithParamResult != null,
                            "Custom placeholder with params should return a result"
                        )
                    }
                } catch (e: Exception) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        // Wait for all threads to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Threads did not complete in time")
        executor.shutdown()

        // Check that no exceptions occurred
        assertTrue(
            exceptions.isEmpty(),
            "Exceptions occurred during concurrent execution: ${exceptions.joinToString()}"
        )

        // Clean up
        MockRandom.clearExtended()
    }

    @Test
    fun `test Mock mock method thread safety`() {
        val threadCount = 8
        val operationsPerThread = 25
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = mutableListOf<Any?>()
        val exceptions = mutableListOf<Exception>()

        val template = mapOf(
            "id" to "@integer(1,1000)",
            "name" to "@name",
            "email" to "@email",
            "age" to "@integer(18,65)",
            "active" to "@boolean"
        )

        repeat(threadCount) {
            executor.submit {
                try {
                    repeat(operationsPerThread) {
                        val result = Mock.mock(template)
                        synchronized(results) {
                            results.add(result)
                        }
                    }
                } catch (e: Exception) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        // Wait for all threads to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Threads did not complete in time")
        executor.shutdown()

        // Check that no exceptions occurred
        assertTrue(
            exceptions.isEmpty(),
            "Exceptions occurred during concurrent execution: ${exceptions.joinToString()}"
        )

        // Check that we got the expected number of results
        val expectedResults = threadCount * operationsPerThread
        assertTrue(results.size == expectedResults, "Expected $expectedResults results, got ${results.size}")

        // Verify that all results are valid maps
        results.forEach { result ->
            assertTrue(result is Map<*, *>, "Result should be a Map")
            val map = result
            assertTrue(map.containsKey("id"), "Result should contain 'id' key")
            assertTrue(map.containsKey("name"), "Result should contain 'name' key")
            assertTrue(map.containsKey("email"), "Result should contain 'email' key")
            assertTrue(map.containsKey("age"), "Result should contain 'age' key")
            assertTrue(map.containsKey("active"), "Result should contain 'active' key")
        }
    }
}