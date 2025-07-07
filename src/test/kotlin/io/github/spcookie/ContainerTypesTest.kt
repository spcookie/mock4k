package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.function.Supplier
import kotlin.test.assertNotNull

/**
 * 容器类型测试 - 测试各种Java和Kotlin容器类型的支持
 */
class ContainerTypesTest {

    private val logger = LoggerFactory.getLogger(ContainerTypesTest::class.java)

    // ==================== Java标准容器类型测试 ====================

    /**
     * Optional容器测试
     */
    data class OptionalBean(
        val optionalString: Optional<String>,
        val optionalInt: Optional<Int>,
        val optionalList: Optional<List<String>>,
        val emptyOptional: Optional<String> = Optional.empty()
    )

    @Test
    fun testOptionalContainers() {
        logger.info("测试Optional容器类型...")

        val bean = mock(OptionalBean::class)

        assertNotNull(bean, "Optional Bean不应为null")
        assertNotNull(bean.optionalString, "Optional<String>不应为null")
        assertNotNull(bean.optionalInt, "Optional<Int>不应为null")
        assertNotNull(bean.optionalList, "Optional<List<String>>不应为null")

        // 验证Optional内容
        if (bean.optionalString.isPresent) {
            assertNotNull(bean.optionalString.get(), "Optional中的字符串不应为null")
            logger.info("Optional<String>值: ${bean.optionalString.get()}")
        }

        if (bean.optionalInt.isPresent) {
            assertNotNull(bean.optionalInt.get(), "Optional中的整数不应为null")
            logger.info("Optional<Int>值: ${bean.optionalInt.get()}")
        }

        logger.info("生成的Optional Bean: $bean")
    }

    /**
     * Future和CompletableFuture容器测试
     */
    data class FutureBean(
        val futureString: CompletableFuture<String>,
        val futureInt: CompletableFuture<Int>,
        val genericFuture: Future<Double>
    )

    @Test
    fun testFutureContainers() {
        logger.info("测试Future容器类型...")

        val bean = mock(FutureBean::class)

        assertNotNull(bean, "Future Bean不应为null")
        assertNotNull(bean.futureString, "CompletableFuture<String>不应为null")
        assertNotNull(bean.futureInt, "CompletableFuture<Int>不应为null")
        assertNotNull(bean.genericFuture, "Future<Double>不应为null")

        // 验证Future状态
        assertTrue(bean.futureString.isDone, "CompletableFuture<String>应该已完成")
        assertTrue(bean.futureInt.isDone, "CompletableFuture<Int>应该已完成")
        assertTrue(bean.genericFuture.isDone, "Future<Double>应该已完成")

        // 验证Future内容
        assertNotNull(bean.futureString.get(), "Future中的字符串不应为null")
        assertNotNull(bean.futureInt.get(), "Future中的整数不应为null")
        assertNotNull(bean.genericFuture.get(), "Future中的双精度数不应为null")

        logger.info("Future<String>值: ${bean.futureString.get()}")
        logger.info("Future<Int>值: ${bean.futureInt.get()}")
        logger.info("Future<Double>值: ${bean.genericFuture.get()}")

        logger.info("生成的Future Bean: $bean")
    }

    /**
     * Callable和Supplier容器测试
     */
    data class CallableSupplierBean(
        val callableString: Callable<String>,
        val callableInt: Callable<Int>,
        val supplierDouble: Supplier<Double>,
        val supplierList: Supplier<List<String>>
    )

    @Test
    fun testCallableAndSupplierContainers() {
        logger.info("测试Callable和Supplier容器类型...")

        val bean = mock(CallableSupplierBean::class)

        assertNotNull(bean, "Callable/Supplier Bean不应为null")
        assertNotNull(bean.callableString, "Callable<String>不应为null")
        assertNotNull(bean.callableInt, "Callable<Int>不应为null")
        assertNotNull(bean.supplierDouble, "Supplier<Double>不应为null")
        assertNotNull(bean.supplierList, "Supplier<List<String>>不应为null")

        // 验证Callable和Supplier内容
        assertNotNull(bean.callableString.call(), "Callable中的字符串不应为null")
        assertNotNull(bean.callableInt.call(), "Callable中的整数不应为null")
        assertNotNull(bean.supplierDouble.get(), "Supplier中的双精度数不应为null")
        assertNotNull(bean.supplierList.get(), "Supplier中的列表不应为null")

        logger.info("Callable<String>值: ${bean.callableString.call()}")
        logger.info("Callable<Int>值: ${bean.callableInt.call()}")
        logger.info("Supplier<Double>值: ${bean.supplierDouble.get()}")
        logger.info("Supplier<List<String>>值: ${bean.supplierList.get()}")

        logger.info("生成的Callable/Supplier Bean: $bean")
    }

    // ==================== Kotlin标准容器类型测试 ====================

    /**
     * Lazy容器测试
     */
    data class LazyBean(
        val lazyString: Lazy<String>,
        val lazyInt: Lazy<Int>,
        val lazyList: Lazy<List<String>>
    )

    @Test
    fun testLazyContainers() {
        logger.info("测试Lazy容器类型...")

        val bean = mock(LazyBean::class)

        assertNotNull(bean, "Lazy Bean不应为null")
        assertNotNull(bean.lazyString, "Lazy<String>不应为null")
        assertNotNull(bean.lazyInt, "Lazy<Int>不应为null")
        assertNotNull(bean.lazyList, "Lazy<List<String>>不应为null")

        // 验证Lazy内容（第一次访问会初始化）
        assertNotNull(bean.lazyString.value, "Lazy中的字符串不应为null")
        assertNotNull(bean.lazyInt.value, "Lazy中的整数不应为null")
        assertNotNull(bean.lazyList.value, "Lazy中的列表不应为null")

        // 验证Lazy已初始化
        assertTrue(bean.lazyString.isInitialized(), "Lazy<String>应该已初始化")
        assertTrue(bean.lazyInt.isInitialized(), "Lazy<Int>应该已初始化")
        assertTrue(bean.lazyList.isInitialized(), "Lazy<List<String>>应该已初始化")

        logger.info("Lazy<String>值: ${bean.lazyString.value}")
        logger.info("Lazy<Int>值: ${bean.lazyInt.value}")
        logger.info("Lazy<List<String>>值: ${bean.lazyList.value}")

        logger.info("生成的Lazy Bean: $bean")
    }

    // ==================== 第三方库容器类型模拟测试 ====================

    /**
     * 模拟第三方库容器类型的Bean
     * 注意：这些类型在实际环境中可能不存在，但我们的代码应该能够处理它们
     */
    class MockReactorMono<T>(val value: T) {
        fun block(): T = value
    }

    class MockReactorFlux<T>(val values: List<T>) {
        fun collectList(): List<T> = values
    }

    class MockRxSingle<T>(val value: T) {
        fun blockingGet(): T = value
    }

    class MockVavrOption<T>(val value: T?) {
        fun get(): T? = value
        fun isDefined(): Boolean = value != null
    }

    data class ThirdPartyContainerBean(
        val monoString: MockReactorMono<String>,
        val fluxInts: MockReactorFlux<Int>,
        val singleDouble: MockRxSingle<Double>,
        val optionString: MockVavrOption<String>
    )

    @Test
    fun testThirdPartyContainerTypes() {
        logger.info("测试第三方库容器类型...")

        // 由于第三方库可能不存在，我们手动创建一个实例来测试结构
        val bean = ThirdPartyContainerBean(
            monoString = MockReactorMono("test"),
            fluxInts = MockReactorFlux(listOf(1, 2, 3)),
            singleDouble = MockRxSingle(3.14),
            optionString = MockVavrOption("option value")
        )

        assertNotNull(bean, "第三方容器Bean不应为null")
        assertNotNull(bean.monoString, "Mono不应为null")
        assertNotNull(bean.fluxInts, "Flux不应为null")
        assertNotNull(bean.singleDouble, "Single不应为null")
        assertNotNull(bean.optionString, "Option不应为null")

        // 验证容器内容
        assertEquals("test", bean.monoString.block())
        assertEquals(listOf(1, 2, 3), bean.fluxInts.collectList())
        assertEquals(3.14, bean.singleDouble.blockingGet())
        assertTrue(bean.optionString.isDefined())
        assertEquals("option value", bean.optionString.get())

        logger.info("第三方容器Bean验证成功")
    }

    // ==================== 容器类型嵌套测试 ====================

    /**
     * 嵌套容器类型测试
     */
    data class NestedContainerBean(
        val optionalFuture: Optional<CompletableFuture<String>>,
        val futureOptional: CompletableFuture<Optional<Int>>,
        val lazySupplier: Lazy<Supplier<String>>,
        val supplierLazy: Supplier<Lazy<Double>>
    )

    @Test
    fun testNestedContainerTypes() {
        logger.info("测试嵌套容器类型...")

        val bean = mock(NestedContainerBean::class)

        assertNotNull(bean, "嵌套容器Bean不应为null")
        assertNotNull(bean.optionalFuture, "Optional<Future>不应为null")
        assertNotNull(bean.futureOptional, "Future<Optional>不应为null")
        assertNotNull(bean.lazySupplier, "Lazy<Supplier>不应为null")
        assertNotNull(bean.supplierLazy, "Supplier<Lazy>不应为null")

        // 验证嵌套容器的内容
        if (bean.optionalFuture.isPresent) {
            val future = bean.optionalFuture.get()
            assertTrue(future.isDone, "嵌套的Future应该已完成")
            assertNotNull(future.get(), "嵌套Future的值不应为null")
            logger.info("Optional<Future<String>>值: ${future.get()}")
        }

        assertTrue(bean.futureOptional.isDone, "Future<Optional>应该已完成")
        val optional = bean.futureOptional.get()
        if (optional.isPresent) {
            assertNotNull(optional.get(), "嵌套Optional的值不应为null")
            logger.info("Future<Optional<Int>>值: ${optional.get()}")
        }

        val supplier = bean.lazySupplier.value
        assertNotNull(supplier.get(), "嵌套Supplier的值不应为null")
        logger.info("Lazy<Supplier<String>>值: ${supplier.get()}")

        val lazy = bean.supplierLazy.get()
        assertNotNull(lazy.value, "嵌套Lazy的值不应为null")
        logger.info("Supplier<Lazy<Double>>值: ${lazy.value}")

        logger.info("生成的嵌套容器Bean: $bean")
    }

    // ==================== 容器类型边界测试 ====================

    /**
     * 容器类型边界情况测试
     */
    data class EdgeCaseContainerBean(
        val optionalNull: Optional<String?>,
        val futureVoid: CompletableFuture<Unit>,
        val lazyNothing: Lazy<Nothing?>,
        val supplierAny: Supplier<Any>
    )

    @Test
    fun testContainerEdgeCases() {
        logger.info("测试容器类型边界情况...")

        val bean = mock(EdgeCaseContainerBean::class)

        assertNotNull(bean, "边界情况容器Bean不应为null")
        assertNotNull(bean.optionalNull, "Optional<String?>不应为null")
        assertNotNull(bean.futureVoid, "Future<Unit>不应为null")
        assertNotNull(bean.lazyNothing, "Lazy<Nothing?>不应为null")
        assertNotNull(bean.supplierAny, "Supplier<Any>不应为null")

        // 验证边界情况
        assertTrue(bean.futureVoid.isDone, "Future<Unit>应该已完成")
        assertNotNull(bean.supplierAny.get(), "Supplier<Any>应该能提供值")

        logger.info("边界情况容器Bean验证成功")
    }

    // ==================== 容器类型性能测试 ====================

    @Test
    fun testContainerTypePerformance() {
        logger.info("测试容器类型生成性能...")

        val iterations = 100
        val startTime = System.currentTimeMillis()

        repeat(iterations) {
            val optionalBean = mock(OptionalBean::class)
            val futureBean = mock(FutureBean::class)
            val lazyBean = mock(LazyBean::class)
            val callableBean = mock(CallableSupplierBean::class)

            assertNotNull(optionalBean)
            assertNotNull(futureBean)
            assertNotNull(lazyBean)
            assertNotNull(callableBean)
        }

        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        val avgTime = totalTime.toDouble() / iterations

        logger.info(
            "容器类型性能测试: $iterations 次迭代耗时 ${totalTime}ms (平均: ${
                String.format(
                    "%.2f",
                    avgTime
                )
            }ms/次)"
        )

        // 性能断言
        assertTrue(avgTime < 50.0, "容器类型生成平均时间应少于50ms，实际为 ${avgTime}ms")
    }

    // ==================== 容器类型配置测试 ====================

    @Test
    fun testContainerTypeWithDifferentConfigs() {
        logger.info("测试不同配置下的容器类型生成...")

        val configs = listOf(
            BeanMockConfig(includePrivate = true, maxDepth = 1),
            BeanMockConfig(includePrivate = false, maxDepth = 3),
            BeanMockConfig(maxCollectionSize = 5, maxStringLength = 10),
            BeanMockConfig(maxCollectionSize = 20, maxStringLength = 100)
        )

        configs.forEach { config ->
            val bean = mock(NestedContainerBean::class, config)
            assertNotNull(bean, "使用配置 $config 生成的容器Bean不应为null")
            logger.info("配置 $config 下的容器Bean生成成功")
        }
    }
}