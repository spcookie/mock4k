package io.github.spcookie

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.*

/**
 * 日志功能演示测试类
 * 
 * @author spcookie
 * @since 1.0.0
 */
class LoggingTest {

    private val logger = LoggerFactory.getLogger(LoggingTest::class.java)

    @Test
    fun `test logging with locale operations`() {
        // 测试语言环境设置的日志记录
        val originalLocale = LocaleManager.getCurrentLocale()
        logger.info("Original locale: $originalLocale")
        
        // 设置不同的语言环境以触发日志记录
        LocaleManager.setLocale(Locale.ENGLISH)
        LocaleManager.setLocale(Locale.CHINESE)
        LocaleManager.setLocale(Locale.FRENCH)
        
        // 测试数据加载的日志记录
        val words = LocaleManager.getDataList("words")
        logger.info("Loaded ${words.size} words")
        
        val cities = LocaleManager.getDataList("cities")
        logger.info("Loaded ${cities.size} cities")
        
        // 测试缓存清理
        LocaleManager.clearCache()
        
        // 恢复原始语言环境
        LocaleManager.setLocale(originalLocale)
    }
    
    @Test
    fun `test logging with mock data generation`() {
        // 测试 MockRandom 操作的日志记录
        val word = GlobalMockConf.Random.word()
        logger.info("Generated word: $word")

        val city = GlobalMockConf.Random.city()
        logger.info("Generated city: $city")

        val company = GlobalMockConf.Random.company()
        logger.info("Generated company: $company")
        
        // 测试多个数据生成
        val words = GlobalMockConf.Random.words(5)
        logger.info("Generated words: $words")
    }
    
    @Test
    fun `test logging with mock engine`() {
        // 测试 MockEngine 操作的日志记录
        val template = mapOf(
            "name" to "@first @last",
            "age|18-65" to 25,
            "city" to "@city",
            "company" to "@company",
            "skills|3-5" to listOf("@word")
        )

        val result = GlobalMockConf.g(template)
        logger.info("Generated mock data: $result")
        
        // 测试列表生成
        val listTemplate = listOf(
            mapOf(
                "id|+1" to 1,
                "name" to "@first @last"
            )
        )

        val listResult = GlobalMockConf.g(listTemplate)
        logger.info("Generated list data: $listResult")
    }
    
    @Test
    fun `test logging with different locales`() {
        val locales = listOf(
            Locale.ENGLISH,
            Locale.CHINESE,
            Locale.FRENCH,
            Locale.GERMAN,
            Locale.JAPANESE
        )
        
        locales.forEach { locale ->
            LocaleManager.setLocale(locale)
            val word = GlobalMockConf.Random.word()
            val city = GlobalMockConf.Random.city()
            logger.info("locale: ${locale.displayName}, Word: $word, City: $city")
        }
        
        // 清理缓存以测试缓存未命中的日志记录
        LocaleManager.clearCache()
        
        // 再次生成数据以测试缓存重新加载
        val word = GlobalMockConf.Random.word()
        logger.info("After cache clear, generated word: $word")
    }
}