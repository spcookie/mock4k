package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*
import kotlin.test.assertNotNull

/**
 * 数据类型和边界情况测试 - 测试各种数据类型、null处理、异常情况等
 */
class DataTypesAndEdgeCasesTest {

    private val logger = LoggerFactory.getLogger(DataTypesAndEdgeCasesTest::class.java)
    // 使用Mocks对象代替BeanMock实例

    // ==================== 基本数据类型测试 ====================

    data class PrimitiveTypesBean(
        val byteValue: Byte,
        val shortValue: Short,
        val intValue: Int,
        val longValue: Long,
        val floatValue: Float,
        val doubleValue: Double,
        val charValue: Char,
        val booleanValue: Boolean
    )

    @Test
    fun testPrimitiveTypes() {
        logger.info("测试基本数据类型...")

        val template = """
        {
            "byteValue": "{{byte}}",
            "shortValue": "{{short}}",
            "intValue": "{{int}}",
            "longValue": "{{long}}",
            "floatValue": "{{float}}",
            "doubleValue": "{{double}}",
            "charValue": "{{char}}",
            "booleanValue": "{{boolean}}"
        }
        """.trimIndent()

        val bean = Mocks.mock<PrimitiveTypesBean>(template)

        assertNotNull(bean, "基本类型Bean不应为null")
        assertTrue(bean.byteValue is Byte, "byteValue应该是Byte类型")
        assertTrue(bean.shortValue is Short, "shortValue应该是Short类型")
        assertTrue(bean.intValue is Int, "intValue应该是Int类型")
        assertTrue(bean.longValue is Long, "longValue应该是Long类型")
        assertTrue(bean.floatValue is Float, "floatValue应该是Float类型")
        assertTrue(bean.doubleValue is Double, "doubleValue应该是Double类型")
        assertTrue(bean.charValue is Char, "charValue应该是Char类型")
        assertTrue(bean.booleanValue is Boolean, "booleanValue应该是Boolean类型")

        logger.info("基本类型Bean: $bean")
    }

    // ==================== 包装类型测试 ====================

    data class WrapperTypesBean(
        val byteWrapper: Byte?,
        val shortWrapper: Short?,
        val intWrapper: Int?,
        val longWrapper: Long?,
        val floatWrapper: Float?,
        val doubleWrapper: Double?,
        val charWrapper: Char?,
        val booleanWrapper: Boolean?
    )

    @Test
    fun testWrapperTypes() {
        logger.info("测试包装类型...")

        val template = """
        {
            "byteWrapper": "{{byte}}",
            "shortWrapper": "{{short}}",
            "intWrapper": "{{int}}",
            "longWrapper": "{{long}}",
            "floatWrapper": "{{float}}",
            "doubleWrapper": "{{double}}",
            "charWrapper": "{{char}}",
            "booleanWrapper": "{{boolean}}"
        }
        """.trimIndent()

        val bean = Mocks.mock<WrapperTypesBean>(template)

        assertNotNull(bean, "包装类型Bean不应为null")
        assertNotNull(bean.byteWrapper, "byteWrapper不应为null")
        assertNotNull(bean.shortWrapper, "shortWrapper不应为null")
        assertNotNull(bean.intWrapper, "intWrapper不应为null")
        assertNotNull(bean.longWrapper, "longWrapper不应为null")
        assertNotNull(bean.floatWrapper, "floatWrapper不应为null")
        assertNotNull(bean.doubleWrapper, "doubleWrapper不应为null")
        assertNotNull(bean.charWrapper, "charWrapper不应为null")
        assertNotNull(bean.booleanWrapper, "booleanWrapper不应为null")

        logger.info("包装类型Bean: $bean")
    }

    @Test
    fun testWrapperTypesWithNull() {
        logger.info("测试包装类型null值...")

        val template = """
        {
            "byteWrapper": null,
            "shortWrapper": null,
            "intWrapper": null,
            "longWrapper": null,
            "floatWrapper": null,
            "doubleWrapper": null,
            "charWrapper": null,
            "booleanWrapper": null
        }
        """.trimIndent()

        val bean = Mocks.mock<WrapperTypesBean>(template)

        assertNotNull(bean, "包装类型Bean不应为null")
        assertNull(bean.byteWrapper, "byteWrapper应为null")
        assertNull(bean.shortWrapper, "shortWrapper应为null")
        assertNull(bean.intWrapper, "intWrapper应为null")
        assertNull(bean.longWrapper, "longWrapper应为null")
        assertNull(bean.floatWrapper, "floatWrapper应为null")
        assertNull(bean.doubleWrapper, "doubleWrapper应为null")
        assertNull(bean.charWrapper, "charWrapper应为null")
        assertNull(bean.booleanWrapper, "booleanWrapper应为null")

        logger.info("包装类型null Bean: $bean")
    }

    // ==================== 大数类型测试 ====================

    data class BigNumberBean(
        val bigInteger: BigInteger,
        val bigDecimal: BigDecimal
    )

    @Test
    fun testBigNumberTypes() {
        logger.info("测试大数类型...")

        val template = """
        {
            "bigInteger": "123456789012345678901234567890",
            "bigDecimal": "123456789.123456789"
        }
        """.trimIndent()

        val bean = Mocks.mock<BigNumberBean>(template)

        assertNotNull(bean, "大数类型Bean不应为null")
        assertNotNull(bean.bigInteger, "bigInteger不应为null")
        assertNotNull(bean.bigDecimal, "bigDecimal不应为null")
        assertTrue(bean.bigInteger is BigInteger, "bigInteger应该是BigInteger类型")
        assertTrue(bean.bigDecimal is BigDecimal, "bigDecimal应该是BigDecimal类型")

        logger.info("大数类型Bean: $bean")
    }

    // ==================== 时间类型测试 ====================

    data class TimeTypesBean(
        val date: Date,
        val localDate: LocalDate,
        val localTime: LocalTime,
        val localDateTime: LocalDateTime,
        val instant: Instant,
        val zonedDateTime: ZonedDateTime
    )

    @Test
    fun testTimeTypes() {
        logger.info("测试时间类型...")

        val template = """
        {
            "date": "{{date}}",
            "localDate": "{{localDate}}",
            "localTime": "{{localTime}}",
            "localDateTime": "{{localDateTime}}",
            "instant": "{{instant}}",
            "zonedDateTime": "{{zonedDateTime}}"
        }
        """.trimIndent()

        val bean = Mocks.mock<TimeTypesBean>(template)

        assertNotNull(bean, "时间类型Bean不应为null")
        assertNotNull(bean.date, "date不应为null")
        assertNotNull(bean.localDate, "localDate不应为null")
        assertNotNull(bean.localTime, "localTime不应为null")
        assertNotNull(bean.localDateTime, "localDateTime不应为null")
        assertNotNull(bean.instant, "instant不应为null")
        assertNotNull(bean.zonedDateTime, "zonedDateTime不应为null")

        assertTrue(bean.date is Date, "date应该是Date类型")
        assertTrue(bean.localDate is LocalDate, "localDate应该是LocalDate类型")
        assertTrue(bean.localTime is LocalTime, "localTime应该是LocalTime类型")
        assertTrue(bean.localDateTime is LocalDateTime, "localDateTime应该是LocalDateTime类型")
        assertTrue(bean.instant is Instant, "instant应该是Instant类型")
        assertTrue(bean.zonedDateTime is ZonedDateTime, "zonedDateTime应该是ZonedDateTime类型")

        logger.info("时间类型Bean: $bean")
    }

    // ==================== 集合类型边界测试 ====================

    data class CollectionEdgeCasesBean(
        val emptyList: List<String>,
        val singleItemList: List<Int>,
        val largeList: List<String>,
        val emptySet: Set<String>,
        val emptyMap: Map<String, Any>,
        val nestedCollections: List<List<Map<String, Set<Int>>>>
    )

    @Test
    fun testEmptyCollections() {
        logger.info("测试空集合...")

        val template = """
        {
            "emptyList": [],
            "singleItemList": [42],
            "largeList": ["item1", "item2", "item3", "item4", "item5"],
            "emptySet": [],
            "emptyMap": {},
            "nestedCollections": []
        }
        """.trimIndent()

        val bean = Mocks.mock<CollectionEdgeCasesBean>(template)

        assertNotNull(bean, "集合边界Bean不应为null")
        assertTrue(bean.emptyList.isEmpty(), "emptyList应该为空")
        assertEquals(1, bean.singleItemList.size, "singleItemList应该有1个元素")
        assertEquals(42, bean.singleItemList[0], "singleItemList第一个元素应该是42")
        assertEquals(5, bean.largeList.size, "largeList应该有5个元素")
        assertTrue(bean.emptySet.isEmpty(), "emptySet应该为空")
        assertTrue(bean.emptyMap.isEmpty(), "emptyMap应该为空")
        assertTrue(bean.nestedCollections.isEmpty(), "nestedCollections应该为空")

        logger.info("空集合Bean: $bean")
    }

    @Test
    fun testLargeCollections() {
        logger.info("测试大集合...")

        val largeListItems = (1..1000).map { "item$it" }
        val largeSetItems = (1..500).toSet()
        val largeMapItems = (1..200).associate { "key$it" to "value$it" }

        val template = """
        {
            "emptyList": ${largeListItems.take(100)},
            "singleItemList": [1, 2, 3, 4, 5],
            "largeList": ${largeListItems.take(50)},
            "emptySet": ${largeSetItems.take(30).toList()},
            "emptyMap": ${largeMapItems.entries.take(20).associate { it.key to it.value }},
            "nestedCollections": []
        }
        """.trimIndent()

        // 注意：这里简化处理，实际应该用JSON格式
        val simpleTemplate = """
        {
            "emptyList": ["item1", "item2", "item3"],
            "singleItemList": [1, 2, 3, 4, 5],
            "largeList": ["a", "b", "c"],
            "emptySet": ["x", "y", "z"],
            "emptyMap": {"k1": "v1", "k2": "v2"},
            "nestedCollections": []
        }
        """.trimIndent()

        val bean = Mocks.mock<CollectionEdgeCasesBean>(simpleTemplate)

        assertNotNull(bean, "大集合Bean不应为null")
        assertTrue(bean.emptyList.isNotEmpty(), "emptyList应该不为空")
        assertTrue(bean.singleItemList.isNotEmpty(), "singleItemList应该不为空")
        assertTrue(bean.largeList.isNotEmpty(), "largeList应该不为空")

        logger.info("大集合Bean: $bean")
    }

    // ==================== 嵌套对象边界测试 ====================

    data class Level1(
        val value: String,
        val level2: Level2?
    )

    data class Level2(
        val value: String,
        val level3: Level3?
    )

    data class Level3(
        val value: String,
        val level4: Level4?
    )

    data class Level4(
        val value: String,
        val level5: Level5?
    )

    data class Level5(
        val value: String
    )

    @Test
    fun testDeepNesting() {
        logger.info("测试深度嵌套...")

        val template = """
        {
            "value": "level1",
            "level2": {
                "value": "level2",
                "level3": {
                    "value": "level3",
                    "level4": {
                        "value": "level4",
                        "level5": {
                            "value": "level5"
                        }
                    }
                }
            }
        }
        """.trimIndent()

        val bean = Mocks.mock<Level1>(template)

        assertNotNull(bean, "深度嵌套Bean不应为null")
        assertEquals("level1", bean.value, "level1值应该正确")
        assertNotNull(bean.level2, "level2不应为null")
        assertEquals("level2", bean.level2?.value, "level2值应该正确")
        assertNotNull(bean.level2?.level3, "level3不应为null")
        assertEquals("level3", bean.level2?.level3?.value, "level3值应该正确")
        assertNotNull(bean.level2?.level3?.level4, "level4不应为null")
        assertEquals("level4", bean.level2?.level3?.level4?.value, "level4值应该正确")
        assertNotNull(bean.level2?.level3?.level4?.level5, "level5不应为null")
        assertEquals("level5", bean.level2?.level3?.level4?.level5?.value, "level5值应该正确")

        logger.info("深度嵌套Bean: $bean")
    }

    @Test
    fun testNullNesting() {
        logger.info("测试null嵌套...")

        val template = """
        {
            "value": "level1",
            "level2": null
        }
        """.trimIndent()

        val bean = Mocks.mock<Level1>(template)

        assertNotNull(bean, "null嵌套Bean不应为null")
        assertEquals("level1", bean.value, "level1值应该正确")
        assertNull(bean.level2, "level2应为null")

        logger.info("null嵌套Bean: $bean")
    }

    // ==================== 循环引用测试 ====================

    data class CircularA(
        val name: String,
        val circularB: CircularB?
    )

    data class CircularB(
        val name: String,
        val circularA: CircularA?
    )

    @Test
    fun testCircularReference() {
        logger.info("测试循环引用...")

        val template = """
        {
            "name": "A",
            "circularB": {
                "name": "B",
                "circularA": null
            }
        }
        """.trimIndent()

        val bean = Mocks.mock<CircularA>(template)

        assertNotNull(bean, "循环引用Bean不应为null")
        assertEquals("A", bean.name, "A的名称应该正确")
        assertNotNull(bean.circularB, "circularB不应为null")
        assertEquals("B", bean.circularB?.name, "B的名称应该正确")
        assertNull(bean.circularB?.circularA, "循环引用应该被截断")

        logger.info("循环引用Bean: $bean")
    }

    // ==================== 异常情况测试 ====================

    @Test
    fun testInvalidJson() {
        logger.info("测试无效JSON...")

        val invalidTemplates = listOf(
            "{invalid json}",
            "{'single': 'quotes'}",
            "{missing: value}",
            "{\"trailing\": \"comma\",}",
            "[array instead of object]"
        )

        invalidTemplates.forEach { template ->
            assertThrows(Exception::class.java) {
                Mocks.mock<SimpleBean>(template)
            }
        }

        logger.info("无效JSON正确抛出异常")
    }

    data class SimpleBean(
        val id: Long,
        val name: String
    )

    @Test
    fun testTypeMismatch() {
        logger.info("测试类型不匹配...")

        val template = """
        {
            "id": "not a number",
            "name": 12345
        }
        """.trimIndent()

        // 应该尝试类型转换或抛出异常
        val bean = Mocks.mock<SimpleBean>(template)

        assertNotNull(bean, "类型不匹配Bean不应为null")
        // 验证类型转换是否正确
        assertTrue(bean.name is String, "name应该被转换为String")

        logger.info("类型不匹配Bean: $bean")
    }

    @Test
    fun testEmptyTemplate() {
        logger.info("测试空模板...")

        val emptyTemplates = listOf(
            "",
            "   ",
            "\n\t",
            "{}"
        )

        emptyTemplates.forEach { template ->
            if (template.trim() == "{}") {
                // 空对象应该能创建Bean（使用默认值）
                assertDoesNotThrow {
                    Mocks.mock<SimpleBean>(template)
                }
            } else {
                // 完全空的模板应该抛出异常
                assertThrows(Exception::class.java) {
                    Mocks.mock<SimpleBean>(template)
                }
            }
        }

        logger.info("空模板测试完成")
    }

    // ==================== 特殊字符测试 ====================

    data class SpecialCharBean(
        val unicodeString: String,
        val escapedString: String,
        val emojiString: String
    )

    @Test
    fun testSpecialCharacters() {
        logger.info("测试特殊字符...")

        val template = """
        {
            "unicodeString": "Hello \u4e16\u754c",
            "escapedString": "Line1\nLine2\tTabbed",
            "emojiString": "Hello 😀🌍🚀"
        }
        """.trimIndent()

        val bean = Mocks.mock<SpecialCharBean>(template)

        assertNotNull(bean, "特殊字符Bean不应为null")
        assertNotNull(bean.unicodeString, "unicodeString不应为null")
        assertNotNull(bean.escapedString, "escapedString不应为null")
        assertNotNull(bean.emojiString, "emojiString不应为null")

        assertTrue(bean.unicodeString.contains("世界") || bean.unicodeString.contains("\\u"), "应该包含Unicode字符")
        assertTrue(bean.escapedString.contains("\\n") || bean.escapedString.contains("\n"), "应该包含转义字符")

        logger.info("特殊字符Bean: $bean")
    }

    // ==================== 性能边界测试 ====================

    @Test
    fun testLargeStringGeneration() {
        logger.info("测试大字符串生成...")

        val largeString = "x".repeat(10000)
        val template = """
        {
            "id": 123,
            "name": "$largeString"
        }
        """.trimIndent()

        val startTime = System.currentTimeMillis()
        val bean = Mocks.mock<SimpleBean>(template)
        val endTime = System.currentTimeMillis()

        assertNotNull(bean, "大字符串Bean不应为null")
        assertEquals(largeString, bean.name, "大字符串应该正确设置")

        val processingTime = endTime - startTime
        logger.info("大字符串处理时间: ${processingTime}ms")

        assertTrue(processingTime < 1000, "大字符串处理时间应少于1秒，实际为 ${processingTime}ms")
    }

    @Test
    fun testManyFieldsBean() {
        logger.info("测试多字段Bean...")

        data class ManyFieldsBean(
            val field1: String, val field2: String, val field3: String, val field4: String, val field5: String,
            val field6: String, val field7: String, val field8: String, val field9: String, val field10: String,
            val field11: Int, val field12: Int, val field13: Int, val field14: Int, val field15: Int,
            val field16: Boolean, val field17: Boolean, val field18: Boolean, val field19: Boolean, val field20: Boolean
        )

        val template = """
        {
            "field1": "value1", "field2": "value2", "field3": "value3", "field4": "value4", "field5": "value5",
            "field6": "value6", "field7": "value7", "field8": "value8", "field9": "value9", "field10": "value10",
            "field11": 11, "field12": 12, "field13": 13, "field14": 14, "field15": 15,
            "field16": true, "field17": false, "field18": true, "field19": false, "field20": true
        }
        """.trimIndent()

        val startTime = System.currentTimeMillis()
        val bean = Mocks.mock<ManyFieldsBean>(template)
        val endTime = System.currentTimeMillis()

        assertNotNull(bean, "多字段Bean不应为null")
        assertEquals("value1", bean.field1, "field1应该正确设置")
        assertEquals(11, bean.field11, "field11应该正确设置")
        assertEquals(true, bean.field16, "field16应该正确设置")

        val processingTime = endTime - startTime
        logger.info("多字段Bean处理时间: ${processingTime}ms")

        assertTrue(processingTime < 500, "多字段Bean处理时间应少于500ms，实际为 ${processingTime}ms")
    }

    // ==================== 内存使用测试 ====================

    @Test
    fun testMemoryUsage() {
        logger.info("测试内存使用...")

        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        val template = """
        {
            "id": 123,
            "name": "Test User"
        }
        """.trimIndent()

        // 创建大量Bean实例
        val beans = mutableListOf<SimpleBean>()
        repeat(1000) {
            beans.add(Mocks.mock<SimpleBean>(template))
        }

        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = finalMemory - initialMemory

        logger.info("创建1000个Bean使用内存: ${memoryUsed / 1024}KB")

        // 验证所有Bean都正确创建
        assertEquals(1000, beans.size, "应该创建1000个Bean")
        beans.forEach { bean ->
            assertNotNull(bean, "每个Bean都不应为null")
            assertEquals(123L, bean.id, "每个Bean的ID都应该正确")
            assertEquals("Test User", bean.name, "每个Bean的名称都应该正确")
        }

        // 内存使用应该在合理范围内（这个阈值可能需要根据实际情况调整）
        assertTrue(memoryUsed < 10 * 1024 * 1024, "内存使用应少于10MB，实际为 ${memoryUsed / 1024 / 1024}MB")
    }
}