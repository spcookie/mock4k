package io.github.spcookie

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * Maps generated mock data back to Bean instances
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
        logger.debug("Mapping data to bean class: ${clazz.simpleName}")

        return try {
            // Try constructor-based creation first
            createInstanceWithConstructor(clazz, data, config)
        } catch (e: Exception) {
            logger.debug("Constructor-based creation failed, trying property-based creation: ${e.message}")
            // Fallback to property-based creation
            createInstanceWithProperties(clazz, data, config)
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

        val args = constructor.parameters.map { param ->
            val propertyName = param.name ?: throw IllegalArgumentException("Parameter name not available")
            val rawValue = findValueForProperty(propertyName, data)

            if (rawValue != null) {
                convertValue(rawValue, param.type, config)
            } else {
                // Use default value if available
                if (param.isOptional) {
                    null
                } else {
                    getDefaultValueForType(param.type)
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

        // Get all mutable properties
        val mutableProperties = clazz.memberProperties.filterIsInstance<KMutableProperty<*>>()

        for (property in mutableProperties) {
            try {
                val propertyAnnotation = property.findAnnotation<Mock.Property>()

                // Skip if property is disabled
                if (propertyAnnotation?.enabled == false) {
                    continue
                }

                // Check if property should be included based on config
                if (!shouldIncludeProperty(property, config)) {
                    continue
                }

                val rawValue = findValueForProperty(property.name, data)
                if (rawValue != null) {
                    val convertedValue = convertValue(rawValue, property.returnType, config)
                    setPropertyValue(instance, property, convertedValue)
                    logger.debug("Set property ${property.name} = $convertedValue")
                }
            } catch (e: Exception) {
                logger.warn("Failed to set property ${property.name}: ${e.message}")
            }
        }

        return instance
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
            // Handle Pair type specially
            targetClass == Pair::class -> {
                when (value) {
                    is String -> {
                        // Parse from string like "first,second" or use default
                        val parts = value.split(",")
                        if (parts.size >= 2) {
                            Pair(parts[0].trim(), parts[1].trim())
                        } else {
                            Pair(value, "")
                        }
                    }

                    is Pair<*, *> -> value
                    else -> Pair(value.toString(), "")
                }
            }

            // Handle basic types with TypeAdapter
            isBasicType(targetClass) -> {
                // If value is a placeholder string with rules, generate actual value first
                val actualValue = if (value is String && value.startsWith("@")) {
                    val mockEngine = MockEngine()
                    mockEngine.generate(value)
                } else {
                    value
                }

                val adapter = typeAdapter.get(targetClass)
                adapter?.invoke(actualValue) ?: actualValue
            }

            // Handle collections
            isCollectionType(targetClass) -> convertCollectionValue(value, targetType, config)

            // Handle container types
            isContainerType(targetClass) -> convertContainerValue(value, targetType, config)

            // Handle custom objects
            isCustomClass(targetClass) -> convertCustomObjectValue(value, targetClass, config)

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
                    is List<*> -> {
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
                    is List<*> -> {
                        if (elementType != null) {
                            value.map { convertValue(it, elementType, config) }.toSet()
                        } else {
                            value.toSet()
                        }
                    }

                    is Set<*> -> value
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
     * Convert container values (Optional, CompletableFuture, Future, Callable, Supplier, Lazy, Deferred, Reactor, RxJava, Vavr, Arrow, etc.)
     */
    private fun convertContainerValue(value: Any?, targetType: KType, config: BeanMockConfig): Any? {
        if (value == null) return null

        val targetClass = targetType.classifier as KClass<*>
        val qualifiedName = targetClass.qualifiedName ?: ""
        val wrappedType = targetType.arguments.firstOrNull()?.type

        return when {
            // Java standard container types
            Optional::class.java.isAssignableFrom(targetClass.java) -> {
                val wrappedValue = convertWrappedValue(value, wrappedType, config)
                Optional.ofNullable(wrappedValue)
            }

            CompletableFuture::class.java.isAssignableFrom(targetClass.java) ||
                    java.util.concurrent.Future::class.java.isAssignableFrom(targetClass.java) -> {
                val wrappedValue = convertWrappedValue(value, wrappedType, config)
                CompletableFuture.completedFuture(wrappedValue)
            }

            java.util.concurrent.Callable::class.java.isAssignableFrom(targetClass.java) -> {
                val wrappedValue = convertWrappedValue(value, wrappedType, config)
                java.util.concurrent.Callable { wrappedValue }
            }

            java.util.function.Supplier::class.java.isAssignableFrom(targetClass.java) -> {
                val wrappedValue = convertWrappedValue(value, wrappedType, config)
                java.util.function.Supplier { wrappedValue }
            }

            kotlin.Lazy::class.java.isAssignableFrom(targetClass.java) -> {
                val wrappedValue = convertWrappedValue(value, wrappedType, config)
                lazy { wrappedValue }
            }

            // Third-party library container types (using string comparison to avoid dependency issues)
            // For these types, we return the converted value directly since we can't create instances without dependencies
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
                    qualifiedName.startsWith("arrow.fx.coroutines.Resource") -> {
                convertWrappedValue(value, wrappedType, config)
            }

            // Stream types - return as list
            qualifiedName.startsWith("reactor.core.publisher.Flux") ||
                    qualifiedName.startsWith("io.reactivex.Observable") ||
                    qualifiedName.startsWith("io.reactivex.rxjava3.core.Observable") ||
                    qualifiedName.startsWith("io.reactivex.Flowable") ||
                    qualifiedName.startsWith("io.reactivex.rxjava3.core.Flowable") -> {
                when (value) {
                    is List<*> -> value.map { convertWrappedValue(it, wrappedType, config) }
                    else -> listOf(convertWrappedValue(value, wrappedType, config))
                }
            }

            // Either-like types - use right/second type parameter
            qualifiedName.startsWith("io.vavr.control.Either") ||
                    qualifiedName.startsWith("io.vavr.control.Validation") ||
                    qualifiedName.startsWith("arrow.core.Either") -> {
                val rightType = targetType.arguments.getOrNull(1)?.type
                convertWrappedValue(value, rightType, config)
            }

            // Completable types - return null
            qualifiedName.startsWith("io.reactivex.Completable") ||
                    qualifiedName.startsWith("io.reactivex.rxjava3.core.Completable") -> {
                null
            }

            else -> value
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
    private fun setPropertyValue(instance: Any, property: KMutableProperty<*>, value: Any?) {
        try {
            property.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            (property as KMutableProperty<Any?>).setter.call(instance, value)
        } catch (e: Exception) {
            logger.warn("Failed to set property ${property.name}: ${e.message}")
            throw e
        }
    }

    /**
     * Get default value for type
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


    /**
     * Check if type is a collection type
     */
    private fun isCollectionType(kClass: KClass<*>): Boolean {
        return when {
            List::class.java.isAssignableFrom(kClass.java) -> true
            Set::class.java.isAssignableFrom(kClass.java) -> true
            Map::class.java.isAssignableFrom(kClass.java) -> true
            kClass.java.isArray -> true
            else -> false
        }
    }

    /**
     * Check if type is a custom class (Category 3)
     */
    private fun isCustomClass(kClass: KClass<*>): Boolean {
        return when {
            isBasicType(kClass) -> false
            isCollectionType(kClass) -> false
            isContainerType(kClass) -> false
            kClass.java.isEnum -> false
            kClass.java.isPrimitive -> false
            kClass.java.isInterface -> false
            kClass.java.isArray -> false
            kClass.isAbstract -> false
            kClass.isSealed -> false
            kClass == Any::class -> false
            else -> true
        }
    }

    /**
     * Check if type is a container type
     */
    private fun isContainerType(kClass: KClass<*>): Boolean {
        return when {
            // Java standard container types
            Optional::class.java.isAssignableFrom(kClass.java) -> true
            CompletableFuture::class.java.isAssignableFrom(kClass.java) -> true
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
}