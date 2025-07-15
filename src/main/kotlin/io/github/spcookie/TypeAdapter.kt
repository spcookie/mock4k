package io.github.spcookie

import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*
import kotlin.reflect.KClass

/**
 * 用于自定义类型转换的类型适配器管理器
 * 为常见类型提供默认适配器并允许自定义注册
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
     * 为常见类型注册默认类型适配器
     */
    private fun registerDefaultAdapters() {
        // 基本类型
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

        // BigDecimal 和 BigInteger
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

        // 旧式日期/时间类型
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

        // Java 8+ 日期/时间类型
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

        // Kotlin Pair 类型
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
     * 注册自定义类型适配器
     */
    fun <T : Any> register(type: KClass<T>, adapter: (Any?) -> T?) {
        adapters[type] = adapter as (Any?) -> Any?
    }

    /**
     * 获取特定类型的适配器
     */
    internal fun get(type: KClass<*>): ((Any?) -> Any?)? {
        return adapters[type]
    }

    /**
     * 获取所有已注册的适配器（供内部使用）
     */
    internal fun getAll(): Map<KClass<*>, (Any?) -> Any?> {
        return adapters.toMap()
    }
}