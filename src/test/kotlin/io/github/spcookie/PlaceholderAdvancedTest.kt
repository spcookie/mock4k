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

        val result = mock(template) as Map<String, Any>

        // 验证布尔值
        val boolean = result["boolean"]
        assertDoesNotThrow({ boolean.toString().toBoolean() }, "布尔值应该是布尔类型")

        // 验证自然数
        val natural = result["natural"]
        assertDoesNotThrow({ natural.toString().toInt() }, "自然数应该是数字类型")
        if (natural is Int) {
            assertTrue(natural >= 0, "自然数应该是非负数")
        }

        // 验证整数
        val integer = result["integer"]
        assertDoesNotThrow({ integer.toString().toInt() }, "整数应该是数字类型")

        // 验证浮点数
        val float = result["float"]
        assertDoesNotThrow({ float.toString().toFloat() }, "浮点数应该是数字类型")

        // 验证字符
        val character = result["character"]
        assertTrue(character is String || character is Char, "字符应该是字符串或字符类型")
        if (character is String) {
            assertEquals(1, character.length, "字符字符串长度应该为1")
        }

        // 验证字符串
        val string = result["string"]
        assertTrue(string is String, "字符串应该是字符串类型")
        assertTrue((string as String).isNotEmpty(), "字符串不应为空")

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

        val result = mock(template) as Map<String, Any>

        // 验证自然数范围
        val naturalRange = result["naturalRange"].toString().toInt()
        assertTrue(naturalRange in 10..100, "自然数范围应该是10-100，得到${naturalRange.toInt()}")

        // 验证整数范围
        val integerRange = result["integerRange"].toString().toInt()
        assertTrue(integerRange in -50..50, "整数范围应该是-50到50，得到${integerRange.toInt()}")

        // 验证浮点数范围
        val floatRange = result["floatRange"].toString().toDouble()
        assertTrue(floatRange in 1.0..10.0, "浮点数范围应该是1.0-10.0，得到${floatRange.toDouble()}")

        // 验证字符串长度
        val stringLength = result["stringLength"] as String
        assertEquals(15, stringLength.length, "字符串长度应该是15，得到${stringLength.length}")

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

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析")

            // 基本格式验证
            when (key) {
                "date" -> {
                    // 日期格式验证（可能包含年月日）
                    assertTrue(valueStr.any { it.isDigit() }, "日期应该包含数字")
                }

                "time" -> {
                    // 时间格式验证（可能包含时分秒）
                    assertTrue(valueStr.any { it.isDigit() }, "时间应该包含数字")
                }

                "datetime", "now" -> {
                    // 日期时间格式验证
                    assertTrue(valueStr.any { it.isDigit() }, "日期时间应该包含数字")
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

        val result = mock(template) as Map<String, Any>

        // 验证单词
        val word = result["word"] as String
        assertTrue(word.isNotEmpty(), "单词不应为空")
        assertTrue(word.all { it.isLetter() || it.isWhitespace() }, "单词应该只包含字母和空格")

        // 验证句子
        val sentence = result["sentence"] as String
        assertTrue(sentence.isNotEmpty(), "句子不应为空")
        assertTrue(sentence.length > word.length, "句子应该比单词长")

        // 验证段落
        val paragraph = result["paragraph"] as String
        assertTrue(paragraph.isNotEmpty(), "段落不应为空")
        assertTrue(paragraph.length > sentence.length, "段落应该比句子长")

        // 验证标题
        val title = result["title"] as String
        assertTrue(title.isNotEmpty(), "标题不应为空")

        println("Text placeholders result: $result")
    }

    @Test
    fun testParameterizedTextPlaceholders() {
        val template = mapOf(
            "shortSentence" to "@SENTENCE(3, 5)",
            "longSentence" to "@SENTENCE(10, 15)",
            "customParagraph" to "@PARAGRAPH(3, 5)"
        )

        val result = mock(template) as Map<String, Any>

        val shortSentence = result["shortSentence"] as String
        val longSentence = result["longSentence"] as String
        val customParagraph = result["customParagraph"] as String

        assertTrue(shortSentence.isNotEmpty(), "短句子不应为空")
        assertTrue(longSentence.isNotEmpty(), "长句子不应为空")
        assertTrue(customParagraph.isNotEmpty(), "自定义段落不应为空")

        // 长句子应该比短句子长（大概率）
        assertTrue(longSentence.length >= shortSentence.length, "长句子应该比短句子长")

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

        val result = mock(template) as Map<String, Any>

        val fullName = result["fullName"] as String
        val firstName = result["firstName"] as String
        val lastName = result["lastName"] as String

        assertTrue(fullName.isNotEmpty(), "全名不应为空")
        assertTrue(firstName.isNotEmpty(), "名字不应为空")
        assertTrue(lastName.isNotEmpty(), "姓氏不应为空")

        // 全名应该包含空格（大概率）
        assertTrue(
            fullName.contains(" ") || fullName.length > firstName.length,
            "全名应该更长或包含空格"
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

        val result = mock(template) as Map<String, Any>

        // 验证URL
        val url = result["url"] as String
        assertTrue(url.isNotEmpty(), "URL 不应为空")
        assertTrue(url.contains(".") || url.contains("://"), "URL应该包含.或://")

        // 验证域名
        val domain = result["domain"] as String
        assertTrue(domain.isNotEmpty(), "域名不应为空")
        assertTrue(domain.contains("."), "域名应该包含 .")

        // 验证邮箱
        val email = result["email"] as String
        assertTrue(email.isNotEmpty(), "邮箱不应为空")
        assertTrue(email.contains("@"), "邮箱应该包含@")
        assertTrue(email.contains("."), "邮箱应该包含 .")

        // 验证IP地址
        val ip = result["ip"] as String
        assertTrue(ip.isNotEmpty(), "IP 不应为空")
        assertTrue(ip.contains("."), "IP 应该包含 .")
        val ipPattern = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")
        assertTrue(ipPattern.matcher(ip).matches(), "IP应该匹配IPv4格式: $ip")

        // 验证顶级域名
        val tld = result["tld"] as String
        assertTrue(tld.isNotEmpty(), "顶级域名不应为空")
        assertTrue(tld.all { it.isLetter() }, "顶级域名应该只包含字母")

        // 验证邮箱域名
        val emailDomain = result["emailDomain"] as String
        assertTrue(emailDomain.isNotEmpty(), "邮箱域名不应为空")
        assertTrue(emailDomain.contains("."), "邮箱域名应该包含.")

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

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析")

            when (key) {
                "areaCode" -> {
                    // 区号应该包含数字
                    assertTrue(valueStr.any { it.isDigit() }, "区号应该包含数字")
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

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val phoneNumber = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(phoneNumber.isNotEmpty(), "$key 不应为空")
            assertFalse(phoneNumber.startsWith("@"), "$key 占位符应该被解析")
            assertTrue(phoneNumber.any { it.isDigit() }, "$key 应该包含数字: $phoneNumber")

            // 电话号码长度验证
            val digitCount = phoneNumber.count { it.isDigit() }
            assertTrue(digitCount >= 7, "$key 应该至少有7位数字，在 $phoneNumber 中得到 ${digitCount} 位")

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

        val result = mock(template) as Map<String, Any>

        val company = result["company"] as String
        val profession = result["profession"] as String
        val bankCard = result["bankCard"] as String

        assertTrue(company.isNotEmpty(), "公司不应为空")
        assertTrue(profession.isNotEmpty(), "职业不应为空")
        assertTrue(bankCard.isNotEmpty(), "银行卡不应为空")

        // 银行卡号应该包含数字
        assertTrue(bankCard.any { it.isDigit() }, "银行卡应该包含数字")

        println("Business placeholders result: $result")
    }

    // ==================== 标识符占位符 ====================

    @Test
    fun testIdentifierPlaceholders() {
        val template = mapOf(
            "guid" to "@GUID",
            "id" to "@ID"
        )

        val result = mock(template) as Map<String, Any>

        val guid = result["guid"] as String
        val id = result["id"] as String

        assertTrue(guid.isNotEmpty(), "GUID不应为空")
        assertTrue(id.isNotEmpty(), "ID不应为空")

        // GUID格式验证（通常包含连字符）
        assertTrue(guid.contains("-") || guid.length >= 32, "GUID应该包含-或至少32个字符")

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

        val result = mock(template) as Map<String, Any>

        val color = result["color"] as String
        val image = result["image"] as String
        val dataImage = result["dataImage"] as String

        assertTrue(color.isNotEmpty(), "颜色不应为空")
        assertTrue(image.isNotEmpty(), "图像不应为空")
        assertTrue(dataImage.isNotEmpty(), "数据图像不应为空")

        // 颜色格式验证（可能是十六进制或颜色名）
        assertTrue(color.startsWith("#") || color.all { it.isLetter() }, "颜色应该是十六进制或颜色名")

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

        val result = mock(template) as Map<String, Any>
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
        assertTrue(email.contains("@"), "邮箱应该包含@")
        assertTrue(workEmail.contains("@"), "工作邮箱应该包含@")

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

        val result = mock(template) as Map<String, Any>
        val products = result["products"] as List<Map<String, Any>>

        assertEquals(5, products.size, "应该有5个产品")

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
            assertDoesNotThrow({ product["price"].toString().toFloat() }, "价格应该是数字")
            assertDoesNotThrow({ availability["inStock"].toString().toBoolean() }, "库存状态应该是布尔值")
            assertDoesNotThrow({ availability["quantity"].toString().toLong() }, "数量应该是数字")
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

        val result = mock(template) as Map<String, Any>

        // 即使有无效占位符，也应该返回某种结果
        result.forEach { (key, value) ->
            assertNotNull(value, "即使有无效占位符，$key 也不应为null")
            val valueStr = value.toString()
            assertTrue(valueStr.isNotEmpty(), "即使有无效占位符，$key 也不应为空")
            println("$key: $valueStr")
        }
    }
}