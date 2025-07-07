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
 * Check if a class is a custom class (not a basic type)
 */
fun isCustomClass(type: KClass<*>): Boolean {
    return when {
        type.java.isPrimitive -> false
        type.java.isEnum -> false
        type.java.packageName.startsWith("java.") -> false
        type.java.packageName.startsWith("kotlin.") -> false
        type == String::class -> false
        type == Int::class || type == Integer::class -> false
        type == Long::class || type == java.lang.Long::class -> false
        type == Float::class || type == java.lang.Float::class -> false
        type == Double::class || type == java.lang.Double::class -> false
        type == Boolean::class || type == java.lang.Boolean::class -> false
        type == java.math.BigDecimal::class -> false
        type == java.math.BigInteger::class -> false
        isDateTimeType(type) -> false
        else -> true
    }
}