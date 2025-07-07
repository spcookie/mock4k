package io.github.spcookie

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.lang.management.ManagementFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.system.measureTimeMillis

/**
 * 测试工具类 - 提供通用的测试辅助方法和断言
 */
object TestUtils {

    /**
     * 性能测试辅助方法
     */
    fun <T> measurePerformance(
        description: String,
        warmupRuns: Int = 5,
        benchmarkRuns: Int = 100,
        maxExecutionTime: Long = 1000,
        operation: () -> T
    ): PerformanceResult<T> {
        // 预热
        repeat(warmupRuns) {
            operation()
        }

        // 基准测试
        val results = mutableListOf<T>()
        val executionTimes = mutableListOf<Long>()

        repeat(benchmarkRuns) {
            val time = measureTimeMillis {
                results.add(operation())
            }
            executionTimes.add(time)
        }

        val avgTime = executionTimes.average()
        val minTime = executionTimes.minOrNull() ?: 0L
        val maxTime = executionTimes.maxOrNull() ?: 0L

        // 性能断言
        assertTrue(
            avgTime <= maxExecutionTime,
            "$description 平均执行时间 ${avgTime}ms 超过了最大允许时间 ${maxExecutionTime}ms"
        )

        return PerformanceResult(
            description = description,
            results = results,
            averageTime = avgTime,
            minTime = minTime,
            maxTime = maxTime,
            totalRuns = benchmarkRuns
        )
    }

    /**
     * 内存使用测试
     */
    fun measureMemoryUsage(description: String, operation: () -> Unit): MemoryResult {
        val runtime = Runtime.getRuntime()

        // 强制垃圾回收
        System.gc()
        Thread.sleep(100)

        val beforeMemory = runtime.totalMemory() - runtime.freeMemory()

        operation()

        val afterMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = afterMemory - beforeMemory

        return MemoryResult(
            description = description,
            beforeMemory = beforeMemory,
            afterMemory = afterMemory,
            memoryUsed = memoryUsed
        )
    }

    /**
     * 并发测试辅助方法
     */
    fun <T> testConcurrency(
        description: String,
        threadCount: Int = 10,
        executionsPerThread: Int = 100,
        timeoutSeconds: Long = 30,
        operation: () -> T
    ): ConcurrencyResult<T> {
        val results = mutableListOf<T>()
        val exceptions = mutableListOf<Exception>()
        val latch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)

        val futures = (1..threadCount).map { threadIndex ->
            CompletableFuture.supplyAsync {
                try {
                    startLatch.await() // 等待所有线程准备就绪
                    val threadResults = mutableListOf<T>()
                    repeat(executionsPerThread) {
                        threadResults.add(operation())
                    }
                    synchronized(results) {
                        results.addAll(threadResults)
                    }
                    threadResults
                } catch (e: Exception) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                    emptyList<T>()
                } finally {
                    latch.countDown()
                }
            }
        }

        val startTime = System.currentTimeMillis()
        startLatch.countDown() // 开始执行

        val completed = latch.await(timeoutSeconds, TimeUnit.SECONDS)
        val endTime = System.currentTimeMillis()

        assertTrue(completed, "$description 并发测试在 ${timeoutSeconds}s 内未完成")
        assertTrue(
            exceptions.isEmpty(),
            "$description 并发测试中发生异常: ${exceptions.map { it.message }}"
        )

        return ConcurrencyResult(
            description = description,
            threadCount = threadCount,
            executionsPerThread = executionsPerThread,
            totalResults = results.size,
            executionTime = endTime - startTime,
            exceptions = exceptions
        )
    }

    /**
     * 深度对象验证
     */
    fun validateObjectStructure(obj: Any?, expectedType: KClass<*>): Boolean {
        if (obj == null) return false
        if (!expectedType.isInstance(obj)) return false

        return try {
            expectedType.memberProperties.all { property ->
                val value = property.getter.call(obj)
                value != null || property.returnType.isMarkedNullable
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 集合验证
     */
    fun validateCollection(
        collection: Collection<*>?,
        expectedSize: IntRange? = null,
        elementValidator: ((Any?) -> Boolean)? = null
    ): Boolean {
        if (collection == null) return false

        if (expectedSize != null && collection.size !in expectedSize) {
            return false
        }

        if (elementValidator != null) {
            return collection.all { elementValidator(it) }
        }

        return true
    }

    /**
     * 数值范围验证
     */
    fun validateNumberRange(value: Number?, min: Number?, max: Number?): Boolean {
        if (value == null) return false

        val doubleValue = value.toDouble()

        if (min != null && doubleValue < min.toDouble()) return false
        if (max != null && doubleValue > max.toDouble()) return false

        return true
    }

    /**
     * 字符串验证
     */
    fun validateString(
        value: String?,
        minLength: Int? = null,
        maxLength: Int? = null,
        pattern: Regex? = null,
        notEmpty: Boolean = false
    ): Boolean {
        if (value == null) return false
        if (notEmpty && value.isEmpty()) return false

        if (minLength != null && value.length < minLength) return false
        if (maxLength != null && value.length > maxLength) return false
        if (pattern != null && !pattern.matches(value)) return false

        return true
    }

    /**
     * 时间验证
     */
    fun validateTimestamp(timestamp: Long?, minTime: Long? = null, maxTime: Long? = null): Boolean {
        if (timestamp == null) return false

        if (minTime != null && timestamp < minTime) return false
        if (maxTime != null && timestamp > maxTime) return false

        return true
    }

    /**
     * 创建测试数据类
     */
    inline fun <reified T> createTestData(count: Int = 1, factory: () -> T): List<T> {
        return (1..count).map { factory() }
    }

    /**
     * 断言性能
     */
    fun assertPerformance(
        description: String,
        maxTime: Long,
        operation: () -> Unit
    ) {
        val executionTime = measureTimeMillis(operation)
        assertTrue(
            executionTime <= maxTime,
            "$description 执行时间 ${executionTime}ms 超过了最大允许时间 ${maxTime}ms"
        )
    }

    /**
     * 断言内存使用
     */
    fun assertMemoryUsage(
        description: String,
        maxMemoryMB: Long,
        operation: () -> Unit
    ) {
        val memoryResult = measureMemoryUsage(description, operation)
        val memoryUsedMB = memoryResult.memoryUsed / (1024 * 1024)
        assertTrue(
            memoryUsedMB <= maxMemoryMB,
            "$description 内存使用 ${memoryUsedMB}MB 超过了最大允许内存 ${maxMemoryMB}MB"
        )
    }

    /**
     * 断言线程安全
     */
    fun assertThreadSafety(
        description: String,
        threadCount: Int = 10,
        executionsPerThread: Int = 100,
        operation: () -> Any
    ) {
        val result = testConcurrency(description, threadCount, executionsPerThread) { operation() }
        assertTrue(
            result.exceptions.isEmpty(),
            "$description 线程安全测试失败: ${result.exceptions.map { it.message }}"
        )
        assertEquals(
            threadCount * executionsPerThread, result.totalResults,
            "$description 并发执行结果数量不匹配"
        )
    }

    /**
     * 获取系统信息
     */
    fun getSystemInfo(): SystemInfo {
        val runtime = Runtime.getRuntime()
        val memoryBean = ManagementFactory.getMemoryMXBean()

        return SystemInfo(
            availableProcessors = runtime.availableProcessors(),
            maxMemory = runtime.maxMemory(),
            totalMemory = runtime.totalMemory(),
            freeMemory = runtime.freeMemory(),
            heapMemoryUsage = memoryBean.heapMemoryUsage,
            nonHeapMemoryUsage = memoryBean.nonHeapMemoryUsage
        )
    }

    /**
     * 性能结果数据类
     */
    data class PerformanceResult<T>(
        val description: String,
        val results: List<T>,
        val averageTime: Double,
        val minTime: Long,
        val maxTime: Long,
        val totalRuns: Int
    )

    /**
     * 内存结果数据类
     */
    data class MemoryResult(
        val description: String,
        val beforeMemory: Long,
        val afterMemory: Long,
        val memoryUsed: Long
    )

    /**
     * 并发结果数据类
     */
    data class ConcurrencyResult<T>(
        val description: String,
        val threadCount: Int,
        val executionsPerThread: Int,
        val totalResults: Int,
        val executionTime: Long,
        val exceptions: List<Exception>
    )

    /**
     * 系统信息数据类
     */
    data class SystemInfo(
        val availableProcessors: Int,
        val maxMemory: Long,
        val totalMemory: Long,
        val freeMemory: Long,
        val heapMemoryUsage: java.lang.management.MemoryUsage,
        val nonHeapMemoryUsage: java.lang.management.MemoryUsage
    )

    /**
     * 测试数据类定义
     */
    data class TestUser(
        val id: Int,
        val name: String,
        val email: String,
        val age: Int,
        val active: Boolean = true
    )

    data class TestProduct(
        val id: String,
        val name: String,
        val price: Double,
        val category: String,
        val inStock: Boolean,
        val tags: List<String> = emptyList()
    )

    data class TestOrder(
        val id: String,
        val customerId: Int,
        val products: List<TestProduct>,
        val totalAmount: Double,
        val status: String,
        val createdAt: Long
    )

    data class TestAddress(
        val street: String,
        val city: String,
        val zipCode: String,
        val country: String
    )

    data class TestCompany(
        val id: String,
        val name: String,
        val address: TestAddress,
        val employees: List<TestUser>,
        val revenue: Double,
        val founded: Int
    )

    /**
     * 容器类型测试数据
     */
    data class TestOptionalData(
        val id: Int,
        val optionalName: String?,
        val optionalEmail: String?
    )

    data class TestFutureData(
        val id: String,
        val futureValue: String,
        val callableValue: Int,
        val supplierValue: Boolean
    )

    data class TestReactiveData(
        val id: String,
        val monoValue: String,
        val fluxValues: List<Int>,
        val singleValue: Float
    )
}