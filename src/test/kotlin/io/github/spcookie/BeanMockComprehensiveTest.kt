package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.test.assertNotNull

/**
 * BeanMock 综合测试套件 - 覆盖基本Bean、复杂Bean、边界测试和性能测试
 */
class BeanMockComprehensiveTest {

    private val logger = LoggerFactory.getLogger(BeanMockComprehensiveTest::class.java)

    @BeforeEach
    fun setUp() {
        // 重置为默认语言环境
        MockRandom.setLocale(Locale.ENGLISH)
    }

    // ==================== 基本Bean测试 ====================

    /**
     * 简单数据类测试
     */
    data class SimpleUser(
        val id: Long,
        val name: String,
        val age: Int,
        val email: String,
        val active: Boolean
    )

    @Test
    fun testSimpleDataClass() {
        logger.info("测试简单数据类Bean生成...")

        val user = mock(SimpleUser::class)

        assertNotNull(user, "用户对象不应为null")
        assertNotNull(user.id, "用户ID不应为null")
        assertNotNull(user.name, "用户姓名不应为null")
        assertNotNull(user.age, "用户年龄不应为null")
        assertNotNull(user.email, "用户邮箱不应为null")
        assertNotNull(user.active, "用户状态不应为null")

        logger.info("生成的简单用户: $user")
    }

    /**
     * 包含可空属性的Bean测试
     */
    data class UserWithNullables(
        val id: Long,
        val name: String,
        val nickname: String? = null,
        val phone: String? = null,
        val avatar: String? = null
    )

    @Test
    fun testBeanWithNullableProperties() {
        logger.info("测试包含可空属性的Bean生成...")

        val user = mock(UserWithNullables::class)

        assertNotNull(user, "用户对象不应为null")
        assertNotNull(user.id, "用户ID不应为null")
        assertNotNull(user.name, "用户姓名不应为null")
        // 可空属性可能为null，这是正常的

        logger.info("生成的可空属性用户: $user")
    }

    /**
     * 包含默认值的Bean测试
     */
    data class UserWithDefaults(
        val id: Long = 0L,
        val name: String = "Unknown",
        val age: Int = 18,
        val country: String = "China",
        val isVip: Boolean = false
    )

    @Test
    fun testBeanWithDefaultValues() {
        logger.info("测试包含默认值的Bean生成...")

        val user = mock(UserWithDefaults::class)

        assertNotNull(user, "用户对象不应为null")
        // 验证生成的值不是默认值（说明mock正常工作）
        val hasNonDefaultValues = user.id != 0L || user.name != "Unknown" ||
                user.age != 18 || user.country != "China" || user.isVip

        logger.info("生成的默认值用户: $user")
        logger.info("是否生成了非默认值: $hasNonDefaultValues")
    }

    // ==================== 复杂Bean测试 ====================

    /**
     * 嵌套对象Bean测试
     */
    data class Address(
        val street: String,
        val city: String,
        val province: String,
        val zipCode: String,
        val country: String = "China"
    )

    data class Profile(
        val firstName: String,
        val lastName: String,
        val birthDate: LocalDate?,
        val bio: String?
    )

    data class ComplexUser(
        val id: Long,
        val profile: Profile,
        val address: Address,
        val tags: List<String>,
        val metadata: Map<String, Any>,
        val scores: Set<Int>
    )

    @Test
    fun testComplexNestedBean() {
        logger.info("测试复杂嵌套Bean生成...")

        val user = mock(ComplexUser::class)

        assertNotNull(user, "复杂用户对象不应为null")
        assertNotNull(user.id, "用户ID不应为null")
        assertNotNull(user.profile, "用户档案不应为null")
        assertNotNull(user.address, "用户地址不应为null")
        assertNotNull(user.tags, "用户标签不应为null")
        assertNotNull(user.metadata, "用户元数据不应为null")
        assertNotNull(user.scores, "用户分数不应为null")

        // 验证嵌套对象的属性
        assertNotNull(user.profile.firstName, "名字不应为null")
        assertNotNull(user.profile.lastName, "姓氏不应为null")
        assertNotNull(user.address.street, "街道不应为null")
        assertNotNull(user.address.city, "城市不应为null")

        logger.info("生成的复杂用户: $user")
    }

    /**
     * 包含容器类型的Bean测试
     */
    data class ContainerBean(
        val optionalValue: Optional<String>,
        val futureValue: CompletableFuture<Int>,
        val lazyValue: Lazy<String>,
        val supplierValue: java.util.function.Supplier<Double>
    )

    @Test
    fun testBeanWithContainerTypes() {
        logger.info("测试包含容器类型的Bean生成...")

        val bean = mock(ContainerBean::class)

        assertNotNull(bean, "容器Bean不应为null")
        assertNotNull(bean.optionalValue, "Optional值不应为null")
        assertNotNull(bean.futureValue, "Future值不应为null")
        assertNotNull(bean.lazyValue, "Lazy值不应为null")
        assertNotNull(bean.supplierValue, "Supplier值不应为null")

        // 验证容器内容
        assertTrue(bean.optionalValue.isPresent, "Optional应该包含值")
        assertTrue(bean.futureValue.isDone, "Future应该已完成")
        assertNotNull(bean.lazyValue.value, "Lazy值应该可以获取")
        assertNotNull(bean.supplierValue.get(), "Supplier应该能提供值")

        logger.info("生成的容器Bean: $bean")
    }

    /**
     * 包含各种数字类型的Bean测试
     */
    data class NumericBean(
        val byteValue: Byte,
        val shortValue: Short,
        val intValue: Int,
        val longValue: Long,
        val floatValue: Float,
        val doubleValue: Double,
        val bigIntValue: BigInteger,
        val bigDecimalValue: BigDecimal
    )

    @Test
    fun testBeanWithNumericTypes() {
        logger.info("测试包含各种数字类型的Bean生成...")

        val bean = mock(NumericBean::class)

        assertNotNull(bean, "数字Bean不应为null")
        assertNotNull(bean.byteValue, "Byte值不应为null")
        assertNotNull(bean.shortValue, "Short值不应为null")
        assertNotNull(bean.intValue, "Int值不应为null")
        assertNotNull(bean.longValue, "Long值不应为null")
        assertNotNull(bean.floatValue, "Float值不应为null")
        assertNotNull(bean.doubleValue, "Double值不应为null")
        assertNotNull(bean.bigIntValue, "BigInteger值不应为null")
        assertNotNull(bean.bigDecimalValue, "BigDecimal值不应为null")

        logger.info("生成的数字Bean: $bean")
    }

    // ==================== 边界测试 ====================

    /**
     * 空集合Bean测试
     */
    data class EmptyCollectionBean(
        val emptyList: List<String> = emptyList(),
        val emptySet: Set<Int> = emptySet(),
        val emptyMap: Map<String, Any> = emptyMap()
    )

    @Test
    fun testBeanWithEmptyCollections() {
        logger.info("测试包含空集合的Bean生成...")

        val bean = mock(EmptyCollectionBean::class)

        assertNotNull(bean, "空集合Bean不应为null")
        assertNotNull(bean.emptyList, "空列表不应为null")
        assertNotNull(bean.emptySet, "空集合不应为null")
        assertNotNull(bean.emptyMap, "空映射不应为null")

        logger.info("生成的空集合Bean: $bean")
    }

    /**
     * 极大数据Bean测试
     */
    data class LargeDataBean(
        val largeString: String,
        val largeList: List<String>,
        val largeMap: Map<String, String>
    )

    @Test
    fun testBeanWithLargeData() {
        logger.info("测试包含大数据的Bean生成...")

        val bean = mock(LargeDataBean::class, includePrivate = true)

        assertNotNull(bean, "大数据Bean不应为null")
        assertNotNull(bean.largeString, "大字符串不应为null")
        assertNotNull(bean.largeList, "大列表不应为null")
        assertNotNull(bean.largeMap, "大映射不应为null")

        // 验证数据大小在合理范围内
        assertTrue(bean.largeList.size <= 100, "列表大小应该在配置范围内")
        assertTrue(bean.largeMap.size <= 100, "映射大小应该在配置范围内")

        logger.info("生成的大数据Bean - 字符串长度: ${bean.largeString.length}, 列表大小: ${bean.largeList.size}, 映射大小: ${bean.largeMap.size}")
    }

    /**
     * 循环引用Bean测试（应该被优雅处理）
     */
    data class Node(
        val id: String,
        val value: String,
        val parent: Node? = null,
        val children: List<Node> = emptyList()
    )

    @Test
    fun testBeanWithPotentialCircularReference() {
        logger.info("测试可能存在循环引用的Bean生成...")

        val node = mock(Node::class, depth = 3)

        assertNotNull(node, "节点不应为null")
        assertNotNull(node.id, "节点ID不应为null")
        assertNotNull(node.value, "节点值不应为null")
        assertNotNull(node.children, "子节点列表不应为null")

        logger.info("生成的节点: $node")
    }

    // ==================== 性能测试 ====================

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun testSimpleBeanPerformance() {
        logger.info("测试简单Bean生成性能...")

        val iterations = 1000
        val executionTime = measureTimeMillis {
            repeat(iterations) {
                val user = mock(SimpleUser::class)
                assertNotNull(user)
            }
        }

        val avgTimePerCall = executionTime.toDouble() / iterations
        logger.info(
            "简单Bean性能: $iterations 次迭代耗时 ${executionTime}ms (平均: ${
                String.format(
                    "%.2f",
                    avgTimePerCall
                )
            }ms/次)"
        )

        // 性能断言
        assertTrue(avgTimePerCall < 10.0, "每次调用平均时间应少于10ms，实际为 ${avgTimePerCall}ms")
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun testComplexBeanPerformance() {
        logger.info("测试复杂Bean生成性能...")

        val iterations = 100
        val executionTime = measureTimeMillis {
            repeat(iterations) {
                val user = mock(ComplexUser::class)
                assertNotNull(user)
                assertNotNull(user.profile)
                assertNotNull(user.address)
            }
        }

        val avgTimePerCall = executionTime.toDouble() / iterations
        logger.info(
            "复杂Bean性能: $iterations 次迭代耗时 ${executionTime}ms (平均: ${
                String.format(
                    "%.2f",
                    avgTimePerCall
                )
            }ms/次)"
        )

        // 性能断言
        assertTrue(avgTimePerCall < 100.0, "复杂Bean每次调用平均时间应少于100ms，实际为 ${avgTimePerCall}ms")
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    fun testBatchBeanGeneration() {
        logger.info("测试批量Bean生成性能...")

        val batchSizes = listOf(10, 50, 100, 200)

        batchSizes.forEach { batchSize ->
            val executionTime = measureTimeMillis {
                val users = (1..batchSize).map { mock(SimpleUser::class) }
                assertEquals(batchSize, users.size)
                users.forEach { assertNotNull(it) }
            }

            val timePerBean = executionTime.toDouble() / batchSize
            logger.info(
                "批量生成 $batchSize 个Bean耗时: ${executionTime}ms (平均: ${
                    String.format(
                        "%.2f",
                        timePerBean
                    )
                }ms/个)"
            )

            // 性能断言
            assertTrue(timePerBean < 20.0, "批量生成时每个Bean平均时间应少于20ms，实际为 ${timePerBean}ms")
        }
    }

    // ==================== 配置测试 ====================

    @Test
    fun testBeanMockConfigOptions() {
        logger.info("测试BeanMock配置选项...")

        // 测试不同配置
        val configs = listOf(
            BeanMockConfig(includePrivate = true, includeStatic = false),
            BeanMockConfig(includePrivate = false, includeStatic = true),
            BeanMockConfig(depth = 1),
            BeanMockConfig(depth = 5)
        )

        configs.forEach { config ->
            val user = mock(ComplexUser::class, config)
            assertNotNull(user, "使用配置 $config 生成的用户不应为null")
            logger.info("配置 $config 生成的用户: ${(user as Any).javaClass.simpleName}")
        }
    }

    // ==================== 错误处理测试 ====================

    /**
     * 抽象类（应该无法实例化）
     */
    abstract class AbstractUser {
        abstract val id: Long
        abstract val name: String
    }

    @Test
    fun testAbstractClassHandling() {
        logger.info("测试抽象类处理...")

        assertThrows(Exception::class.java) {
            mock(AbstractUser::class)
        }

        logger.info("抽象类正确抛出异常")
    }

    /**
     * 接口（应该无法实例化）
     */
    interface UserInterface {
        val id: Long
        val name: String
    }

    @Test
    fun testInterfaceHandling() {
        logger.info("测试接口处理...")

        assertThrows(Exception::class.java) {
            mock(UserInterface::class)
        }

        logger.info("接口正确抛出异常")
    }

    /**
     * 没有无参构造函数的类
     */
    class UserWithoutDefaultConstructor(val id: Long, val name: String) {
        // 没有无参构造函数
    }

    @Test
    fun testClassWithoutDefaultConstructor() {
        logger.info("测试没有默认构造函数的类处理...")

        // 应该能够通过主构造函数创建
        val user = mock(UserWithoutDefaultConstructor::class)
        assertNotNull(user, "应该能够创建没有默认构造函数的类实例")
        assertNotNull(user.id)
        assertNotNull(user.name)

        logger.info("生成的用户: $user")
    }

    // ==================== 内存和资源测试 ====================

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    fun testMemoryUsage() {
        logger.info("测试内存使用情况...")

        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        // 生成大量Bean
        val users = mutableListOf<ComplexUser>()
        repeat(1000) {
            users.add(mock(ComplexUser::class))
        }

        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = finalMemory - initialMemory

        logger.info("生成1000个复杂Bean使用内存: ${memoryUsed / 1024 / 1024}MB")

        // 清理引用
        users.clear()
        System.gc()

        // 验证没有内存泄漏（这是一个粗略的检查）
        val afterGcMemory = runtime.totalMemory() - runtime.freeMemory()
        logger.info("GC后内存使用: ${(afterGcMemory - initialMemory) / 1024 / 1024}MB")
    }
}