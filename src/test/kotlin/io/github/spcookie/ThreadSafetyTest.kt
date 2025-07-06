package io.github.spcookie

import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

/**
 * 测试 MockRandom 的线程安全性
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
                        // 测试各种 MockRandom 操作
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

        // 等待所有线程完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "线程未能及时完成")
        executor.shutdown()

        // 检查是否没有异常发生
        assertTrue(
            exceptions.isEmpty(),
            "并发执行期间发生异常: ${exceptions.joinToString()}"
        )

        // 检查是否得到了预期数量的结果
        val expectedResults = threadCount * operationsPerThread * 5 // 每次迭代5个操作
        assertTrue(results.size == expectedResults, "期望 $expectedResults 个结果，实际得到 ${results.size}")
    }

    @Test
    fun `test MockRandom thread safety with custom placeholders`() {
        val threadCount = 5
        val operationsPerThread = 50
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val exceptions = mutableListOf<Exception>()

        // 注册一些自定义占位符
        MockRandom.extend("threadTest") { "Thread-${Thread.currentThread().id}" }
        MockRandom.extendWithParams("threadTestWithParam") { params ->
            "Thread-${Thread.currentThread().id}-${params.firstOrNull() ?: "default"}"
        }

        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    repeat(operationsPerThread) {
                        // 测试自定义占位符操作
                        val hasExtended = MockRandom.hasExtended("threadTest")
                        val customResult = MockRandom.getExtended("threadTest")?.invoke()
                        val customWithParamResult =
                            MockRandom.getExtendedWithParams("threadTestWithParam")?.invoke(listOf(threadIndex))

                        assertTrue(hasExtended, "自定义占位符应该存在")
                        assertTrue(customResult != null, "自定义占位符应该返回结果")
                        assertTrue(
                            customWithParamResult != null,
                            "带参数的自定义占位符应该返回结果"
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
        assertTrue(latch.await(30, TimeUnit.SECONDS), "线程未能及时完成")
        executor.shutdown()

        // Check that no exceptions occurred
        assertTrue(
            exceptions.isEmpty(),
            "并发执行期间发生异常: ${exceptions.joinToString()}"
        )

        // 清理
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
                        val result = mock(template)
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
        assertTrue(latch.await(30, TimeUnit.SECONDS), "线程未能及时完成")
        executor.shutdown()

        // Check that no exceptions occurred
        assertTrue(
            exceptions.isEmpty(),
            "并发执行期间发生异常: ${exceptions.joinToString()}"
        )

        // 检查我们是否得到了预期数量的结果
        val expectedResults = threadCount * operationsPerThread
        assertTrue(results.size == expectedResults, "期望 $expectedResults 个结果，实际得到 ${results.size}")

        // 验证所有结果都是有效的映射
        results.forEach { result ->
            assertTrue(result is Map<*, *>, "结果应该是一个Map")
            val map = result
            assertTrue(map.containsKey("id"), "结果应该包含'id'键")
            assertTrue(map.containsKey("name"), "结果应该包含'name'键")
            assertTrue(map.containsKey("email"), "结果应该包含'email'键")
            assertTrue(map.containsKey("age"), "结果应该包含'age'键")
            assertTrue(map.containsKey("active"), "结果应该包含'active'键")
        }
    }
}