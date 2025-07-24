package io.github.spcookie

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

/**
 * 高级国际化测试 - 详细测试多语言环境和国际化功能
 */
class InternationalizationAdvancedTest {

    private var originalLocale: Locale? = null

    @BeforeEach
    fun setUp() {
        // 保存原始语言环境
        originalLocale = GlobalMockConf.Locale.getCurrentLocale()
    }

    @AfterEach
    fun tearDown() {
        // 恢复原始语言环境
        originalLocale?.let { GlobalMockConf.Locale.setLocale(it) }
    }

    // ==================== 基础语言环境测试 ====================

    @Test
    fun testSupportedLocales() {
        val supportedLocales = listOf(
            Locale.ENGLISH to "English",
            Locale.CHINESE to "Chinese",
            Locale.SIMPLIFIED_CHINESE to "Simplified Chinese",
            Locale.TRADITIONAL_CHINESE to "Traditional Chinese",
            Locale.JAPANESE to "Japanese",
            Locale.KOREAN to "Korean",
            Locale.FRENCH to "French",
            Locale.GERMAN to "German",
            Locale.ITALIAN to "Italian",
            Locale.US to "US English",
            Locale.UK to "UK English",
            Locale.CANADA to "Canadian English"
        )

        supportedLocales.forEach { (locale, localeName) ->
            GlobalMockConf.Locale.setLocale(locale)
            assertEquals(locale, GlobalMockConf.Locale.getCurrentLocale(), "语言环境应该设置为 $localeName")

            // 测试基本功能在每个语言环境下都能工作
            val name = GlobalMockConf.Random.name()
            val word = GlobalMockConf.Random.word()

            assertNotNull(name, "$localeName 的姓名不应为 null")
            assertNotNull(word, "$localeName 的单词不应为 null")
            assertTrue(name.isNotEmpty(), "$localeName 的姓名不应为空")
            assertTrue(word.isNotEmpty(), "$localeName 的单词不应为空")

            println("$localeName - Name: $name, Word: $word")
        }
    }

    // ==================== 中文语言环境测试 ====================

    @Test
    fun testChineseLocaleComprehensive() {
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)

        val template = mapOf(
            "姓名" to "@NAME",
            "城市" to "@CITY",
            "公司" to "@COMPANY",
            "省份" to "@PROVINCE",
            "职业" to "@PROFESSION",
            "街道" to "@STREETNAME",
            "词汇" to "@WORD",
            "句子" to "@SENTENCE",
            "段落" to "@PARAGRAPH",
            "标题" to "@TITLE",
            "多个词汇|5" to listOf("@WORD"),
            "用户列表|3" to listOf(
                mapOf(
                    "id|+1" to 1,
                    "姓名" to "@NAME",
                    "城市" to "@CITY",
                    "公司" to "@COMPANY"
                )
            )
        )

        val result = mock(template) as Map<String, Any>

        // 验证所有字段都有值
        result.forEach { (key, value) ->
            when (value) {
                is String -> {
                    assertNotNull(value, "$key 不应为 null")
                    assertTrue(value.isNotEmpty(), "$key 不应为空")
                    assertFalse(value.startsWith("@"), "$key 占位符应该被解析")
                }

                is List<*> -> {
                    assertTrue(value.isNotEmpty(), "$key 列表不应为空")
                }
            }
            println("中文 $key: $value")
        }

        // 验证用户列表
        val userList = result["用户列表"] as List<Map<String, Any>>
        assertEquals(3, userList.size)
        userList.forEachIndexed { index, user ->
            assertEquals(1 + index, user["id"])
            assertNotNull(user["姓名"])
            assertNotNull(user["城市"])
            assertNotNull(user["公司"])
        }
    }

    @Test
    fun testSimplifiedVsTraditionalChinese() {
        // 测试简体中文
        GlobalMockConf.Locale.setLocale(Locale.SIMPLIFIED_CHINESE)
        val simplifiedName = GlobalMockConf.Random.name()
        val simplifiedCity = GlobalMockConf.Random.city()
        val simplifiedCompany = GlobalMockConf.Random.company()

        // 测试繁体中文
        GlobalMockConf.Locale.setLocale(Locale.TRADITIONAL_CHINESE)
        val traditionalName = GlobalMockConf.Random.name()
        val traditionalCity = GlobalMockConf.Random.city()
        val traditionalCompany = GlobalMockConf.Random.company()

        // 验证都能生成有效数据
        assertNotNull(simplifiedName)
        assertNotNull(simplifiedCity)
        assertNotNull(simplifiedCompany)
        assertNotNull(traditionalName)
        assertNotNull(traditionalCity)
        assertNotNull(traditionalCompany)

        println("简体中文 - 姓名: $simplifiedName, 城市: $simplifiedCity, 公司: $simplifiedCompany")
        println("繁体中文 - 姓名: $traditionalName, 城市: $traditionalCity, 公司: $traditionalCompany")
    }

    // ==================== 日文语言环境测试 ====================

    @Test
    fun testJapaneseLocaleComprehensive() {
        GlobalMockConf.Locale.setLocale(Locale.JAPANESE)

        val template = mapOf(
            "名前" to "@NAME",
            "都市" to "@CITY",
            "会社" to "@COMPANY",
            "職業" to "@PROFESSION",
            "単語" to "@WORD",
            "文章" to "@SENTENCE",
            "ユーザー|3" to listOf(
                mapOf(
                    "id|+1" to 1,
                    "名前" to "@NAME",
                    "都市" to "@CITY",
                    "メール" to "@EMAIL",
                    "電話" to "@PHONENUMBER"
                )
            )
        )

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            when (value) {
                is String -> {
                    assertNotNull(value, "$key 不应为 null")
                    assertTrue(value.isNotEmpty(), "$key 不应为空")
                    assertFalse(value.startsWith("@"), "$key 占位符应该被解析")
                }

                is List<*> -> {
                    assertTrue(value.isNotEmpty(), "$key 列表不应为空")
                }
            }
            println("日本語 $key: $value")
        }
    }

    // ==================== 韩文语言环境测试 ====================

    @Test
    fun testKoreanLocaleComprehensive() {
        GlobalMockConf.Locale.setLocale(Locale.KOREAN)

        val template = mapOf(
            "이름" to "@NAME",
            "도시" to "@CITY",
            "회사" to "@COMPANY",
            "직업" to "@PROFESSION",
            "단어" to "@WORD",
            "문장" to "@SENTENCE",
            "사용자목록|3" to listOf(
                mapOf(
                    "id|+1" to 1,
                    "이름" to "@NAME",
                    "도시" to "@CITY",
                    "이메일" to "@EMAIL"
                )
            )
        )

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            when (value) {
                is String -> {
                    assertNotNull(value, "$key 不应为 null")
                    assertTrue(value.isNotEmpty(), "$key 不应为空")
                    assertFalse(value.startsWith("@"), "$key 占位符应该被解析")
                }

                is List<*> -> {
                    assertTrue(value.isNotEmpty(), "$key 列表不应为空")
                }
            }
            println("한국어 $key: $value")
        }
    }

    // ==================== 欧洲语言环境测试 ====================

    @Test
    fun testEuropeanLocales() {
        val europeanLocales = listOf(
            Locale.FRENCH to "French",
            Locale.GERMAN to "German",
            Locale.ITALIAN to "Italian",
            Locale.forLanguageTag("es") to "Spanish",
            Locale.forLanguageTag("pt") to "Portuguese",
            Locale.forLanguageTag("ru") to "Russian"
        )

        europeanLocales.forEach { (locale, localeName) ->
            GlobalMockConf.Locale.setLocale(locale)

            val template = mapOf(
                "name" to "@NAME",
                "city" to "@CITY",
                "company" to "@COMPANY",
                "word" to "@WORD",
                "sentence" to "@SENTENCE"
            )

            val result = mock(template) as Map<String, Any>

            result.forEach { (key, value) ->
                val valueStr = value.toString()
                assertNotNull(value, "$localeName $key 不应为 null")
                assertTrue(valueStr.isNotEmpty(), "$localeName $key 不应为空")
                assertFalse(valueStr.startsWith("@"), "$localeName $key 占位符应该被解析")
                println("$localeName $key: $valueStr")
            }
        }
    }

    // ==================== 语言环境切换测试 ====================

    @Test
    fun testLocaleSwitch() {
        val locales = listOf(
            Locale.ENGLISH,
            Locale.CHINESE,
            Locale.JAPANESE,
            Locale.KOREAN,
            Locale.FRENCH
        )

        val results = mutableMapOf<Locale, Map<String, Any>>()

        locales.forEach { locale ->
            GlobalMockConf.Locale.setLocale(locale)
            assertEquals(locale, GlobalMockConf.Locale.getCurrentLocale())

            val template = mapOf(
                "name" to "@NAME",
                "city" to "@CITY",
                "company" to "@COMPANY"
            )

            val result = mock(template) as Map<String, Any>
            results[locale] = result

            println("${locale.displayName} result: $result")
        }

        // 验证不同语言环境生成的数据确实不同（大概率）
        val names = results.values.map { it["name"] as String }.distinct()
        val cities = results.values.map { it["city"] as String }.distinct()

        // 至少应该有一些不同的值
        assertTrue(names.size > 1 || cities.size > 1, "不同的语言环境应该生成不同的数据")
    }

    // ==================== 多语言混合测试 ====================

    @Test
    fun testMultilingualData() {
        val template = mapOf(
            "users|5" to listOf(
                mapOf(
                    "id|+1" to 1,
                    "profiles" to mapOf(
                        "english" to mapOf(
                            "name" to "@NAME",
                            "city" to "@CITY",
                            "company" to "@COMPANY"
                        ),
                        "chinese" to mapOf(
                            "name" to "@NAME",
                            "city" to "@CITY",
                            "company" to "@COMPANY"
                        ),
                        "japanese" to mapOf(
                            "name" to "@NAME",
                            "city" to "@CITY",
                            "company" to "@COMPANY"
                        )
                    )
                )
            )
        )

        // 为每个用户的每个语言配置生成数据
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)
        val englishResult = mock(template) as Map<String, Any>

        GlobalMockConf.Locale.setLocale(Locale.CHINESE)
        val chineseResult = mock(template) as Map<String, Any>

        GlobalMockConf.Locale.setLocale(Locale.JAPANESE)
        val japaneseResult = mock(template) as Map<String, Any>

        // 验证所有结果都有效
        listOf(
            englishResult to "English",
            chineseResult to "Chinese",
            japaneseResult to "Japanese"
        ).forEach { (result, language) ->
            val users = result["users"] as List<Map<String, Any>>
            assertEquals(5, users.size, "$language 应该有5个用户")

            users.forEach { user ->
                val profiles = user["profiles"] as Map<String, Map<String, Any>>
                profiles.forEach { (profileLang, profile) ->
                    assertNotNull(profile["name"], "$language $profileLang 姓名不应为 null")
                    assertNotNull(profile["city"], "$language $profileLang 城市不应为 null")
                    assertNotNull(profile["company"], "$language $profileLang 公司不应为 null")
                }
            }

            println("$language multilingual result: $result")
        }
    }

    // ==================== 语言环境特定功能测试 ====================

    @Test
    fun testLocaleSpecificFeatures() {
        // 测试中文特定功能
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)
        val chineseWords = GlobalMockConf.Random.words(10)
        assertTrue(chineseWords.isNotEmpty(), "中文词汇不应为空")

        // 测试英文特定功能
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)
        val englishWords = GlobalMockConf.Random.words(10)
        assertTrue(englishWords.isNotEmpty(), "英文词汇不应为空")

        // 测试日文特定功能
        GlobalMockConf.Locale.setLocale(Locale.JAPANESE)
        val japaneseWords = GlobalMockConf.Random.words(10)
        assertTrue(japaneseWords.isNotEmpty(), "日文词汇不应为空")

        println("Chinese words: $chineseWords")
        println("English words: $englishWords")
        println("Japanese words: $japaneseWords")

        // 验证不同语言的词汇确实不同
        val allWords = chineseWords + englishWords + japaneseWords
        val uniqueWords = allWords.distinct()
        assertTrue(uniqueWords.size > allWords.size * 0.5, "不同语言环境应该生成多样化的词汇")
    }

    // ==================== 语言环境错误处理测试 ====================

    @Test
    fun testLocaleErrorHandling() {
        // 测试不支持的语言环境
        val unsupportedLocales = listOf(
            Locale.forLanguageTag("xx"),
            Locale.forLanguageTag("zz-ZZ"),
            Locale("unknown")
        )

        unsupportedLocales.forEach { locale ->
            GlobalMockConf.Locale.setLocale(locale)

            // 即使是不支持的语言环境，也应该能生成基本数据
            val name = GlobalMockConf.Random.name()
            val word = GlobalMockConf.Random.word()

            assertNotNull(name, "即使是不支持的语言环境，姓名也不应为 null")
            assertNotNull(word, "即使是不支持的语言环境，单词也不应为 null")
            assertTrue(name.isNotEmpty(), "即使是不支持的语言环境，姓名也不应为空")
            assertTrue(word.isNotEmpty(), "即使是不支持的语言环境，单词也不应为空")

            println("不支持的语言环境 ${locale.toLanguageTag()} - 姓名: $name, 单词: $word")
        }
    }

    // ==================== 性能测试 ====================

    @Test
    fun testLocalePerformance() {
        val locales = listOf(
            Locale.ENGLISH,
            Locale.CHINESE,
            Locale.JAPANESE,
            Locale.KOREAN
        )

        locales.forEach { locale ->
            GlobalMockConf.Locale.setLocale(locale)

            val startTime = System.currentTimeMillis()

            // 生成大量数据
            repeat(100) {
                GlobalMockConf.Random.name()
                GlobalMockConf.Random.city()
                GlobalMockConf.Random.company()
                GlobalMockConf.Random.word()
                GlobalMockConf.Random.sentence()
            }

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            println("${locale.displayName} 性能: ${duration}ms 执行500次操作")
            assertTrue(duration < 5000, "${locale.displayName} 的性能应该是可接受的")
        }
    }
}