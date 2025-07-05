package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlaceholderEnhancementTest {

    @Test
    fun testPropertyReferenceInSameLevel() {
        val template = mapOf(
            "first" to "@FIRST",
            "middle" to "@FIRST",
            "last" to "@LAST",
            "full" to "@first @middle @last"
        )

        val result = Mock.mock(template) as Map<String, Any?>

        // Verify that properties are generated
        assertNotNull(result["first"])
        assertNotNull(result["middle"])
        assertNotNull(result["last"])
        assertNotNull(result["full"])

        // Verify that full name references the generated first, middle, last names
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

        val result = Mock.mock(template) as Map<String, Any?>
        val nameObj = result["name"] as Map<String, Any?>

        // Verify that properties are generated
        assertNotNull(nameObj["first"])
        assertNotNull(nameObj["middle"])
        assertNotNull(nameObj["last"])
        assertNotNull(nameObj["full"])

        // Verify that full name references the generated first, middle, last names
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

        val result = Mock.mock(template) as Map<String, Any?>
        val userObj = result["user"] as Map<String, Any?>
        val nameObj = userObj["name"] as Map<String, Any?>
        val profileObj = userObj["profile"] as Map<String, Any?>

        val firstName = nameObj["first"] as String
        val lastName = nameObj["last"] as String
        val displayName = profileObj["displayName"] as String
        val username = profileObj["username"] as String

        // Debug output
        println("firstName: $firstName")
        println("lastName: $lastName")
        println("displayName: $displayName")
        println("username: $username")

        // Verify absolute path references work
        assertTrue(displayName.contains(firstName), "displayName '$displayName' should contain firstName '$firstName'")
        assertTrue(displayName.contains(lastName), "displayName '$displayName' should contain lastName '$lastName'")
        assertTrue(username.contains(firstName), "username '$username' should contain firstName '$firstName'")


    }

    @Test
    fun testMixedPlaceholdersAndReferences() {
        val template = mapOf(
            "id" to "@INTEGER(1000, 9999)",
            "name" to "@FIRST",
            "email" to "@EMAIL",
            "summary" to "User @name has ID @id and email @email"
        )

        val result = Mock.mock(template) as Map<String, Any?>

        val id = result["id"]
        val name = result["name"] as String
        val email = result["email"] as String
        val summary = result["summary"] as String

        // Verify that summary contains references to other properties
        assertTrue(summary.contains(name))
        assertTrue(summary.contains(id.toString()))
        assertTrue(summary.contains(email))


    }

    @Test
    fun testFallbackToBuiltinPlaceholders() {
        val template = mapOf(
            "name" to "@FIRST",
            "randomData" to "@NONEXISTENT_PROPERTY @LAST" // Should fall back to @LAST placeholder
        )

        val result = Mock.mock(template) as Map<String, Any?>

        assertNotNull(result["name"])
        assertNotNull(result["randomData"])

        val randomData = result["randomData"] as String
        // Should contain the fallback @LAST value but not resolve @NONEXISTENT_PROPERTY
        assertTrue(randomData.contains("@NONEXISTENT_PROPERTY"))
        assertFalse(randomData.contains("@LAST")) // @LAST should be resolved


    }

    @Test
    fun testRegexWithPropertyReference() {
        val template = mapOf(
            "domain" to "example.com",
            "username" to "/[a-z]{5,8}/",
            "email" to "@username@@domain"
        )

        val result = Mock.mock(template) as Map<String, Any?>

        val domain = result["domain"] as String
        val username = result["username"] as String
        val email = result["email"] as String

        // Verify email contains both username and domain
        assertTrue(email.contains(username))
        assertTrue(email.contains(domain))
        assertTrue(email.contains("@"))


    }
}