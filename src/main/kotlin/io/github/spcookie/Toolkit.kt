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
fun isContainerType(kClass: KClass<*>): Boolean {
    return when {
        // Java standard container types
        java.util.Optional::class.java.isAssignableFrom(kClass.java) -> true
        java.util.concurrent.CompletableFuture::class.java.isAssignableFrom(kClass.java) -> true
        java.util.concurrent.Future::class.java.isAssignableFrom(kClass.java) -> true
        java.util.concurrent.Callable::class.java.isAssignableFrom(kClass.java) -> true
        java.util.function.Supplier::class.java.isAssignableFrom(kClass.java) -> true
        // Kotlin standard container types
        kotlin.Lazy::class.java.isAssignableFrom(kClass.java) -> true
        else -> {
            val qualifiedName = kClass.qualifiedName ?: return false
            // Check for third-party library container types using string comparison to avoid dependency issues
            when {
                // Kotlin Coroutines
                qualifiedName.startsWith("kotlinx.coroutines.Deferred") -> true
                // Project Reactor
                qualifiedName.startsWith("reactor.core.publisher.Mono") -> true
                qualifiedName.startsWith("reactor.core.publisher.Flux") -> true
                // RxJava 2/3
                qualifiedName.startsWith("io.reactivex.Observable") -> true
                qualifiedName.startsWith("io.reactivex.Single") -> true
                qualifiedName.startsWith("io.reactivex.Maybe") -> true
                qualifiedName.startsWith("io.reactivex.Completable") -> true
                qualifiedName.startsWith("io.reactivex.Flowable") -> true
                qualifiedName.startsWith("io.reactivex.rxjava3.core.Observable") -> true
                qualifiedName.startsWith("io.reactivex.rxjava3.core.Single") -> true
                qualifiedName.startsWith("io.reactivex.rxjava3.core.Maybe") -> true
                qualifiedName.startsWith("io.reactivex.rxjava3.core.Completable") -> true
                qualifiedName.startsWith("io.reactivex.rxjava3.core.Flowable") -> true
                // Vavr (formerly Javaslang)
                qualifiedName.startsWith("io.vavr.control.Option") -> true
                qualifiedName.startsWith("io.vavr.control.Try") -> true
                qualifiedName.startsWith("io.vavr.control.Either") -> true
                qualifiedName.startsWith("io.vavr.control.Validation") -> true
                qualifiedName.startsWith("io.vavr.Lazy") -> true
                qualifiedName.startsWith("io.vavr.concurrent.Future") -> true
                // Arrow (Kotlin functional programming)
                qualifiedName.startsWith("arrow.core.Option") -> true
                qualifiedName.startsWith("arrow.core.Either") -> true
                qualifiedName.startsWith("arrow.core.Try") -> true
                qualifiedName.startsWith("arrow.core.Validated") -> true
                qualifiedName.startsWith("arrow.fx.coroutines.Resource") -> true
                else -> false
            }
        }
    }
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