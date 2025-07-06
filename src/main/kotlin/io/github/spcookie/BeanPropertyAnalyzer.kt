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

/**
 * Analyzes Bean properties and converts them to Map structure for MockEngine
 *
 * @author spcookie
 * @since 1.2.0
 */
internal class BeanPropertyAnalyzer {

    private val logger = LoggerFactory.getLogger(BeanPropertyAnalyzer::class.java)

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
                    val mockParam = property.findAnnotation<Mock.Property>()
                    val javaFieldAnnotation = javaField.getAnnotation(Mock.Property::class.java)
                    
                    // Try to get annotation from constructor parameter
                    val constructorParam = constructor?.parameters?.find { it.name == property.name }
                    val constructorAnnotation = constructorParam?.findAnnotation<Mock.Property>()

                    val finalAnnotation = mockParam ?: javaFieldAnnotation ?: constructorAnnotation
                    val enabled = finalAnnotation?.enabled != false
                    enabled
                }
            }
        }
        return properties
    }

    /**
     * Build property key with rules if Mock.Property annotation is present
     */
    private fun buildPropertyKey(property: KProperty<*>, clazz: KClass<*>): String {
        val mockParam = property.findAnnotation<Mock.Property>()
        val javaFieldAnnotation = property.javaField?.getAnnotation(Mock.Property::class.java)
        
        // Try to get annotation from constructor parameter
        val constructor = clazz.primaryConstructor
        val constructorParam = constructor?.parameters?.find { it.name == property.name }
        val constructorAnnotation = constructorParam?.findAnnotation<Mock.Property>()
        
        val finalAnnotation = mockParam ?: javaFieldAnnotation ?: constructorAnnotation
        val propertyName = property.name
        val propertyType = property.returnType
        val propertyClass = propertyType.classifier as? KClass<*>

        if (finalAnnotation != null && finalAnnotation.enabled) {
            val rule = finalAnnotation.rule
            val placeholder = finalAnnotation.placeholder
            
            // Priority 1: If placeholder is specified and property type supports placeholders, use property name (placeholder will be handled in value)
            if (placeholder.value.isNotEmpty() && isPlaceholderSupportedType(propertyClass)) {
                return propertyName
            }
            
            // Priority 2: For collection types with rules, apply rule to the key
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
        val mockParam = property.findAnnotation<Mock.Property>()
        val javaFieldAnnotation = property.javaField?.getAnnotation(Mock.Property::class.java)
        
        // Try to get annotation from constructor parameter
        val propertyType = property.returnType
        val propertyClass = propertyType.classifier as? KClass<*> ?: return null
        val constructor = clazz.primaryConstructor
        val constructorParam = constructor?.parameters?.find { it.name == property.name }
        val constructorAnnotation = constructorParam?.findAnnotation<Mock.Property>()
        
        val finalAnnotation = mockParam ?: javaFieldAnnotation ?: constructorAnnotation

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
    private fun buildPlaceholderValue(placeholder: Mock.Property.Placeholder): String {
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
    private fun hasValidRule(rule: Mock.Property.Rule): Boolean {
        return rule.count != -1 || rule.min != -1 || rule.max != -1 || 
               rule.step != -1 || rule.dmin != -1 || rule.dmax != -1 || rule.dcount != -1
    }
    
    /**
     * Build rule-based value
     */
    private fun buildRuleBasedValue(rule: Mock.Property.Rule, propertyClass: KClass<*>, propertyType: KType, config: BeanMockConfig): Any? {
        // For collection types, the rule is applied to the key (handled in buildPropertyKey)
        // Here we just build the template structure
        return when (propertyClass) {
            List::class, MutableList::class, ArrayList::class -> {
                buildRuleBasedList(rule, propertyType, config)
            }
            Set::class, MutableSet::class, HashSet::class -> {
                buildRuleBasedSet(rule, propertyType, config)
            }
            Map::class, MutableMap::class, HashMap::class -> {
                buildRuleBasedMap(rule, propertyType, config)
            }
            else -> {
                // For basic types, generate template with rule
                buildRuleTemplate(rule, propertyClass)
            }
        }
    }
    
    /**
     * Build rule-based list
     */
    private fun buildRuleBasedList(rule: Mock.Property.Rule, kType: KType, config: BeanMockConfig): List<Any?> {
        val elementType = getGenericTypeArgument(kType, 0) ?: return emptyList()
        val elementClass = elementType.classifier as? KClass<*> ?: return emptyList()
        
        val elementValue = buildValueByType(elementClass, elementType, config)
        
        // Return a list with one template element
        // The rule is applied to the property key, MockEngine will handle the repetition
        logger.debug("Built rule-based list template: [$elementValue]")
        return listOf(elementValue)
    }
    
    /**
     * Build rule-based set
     */
    private fun buildRuleBasedSet(rule: Mock.Property.Rule, kType: KType, config: BeanMockConfig): List<Any?> {
        // Use same structure as list for Set, MockEngine will convert to Set
        return buildRuleBasedList(rule, kType, config)
    }
    
    /**
     * Build rule-based map
     */
    private fun buildRuleBasedMap(rule: Mock.Property.Rule, kType: KType, config: BeanMockConfig): Map<String, Any?> {
        val keyType = getGenericTypeArgument(kType, 0) ?: return emptyMap()
        val valueType = getGenericTypeArgument(kType, 1) ?: return emptyMap()
        val keyClass = keyType.classifier as? KClass<*> ?: return emptyMap()
        val valueClass = valueType.classifier as? KClass<*> ?: return emptyMap()
        
        val keyTemplate = buildValueByType(keyClass, keyType, config)
        val valueTemplate = buildValueByType(valueClass, valueType, config)
        
        // For Map, use default key pattern since rule is applied to property key
        val mapTemplate = mapOf(
            "key|1-3" to keyTemplate,
            "value|1-3" to valueTemplate
        )
        
        logger.debug("Built rule-based map template: $mapTemplate")
        return mapTemplate
    }
    
    /**
     * Build rule template for basic types
     */
    private fun buildRuleTemplate(rule: Mock.Property.Rule, propertyClass: KClass<*>): String {
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
            type == List::class || type == MutableList::class || type == ArrayList::class -> {
                buildListValue(kType, config)
            }
            // Handle Set types
            type == Set::class || type == MutableSet::class || type == HashSet::class -> {
                buildSetValue(kType, config)
            }
            // Handle Map types
            type == Map::class || type == MutableMap::class || type == HashMap::class -> {
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
        val elementType = getGenericTypeArgument(kType, 0) ?: return emptyList()
        val elementClass = elementType.classifier as? KClass<*> ?: return emptyList()
        
        // Create a template list with one element
        val elementValue = buildValueByType(elementClass, elementType, config)
        return listOf(elementValue)
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
        val keyType = getGenericTypeArgument(kType, 0) ?: return emptyMap()
        val valueType = getGenericTypeArgument(kType, 1) ?: return emptyMap()
        val keyClass = keyType.classifier as? KClass<*> ?: return emptyMap()
        val valueClass = valueType.classifier as? KClass<*> ?: return emptyMap()
        
        // Create a template map with dynamic key generation
        val keyTemplate = buildValueByType(keyClass, keyType, config)
        val valueTemplate = buildValueByType(valueClass, valueType, config)
        
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
            type == Set::class || type == MutableSet::class  ||
            type == Map::class || type == MutableMap::class
        )
    }
    
    /**
     * Build rule string from rule annotation
     */
    private fun buildRuleString(rule: Mock.Property.Rule): String {
        return when {
            rule.count != -1 -> rule.count.toString()
            rule.min != -1 && rule.max != -1 -> "${rule.min}-${rule.max}"
            rule.step != -1 -> "+${rule.step}"
            else -> ""
        }
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
            else -> true
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

    /**
     * Configuration for bean mocking
     */
    data class BeanMockConfig(
        val includePrivate: Boolean = false,
        val includeStatic: Boolean = false,
        val includeTransient: Boolean = false
    )
}