package io.github.spcookie

import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.function.Supplier
import java.util.stream.Stream
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * 常用操作的实用函数
 *
 * @author spcookie
 * @since 1.2.0
 */

/**
 * 检查类型是否为基本类型
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
        UUID::class -> true
        else -> isDateTimeType(kClass)
    }
}

/**
 * 检查类型是否为 Kotlin 或 Java 基本类型（包括包装类型）
 */
fun isPrimitiveType(kClass: KClass<*>): Boolean {
    return when (kClass) {
        // Kotlin 基本类型及其 Java 包装类型等效项
        String::class -> true
        Int::class, Integer::class -> true
        Long::class, java.lang.Long::class -> true
        Float::class, java.lang.Float::class -> true
        Double::class, java.lang.Double::class -> true
        Boolean::class, java.lang.Boolean::class -> true
        Char::class, Character::class -> true
        Byte::class, java.lang.Byte::class -> true
        Short::class, java.lang.Short::class -> true
        else -> false
    }
}

/**
 * 检查类是否为日期/时间类型（包括旧式和 Java 8+ 类型）
 */
fun isDateTimeType(type: KClass<*>): Boolean {
    return when (type) {
        // 旧式日期/时间类型（Java 8 之前）
        Date::class -> true
        java.sql.Date::class -> true
        java.sql.Time::class -> true
        java.sql.Timestamp::class -> true
        Calendar::class -> true
        // Java 8+ 日期/时间类型
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
 * 检查类型是否为容器类型
 */
fun isContainerType(kClass: KClass<*>, containerAdapter: ContainerAdapter): Boolean {
    // 首先检查 Java 标准类型
    when {
        Optional::class.java.isAssignableFrom(kClass.java) -> return true
        Stream::class.java.isAssignableFrom(kClass.java) -> return true
        Future::class.java.isAssignableFrom(kClass.java) -> return true
        Callable::class.java.isAssignableFrom(kClass.java) -> return true
        Supplier::class.java.isAssignableFrom(kClass.java) -> return true
        Lazy::class.java.isAssignableFrom(kClass.java) -> return true
    }

    // 然后检查已注册的第三方类型
    val qualifiedName = kClass.qualifiedName ?: return false
    return containerAdapter.getRegisteredPrefixes().any { qualifiedName.startsWith(it) }
}

/**
 * 检查类型是否为集合类型
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
 * 检查类是否为自定义类（非基本类型）
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

/**
 * 检查类型是否为枚举类
 */
fun isEnumClass(type: KClass<*>): Boolean {
    return type.java.isEnum
}

/**
 * 根据配置获取符合条件的模拟属性
 */
fun getEligibleProperties(clazz: KClass<*>, config: BeanMockConfig): List<KProperty<*>> {
    val constructor = clazz.primaryConstructor
    val isDataClass = clazz.isData
    val isJavaRecord = clazz.java.isRecord

    val properties = clazz.memberProperties.filter { property ->
        val javaField = property.javaField

        // 对于 Kotlin 数据类，应包括主构造函数中的所有 val 属性
        val isDataClassProperty = isDataClass && constructor?.parameters?.any { it.name == property.name } == true

        // 对于 Java 记录，应包括所有属性（它们没有 javaField）
        val isRecordProperty = isJavaRecord && javaField == null

        // 对于 Kotlin 属性，检查它们是否是可变的 (var) 而不是字段可见性
        val isMutableProperty = property is KMutableProperty<*>

        // 对于 Kotlin 属性，检查它是否实际上是私有属性
        val isKotlinPrivateProperty = property.visibility == KVisibility.PRIVATE

        // 根据配置检查是否应包括属性
        when {
            // 数据类和记录属性的特殊处理
            isDataClassProperty || isRecordProperty -> {
                // 对于数据类和记录，检查隐私设置
                if (!config.includePrivate && isKotlinPrivateProperty) {
                    false
                } else {
                    // 检查是否存在并启用 Mock 注解
                    val mockParam = property.findAnnotation<Mock.Property>()
                    val javaFieldAnnotation = javaField?.getAnnotation(Mock.Property::class.java)

                    // 尝试从构造函数参数中获取注解
                    getConstructorAnnotation(constructor, property, mockParam, javaFieldAnnotation)
                }
            }

            // 常规属性处理
            javaField == null -> {
                false
            }

            !isMutableProperty -> {
                false
            }

            !config.includePrivate && isKotlinPrivateProperty -> {
                false
            }

            !config.includeStatic && Modifier.isStatic(javaField.modifiers) -> {
                false
            }

            !config.includeTransient && Modifier.isTransient(javaField.modifiers) -> {
                false
            }

            else -> {
                // 检查是否存在并启用 Mock 注解
                val mockParam = property.findAnnotation<Mock.Property>()
                val javaFieldAnnotation = javaField.getAnnotation(Mock.Property::class.java)

                // 尝试从构造函数参数中获取注解
                getConstructorAnnotation(constructor, property, mockParam, javaFieldAnnotation)
            }
        }
    }
    return properties
}

private fun getConstructorAnnotation(
    constructor: KFunction<Any>?,
    property: KProperty1<out Any, *>,
    mockParam: Mock.Property?,
    javaFieldAnnotation: Mock.Property?
): Boolean {
    val constructorParam = constructor?.parameters?.find { it.name == property.name }
    val constructorAnnotation = constructorParam?.findAnnotation<Mock.Property>()

    val finalAnnotation = mockParam ?: javaFieldAnnotation ?: constructorAnnotation
    val enabled = finalAnnotation?.enabled != false
    return enabled
}