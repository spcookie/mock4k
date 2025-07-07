package io.github.spcookie

import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*
import kotlin.reflect.KClass

/**
 * Type adapter manager for custom type conversion
 * Provides default adapters for common types and allows custom registration
 *
 * @author spcookie
 * @since 1.2.0
 */
class TypeAdapter {

    private val adapters = mutableMapOf<KClass<*>, (Any?) -> Any?>()

    init {
        registerDefaultAdapters()
    }

    /**
     * Register default type adapters for common types
     */
    private fun registerDefaultAdapters() {
        // Basic types
        register(String::class) { value -> value?.toString() }
        register(Int::class) { value ->
            when (value) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            }
        }
        register(Long::class) { value ->
            when (value) {
                is Number -> value.toLong()
                is String -> value.toLongOrNull()
                else -> null
            }
        }
        register(Float::class) { value ->
            when (value) {
                is Number -> value.toFloat()
                is String -> value.toFloatOrNull()
                else -> null
            }
        }
        register(Double::class) { value ->
            when (value) {
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull()
                else -> null
            }
        }
        register(Boolean::class) { value ->
            when (value) {
                is Boolean -> value
                is String -> value.toBooleanStrictOrNull()
                else -> null
            }
        }
        register(Char::class) { value ->
            when (value) {
                is Char -> value
                is String -> if (value.isNotEmpty()) value[0] else null
                is Number -> value.toInt().toChar()
                else -> null
            }
        }
        register(Byte::class) { value ->
            when (value) {
                is Number -> value.toByte()
                is String -> value.toByteOrNull()
                else -> null
            }
        }
        register(Short::class) { value ->
            when (value) {
                is Number -> value.toShort()
                is String -> value.toShortOrNull()
                else -> null
            }
        }

        // BigDecimal and BigInteger
        register(BigDecimal::class) { value ->
            when (value) {
                is Number -> BigDecimal.valueOf(value.toDouble())
                is String -> try {
                    BigDecimal(value)
                } catch (e: Exception) {
                    null
                }

                else -> null
            }
        }
        register(BigInteger::class) { value ->
            when (value) {
                is Number -> BigInteger.valueOf(value.toLong())
                is String -> try {
                    BigInteger(value)
                } catch (e: Exception) {
                    null
                }

                else -> null
            }
        }

        // Legacy date/time types
        register(Date::class) { value ->
            when (value) {
                is Date -> value
                is String -> try {
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value)
                } catch (e: Exception) {
                    null
                }

                is Number -> Date(value.toLong())
                else -> null
            }
        }
        register(java.sql.Date::class) { value ->
            when (value) {
                is java.sql.Date -> value
                is Date -> java.sql.Date(value.time)
                is String -> try {
                    java.sql.Date.valueOf(value)
                } catch (e: Exception) {
                    null
                }

                is Number -> java.sql.Date(value.toLong())
                else -> null
            }
        }
        register(java.sql.Time::class) { value ->
            when (value) {
                is java.sql.Time -> value
                is String -> try {
                    java.sql.Time.valueOf(value)
                } catch (e: Exception) {
                    null
                }

                is Number -> java.sql.Time(value.toLong())
                else -> null
            }
        }
        register(java.sql.Timestamp::class) { value ->
            when (value) {
                is java.sql.Timestamp -> value
                is Date -> java.sql.Timestamp(value.time)
                is String -> try {
                    java.sql.Timestamp.valueOf(value)
                } catch (e: Exception) {
                    null
                }

                is Number -> java.sql.Timestamp(value.toLong())
                else -> null
            }
        }

        // Java 8+ date/time types
        register(LocalDate::class) { value ->
            when (value) {
                is LocalDate -> value
                is String -> try {
                    LocalDate.parse(value)
                } catch (e: Exception) {
                    null
                }

                else -> null
            }
        }
        register(LocalTime::class) { value ->
            when (value) {
                is LocalTime -> value
                is String -> try {
                    LocalTime.parse(value)
                } catch (e: Exception) {
                    null
                }

                else -> null
            }
        }
        register(LocalDateTime::class) { value ->
            when (value) {
                is LocalDateTime -> value
                is String -> try {
                    LocalDateTime.parse(value)
                } catch (e: Exception) {
                    null
                }

                else -> null
            }
        }
        register(Instant::class) { value ->
            when (value) {
                is Instant -> value
                is String -> try {
                    Instant.parse(value)
                } catch (e: Exception) {
                    null
                }

                is Number -> Instant.ofEpochMilli(value.toLong())
                else -> null
            }
        }
        register(ZonedDateTime::class) { value ->
            when (value) {
                is ZonedDateTime -> value
                is String -> try {
                    ZonedDateTime.parse(value)
                } catch (e: Exception) {
                    null
                }

                else -> null
            }
        }
        register(OffsetDateTime::class) { value ->
            when (value) {
                is OffsetDateTime -> value
                is String -> try {
                    OffsetDateTime.parse(value)
                } catch (e: Exception) {
                    null
                }

                else -> null
            }
        }

        // Kotlin Pair type
        register(Pair::class) { value ->
            when (value) {
                is Pair<*, *> -> value
                is String -> {
                    val parts = value.split(",")
                    if (parts.size >= 2) {
                        Pair(parts[0].trim(), parts[1].trim())
                    } else {
                        Pair(value, "")
                    }
                }

                else -> Pair(value.toString(), "")
            }
        }
    }

    /**
     * Register a custom type adapter
     */
    fun <T : Any> register(type: KClass<T>, adapter: (Any?) -> T?) {
        adapters[type] = adapter as (Any?) -> Any?
    }

    /**
     * Get type adapter for a specific type
     */
    internal fun get(type: KClass<*>): ((Any?) -> Any?)? {
        return adapters[type]
    }

    /**
     * Get all registered adapters (for internal use)
     */
    internal fun getAll(): Map<KClass<*>, (Any?) -> Any?> {
        return adapters.toMap()
    }
}