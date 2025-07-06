package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Bean Mock Nested Object Test Suite
 * Tests nested object handling in Bean Mock functionality
 */
class BeanMockNestedObjectTest {

    private val logger = LoggerFactory.getLogger(BeanMockNestedObjectTest::class.java)

    // ==================== Test Data Classes for Nested Objects ====================

    data class Address(
        var street: String = "",
        var city: String = "",
        var zipCode: String = "",
        var country: String = ""
    )

    data class ContactInfo(
        var email: String = "",
        var phone: String = "",
        var address: Address = Address()
    )

    data class UserProfile(
        var firstName: String = "",
        var lastName: String = "",
        var age: Int = 0,
        var contactInfo: ContactInfo = ContactInfo()
    )

    data class Company(
        var name: String = "",
        var address: Address = Address(),
        var employees: List<UserProfile> = emptyList()
    )

    // Test nested object with annotations
    data class UserWithAnnotatedNestedObject(
        var id: Long = 0L,
        var name: String = "",
        @Mock.Property(rule = Mock.Property.Rule(min = 1, max = 5))
        var profile: UserProfile = UserProfile()
    )

    // Test deeply nested objects
    data class Level1Object(
        var id: Long = 0L,
        var level2: Level2Object = Level2Object()
    )

    data class Level2Object(
        var name: String = "",
        var level3: Level3Object = Level3Object()
    )

    data class Level3Object(
        var value: String = "",
        var data: Map<String, String> = emptyMap()
    )

    // ==================== Basic Nested Object Tests ====================

    @Test
    fun testSimpleNestedObject() {
        val user = mock(UserProfile::class)
        
        assertNotNull(user)
        assertNotNull(user.firstName)
        assertNotNull(user.lastName)
        assertTrue(user.age > 0)
        
        // Verify nested object is properly generated
        assertNotNull(user.contactInfo)
        assertNotNull(user.contactInfo.email)
        assertNotNull(user.contactInfo.phone)
        
        // Verify deeply nested object
        assertNotNull(user.contactInfo.address)
        assertNotNull(user.contactInfo.address.street)
        assertNotNull(user.contactInfo.address.city)
        assertNotNull(user.contactInfo.address.zipCode)
        assertNotNull(user.contactInfo.address.country)
        
        logger.info("Simple nested object result: $user")
        logger.info("Contact info: ${user.contactInfo}")
        logger.info("Address: ${user.contactInfo.address}")
    }

    @Test
    fun testNestedObjectWithCollections() {
        val company = mock(Company::class)
        
        assertNotNull(company)
        assertNotNull(company.name)
        
        // Verify nested address object
        assertNotNull(company.address)
        assertNotNull(company.address.street)
        assertNotNull(company.address.city)
        
        // Verify nested collection of objects
        assertNotNull(company.employees)
        assertTrue(company.employees.isNotEmpty())
        
        // Verify each employee object is properly generated
        company.employees.forEach { employee ->
            assertNotNull(employee.firstName)
            assertNotNull(employee.lastName)
            assertTrue(employee.age > 0)
            
            // Verify nested objects within collection elements
            assertNotNull(employee.contactInfo)
            assertNotNull(employee.contactInfo.email)
            assertNotNull(employee.contactInfo.address)
            assertNotNull(employee.contactInfo.address.street)
        }
        
        logger.info("Company with nested objects: $company")
        logger.info("Number of employees: ${company.employees.size}")
        company.employees.forEachIndexed { index, employee ->
            logger.info("Employee $index: ${employee.firstName} ${employee.lastName}")
            logger.info("  Contact: ${employee.contactInfo.email}")
            logger.info("  Address: ${employee.contactInfo.address.city}")
        }
    }

    @Test
    fun testAnnotatedNestedObject() {
        val user = mock(UserWithAnnotatedNestedObject::class)
        
        assertNotNull(user)
        assertNotNull(user.id)
        assertNotNull(user.name)
        
        // Verify annotated nested object is generated
        assertNotNull(user.profile)
        assertNotNull(user.profile.firstName)
        assertNotNull(user.profile.lastName)
        assertTrue(user.profile.age > 0)
        
        // Verify nested object within annotated object
        assertNotNull(user.profile.contactInfo)
        assertNotNull(user.profile.contactInfo.email)
        assertNotNull(user.profile.contactInfo.address)
        
        logger.info("User with annotated nested object: $user")
        logger.info("Profile: ${user.profile}")
        logger.info("Profile contact: ${user.profile.contactInfo}")
    }

    @Test
    fun testDeeplyNestedObjects() {
        val level1 = mock(Level1Object::class)
        
        assertNotNull(level1)
        assertNotNull(level1.id)
        
        // Verify level 2 nesting
        assertNotNull(level1.level2)
        assertNotNull(level1.level2.name)
        
        // Verify level 3 nesting
        assertNotNull(level1.level2.level3)
        assertNotNull(level1.level2.level3.value)
        assertNotNull(level1.level2.level3.data)
        
        // Verify nested map is populated
        assertTrue(level1.level2.level3.data.isNotEmpty())
        
        logger.info("Deeply nested object: $level1")
        logger.info("Level 2: ${level1.level2}")
        logger.info("Level 3: ${level1.level2.level3}")
        logger.info("Level 3 data: ${level1.level2.level3.data}")
    }

    // ==================== Edge Cases for Nested Objects ====================

    @Test
    fun testNestedObjectConsistency() {
        // Generate multiple instances to verify consistency
        val users = (1..5).map { mock(UserProfile::class) }
        
        users.forEach { user ->
            assertNotNull(user)
            assertNotNull(user.contactInfo)
            assertNotNull(user.contactInfo.address)
            
            // Verify all nested properties are populated
            assertTrue(user.firstName.isNotEmpty())
            assertTrue(user.contactInfo.email.isNotEmpty())
            assertTrue(user.contactInfo.address.city.isNotEmpty())
        }
        
        logger.info("Generated ${users.size} consistent nested objects")
        users.forEachIndexed { index, user ->
            logger.info("User $index: ${user.firstName} from ${user.contactInfo.address.city}")
        }
    }

    @Test
    fun testNestedObjectWithNullableFields() {
        data class OptionalNestedObject(
            var id: Long = 0L,
            var name: String = "",
            var optionalAddress: Address? = null,
            var requiredAddress: Address = Address()
        )
        
        val obj = mock(OptionalNestedObject::class)
        
        assertNotNull(obj)
        assertNotNull(obj.id)
        assertNotNull(obj.name)
        
        // Required nested object should be populated
        assertNotNull(obj.requiredAddress)
        assertNotNull(obj.requiredAddress.street)
        
        // Optional nested object might be null or populated
        logger.info("Object with nullable nested field: $obj")
        logger.info("Optional address is null: ${obj.optionalAddress == null}")
        logger.info("Required address: ${obj.requiredAddress}")
    }

}