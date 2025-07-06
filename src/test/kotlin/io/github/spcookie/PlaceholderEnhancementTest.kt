package io.github.spcookie

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory

class PlaceholderEnhancementTest {

    private val logger = LoggerFactory.getLogger(PlaceholderEnhancementTest::class.java)

    @Test
    fun testPropertyReferenceInSameLevel() {
        val template = mapOf(
            "first" to "@FIRST",
            "middle" to "@FIRST",
            "last" to "@LAST",
            "full" to "@first @middle @last"
        )

        val result = mock(template) as Map<String, Any?>

        // 验证属性已生成
        assertNotNull(result["first"])
        assertNotNull(result["middle"])
        assertNotNull(result["last"])
        assertNotNull(result["full"])

        // 验证全名引用了生成的名、中间名、姓
        val fullName = result["full"] as String
        val firstName = result["first"] as String
        val middleName = result["middle"] as String
        val lastName = result["last"] as String

        assertTrue(fullName.contains(firstName))
        assertTrue(fullName.contains(middleName))
        assertTrue(fullName.contains(lastName))


    }

    @Test
    fun testNestedPropertyReference() {
        val template = mapOf(
            "name" to mapOf(
                "first" to "@FIRST",
                "middle" to "@FIRST",
                "last" to "@LAST",
                "full" to "@first @middle @last"
            )
        )

        val result = mock(template) as Map<String, Any?>
        val nameObj = result["name"] as Map<String, Any?>

        // 验证属性已生成
        assertNotNull(nameObj["first"])
        assertNotNull(nameObj["middle"])
        assertNotNull(nameObj["last"])
        assertNotNull(nameObj["full"])

        // 验证全名引用了生成的名、中间名、姓
        val fullName = nameObj["full"] as String
        val firstName = nameObj["first"] as String
        val middleName = nameObj["middle"] as String
        val lastName = nameObj["last"] as String

        assertTrue(fullName.contains(firstName))
        assertTrue(fullName.contains(middleName))
        assertTrue(fullName.contains(lastName))


    }

    @Test
    fun testAbsolutePathReference() {
        val template = mapOf(
            "user" to mapOf(
                "name" to mapOf(
                    "first" to "@FIRST",
                    "last" to "@LAST"
                ),
                "profile" to mapOf(
                    "displayName" to "@user.name.first @user.name.last",
                    "username" to "@user.name.first"
                )
            )
        )

        val result = mock(template) as Map<String, Any?>
        val userObj = result["user"] as Map<String, Any?>
        val nameObj = userObj["name"] as Map<String, Any?>
        val profileObj = userObj["profile"] as Map<String, Any?>

        val firstName = nameObj["first"] as String
        val lastName = nameObj["last"] as String
        val displayName = profileObj["displayName"] as String
        val username = profileObj["username"] as String

        // 调试输出
        logger.info("firstName: $firstName")
        logger.info("lastName: $lastName")
        logger.info("displayName: $displayName")
        logger.info("username: $username")

        // 验证绝对路径引用有效
        assertTrue(displayName.contains(firstName), "显示名称 '$displayName' 应该包含名字 '$firstName'")
        assertTrue(displayName.contains(lastName), "显示名称 '$displayName' 应该包含姓氏 '$lastName'")
        assertTrue(username.contains(firstName), "用户名 '$username' 应该包含名字 '$firstName'")


    }

    @Test
    fun testMixedPlaceholdersAndReferences() {
        val template = mapOf(
            "id" to "@INTEGER(1000, 9999)",
            "name" to "@FIRST",
            "email" to "@EMAIL",
            "summary" to "User @name has ID @id and email @email"
        )

        val result = mock(template) as Map<String, Any?>

        val id = result["id"]
        val name = result["name"] as String
        val email = result["email"] as String
        val summary = result["summary"] as String

        // 验证摘要包含对其他属性的引用
        assertTrue(summary.contains(name))
        assertTrue(summary.contains(id.toString()))
        assertTrue(summary.contains(email))


    }

    @Test
    fun testFallbackToBuiltinPlaceholders() {
        val template = mapOf(
            "name" to "@FIRST",
            "randomData" to "@NONEXISTENT_PROPERTY @LAST" // 应该回退到 @LAST 占位符
        )

        val result = mock(template) as Map<String, Any?>

        assertNotNull(result["name"])
        assertNotNull(result["randomData"])

        val randomData = result["randomData"] as String
        // 应该包含回退的 @LAST 值但不解析 @NONEXISTENT_PROPERTY
        assertTrue(randomData.contains("@NONEXISTENT_PROPERTY"))
        assertFalse(randomData.contains("@LAST")) // @LAST 应该被解析


    }

    @Test
    fun testRegexWithPropertyReference() {
        val template = mapOf(
            "domain" to "example.com",
            "username" to "/[a-z]{5,8}/",
            "email" to "@username@@domain"
        )

        val result = mock(template) as Map<String, Any?>

        val domain = result["domain"] as String
        val username = result["username"] as String
        val email = result["email"] as String

        // 验证邮箱包含用户名和域名
        assertTrue(email.contains(username))
        assertTrue(email.contains(domain))
        assertTrue(email.contains("@"))


    }
}