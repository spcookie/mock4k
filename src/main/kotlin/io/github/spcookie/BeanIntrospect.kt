package io.github.spcookie

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * 分析Bean属性并将其转换为MockEngine的Map结构
 *
 * @author spcookie
 * @since 1.2.0
 */
internal class BeanIntrospect(val containerAdapter: ContainerAdapter) {

    private val logger = LoggerFactory.getLogger(BeanIntrospect::class.java)

    /**
     * 分析Bean类并转换为MockEngine的MapList结构
     */
    fun <T : Any> analyzeBean(clazz: KClass<T>, config: BeanMockConfig): Map<String, Any?> {
        return analyzeBean(clazz, config, 0)
    }

    /**
     * 分析Bean类并转换为MockEngine的MapList结构，带深度跟踪
     */
    private fun <T : Any> analyzeBean(clazz: KClass<T>, config: BeanMockConfig, currentDepth: Int): Map<String, Any?> {

        // 检查深度限制以避免无限递归
        if (currentDepth > config.depth) {
            return emptyMap()
        }

        val result = mutableMapOf<String, Any?>()

        // 根据配置获取所有属性
        val properties = getEligibleProperties(clazz, config)

        for (property in properties) {
            try {
                // 首先尝试从构造函数参数获取注解，然后从属性获取
                val propertyAnnotation = getPropertyAnnotation(clazz, property)
                val propertyBeanAnnotation = getPropertyBeanAnnotation(clazz, property)

                // 如果属性被禁用则跳过
                if (propertyAnnotation?.enabled == false) {
                    continue
                }

                val key = buildPropertyKey(property, propertyAnnotation)
                val value = analyzePropertyType(
                    property.returnType,
                    propertyAnnotation,
                    propertyBeanAnnotation,
                    config,
                    currentDepth
                )

                result[key] = value
            } catch (e: Exception) {
                logger.warn("Failed to analyze property ${property.name}: ${e.message}")
            }
        }

        return result
    }

    /**
     * 从构造函数参数或属性本身获取属性注解
     */
    private fun <T : Any> getPropertyAnnotation(clazz: KClass<T>, property: KProperty<*>): Mock.Property? {
        // 首先尝试从构造函数参数获取注解
        val constructor = clazz.primaryConstructor
        if (constructor != null) {
            val parameter = constructor.parameters.find { it.name == property.name }
            if (parameter != null) {
                val paramAnnotation = parameter.findAnnotation<Mock.Property>()
                if (paramAnnotation != null) {
                    return paramAnnotation
                }
            }
        }

        // 回退到属性注解
        return property.findAnnotation<Mock.Property>()
    }

    /**
     * 从构造函数参数或属性本身获取属性级别的@Mock.Bean注解
     * 属性级别的注解优先级高于类级别的注解
     */
    private fun <T : Any> getPropertyBeanAnnotation(clazz: KClass<T>, property: KProperty<*>): Mock.Bean? {
        // 首先尝试从构造函数参数获取注解
        val constructor = clazz.primaryConstructor
        if (constructor != null) {
            val parameter = constructor.parameters.find { it.name == property.name }
            if (parameter != null) {
                val paramAnnotation = parameter.findAnnotation<Mock.Bean>()
                if (paramAnnotation != null) {
                    return paramAnnotation
                }
            }
        }

        // 回退到属性注解
        return property.findAnnotation<Mock.Bean>()
    }

    /**
     * 使用注解中的规则构建属性键
     * 规则优先级：step > count > range and decimal > range > decimal
     */
    private fun buildPropertyKey(property: KProperty<*>, annotation: Mock.Property?): String {
        val baseName = property.name
        val rule = annotation?.rule

        if (rule == null) {
            return baseName
        }

        // 优先级1：step规则（最高优先级）
        if (rule.step >= 0) {
            return "$baseName|+${rule.step}"
        }

        // 优先级2：count规则
        if (rule.count >= 0) {
            val countPart = rule.count.toString()
            val decimalPart = getDecimalPart(rule)
            return if (decimalPart != null) {
                "$baseName|$countPart.$decimalPart"
            } else {
                "$baseName|$countPart"
            }
        }

        // 优先级3：range和decimal
        if (rule.min >= 0 && rule.max >= 0) {
            val rangePart = "${rule.min}-${rule.max}"
            val decimalPart = getDecimalPart(rule)
            return if (decimalPart != null) {
                "$baseName|$rangePart.$decimalPart"
            } else {
                "$baseName|$rangePart"
            }
        }

        // 优先级4：仅decimal
        val decimalPart = getDecimalPart(rule)
        if (decimalPart != null) {
            return "$baseName|1.$decimalPart"
        }

        return baseName
    }

    /**
     * 从规则中提取小数部分
     */
    private fun getDecimalPart(rule: Mock.Rule): String? {
        return when {
            rule.dmin >= 0 && rule.dmax >= 0 -> "${rule.dmin}-${rule.dmax}"
            rule.dcount >= 0 -> rule.dcount.toString()
            else -> null
        }
    }

    /**
     * 分析属性类型并转换为适当的占位符或结构
     */
    internal fun analyzePropertyType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean? = null,
        config: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Any? {
        val kClass = type.classifier as? KClass<*> ?: return "@string"

        // 首先检查自定义占位符
        val placeholder = annotation?.placeholder
        if (placeholder != null && placeholder.value.isNotEmpty() && isBasicType(kClass)) {
            return placeholder.value
        }

        return when {
            // 基本类型
            isBasicType(kClass) -> getBasicTypePlaceholder(kClass)

            // 集合类型
            isCollectionType(kClass) -> analyzeCollectionType(
                type,
                annotation,
                propertyBeanAnnotation,
                config,
                currentDepth
            )

            // 容器对象
            isContainerType(kClass, containerAdapter) -> analyzeContainerType(
                type,
                annotation,
                propertyBeanAnnotation,
                config,
                currentDepth
            )

            // 枚举类型
            isEnumClass(kClass) -> analyzeEnumClass(kClass)

            // 自定义对象
            isCustomClass(kClass, containerAdapter) -> analyzeCustomObject(
                kClass,
                propertyBeanAnnotation,
                config,
                currentDepth
            )

            else -> "@string"
        }
    }

    private fun <T : Any> analyzeEnumClass(kClass: KClass<T>): Any? {
        val size = kClass.java.enumConstants.size
        return "@integer(0, $size)"
    }

    /**
     * 获取基本类型的占位符
     */
    private fun getBasicTypePlaceholder(kClass: KClass<*>): String {
        return when (kClass) {
            String::class -> "@string"
            Int::class, Integer::class -> "@integer"
            Long::class, java.lang.Long::class -> "@long"
            Float::class, java.lang.Float::class -> "@float"
            Double::class, java.lang.Double::class -> "@float"
            Boolean::class, java.lang.Boolean::class -> "@boolean"
            Char::class, Character::class -> "@character"
            Byte::class, java.lang.Byte::class -> "@integer"
            Short::class, java.lang.Short::class -> "@integer"
            BigDecimal::class -> "@float"
            BigInteger::class -> "@integer"
            UUID::class -> "@uuid"
            Date::class -> "@date"
            java.sql.Date::class -> "@date"
            java.sql.Time::class -> "@time"
            java.sql.Timestamp::class -> "@datetime"
            java.time.LocalDate::class -> "@date"
            java.time.LocalTime::class -> "@time"
            java.time.LocalDateTime::class -> "@datetime"
            java.time.ZonedDateTime::class -> "@datetime"
            java.time.OffsetDateTime::class -> "@datetime"
            java.time.Instant::class -> "@datetime"
            else -> "@string"
        }
    }


    /**
     * 分析集合类型并返回适当的结构
     */
    private fun analyzeCollectionType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean? = null,
        config: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Any {
        val kClass = type.classifier as KClass<*>
        val length = annotation?.length?.value ?: 1
        val fill = annotation?.length?.fill ?: Mock.Fill.RANDOM

        return when {
            List::class.java.isAssignableFrom(kClass.java)
                    || kClass.java.isArray
                    || Set::class.java.isAssignableFrom(kClass.java) -> {
                val elementType = type.arguments.firstOrNull()?.type

                when (fill) {
                    Mock.Fill.REPEAT -> {
                        // REPEAT: 对所有位置使用相同的元素
                        val elementValue = if (elementType != null) {
                            analyzePropertyType(elementType, null, propertyBeanAnnotation, config, currentDepth)
                        } else {
                            "@string"
                        }
                        List(length) { elementValue }
                    }

                    Mock.Fill.RANDOM -> {
                        // RANDOM: 为每个位置生成不同的元素
                        List(length) {
                            if (elementType != null) {
                                analyzePropertyType(elementType, null, propertyBeanAnnotation, config, currentDepth)
                            } else {
                                "@string"
                            }
                        }
                    }
                }
            }

            Map::class.java.isAssignableFrom(kClass.java) -> {
                val keyType = type.arguments.getOrNull(0)?.type
                val valueType = type.arguments.getOrNull(1)?.type

                // 根据长度生成随机键值对
                (1..length).associate {
                    val keyValue = if (keyType != null) {
                        val keyClass = keyType.classifier as? KClass<*>
                        if (keyClass == String::class) {
                            "@string"
                        } else {
                            val propertyType = analyzePropertyType(
                                keyType,
                                null,
                                propertyBeanAnnotation,
                                config,
                                currentDepth
                            )
                            Mson.stringify(propertyType)
                        }
                    } else {
                        "@string"
                    }

                    val valueValue = if (valueType != null) {
                        analyzePropertyType(valueType, null, propertyBeanAnnotation, config, currentDepth)
                    } else {
                        "@string"
                    }

                    keyValue to valueValue
                }
            }

            else -> emptyList<Any>()
        }
    }

    /**
     * 递归分析自定义对象
     */
    private fun analyzeCustomObject(
        kClass: KClass<*>,
        propertyBeanAnnotation: Mock.Bean? = null,
        parentConfig: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Map<String, Any?> {
        return try {
            // 属性级别的@Mock.Bean注解优先级高于类级别的注解
            val effectiveBeanAnnotation = propertyBeanAnnotation ?: kClass.findAnnotation<Mock.Bean>()
            val config = if (effectiveBeanAnnotation != null) {
                // 如果存在注解配置则使用（属性级别优先）
                BeanMockConfig(
                    includePrivate = effectiveBeanAnnotation.includePrivate,
                    includeStatic = effectiveBeanAnnotation.includeStatic,
                    includeTransient = effectiveBeanAnnotation.includeTransient,
                    depth = effectiveBeanAnnotation.depth
                )
            } else {
                // 如果没有注解则使用父配置
                parentConfig
            }
            analyzeBean(kClass, config, currentDepth + 1)
        } catch (e: Exception) {
            logger.warn("Failed to analyze custom object ${kClass.simpleName}: ${e.message}")
            mapOf("value" to "@string")
        }
    }

    /**
     * 使用ContainerAdapter分析容器类型
     */
    private fun analyzeContainerType(
        type: KType,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean? = null,
        config: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Any? {
        return containerAdapter.analyzeContainerType(
            type,
            annotation,
            propertyBeanAnnotation,
            config,
            currentDepth
        ) { wrappedType, ann, beanAnn, cfg, depth ->
            analyzeWrappedType(wrappedType, ann, beanAnn, cfg, depth)
        }
    }

    /**
     * 分析包装类型，如果失败则回退到默认值
     */
    private fun analyzeWrappedType(
        wrappedType: KType?,
        annotation: Mock.Property?,
        propertyBeanAnnotation: Mock.Bean?,
        config: BeanMockConfig = BeanMockConfig(),
        currentDepth: Int = 0
    ): Any? {
        return if (wrappedType != null) {
            analyzePropertyType(wrappedType, annotation, propertyBeanAnnotation, config, currentDepth)
        } else {
            "@string"
        }
    }
}