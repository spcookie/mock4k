package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.test.assertNotNull

/**
 * Bean内省和映射测试 - 测试BeanIntrospect和BeanMockMapper的功能
 */
class BeanIntrospectAndMapperTest {

    private val logger = LoggerFactory.getLogger(BeanIntrospectAndMapperTest::class.java)
    private val beanIntrospect = BeanIntrospect()
    private val typeAdapter = TypeAdapter()
    private val beanMockMapper = BeanMockMapper(typeAdapter)

    // ==================== Bean内省测试 ====================

    /**
     * 简单Bean内省测试
     */
    data class SimpleBean(
        val id: Long,
        val name: String,
        val active: Boolean
    )

    @Test
    fun testSimpleBeanIntrospection() {
        logger.info("测试简单Bean内省...")

        val config = BeanMockConfig(includePrivate = true)
        val template = beanIntrospect.analyzeBean(SimpleBean::class, config)

        assertNotNull(template, "模板不应为null")
        assertTrue(template is Map<*, *>, "模板应该是一个Map")

        val templateMap = template as Map<String, Any>
        assertTrue(templateMap.containsKey("id"), "模板应该包含id字段")
        assertTrue(templateMap.containsKey("name"), "模板应该包含name字段")
        assertTrue(templateMap.containsKey("active"), "模板应该包含active字段")

        logger.info("简单Bean模板: $template")
    }

    /**
     * 复杂Bean内省测试
     */
    data class Address(
        val street: String,
        val city: String,
        val zipCode: String
    )

    data class ComplexBean(
        val id: Long,
        val name: String,
        val address: Address,
        val tags: List<String>,
        val metadata: Map<String, Any>,
        val scores: Set<Int>,
        val optionalValue: Optional<String>
    )

    @Test
    fun testComplexBeanIntrospection() {
        logger.info("测试复杂Bean内省...")

        val config = BeanMockConfig(includePrivate = true, depth = 3)
        val template = beanIntrospect.analyzeBean(ComplexBean::class, config)

        assertNotNull(template, "复杂Bean模板不应为null")
        assertTrue(template is Map<*, *>, "模板应该是一个Map")

        val templateMap = template as Map<String, Any>
        assertTrue(templateMap.containsKey("id"), "模板应该包含id字段")
        assertTrue(templateMap.containsKey("name"), "模板应该包含name字段")
        assertTrue(templateMap.containsKey("address"), "模板应该包含address字段")
        assertTrue(templateMap.containsKey("tags"), "模板应该包含tags字段")
        assertTrue(templateMap.containsKey("metadata"), "模板应该包含metadata字段")
        assertTrue(templateMap.containsKey("scores"), "模板应该包含scores字段")
        assertTrue(templateMap.containsKey("optionalValue"), "模板应该包含optionalValue字段")

        // 验证嵌套对象
        val addressTemplate = templateMap["address"]
        assertNotNull(addressTemplate, "地址模板不应为null")
        assertTrue(addressTemplate is Map<*, *>, "地址模板应该是一个Map")

        logger.info("复杂Bean模板: $template")
    }

    /**
     * 容器类型内省测试
     */
    data class ContainerBean(
        val optionalString: Optional<String>,
        val futureInt: CompletableFuture<Int>,
        val lazyDouble: Lazy<Double>,
        val supplierList: java.util.function.Supplier<List<String>>
    )

    @Test
    fun testContainerTypeIntrospection() {
        logger.info("测试容器类型内省...")

        val config = BeanMockConfig(includePrivate = true)
        val template = beanIntrospect.analyzeBean(ContainerBean::class, config)

        assertNotNull(template, "容器Bean模板不应为null")
        assertTrue(template is Map<*, *>, "模板应该是一个Map")

        val templateMap = template as Map<String, Any>
        assertTrue(templateMap.containsKey("optionalString"), "模板应该包含optionalString字段")
        assertTrue(templateMap.containsKey("futureInt"), "模板应该包含futureInt字段")
        assertTrue(templateMap.containsKey("lazyDouble"), "模板应该包含lazyDouble字段")
        assertTrue(templateMap.containsKey("supplierList"), "模板应该包含supplierList字段")

        logger.info("容器Bean模板: $template")
    }

    // ==================== 属性类型分析测试 ====================

    @Test
    fun testBasicTypeAnalysis() {
        logger.info("测试基本类型分析...")

        val stringType = String::class.createType()
        val intType = Int::class.createType()
        val booleanType = Boolean::class.createType()
        val doubleType = Double::class.createType()

        val stringTemplate = beanIntrospect.analyzePropertyType(stringType, null, null)
        val intTemplate = beanIntrospect.analyzePropertyType(intType, null, null)
        val booleanTemplate = beanIntrospect.analyzePropertyType(booleanType, null, null)
        val doubleTemplate = beanIntrospect.analyzePropertyType(doubleType, null, null)

        assertNotNull(stringTemplate, "字符串类型模板不应为null")
        assertNotNull(intTemplate, "整数类型模板不应为null")
        assertNotNull(booleanTemplate, "布尔类型模板不应为null")
        assertNotNull(doubleTemplate, "双精度类型模板不应为null")

        logger.info("基本类型模板 - String: $stringTemplate, Int: $intTemplate, Boolean: $booleanTemplate, Double: $doubleTemplate")
    }

    @Test
    fun testCollectionTypeAnalysis() {
        logger.info("测试集合类型分析...")
        val listType = List::class.createType(arguments = listOf(KTypeProjection.covariant(String::class.createType())))
        val setType = Set::class.createType(arguments = listOf(KTypeProjection.covariant(Int::class.createType())))
        val mapType = Map::class.createType(
            arguments = listOf(
                KTypeProjection.invariant(String::class.createType()),
                KTypeProjection.covariant(Any::class.createType())
            )
        )

        val listTemplate = beanIntrospect.analyzePropertyType(listType, null, null)
        val setTemplate = beanIntrospect.analyzePropertyType(setType, null, null)
        val mapTemplate = beanIntrospect.analyzePropertyType(mapType, null, null)

        assertNotNull(listTemplate, "列表类型模板不应为null")
        assertNotNull(setTemplate, "集合类型模板不应为null")
        assertNotNull(mapTemplate, "映射类型模板不应为null")

        logger.info("集合类型模板 - List: $listTemplate, Set: $setTemplate, Map: $mapTemplate")
    }

    @Test
    fun testContainerTypeAnalysis() {
        logger.info("测试容器类型分析...")

        val optionalType = Optional::class.createType(listOf(KTypeProjection.invariant(String::class.createType())))
        val futureType = CompletableFuture::class.createType(listOf(KTypeProjection.invariant(Int::class.createType())))

        val optionalTemplate = beanIntrospect.analyzePropertyType(optionalType, null, null)
        val futureTemplate = beanIntrospect.analyzePropertyType(futureType, null, null)

        assertNotNull(optionalTemplate, "Optional类型模板不应为null")
        assertNotNull(futureTemplate, "Future类型模板不应为null")

        logger.info("容器类型模板 - Optional: $optionalTemplate, Future: $futureTemplate")
    }

    // ==================== Bean映射测试 ====================

    @Test
    fun testSimpleBeanMapping() {
        logger.info("测试简单Bean映射...")

        val data = mapOf(
            "id" to 12345L,
            "name" to "Test User",
            "active" to true
        )

        val config = BeanMockConfig()
        val bean = beanMockMapper.mapToBean(SimpleBean::class, data, config)

        assertNotNull(bean, "映射的Bean不应为null")
        assertEquals(12345L, bean.id, "ID应该正确映射")
        assertEquals("Test User", bean.name, "名称应该正确映射")
        assertEquals(true, bean.active, "状态应该正确映射")

        logger.info("映射的简单Bean: $bean")
    }

    @Test
    fun testComplexBeanMapping() {
        logger.info("测试复杂Bean映射...")

        val data = mapOf(
            "id" to 67890L,
            "name" to "Complex User",
            "address" to mapOf(
                "street" to "123 Main St",
                "city" to "Test City",
                "zipCode" to "12345"
            ),
            "tags" to listOf("tag1", "tag2", "tag3"),
            "metadata" to mapOf(
                "key1" to "value1",
                "key2" to 42
            ),
            "scores" to setOf(85, 90, 95),
            "optionalValue" to "optional content"
        )

        val config = BeanMockConfig()
        val bean = beanMockMapper.mapToBean(ComplexBean::class, data, config)

        assertNotNull(bean, "映射的复杂Bean不应为null")
        assertEquals(67890L, bean.id, "ID应该正确映射")
        assertEquals("Complex User", bean.name, "名称应该正确映射")

        // 验证嵌套对象
        assertNotNull(bean.address, "地址不应为null")
        assertEquals("123 Main St", bean.address.street, "街道应该正确映射")
        assertEquals("Test City", bean.address.city, "城市应该正确映射")
        assertEquals("12345", bean.address.zipCode, "邮编应该正确映射")

        // 验证集合
        assertNotNull(bean.tags, "标签不应为null")
        assertEquals(3, bean.tags.size, "标签数量应该正确")
        assertTrue(bean.tags.contains("tag1"), "应该包含tag1")

        assertNotNull(bean.metadata, "元数据不应为null")
        assertEquals("value1", bean.metadata["key1"], "元数据key1应该正确映射")
        assertEquals(42, bean.metadata["key2"], "元数据key2应该正确映射")

        assertNotNull(bean.scores, "分数不应为null")
        assertTrue(bean.scores.contains(85), "应该包含分数85")

        // 验证容器类型
        assertNotNull(bean.optionalValue, "Optional值不应为null")

        logger.info("映射的复杂Bean: $bean")
    }

    @Test
    fun testContainerBeanMapping() {
        logger.info("测试容器Bean映射...")

        val data = mapOf(
            "optionalString" to "optional value",
            "futureInt" to 42,
            "lazyDouble" to 3.14,
            "supplierList" to listOf("item1", "item2")
        )

        val config = BeanMockConfig()
        val bean = beanMockMapper.mapToBean(ContainerBean::class, data, config)

        assertNotNull(bean, "映射的容器Bean不应为null")
        assertNotNull(bean.optionalString, "Optional字符串不应为null")
        assertNotNull(bean.futureInt, "Future整数不应为null")
        assertNotNull(bean.lazyDouble, "Lazy双精度数不应为null")
        assertNotNull(bean.supplierList, "Supplier列表不应为null")

        logger.info("映射的容器Bean: $bean")
    }

    // ==================== 边界情况映射测试 ====================

    @Test
    fun testMappingWithNullValues() {
        logger.info("测试包含null值的映射...")

        val data = mapOf(
            "id" to 123L,
            "name" to null,
            "active" to true
        )

        val config = BeanMockConfig()

        // 对于不可空字段包含null值，应该使用默认值
        assertDoesNotThrow {
            val bean = beanMockMapper.mapToBean(SimpleBean::class, data, config)
            logger.info("包含null值的Bean: $bean")
        }

        logger.info("null值映射正确处理异常")
    }

    @Test
    fun testMappingWithMissingFields() {
        logger.info("测试缺少字段的映射...")

        val data = mapOf(
            "id" to 123L
            // 缺少name和active字段
        )

        val config = BeanMockConfig()

        // 对于缺少必需字段，应该使用默认值
        assertDoesNotThrow {
            val bean = beanMockMapper.mapToBean(SimpleBean::class, data, config)
            logger.info("测试缺少字段的Bean: $bean")
        }

        logger.info("缺少字段映射正确处理异常")
    }

    @Test
    fun testMappingWithExtraFields() {
        logger.info("测试包含额外字段的映射...")

        val data = mapOf(
            "id" to 123L,
            "name" to "Test User",
            "active" to true,
            "extraField" to "extra value", // 额外字段
            "anotherExtra" to 999
        )

        val config = BeanMockConfig()
        val bean = beanMockMapper.mapToBean(SimpleBean::class, data, config)

        // 额外字段应该被忽略，Bean应该正常创建
        assertNotNull(bean, "包含额外字段的Bean应该能正常创建")
        assertEquals(123L, bean.id, "ID应该正确映射")
        assertEquals("Test User", bean.name, "名称应该正确映射")
        assertEquals(true, bean.active, "状态应该正确映射")

        logger.info("包含额外字段的Bean映射成功: $bean")
    }

    // ==================== 类型转换测试 ====================

    @Test
    fun testTypeConversion() {
        logger.info("测试类型转换...")

        val data = mapOf(
            "id" to "123", // 字符串转Long
            "name" to 456, // 数字转String
            "active" to "true" // 字符串转Boolean
        )

        val config = BeanMockConfig()
        val bean = beanMockMapper.mapToBean(SimpleBean::class, data, config)

        assertNotNull(bean, "类型转换Bean不应为null")
        // 验证类型转换是否正确
        assertTrue(bean.id is Long, "ID应该是Long类型")
        assertTrue(bean.name is String, "名称应该是String类型")
        assertTrue(bean.active is Boolean, "状态应该是Boolean类型")

        logger.info("类型转换Bean: $bean")
    }

    // ==================== 配置测试 ====================

    @Test
    fun testDifferentConfigurations() {
        logger.info("测试不同配置下的内省和映射...")

        val configs = listOf(
            BeanMockConfig(includePrivate = true, includeStatic = false, depth = 1),
            BeanMockConfig(includePrivate = false, includeStatic = true, depth = 3)
        )

        configs.forEach { config ->
            // 测试内省
            val template = beanIntrospect.analyzeBean(ComplexBean::class, config)
            assertNotNull(template, "配置 $config 下的模板不应为null")

            // 测试映射
            val data = mapOf(
                "id" to 123L,
                "name" to "Test",
                "address" to mapOf("street" to "St", "city" to "City", "zipCode" to "123"),
                "tags" to listOf("tag"),
                "metadata" to mapOf("key" to "value"),
                "scores" to setOf(90),
                "optionalValue" to "value"
            )

            val bean = beanMockMapper.mapToBean(ComplexBean::class, data, config)
            assertNotNull(bean, "配置 $config 下的Bean不应为null")

            logger.info("配置 $config 测试通过")
        }
    }

    // ==================== 性能测试 ====================

    @Test
    fun testIntrospectionPerformance() {
        logger.info("测试内省性能...")

        val config = BeanMockConfig()
        val iterations = 100

        val startTime = System.currentTimeMillis()
        repeat(iterations) {
            val template = beanIntrospect.analyzeBean(ComplexBean::class, config)
            assertNotNull(template)
        }
        val endTime = System.currentTimeMillis()

        val totalTime = endTime - startTime
        val avgTime = totalTime.toDouble() / iterations

        logger.info("内省性能: $iterations 次迭代耗时 ${totalTime}ms (平均: ${String.format("%.2f", avgTime)}ms/次)")

        assertTrue(avgTime < 20.0, "内省平均时间应少于20ms，实际为 ${avgTime}ms")
    }

    @Test
    fun testMappingPerformance() {
        logger.info("测试映射性能...")

        val config = BeanMockConfig()
        val data = mapOf(
            "id" to 123L,
            "name" to "Test User",
            "address" to mapOf("street" to "Street", "city" to "City", "zipCode" to "123"),
            "tags" to listOf("tag1", "tag2"),
            "metadata" to mapOf("key" to "value"),
            "scores" to setOf(85, 90),
            "optionalValue" to "value"
        )

        val iterations = 100
        val startTime = System.currentTimeMillis()

        repeat(iterations) {
            val bean = beanMockMapper.mapToBean(ComplexBean::class, data, config)
            assertNotNull(bean)
        }

        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        val avgTime = totalTime.toDouble() / iterations

        logger.info("映射性能: $iterations 次迭代耗时 ${totalTime}ms (平均: ${String.format("%.2f", avgTime)}ms/次)")

        assertTrue(avgTime < 30.0, "映射平均时间应少于30ms，实际为 ${avgTime}ms")
    }
}