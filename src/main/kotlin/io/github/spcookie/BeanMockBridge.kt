package io.github.spcookie

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Bean mock engine for generating mock objects
 * Redesigned to leverage MockEngine's full capabilities
 *
 * @author spcookie
 * @since 1.2.0
 */
internal class BeanMockBridge(private val mockEngine: MockEngine, private val typeAdapter: TypeAdapter) {

    private val logger = LoggerFactory.getLogger(BeanMockBridge::class.java)
    private val beanIntrospect = BeanIntrospect()
    private val beanMockMapper = BeanMockMapper(typeAdapter)

    /**
     * Mock a bean object with optional configuration
     */
    fun <T : Any> mockBean(
        clazz: KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null,
        depth: Int? = null
    ): T {
        val mockBeanAnnotation = clazz.findAnnotation<Mock.Bean>()
        val config = BeanMockConfig(
            // Method parameters take precedence over annotation values
            // If method parameter is null, use annotation value; otherwise use method parameter
            includePrivate = includePrivate ?: (mockBeanAnnotation?.includePrivate ?: false),
            includeStatic = includeStatic ?: (mockBeanAnnotation?.includeStatic ?: false),
            includeTransient = includeTransient ?: (mockBeanAnnotation?.includeTransient ?: false),
            depth = depth ?: (mockBeanAnnotation?.depth ?: 3)
        )

        return mockBeanInternal(clazz, config)
    }

    /**
     * Internal bean mocking implementation using new architecture:
     * 1. Analyze Bean properties and convert to Map structure
     * 2. Use MockEngine to generate data
     * 3. Map generated data back to Bean object
     */
    private fun <T : Any> mockBeanInternal(clazz: KClass<T>, config: BeanMockConfig): T {
        try {
            // Step 1: Analyze Bean properties and convert to Map structure
            val propertyMap = beanIntrospect.analyzeBean(clazz, config)

            // Step 2: Use MockEngine to generate data
            @Suppress("UNCHECKED_CAST")
            val generatedData = mockEngine.generate(propertyMap) as Map<String, Any?>

            // Step 3: Map generated data back to Bean object
            val result = beanMockMapper.mapToBean(clazz, generatedData, config)

            return result
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot generate mock bean for ${clazz.simpleName}", e)
        }
    }
}