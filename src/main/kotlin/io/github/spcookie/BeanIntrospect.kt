package io.github.spcookie

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * Analyzes Bean properties and converts them to Map structure for MockEngine
 *
 * @author spcookie
 * @since 1.2.0
 */
internal class BeanIntrospect(val containerAdapter: ContainerAdapter) {

    private val logger = LoggerFactory.getLogger(BeanIntrospect::class.java)

    /**
     * Analyze Bean class and convert to MapList structure for MockEngine
     */
    fun <T : Any> analyzeBean(clazz: KClass<T>, config: BeanMockConfig): Map<String, Any?> {
        return analyzeBean(clazz, config, 0)
    }

    /**
     * Analyze Bean class and convert to MapList structure for MockEngine with depth tracking
     */
    private fun <T : Any> analyzeBean(clazz: KClass<T>, config: BeanMockConfig, currentDepth: Int): Map<String, Any?> {

        // Check depth limit to avoid infinite recursion
        if (currentDepth >= config.depth) {
            return emptyMap()
        }

        val result = mutableMapOf<String, Any?>()

        // Get all properties based on configuration
        val properties = getEligibleProperties(clazz, config)

        for (property in properties) {
            try {
                // Try to get annotation from constructor parameter first, then from property
                val propertyAnnotation = getPropertyAnnotation(clazz, property)
                val propertyBeanAnnotation = getPropertyBeanAnnotation(clazz, property)

                // Skip if property is disabled
                if (propertyAnnotation?.enabled == false) {
                    continue
                }

                val key = buildPropertyKey(property, propertyAnnotation)
                val value = analyzePropertyType(
                    property.returnType,
                    propertyAnnotation,
                    propertyBeanAnnotation,
                    config,
                    currentDepth
                )

                result[key] = value
            } catch (e: Exception) {
                logger.warn("Failed to analyze property ${property.name}: ${e.message}")
            }
        }

        return result
    }

    /**
     * Get property annotation from constructor parameter or property itself
     */
    private fun <T : Any> getPropertyAnnotation(clazz: KClass<T>, property: KProperty<*>): Mock.Property? {
        // First try to get annotation from constructor parameter
        val constructor = clazz.primaryConstructor
        if (constructor != null) {
            val parameter = constructor.parameters.find { it.name == property.name }
            if (parameter != null) {
                val paramAnnotation = parameter.findAnnotation<Mock.Property>()
                if (paramAnnotation != null) {
                    return paramAnnotation
                }
            }
        }

        // Fallback to property annotation
        return property.findAnnotation<Mock.Property>()
    }

    /**
     * Get property-level @Mock.Bean annotation from constructor parameter or property itself
     * Property-level annotation has higher priority than class-level annotation
     */
    private fun <T : Any> getPropertyBeanAnnotation(clazz: KClass<T>, property: KProperty<*>): Mock.Bean? {
        // First try to get annotation from constructor parameter
        val constructor = clazz.primaryConstructor
        if (constructor != null) {
            val parameter = constructor.parameters.find { it.name == property.name }
            if (parameter != null) {
                val paramAnnotation = parameter.findAnnotation<Mock.Bean>()
                if (paramAnnotation != null) {
                    return paramAnnotation
                }
            }
        }

        // Fallback to property annotation
        return property.findAnnotation<Mock.Bean>()
    }

    /**
     * Build property key with rules from annotation
     * Rule priority: step > count > range and decimal > range > decimal
     */
    private fun buildPropertyKey(property: KProperty<*>, annotation: Mock.Property?): String {
        val baseName = property.name
        val rule = annotation?.rule

        if (rule == null) {
            return baseName
        }

        // Priority 1: step rule (highest priority)
        if (rule.step >= 0) {
            return "$baseName|+${rule.step}"
        }

        // Priority 2: count rule
        if (rule.count >= 0) {
            val countPart = rule.count.toString()
            val decimalPart = getDecimalPart(rule)
            return if (decimalPart != null) {
                "$baseName|$countPart.$decimalPart"
            } else {
                "$baseName|$countPart"
            }
        }

        // Priority 3: range and decimal
        if (rule.min >= 0 && rule.max >= 0) {
            val rangePart = "${rule.min}-${rule.max}"
            val decimalPart = getDecimalPart(rule)
            return if (decimalPart != null) {
                "$baseName|$rangePart.$decimalPart"
            } else {
                "$baseName|$rangePart"
            }
        }

        // Priority 4: only decimal
        val decimalPart = getDecimalPart(rule)
        if (decimalPart != null) {
            return "$baseName|1.$decimalPart"
        }

        return baseName
    }

    /**
     * Extract decimal part from rule
     */
    private fun getDecimalPart(rule: Mock.Rule): String? {
        return when {
            rule.dmin >= 0 && rule.dmax >= 0 -> "${rule.dmin}-${rule.dmax}"
            rule.dcount >= 0 -> rule.dcount.toString()
            else -> null
        }
    }

    /**
     * Analyze property type and convert to appropriate placeholder or structure
     */
    internal fun analyzePropertyType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean? = null,
        config: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Any? {
        val kClass = type.classifier as? KClass<*> ?: return "@string"

        // Check for custom placeholder first
        val placeholder = annotation?.placeholder
        if (placeholder != null && placeholder.value.isNotEmpty() && isBasicType(kClass)) {
            return placeholder.value
        }

        return when {
            // Category 1: Basic types
            isBasicType(kClass) -> getBasicTypePlaceholder(kClass)

            // Category 2: Collections
            isCollectionType(kClass) -> analyzeCollectionType(
                type,
                annotation,
                propertyBeanAnnotation,
                config,
                currentDepth
            )

            // Advanced: Container objects
            isContainerType(kClass, containerAdapter) -> analyzeContainerType(
                type,
                annotation,
                propertyBeanAnnotation,
                config,
                currentDepth
            )

            // Category 3: Custom objects
            isCustomClass(kClass, containerAdapter) -> analyzeCustomObject(
                kClass,
                propertyBeanAnnotation,
                config,
                currentDepth
            )

            else -> "@string"
        }
    }

    /**
     * Get placeholder for basic types
     */
    private fun getBasicTypePlaceholder(kClass: KClass<*>): String {
        return when (kClass) {
            String::class -> "@string"
            Int::class, Integer::class -> "@integer"
            Long::class, java.lang.Long::class -> "@long"
            Float::class, java.lang.Float::class -> "@float"
            Double::class, java.lang.Double::class -> "@float"
            Boolean::class, java.lang.Boolean::class -> "@boolean"
            Char::class, Character::class -> "@character"
            Byte::class, java.lang.Byte::class -> "@integer"
            Short::class, java.lang.Short::class -> "@integer"
            BigDecimal::class -> "@float"
            BigInteger::class -> "@integer"
            UUID::class -> "@uuid"
            Date::class -> "@date"
            java.sql.Date::class -> "@date"
            java.sql.Time::class -> "@time"
            java.sql.Timestamp::class -> "@datetime"
            java.time.LocalDate::class -> "@date"
            java.time.LocalTime::class -> "@time"
            java.time.LocalDateTime::class -> "@datetime"
            java.time.ZonedDateTime::class -> "@datetime"
            java.time.OffsetDateTime::class -> "@datetime"
            java.time.Instant::class -> "@datetime"
            else -> "@string"
        }
    }


    /**
     * Analyze collection type and return appropriate structure
     */
    private fun analyzeCollectionType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean? = null,
        config: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Any {
        val kClass = type.classifier as KClass<*>
        val length = annotation?.length?.value ?: 1
        val fill = annotation?.length?.fill ?: Mock.Fill.RANDOM

        return when {
            List::class.java.isAssignableFrom(kClass.java)
                    || kClass.java.isArray
                    || Set::class.java.isAssignableFrom(kClass.java) -> {
                val elementType = type.arguments.firstOrNull()?.type

                when (fill) {
                    Mock.Fill.REPEAT -> {
                        // REPEAT: use the same element for all positions
                        val elementValue = if (elementType != null) {
                            analyzePropertyType(elementType, null, propertyBeanAnnotation, config, currentDepth)
                        } else {
                            "@string"
                        }
                        List(length) { elementValue }
                    }

                    Mock.Fill.RANDOM -> {
                        // RANDOM: generate different elements for each position
                        List(length) {
                            if (elementType != null) {
                                analyzePropertyType(elementType, null, propertyBeanAnnotation, config, currentDepth)
                            } else {
                                "@string"
                            }
                        }
                    }
                }
            }

            Map::class.java.isAssignableFrom(kClass.java) -> {
                val keyType = type.arguments.getOrNull(0)?.type
                val valueType = type.arguments.getOrNull(1)?.type

                // Generate random key-value pairs based on length
                (1..length).associate {
                    val keyValue = if (keyType != null) {
                        val keyClass = keyType.classifier as? KClass<*>
                        if (keyClass == String::class) {
                            "@string"
                        } else {
                            analyzePropertyType(keyType, null, propertyBeanAnnotation, config, currentDepth).toString()
                        }
                    } else {
                        "@string"
                    }

                    val valueValue = if (valueType != null) {
                        analyzePropertyType(valueType, null, propertyBeanAnnotation, config, currentDepth)
                    } else {
                        "@string"
                    }

                    keyValue to valueValue
                }
            }

            else -> emptyList<Any>()
        }
    }

    /**
     * Analyze custom object recursively
     */
    private fun analyzeCustomObject(
        kClass: KClass<*>,
        propertyBeanAnnotation: Mock.Bean? = null,
        parentConfig: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Map<String, Any?> {
        return try {
            // Property-level @Mock.Bean annotation has higher priority than class-level annotation
            val effectiveBeanAnnotation = propertyBeanAnnotation ?: kClass.findAnnotation<Mock.Bean>()
            val config = if (effectiveBeanAnnotation != null) {
                // Use annotation configuration if present (property-level takes precedence)
                BeanMockConfig(
                    includePrivate = effectiveBeanAnnotation.includePrivate,
                    includeStatic = effectiveBeanAnnotation.includeStatic,
                    includeTransient = effectiveBeanAnnotation.includeTransient,
                    depth = effectiveBeanAnnotation.depth
                )
            } else {
                // Use parent configuration if no annotation
                parentConfig
            }
            analyzeBean(kClass, config, currentDepth + 1)
        } catch (e: Exception) {
            logger.warn("Failed to analyze custom object ${kClass.simpleName}: ${e.message}")
            mapOf("value" to "@string")
        }
    }

    /**
     * Analyze container types using ContainerAdapter
     */
    private fun analyzeContainerType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean? = null,
        config: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Any? {
        return containerAdapter.analyzeContainerType(
            type,
            annotation,
            propertyBeanAnnotation,
            config,
            currentDepth
        ) { wrappedType, ann, beanAnn, cfg, depth ->
            analyzeWrappedType(wrappedType, ann, beanAnn, cfg, depth)
        }
    }

    /**
     * Analyze wrapped type with fallback to default value
     */
    private fun analyzeWrappedType(
        wrappedType: KType?,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean?,
        config: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Any? {
        return if (wrappedType != null) {
            analyzePropertyType(wrappedType, annotation, propertyBeanAnnotation, config, currentDepth)
        } else {
            "@string"
        }
    }
}