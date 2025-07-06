package io.github.spcookie

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * Maps MockEngine generated results back to Bean objects
 *
 * @author spcookie
 * @since 1.2.0
 */
internal class BeanMockMapper(private val typeAdapter: TypeAdapter) {

    private val logger = LoggerFactory.getLogger(BeanMockMapper::class.java)

    /**
     * Map generated data to Bean instance
     */
    fun <T : Any> mapToBean(clazz: KClass<T>, data: Map<String, Any?>, config: BeanMockConfig): T {
        val instance = createInstance(clazz)
        val properties = getEligibleProperties(clazz, config)

        properties.forEach { property ->
            try {
                val propertyName = property.name
                val generatedValue = data[propertyName]
                val mappedValue = mapValueToProperty(generatedValue, property)
                setPropertyValue(instance, property, mappedValue)
            } catch (e: Exception) {
                logger.warn("Failed to map property ${property.name}: ${e.message}", e)
            }
        }

        return instance
    }

    /**
     * Create instance of the given class
     */
    private fun <T : Any> createInstance(clazz: KClass<T>): T {
        return try {
            clazz.createInstance()
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot create instance of ${clazz.simpleName}. Make sure it has a no-arg constructor.", e)
        }
    }

    /**
     * Get eligible properties for mapping
     */
    private fun getEligibleProperties(clazz: KClass<*>, config: BeanMockConfig): List<KProperty<*>> {
        return clazz.memberProperties.filter { property ->
            val javaField = property.javaField
            val isMutableProperty = property is KMutableProperty<*>
            
            when {
                javaField == null -> false
                !isMutableProperty -> false
                else -> {
                    val mockParam = property.findAnnotation<io.github.spcookie.Mock.Property>()
                    mockParam?.enabled != false
                }
            }
        }
    }

    /**
     * Map generated value to property type
     */
    private fun mapValueToProperty(value: Any?, property: KProperty<*>): Any? {
        return mapValueToProperty(value, property.returnType, BeanMockConfig())
    }

    /**
     * Map generated value to property type with config
     */
    private fun mapValueToProperty(value: Any?, kType: KType, config: BeanMockConfig): Any? {
        if (value == null) return null
        
        val propertyClass = kType.classifier as? KClass<*> ?: return null

        return when {
            // Handle nested objects (Maps that should become Bean instances)
            value is Map<*, *> && isCustomClass(propertyClass) -> {
                @Suppress("UNCHECKED_CAST")
                val dataMap = value as Map<String, Any?>
                mapToBean(propertyClass, dataMap, config)
            }
            // Handle Lists
            value is List<*> && (propertyClass == List::class || propertyClass == MutableList::class) -> {
                mapListValue(value, kType)
            }
            // Handle Sets
            value is List<*> && (propertyClass == Set::class || propertyClass == MutableSet::class) -> {
                mapSetValue(value, kType)
            }
            // Handle Maps
            value is Map<*, *> && (propertyClass == Map::class || propertyClass == MutableMap::class) -> {
                mapMapValue(value, kType)
            }
            // Handle basic types
            else -> {
                convertToType(value, propertyClass, kType)
            }
        }
    }

    /**
     * Map List value
     */
    private fun mapListValue(value: List<*>, kType: KType): List<Any?> {
        val elementType = getGenericTypeArgument(kType, 0) ?: return value.toMutableList()
        val config = BeanMockConfig()
        
        return value.map { element ->
            mapValueToProperty(element, elementType, config)
        }.toMutableList()
    }

    /**
     * Map Set value
     */
    private fun mapSetValue(value: List<*>, kType: KType): Set<Any?> {
        val listValue = mapListValue(value, kType)
        return listValue.toMutableSet()
    }

    /**
     * Map Map value
     */
    private fun mapMapValue(value: Map<*, *>, kType: KType): Map<Any?, Any?> {
        val keyType = getGenericTypeArgument(kType, 0) ?: return value.toMutableMap()
        val valueType = getGenericTypeArgument(kType, 1) ?: return value.toMutableMap()
        val keyClass = keyType.classifier as? KClass<*> ?: return value.toMutableMap()
        val config = BeanMockConfig()
        
        return value.mapKeys { (key, _) ->
            convertToType(key, keyClass, keyType)
        }.mapValues { (_, mapValue) ->
            mapValueToProperty(mapValue, valueType, config)
        }.toMutableMap()
    }

    /**
     * Get generic type argument at specified index
     */
    private fun getGenericTypeArgument(kType: KType, index: Int): KType? {
        return kType.arguments.getOrNull(index)?.type
    }

    /**
     * Check if a class is a custom class (not a basic type)
     */
    private fun isCustomClass(type: KClass<*>): Boolean {
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
            type == BigDecimal::class -> false
            type == java.math.BigInteger::class -> false
            isDateTimeType(type) -> false
            else -> true
        }
    }

    /**
     * Check if a class is a date/time type (both legacy and Java 8+ types)
     */
    private fun isDateTimeType(type: KClass<*>): Boolean {
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
     * Convert result to target type
     */
    private fun convertToType(value: Any?, targetType: KClass<*>, kType: KType): Any? {
        if (value == null) return null
        
        val adapter = typeAdapter.get(targetType)
        if (adapter != null) {
            return adapter(value)
        }

        return when (targetType) {
            String::class -> value.toString()
            Int::class, Integer::class -> {
                when (value) {
                    is Number -> value.toInt()
                    is String -> value.toIntOrNull() ?: 0
                    else -> 0
                }
            }
            Long::class, java.lang.Long::class -> {
                when (value) {
                    is Number -> value.toLong()
                    is String -> value.toLongOrNull() ?: 0L
                    else -> 0L
                }
            }
            Float::class, java.lang.Float::class -> {
                when (value) {
                    is Number -> value.toFloat()
                    is String -> value.toFloatOrNull() ?: 0f
                    else -> 0f
                }
            }
            Double::class, java.lang.Double::class -> {
                when (value) {
                    is Number -> value.toDouble()
                    is String -> value.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
            }
            Boolean::class, java.lang.Boolean::class -> {
                when (value) {
                    is Boolean -> value
                    is String -> value.toBoolean()
                    is Number -> value.toInt() != 0
                    else -> false
                }
            }
            BigDecimal::class -> {
                when (value) {
                    is BigDecimal -> value
                    is Number -> BigDecimal.valueOf(value.toDouble())
                    is String -> BigDecimal(value)
                    else -> BigDecimal.ZERO
                }
            }
            java.math.BigInteger::class -> {
                when (value) {
                    is java.math.BigInteger -> value
                    is Number -> java.math.BigInteger.valueOf(value.toLong())
                    is String -> java.math.BigInteger(value)
                    else -> java.math.BigInteger.ZERO
                }
            }
            // Time and date types before Java 8
            java.util.Date::class -> {
                when (value) {
                    is java.util.Date -> value
                    is String -> java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value)
                    is Number -> java.util.Date(value.toLong())
                    else -> java.util.Date()
                }
            }
            java.sql.Date::class -> {
                when (value) {
                    is java.sql.Date -> value
                    is java.util.Date -> java.sql.Date(value.time)
                    is String -> java.sql.Date.valueOf(value)
                    is Number -> java.sql.Date(value.toLong())
                    else -> java.sql.Date(System.currentTimeMillis())
                }
            }
            java.sql.Time::class -> {
                when (value) {
                    is java.sql.Time -> value
                    is String -> java.sql.Time.valueOf(value)
                    is Number -> java.sql.Time(value.toLong())
                    else -> java.sql.Time(System.currentTimeMillis())
                }
            }
            java.sql.Timestamp::class -> {
                when (value) {
                    is java.sql.Timestamp -> value
                    is java.util.Date -> java.sql.Timestamp(value.time)
                    is String -> java.sql.Timestamp.valueOf(value)
                    is Number -> java.sql.Timestamp(value.toLong())
                    else -> java.sql.Timestamp(System.currentTimeMillis())
                }
            }
            // Java 8 new time and date types
            java.time.LocalDate::class -> {
                when (value) {
                    is java.time.LocalDate -> value
                    is String -> java.time.LocalDate.parse(value)
                    else -> java.time.LocalDate.now()
                }
            }
            java.time.LocalTime::class -> {
                when (value) {
                    is java.time.LocalTime -> value
                    is String -> java.time.LocalTime.parse(value)
                    else -> java.time.LocalTime.now()
                }
            }
            java.time.LocalDateTime::class -> {
                when (value) {
                    is java.time.LocalDateTime -> value
                    is String -> java.time.LocalDateTime.parse(value)
                    else -> java.time.LocalDateTime.now()
                }
            }
            java.time.Instant::class -> {
                when (value) {
                    is java.time.Instant -> value
                    is String -> java.time.Instant.parse(value)
                    is Number -> java.time.Instant.ofEpochMilli(value.toLong())
                    else -> java.time.Instant.now()
                }
            }
            else -> {
                if (targetType.java.isEnum) {
                    val enumConstants = targetType.java.enumConstants
                    return enumConstants?.find { it.toString() == value.toString() }
                        ?: enumConstants?.firstOrNull()
                }
                value
            }
        }
    }

    /**
     * Set property value using reflection
     */
    private fun setPropertyValue(instance: Any, property: KProperty<*>, value: Any?) {
        if (property is KMutableProperty<*>) {
            property.isAccessible = true
            property.setter.call(instance, value)
        } else {
            // Try to set via field if property is not mutable
            val field = property.javaField
            if (field != null) {
                field.isAccessible = true
                field.set(instance, value)
            }
        }
    }
}