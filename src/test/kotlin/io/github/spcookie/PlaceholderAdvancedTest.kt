package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

/**
 * 高级占位符测试 - 详细测试所有占位符功能和参数化使用
 */
class PlaceholderAdvancedTest {

    // ==================== 基础数据类型占位符 ====================

    @Test
    fun testBasicDataTypePlaceholders() {
        val template = mapOf(
            "boolean" to "@BOOLEAN",
            "natural" to "@NATURAL",
            "integer" to "@INTEGER",
            "float" to "@FLOAT",
            "character" to "@CHARACTER",
            "string" to "@STRING",
            "range" to "@RANGE"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证布尔值
        val boolean = result["boolean"]
        assertDoesNotThrow({ boolean.toString().toBoolean() }, "Boolean should be a Boolean type")

        // 验证自然数
        val natural = result["natural"]
        assertDoesNotThrow({ natural.toString().toInt() }, "Natural should be a Number type")
        if (natural is Int) {
            assertTrue(natural >= 0, "Natural should be non-negative")
        }

        // 验证整数
        val integer = result["integer"]
        assertDoesNotThrow({ integer.toString().toInt() }, "Integer should be a Number type")

        // 验证浮点数
        val float = result["float"]
        assertDoesNotThrow({ float.toString().toFloat() }, "Float should be a Number type")

        // 验证字符
        val character = result["character"]
        assertTrue(character is String || character is Char, "Character should be String or Char")
        if (character is String) {
            assertEquals(1, character.length, "Character string should have length 1")
        }

        // 验证字符串
        val string = result["string"]
        assertTrue(string is String, "String should be a String type")
        assertTrue((string as String).isNotEmpty(), "String should not be empty")

        println("Basic data type placeholders result: $result")
    }

    @Test
    fun testParameterizedBasicPlaceholders() {
        val template = mapOf(
            "naturalRange" to "@NATURAL(10, 100)",
            "integerRange" to "@INTEGER(-50, 50)",
            "floatRange" to "@FLOAT(1.0, 10.0)",
            "stringLength" to "@STRING(15)",
            "rangeCustom" to "@RANGE(5, 20)"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证自然数范围
        val naturalRange = result["naturalRange"].toString().toInt()
        assertTrue(naturalRange in 10..100, "Natural range should be 10-100, got ${naturalRange.toInt()}")

        // 验证整数范围
        val integerRange = result["integerRange"].toString().toInt()
        assertTrue(integerRange in -50..50, "Integer range should be -50 to 50, got ${integerRange.toInt()}")

        // 验证浮点数范围
        val floatRange = result["floatRange"].toString().toDouble()
        assertTrue(floatRange in 1.0..10.0, "Float range should be 1.0-10.0, got ${floatRange.toDouble()}")

        // 验证字符串长度
        val stringLength = result["stringLength"] as String
        assertEquals(15, stringLength.length, "String length should be 15, got ${stringLength.length}")

        println("Parameterized basic placeholders result: $result")
    }

    // ==================== 日期时间占位符 ====================

    @Test
    fun testDateTimePlaceholders() {
        val template = mapOf(
            "date" to "@DATE",
            "time" to "@TIME",
            "datetime" to "@DATETIME",
            "now" to "@NOW"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved")

            // 基本格式验证
            when (key) {
                "date" -> {
                    // 日期格式验证（可能包含年月日）
                    assertTrue(valueStr.any { it.isDigit() }, "Date should contain digits")
                }

                "time" -> {
                    // 时间格式验证（可能包含时分秒）
                    assertTrue(valueStr.any { it.isDigit() }, "Time should contain digits")
                }

                "datetime", "now" -> {
                    // 日期时间格式验证
                    assertTrue(valueStr.any { it.isDigit() }, "DateTime should contain digits")
                }
            }

            println("$key: $valueStr")
        }
    }

    // ==================== 文本占位符 ====================

    @Test
    fun testTextPlaceholders() {
        val template = mapOf(
            "word" to "@WORD",
            "sentence" to "@SENTENCE",
            "paragraph" to "@PARAGRAPH",
            "title" to "@TITLE"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证单词
        val word = result["word"] as String
        assertTrue(word.isNotEmpty(), "Word should not be empty")
        assertTrue(word.all { it.isLetter() || it.isWhitespace() }, "Word should contain only letters and spaces")

        // 验证句子
        val sentence = result["sentence"] as String
        assertTrue(sentence.isNotEmpty(), "Sentence should not be empty")
        assertTrue(sentence.length > word.length, "Sentence should be longer than word")

        // 验证段落
        val paragraph = result["paragraph"] as String
        assertTrue(paragraph.isNotEmpty(), "Paragraph should not be empty")
        assertTrue(paragraph.length > sentence.length, "Paragraph should be longer than sentence")

        // 验证标题
        val title = result["title"] as String
        assertTrue(title.isNotEmpty(), "Title should not be empty")

        println("Text placeholders result: $result")
    }

    @Test
    fun testParameterizedTextPlaceholders() {
        val template = mapOf(
            "shortSentence" to "@SENTENCE(3, 5)",
            "longSentence" to "@SENTENCE(10, 15)",
            "customParagraph" to "@PARAGRAPH(3, 5)"
        )

        val result = Mock.mock(template) as Map<String, Any>

        val shortSentence = result["shortSentence"] as String
        val longSentence = result["longSentence"] as String
        val customParagraph = result["customParagraph"] as String

        assertTrue(shortSentence.isNotEmpty(), "Short sentence should not be empty")
        assertTrue(longSentence.isNotEmpty(), "Long sentence should not be empty")
        assertTrue(customParagraph.isNotEmpty(), "Custom paragraph should not be empty")

        // 长句子应该比短句子长（大概率）
        assertTrue(longSentence.length >= shortSentence.length, "Long sentence should be longer than short sentence")

        println("Parameterized text placeholders result: $result")
    }

    // ==================== 人名占位符 ====================

    @Test
    fun testNamePlaceholders() {
        val template = mapOf(
            "fullName" to "@NAME",
            "firstName" to "@FIRST",
            "lastName" to "@LAST"
        )

        val result = Mock.mock(template) as Map<String, Any>

        val fullName = result["fullName"] as String
        val firstName = result["firstName"] as String
        val lastName = result["lastName"] as String

        assertTrue(fullName.isNotEmpty(), "Full name should not be empty")
        assertTrue(firstName.isNotEmpty(), "First name should not be empty")
        assertTrue(lastName.isNotEmpty(), "Last name should not be empty")

        // 全名应该包含空格（大概率）
        assertTrue(
            fullName.contains(" ") || fullName.length > firstName.length,
            "Full name should be longer or contain space"
        )

        println("Name placeholders result: $result")
    }

    // ==================== 网络相关占位符 ====================

    @Test
    fun testNetworkPlaceholders() {
        val template = mapOf(
            "url" to "@URL",
            "domain" to "@DOMAIN",
            "email" to "@EMAIL",
            "ip" to "@IP",
            "tld" to "@TLD",
            "emailDomain" to "@EMAILDOMAIN"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证URL
        val url = result["url"] as String
        assertTrue(url.isNotEmpty(), "URL should not be empty")
        assertTrue(url.contains(".") || url.contains("://"), "URL should contain . or ://")

        // 验证域名
        val domain = result["domain"] as String
        assertTrue(domain.isNotEmpty(), "Domain should not be empty")
        assertTrue(domain.contains("."), "Domain should contain .")

        // 验证邮箱
        val email = result["email"] as String
        assertTrue(email.isNotEmpty(), "Email should not be empty")
        assertTrue(email.contains("@"), "Email should contain @")
        assertTrue(email.contains("."), "Email should contain .")

        // 验证IP地址
        val ip = result["ip"] as String
        assertTrue(ip.isNotEmpty(), "IP should not be empty")
        assertTrue(ip.contains("."), "IP should contain .")
        val ipPattern = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")
        assertTrue(ipPattern.matcher(ip).matches(), "IP should match IPv4 pattern: $ip")

        // 验证顶级域名
        val tld = result["tld"] as String
        assertTrue(tld.isNotEmpty(), "TLD should not be empty")
        assertTrue(tld.all { it.isLetter() }, "TLD should contain only letters")

        // 验证邮箱域名
        val emailDomain = result["emailDomain"] as String
        assertTrue(emailDomain.isNotEmpty(), "Email domain should not be empty")
        assertTrue(emailDomain.contains("."), "Email domain should contain .")

        println("Network placeholders result: $result")
    }

    // ==================== 地址相关占位符 ====================

    @Test
    fun testAddressPlaceholders() {
        val template = mapOf(
            "city" to "@CITY",
            "province" to "@PROVINCE",
            "streetName" to "@STREETNAME",
            "areaCode" to "@AREACODE"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved")

            when (key) {
                "areaCode" -> {
                    // 区号应该包含数字
                    assertTrue(valueStr.any { it.isDigit() }, "Area code should contain digits")
                }
            }

            println("$key: $valueStr")
        }
    }

    // ==================== 电话号码占位符 ====================

    @Test
    fun testPhoneNumberPlaceholders() {
        val template = mapOf(
            "basicPhone" to "@PHONENUMBER",
            "mobilePhone2" to "@PHONENUMBER(PT.M)",
            "landlinePhone2" to "@PHONENUMBER(PT.L)",
            "tollFreePhone2" to "@PHONENUMBER(PT.TF)",
            "premiumPhone2" to "@PHONENUMBER(PT.P)"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val phoneNumber = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(phoneNumber.isNotEmpty(), "$key should not be empty")
            assertFalse(phoneNumber.startsWith("@"), "$key placeholder should be resolved")
            assertTrue(phoneNumber.any { it.isDigit() }, "$key should contain digits: $phoneNumber")

            // 电话号码长度验证
            val digitCount = phoneNumber.count { it.isDigit() }
            assertTrue(digitCount >= 7, "$key should have at least 7 digits, got $digitCount in $phoneNumber")

            println("$key: $phoneNumber")
        }
    }

    // ==================== 商业相关占位符 ====================

    @Test
    fun testBusinessPlaceholders() {
        val template = mapOf(
            "company" to "@COMPANY",
            "profession" to "@PROFESSION",
            "bankCard" to "@BANKCARD"
        )

        val result = Mock.mock(template) as Map<String, Any>

        val company = result["company"] as String
        val profession = result["profession"] as String
        val bankCard = result["bankCard"] as String

        assertTrue(company.isNotEmpty(), "Company should not be empty")
        assertTrue(profession.isNotEmpty(), "Profession should not be empty")
        assertTrue(bankCard.isNotEmpty(), "Bank card should not be empty")

        // 银行卡号应该包含数字
        assertTrue(bankCard.any { it.isDigit() }, "Bank card should contain digits")

        println("Business placeholders result: $result")
    }

    // ==================== 标识符占位符 ====================

    @Test
    fun testIdentifierPlaceholders() {
        val template = mapOf(
            "guid" to "@GUID",
            "id" to "@ID"
        )

        val result = Mock.mock(template) as Map<String, Any>

        val guid = result["guid"] as String
        val id = result["id"] as String

        assertTrue(guid.isNotEmpty(), "GUID should not be empty")
        assertTrue(id.isNotEmpty(), "ID should not be empty")

        // GUID格式验证（通常包含连字符）
        assertTrue(guid.contains("-") || guid.length >= 32, "GUID should contain - or be at least 32 chars")

        println("Identifier placeholders result: $result")
    }

    // ==================== 颜色和图像占位符 ====================

    @Test
    fun testColorAndImagePlaceholders() {
        val template = mapOf(
            "color" to "@COLOR",
            "image" to "@IMAGE",
            "dataImage" to "@DATAIMAGE"
        )

        val result = Mock.mock(template) as Map<String, Any>

        val color = result["color"] as String
        val image = result["image"] as String
        val dataImage = result["dataImage"] as String

        assertTrue(color.isNotEmpty(), "Color should not be empty")
        assertTrue(image.isNotEmpty(), "Image should not be empty")
        assertTrue(dataImage.isNotEmpty(), "Data image should not be empty")

        // 颜色格式验证（可能是十六进制或颜色名）
        assertTrue(color.startsWith("#") || color.all { it.isLetter() }, "Color should be hex or color name")

        println("Color and image placeholders result: $result")
    }

    // ==================== 复合占位符测试 ====================

    @Test
    fun testCompoundPlaceholders() {
        val template = mapOf(
            "userProfile" to mapOf(
                "id" to "@GUID",
                "name" to "@NAME",
                "email" to "@EMAIL",
                "phone" to "@PHONENUMBER(PT.M)",
                "address" to mapOf(
                    "city" to "@CITY",
                    "province" to "@PROVINCE",
                    "street" to "@STREETNAME",
                    "zipCode" to "@NATURAL(10000, 99999)"
                ),
                "work" to mapOf(
                    "company" to "@COMPANY",
                    "profession" to "@PROFESSION",
                    "workPhone" to "@PHONENUMBER(PT.L)",
                    "workEmail" to "@EMAIL"
                ),
                "preferences" to mapOf(
                    "theme" to "@COLOR",
                    "language" to "@WORD",
                    "avatar" to "@IMAGE"
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val userProfile = result["userProfile"] as Map<String, Any>

        // 验证基本信息
        assertNotNull(userProfile["id"])
        assertNotNull(userProfile["name"])
        assertNotNull(userProfile["email"])
        assertNotNull(userProfile["phone"])

        // 验证地址信息
        val address = userProfile["address"] as Map<String, Any>
        assertNotNull(address["city"])
        assertNotNull(address["province"])
        assertNotNull(address["street"])
        assertNotNull(address["zipCode"])

        // 验证工作信息
        val work = userProfile["work"] as Map<String, Any>
        assertNotNull(work["company"])
        assertNotNull(work["profession"])
        assertNotNull(work["workPhone"])
        assertNotNull(work["workEmail"])

        // 验证偏好设置
        val preferences = userProfile["preferences"] as Map<String, Any>
        assertNotNull(preferences["theme"])
        assertNotNull(preferences["language"])
        assertNotNull(preferences["avatar"])

        // 验证邮箱格式
        val email = userProfile["email"] as String
        val workEmail = work["workEmail"] as String
        assertTrue(email.contains("@"), "Email should contain @")
        assertTrue(workEmail.contains("@"), "Work email should contain @")

        println("Compound placeholders result: $result")
    }

    // ==================== 占位符组合测试 ====================

    @Test
    fun testPlaceholderCombinations() {
        val template = mapOf(
            "products|5" to listOf(
                mapOf(
                    "id" to "@GUID",
                    "name" to "@WORD @WORD",
                    "description" to "@SENTENCE(10, 20)",
                    "price" to "@FLOAT(10.0, 1000.0)",
                    "category" to "@WORD",
                    "manufacturer" to mapOf(
                        "name" to "@COMPANY",
                        "contact" to "@EMAIL",
                        "phone" to "@PHONENUMBER(PT.L)",
                        "address" to "@STREETNAME, @CITY, @PROVINCE"
                    ),
                    "specs" to mapOf(
                        "weight" to "@FLOAT(0.1, 10.0)",
                        "dimensions" to "@NATURAL(1, 100) x @NATURAL(1, 100) x @NATURAL(1, 100)",
                        "color" to "@COLOR",
                        "material" to "@WORD"
                    ),
                    "availability" to mapOf(
                        "inStock" to "@BOOLEAN",
                        "quantity" to "@NATURAL(0, 1000)",
                        "lastUpdated" to "@DATETIME"
                    )
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val products = result["products"] as List<Map<String, Any>>

        assertEquals(5, products.size, "Should have 5 products")

        products.forEach { product ->
            // 验证基本信息
            assertNotNull(product["id"])
            assertNotNull(product["name"])
            assertNotNull(product["description"])
            assertNotNull(product["price"])
            assertNotNull(product["category"])

            // 验证制造商信息
            val manufacturer = product["manufacturer"] as Map<String, Any>
            assertNotNull(manufacturer["name"])
            assertNotNull(manufacturer["contact"])
            assertNotNull(manufacturer["phone"])
            assertNotNull(manufacturer["address"])

            // 验证规格信息
            val specs = product["specs"] as Map<String, Any>
            assertNotNull(specs["weight"])
            assertNotNull(specs["dimensions"])
            assertNotNull(specs["color"])
            assertNotNull(specs["material"])

            // 验证可用性信息
            val availability = product["availability"] as Map<String, Any>
            assertNotNull(availability["inStock"])
            assertNotNull(availability["quantity"])
            assertNotNull(availability["lastUpdated"])

            // 验证数据类型
            assertDoesNotThrow({ product["price"].toString().toFloat() }, "Price should be a number")
            assertDoesNotThrow({ availability["inStock"].toString().toBoolean() }, "InStock should be boolean")
            assertDoesNotThrow({ availability["quantity"].toString().toLong() }, "Quantity should be a number")
        }

        println("Placeholder combinations result: $result")
    }

    // ==================== 错误处理测试 ====================

    @Test
    fun testPlaceholderErrorHandling() {
        val template = mapOf(
            "invalidPlaceholder" to "@INVALID",
            "malformedPlaceholder" to "@NATURAL(",
            "emptyPlaceholder" to "@",
            "mixedContent" to "Hello @NAME, your email is @EMAIL!",
            "nestedPlaceholders" to "@SENTENCE(@NATURAL(5, 10))"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 即使有无效占位符，也应该返回某种结果
        result.forEach { (key, value) ->
            assertNotNull(value, "$key should not be null even with invalid placeholder")
            val valueStr = value.toString()
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty even with invalid placeholder")
            println("$key: $valueStr")
        }
    }
}