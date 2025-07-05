package io.github.spcookie

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Locale management utility for MockRandom
 * Handles locale settings and resource loading
 *
 * @author spcookie
 * @since 1.0.0
 */
object LocaleManager {

    private var currentLocale: Locale = Locale.getDefault()
    private val localeDataCache = ConcurrentHashMap<String, Properties>()

    /**
     * Set current locale
     * @param locale locale to set
     */
    fun setLocale(locale: Locale) {
        currentLocale = locale
    }

    /**
     * Get current locale
     * @return current locale
     */
    fun getCurrentLocale(): Locale {
        return currentLocale
    }

    /**
     * Load locale data from properties file
     * @param locale locale to load
     * @return properties object
     */
    fun loadLocaleData(locale: Locale = currentLocale): Properties {
        val localeKey = locale.language

        if (localeDataCache.containsKey(localeKey)) {
            return localeDataCache[localeKey]!!
        }

        val properties = Properties()
        val resourceName = when (localeKey) {
            "zh" -> "/messages_zh.properties"
            "en" -> "/messages_en.properties"
            else -> "/messages.properties" // default to English
        }

        try {
            val inputStream = this::class.java.getResourceAsStream(resourceName)
            if (inputStream != null) {
                properties.load(inputStream)
                inputStream.close()
            } else {
                // Fallback to default properties if specific locale not found
                val defaultStream = this::class.java.getResourceAsStream("/messages.properties")
                if (defaultStream != null) {
                    properties.load(defaultStream)
                    defaultStream.close()
                }
            }
        } catch (e: Exception) {
            // If loading fails, use empty properties
            println("Warning: Failed to load locale data for $localeKey: ${e.message}")
        }

        localeDataCache[localeKey] = properties
        return properties
    }

    /**
     * Get data list from properties
     * @param key property key
     * @param locale optional locale, defaults to current locale
     * @return list of data
     */
    fun getDataList(key: String, locale: Locale = currentLocale): List<String> {
        val properties = loadLocaleData(locale)
        val value = properties.getProperty(key, "")
        return if (value.isNotEmpty()) {
            value.split(",").map { it.trim() }
        } else {
            emptyList()
        }
    }

    /**
     * Clear locale data cache
     * Useful for testing or when locale resources are updated
     */
    fun clearCache() {
        localeDataCache.clear()
    }

    /**
     * Check if a locale is supported
     * @param locale locale to check
     * @return true if supported, false otherwise
     */
    fun isLocaleSupported(locale: Locale): Boolean {
        val localeKey = locale.language
        return when (localeKey) {
            "zh", "en" -> true
            else -> false
        }
    }

    /**
     * Get all supported locales
     * @return list of supported locales
     */
    fun getSupportedLocales(): List<Locale> {
        return listOf(
            Locale.ENGLISH,
            Locale.CHINESE
        )
    }
}