package io.github.spcookie

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

/**
 * Bean Mock Benchmark Test Suite
 * Performance testing and benchmarking for Bean Mock functionality
 */
class BeanMockBenchmarkTest {

    private val logger = LoggerFactory.getLogger(BeanMockBenchmarkTest::class.java)
    private var startTime: Long = 0
    private var initialMemory: Long = 0

    @BeforeEach
    fun setUp() {
        startTime = System.currentTimeMillis()
        System.gc() // Force garbage collection before test
        Thread.sleep(100)
        initialMemory = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
    }

    @AfterEach
    fun tearDown() {
        val endTime = System.currentTimeMillis()
        System.gc() // Force garbage collection after test
        Thread.sleep(100)
        val finalMemory = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        
        logger.info("Test completed in ${endTime - startTime}ms")
        logger.info("Memory usage: ${(finalMemory - initialMemory) / 1024}KB")
    }

    // ==================== Benchmark Data Classes ====================

    data class SimpleUser(
        var id: Long = 0L,
        var name: String = "",
        var email: String = "",
        var age: Int = 0
    )

    data class MediumComplexityUser(
        var id: Long = 0L,
        var profile: UserProfile = UserProfile(),
        var addresses: List<Address> = emptyList(),
        var phoneNumbers: Map<String, String> = emptyMap(),
        var preferences: Set<String> = emptySet()
    )

    data class UserProfile(
        var firstName: String = "",
        var lastName: String = "",
        var birthDate: String = "",
        var avatar: String? = null
    )

    data class Address(
        var street: String = "",
        var city: String = "",
        var state: String = "",
        var zipCode: String = "",
        var country: String = ""
    )

    data class HighComplexityUser(
        var id: Long = 0L,
        var personalInfo: PersonalInfo = PersonalInfo(),
        var contactInfo: ContactInfo = ContactInfo(),
        var workInfo: WorkInfo = WorkInfo(),
        var socialInfo: SocialInfo = SocialInfo(),
        var preferences: UserPreferences = UserPreferences(),
        var metadata: Map<String, Any> = emptyMap()
    )

    data class PersonalInfo(
        var firstName: String = "",
        var lastName: String = "",
        var middleName: String? = null,
        var birthDate: String = "",
        var gender: String = "",
        var nationality: String = ""
    )

    data class ContactInfo(
        var emails: List<String> = emptyList(),
        var phones: Map<String, String> = emptyMap(),
        var addresses: List<Address> = emptyList(),
        var socialMedia: Map<String, String> = emptyMap()
    )

    data class WorkInfo(
        var company: String = "",
        var position: String = "",
        var department: String = "",
        var startDate: String = "",
        var salary: Double = 0.0,
        var skills: Set<String> = emptySet()
    )

    data class SocialInfo(
        var friends: List<String> = emptyList(),
        var groups: Set<String> = emptySet(),
        var activities: Map<String, String> = emptyMap(),
        var interests: List<String> = emptyList()
    )

    data class UserPreferences(
        var theme: String = "",
        var language: String = "",
        var timezone: String = "",
        var notifications: Map<String, Boolean> = emptyMap(),
        var privacy: Map<String, String> = emptyMap()
    )

    // ==================== Single Instance Performance Tests ====================

    @Test
    fun benchmarkSimpleUserGeneration() {
        val iterations = 1000
        val times = mutableListOf<Long>()
        
        // Warm up
        repeat(10) { mock(SimpleUser::class) }
        
        // Benchmark
        repeat(iterations) {
            val time = measureTimeMillis {
                val user = mock(SimpleUser::class)
                assertNotNull(user)
            }
            times.add(time)
        }
        
        val avgTime = times.average()
        val minTime = times.minOrNull() ?: 0
        val maxTime = times.maxOrNull() ?: 0
        val medianTime = times.sorted()[times.size / 2]
        
        logger.info("Simple User Generation Benchmark ($iterations iterations):")
        logger.info("  Average: ${String.format("%.2f", avgTime)}ms")
        logger.info("  Median: ${medianTime}ms")
        logger.info("  Min: ${minTime}ms")
        logger.info("  Max: ${maxTime}ms")
        
        // Performance assertions
        assertTrue(avgTime < 10.0, "Average time should be less than 10ms, actual: ${avgTime}ms")
        assertTrue(maxTime < 100, "Max time should be less than 100ms, actual: ${maxTime}ms")
    }

    @Test
    fun benchmarkMediumComplexityUserGeneration() {
        val iterations = 1000
        val times = mutableListOf<Long>()
        
        // Warm up
        repeat(10) { mock(MediumComplexityUser::class) }
        
        // Benchmark
        repeat(iterations) {
            val time = measureTimeMillis {
                val user = mock(MediumComplexityUser::class)
                assertNotNull(user)
                assertNotNull(user.profile)
                assertTrue(user.addresses.isNotEmpty())
            }
            times.add(time)
        }
        
        val avgTime = times.average()
        val minTime = times.minOrNull() ?: 0
        val maxTime = times.maxOrNull() ?: 0
        
        logger.info("Medium Complexity User Generation Benchmark ($iterations iterations):")
        logger.info("  Average: ${String.format("%.2f", avgTime)}ms")
        logger.info("  Min: ${minTime}ms")
        logger.info("  Max: ${maxTime}ms")
        
        // Performance assertions
        assertTrue(avgTime < 50.0, "Average time should be less than 50ms, actual: ${avgTime}ms")
    }

    @Test
    fun benchmarkHighComplexityUserGeneration() {
        val iterations = 100
        val times = mutableListOf<Long>()
        
        // Warm up
        repeat(5) { mock(HighComplexityUser::class) }
        
        // Benchmark
        repeat(iterations) {
            val time = measureTimeMillis {
                val user = mock(HighComplexityUser::class)
                assertNotNull(user)
                assertNotNull(user.personalInfo)
                assertNotNull(user.contactInfo)
                assertNotNull(user.workInfo)
                assertNotNull(user.socialInfo)
                assertNotNull(user.preferences)
                assertTrue(user.contactInfo.addresses.isNotEmpty())
            }
            times.add(time)
        }
        
        val avgTime = times.average()
        val minTime = times.minOrNull() ?: 0
        val maxTime = times.maxOrNull() ?: 0
        
        logger.info("High Complexity User Generation Benchmark ($iterations iterations):")
        logger.info("  Average: ${String.format("%.2f", avgTime)}ms")
        logger.info("  Min: ${minTime}ms")
        logger.info("  Max: ${maxTime}ms")
        
        // Performance assertions
        assertTrue(avgTime < 200.0, "Average time should be less than 200ms, actual: ${avgTime}ms")
    }

    // ==================== Batch Generation Performance Tests ====================

    @Test
    fun benchmarkBatchSimpleUserGeneration() {
        val batchSizes = listOf(10, 50, 100, 500)
        
        batchSizes.forEach { batchSize ->
            val time = measureTimeMillis {
                val users = (1..batchSize).map { mock(SimpleUser::class) }
                assertEquals(batchSize, users.size)
                users.forEach { assertNotNull(it) }
            }
            
            val timePerUser = time.toDouble() / batchSize
            
            logger.info("Batch Simple User Generation (${batchSize} users):")
            logger.info("  Total time: ${time}ms")
            logger.info("  Time per user: ${String.format("%.2f", timePerUser)}ms")
            
            // Performance assertions
            assertTrue(timePerUser < 10.0, 
                "Time per user should be less than 10ms for batch size $batchSize, actual: ${timePerUser}ms")
        }
    }

    @Test
    fun benchmarkBatchComplexUserGeneration() {
        val batchSizes = listOf(5, 10, 25, 50)
        
        batchSizes.forEach { batchSize ->
            val time = measureTimeMillis {
                val users = (1..batchSize).map { mock(HighComplexityUser::class) }
                assertEquals(batchSize, users.size)
                users.forEach { user ->
                    assertNotNull(user)
                    assertNotNull(user.personalInfo)
                    assertNotNull(user.contactInfo)
                }
            }
            
            val timePerUser = time.toDouble() / batchSize
            
            logger.info("Batch Complex User Generation (${batchSize} users):")
            logger.info("  Total time: ${time}ms")
            logger.info("  Time per user: ${String.format("%.2f", timePerUser)}ms")
            
            // Performance assertions
            assertTrue(timePerUser < 500.0, 
                "Time per user should be less than 500ms for batch size $batchSize, actual: ${timePerUser}ms")
        }
    }

    // ==================== Memory Usage Tests ====================

    @Test
    fun benchmarkMemoryUsageSimpleUser() {
        val batchSize = 1000
        
        val beforeMemory = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        
        val users = (1..batchSize).map { mock(SimpleUser::class) }
        
        val afterMemory = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        val memoryUsed = afterMemory - beforeMemory
        val memoryPerUser = memoryUsed.toDouble() / batchSize
        
        logger.info("Memory Usage for Simple Users (${batchSize} users):")
        logger.info("  Total memory: ${memoryUsed / 1024}KB")
        logger.info("  Memory per user: ${String.format("%.2f", memoryPerUser)}bytes")
        
        // Verify all users are valid
        users.forEach { assertNotNull(it) }
        
        // Memory assertions
        assertTrue(
            memoryPerUser < 30000,
            "Memory per user should be less than 30000 bytes, actual: ${memoryPerUser}bytes"
        )
    }

    @Test
    fun benchmarkMemoryUsageComplexUser() {
        val batchSize = 100
        
        val beforeMemory = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        
        val users = (1..batchSize).map { mock(HighComplexityUser::class) }
        
        val afterMemory = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        val memoryUsed = afterMemory - beforeMemory
        val memoryPerUser = memoryUsed.toDouble() / batchSize
        
        logger.info("Memory Usage for Complex Users (${batchSize} users):")
        logger.info("  Total memory: ${memoryUsed / 1024}KB")
        logger.info("  Memory per user: ${String.format("%.2f", memoryPerUser)}bytes")
        
        // Verify all users are valid
        users.forEach { user ->
            assertNotNull(user)
            assertNotNull(user.personalInfo)
            assertNotNull(user.contactInfo)
        }
        
        // Memory assertions
        assertTrue(
            memoryPerUser < 200000,
            "Memory per user should be less than 200KB, actual: ${memoryPerUser}bytes"
        )
    }

    // ==================== Concurrent Performance Tests ====================

    @Test
    fun benchmarkConcurrentGeneration() {
        val threadCount = 10
        val iterationsPerThread = 100
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = ConcurrentHashMap<Int, List<Long>>()
        
        val startTime = System.currentTimeMillis()
        
        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    val times = mutableListOf<Long>()
                    
                    repeat(iterationsPerThread) {
                        val time = measureTimeMillis {
                            val user = mock(MediumComplexityUser::class)
                            assertNotNull(user)
                        }
                        times.add(time)
                    }
                    
                    results[threadIndex] = times
                } finally {
                    latch.countDown()
                }
            }
        }
        
        assertTrue(latch.await(60, TimeUnit.SECONDS), "Concurrent test should complete within 60 seconds")
        
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        
        executor.shutdown()
        
        // Analyze results
        val allTimes = results.values.flatten()
        val avgTime = allTimes.average()
        val totalOperations = threadCount * iterationsPerThread
        val operationsPerSecond = totalOperations * 1000.0 / totalTime
        
        logger.info("Concurrent Generation Benchmark:")
        logger.info("  Threads: $threadCount")
        logger.info("  Operations per thread: $iterationsPerThread")
        logger.info("  Total operations: $totalOperations")
        logger.info("  Total time: ${totalTime}ms")
        logger.info("  Average time per operation: ${String.format("%.2f", avgTime)}ms")
        logger.info("  Operations per second: ${String.format("%.2f", operationsPerSecond)}")
        
        // Performance assertions
        assertTrue(operationsPerSecond > 10, 
            "Should achieve at least 10 operations per second, actual: ${operationsPerSecond}")
        assertTrue(avgTime < 100.0, 
            "Average time should be less than 100ms, actual: ${avgTime}ms")
    }

    // ==================== Scalability Tests ====================

    @Test
    fun benchmarkScalability() {
        val scales = listOf(1, 10, 50, 100, 500)
        val results = mutableMapOf<Int, Double>()
        
        scales.forEach { scale ->
            val time = measureTimeMillis {
                val users = (1..scale).map { mock(SimpleUser::class) }
                users.forEach { assertNotNull(it) }
            }
            
            val timePerUser = time.toDouble() / scale
            results[scale] = timePerUser
            
            logger.info("Scalability test for $scale users: ${time}ms total, ${String.format("%.2f", timePerUser)}ms per user")
        }
        
        // Check that performance doesn't degrade significantly with scale
        val baselineTime = results[1] ?: 0.0
        val largeScaleTime = results[500] ?: 0.0
        
        // Performance should not degrade more than 20x
        assertTrue(
            largeScaleTime < baselineTime * 20,
            "Performance degradation should be less than 10x. Baseline: ${baselineTime}ms, Large scale: ${largeScaleTime}ms")
    }

    // ==================== Stress Tests ====================

    @Test
    fun stressTestContinuousGeneration() {
        val duration = 10000 // 10 seconds
        val startTime = System.currentTimeMillis()
        var count = 0
        val errors = mutableListOf<Exception>()
        
        while (System.currentTimeMillis() - startTime < duration) {
            try {
                val user = mock(SimpleUser::class)
                assertNotNull(user)
                count++
            } catch (e: Exception) {
                errors.add(e)
            }
        }
        
        val actualDuration = System.currentTimeMillis() - startTime
        val rate = count * 1000.0 / actualDuration
        
        logger.info("Stress Test Results:")
        logger.info("  Duration: ${actualDuration}ms")
        logger.info("  Objects generated: $count")
        logger.info("  Generation rate: ${String.format("%.2f", rate)} objects/second")
        logger.info("  Errors: ${errors.size}")
        
        // Stress test assertions
        assertTrue(count > 100, "Should generate at least 100 objects in stress test")
        assertTrue(errors.isEmpty(), "Should not have errors during stress test")
        assertTrue(rate > 10, "Should maintain at least 10 objects/second rate")
    }

    // ==================== Comparison Tests ====================

    @Test
    fun compareComplexityLevels() {
        val iterations = 100
        
        // Simple user benchmark
        val simpleTime = measureTimeMillis {
            repeat(iterations) {
                val user = mock(SimpleUser::class)
                assertNotNull(user)
            }
        }
        
        // Medium complexity user benchmark
        val mediumTime = measureTimeMillis {
            repeat(iterations) {
                val user = mock(MediumComplexityUser::class)
                assertNotNull(user)
            }
        }
        
        // High complexity user benchmark
        val complexTime = measureTimeMillis {
            repeat(iterations) {
                val user = mock(HighComplexityUser::class)
                assertNotNull(user)
            }
        }
        
        val simpleAvg = simpleTime.toDouble() / iterations
        val mediumAvg = mediumTime.toDouble() / iterations
        val complexAvg = complexTime.toDouble() / iterations
        
        logger.info("Complexity Level Comparison ($iterations iterations each):")
        logger.info("  Simple User: ${String.format("%.2f", simpleAvg)}ms avg")
        logger.info("  Medium User: ${String.format("%.2f", mediumAvg)}ms avg")
        logger.info("  Complex User: ${String.format("%.2f", complexAvg)}ms avg")
        logger.info("  Medium/Simple ratio: ${String.format("%.2f", mediumAvg / simpleAvg)}x")
        logger.info("  Complex/Simple ratio: ${String.format("%.2f", complexAvg / simpleAvg)}x")
        
        // Complexity should increase time, but not excessively
        assertTrue(mediumAvg > simpleAvg, "Medium complexity should take longer than simple")
        assertTrue(complexAvg > mediumAvg, "High complexity should take longer than medium")
        assertTrue(complexAvg < simpleAvg * 100, "Complex should not be more than 100x slower than simple")
    }
}