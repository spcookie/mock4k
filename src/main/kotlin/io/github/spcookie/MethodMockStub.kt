package io.github.spcookie

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.implementation.bind.annotation.AllArguments
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.matcher.ElementMatchers.*
import java.lang.invoke.MethodType
import kotlin.reflect.KClass

/**
 * Internal utility object for creating mock stub objects using ByteBuddy
 *
 * This object provides functionality to create dynamic subclasses that intercept
 * method calls and return mock values. It uses caching to improve performance
 * by reusing previously generated stub classes.
 */
internal object MethodMockStub {

    /**
     * Cache for storing generated stub classes to avoid regeneration
     *
     * Maps original class types to their corresponding generated stub class types.
     * This cache improves performance by reusing previously created dynamic classes.
     */
    private val stubCache: MutableMap<KClass<*>, KClass<out Any>> = mutableMapOf()

    /**
     * Create or retrieve a cached stub class for the given type
     *
     * This method uses ByteBuddy to create a dynamic subclass that intercepts
     * all public non-void/non-Unit methods. The generated class is cached for
     * future use to improve performance.
     *
     * @param clazz The class to create a stub for
     * @return The generated stub class that can be instantiated
     * @throws IllegalArgumentException if the class cannot be subclassed
     */
    @JvmStatic
    fun <T : Any> make(clazz: KClass<T>): KClass<out T> {
        // Double-checked locking pattern for thread-safe cache access
        if (!stubCache.containsKey(clazz)) {
            synchronized(MethodMockStub) {
                if (!stubCache.containsKey(clazz)) {
                    try {
                        // Create dynamic subclass using ByteBuddy
                        val dynamicType = ByteBuddy()
                            .subclass(clazz.java)
                            .method(
                                // Intercept public methods that return non-void/non-Unit values
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
     * Interceptor class for ByteBuddy method delegation
     *
     * This class handles the interception of method calls and generates
     * appropriate mock return values based on the method's return type.
     */
    class MockInterceptor {

        companion object {
            /**
             * Intercepts method calls and returns mock values
             *
             * This method is called by ByteBuddy whenever an intercepted method is invoked.
             * It analyzes the method's return type and generates appropriate mock data.
             *
             * @param methodType The method type information including return type
             * @param args The method arguments (currently unused but required by ByteBuddy)
             * @return Mock value appropriate for the method's return type, or null for void/Unit
             */
            @JvmStatic
            @RuntimeType
            fun intercept(
                @Origin methodType: MethodType,
                @AllArguments args: Array<Any?>
            ): Any? {
                val returnType = methodType.returnType()

                // Handle void and Unit returns
                if (returnType == Void.TYPE || returnType == Unit::class.java) {
                    return null
                }

                return try {
                    // Convert Java Class to Kotlin KClass and generate mock
                    val kotlinClass = returnType.kotlin
                    when (kotlinClass) {
                        // Handle primitive and wrapper types with appropriate mock values
                        String::class -> Mocks.Random.string()
                        Int::class, Integer::class -> Mocks.Random.integer()
                        Long::class, java.lang.Long::class -> Mocks.Random.long()
                        Float::class, java.lang.Float::class -> Mocks.Random.float()
                        Double::class, java.lang.Double::class -> Mocks.Random.float()
                        Boolean::class, java.lang.Boolean::class -> Mocks.Random.boolean()
                        Char::class, Character::class -> Mocks.Random.string(1).first()
                        Byte::class, java.lang.Byte::class -> Mocks.Random.integer(0, 255).toByte()
                        Short::class, java.lang.Short::class -> Mocks.Random.integer(0, 65535).toShort()
                        else -> {
                            try {
                                // Try to generate a mock bean for complex types
                                mock(kotlinClass)
                            } catch (_: Exception) {
                                // If bean generation fails, try to create a simple instance
                                try {
                                    returnType.getDeclaredConstructor().newInstance()
                                } catch (_: Exception) {
                                    null
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Return null if all else fails
                    null
                }
            }

        }
    }

}