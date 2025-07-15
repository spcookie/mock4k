package io.github.spcookie

import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(LocaleManager::class.java)
    private var currentLocale: Locale = Locale.getDefault()
    private val localeDataCache = ConcurrentHashMap<String, Properties>()

    /**
     * Set current locale
     * @param locale locale to set
     */
    fun setLocale(locale: Locale) {
        logger.info("Setting locale from {} to {}", currentLocale, locale)
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
        logger.debug("Loading locale data for: {}", localeKey)

        if (localeDataCache.containsKey(localeKey)) {
            logger.debug("Locale data found in cache for: {}", localeKey)
            return localeDataCache[localeKey]!!
        }

        val properties = Properties()

        // Try to load specific locale file first
        val resourceName = "/messages_$localeKey.properties"
        var loaded = false

        try {
            val inputStream = this::class.java.getResourceAsStream(resourceName)
            if (inputStream != null) {
                inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    properties.load(reader)
                }
                loaded = true
                logger.debug("Successfully loaded locale data from: {}", resourceName)
            } else {
                logger.debug("Locale resource not found: {}", resourceName)
            }
        } catch (e: Exception) {
            logger.warn("Failed to load locale resource {}: {}", resourceName, e.message)
        }

        // If specific locale not found, try fallback order
        if (!loaded) {
            val fallbackOrder = when (localeKey) {
                // For Chinese variants, try Chinese first
                "zh-cn", "zh-tw", "zh-hk", "zh-sg" -> listOf("zh", "en")
                // For English variants, try English first
                "en-us", "en-gb", "en-ca", "en-au" -> listOf("en")
                // For other languages, try English as fallback
                else -> listOf("en")
            }

            for (fallbackLang in fallbackOrder) {
                try {
                    val fallbackResource = "/messages_$fallbackLang.properties"
                    val fallbackStream = this::class.java.getResourceAsStream(fallbackResource)
                    if (fallbackStream != null) {
                        fallbackStream.bufferedReader(Charsets.UTF_8).use { reader ->
                            properties.load(reader)
                        }
                        loaded = true
                        logger.info(
                            "Loaded fallback locale data from: {} for requested locale: {}",
                            fallbackResource,
                            localeKey
                        )
                        break
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to load fallback locale {}: {}", fallbackLang, e.message)
                }
            }
        }

        // Final fallback to default properties
        if (!loaded) {
            try {
                val defaultStream = this::class.java.getResourceAsStream("/messages.properties")
                defaultStream?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                    properties.load(reader)
                }
                logger.info("Loaded default locale data for: {}", localeKey)
            } catch (e: Exception) {
                logger.error("Failed to load any locale data for {}: {}", localeKey, e.message)
            }
        }

        localeDataCache[localeKey] = properties
        logger.debug("Cached locale data for: {} with {} properties", localeKey, properties.size)
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
        logger.info("Clearing locale data cache, {} entries removed", localeDataCache.size)
        localeDataCache.clear()
    }

    /**
     * Check if a locale is supported
     * @param locale locale to check
     * @return true if supported, false otherwise
     */
    fun isLocaleSupported(locale: Locale): Boolean {
        val localeKey = locale.language
        return getSupportedLanguageCodes().contains(localeKey)
    }

    /**
     * Get all supported language codes (ISO 639-1)
     * @return set of supported language codes
     */
    fun getSupportedLanguageCodes(): Set<String> {
        return setOf(
            "aa", "ab", "ae", "af", "ak", "am", "an", "ar", "as", "av", "ay", "az",
            "ba", "be", "bg", "bi", "bm", "bn", "bo", "br", "bs", "ca", "ce", "ch",
            "co", "cr", "cs", "cu", "cv", "cy", "da", "de", "dv", "dz", "ee", "el",
            "en", "eo", "es", "et", "eu", "fa", "ff", "fi", "fj", "fo", "fr", "fy",
            "ga", "gd", "gl", "gn", "gu", "gv", "ha", "he", "hi", "ho", "hr", "ht",
            "hu", "hy", "hz", "ia", "id", "ie", "ig", "ii", "ik", "io", "is", "it",
            "iu", "ja", "jv", "ka", "kg", "ki", "kj", "kk", "kl", "km", "kn", "ko",
            "kr", "ks", "ku", "kv", "kw", "ky", "la", "lb", "lg", "li", "ln", "lo",
            "lt", "lu", "lv", "mg", "mh", "mi", "mk", "ml", "mn", "mo", "mr", "ms",
            "mt", "my", "na", "nb", "nd", "ne", "ng", "nl", "nn", "no", "nr", "nv",
            "ny", "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps", "pt", "qu",
            "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "si", "sk",
            "sl", "sm", "sn", "so", "sq", "sr", "ss", "st", "su", "sv", "sw", "ta",
            "te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw",
            "ty", "ug", "uk", "ur", "uz", "ve", "vi", "vo", "wa", "wo", "xh", "yi",
            "yo", "za", "zh", "zu"
        )
    }

    /**
     * Get all supported locales
     * @return list of supported locales
     */
    fun getSupportedLocales(): List<Locale> {
        return getSupportedLanguageCodes().map { Locale(it) }
    }
}