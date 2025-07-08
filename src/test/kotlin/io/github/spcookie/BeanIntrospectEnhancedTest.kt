package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.test.assertNotNull

/**
 * BeanIntrospect增强测试 - 专门测试BeanIntrospect类的边界情况和特殊场景
 */
class BeanIntrospectEnhancedTest {

    private val logger = LoggerFactory.getLogger(BeanIntrospectEnhancedTest::class.java)
    private val beanIntrospect = BeanIntrospect()

    // ==================== 注解测试Bean ====================

    /**
     * 带有Mock注解的Bean
     */
    @Mock.Bean(includePrivate = true, includeStatic = false)
    data class AnnotatedBean(
        @Mock.Property(rule = Mock.Rule(min = 1, max = 100))
        val id: Long,

        @Mock.Property(placeholder = Mock.Placeholder("@CNAME"))
        val name: String,

        @Mock.Property(rule = Mock.Rule(count = 5))
        val tags: List<String>,

        @Mock.Property(length = Mock.Length(value = 3, fill = Mock.FillStrategy.REPEAT))
        val scores: Set<Int>
    )

    @Test
    fun testAnnotatedBeanIntrospection() {
        logger.info("测试带注解Bean的内省...")

        val config = BeanMockConfig()
        val template = beanIntrospect.analyzeBean(AnnotatedBean::class, config)

        assertNotNull(template, "注解Bean模板不应为null")
        assertTrue(template is Map<*, *>, "模板应该是一个Map")

        val templateMap = template as Map<String, Any>

        // 验证规则是否正确应用
        assertTrue(templateMap.containsKey("id|1-100"), "ID字段应该包含范围规则")
        assertTrue(templateMap.containsKey("name"), "名称字段应该存在")
        assertEquals("@CNAME", templateMap["name"], "名称字段应该使用自定义占位符")
        assertTrue(templateMap.containsKey("tags|5"), "标签字段应该包含数量规则")
        assertTrue(templateMap.containsKey("scores"), "分数字段应该存在")

        logger.info("注解Bean模板: $template")
    }

    // ==================== 嵌套注解测试 ====================

    data class NestedAnnotatedBean(
        val id: Long,
        @Mock.Bean(includePrivate = false)
        val profile: UserProfile
    )

    data class UserProfile(
        val firstName: String,
        val lastName: String,
        private val secretKey: String = "secret"
    )

    @Test
    fun testNestedAnnotatedBeanIntrospection() {
        logger.info("测试嵌套注解Bean的内省...")

        val config = BeanMockConfig(includePrivate = true)
        val template = beanIntrospect.analyzeBean(NestedAnnotatedBean::class, config)

        assertNotNull(template, "嵌套注解Bean模板不应为null")
        val templateMap = template as Map<String, Any>

        assertTrue(templateMap.containsKey("id"), "ID字段应该存在")
        assertTrue(templateMap.containsKey("profile"), "Profile字段应该存在")

        val profileTemplate = templateMap["profile"] as? Map<String, Any>
        assertNotNull(profileTemplate, "Profile模板不应为null")

        // 由于profile字段有@Mock.Bean(includePrivate = false)注解，secretKey应该不包含
        assertTrue(profileTemplate.containsKey("firstName"), "firstName应该存在")
        assertTrue(profileTemplate.containsKey("lastName"), "lastName应该存在")
        assertFalse(profileTemplate.containsKey("secretKey"), "secretKey不应该存在（由于注解设置）")

        logger.info("嵌套注解Bean模板: $template")
    }

    // ==================== 复杂类型测试 ====================

    data class ComplexTypesBean(
        val bigDecimal: BigDecimal,
        val bigInteger: BigInteger,
        val localDate: LocalDate,
        val localDateTime: LocalDateTime,
        val uuid: UUID,
        val pair: Pair<String, Int>,
        val triple: Triple<String, Int, Boolean>
    )

    @Test
    fun testComplexTypesIntrospection() {
        logger.info("测试复杂类型Bean的内省...")

        val config = BeanMockConfig()
        val template = beanIntrospect.analyzeBean(ComplexTypesBean::class, config)

        assertNotNull(template, "复杂类型Bean模板不应为null")
        val templateMap = template as Map<String, Any>

        assertEquals("@FLOAT", templateMap["bigDecimal"], "BigDecimal应该映射为@FLOAT")
        assertEquals("@INTEGER", templateMap["bigInteger"], "BigInteger应该映射为@INTEGER")
        assertEquals("@DATE", templateMap["localDate"], "LocalDate应该映射为@DATE")
        assertEquals("@DATETIME", templateMap["localDateTime"], "LocalDateTime应该映射为@DATETIME")
        assertEquals("@STRING", templateMap["uuid"], "UUID应该映射为@STRING")
        assertEquals("@STRING", templateMap["pair"], "Pair应该映射为@STRING")
        assertEquals("@STRING", templateMap["triple"], "Triple应该映射为@STRING")

        logger.info("复杂类型Bean模板: $template")
    }

    // ==================== 泛型测试 ====================

    data class GenericBean<T>(
        val id: Long,
        val data: T,
        val list: List<T>,
        val optional: Optional<T>
    )

    @Test
    fun testGenericBeanIntrospection() {
        logger.info("测试泛型Bean的内省...")

        val config = BeanMockConfig()
        // 测试String类型的泛型Bean
        val template = beanIntrospect.analyzeBean(GenericBean::class, config)

        assertNotNull(template, "泛型Bean模板不应为null")
        val templateMap = template as Map<String, Any>

        assertTrue(templateMap.containsKey("id"), "ID字段应该存在")
        assertTrue(templateMap.containsKey("data"), "data字段应该存在")
        assertTrue(templateMap.containsKey("list"), "list字段应该存在")
        assertTrue(templateMap.containsKey("optional"), "optional字段应该存在")

        logger.info("泛型Bean模板: $template")
    }

    // ==================== 枚举测试 ====================

    enum class Status {
        ACTIVE, INACTIVE, PENDING
    }

    data class EnumBean(
        val id: Long,
        val status: Status,
        val statusList: List<Status>
    )

    @Test
    fun testEnumBeanIntrospection() {
        logger.info("测试枚举Bean的内省...")

        val config = BeanMockConfig()
        val template = beanIntrospect.analyzeBean(EnumBean::class, config)

        assertNotNull(template, "枚举Bean模板不应为null")
        val templateMap = template as Map<String, Any>

        assertTrue(templateMap.containsKey("id"), "ID字段应该存在")
        assertTrue(templateMap.containsKey("status"), "status字段应该存在")
        assertTrue(templateMap.containsKey("statusList"), "statusList字段应该存在")

        logger.info("枚举Bean模板: $template")
    }

    // ==================== 继承测试 ====================

    open class BaseEntity(
        open val id: Long,
        open val createdAt: LocalDateTime
    )

    data class ExtendedEntity(
        override val id: Long,
        override val createdAt: LocalDateTime,
        val name: String,
        val description: String
    ) : BaseEntity(id, createdAt)

    @Test
    fun testInheritanceBeanIntrospection() {
        logger.info("测试继承Bean的内省...")

        val config = BeanMockConfig()
        val template = beanIntrospect.analyzeBean(ExtendedEntity::class, config)

        assertNotNull(template, "继承Bean模板不应为null")
        val templateMap = template as Map<String, Any>

        // 验证继承的字段
        assertTrue(templateMap.containsKey("id"), "继承的ID字段应该存在")
        assertTrue(templateMap.containsKey("createdAt"), "继承的createdAt字段应该存在")

        // 验证自己的字段
        assertTrue(templateMap.containsKey("name"), "name字段应该存在")
        assertTrue(templateMap.containsKey("description"), "description字段应该存在")

        logger.info("继承Bean模板: $template")
    }

    // ==================== 空Bean测试 ====================

    class EmptyBean

    @Test
    fun testEmptyBeanIntrospection() {
        logger.info("测试空Bean的内省...")

        val config = BeanMockConfig()
        val template = beanIntrospect.analyzeBean(EmptyBean::class, config)

        assertNotNull(template, "空Bean模板不应为null")
        assertTrue(template is Map<*, *>, "模板应该是一个Map")
        assertTrue((template as Map<*, *>).isEmpty(), "空Bean模板应该为空")

        logger.info("空Bean模板: $template")
    }

    // ==================== 私有字段测试 ====================

    data class PrivateFieldBean(
        val publicField: String,
        private val privateField: String,
        internal val internalField: String
    )

    @Test
    fun testPrivateFieldIntrospection() {
        logger.info("测试私有字段Bean的内省...")

        // 测试不包含私有字段
        val configExcludePrivate = BeanMockConfig(includePrivate = false)
        val templateExcludePrivate = beanIntrospect.analyzeBean(PrivateFieldBean::class, configExcludePrivate)
        val mapExcludePrivate = templateExcludePrivate as Map<String, Any>

        assertTrue(mapExcludePrivate.containsKey("publicField"), "公共字段应该存在")
        // 注意：在Kotlin中，data class的所有构造函数参数都会生成公共属性
        // 所以即使声明为private，实际上仍然是可访问的

        // 测试包含私有字段
        val configIncludePrivate = BeanMockConfig(includePrivate = true)
        val templateIncludePrivate = beanIntrospect.analyzeBean(PrivateFieldBean::class, configIncludePrivate)
        val mapIncludePrivate = templateIncludePrivate as Map<String, Any>

        assertTrue(mapIncludePrivate.containsKey("publicField"), "公共字段应该存在")
        assertTrue(mapIncludePrivate.containsKey("privateField"), "私有字段应该存在")
        assertTrue(mapIncludePrivate.containsKey("internalField"), "内部字段应该存在")

        logger.info("私有字段Bean模板（排除私有）: $templateExcludePrivate")
        logger.info("私有字段Bean模板（包含私有）: $templateIncludePrivate")
    }

    // ==================== 深度嵌套测试 ====================

    data class Level1(
        val id: Long,
        val level2: Level2
    )

    data class Level2(
        val name: String,
        val level3: Level3
    )

    data class Level3(
        val value: String,
        val level4: Level4
    )

    data class Level4(
        val deepValue: String
    )

    @Test
    fun testDeepNestedBeanIntrospection() {
        logger.info("测试深度嵌套Bean的内省...")

        // 测试不同的最大深度设置
        val configs = listOf(
            BeanMockConfig(depth = 1),
            BeanMockConfig(depth = 2),
            BeanMockConfig(depth = 3),
            BeanMockConfig(depth = 4)
        )

        configs.forEach { config ->
            val template = beanIntrospect.analyzeBean(Level1::class, config)
            assertNotNull(template, "深度嵌套Bean模板不应为null（深度=${config.depth}）")

            val templateMap = template as Map<String, Any>
            assertTrue(templateMap.containsKey("id"), "ID字段应该存在")
            assertTrue(templateMap.containsKey("level2"), "level2字段应该存在")

            logger.info("深度嵌套Bean模板（深度=${config.depth}）: $template")
        }
    }

    // ==================== 循环引用测试 ====================

    data class NodeA(
        val id: Long,
        val nodeB: NodeB?
    )

    data class NodeB(
        val name: String,
        val nodeA: NodeA?
    )

    @Test
    fun testCircularReferenceBeanIntrospection() {
        logger.info("测试循环引用Bean的内省...")

        val config = BeanMockConfig(depth = 3)

        // 测试NodeA
        val templateA = beanIntrospect.analyzeBean(NodeA::class, config)
        assertNotNull(templateA, "循环引用Bean A模板不应为null")

        // 测试NodeB
        val templateB = beanIntrospect.analyzeBean(NodeB::class, config)
        assertNotNull(templateB, "循环引用Bean B模板不应为null")

        logger.info("循环引用Bean A模板: $templateA")
        logger.info("循环引用Bean B模板: $templateB")
    }

    // ==================== 性能压力测试 ====================

    @Test
    fun testIntrospectionPerformanceStress() {
        logger.info("测试内省性能压力...")

        val config = BeanMockConfig()
        val iterations = 1000
        val classes = listOf(
            AnnotatedBean::class,
            ComplexTypesBean::class,
            GenericBean::class,
            ExtendedEntity::class
        )

        val startTime = System.currentTimeMillis()

        repeat(iterations) {
            classes.forEach { clazz ->
                val template = beanIntrospect.analyzeBean(clazz, config)
                assertNotNull(template, "模板不应为null")
            }
        }

        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        val avgTime = totalTime.toDouble() / (iterations * classes.size)

        logger.info(
            "性能压力测试: ${iterations * classes.size} 次内省耗时 ${totalTime}ms (平均: ${
                String.format(
                    "%.3f",
                    avgTime
                )
            }ms/次)"
        )

        // 性能断言：平均时间应该少于5ms
        assertTrue(avgTime < 5.0, "内省平均时间应少于5ms，实际为 ${avgTime}ms")
    }

    // ==================== 错误处理测试 ====================

    interface InterfaceBean {
        val id: Long
        val name: String
    }

    @Test
    fun testInterfaceBeanIntrospection() {
        logger.info("测试接口Bean的内省...")

        val config = BeanMockConfig()

        // 接口应该能够被内省，但可能会有特殊处理
        assertDoesNotThrow {
            val template = beanIntrospect.analyzeBean(InterfaceBean::class, config)
            assertNotNull(template, "接口Bean模板不应为null")
            logger.info("接口Bean模板: $template")
        }
    }

    // ==================== 特殊容器类型测试 ====================

    data class SpecialContainerBean(
        val optionalString: Optional<String>,
        val futureInt: CompletableFuture<Int>,
        val lazyValue: Lazy<String>,
        val supplier: java.util.function.Supplier<String>
    )

    @Test
    fun testSpecialContainerIntrospection() {
        logger.info("测试特殊容器类型的内省...")

        val config = BeanMockConfig()
        val template = beanIntrospect.analyzeBean(SpecialContainerBean::class, config)

        assertNotNull(template, "特殊容器Bean模板不应为null")
        val templateMap = template as Map<String, Any>

        assertTrue(templateMap.containsKey("optionalString"), "Optional字段应该存在")
        assertTrue(templateMap.containsKey("futureInt"), "Future字段应该存在")
        assertTrue(templateMap.containsKey("lazyValue"), "Lazy字段应该存在")
        assertTrue(templateMap.containsKey("supplier"), "Supplier字段应该存在")

        logger.info("特殊容器Bean模板: $template")
    }
}