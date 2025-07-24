package io.github.spcookie

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

/**
 * 将生成的模拟数据映射回 Bean 实例
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
     * 将生成的数据映射到 Bean 实例
     */
    fun <T : Any> mapToBean(clazz: KClass<T>, data: Map<String, Any?>, config: BeanMockConfig): T {
        val instance = createInstanceWithConstructor(clazz, data, config)
        mapRemainingProperties(instance, clazz, data, config)
        return instance
    }

    /**
     * 使用主构造函数或Java构造函数创建实例
     */
    private fun <T : Any> createInstanceWithConstructor(
        clazz: KClass<T>,
        data: Map<String, Any?>,
        config: BeanMockConfig
    ): T {
        val constructor = clazz.primaryConstructor

        // 如果有主构造函数，使用主构造函数创建实例
        if (constructor != null) {
            val args = constructor.parameters
                .filter { it.findAnnotation<Mock.Property>()?.enabled != false }
                .map { param ->
                    val propertyName = param.name ?: throw IllegalArgumentException("Parameter name not available")

                    val rawValue = findValueForProperty(propertyName, data)

                    if (rawValue != null) {
                        convertValue(rawValue, param.type, config)
                    } else {
                        // 如果有默认值，则使用默认值
                        if (param.isOptional) {
                            null
                        } else {
                            generateValueForType(param.type, config)
                        }
                    }
                }.toTypedArray()

            return constructor.call(*args)
        } else {
            // 如果没有主构造函数，直接使用Java类的第一个构造函数
            return createInstanceWithJavaConstructor(clazz, data, config)
        }
    }

    /**
     * 使用Java类的第一个构造函数创建实例
     */
    private fun <T : Any> createInstanceWithJavaConstructor(
        clazz: KClass<T>,
        data: Map<String, Any?>,
        config: BeanMockConfig
    ): T {
        val javaClass = clazz.java
        val constructors = javaClass.constructors.sortedByDescending { it.parameterCount }

        // 直接使用第一个构造函数
        val firstConstructor = constructors.firstOrNull()
            ?: throw IllegalArgumentException("No constructor found for ${clazz.simpleName}")

        try {
            val args = firstConstructor.parameters.mapIndexed { index, param ->
                // 尝试通过参数名或索引查找值
                val parameterName = param.name ?: "arg$index"
                val rawValue = findValueForProperty(parameterName, data)

                if (rawValue != null) {
                    // 如果在data中找到值，进行类型转换
                    val paramType = param.type.kotlin.createType()
                    convertValue(rawValue, paramType, config)
                } else {
                    // 如果没有找到值，生成默认值
                    val paramType = param.type.kotlin.createType()
                    generateValueForType(paramType, config)
                }
            }.toTypedArray()

            @Suppress("UNCHECKED_CAST")
            return firstConstructor.newInstance(*args) as T
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to create instance using first constructor for ${clazz.simpleName}: ${e.message}",
                e
            )
        }
    }

    /**
     * 映射构造函数未处理的剩余属性
     */
    private fun <T : Any> mapRemainingProperties(
        instance: T,
        clazz: KClass<T>,
        data: Map<String, Any?>,
        config: BeanMockConfig
    ) {
        val constructor = clazz.primaryConstructor
        val constructorParamNames = constructor?.parameters?.mapNotNull { it.name }?.toSet() ?: emptySet()

        // 获取构造函数未映射的属性
        val eligibleProperties = getEligibleProperties(clazz, config)
        val unmappedProperties = eligibleProperties.filter { property ->
            // 仅包括构造函数未处理的属性
            property.name !in constructorParamNames
        }

        mapPropertiesToInstance(instance, unmappedProperties, data, config)
    }

    /**
     * 将特定属性映射到实例
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
     * 在生成的数据中查找属性值，处理基于规则的键
     */
    private fun findValueForProperty(propertyName: String, data: Map<String, Any?>): Any? {
        // 首先尝试精确匹配
        data[propertyName]?.let { return it }

        // 然后尝试查找以属性名开头后跟 | 的键
        val ruleBasedKey = data.keys.find { it.startsWith("$propertyName|") }
        if (ruleBasedKey != null) {
            return data[ruleBasedKey]
        }

        return null
    }

    /**
     * 使用 typeAdapter 将值转换为目标类型
     */
    internal fun convertValue(value: Any?, targetType: KType, config: BeanMockConfig): Any? {
        if (value == null) return null

        val targetClass = targetType.classifier as? KClass<*> ?: return value

        return when {
            // 使用 typeAdapter 处理基本类型
            isBasicType(targetClass) -> {
                val adapter = typeAdapter.get(targetClass)
                adapter?.invoke(value) ?: value
            }

            // 处理集合
            isCollectionType(targetClass) -> convertCollectionValue(value, targetType, config)

            // 处理容器类型
            isContainerType(targetClass, containerAdapter) -> convertContainerValue(value, targetType, config)

            // 处理枚举
            isEnumClass(targetClass) -> convertEnumValue(value, targetClass)

            // 处理自定义对象
            isCustomClass(targetClass, containerAdapter) -> convertCustomObjectValue(value, targetClass, config)

            else -> value
        }
    }

    private fun convertEnumValue(value: Any, targetClass: KClass<*>): Any? {
        return when (value) {
            is String -> try {
                targetClass.java.enumConstants.getOrNull(value.toInt())
            } catch (_: NumberFormatException) {
                null
            }

            is Int -> targetClass.java.enumConstants.getOrNull(value)
            else -> null
        }
    }

    /**
     * 转换集合值
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
     * 使用 containerAdapter 转换容器值
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
     * 转换包装值，回退到原始值
     */
    private fun convertWrappedValue(value: Any?, wrappedType: KType?, config: BeanMockConfig): Any? {
        return if (wrappedType != null) {
            convertValue(value, wrappedType, config)
        } else {
            value
        }
    }

    /**
     * 转换自定义对象值
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
                // 如果可用，尝试使用 typeAdapter
                val adapter = typeAdapter.get(targetClass)
                adapter?.invoke(value) ?: value
            }
        }
    }

    /**
     * 在实例上设置属性值
     */
    private fun setPropertyValue(instance: Any, property: KProperty<*>, value: Any?) {
        property.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (property as KMutableProperty<Any?>).setter.call(instance, value)
    }

    /**
     * 为类型生成适当的值，包括递归对象创建
     */
    private fun generateValueForType(type: KType, config: BeanMockConfig): Any? {
        val kClass = type.classifier as? KClass<*> ?: return null

        return when {
            // 处理基本类型和集合
            isBasicType(kClass) || isCollectionType(kClass) -> getDefaultValueForType(type)

            // 处理容器类型
            isContainerType(kClass, containerAdapter) -> {
                // 对于容器类型，尝试使用默认包装值创建
                val wrappedType = type.arguments.firstOrNull()?.type
                if (wrappedType != null && config.depth > 0) {
                    val reducedConfig = config.copy(depth = config.depth - 1)
                    val wrappedValue = generateValueForType(wrappedType, reducedConfig)
                    convertContainerValue(wrappedValue, type, reducedConfig)
                } else {
                    null
                }
            }

            // 处理自定义对象 - 递归创建它们
            isCustomClass(kClass, containerAdapter) -> {
                // 检查深度以防止无限递归
                if (config.depth <= 0) {
                    return null
                }

                try {
                    // 使用空数据和减少的深度递归创建自定义对象
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
     * 获取基本类型的默认值
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