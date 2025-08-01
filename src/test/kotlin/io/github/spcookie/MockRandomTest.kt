package io.github.spcookie

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MockRandomTest {


    @Test
    fun testBasicMock() {
        // 测试基本的模拟数据生成
        val template = mapOf(
            "list|1-10" to listOf(
                mapOf(
                    "id|+1" to 1
                )
            )
        )

        val result = mock(template)
        assertNotNull(result)
        println("Basic mock result: $result")
    }

    @Test
    fun testStringRule() {
        // 测试字符串规则
        val template = mapOf(
            "name|3" to "Hello"
        )

        val result = mock(template) as Map<String, Any>
        val name = result["name"] as String
        assertTrue(name == "HelloHelloHello")
        println("String rule result: $result")
    }

    @Test
    fun testNumberRule() {
        // 测试数字规则
        val template = mapOf(
            "age|18-65" to 25,
            "score|1-100.1-2" to 85.5
        )

        val result = mock(template) as Map<String, Any>
        assertNotNull(result["age"])
        assertNotNull(result["score"])
        println("Number rule result: $result")
    }

    @Test
    fun testArrayRule() {
        // 测试数组规则
        val template = mapOf(
            "items|3-5" to listOf("apple", "banana", "orange")
        )

        val result = mock(template) as Map<String, Any>
        val items = result["items"] as List<*>
        assertTrue(items.isNotEmpty())
        println("Array rule result: $result")
    }

    @Test
    fun testPlaceholder() {
        // 测试占位符
        val template = mapOf(
            "name" to "@NAME",
            "email" to "@EMAIL",
            "date" to "@DATE",
            "id" to "@GUID"
        )

        val result = mock(template) as Map<String, Any>
        assertNotNull(result["name"])
        assertNotNull(result["email"])
        assertNotNull(result["date"])
        assertNotNull(result["id"])
        println("Placeholder result: $result")
    }

    @Test
    fun testComplexTemplate() {
        // 测试复杂模板
        val template = mapOf(
            "users|5-10" to listOf(
                mapOf(
                    "id|+1" to 1,
                    "name" to "@NAME",
                    "email" to "@EMAIL",
                    "age|18-65" to 25,
                    "address" to mapOf(
                        "city" to "@WORD",
                        "street" to "@SENTENCE"
                    ),
                    "tags|1-3" to listOf("tag1", "tag2", "tag3", "tag4")
                )
            )
        )

        val result = mock(template) as Map<String, Any>
        val users = result["users"] as List<*>
        assertTrue(users.isNotEmpty())
        println("Complex template result: $result")
    }

    @Test
    fun testIncrementRule() {
        // 测试递增规则
        val template = mapOf(
            "list|3" to listOf(
                mapOf(
                    "id|+1" to 100,
                    "name" to "Item"
                )
            )
        )

        val result = mock(template) as Map<String, Any>
        val list = result["list"] as List<*>
        assertTrue(list.size == 3)
        println("Increment rule result: $result")
    }

    @Test
    fun testBooleanRule() {
        // 测试布尔值规则
        val template = mapOf(
            "isActive|1" to true,
            "isEnabled|3-7" to false
        )

        val result = mock(template) as Map<String, Any>
        assertNotNull(result["isActive"])
        assertNotNull(result["isEnabled"])
        println("Boolean rule result: $result")
    }

    @Test
    fun testObjectRule() {
        // 测试对象规则
        val template = mapOf(
            "config|2-3" to mapOf(
                "debug" to true,
                "timeout" to 5000,
                "retries" to 3,
                "cache" to false,
                "logging" to true
            )
        )

        val result = mock(template) as Map<String, Any>
        val config = result["config"] as Map<*, *>
        assertTrue(config.size in 2..3)
        println("Object rule result: $result")
    }


    @Test
    fun testPhoneTypePTPrefix() {
        // 测试电话类型PT前缀
        val template = mapOf(
            "mobile1" to "@PHONENUMBER(PT.M)",
            "mobile2" to "@PHONENUMBER(PT.MOBILE)",
            "landline1" to "@PHONENUMBER(PT.L)",
            "landline2" to "@PHONENUMBER(PT.LANDLINE)",
            "tollFree1" to "@PHONENUMBER(PT.TF)",
            "tollFree2" to "@PHONENUMBER(PT.TOLL_FREE)",
            "premium1" to "@PHONENUMBER(PT.P)",
            "premium2" to "@PHONENUMBER(PT.PREMIUM)"
        )

        val result = mock(template) as Map<String, Any>

        // 验证所有PT前缀都有效
        result.values.forEach { phone ->
            val phoneStr = phone as String
            assertTrue(
                !phoneStr.startsWith("@PHONENUMBER"),
                "电话号码应该被生成，而不是占位符: $phoneStr"
            )
        }

        println("PhoneType PT. prefix result: $result")
    }

    @Test
    fun testRandomMethods() {
        // 测试随机方法
        println("random boolean: ${GlobalMockConf.Random.boolean()}")
        println("random integer: ${GlobalMockConf.Random.integer(1, 100)}")
        println("random string: ${GlobalMockConf.Random.string(10)}")
        println("random name: ${GlobalMockConf.Random.name()}")
        println("random email: ${GlobalMockConf.Random.email()}")
        println("random date: ${GlobalMockConf.Random.date()}")
        println("random color: ${GlobalMockConf.Random.color()}")
        println("random GUID: ${GlobalMockConf.Random.guid()}")
    }

    @Test
    fun testMockRandomMethods() {
        // 测试GlobalMockConf.Random的所有方法
        val result1 = GlobalMockConf.Random.boolean()
        val result2 = GlobalMockConf.Random.natural()
        val result3 = GlobalMockConf.Random.integer()
        val result4 = GlobalMockConf.Random.float()
        val result5 = GlobalMockConf.Random.character()
        val result6 = GlobalMockConf.Random.string()
        val result7 = GlobalMockConf.Random.range(1, 10)
        val result8 = GlobalMockConf.Random.date()
        val result9 = GlobalMockConf.Random.time()
        val result10 = GlobalMockConf.Random.datetime()
        val result11 = GlobalMockConf.Random.now()
        val result12 = GlobalMockConf.Random.word()
        val result13 = GlobalMockConf.Random.sentence()
        val result14 = GlobalMockConf.Random.paragraph()
        val result15 = GlobalMockConf.Random.title()
        val result16 = GlobalMockConf.Random.first()
        val result17 = GlobalMockConf.Random.last()
        val result18 = GlobalMockConf.Random.name()
        val result19 = GlobalMockConf.Random.url()
        val result20 = GlobalMockConf.Random.domain()
        val result21 = GlobalMockConf.Random.email()
        val result22 = GlobalMockConf.Random.ip()
        val result23 = GlobalMockConf.Random.tld()
        val result24 = GlobalMockConf.Random.guid()
        val result25 = GlobalMockConf.Random.id()
        val result26 = GlobalMockConf.Random.color()
        val result27 = GlobalMockConf.Random.image()
        val result28 = GlobalMockConf.Random.dataImage()
        val result29 = GlobalMockConf.Random.bankCard()
        val result30 = GlobalMockConf.Random.areaCode()
        val result31 = GlobalMockConf.Random.phoneNumber()
        val result32 = GlobalMockConf.Random.city()
        val result33 = GlobalMockConf.Random.company()
        val result34 = GlobalMockConf.Random.province()
        val result35 = GlobalMockConf.Random.profession()
        val result36 = GlobalMockConf.Random.streetName()
        val result37 = GlobalMockConf.Random.emailDomain()

        println("Boolean: $result1")
        println("Natural: $result2")
        println("Integer: $result3")
        println("Float: $result4")
        println("Character: $result5")
        println("String: $result6")
        println("Range: $result7")
        println("Date: $result8")
        println("Time: $result9")
        println("Datetime: $result10")
        println("Now: $result11")
        println("Word: $result12")
        println("Sentence: $result13")
        println("Paragraph: $result14")
        println("Title: $result15")
        println("First: $result16")
        println("Last: $result17")
        println("Name: $result18")
        println("URL: $result19")
        println("Domain: $result20")
        println("Email: $result21")
        println("IP: $result22")
        println("TLD: $result23")
        println("GUID: $result24")
        println("ID: $result25")
        println("Color: $result26")
        println("Image: $result27")
        println("DataImage: $result28")
        println("BankCard: $result29")
        println("AreaCode: $result30")
        println("PhoneNumber: $result31")
        println("City: $result32")
        println("Company: $result33")
        println("Province: $result34")
        println("Profession: $result35")
        println("StreetName: $result36")
        println("EmailDomain: $result37")
    }

    @Test
    fun testComprehensivePlaceholders() {
        // 测试综合占位符
        val template = mapOf(
            "boolean" to "@BOOLEAN",
            "natural" to "@NATURAL(10, 100)",
            "integer" to "@INTEGER(-50, 50)",
            "float" to "@FLOAT(1.0, 10.0)",
            "string" to "@STRING(10)",
            "word" to "@WORD",
            "sentence" to "@SENTENCE(3, 8)",
            "name" to "@NAME",
            "email" to "@EMAIL",
            "phone" to "@PHONENUMBER(PT.M)",
            "city" to "@CITY",
            "company" to "@COMPANY"
        )

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 的值不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 的值不应为空")
            assertTrue(!valueStr.startsWith("@"), "占位符 $key 应该被解析，得到: $valueStr")
        }

        println("Comprehensive placeholders test result: $result")
    }

    @Test
    fun testChineseLocale() {
        // 测试中文语言环境
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)

        // 测试单词生成
        val word = GlobalMockConf.Random.word()
        assertNotNull(word)
        assertTrue(word.isNotEmpty())

        // 测试城市生成
        val city = GlobalMockConf.Random.city()
        assertNotNull(city)
        assertTrue(city.isNotEmpty())

        // 测试公司生成
        val company = GlobalMockConf.Random.company()
        assertNotNull(company)
        assertTrue(company.isNotEmpty())

        println("Chinese - Word: $word, City: $city, Company: $company")
    }

    @Test
    fun testEnglishLocale() {
        // 测试英文语言环境
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        // 测试单词生成
        val word = GlobalMockConf.Random.word()
        assertNotNull(word)
        assertTrue(word.isNotEmpty())

        // 测试城市生成
        val city = GlobalMockConf.Random.city()
        assertNotNull(city)
        assertTrue(city.isNotEmpty())

        // 测试公司生成
        val company = GlobalMockConf.Random.company()
        assertNotNull(company)
        assertTrue(company.isNotEmpty())

        println("English - Word: $word, City: $city, Company: $company")
    }

    @Test
    fun testMultipleWords() {
        // 测试多个单词生成
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)

        val words = GlobalMockConf.Random.words(5)
        assertEquals(5, words.size)
        words.forEach { word ->
            assertNotNull(word)
            assertTrue(word.isNotEmpty())
        }

        println("Chinese words: $words")
    }

    @Test
    fun testLocaleSwitch() {
        // 测试语言环境切换
        // 测试中文
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)
        assertEquals(Locale.CHINESE, GlobalMockConf.Locale.getCurrentLocale())
        val chineseWord = GlobalMockConf.Random.word()

        // 测试英文
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)
        assertEquals(Locale.ENGLISH, GlobalMockConf.Locale.getCurrentLocale())
        val englishWord = GlobalMockConf.Random.word()

        // 单词应该不同（很可能）
        println("Chinese word: $chineseWord, English word: $englishWord")
    }

    @Test
    fun testAllDataTypes() {
        // 测试所有数据类型
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        // 测试所有数据类型
        val word = GlobalMockConf.Random.word()
        val city = GlobalMockConf.Random.city()
        val company = GlobalMockConf.Random.company()
        val province = GlobalMockConf.Random.province()
        val profession = GlobalMockConf.Random.profession()
        val streetName = GlobalMockConf.Random.streetName()
        val emailDomain = GlobalMockConf.Random.emailDomain()

        // 所有值都应该非空且不为空字符串
        listOf(word, city, company, province, profession, streetName, emailDomain).forEach {
            assertNotNull(it)
            assertTrue(it.isNotEmpty())
        }

        println("Generated data: word=$word, city=$city, company=$company, province=$province, profession=$profession, street=$streetName, email=$emailDomain")
    }

    @Test
    fun testChineseNames() {
        // 测试中文姓名
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)

        val firstName = GlobalMockConf.Random.first()
        val lastName = GlobalMockConf.Random.last()
        val fullName = GlobalMockConf.Random.name()

        assertNotNull(firstName)
        assertNotNull(lastName)
        assertNotNull(fullName)
        assertTrue(firstName.isNotEmpty())
        assertTrue(lastName.isNotEmpty())
        assertTrue(fullName.isNotEmpty())

        println("Chinese names - First: $firstName, Last: $lastName, Full: $fullName")
    }

    @Test
    fun testEnglishNames() {
        // 测试英文姓名
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        val firstName = GlobalMockConf.Random.first()
        val lastName = GlobalMockConf.Random.last()
        val fullName = GlobalMockConf.Random.name()

        assertNotNull(firstName)
        assertNotNull(lastName)
        assertNotNull(fullName)
        assertTrue(firstName.isNotEmpty())
        assertTrue(lastName.isNotEmpty())
        assertTrue(fullName.isNotEmpty())

        println("English names - First: $firstName, Last: $lastName, Full: $fullName")
    }

    @Test
    fun testNameGeneration() {
        // 测试多个姓名生成以确保多样性
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        val names = mutableSetOf<String>()
        repeat(10) {
            names.add(GlobalMockConf.Random.name())
        }

        // 应该有一些多样性（不是全部相同）
        assertTrue(names.size > 1, "生成的姓名应该有一些多样性")

        println("Generated names: $names")
    }

    @Test
    fun testChinesePhoneFormats() {
        // 测试中文电话格式
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)

        val phoneNumbers = (1..10).map { GlobalMockConf.Random.phoneNumber() }

        phoneNumbers.forEach { phone ->
            assertNotNull(phone)
            assertTrue(phone.isNotEmpty(), "电话号码不应为空")
            println("Chinese phone: $phone")

            // 检查是否匹配中文电话模式
            val isValidChineseFormat = phone.matches(Regex("1\\d{10}")) || // 1##########
                    phone.matches(Regex("\\d{3}-\\d{4}-\\d{4}")) || // ###-####-####
                    phone.matches(Regex("\\d{3} \\d{4} \\d{4}")) || // ### #### ####
                    phone.matches(Regex("\\+86-\\d{3}-\\d{4}-\\d{4}")) || // +86-###-####-####
                    phone.matches(Regex("\\+86 \\d{3} \\d{4} \\d{4}")) // +86 ### #### ####

            assertTrue(isValidChineseFormat, "电话应该匹配中文格式: $phone")
        }
    }

    @Test
    fun testEnglishPhoneFormats() {
        // 测试英文电话格式
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        val phoneNumbers = (1..10).map { GlobalMockConf.Random.phoneNumber() }

        phoneNumbers.forEach { phone ->
            assertNotNull(phone)
            assertTrue(phone.isNotEmpty(), "电话号码不应为空")
            println("English phone: $phone")

            // 检查是否匹配美国电话模式
            val isValidUSFormat = phone.matches(Regex("\\(\\d{3}\\) \\d{3}-\\d{4}")) || // (###) ###-####
                    phone.matches(Regex("\\d{3}-\\d{3}-\\d{4}")) || // ###-###-####
                    phone.matches(Regex("\\d{3} \\d{3} \\d{4}")) || // ### ### ####
                    phone.matches(Regex("\\d{3}\\.\\d{3}\\.\\d{4}")) || // ###.###.####
                    phone.matches(Regex("\\+1-\\d{3}-\\d{3}-\\d{4}")) // +1-###-###-####

            assertTrue(isValidUSFormat, "电话应该匹配美国格式: $phone")
        }
    }

    @Test
    fun testCustomPhoneFormat() {
        // 测试自定义电话格式
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        val customFormat = "(###) ###-####"
        val phone = GlobalMockConf.Random.phoneNumber(format = customFormat)

        assertTrue(
            phone.matches(Regex("\\(\\d{3}\\) \\d{3}-\\d{4}")),
            "应该遵循自定义格式: $phone"
        )
        println("Custom format phone: $phone")
    }

    @Test
    fun testDefaultPhoneGeneration() {
        // 测试默认电话生成（应该使用国际化格式）
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)
        val defaultPhone = GlobalMockConf.Random.phoneNumber()
        assertNotNull(defaultPhone)
        assertTrue(defaultPhone.isNotEmpty(), "默认电话不应为空")
        println("Default phone: $defaultPhone")

        // 应该匹配英文电话格式之一
        val isValidFormat = defaultPhone.matches(Regex("\\(\\d{3}\\) \\d{3}-\\d{4}")) ||
                defaultPhone.matches(Regex("\\d{3}-\\d{3}-\\d{4}")) ||
                defaultPhone.matches(Regex("\\d{3} \\d{3} \\d{4}")) ||
                defaultPhone.matches(Regex("\\d{3}\\.\\d{3}\\.\\d{4}")) ||
                defaultPhone.matches(Regex("\\+1-\\d{3}-\\d{3}-\\d{4}"))

        assertTrue(isValidFormat, "默认电话应该匹配英文格式: $defaultPhone")
    }

    @Test
    fun testPhoneFormatSwitching() {
        // 测试电话格式切换
        // 测试中文格式
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)
        val chinesePhone = GlobalMockConf.Random.phoneNumber()
        println("Chinese locale phone: $chinesePhone")

        // 切换到英文格式
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)
        val englishPhone = GlobalMockConf.Random.phoneNumber()
        println("English locale phone: $englishPhone")

        // 电话应该是不同的格式（尽管由于随机性不能保证）
        assertNotNull(chinesePhone)
        assertNotNull(englishPhone)
        assertTrue(chinesePhone.isNotEmpty())
        assertTrue(englishPhone.isNotEmpty())
    }

    @Test
    fun testPhoneNumberDigitsOnly() {
        // 测试电话号码仅数字
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        val phone = GlobalMockConf.Random.phoneNumber()
        val digitsOnly = phone.replace(Regex("[^\\d]"), "")

        // 应该有合理的数字位数（7-15是电话号码的典型位数）
        assertTrue(
            digitsOnly.length >= 7 && digitsOnly.length <= 15,
            "电话应该有7-15位数字，得到 ${digitsOnly.length}: $phone"
        )
        println("Phone digits: $digitsOnly (length: ${digitsOnly.length})")
    }

    @Test
    fun testBasicPhoneGeneration() {
        // 测试基本电话生成
        val phoneNumber = GlobalMockConf.Random.phoneNumber()
        println("Generated phone number: $phoneNumber")
        Assertions.assertNotNull(phoneNumber)
        Assertions.assertTrue(phoneNumber.isNotEmpty())
    }

    @Test
    fun testPhoneTypeGeneration() {
        // 测试电话类型生成
        val mobilePhone = GlobalMockConf.Random.phoneNumber(MockRandom.PhoneType.MOBILE.alias)
        val landlinePhone = GlobalMockConf.Random.phoneNumber(MockRandom.PhoneType.LANDLINE.alias)
        val tollFreePhone = GlobalMockConf.Random.phoneNumber(MockRandom.PhoneType.TOLL_FREE.alias)
        val premiumPhone = GlobalMockConf.Random.phoneNumber(MockRandom.PhoneType.PREMIUM.alias)

        println("Mobile phone: $mobilePhone")
        println("Landline phone: $landlinePhone")
        println("Toll-free phone: $tollFreePhone")
        println("Premium phone: $premiumPhone")

        Assertions.assertNotNull(mobilePhone)
        Assertions.assertNotNull(landlinePhone)
        Assertions.assertNotNull(tollFreePhone)
        Assertions.assertNotNull(premiumPhone)

        Assertions.assertTrue(mobilePhone.isNotEmpty())
        Assertions.assertTrue(landlinePhone.isNotEmpty())
        Assertions.assertTrue(tollFreePhone.isNotEmpty())
        Assertions.assertTrue(premiumPhone.isNotEmpty())
    }

    @Test
    fun testAreaCodeGeneration() {
        // 测试区号生成
        val areaCode = GlobalMockConf.Random.areaCode()
        println("Generated area code: $areaCode")
        Assertions.assertNotNull(areaCode)
        Assertions.assertTrue(areaCode.isNotEmpty())
    }

    @Test
    fun testChineseText() {
        // 测试中文文本
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)

        val word = GlobalMockConf.Random.word()
        val sentence = GlobalMockConf.Random.sentence()
        val paragraph = GlobalMockConf.Random.paragraph()
        val title = GlobalMockConf.Random.title()
        val words = GlobalMockConf.Random.words(3)

        Assertions.assertNotNull(word)
        Assertions.assertNotNull(sentence)
        Assertions.assertNotNull(paragraph)
        Assertions.assertNotNull(title)
        Assertions.assertNotNull(words)

        Assertions.assertTrue(word.isNotEmpty())
        Assertions.assertTrue(sentence.isNotEmpty())
        Assertions.assertTrue(paragraph.isNotEmpty())
        Assertions.assertTrue(title.isNotEmpty())
        assertEquals(3, words.size)

        println("Chinese text - Word: $word")
        println("Chinese text - Sentence: $sentence")
        println("Chinese text - Paragraph: $paragraph")
        println("Chinese text - Title: $title")
        println("Chinese text - Words: $words")
    }

    @Test
    fun testEnglishText() {
        // 测试英文文本
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        val word = GlobalMockConf.Random.word()
        val sentence = GlobalMockConf.Random.sentence()
        val paragraph = GlobalMockConf.Random.paragraph()
        val title = GlobalMockConf.Random.title()
        val words = GlobalMockConf.Random.words(3)

        Assertions.assertNotNull(word)
        Assertions.assertNotNull(sentence)
        Assertions.assertNotNull(paragraph)
        Assertions.assertNotNull(title)
        Assertions.assertNotNull(words)

        Assertions.assertTrue(word.isNotEmpty())
        Assertions.assertTrue(sentence.isNotEmpty())
        Assertions.assertTrue(paragraph.isNotEmpty())
        Assertions.assertTrue(title.isNotEmpty())
        assertEquals(3, words.size)

        println("English text - Word: $word")
        println("English text - Sentence: $sentence")
        println("English text - Paragraph: $paragraph")
        println("English text - Title: $title")
        println("English text - Words: $words")
    }

    @Test
    fun testWordWithLength() {
        // 测试指定长度的单词
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        val shortWord = GlobalMockConf.Random.word(3, 5)
        val longWord = GlobalMockConf.Random.word(8, 12)

        Assertions.assertTrue(shortWord.length >= 3 && shortWord.length <= 5)
        Assertions.assertTrue(longWord.length >= 8 && longWord.length <= 12)

        println("Short word (3-5): $shortWord (length: ${shortWord.length})")
        println("Long word (8-12): $longWord (length: ${longWord.length})")
    }

    @Test
    fun testSentenceStructure() {
        // 测试句子结构
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        val sentence = GlobalMockConf.Random.sentence()

        // 应该以大写字母开头并以句号结尾
        Assertions.assertTrue(sentence.first().isUpperCase(), "句子应该以大写字母开头")
        Assertions.assertTrue(sentence.endsWith("."), "句子应该以句号结尾")

        // 应该包含多个单词
        val wordCount = sentence.dropLast(1).split(" ").size
        Assertions.assertTrue(wordCount >= 12, "句子应该至少包含12个单词")

        println("Generated sentence: $sentence")
        println("Word count: $wordCount")
    }

    @Test
    fun testTitleFormat() {
        // 测试标题格式
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)

        val title = GlobalMockConf.Random.title()

        // 每个单词都应该以大写字母开头
        val words = title.split(" ")
        words.forEach { word ->
            Assertions.assertTrue(word.first().isUpperCase(), "标题中的每个单词都应该以大写字母开头: $word")
        }

        println("Generated title: $title")
    }

    @Test
    fun testImageTextI18n() {
        // 测试英文图片文本
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)
        val englishImageUrl = GlobalMockConf.Random.image()
        Assertions.assertTrue(englishImageUrl.contains("text="), "图片URL应该包含text参数")
        println("English image URL: $englishImageUrl")

        // 测试中文图片文本
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)
        val chineseImageUrl = GlobalMockConf.Random.image()
        Assertions.assertTrue(chineseImageUrl.contains("text="), "图片URL应该包含text参数")
        println("Chinese image URL: $chineseImageUrl")

        // 测试自定义文本（应该覆盖国际化）
        val customImageUrl = GlobalMockConf.Random.image(text = "Custom")
        Assertions.assertTrue(customImageUrl.contains("text=Custom"), "应该使用自定义文本")
        println("Custom image URL: $customImageUrl")
    }
}