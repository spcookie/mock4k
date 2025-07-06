package io.github.spcookie

import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField
import io.github.spcookie.Mock.Property as MockProperty

/**
 * Analyzes Bean properties and converts them to Map structure for MockEngine
 *
 * @author spcookie
 * @since 1.2.0
 */
internal class BeanIntrospect {

    private val logger = LoggerFactory.getLogger(BeanIntrospect::class.java)

    /**
     * Analyze Bean class and convert to Map structure
     */
    fun <T : Any> analyzeBean(clazz: KClass<T>, config: BeanMockConfig): Map<String, Any?> {
        val properties = getEligibleProperties(clazz, config)
        val result = mutableMapOf<String, Any?>()

        properties.forEach { property ->
            try {
                val propertyKey = buildPropertyKey(property, clazz)
                val propertyValue = buildPropertyValue(property, config, clazz)
                result[propertyKey] = propertyValue
            } catch (e: Exception) {
                logger.warn("Failed to analyze property ${property.name}: ${e.message}", e)
            }
        }

        return result
    }

    /**
     * Get eligible properties for mocking based on configuration
     */
    private fun getEligibleProperties(clazz: KClass<*>, config: BeanMockConfig): List<KProperty<*>> {

        // Also check constructor parameters for annotations
        val constructor = clazz.primaryConstructor

        val properties = clazz.memberProperties.filter { property ->
            val javaField = property.javaField
            
            // For Kotlin properties, check if they are mutable (var) rather than field visibility
            val isMutableProperty = property is KMutableProperty<*>
            
            // For Kotlin properties, check if it's actually a private property
            val isKotlinPrivateProperty = property.visibility == KVisibility.PRIVATE

            // Check if property should be included based on configuration
            when {
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
                    // Check if Mock annotation is present and enabled
                    val mockParam = property.findAnnotation<MockProperty>()
                    val javaFieldAnnotation = javaField.getAnnotation(MockProperty::class.java)
                    
                    // Try to get annotation from constructor parameter
                    val constructorParam = constructor?.parameters?.find { it.name == property.name }
                    val constructorAnnotation = constructorParam?.findAnnotation<MockProperty>()

                    val finalAnnotation = mockParam ?: javaFieldAnnotation ?: constructorAnnotation
                    val enabled = finalAnnotation?.enabled != false
                    enabled
                }
            }
        }
        return properties
    }

    /**
     * Get Mock.Property annotation from property, field, or constructor parameter
     */
    private fun getMockPropertyAnnotation(property: KProperty<*>, clazz: KClass<*>): MockProperty? {
        val mockParam = property.findAnnotation<MockProperty>()
        val javaFieldAnnotation = property.javaField?.getAnnotation(MockProperty::class.java)
        
        // Try to get annotation from constructor parameter
        val constructor = clazz.primaryConstructor
        val constructorParam = constructor?.parameters?.find { it.name == property.name }
        val constructorAnnotation = constructorParam?.findAnnotation<MockProperty>()
        
        return mockParam ?: javaFieldAnnotation ?: constructorAnnotation
    }

    /**
     * Build property key with rules if Mock.Property annotation is present
     */
    private fun buildPropertyKey(property: KProperty<*>, clazz: KClass<*>): String {
        val finalAnnotation = getMockPropertyAnnotation(property, clazz)
        val propertyName = property.name
        val propertyType = property.returnType
        val propertyClass = propertyType.classifier as? KClass<*>

        if (finalAnnotation != null && finalAnnotation.enabled) {
            val rule = finalAnnotation.rule
            
            // Only apply rule to key for collection types (List, Set, Map)
            // For basic types, rule should be applied to value, not key
            if (hasValidRule(rule) && isCollectionType(propertyClass)) {
                val ruleString = buildRuleString(rule)
                val keyWithRule = "$propertyName|$ruleString"
                return keyWithRule
            }
        }
        
        return propertyName
    }

    /**
     * Build property value based on type and annotations
     */
    private fun buildPropertyValue(property: KProperty<*>, config: BeanMockConfig, clazz: KClass<*>): Any? {
        val propertyType = property.returnType
        val propertyClass = propertyType.classifier as? KClass<*> ?: return null
        val finalAnnotation = getMockPropertyAnnotation(property, clazz)

        if (finalAnnotation != null && finalAnnotation.enabled) {
            val rule = finalAnnotation.rule
            val placeholder = finalAnnotation.placeholder
            
            // Priority 1: If placeholder is specified and property type supports placeholders, use it
            if (placeholder.value.isNotEmpty() && isPlaceholderSupportedType(propertyClass)) {
                val placeholderValue = buildPlaceholderValue(finalAnnotation.placeholder)
                return placeholderValue
            }
            
            // Priority 2: If rule is specified, build rule-based value
            if (hasValidRule(rule)) {
                val ruleBasedValue = buildRuleBasedValue(rule, propertyClass, propertyType, config)
                return ruleBasedValue
            }
        }
        
        // Priority 3: For properties without MockParam annotation or disabled, use buildValueByType
        return buildValueByType(propertyClass, propertyType, config)
    }

    /**
     * Check if property type supports placeholder
     */
    private fun isPlaceholderSupportedType(propertyClass: KClass<*>?): Boolean {
        return when (propertyClass) {
            String::class,
            Int::class, Integer::class,
            Long::class, java.lang.Long::class,
            Float::class, java.lang.Float::class,
            Double::class, java.lang.Double::class,
            Boolean::class, java.lang.Boolean::class -> true
            else -> false
        }
    }

    /**
     * Build placeholder value
     */
    private fun buildPlaceholderValue(placeholder: MockProperty.Placeholder): String {
        return if (placeholder.value.isNotEmpty()) {
            // Handle placeholder value - if it already starts with @, use as is, otherwise add @
            if (placeholder.value.startsWith("@")) {
                placeholder.value
            } else {
                "@${placeholder.value.uppercase()}"
            }
        } else {
            "@WORD" // Default placeholder
        }
    }
    
    /**
     * Check if rule has valid values
     */
    private fun hasValidRule(rule: MockProperty.Rule): Boolean {
        return rule.count != -1 || rule.min != -1 || rule.max != -1 || 
               rule.step != -1 || rule.dmin != -1 || rule.dmax != -1 || rule.dcount != -1
    }
    
    /**
     * Build rule-based value
     */
    private fun buildRuleBasedValue(rule: MockProperty.Rule, propertyClass: KClass<*>, propertyType: KType, config: BeanMockConfig): Any? {
        // For collection types, the rule is applied to the key (handled in buildPropertyKey)
        // Here we just build the template structure
        return when (propertyClass) {
            List::class, MutableList::class -> {
                buildRuleBasedList(rule, propertyType, config)
            }
            Set::class, MutableSet::class -> {
                buildRuleBasedSet(rule, propertyType, config)
            }
            Map::class, MutableMap::class -> {
                buildRuleBasedMap(rule, propertyType, config)
            }
            else -> {
                // Check if it's a custom class (nested object)
                if (isCustomClass(propertyClass)) {
                    // For nested objects, analyze the bean structure
                    analyzeBean(propertyClass, config)
                } else {
                    // For basic types, generate template with rule
                    buildRuleTemplate(rule, propertyClass)
                }
            }
        }
    }
    
    /**
     * Build rule-based list
     */
    private fun buildRuleBasedList(rule: MockProperty.Rule, kType: KType, config: BeanMockConfig): List<Any?> {
        val elementTemplate = extractCollectionElementTemplate(kType, config) ?: return emptyList()
        
        // Return a list with one template element
        // The rule is applied to the property key, MockEngine will handle the repetition
        return listOf(elementTemplate)
    }
    
    /**
     * Build rule-based set
     */
    private fun buildRuleBasedSet(rule: MockProperty.Rule, kType: KType, config: BeanMockConfig): List<Any?> {
        // Use same structure as list for Set, MockEngine will convert to Set
        return buildRuleBasedList(rule, kType, config)
    }
    
    /**
     * Build rule-based map
     */
    private fun buildRuleBasedMap(rule: MockProperty.Rule, kType: KType, config: BeanMockConfig): Map<String, Any?> {
        val (keyTemplate, valueTemplate) = extractMapTemplates(kType, config) ?: return emptyMap()
        
        // For Map, use default key pattern since rule is applied to property key
        val mapTemplate = mapOf(
            "key|1-3" to keyTemplate,
            "value|1-3" to valueTemplate
        )
        
        return mapTemplate
    }
    
    /**
     * Build rule template for basic types
     */
    private fun buildRuleTemplate(rule: MockProperty.Rule, propertyClass: KClass<*>): String {
        val baseTemplate = getDefaultTemplateValue(propertyClass)
        val ruleString = buildRuleString(rule)
        
        return if (ruleString.isNotEmpty()) {
            when (propertyClass) {
                Int::class, Integer::class, Long::class, java.lang.Long::class -> {
                    when {
                        rule.min != -1 && rule.max != -1 -> "@natural(${rule.min},${rule.max})"
                        rule.count != -1 -> rule.count.toString()
                        rule.step != -1 -> "@increment(${rule.step})"
                        else -> baseTemplate
                    }
                }
                Float::class, java.lang.Float::class, Double::class, java.lang.Double::class -> {
                    when {
                        rule.min != -1 && rule.max != -1 -> {
                            val dPart = when {
                                rule.dcount != -1 -> ".${rule.dcount}"
                                rule.dmin != -1 && rule.dmax != -1 -> ".${rule.dmin}-${rule.dmax}"
                                else -> ""
                            }
                            "@float(${rule.min}.0,${rule.max}.0$dPart)"
                        }
                        rule.count != -1 -> rule.count.toString()
                        else -> baseTemplate
                    }
                }
                String::class -> {
                    when {
                        rule.count != -1 -> "@string(${rule.count})"
                        rule.min != -1 && rule.max != -1 -> "@string(${rule.min},${rule.max})"
                        else -> baseTemplate
                    }
                }
                Boolean::class, java.lang.Boolean::class -> {
                    if (rule.count != -1) {
                        if (rule.count == 1) "true" else "false"
                    } else baseTemplate
                }
                java.math.BigInteger::class -> {
                    when {
                        rule.min != -1 && rule.max != -1 -> "@natural(${rule.min},${rule.max})"
                        rule.count != -1 -> rule.count.toString()
                        rule.step != -1 -> "@increment(${rule.step})"
                        else -> baseTemplate
                    }
                }
                java.math.BigDecimal::class -> {
                    when {
                        rule.min != -1 && rule.max != -1 -> {
                            val dPart = when {
                                rule.dcount != -1 -> ".${rule.dcount}"
                                rule.dmin != -1 && rule.dmax != -1 -> ".${rule.dmin}-${rule.dmax}"
                                else -> ""
                            }
                            "@float(${rule.min}.0,${rule.max}.0$dPart)"
                        }
                        rule.count != -1 -> rule.count.toString()
                        else -> baseTemplate
                    }
                }
                else -> baseTemplate
            }
        } else {
            baseTemplate
        }
    }

    /**
     * Build value by property type
     */
    private fun buildValueByType(type: KClass<*>, kType: KType, config: BeanMockConfig): Any? {
        return when {
            // Handle List types
            type == List::class || type == MutableList::class -> {
                buildListValue(kType, config)
            }
            // Handle Set types
            type == Set::class || type == MutableSet::class -> {
                buildSetValue(kType, config)
            }
            // Handle Map types
            type == Map::class || type == MutableMap::class -> {
                buildMapValue(kType, config)
            }
            // Handle custom classes (potential nested beans)
            isCustomClass(type) -> {
                analyzeBean(type, config)
            }
            // Handle basic types
            else -> {
                getDefaultTemplateValue(type)
            }
        }
    }

    /**
     * Build List value structure
     */
    private fun buildListValue(kType: KType, config: BeanMockConfig): List<Any?> {
        val elementTemplate = extractCollectionElementTemplate(kType, config) ?: return emptyList()
        
        // Create a template list with one element
        return listOf(elementTemplate)
    }

    /**
     * Build Set value structure
     */
    private fun buildSetValue(kType: KType, config: BeanMockConfig): List<Any?> {
        // Use List structure for Set, MockEngine will handle the generation
        return buildListValue(kType, config)
    }

    /**
     * Build Map value structure
     */
    private fun buildMapValue(kType: KType, config: BeanMockConfig): Map<String, Any?> {
        val (keyTemplate, valueTemplate) = extractMapTemplates(kType, config) ?: return emptyMap()
        
        // Use MockEngine's map generation pattern
        return mapOf(
            "key|1-3" to keyTemplate,
            "value|1-3" to valueTemplate
        )
    }

    /**
     * Check if a class is a collection type
     */
    private fun isCollectionType(type: KClass<*>?): Boolean {
        return type != null && (
            type == List::class || type == MutableList::class ||
            type == Set::class || type == MutableSet::class ||
            type == Map::class || type == MutableMap::class
        )
    }
    
    /**
     * Build rule string from rule annotation
     */
    private fun buildRuleString(rule: MockProperty.Rule): String {
        return when {
            rule.count != -1 -> rule.count.toString()
            rule.min != -1 && rule.max != -1 -> "${rule.min}-${rule.max}"
            rule.step != -1 -> "+${rule.step}"
            else -> ""
        }
    }

    /**
     * Extract collection element template
     */
    private fun extractCollectionElementTemplate(kType: KType, config: BeanMockConfig): Any? {
        val elementType = getGenericTypeArgument(kType, 0) ?: return null
        val elementClass = elementType.classifier as? KClass<*> ?: return null
        
        return buildValueByType(elementClass, elementType, config)
    }

    /**
     * Extract Map key and value templates
     */
    private fun extractMapTemplates(kType: KType, config: BeanMockConfig): Pair<Any?, Any?>? {
        val keyType = getGenericTypeArgument(kType, 0) ?: return null
        val valueType = getGenericTypeArgument(kType, 1) ?: return null
        val keyClass = keyType.classifier as? KClass<*> ?: return null
        val valueClass = valueType.classifier as? KClass<*> ?: return null
        
        val keyTemplate = buildValueByType(keyClass, keyType, config)
        val valueTemplate = buildValueByType(valueClass, valueType, config)
        
        return Pair(keyTemplate, valueTemplate)
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
            type == java.math.BigDecimal::class -> false
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
     * Get default template for a property
     */
    private fun getDefaultTemplate(property: KProperty<*>): String {
        val type = property.returnType.classifier as? KClass<*> ?: return "@word"
        return getDefaultTemplateValue(type)
    }

    /**
     * Get default template value for a type
     */
    private fun getDefaultTemplateValue(type: KClass<*>): String {
        return when (type) {
            String::class -> "@word"
            Int::class, Integer::class -> "@natural(1,100)"
            Long::class, java.lang.Long::class -> "@natural(1,1000)"
            Float::class, java.lang.Float::class -> "@float(1.0,100.0)"
            Double::class, java.lang.Double::class -> "@float(1.0,100.0)"
            Boolean::class, java.lang.Boolean::class -> "@boolean"
            java.math.BigDecimal::class -> "@float(1.0,1000.0)"
            java.math.BigInteger::class -> "@natural(1,10000)"
            // Time and date types before Java 8
            java.util.Date::class -> "@date"
            java.sql.Date::class -> "@date"
            java.sql.Time::class -> "@time"
            java.sql.Timestamp::class -> "@datetime"
            java.util.Calendar::class -> "@datetime"
            // Java 8 new time and date types
            java.time.LocalDate::class -> "@date"
            java.time.LocalTime::class -> "@time"
            java.time.LocalDateTime::class -> "@datetime"
            java.time.ZonedDateTime::class -> "@datetime"
            java.time.OffsetDateTime::class -> "@datetime"
            java.time.OffsetTime::class -> "@time"
            java.time.Instant::class -> "@datetime"
            java.time.Duration::class -> "@natural(1,86400)"
            java.time.Period::class -> "@natural(1,365)"
            java.time.Year::class -> "@natural(1970,2030)"
            java.time.YearMonth::class -> "@date"
            java.time.MonthDay::class -> "@date"
            else -> {
                if (type.java.isEnum) {
                    // For enums, pick a random enum value
                    val enumConstants = type.java.enumConstants
                    return enumConstants?.random()?.toString() ?: "@word"
                }
                "@word"
            }
        }
    }

}