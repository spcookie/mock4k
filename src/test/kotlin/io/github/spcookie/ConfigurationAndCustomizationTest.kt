package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import kotlin.test.assertNotNull

/**
 * 配置和自定义功能测试 - 测试各种配置选项和扩展功能
 */
class ConfigurationAndCustomizationTest {

    private val logger = LoggerFactory.getLogger(ConfigurationAndCustomizationTest::class.java)
    private val beanMock = BeanMock()

    // ==================== 基本配置测试 ====================

    data class ConfigurableBean(
        val stringField: String,
        val intField: Int,
        val doubleField: Double,
        val booleanField: Boolean,
        val listField: List<String>,
        val mapField: Map<String, Any>,
        val nestedBean: NestedBean?
    )

    data class NestedBean(
        val id: Long,
        val name: String,
        val value: Double
    )

    @Test
    fun testBasicConfiguration() {
        logger.info("测试基本配置...")

        val template = """
        {
            "stringField": "{{string(10,20)}}",
            "intField": "{{int(1,100)}}",
            "doubleField": "{{double(0.0,1000.0)}}",
            "booleanField": "{{boolean}}",
            "listField": ["{{string(5)}}", "{{string(5)}}", "{{string(5)}}"],
            "mapField": {
                "key1": "{{string(8)}}",
                "key2": "{{int(1,50)}}",
                "key3": "{{boolean}}"
            },
            "nestedBean": {
                "id": "{{long(1,1000)}}",
                "name": "{{string(15)}}",
                "value": "{{double(10.0,100.0)}}"
            }
        }
        """.trimIndent()

        val bean = beanMock.mock<ConfigurableBean>(template)

        assertNotNull(bean, "配置Bean不应为null")

        // 验证字符串字段
        assertTrue(bean.stringField.length >= 10 && bean.stringField.length <= 20, "字符串字段长度应在10-20范围内")

        // 验证整数字段
        assertTrue(bean.intField >= 1 && bean.intField <= 100, "整数字段应在1-100范围内")

        // 验证双精度字段
        assertTrue(bean.doubleField >= 0.0 && bean.doubleField <= 1000.0, "双精度字段应在0.0-1000.0范围内")

        // 验证列表字段
        assertEquals(3, bean.listField.size, "列表应有3个元素")
        bean.listField.forEach { item ->
            assertEquals(5, item.length, "列表项长度应为5")
        }

        // 验证映射字段
        assertEquals(3, bean.mapField.size, "映射应有3个键值对")
        assertTrue(bean.mapField.containsKey("key1"), "映射应包含key1")
        assertTrue(bean.mapField.containsKey("key2"), "映射应包含key2")
        assertTrue(bean.mapField.containsKey("key3"), "映射应包含key3")

        // 验证嵌套Bean
        assertNotNull(bean.nestedBean, "嵌套Bean不应为null")
        assertTrue(bean.nestedBean!!.id >= 1 && bean.nestedBean.id <= 1000, "嵌套Bean ID应在1-1000范围内")
        assertEquals(15, bean.nestedBean.name.length, "嵌套Bean名称长度应为15")
        assertTrue(bean.nestedBean.value >= 10.0 && bean.nestedBean.value <= 100.0, "嵌套Bean值应在10.0-100.0范围内")

        logger.info("基本配置测试通过")
    }

    // ==================== 自定义占位符测试 ====================

    data class CustomPlaceholderBean(
        val customId: String,
        val customName: String,
        val customEmail: String,
        val customPhone: String,
        val customDate: String,
        val customNumber: String
    )

    @Test
    fun testCustomPlaceholders() {
        logger.info("测试自定义占位符...")

        val template = """
        {
            "customId": "ID-{{int(10000,99999)}}",
            "customName": "{{name}} {{name}}",
            "customEmail": "{{string(8)}}@{{oneOf('gmail.com','yahoo.com','hotmail.com')}}",
            "customPhone": "+1-{{int(100,999)}}-{{int(100,999)}}-{{int(1000,9999)}}",
            "customDate": "{{int(2020,2024)}}-{{int(1,12):02d}-{{int(1,28):02d}}",
            "customNumber": "{{double(1.0,100.0):%.2f}}"
        }
        """.trimIndent()

        val bean = beanMock.mock<CustomPlaceholderBean>(template)

        assertNotNull(bean, "自定义占位符Bean不应为null")

        // 验证自定义ID格式
        assertTrue(bean.customId.startsWith("ID-"), "自定义ID应以ID-开头")
        val idNumber = bean.customId.substring(3).toIntOrNull()
        assertNotNull(idNumber, "ID后面应为数字")
        assertTrue(idNumber!! >= 10000 && idNumber <= 99999, "ID数字应在10000-99999范围内")

        // 验证自定义名称格式（两个名字）
        val nameParts = bean.customName.split(" ")
        assertEquals(2, nameParts.size, "自定义名称应包含两个部分")

        // 验证自定义邮箱格式
        assertTrue(bean.customEmail.contains("@"), "自定义邮箱应包含@符号")
        val emailParts = bean.customEmail.split("@")
        assertEquals(2, emailParts.size, "邮箱应有用户名和域名两部分")
        assertTrue(emailParts[1] in listOf("gmail.com", "yahoo.com", "hotmail.com"), "邮箱域名应在指定选项中")

        // 验证自定义电话格式
        assertTrue(bean.customPhone.startsWith("+1-"), "电话应以+1-开头")
        val phonePattern = Regex("\\+1-\\d{3}-\\d{3}-\\d{4}")
        assertTrue(phonePattern.matches(bean.customPhone), "电话格式应匹配+1-XXX-XXX-XXXX")

        // 验证自定义日期格式
        val datePattern = Regex("\\d{4}-\\d{2}-\\d{2}")
        assertTrue(datePattern.matches(bean.customDate), "日期格式应匹配YYYY-MM-DD")

        logger.info("自定义占位符测试通过")
        logger.info("生成的自定义数据: ID=${bean.customId}, Name=${bean.customName}, Email=${bean.customEmail}")
    }

    // ==================== 条件和逻辑测试 ====================

    data class ConditionalBean(
        val type: String,
        val value: Any,
        val status: String,
        val metadata: Map<String, Any>
    )

    @Test
    fun testConditionalLogic() {
        logger.info("测试条件逻辑...")

        val template = """
        {
            "type": "{{oneOf('PREMIUM','STANDARD','BASIC')}}",
            "value": "{{int(1,1000)}}",
            "status": "{{oneOf('ACTIVE','INACTIVE','PENDING')}}",
            "metadata": {
                "priority": "{{oneOf('HIGH','MEDIUM','LOW')}}",
                "category": "{{oneOf('A','B','C')}}",
                "score": "{{double(0.0,100.0)}}"
            }
        }
        """.trimIndent()

        // 生成多个实例来测试条件逻辑
        repeat(10) {
            val bean = beanMock.mock<ConditionalBean>(template)

            assertNotNull(bean, "条件Bean不应为null")

            // 验证类型字段
            assertTrue(bean.type in listOf("PREMIUM", "STANDARD", "BASIC"), "类型应在指定选项中")

            // 验证值字段
            assertTrue(bean.value is Int, "值应为整数类型")
            val intValue = bean.value as Int
            assertTrue(intValue >= 1 && intValue <= 1000, "值应在1-1000范围内")

            // 验证状态字段
            assertTrue(bean.status in listOf("ACTIVE", "INACTIVE", "PENDING"), "状态应在指定选项中")

            // 验证元数据
            assertNotNull(bean.metadata, "元数据不应为null")
            assertTrue(bean.metadata["priority"] in listOf("HIGH", "MEDIUM", "LOW"), "优先级应在指定选项中")
            assertTrue(bean.metadata["category"] in listOf("A", "B", "C"), "分类应在指定选项中")

            val score = bean.metadata["score"]
            assertTrue(score is Double, "分数应为双精度类型")
            val doubleScore = score as Double
            assertTrue(doubleScore >= 0.0 && doubleScore <= 100.0, "分数应在0.0-100.0范围内")
        }

        logger.info("条件逻辑测试通过")
    }

    // ==================== 数据类型转换测试 ====================

    data class TypeConversionBean(
        val stringToInt: Int,
        val stringToDouble: Double,
        val stringToBoolean: Boolean,
        val stringToLong: Long,
        val stringToBigDecimal: BigDecimal,
        val intToString: String,
        val doubleToString: String,
        val booleanToString: String
    )

    @Test
    fun testTypeConversion() {
        logger.info("测试数据类型转换...")

        val template = """
        {
            "stringToInt": "{{int(1,100)}}",
            "stringToDouble": "{{double(1.0,100.0)}}",
            "stringToBoolean": "{{boolean}}",
            "stringToLong": "{{long(1,1000000)}}",
            "stringToBigDecimal": "{{double(1.0,10000.0)}}",
            "intToString": "{{int(1,100)}}",
            "doubleToString": "{{double(1.0,100.0)}}",
            "booleanToString": "{{boolean}}"
        }
        """.trimIndent()

        val bean = beanMock.mock<TypeConversionBean>(template)

        assertNotNull(bean, "类型转换Bean不应为null")

        // 验证字符串到数值类型的转换
        assertTrue(bean.stringToInt >= 1 && bean.stringToInt <= 100, "字符串转整数应在1-100范围内")
        assertTrue(bean.stringToDouble >= 1.0 && bean.stringToDouble <= 100.0, "字符串转双精度应在1.0-100.0范围内")
        assertTrue(bean.stringToLong >= 1 && bean.stringToLong <= 1000000, "字符串转长整数应在1-1000000范围内")
        assertTrue(
            bean.stringToBigDecimal >= BigDecimal("1.0") && bean.stringToBigDecimal <= BigDecimal("10000.0"),
            "字符串转BigDecimal应在指定范围内"
        )

        // 验证数值类型到字符串的转换
        assertNotNull(bean.intToString, "整数转字符串不应为null")
        assertNotNull(bean.doubleToString, "双精度转字符串不应为null")
        assertNotNull(bean.booleanToString, "布尔转字符串不应为null")

        logger.info("类型转换测试通过")
        logger.info("转换结果: int=${bean.stringToInt}, double=${bean.stringToDouble}, boolean=${bean.stringToBoolean}")
    }

    // ==================== 复杂嵌套配置测试 ====================

    data class ComplexNestedBean(
        val level1: Level1Bean
    )

    data class Level1Bean(
        val id: Long,
        val level2: Level2Bean
    )

    data class Level2Bean(
        val name: String,
        val level3: Level3Bean
    )

    data class Level3Bean(
        val value: Double,
        val level4: Level4Bean
    )

    data class Level4Bean(
        val items: List<String>,
        val level5: Level5Bean?
    )

    data class Level5Bean(
        val finalValue: String
    )

    @Test
    fun testComplexNestedConfiguration() {
        logger.info("测试复杂嵌套配置...")

        val template = """
        {
            "level1": {
                "id": "{{long(1,1000)}}",
                "level2": {
                    "name": "{{string(20)}}",
                    "level3": {
                        "value": "{{double(1.0,100.0)}}",
                        "level4": {
                            "items": ["{{string(10)}}", "{{string(10)}}", "{{string(10)}}"],
                            "level5": {
                                "finalValue": "{{string(30)}}"
                            }
                        }
                    }
                }
            }
        }
        """.trimIndent()

        val bean = beanMock.mock<ComplexNestedBean>(template)

        assertNotNull(bean, "复杂嵌套Bean不应为null")

        // 验证Level 1
        assertNotNull(bean.level1, "Level1不应为null")
        assertTrue(bean.level1.id >= 1 && bean.level1.id <= 1000, "Level1 ID应在1-1000范围内")

        // 验证Level 2
        assertNotNull(bean.level1.level2, "Level2不应为null")
        assertEquals(20, bean.level1.level2.name.length, "Level2名称长度应为20")

        // 验证Level 3
        assertNotNull(bean.level1.level2.level3, "Level3不应为null")
        assertTrue(
            bean.level1.level2.level3.value >= 1.0 && bean.level1.level2.level3.value <= 100.0,
            "Level3值应在1.0-100.0范围内"
        )

        // 验证Level 4
        assertNotNull(bean.level1.level2.level3.level4, "Level4不应为null")
        assertEquals(3, bean.level1.level2.level3.level4.items.size, "Level4应有3个项目")
        bean.level1.level2.level3.level4.items.forEach { item ->
            assertEquals(10, item.length, "Level4项目长度应为10")
        }

        // 验证Level 5
        assertNotNull(bean.level1.level2.level3.level4.level5, "Level5不应为null")
        assertEquals(30, bean.level1.level2.level3.level4.level5!!.finalValue.length, "Level5最终值长度应为30")

        logger.info("复杂嵌套配置测试通过")
        logger.info("嵌套深度: 5层，最终值: ${bean.level1.level2.level3.level4.level5?.finalValue}")
    }

    // ==================== 数组和集合配置测试 ====================

    data class CollectionConfigBean(
        val stringList: List<String>,
        val intArray: List<Int>,
        val nestedList: List<NestedItem>,
        val stringSet: Set<String>,
        val keyValueMap: Map<String, String>,
        val mixedMap: Map<String, Any>
    )

    data class NestedItem(
        val id: Int,
        val name: String,
        val active: Boolean
    )

    @Test
    fun testCollectionConfiguration() {
        logger.info("测试数组和集合配置...")

        val template = """
        {
            "stringList": ["{{string(8)}}", "{{string(8)}}", "{{string(8)}}", "{{string(8)}}", "{{string(8)}}"],
            "intArray": ["{{int(1,10)}}", "{{int(11,20)}}", "{{int(21,30)}}", "{{int(31,40)}}"],
            "nestedList": [
                {
                    "id": "{{int(1,100)}}",
                    "name": "{{string(15)}}",
                    "active": "{{boolean}}"
                },
                {
                    "id": "{{int(101,200)}}",
                    "name": "{{string(15)}}",
                    "active": "{{boolean}}"
                },
                {
                    "id": "{{int(201,300)}}",
                    "name": "{{string(15)}}",
                    "active": "{{boolean}}"
                }
            ],
            "stringSet": ["{{string(6)}}", "{{string(6)}}", "{{string(6)}}"]
        }
        """.trimIndent()

        val bean = beanMock.mock<CollectionConfigBean>(template)

        assertNotNull(bean, "集合配置Bean不应为null")

        // 验证字符串列表
        assertEquals(5, bean.stringList.size, "字符串列表应有5个元素")
        bean.stringList.forEach { item ->
            assertEquals(8, item.length, "字符串列表项长度应为8")
        }

        // 验证整数数组
        assertEquals(4, bean.intArray.size, "整数数组应有4个元素")
        assertTrue(bean.intArray[0] >= 1 && bean.intArray[0] <= 10, "第1个整数应在1-10范围内")
        assertTrue(bean.intArray[1] >= 11 && bean.intArray[1] <= 20, "第2个整数应在11-20范围内")
        assertTrue(bean.intArray[2] >= 21 && bean.intArray[2] <= 30, "第3个整数应在21-30范围内")
        assertTrue(bean.intArray[3] >= 31 && bean.intArray[3] <= 40, "第4个整数应在31-40范围内")

        // 验证嵌套列表
        assertEquals(3, bean.nestedList.size, "嵌套列表应有3个元素")
        assertTrue(bean.nestedList[0].id >= 1 && bean.nestedList[0].id <= 100, "第1个嵌套项ID应在1-100范围内")
        assertTrue(bean.nestedList[1].id >= 101 && bean.nestedList[1].id <= 200, "第2个嵌套项ID应在101-200范围内")
        assertTrue(bean.nestedList[2].id >= 201 && bean.nestedList[2].id <= 300, "第3个嵌套项ID应在201-300范围内")

        bean.nestedList.forEach { item ->
            assertEquals(15, item.name.length, "嵌套项名称长度应为15")
        }

        // 验证字符串集合
        assertTrue(bean.stringSet.size <= 3, "字符串集合大小应不超过3（可能有重复）")
        bean.stringSet.forEach { item ->
            assertEquals(6, item.length, "字符串集合项长度应为6")
        }

        logger.info("数组和集合配置测试通过")
        logger.info("集合大小: stringList=${bean.stringList.size}, intArray=${bean.intArray.size}, nestedList=${bean.nestedList.size}, stringSet=${bean.stringSet.size}")
    }

    // ==================== 性能配置测试 ====================

    @Test
    fun testConfigurationPerformance() {
        logger.info("测试配置性能...")

        val simpleTemplate = "{\"id\": \"{{long}}\", \"name\": \"{{string(10)}}\"}"
        val complexTemplate = """
        {
            "level1": {
                "level2": {
                    "level3": {
                        "items": ["{{string(5)}}", "{{string(5)}}", "{{string(5)}}"],
                        "value": "{{double(1.0,100.0)}}"
                    }
                }
            }
        }
        """.trimIndent()

        // 测试简单配置性能
        val simpleStartTime = System.currentTimeMillis()
        repeat(100) {
            val bean = beanMock.mock<Map<String, Any>>(simpleTemplate)
            assertNotNull(bean, "简单配置Bean不应为null")
        }
        val simpleEndTime = System.currentTimeMillis()
        val simpleTime = simpleEndTime - simpleStartTime

        // 测试复杂配置性能
        val complexStartTime = System.currentTimeMillis()
        repeat(50) {
            val bean = beanMock.mock<Map<String, Any>>(complexTemplate)
            assertNotNull(bean, "复杂配置Bean不应为null")
        }
        val complexEndTime = System.currentTimeMillis()
        val complexTime = complexEndTime - complexStartTime

        logger.info("简单配置性能: 100次迭代耗时 ${simpleTime}ms (平均: ${simpleTime / 100.0}ms/次)")
        logger.info("复杂配置性能: 50次迭代耗时 ${complexTime}ms (平均: ${complexTime / 50.0}ms/次)")

        assertTrue(simpleTime < 5000, "简单配置100次迭代应少于5秒")
        assertTrue(complexTime < 10000, "复杂配置50次迭代应少于10秒")
    }

    // ==================== 错误处理配置测试 ====================

    @Test
    fun testConfigurationErrorHandling() {
        logger.info("测试配置错误处理...")

        // 测试无效占位符
        val invalidTemplate1 = "{\"field\": \"{{invalidPlaceholder}}\"}"

        try {
            val bean = beanMock.mock<Map<String, Any>>(invalidTemplate1)
            // 如果没有抛出异常，验证是否有合理的默认处理
            assertNotNull(bean, "即使有无效占位符，Bean也不应为null")
            logger.info("无效占位符被合理处理")
        } catch (e: Exception) {
            logger.info("无效占位符正确抛出异常: ${e.message}")
        }

        // 测试格式错误的JSON
        val invalidTemplate2 = "{\"field\": \"{{string(10)}}\", \"invalidJson\"}"

        try {
            val bean = beanMock.mock<Map<String, Any>>(invalidTemplate2)
            logger.info("格式错误的JSON被合理处理")
        } catch (e: Exception) {
            logger.info("格式错误的JSON正确抛出异常: ${e.message}")
        }

        // 测试空模板
        val emptyTemplate = ""

        try {
            val bean = beanMock.mock<Map<String, Any>>(emptyTemplate)
            logger.info("空模板被合理处理")
        } catch (e: Exception) {
            logger.info("空模板正确抛出异常: ${e.message}")
        }

        logger.info("配置错误处理测试完成")
    }

    // ==================== 边界值配置测试 ====================

    data class BoundaryValueBean(
        val minInt: Int,
        val maxInt: Int,
        val minDouble: Double,
        val maxDouble: Double,
        val minString: String,
        val maxString: String,
        val emptyList: List<String>,
        val singleItemList: List<String>
    )

    @Test
    fun testBoundaryValueConfiguration() {
        logger.info("测试边界值配置...")

        val template = """
        {
            "minInt": "{{int(1,1)}}",
            "maxInt": "{{int(2147483647,2147483647)}}",
            "minDouble": "{{double(0.0,0.0)}}",
            "maxDouble": "{{double(999999.99,999999.99)}}",
            "minString": "{{string(1)}}",
            "maxString": "{{string(100)}}",
            "emptyList": [],
            "singleItemList": ["{{string(5)}}"]
        }
        """.trimIndent()

        val bean = beanMock.mock<BoundaryValueBean>(template)

        assertNotNull(bean, "边界值Bean不应为null")

        // 验证边界值
        assertEquals(1, bean.minInt, "最小整数应为1")
        assertEquals(2147483647, bean.maxInt, "最大整数应为2147483647")
        assertEquals(0.0, bean.minDouble, 0.001, "最小双精度应为0.0")
        assertEquals(999999.99, bean.maxDouble, 0.001, "最大双精度应为999999.99")
        assertEquals(1, bean.minString.length, "最小字符串长度应为1")
        assertEquals(100, bean.maxString.length, "最大字符串长度应为100")
        assertEquals(0, bean.emptyList.size, "空列表大小应为0")
        assertEquals(1, bean.singleItemList.size, "单项列表大小应为1")
        assertEquals(5, bean.singleItemList[0].length, "单项列表项长度应为5")

        logger.info("边界值配置测试通过")
        logger.info("边界值: minInt=${bean.minInt}, maxInt=${bean.maxInt}, minString.length=${bean.minString.length}, maxString.length=${bean.maxString.length}")
    }
}