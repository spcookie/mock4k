package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

/**
 * 性能和压力测试 - 测试Mock4k在各种高负载场景下的性能表现
 */
class PerformanceAndStressTest {

    // ==================== 基础性能测试 ====================

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun testBasicPerformance() {
        val template = mapOf(
            "id" to "@GUID",
            "name" to "@NAME",
            "email" to "@EMAIL",
            "phone" to "@PHONENUMBER",
            "address" to "@STREETNAME, @CITY, @PROVINCE"
        )

        val iterations = 1000
        val executionTime = measureTimeMillis {
            repeat(iterations) {
                val result = mock(template)
                assertNotNull(result, "结果不应为null")
            }
        }

        val avgTimePerCall = executionTime.toDouble() / iterations
        println(
            "Basic performance: $iterations iterations in ${executionTime}ms (avg: ${
                String.format(
                    "%.2f",
                    avgTimePerCall
                )
            }ms per call)"
        )

        // 性能断言 - 平均每次调用应该在合理时间内完成
        assertTrue(avgTimePerCall < 10.0, "每次调用的平均时间应该少于10ms，实际为 ${avgTimePerCall}ms")
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun testComplexTemplatePerformance() {
        val template = mapOf(
            "users|50" to listOf(
                mapOf(
                    "id" to "@GUID",
                    "profile" to mapOf(
                        "firstName" to "@FIRST",
                        "lastName" to "@LAST",
                        "email" to "@EMAIL",
                        "phone" to "@PHONENUMBER(PT.M)",
                        "birthDate" to "@DATE",
                        "avatar" to "@IMAGE"
                    ),
                    "address" to mapOf(
                        "street" to "@STREETNAME",
                        "city" to "@CITY",
                        "province" to "@PROVINCE",
                        "zipCode" to "@NATURAL(10000, 99999)"
                    ),
                    "work" to mapOf(
                        "company" to "@COMPANY",
                        "profession" to "@PROFESSION",
                        "salary" to "@FLOAT(30000.0, 200000.0)",
                        "startDate" to "@DATE"
                    ),
                    "preferences" to mapOf(
                        "theme" to "@COLOR",
                        "language" to "@WORD",
                        "notifications" to "@BOOLEAN",
                        "newsletter" to "@BOOLEAN"
                    )
                )
            )
        )

        val iterations = 10
        val executionTime = measureTimeMillis {
            repeat(iterations) {
                val result = mock(template) as Map<String, Any>
                val users = result["users"] as List<*>
                assertEquals(50, users.size, "应该有50个用户")
            }
        }

        val avgTimePerCall = executionTime.toDouble() / iterations
        println(
            "Complex template performance: $iterations iterations in ${executionTime}ms (avg: ${
                String.format(
                    "%.2f",
                    avgTimePerCall
                )
            }ms per call)"
        )

        // 复杂模板的性能断言
        assertTrue(
            avgTimePerCall < 1000.0,
            "复杂调用的平均时间应该少于1000ms，实际为 ${avgTimePerCall}ms"
        )
    }

    // ==================== 大数据量测试 ====================

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    fun testLargeArrayGeneration() {
        val arraySizes = listOf(100, 500, 1000, 2000)

        arraySizes.forEach { size ->
            val template = mapOf(
                "items|$size" to listOf(
                    mapOf(
                        "id" to "@NATURAL",
                        "name" to "@STRING(20)",
                        "value" to "@FLOAT(1.0, 100.0)",
                        "active" to "@BOOLEAN"
                    )
                )
            )

            val executionTime = measureTimeMillis {
                val result = mock(template) as Map<String, Any>
                val items = result["items"] as List<*>
                assertEquals(size, items.size, "应该有 $size 个项目")

                // 验证前几个元素的结构
                val firstItem = items[0] as Map<String, Any>
                assertNotNull(firstItem["id"], "项目应该有id")
                assertNotNull(firstItem["name"], "项目应该有name")
                assertNotNull(firstItem["value"], "项目应该有value")
                assertNotNull(firstItem["active"], "项目应该有active")
            }

            val timePerItem = executionTime.toDouble() / size
            println("Large array ($size items): ${executionTime}ms (${String.format("%.3f", timePerItem)}ms per item)")

            // 性能断言 - 每个项目的生成时间应该合理
            assertTrue(timePerItem < 5.0, "大小为 $size 时每个项目的时间应该少于5ms，实际为 ${timePerItem}ms")
        }
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    fun testDeepNestingPerformance() {
        // 创建深度嵌套的模板
        fun createNestedTemplate(depth: Int): Any {
            return if (depth <= 0) {
                "@STRING"
            } else {
                mapOf(
                    "level$depth" to createNestedTemplate(depth - 1),
                    "data$depth" to "@NATURAL",
                    "info$depth" to "@WORD"
                )
            }
        }

        val depths = listOf(5, 10, 15, 20)

        depths.forEach { depth ->
            val template = mapOf(
                "nested" to createNestedTemplate(depth)
            )

            val executionTime = measureTimeMillis {
                val result = mock(template) as Map<String, Any>
                assertNotNull(result["nested"], "嵌套结构不应为null")

                // 验证嵌套结构的深度
                var current: Any = result["nested"]!!
                var actualDepth = 0
                while (current is Map<*, *> && current.containsKey("level${depth - actualDepth}")) {
                    actualDepth++
                    current = current["level${depth - actualDepth + 1}"]!!
                    if (actualDepth >= depth) break
                }
                assertTrue(actualDepth > 0, "应该有一定的嵌套深度")
            }

            println("Deep nesting (depth $depth): ${executionTime}ms")

            // 性能断言 - 深度嵌套不应该导致指数级性能下降
            assertTrue(
                executionTime < 5000,
                "深度嵌套（深度 $depth）应该在5秒内完成，实际用时 ${executionTime}ms"
            )
        }
    }

    // ==================== 并发性能测试 ====================

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    fun testConcurrentPerformance() {
        val template = mapOf(
            "data" to mapOf(
                "id" to "@GUID",
                "users|20" to listOf(
                    mapOf(
                        "name" to "@NAME",
                        "email" to "@EMAIL",
                        "phone" to "@PHONENUMBER"
                    )
                )
            )
        )

        val threadCounts = listOf(1, 2, 4, 8)
        val iterationsPerThread = 50

        threadCounts.forEach { threadCount ->
            val results = mutableListOf<Long>()
            val threads = mutableListOf<Thread>()

            val startTime = System.currentTimeMillis()

            repeat(threadCount) { threadIndex ->
                val thread = Thread {
                    val threadStartTime = System.currentTimeMillis()
                    repeat(iterationsPerThread) {
                        val result = mock(template)
                        assertNotNull(result, "结果不应为null")
                    }
                    val threadEndTime = System.currentTimeMillis()
                    synchronized(results) {
                        results.add(threadEndTime - threadStartTime)
                    }
                }
                threads.add(thread)
                thread.start()
            }

            threads.forEach { it.join() }
            val totalTime = System.currentTimeMillis() - startTime

            val avgThreadTime = results.average()
            val totalOperations = threadCount * iterationsPerThread
            val operationsPerSecond = (totalOperations * 1000.0) / totalTime

            println(
                "Concurrent performance ($threadCount threads): ${totalTime}ms total, ${
                    String.format(
                        "%.1f",
                        operationsPerSecond
                    )
                } ops/sec"
            )
            println("  Average thread time: ${String.format("%.1f", avgThreadTime)}ms")

            // 性能断言
            assertTrue(
                operationsPerSecond > 10.0,
                "使用 $threadCount 个线程应该至少达到每秒10次操作"
            )
        }
    }

    // ==================== 内存压力测试 ====================

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    fun testMemoryStress() {
        val template = mapOf(
            "largeData" to mapOf(
                "bigString" to "@STRING(1000)",
                "manyNumbers|100" to listOf("@FLOAT(1.0, 1000.0)"),
                "nestedObjects|50" to listOf(
                    mapOf(
                        "id" to "@GUID",
                        "content" to "@PARAGRAPH(5, 10)",
                        "metadata" to mapOf(
                            "created" to "@DATETIME",
                            "tags|10" to listOf("@WORD")
                        )
                    )
                )
            )
        )

        val iterations = 100
        val results = mutableListOf<Map<String, Any>>()

        val executionTime = measureTimeMillis {
            repeat(iterations) { iteration ->
                val result = mock(template) as Map<String, Any>
                results.add(result)

                // 每10次迭代进行一次验证
                if (iteration % 10 == 0) {
                    val largeData = result["largeData"] as Map<String, Any>
                    assertNotNull(largeData["bigString"], "大字符串不应为null")
                    assertNotNull(largeData["manyNumbers"], "多个数字不应为null")
                    assertNotNull(largeData["nestedObjects"], "嵌套对象不应为null")

                    val bigString = largeData["bigString"] as String
                    assertTrue(bigString.length >= 100, "大字符串应该有合理的长度")

                    val manyNumbers = largeData["manyNumbers"] as List<*>
                    assertEquals(100, manyNumbers.size, "应该有100个数字")

                    val nestedObjects = largeData["nestedObjects"] as List<*>
                    assertEquals(50, nestedObjects.size, "应该有50个嵌套对象")

                    println("Memory stress iteration $iteration completed")
                }
            }
        }

        assertEquals(iterations, results.size, "应该有 $iterations 个结果")

        val avgTimePerIteration = executionTime.toDouble() / iterations
        println(
            "Memory stress test: $iterations iterations in ${executionTime}ms (avg: ${
                String.format(
                    "%.2f",
                    avgTimePerIteration
                )
            }ms per iteration)"
        )

        // 性能断言
        assertTrue(avgTimePerIteration < 500.0, "内存压力测试每次迭代的平均时间应该少于500ms")
    }

    // ==================== 占位符性能测试 ====================

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    fun testPlaceholderPerformance() {
        val placeholderTypes = mapOf(
            "basic" to listOf("@STRING", "@NATURAL", "@BOOLEAN", "@FLOAT"),
            "text" to listOf("@WORD", "@SENTENCE", "@PARAGRAPH", "@TITLE"),
            "personal" to listOf("@NAME", "@FIRST", "@LAST", "@EMAIL"),
            "network" to listOf("@URL", "@DOMAIN", "@IP", "@TLD"),
            "location" to listOf("@CITY", "@PROVINCE", "@STREETNAME", "@AREACODE"),
            "business" to listOf("@COMPANY", "@PROFESSION", "@BANKCARD"),
            "phone" to listOf("@PHONENUMBER", "@PHONENUMBER(PT.M)", "@PHONENUMBER(PT.L)", "@PHONENUMBER(PT.TF)"),
            "datetime" to listOf("@DATE", "@TIME", "@DATETIME", "@NOW"),
            "identifier" to listOf("@GUID", "@ID"),
            "visual" to listOf("@COLOR", "@IMAGE", "@DATAIMAGE")
        )

        placeholderTypes.forEach { (category, placeholders) ->
            val template = placeholders.mapIndexed { index, placeholder ->
                "${category}_item$index" to placeholder
            }.toMap()

            val iterations = 200
            val executionTime = measureTimeMillis {
                repeat(iterations) {
                    val result = mock(template) as Map<String, Any>

                    // 验证所有占位符都被解析
                    result.forEach { (key, value) ->
                        assertNotNull(value, "$key 不应为null")
                        val valueStr = value.toString()
                        assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析: $valueStr")
                    }
                }
            }

            val avgTimePerIteration = executionTime.toDouble() / iterations
            val avgTimePerPlaceholder = avgTimePerIteration / placeholders.size

            println("$category placeholders: $iterations iterations in ${executionTime}ms")
            println("  Avg per iteration: ${String.format("%.2f", avgTimePerIteration)}ms")
            println("  Avg per placeholder: ${String.format("%.3f", avgTimePerPlaceholder)}ms")

            // 性能断言
            assertTrue(avgTimePerPlaceholder < 1.0, "$category 占位符的平均时间应该少于1ms")
        }
    }

    // ==================== 规则性能测试 ====================

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun testRulePerformance() {
        val ruleTemplates = mapOf(
            "stringRules" to mapOf(
                "fixedLength" to "string|10",
                "rangeLength" to "string|5-15",
                "countRule" to "string|{count: 8}",
                "rangeRule" to "string|{range: [3, 12]}"
            ),
            "numberRules" to mapOf(
                "fixedNumber" to "number|100",
                "rangeNumber" to "number|50-150",
                "incrementRule" to "number|{increment: 5}",
                "floatRule" to "number|{float: [1.0, 10.0, 2]}"
            ),
            "arrayRules" to mapOf(
                "fixedArray" to listOf("@STRING")
            ),
            "booleanRules" to mapOf(
                "simpleBool" to "boolean"
            )
        )

        ruleTemplates.forEach { (category, templates) ->
            val iterations = 500
            val executionTime = measureTimeMillis {
                repeat(iterations) {
                    templates.forEach { (ruleName, template) ->
                        val result = mock(mapOf("test" to template))
                        assertNotNull(result, "$ruleName 的结果不应为null")
                    }
                }
            }

            val totalRuleExecutions = iterations * templates.size
            val avgTimePerRule = executionTime.toDouble() / totalRuleExecutions

            println(
                "$category rules: $totalRuleExecutions executions in ${executionTime}ms (avg: ${
                    String.format(
                        "%.3f",
                        avgTimePerRule
                    )
                }ms per rule)"
            )

            // 性能断言
            assertTrue(avgTimePerRule < 2.0, "$category 规则的平均时间应该少于2ms")
        }
    }

    // ==================== 极限压力测试 ====================

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    fun testExtremeStress() {
        val template = mapOf(
            "massiveData" to mapOf(
                "users|1000" to listOf(
                    mapOf(
                        "id" to "@GUID",
                        "profile" to mapOf(
                            "name" to "@NAME",
                            "email" to "@EMAIL",
                            "bio" to "@PARAGRAPH(3, 5)"
                        ),
                        "posts|10" to listOf(
                            mapOf(
                                "id" to "@NATURAL",
                                "title" to "@SENTENCE(5, 10)",
                                "content" to "@PARAGRAPH(5, 10)",
                                "tags|5" to listOf("@WORD")
                            )
                        )
                    )
                )
            )
        )

        val executionTime = measureTimeMillis {
            val result = mock(template) as Map<String, Any>
            val massiveData = result["massiveData"] as Map<String, Any>
            val users = massiveData["users"] as List<*>

            assertEquals(1000, users.size, "应该有1000个用户")

            // 验证第一个用户的结构
            val firstUser = users[0] as Map<String, Any>
            assertNotNull(firstUser["id"], "用户应该有id")
            assertNotNull(firstUser["profile"], "用户应该有profile")
            assertNotNull(firstUser["posts"], "用户应该有posts")

            val posts = firstUser["posts"] as List<*>
            assertEquals(10, posts.size, "用户应该有10个帖子")

            // 验证第一个帖子的结构
            val firstPost = posts[0] as Map<String, Any>
            assertNotNull(firstPost["id"], "帖子应该有id")
            assertNotNull(firstPost["title"], "帖子应该有title")
            assertNotNull(firstPost["content"], "帖子应该有content")
            assertNotNull(firstPost["tags"], "帖子应该有tags")

            val tags = firstPost["tags"] as List<*>
            assertEquals(5, tags.size, "帖子应该有5个标签")
        }

        println("Extreme stress test completed in ${executionTime}ms")

        // 极限测试的性能断言 - 应该在合理时间内完成
        assertTrue(
            executionTime < 60000,
            "极限压力测试应该在60秒内完成，实际用时 ${executionTime}ms"
        )

        // 计算生成的总数据量
        val totalUsers = 1000
        val totalPosts = totalUsers * 10
        val totalTags = totalPosts * 5
        val totalDataPoints = totalUsers + totalPosts + totalTags

        val timePerDataPoint = executionTime.toDouble() / totalDataPoints
        println(
            "Generated $totalDataPoints data points in ${executionTime}ms (${
                String.format(
                    "%.3f",
                    timePerDataPoint
                )
            }ms per data point)"
        )

        assertTrue(timePerDataPoint < 1.0, "极限压力测试中每个数据点的时间应该少于1ms")
    }

    // ==================== 资源清理测试 ====================

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    fun testResourceCleanup() {
        val template = mapOf(
            "data" to mapOf(
                "content" to "@STRING(500)",
                "items|100" to listOf("@PARAGRAPH(2, 4)")
            )
        )

        val iterations = 50
        val results = mutableListOf<Any>()

        // 执行多次生成，然后清理引用
        repeat(iterations) { iteration ->
            val result = mock(template)
            results.add(result)

            // 每10次迭代清理一次结果列表
            if (iteration % 10 == 9) {
                results.clear()
                System.gc() // 建议垃圾回收
                println("Cleaned up resources at iteration $iteration")
            }
        }

        // 最终验证
        assertTrue(results.size <= 10, "结果列表应该被清理，大小: ${results.size}")
        println("Resource cleanup test completed successfully")
    }
}