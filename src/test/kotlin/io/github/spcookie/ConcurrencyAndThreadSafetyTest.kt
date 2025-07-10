package io.github.spcookie

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.assertNotNull

/**
 * 并发和线程安全测试 - 测试BeanMock在多线程环境下的安全性和性能
 */
class ConcurrencyAndThreadSafetyTest {

    private val logger = LoggerFactory.getLogger(ConcurrencyAndThreadSafetyTest::class.java)

    // ==================== 基本线程安全测试 ====================

    data class ThreadSafeBean(
        val id: Long,
        val name: String,
        val value: Int,
        val timestamp: String
    )

    @Test
    fun testBasicThreadSafety() {
        logger.info("测试基本线程安全...")

        val template = """
        {
            "id": "@long",
            "name": "@name",
            "value": "@integer(1,1000)",
            "timestamp": "@datetime"
        }
        """.trimIndent()

        val threadCount = 10
        val iterationsPerThread = 100
        val executor = Executors.newFixedThreadPool(threadCount)
        val results = ConcurrentLinkedQueue<ThreadSafeBean>()
        val exceptions = ConcurrentLinkedQueue<Exception>()
        val latch = CountDownLatch(threadCount)
        val gson = Gson()

        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    repeat(iterationsPerThread) {
                        val bean = gson.fromJson(
                            gson.toJson(
                                mock(
                                    gson.fromJson(
                                        template,
                                        Map::class.java
                                    ) as Map<String, Any>
                                )
                            ), ThreadSafeBean::class.java
                        )
                        results.add(bean)
                    }
                } catch (e: Exception) {
                    exceptions.add(e)
                    logger.error("线程 $threadIndex 发生异常", e)
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "所有线程应在30秒内完成")
        executor.shutdown()

        // 验证结果
        assertTrue(exceptions.isEmpty(), "不应有异常发生，实际异常数: ${exceptions.size}")
        assertEquals(threadCount * iterationsPerThread, results.size, "结果数量应正确")

        // 验证每个结果都是有效的
        results.forEach { bean ->
            assertNotNull(bean, "Bean不应为null")
            assertTrue(bean.id != 0L, "ID不应为0")
            assertTrue(bean.name.isNotEmpty(), "名称不应为空")
            assertTrue(bean.value >= 1 && bean.value <= 1000, "值应在范围内")
            assertTrue(bean.timestamp.isNotEmpty(), "时间戳不应为空")
        }

        logger.info("基本线程安全测试完成，生成了 ${results.size} 个Bean")
    }

    // ==================== 高并发测试 ====================

    @Test
    fun testHighConcurrency() {
        logger.info("测试高并发...")

        val template = """
        {
            "id": "@long",
            "name": "@string(10)",
            "active": "@boolean"
        }
        """.trimIndent()

        val threadCount = 50
        val iterationsPerThread = 50
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)
        val latch = CountDownLatch(threadCount)

        val startTime = System.currentTimeMillis()

        val gson = Gson()

        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    repeat(iterationsPerThread) {
                        val bean = mock(gson.toJson(template)) as Map<String, Any>
                        assertNotNull(bean)
                        successCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                    logger.error("高并发测试线程 $threadIndex 异常", e)
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS), "高并发测试应在60秒内完成")
        executor.shutdown()

        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        val totalOperations = threadCount * iterationsPerThread
        val operationsPerSecond = (totalOperations * 1000.0) / totalTime

        logger.info("高并发测试结果:")
        logger.info("  总操作数: $totalOperations")
        logger.info("  成功数: ${successCount.get()}")
        logger.info("  错误数: ${errorCount.get()}")
        logger.info("  总时间: ${totalTime}ms")
        logger.info("  每秒操作数: ${String.format("%.2f", operationsPerSecond)}")

        assertEquals(totalOperations, successCount.get(), "所有操作都应成功")
        assertEquals(0, errorCount.get(), "不应有错误")
        assertTrue(operationsPerSecond > 100, "每秒操作数应大于100，实际为 ${operationsPerSecond}")
    }

    // ==================== 共享资源测试 ====================

    @Test
    fun testSharedResourceAccess() {
        logger.info("测试共享资源访问...")

        // 使用直接的mock函数调用，无需共享实例
        val template = """
        {
            "id": "{{int(1,10000)}}",
            "data": "{{string(20)}}"
        }
        """.trimIndent()

        val threadCount = 20
        val iterationsPerThread = 25
        val executor = Executors.newFixedThreadPool(threadCount)
        val results = ConcurrentHashMap<Int, MutableList<Map<String, Any>>>()
        val latch = CountDownLatch(threadCount)

        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    val threadResults = mutableListOf<Map<String, Any>>()
                    repeat(iterationsPerThread) {
                        val bean = mock(template) as Map<String, Any>
                        threadResults.add(bean)
                    }
                    results[threadIndex] = threadResults
                } catch (e: Exception) {
                    logger.error("共享资源测试线程 $threadIndex 异常", e)
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "共享资源测试应在30秒内完成")
        executor.shutdown()

        // 验证结果
        assertEquals(threadCount, results.size, "所有线程都应有结果")

        var totalResults = 0
        results.values.forEach { threadResults ->
            assertEquals(iterationsPerThread, threadResults.size, "每个线程应有正确数量的结果")
            totalResults += threadResults.size

            threadResults.forEach { bean ->
                assertNotNull(bean["id"], "ID不应为null")
                assertNotNull(bean["data"], "data不应为null")
            }
        }

        assertEquals(threadCount * iterationsPerThread, totalResults, "总结果数应正确")
        logger.info("共享资源测试完成，总共生成 $totalResults 个结果")
    }

    // ==================== 不同模板并发测试 ====================

    data class UserBean(
        val id: Long,
        val name: String,
        val email: String
    )

    data class ProductBean(
        val id: Long,
        val name: String,
        val price: Double
    )

    data class OrderBean(
        val id: Long,
        val userId: Long,
        val productId: Long,
        val quantity: Int
    )

    @Test
    fun testDifferentTemplatesConcurrency() {
        logger.info("测试不同模板并发...")

        val userTemplate = """
        {
            "id": "{{long(1,1000)}}",
            "name": "{{name}}",
            "email": "{{email}}"
        }
        """.trimIndent()

        val productTemplate = """
        {
            "id": "{{long(1001,2000)}}",
            "name": "{{string(15)}}",
            "price": "{{double(1.0,1000.0)}}"
        }
        """.trimIndent()

        val orderTemplate = """
        {
            "id": "{{long(2001,3000)}}",
            "userId": "{{long(1,1000)}}",
            "productId": "{{long(1001,2000)}}",
            "quantity": "{{int(1,10)}}"
        }
        """.trimIndent()

        val executor = Executors.newFixedThreadPool(15)
        val users = ConcurrentLinkedQueue<UserBean>()
        val products = ConcurrentLinkedQueue<ProductBean>()
        val orders = ConcurrentLinkedQueue<OrderBean>()
        val latch = CountDownLatch(15)

        // 用户生成线程
        repeat(5) {
            executor.submit {
                try {
                    repeat(20) {
                        val user = mock(userTemplate) as UserBean
                        users.add(user)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        // 产品生成线程
        repeat(5) {
            executor.submit {
                try {
                    repeat(20) {
                        val product = mock(productTemplate) as ProductBean
                        products.add(product)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        // 订单生成线程
        repeat(5) {
            executor.submit {
                try {
                    repeat(20) {
                        val order = mock(orderTemplate) as OrderBean
                        orders.add(order)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "不同模板并发测试应在30秒内完成")
        executor.shutdown()

        // 验证结果
        assertEquals(100, users.size, "应生成100个用户")
        assertEquals(100, products.size, "应生成100个产品")
        assertEquals(100, orders.size, "应生成100个订单")

        // 验证用户数据
        users.forEach { user ->
            assertTrue(user.id >= 1 && user.id <= 1000, "用户ID应在范围内")
            assertTrue(user.name.isNotEmpty(), "用户名不应为空")
            assertTrue(user.email.isNotEmpty(), "邮箱不应为空")
        }

        // 验证产品数据
        products.forEach { product ->
            assertTrue(product.id >= 1001 && product.id <= 2000, "产品ID应在范围内")
            assertTrue(product.name.isNotEmpty(), "产品名不应为空")
            assertTrue(product.price >= 1.0 && product.price <= 1000.0, "价格应在范围内")
        }

        // 验证订单数据
        orders.forEach { order ->
            assertTrue(order.id >= 2001 && order.id <= 3000, "订单ID应在范围内")
            assertTrue(order.userId >= 1 && order.userId <= 1000, "用户ID应在范围内")
            assertTrue(order.productId >= 1001 && order.productId <= 2000, "产品ID应在范围内")
            assertTrue(order.quantity >= 1 && order.quantity <= 10, "数量应在范围内")
        }

        logger.info("不同模板并发测试完成")
    }

    // ==================== 长时间运行测试 ====================

    @Test
    fun testLongRunningConcurrency() {
        logger.info("测试长时间运行并发...")

        val template = """
        {
            "id": "{{long}}",
            "data": "{{string(50)}}",
            "numbers": ["{{int}}", "{{int}}", "{{int}}"],
            "nested": {
                "value": "{{double}}",
                "flag": "{{boolean}}"
            }
        }
        """.trimIndent()

        val threadCount = 8
        val runTimeSeconds = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val totalOperations = AtomicLong(0)
        val errors = AtomicInteger(0)
        val running = AtomicInteger(threadCount)

        val startTime = System.currentTimeMillis()

        repeat(threadCount) { threadIndex ->
            executor.submit {
                val threadStartTime = System.currentTimeMillis()
                var threadOperations = 0

                try {
                    while (System.currentTimeMillis() - threadStartTime < runTimeSeconds * 1000) {
                        val bean = mock(template) as Map<String, Any>
                        assertNotNull(bean)
                        threadOperations++
                        totalOperations.incrementAndGet()

                        // 偶尔休眠一下，模拟真实使用场景
                        if (threadOperations % 100 == 0) {
                            Thread.sleep(1)
                        }
                    }
                } catch (e: Exception) {
                    errors.incrementAndGet()
                    logger.error("长时间运行测试线程 $threadIndex 异常", e)
                } finally {
                    running.decrementAndGet()
                    logger.info("线程 $threadIndex 完成，执行了 $threadOperations 次操作")
                }
            }
        }

        // 等待所有线程完成
        while (running.get() > 0) {
            Thread.sleep(100)
        }

        executor.shutdown()

        val endTime = System.currentTimeMillis()
        val actualRunTime = (endTime - startTime) / 1000.0
        val operationsPerSecond = totalOperations.get() / actualRunTime

        logger.info("长时间运行测试结果:")
        logger.info("  实际运行时间: ${String.format("%.2f", actualRunTime)}秒")
        logger.info("  总操作数: ${totalOperations.get()}")
        logger.info("  错误数: ${errors.get()}")
        logger.info("  每秒操作数: ${String.format("%.2f", operationsPerSecond)}")

        assertEquals(0, errors.get(), "不应有错误")
        assertTrue(totalOperations.get() > 0, "应有操作执行")
        assertTrue(operationsPerSecond > 50, "每秒操作数应大于50")
    }

    // ==================== 内存压力测试 ====================

    @Test
    fun testMemoryPressureConcurrency() {
        logger.info("测试内存压力并发...")

        val template = """
        {
            "largeString": "{{string(1000)}}",
            "largeArray": ["{{string(100)}}", "{{string(100)}}", "{{string(100)}}", "{{string(100)}}", "{{string(100)}}"],
            "nestedData": {
                "level1": {
                    "level2": {
                        "data": "{{string(200)}}"
                    }
                }
            }
        }
        """.trimIndent()

        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        val threadCount = 10
        val iterationsPerThread = 50
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = ConcurrentLinkedQueue<Map<String, Any>>()

        repeat(threadCount) {
            executor.submit {
                try {
                    repeat(iterationsPerThread) {
                        val bean = mock(template) as Map<String, Any>
                        results.add(bean)

                        // 偶尔触发GC
                        if (results.size % 100 == 0) {
                            System.gc()
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS), "内存压力测试应在60秒内完成")
        executor.shutdown()

        System.gc()
        Thread.sleep(1000) // 等待GC完成

        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = finalMemory - initialMemory

        logger.info("内存压力测试结果:")
        logger.info("  生成对象数: ${results.size}")
        logger.info("  内存使用: ${memoryUsed / 1024 / 1024}MB")

        assertEquals(threadCount * iterationsPerThread, results.size, "应生成正确数量的对象")

        // 验证生成的对象
        results.forEach { bean ->
            assertNotNull(bean["largeString"], "largeString不应为null")
            assertNotNull(bean["largeArray"], "largeArray不应为null")
            assertNotNull(bean["nestedData"], "nestedData不应为null")
        }

        // 内存使用应该在合理范围内
        assertTrue(memoryUsed < 100 * 1024 * 1024, "内存使用应少于100MB")
    }

    // ==================== 异常处理并发测试 ====================

    @Test
    fun testExceptionHandlingConcurrency() {
        logger.info("测试异常处理并发...")

        val validTemplate = """
        {
            "id": "@long",
            "name": "@string(10)"
        }
        """.trimIndent()

        val invalidTemplate = "{invalid json}"

        val threadCount = 20
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)
        val exceptionCount = AtomicInteger(0)
        val latch = CountDownLatch(threadCount)

        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    repeat(10) { iteration ->
                        try {
                            // 交替使用有效和无效模板
                            val template = if (iteration % 2 == 0) validTemplate else invalidTemplate
                            val bean = mock(Gson().fromJson(template, Map::class.java) as Map<String, Any>)

                            if (iteration % 2 == 0) {
                                // 有效模板应该成功
                                assertNotNull(bean)
                                successCount.incrementAndGet()
                            }
                        } catch (e: Exception) {
                            if (iteration % 2 != 0) {
                                // 无效模板应该抛出异常
                                exceptionCount.incrementAndGet()
                            } else {
                                // 有效模板不应该抛出异常
                                logger.error("有效模板意外异常", e)
                                throw e
                            }
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "异常处理测试应在30秒内完成")
        executor.shutdown()

        logger.info("异常处理并发测试结果:")
        logger.info("  成功操作数: ${successCount.get()}")
        logger.info("  异常数: ${exceptionCount.get()}")

        assertEquals(threadCount * 5, successCount.get(), "应有正确数量的成功操作")
        assertEquals(threadCount * 5, exceptionCount.get(), "应有正确数量的异常")
    }
}