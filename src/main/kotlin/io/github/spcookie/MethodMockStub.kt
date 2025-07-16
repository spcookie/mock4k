package io.github.spcookie

import net.bytebuddy.ByteBuddy
import net.bytebuddy.NamingStrategy
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.matcher.ElementMatchers.*
import java.lang.invoke.MethodType
import kotlin.reflect.KClass

/**
 * 使用 ByteBuddy 创建模拟Stub对象的内部实用工具对象
 *
 * 此对象提供创建动态子类的功能，这些子类可以拦截
 * 方法调用并返回模拟值。它使用缓存通过重用
 * 先前生成的Stub类来提高性能。
 */
internal object MethodMockStub {

    /**
     * 用于存储生成的Stub类的缓存，以避免重新生成
     *
     * 将原始类类型映射到其对应的生成的Stub类类型。
     * 此缓存通过重用先前创建的动态类来提高性能。
     */
    private val stubCache: MutableMap<KClass<*>, KClass<out Any>> = mutableMapOf()

    /**
     * 为给定类型创建或检索缓存的Stub类
     *
     * 此方法使用 ByteBuddy 创建一个动态子类，该子类拦截
     * 所有公共的非 void/非 Unit 方法。生成的类将被缓存以供
     * 将来使用，以提高性能。
     *
     * @param clazz 要为其创建Stub的类
     * @return 可实例化的生成的Stub类
     * @throws IllegalArgumentException 如果该类无法被子类化
     */
    @JvmStatic
    fun <T : Any> make(clazz: KClass<T>): KClass<out T> {
        // 用于线程安全缓存访问的双重检查锁定模式
        if (!stubCache.containsKey(clazz)) {
            synchronized(MethodMockStub) {
                if (!stubCache.containsKey(clazz)) {
                    try {
                        // 使用 ByteBuddy 创建动态子类
                        val dynamicType = ByteBuddy()
                            .with(NamingStrategy.SuffixingRandom("Mock"))
                            .subclass(clazz.java)
                            .method(
                                // 拦截返回非 void/非 Unit 值的公共方法
                                isPublic<MethodDescription>()
                                    .and(not(returns(Void::class.java)))
                                    .and(not(returns(Unit::class.java)))
                            )
                            .intercept(MethodDelegation.to(MockInterceptor::class.java))
                            .make()
                            .load(clazz.java.classLoader)
                            .loaded
                            .kotlin
                        stubCache[clazz] = dynamicType
                    } catch (e: Exception) {
                        throw IllegalArgumentException(
                            "Cannot create mock stub for class ${clazz.simpleName}: ${e.message}",
                            e
                        )
                    }
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        return stubCache[clazz] as KClass<out T>
    }

    /**
     * 用于 ByteBuddy 方法委托的拦截器类
     *
     * 此类处理方法调用的拦截，并根据
     * 方法的返回类型生成适当的模拟返回值。
     */
    class MockInterceptor {

        companion object {
            /**
             * 拦截方法调用并返回模拟值
             *
             * 每当调用被拦截的方法时，ByteBuddy 都会调用此方法。
             * 它会分析方法的返回类型并生成适当的模拟数据。
             *
             * @param methodType 包括返回类型在内的方法类型信息
             * @param args 方法参数（当前未使用，但 ByteBuddy 需要）
             * @return 适合方法返回类型的模拟值，对于 void/Unit 则为 null
             */
            @JvmStatic
            @RuntimeType
            fun intercept(
                @Origin methodType: MethodType,
                @AllArguments args: Array<Any?>
            ): Any? {
                val returnType = methodType.returnType()
                // 处理 void 和 Unit 返回
                if (returnType == Void.TYPE || returnType == Unit::class.java) {
                    return null
                }
                // 将 Java 类转换为 Kotlin KClass 并生成模拟
                val kotlinClass = returnType.kotlin
                // 尝试为复杂类型生成模拟 bean
                return mock(kotlinClass)
            }

        }
    }

}