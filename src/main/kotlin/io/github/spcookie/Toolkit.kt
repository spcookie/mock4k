package io.github.spcookie

import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Utility functions for common operations
 *
 * @author spcookie
 * @since 1.2.0
 */

/**
 * Check if type is a basic type
 */
fun isBasicType(kClass: KClass<*>): Boolean {
    return when (kClass) {
        String::class -> true
        Int::class, Integer::class -> true
        Long::class, java.lang.Long::class -> true
        Float::class, java.lang.Float::class -> true
        Double::class, java.lang.Double::class -> true
        Boolean::class, java.lang.Boolean::class -> true
        Char::class, Character::class -> true
        Byte::class, java.lang.Byte::class -> true
        Short::class, java.lang.Short::class -> true
        BigDecimal::class -> true
        BigInteger::class -> true
        UUID::class -> true
        else -> isDateTimeType(kClass)
    }
}

/**
 * Check if a class is a date/time type (both legacy and Java 8+ types)
 */
fun isDateTimeType(type: KClass<*>): Boolean {
    return when (type) {
        // Legacy date/time types (before Java 8)
        java.util.Date::class -> true
        java.sql.Date::class -> true
        java.sql.Time::class -> true
        java.sql.Timestamp::class -> true
        java.util.Calendar::class -> true
        // Java 8+ date/time types
        java.time.LocalDate::class -> true
        java.time.LocalTime::class -> true
        java.time.LocalDateTime::class -> true
        java.time.ZonedDateTime::class -> true
        java.time.OffsetDateTime::class -> true
        java.time.OffsetTime::class -> true
        java.time.Instant::class -> true
        java.time.Duration::class -> true
        java.time.Period::class -> true
        java.time.Year::class -> true
        java.time.YearMonth::class -> true
        java.time.MonthDay::class -> true
        else -> false
    }
}

/**
 * Check if type is a container type
 */
fun isContainerType(kClass: KClass<*>, containerAdapter: ContainerAdapter): Boolean {
    // First check Java standard types
    when {
        java.util.Optional::class.java.isAssignableFrom(kClass.java) -> return true
        java.util.concurrent.CompletableFuture::class.java.isAssignableFrom(kClass.java) -> return true
        java.util.concurrent.Future::class.java.isAssignableFrom(kClass.java) -> return true
        java.util.concurrent.Callable::class.java.isAssignableFrom(kClass.java) -> return true
        java.util.function.Supplier::class.java.isAssignableFrom(kClass.java) -> return true
        Lazy::class.java.isAssignableFrom(kClass.java) -> return true
    }

    // Then check registered third-party types
    val qualifiedName = kClass.qualifiedName ?: return false
    return containerAdapter.getRegisteredPrefixes().any { qualifiedName.startsWith(it) }
}

/**
 * Check if type is a collection type
 */
fun isCollectionType(kClass: KClass<*>): Boolean {
    return when {
        List::class.java.isAssignableFrom(kClass.java) -> true
        Set::class.java.isAssignableFrom(kClass.java) -> true
        Map::class.java.isAssignableFrom(kClass.java) -> true
        kClass.java.isArray -> true
        else -> false
    }
}

/**
 * Check if a class is a custom class (not a basic type)
 */
fun isCustomClass(type: KClass<*>, containerAdapter: ContainerAdapter): Boolean {
    return when {
        isBasicType(type) -> false
        isCollectionType(type) -> false
        isContainerType(type, containerAdapter) -> false
        type.java.isEnum -> false
        type.java.isInterface -> false
        type.isAbstract -> false
        type == Any::class -> false
        type.isSealed -> false
        type.java.packageName.startsWith("java.") -> false
        type.java.packageName.startsWith("kotlin.") -> false
        else -> true
    }
}

/**
 * Get eligible properties for mocking based on configuration
 */
fun getEligibleProperties(clazz: KClass<*>, config: BeanMockConfig): List<KProperty<*>> {
    val constructor = clazz.primaryConstructor
    val isDataClass = clazz.isData
    val isJavaRecord = clazz.java.isRecord

    val properties = clazz.memberProperties.filter { property ->
        val javaField = property.javaField

        // For Kotlin data class, all val properties from primary constructor should be included
        val isDataClassProperty = isDataClass && constructor?.parameters?.any { it.name == property.name } == true

        // For Java record, all properties should be included (they don't have javaField)
        val isRecordProperty = isJavaRecord && javaField == null

        // For Kotlin properties, check if they are mutable (var) rather than field visibility
        val isMutableProperty = property is KMutableProperty<*>

        // For Kotlin properties, check if it's actually a private property
        val isKotlinPrivateProperty = property.visibility == KVisibility.PRIVATE

        // Check if property should be included based on configuration
        when {
            // Special handling for data class and record properties
            isDataClassProperty || isRecordProperty -> {
                // For data class and record, check privacy settings
                if (!config.includePrivate && isKotlinPrivateProperty) {
                    false
                } else {
                    // Check if Mock annotation is present and enabled
                    val mockParam = property.findAnnotation<Mock.Property>()
                    val javaFieldAnnotation = javaField?.getAnnotation(Mock.Property::class.java)

                    // Try to get annotation from constructor parameter
                    getConstructorAnnotation(constructor, property, mockParam, javaFieldAnnotation)
                }
            }

            // Regular property handling
            javaField == null -> {
                false
            }

            !isMutableProperty -> {
                false
            }

            !config.includePrivate && isKotlinPrivateProperty -> {
                false
            }

            !config.includeStatic && Modifier.isStatic(javaField.modifiers) -> {
                false
            }

            !config.includeTransient && Modifier.isTransient(javaField.modifiers) -> {
                false
            }

            else -> {
                // Check if Mock annotation is present and enabled
                val mockParam = property.findAnnotation<Mock.Property>()
                val javaFieldAnnotation = javaField.getAnnotation(Mock.Property::class.java)

                // Try to get annotation from constructor parameter
                getConstructorAnnotation(constructor, property, mockParam, javaFieldAnnotation)
            }
        }
    }
    return properties
}

private fun getConstructorAnnotation(
    constructor: KFunction<Any>?,
    property: KProperty1<out Any, *>,
    mockParam: Mock.Property?,
    javaFieldAnnotation: Mock.Property?
): Boolean {
    val constructorParam = constructor?.parameters?.find { it.name == property.name }
    val constructorAnnotation = constructorParam?.findAnnotation<Mock.Property>()

    val finalAnnotation = mockParam ?: javaFieldAnnotation ?: constructorAnnotation
    val enabled = finalAnnotation?.enabled != false
    return enabled
}