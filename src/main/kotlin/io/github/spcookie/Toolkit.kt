package io.github.spcookie

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

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
        else -> isDateTimeType(kClass) || kClass == Pair::class
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
        kotlin.Lazy::class.java.isAssignableFrom(kClass.java) -> return true
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