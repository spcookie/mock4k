package io.github.spcookie

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * Maps generated mock data back to Bean instances
 *
 * @author spcookie
 * @since 1.2.0
 */
internal class BeanMockMapper(
    private val typeAdapter: TypeAdapter,
    private val containerAdapter: ContainerAdapter
) {

    private val logger = LoggerFactory.getLogger(BeanMockMapper::class.java)

    /**
     * Map generated data to Bean instance
     */
    fun <T : Any> mapToBean(clazz: KClass<T>, data: Map<String, Any?>, config: BeanMockConfig): T {
        val isDataClass = clazz.isData
        val isJavaRecord = clazz.java.isRecord

        return when {
            // For Kotlin data class or Java record, use constructor-based creation
            isDataClass || isJavaRecord -> {
                val instance = createInstanceWithConstructor(clazz, data, config)
                // After constructor creation, map any remaining unmapped properties
                mapRemainingProperties(instance, clazz, data, config)
                instance
            }

            // For regular beans, use property-based creation
            else -> createInstanceWithProperties(clazz, data, config)
        }
    }

    /**
     * Create instance using primary constructor
     */
    private fun <T : Any> createInstanceWithConstructor(
        clazz: KClass<T>,
        data: Map<String, Any?>,
        config: BeanMockConfig
    ): T {
        val constructor = clazz.primaryConstructor
            ?: throw IllegalArgumentException("No primary constructor found for ${clazz.simpleName}")

        val args = constructor.parameters
            .filter { it.findAnnotation<Mock.Property>()?.enabled != false }
            .map { param ->
                val propertyName = param.name ?: throw IllegalArgumentException("Parameter name not available")

                val rawValue = findValueForProperty(propertyName, data)

                if (rawValue != null) {
                    convertValue(rawValue, param.type, config)
                } else {
                    // Use default value if available
                    if (param.isOptional) {
                        null
                    } else {
                        generateValueForType(param.type, config)
                    }
                }
            }.toTypedArray()

        return constructor.call(*args)
    }

    /**
     * Create instance using default constructor and set properties
     */
    private fun <T : Any> createInstanceWithProperties(
        clazz: KClass<T>,
        data: Map<String, Any?>,
        config: BeanMockConfig
    ): T {
        val instance = clazz.createInstance()
        mapPropertiesToInstance(instance, clazz, data, config)
        return instance
    }

    /**
     * Map remaining properties that were not handled by constructor
     */
    private fun <T : Any> mapRemainingProperties(
        instance: T,
        clazz: KClass<T>,
        data: Map<String, Any?>,
        config: BeanMockConfig
    ) {
        val constructor = clazz.primaryConstructor
        val constructorParamNames = constructor?.parameters?.mapNotNull { it.name }?.toSet() ?: emptySet()

        // Get properties that were not mapped by constructor
        val eligibleProperties = getEligibleProperties(clazz, config)
        val unmappedProperties = eligibleProperties.filter { property ->
            // Only include properties that are not handled by constructor
            property.name !in constructorParamNames
        }

        mapPropertiesToInstance(instance, unmappedProperties, data, config)
    }

    /**
     * Map properties to instance
     */
    private fun mapPropertiesToInstance(
        instance: Any,
        clazz: KClass<*>,
        data: Map<String, Any?>,
        config: BeanMockConfig
    ) {
        val eligibleProperties = getEligibleProperties(clazz, config)
        mapPropertiesToInstance(instance, eligibleProperties, data, config)
    }

    /**
     * Map specific properties to instance
     */
    private fun mapPropertiesToInstance(
        instance: Any,
        properties: List<KProperty<*>>,
        data: Map<String, Any?>,
        config: BeanMockConfig
    ) {
        for (property in properties) {
            try {
                val rawValue = findValueForProperty(property.name, data)
                if (rawValue != null) {
                    val convertedValue = convertValue(rawValue, property.returnType, config)
                    setPropertyValue(instance, property, convertedValue)
                }
            } catch (e: Exception) {
                logger.warn("Failed to set property ${property.name}: ${e.message}")
            }
        }
    }

    /**
     * Find value for property in generated data, handling rule-based keys
     */
    private fun findValueForProperty(propertyName: String, data: Map<String, Any?>): Any? {
        // First try exact match
        data[propertyName]?.let { return it }

        // Then try to find keys that start with property name followed by |
        val ruleBasedKey = data.keys.find { it.startsWith("$propertyName|") }
        if (ruleBasedKey != null) {
            return data[ruleBasedKey]
        }

        return null
    }

    /**
     * Check if property should be included based on configuration
     */
    private fun shouldIncludeProperty(property: KProperty<*>, config: BeanMockConfig): Boolean {
        val javaField = property.javaField

        // Check private access
        if (!config.includePrivate && javaField != null && !java.lang.reflect.Modifier.isPublic(javaField.modifiers)) {
            return false
        }

        // Check static access
        if (!config.includeStatic && javaField != null && java.lang.reflect.Modifier.isStatic(javaField.modifiers)) {
            return false
        }

        // Check transient access
        if (!config.includeTransient && javaField != null && java.lang.reflect.Modifier.isTransient(javaField.modifiers)) {
            return false
        }

        return true
    }

    /**
     * Convert value to target type using TypeAdapter
     */
    private fun convertValue(value: Any?, targetType: KType, config: BeanMockConfig): Any? {
        if (value == null) return null

        val targetClass = targetType.classifier as? KClass<*> ?: return value

        return when {
            // Handle basic types with TypeAdapter
            isBasicType(targetClass) -> {
                val adapter = typeAdapter.get(targetClass)
                adapter?.invoke(value) ?: value
            }

            // Handle collections
            isCollectionType(targetClass) -> convertCollectionValue(value, targetType, config)

            // Handle container types
            isContainerType(targetClass, containerAdapter) -> convertContainerValue(value, targetType, config)

            // Handle custom objects
            isCustomClass(targetClass, containerAdapter) -> convertCustomObjectValue(value, targetClass, config)

            else -> value
        }
    }

    /**
     * Convert collection values
     */
    private fun convertCollectionValue(value: Any?, targetType: KType, config: BeanMockConfig): Any? {
        if (value == null) return null

        val targetClass = targetType.classifier as KClass<*>

        return when {
            List::class.java.isAssignableFrom(targetClass.java) -> {
                val elementType = targetType.arguments.firstOrNull()?.type
                when (value) {
                    is Collection<*> -> {
                        if (elementType != null) {
                            value.map { convertValue(it, elementType, config) }
                        } else {
                            value
                        }
                    }

                    else -> listOf(value)
                }
            }

            Set::class.java.isAssignableFrom(targetClass.java) -> {
                val elementType = targetType.arguments.firstOrNull()?.type
                when (value) {
                    is Collection<*> -> {
                        if (elementType != null) {
                            value.map { convertValue(it, elementType, config) }.toSet()
                        } else {
                            value.toSet()
                        }
                    }

                    else -> setOf(value)
                }
            }

            Map::class.java.isAssignableFrom(targetClass.java) -> {
                val keyType = targetType.arguments.getOrNull(0)?.type
                val valueType = targetType.arguments.getOrNull(1)?.type

                when (value) {
                    is Map<*, *> -> {
                        if (keyType != null && valueType != null) {
                            value.mapKeys { (k, _) -> convertValue(k, keyType, config) }
                                .mapValues { (_, v) -> convertValue(v, valueType, config) }
                        } else {
                            value
                        }
                    }

                    else -> mapOf("key" to value)
                }
            }

            targetClass.java.isArray -> {
                when (value) {
                    is List<*> -> {
                        val componentType = targetClass.java.componentType
                        val array = java.lang.reflect.Array.newInstance(componentType, value.size)
                        value.forEachIndexed { index, item ->
                            java.lang.reflect.Array.set(array, index, item)
                        }
                        array
                    }

                    else -> arrayOf(value)
                }
            }

            else -> value
        }
    }

    /**
     * Convert container values using ContainerAdapter
     */
    private fun convertContainerValue(value: Any?, targetType: KType, config: BeanMockConfig): Any? {
        return containerAdapter.convertContainerValue(
            value,
            targetType,
            config,
            typeAdapter
        ) { v, wrappedType, cfg ->
            convertWrappedValue(v, wrappedType, cfg)
        }
    }

    /**
     * Convert wrapped value with fallback to original value
     */
    private fun convertWrappedValue(value: Any?, wrappedType: KType?, config: BeanMockConfig): Any? {
        return if (wrappedType != null) {
            convertValue(value, wrappedType, config)
        } else {
            value
        }
    }

    /**
     * Convert custom object values
     */
    private fun convertCustomObjectValue(value: Any?, targetClass: KClass<*>, config: BeanMockConfig): Any? {
        if (value == null) return null

        return when (value) {
            is Map<*, *> -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val dataMap = value as Map<String, Any?>
                    mapToBean(targetClass, dataMap, config)
                } catch (e: Exception) {
                    logger.warn("Failed to convert map to ${targetClass.simpleName}: ${e.message}")
                    null
                }
            }

            else -> {
                // Try to use TypeAdapter if available
                val adapter = typeAdapter.get(targetClass)
                adapter?.invoke(value) ?: value
            }
        }
    }

    /**
     * Set property value on instance
     */
    private fun setPropertyValue(instance: Any, property: KProperty<*>, value: Any?) {
        property.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (property as KMutableProperty<Any?>).setter.call(instance, value)
    }

    /**
     * Generate appropriate value for type, including recursive object creation
     */
    private fun generateValueForType(type: KType, config: BeanMockConfig): Any? {
        val kClass = type.classifier as? KClass<*> ?: return null

        return when {
            // Handle basic types and collections
            isBasicType(kClass) || isCollectionType(kClass) -> getDefaultValueForType(type)

            // Handle container types
            isContainerType(kClass, containerAdapter) -> {
                // For container types, try to create with default wrapped value
                val wrappedType = type.arguments.firstOrNull()?.type
                if (wrappedType != null && config.depth > 0) {
                    val reducedConfig = config.copy(depth = config.depth - 1)
                    val wrappedValue = generateValueForType(wrappedType, reducedConfig)
                    convertContainerValue(wrappedValue, type, reducedConfig)
                } else {
                    null
                }
            }

            // Handle custom objects - recursively create them
            isCustomClass(kClass, containerAdapter) -> {
                // Check depth to prevent infinite recursion
                if (config.depth <= 0) {
                    return null
                }

                try {
                    // Recursively create the custom object with empty data and reduced depth
                    val reducedConfig = config.copy(depth = config.depth - 1)
                    mapToBean(kClass, emptyMap(), reducedConfig)
                } catch (e: Exception) {
                    logger.warn("Failed to recursively create ${kClass.simpleName}: ${e.message}")
                    null
                }
            }

            else -> getDefaultValueForType(type)
        }
    }

    /**
     * Get default value for basic types
     */
    private fun getDefaultValueForType(type: KType): Any? {
        val kClass = type.classifier as? KClass<*> ?: return null

        return when (kClass) {
            String::class -> ""
            Int::class, Integer::class -> 0
            Long::class, java.lang.Long::class -> 0L
            Float::class, java.lang.Float::class -> 0.0f
            Double::class, java.lang.Double::class -> 0.0
            Boolean::class, java.lang.Boolean::class -> false
            Char::class, Character::class -> '\u0000'
            Byte::class, java.lang.Byte::class -> 0.toByte()
            Short::class, java.lang.Short::class -> 0.toShort()
            BigDecimal::class -> BigDecimal.ZERO
            BigInteger::class -> BigInteger.ZERO
            List::class -> emptyList<Any>()
            Set::class -> emptySet<Any>()
            Map::class -> emptyMap<Any, Any>()
            else -> null
        }
    }


}