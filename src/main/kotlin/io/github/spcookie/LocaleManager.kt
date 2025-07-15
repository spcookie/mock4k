package io.github.spcookie

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * MockRandom的区域设置管理工具
 * 处理区域设置和资源加载
 *
 * @author spcookie
 * @since 1.0.0
 */
object LocaleManager {

    private val logger = LoggerFactory.getLogger(LocaleManager::class.java)
    private var currentLocale: Locale = Locale.getDefault()
    private val localeDataCache = ConcurrentHashMap<String, Properties>()

    /**
     * 设置当前区域设置
     * @param locale 要设置的区域设置
     */
    fun setLocale(locale: Locale) {
        logger.info("Setting locale from {} to {}", currentLocale, locale)
        currentLocale = locale
    }

    /**
     * 获取当前区域设置
     * @return 当前区域设置
     */
    fun getCurrentLocale(): Locale {
        return currentLocale
    }

    /**
     * 从属性文件加载区域设置数据
     * @param locale 要加载的区域设置
     * @return 属性对象
     */
    fun loadLocaleData(locale: Locale = currentLocale): Properties {
        val localeKey = locale.language
        logger.debug("Loading locale data for: {}", localeKey)

        if (localeDataCache.containsKey(localeKey)) {
            logger.debug("Locale data found in cache for: {}", localeKey)
            return localeDataCache[localeKey]!!
        }

        val properties = Properties()

        // 首先尝试加载特定的区域设置文件
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

        // 如果未找到特定区域设置，尝试回退顺序
        if (!loaded) {
            val fallbackOrder = when (localeKey) {
                // 对于中文变体，首先尝试中文
                "zh-cn", "zh-tw", "zh-hk", "zh-sg" -> listOf("zh", "en")
                // 对于英文变体，首先尝试英文
                "en-us", "en-gb", "en-ca", "en-au" -> listOf("en")
                // 对于其他语言，使用英文作为回退
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

        // 最终回退到默认属性
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
     * 从属性中获取数据列表
     * @param key 属性键
     * @param locale 可选的区域设置，默认为当前区域设置
     * @return 数据列表
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
     * 清除区域设置数据缓存
     * 在测试或区域设置资源更新时很有用
     */
    fun clearCache() {
        logger.info("Clearing locale data cache, {} entries removed", localeDataCache.size)
        localeDataCache.clear()
    }

    /**
     * 检查是否支持某个区域设置
     * @param locale 要检查的区域设置
     * @return 如果支持返回true，否则返回false
     */
    fun isLocaleSupported(locale: Locale): Boolean {
        val localeKey = locale.language
        return getSupportedLanguageCodes().contains(localeKey)
    }

    /**
     * 获取所有支持的语言代码 (ISO 639-1)
     * @return 支持的语言代码集合
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
     * 获取所有支持的区域设置
     * @return 支持的区域设置列表
     */
    fun getSupportedLocales(): List<Locale> {
        return getSupportedLanguageCodes().map { Locale(it) }
    }
}