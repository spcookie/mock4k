package io.github.spcookie

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Analyzes Bean properties and converts them to Map structure for MockEngine
 *
 * @author spcookie
 * @since 1.2.0
 */
internal class BeanIntrospect {

    private val logger = LoggerFactory.getLogger(BeanIntrospect::class.java)

    /**
     * Analyze Bean class and convert to MapList structure for MockEngine
     */
    fun <T : Any> analyzeBean(clazz: KClass<T>, config: BeanMockConfig): Map<String, Any?> {
        logger.debug("Analyzing bean class: ${clazz.simpleName}")

        val result = mutableMapOf<String, Any?>()

        // Get all properties based on configuration
        val properties = getFilteredProperties(clazz, config)

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
                val value = analyzePropertyType(property.returnType, propertyAnnotation, propertyBeanAnnotation)

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
    private fun <T : Any> getPropertyAnnotation(clazz: KClass<T>, property: KProperty1<T, *>): Mock.Property? {
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
    private fun <T : Any> getPropertyBeanAnnotation(clazz: KClass<T>, property: KProperty1<T, *>): Mock.Bean? {
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
     * Get filtered properties based on configuration
     */
    private fun <T : Any> getFilteredProperties(
        clazz: KClass<T>,
        config: BeanMockConfig
    ): Collection<KProperty1<T, *>> {
        return clazz.memberProperties.filter { property ->
            val javaField = property.javaField

            // For properties without backing field (like computed properties), include them by default
            if (javaField == null) {
                return@filter true
            }

            // Check private access
            if (!config.includePrivate && !java.lang.reflect.Modifier.isPublic(javaField.modifiers)) {
                return@filter false
            }

            // Check static access
            if (!config.includeStatic && java.lang.reflect.Modifier.isStatic(javaField.modifiers)) {
                return@filter false
            }

            // Check transient access
            if (!config.includeTransient && java.lang.reflect.Modifier.isTransient(javaField.modifiers)) {
                return@filter false
            }

            true
        }
    }

    /**
     * Build property key with rules from annotation
     * Rule priority: step > count > range and decimal > range > decimal
     */
    private fun buildPropertyKey(property: KProperty1<*, *>, annotation: Mock.Property?): String {
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
    private fun analyzePropertyType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean? = null
    ): Any? {
        val kClass = type.classifier as? KClass<*> ?: return "@STRING"

        // Check for custom placeholder first
        val placeholder = annotation?.placeholder
        if (placeholder != null && placeholder.value.isNotEmpty() && isBasicType(kClass)) {
            return placeholder.value
        }

        return when {
            // Category 1: Basic types
            isBasicType(kClass) -> getBasicTypePlaceholder(kClass)

            // Category 2: Collections
            isCollectionType(kClass) -> analyzeCollectionType(type, annotation, propertyBeanAnnotation)

            // Advanced: Container objects
            isContainerType(kClass) -> analyzeContainerType(type, annotation, propertyBeanAnnotation)

            // Category 3: Custom objects
            isCustomClass(kClass) -> analyzeCustomObject(kClass, propertyBeanAnnotation)

            else -> "@STRING"
        }
    }




    /**
     * Get placeholder for basic types
     */
    private fun getBasicTypePlaceholder(kClass: KClass<*>): String {
        return when (kClass) {
            String::class -> "@STRING"
            Int::class, Integer::class -> "@INTEGER"
            Long::class, java.lang.Long::class -> "@LONG"
            Float::class, java.lang.Float::class -> "@FLOAT"
            Double::class, java.lang.Double::class -> "@DOUBLE"
            Boolean::class, java.lang.Boolean::class -> "@BOOLEAN"
            Char::class, Character::class -> "@CHARACTER"
            Byte::class, java.lang.Byte::class -> "@INTEGER"
            Short::class, java.lang.Short::class -> "@INTEGER"
            BigDecimal::class -> "@FLOAT"
            BigInteger::class -> "@INTEGER"
            Date::class -> "@DATE"
            java.sql.Date::class -> "@DATE"
            java.sql.Time::class -> "@TIME"
            java.sql.Timestamp::class -> "@DATETIME"
            java.time.LocalDate::class -> "@DATE"
            java.time.LocalTime::class -> "@TIME"
            java.time.LocalDateTime::class -> "@DATETIME"
            java.time.ZonedDateTime::class -> "@DATETIME"
            java.time.OffsetDateTime::class -> "@DATETIME"
            java.time.Instant::class -> "@DATETIME"
            Pair::class -> "@STRING"
            else -> "@STRING"
        }
    }


    /**
     * Analyze collection type and return appropriate structure
     */
    private fun analyzeCollectionType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean? = null
    ): Any {
        val kClass = type.classifier as KClass<*>
        val length = annotation?.length?.value ?: 1
        val fill = annotation?.length?.fill ?: FillStrategy.RANDOM

        return when {
            List::class.java.isAssignableFrom(kClass.java)
                    || kClass.java.isArray
                    || Set::class.java.isAssignableFrom(kClass.java) -> {
                val elementType = type.arguments.firstOrNull()?.type

                when (fill) {
                    FillStrategy.REPEAT -> {
                        // REPEAT: use the same element for all positions
                        val elementValue = if (elementType != null) {
                            analyzePropertyType(elementType, null, propertyBeanAnnotation)
                        } else {
                            "@STRING"
                        }
                        List(length) { elementValue }
                    }

                    FillStrategy.RANDOM -> {
                        // RANDOM: generate different elements for each position
                        List(length) {
                            if (elementType != null) {
                                analyzePropertyType(elementType, null, propertyBeanAnnotation)
                            } else {
                                "@STRING"
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
                            "@STRING"
                        } else {
                            analyzePropertyType(keyType, null, propertyBeanAnnotation).toString()
                        }
                    } else {
                        "@STRING"
                    }

                    val valueValue = if (valueType != null) {
                        analyzePropertyType(valueType, null, propertyBeanAnnotation)
                    } else {
                        "@STRING"
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
    private fun analyzeCustomObject(kClass: KClass<*>, propertyBeanAnnotation: Mock.Bean? = null): Map<String, Any?> {
        return try {
            // Property-level @Mock.Bean annotation has higher priority than class-level annotation
            val effectiveBeanAnnotation = propertyBeanAnnotation ?: kClass.findAnnotation<Mock.Bean>()
            val config = if (effectiveBeanAnnotation != null) {
                // Use annotation configuration if present (property-level takes precedence)
                BeanMockConfig(
                    includePrivate = effectiveBeanAnnotation.includePrivate,
                    includeStatic = effectiveBeanAnnotation.includeStatic,
                    includeTransient = effectiveBeanAnnotation.includeTransient
                )
            } else {
                // Use default configuration if no annotation
                BeanMockConfig()
            }
            analyzeBean(kClass, config)
        } catch (e: Exception) {
            logger.warn("Failed to analyze custom object ${kClass.simpleName}: ${e.message}")
            mapOf("value" to "@STRING")
        }
    }

    /**
     * Container type behavior enumeration
     */
    private enum class ContainerBehavior {
        SINGLE_VALUE,    // Returns single wrapped value
        STREAM_VALUES,   // Returns list of values (for streams like Flux, Observable)
        RIGHT_TYPE,      // Uses second type parameter (for Either, Validation)
        NO_VALUE         // Returns null (for Completable)
    }

    /**
     * Analyze container types (Optional, CompletableFuture, Future, Callable, Supplier, Lazy, Deferred, Reactor, RxJava, Vavr, Arrow, etc.)
     */
    private fun analyzeContainerType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean? = null
    ): Any? {
        val kClass = type.classifier as KClass<*>
        val qualifiedName = kClass.qualifiedName ?: ""

        val behavior = getContainerBehavior(kClass, qualifiedName)

        return when (behavior) {
            ContainerBehavior.SINGLE_VALUE -> {
                val wrappedType = type.arguments.firstOrNull()?.type
                analyzeWrappedType(wrappedType, annotation, propertyBeanAnnotation)
            }

            ContainerBehavior.STREAM_VALUES -> {
                val elementType = type.arguments.firstOrNull()?.type
                val elementValue = analyzeWrappedType(elementType, annotation, propertyBeanAnnotation)
                listOf(elementValue)
            }

            ContainerBehavior.RIGHT_TYPE -> {
                val rightType = type.arguments.getOrNull(1)?.type
                analyzeWrappedType(rightType, annotation, propertyBeanAnnotation)
            }

            ContainerBehavior.NO_VALUE -> null
        }
    }

    /**
     * Determine container behavior based on class type
     */
    private fun getContainerBehavior(kClass: KClass<*>, qualifiedName: String): ContainerBehavior {
        return when {
            // Java standard types - single value
            Optional::class.java.isAssignableFrom(kClass.java) ||
                    CompletableFuture::class.java.isAssignableFrom(kClass.java) ||
                    java.util.concurrent.Future::class.java.isAssignableFrom(kClass.java) ||
                    java.util.concurrent.Callable::class.java.isAssignableFrom(kClass.java) ||
                    java.util.function.Supplier::class.java.isAssignableFrom(kClass.java) ||
                    kotlin.Lazy::class.java.isAssignableFrom(kClass.java) -> ContainerBehavior.SINGLE_VALUE

            // Stream types - multiple values
            qualifiedName.startsWith("reactor.core.publisher.Flux") ||
                    qualifiedName.startsWith("io.reactivex.Observable") ||
                    qualifiedName.startsWith("io.reactivex.rxjava3.core.Observable") ||
                    qualifiedName.startsWith("io.reactivex.Flowable") ||
                    qualifiedName.startsWith("io.reactivex.rxjava3.core.Flowable") -> ContainerBehavior.STREAM_VALUES

            // Either-like types - use right/second type parameter
            qualifiedName.startsWith("io.vavr.control.Either") ||
                    qualifiedName.startsWith("io.vavr.control.Validation") ||
                    qualifiedName.startsWith("arrow.core.Either") -> ContainerBehavior.RIGHT_TYPE

            // Completable types - no value
            qualifiedName.startsWith("io.reactivex.Completable") ||
                    qualifiedName.startsWith("io.reactivex.rxjava3.core.Completable") -> ContainerBehavior.NO_VALUE

            // Single value types (third-party libraries)
            qualifiedName.startsWith("kotlinx.coroutines.Deferred") ||
                    qualifiedName.startsWith("reactor.core.publisher.Mono") ||
                    qualifiedName.startsWith("io.reactivex.Single") ||
                    qualifiedName.startsWith("io.reactivex.rxjava3.core.Single") ||
                    qualifiedName.startsWith("io.reactivex.Maybe") ||
                    qualifiedName.startsWith("io.reactivex.rxjava3.core.Maybe") ||
                    qualifiedName.startsWith("io.vavr.control.Option") ||
                    qualifiedName.startsWith("io.vavr.control.Try") ||
                    qualifiedName.startsWith("io.vavr.Lazy") ||
                    qualifiedName.startsWith("io.vavr.concurrent.Future") ||
                    qualifiedName.startsWith("arrow.core.Option") ||
                    qualifiedName.startsWith("arrow.core.Try") ||
                    qualifiedName.startsWith("arrow.core.Validated") ||
                    qualifiedName.startsWith("arrow.fx.coroutines.Resource") -> ContainerBehavior.SINGLE_VALUE

            else -> ContainerBehavior.SINGLE_VALUE
        }
    }

    /**
     * Analyze wrapped type with fallback to default value
     */
    private fun analyzeWrappedType(
        wrappedType: KType?,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean?
    ): Any? {
        return if (wrappedType != null) {
            analyzePropertyType(wrappedType, annotation, propertyBeanAnnotation)
        } else {
            "@STRING"
        }
    }
}