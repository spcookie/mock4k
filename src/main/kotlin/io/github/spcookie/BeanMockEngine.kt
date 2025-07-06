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
internal class BeanMockEngine(private val mockEngine: MockEngine, private val typeAdapter: TypeAdapter) {

    private val logger = LoggerFactory.getLogger(BeanMockEngine::class.java)
    private val propertyAnalyzer = BeanPropertyAnalyzer()
    private val resultMapper = BeanResultMapper(typeAdapter)

    /**
     * Mock a bean object with optional configuration
     */
    fun <T : Any> mockBean(
        clazz: KClass<T>,
        includePrivate: Boolean? = null,
        includeStatic: Boolean? = null,
        includeTransient: Boolean? = null
    ): T {
        val mockBeanAnnotation = clazz.findAnnotation<Mock.Bean>()
        val config = BeanPropertyAnalyzer.BeanMockConfig(
            // Method parameters take precedence over annotation values
            // If method parameter is null, use annotation value; otherwise use method parameter
            includePrivate = includePrivate ?: (mockBeanAnnotation?.includePrivate ?: false),
            includeStatic = includeStatic ?: (mockBeanAnnotation?.includeStatic ?: false),
            includeTransient = includeTransient ?: (mockBeanAnnotation?.includeTransient ?: false)
        )

        return mockBeanInternal(clazz, config)
    }

    /**
     * Internal bean mocking implementation using new architecture:
     * 1. Analyze Bean properties and convert to Map structure
     * 2. Use MockEngine to generate data
     * 3. Map generated data back to Bean object
     */
    private fun <T : Any> mockBeanInternal(clazz: KClass<T>, config: BeanPropertyAnalyzer.BeanMockConfig): T {
        try {
            // Step 1: Analyze Bean properties and convert to Map structure
            val propertyMap = propertyAnalyzer.analyzeBean(clazz, config)

            // Step 2: Use MockEngine to generate data
            @Suppress("UNCHECKED_CAST")
            val generatedData = mockEngine.generate(propertyMap) as Map<String, Any?>

            // Step 3: Map generated data back to Bean object
            val result = resultMapper.mapToBean(clazz, generatedData, config)

            return result
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot generate mock bean for ${clazz.simpleName}", e)
        }
    }
}