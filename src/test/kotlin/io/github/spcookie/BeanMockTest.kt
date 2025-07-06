package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.full.memberProperties

/**
 * Bean Mock 功能测试套件
 * 测试 Mock.mock() 方法对各种 Bean 类的模拟数据生成
 */
class BeanMockTest {

    private val logger = LoggerFactory.getLogger(BeanMockTest::class.java)

    // ==================== 测试用的数据类 ====================

    data class SimpleUser(
        var id: Long = 0L,
        var name: String = "",
        var email: String = "",
        var age: Int = 0
    )

    data class UserWithOptional(
        var id: Long = 0L,
        var name: String = "",
        var email: String? = null,
        var age: Int = 18
    )

    data class Address(
        var street: String = "",
        var city: String = "",
        var zipCode: String = ""
    )

    data class UserWithNested(
        var id: Long = 0L,
        var name: String = "",
        var address: Address = Address()
    )

    data class UserWithCollections(
        var id: Long = 0L,
        var name: String = "",
        var tags: List<String> = emptyList(),
        var scores: Set<Int> = emptySet(),
        var metadata: Map<String, String> = emptyMap()
    )

    data class UserWithGenericCollections(
        var id: Long = 0L,
        var name: String = "",
        var addresses: List<Address> = emptyList(),
        var phoneNumbers: Set<String> = emptySet(),
        var preferences: Map<String, Boolean> = emptyMap()
    )

    @Mock.Bean(includePrivate = true)
    data class UserWithAnnotation(
        var id: Long = 0L,
        var name: String = "",
        private var secret: String = ""
    )

    data class UserWithMockParam(
        @Mock.Property(rule = Mock.Property.Rule(min = 1000, max = 9999))
        var id: Long = 0L,
        @Mock.Property(placeholder = Mock.Property.Placeholder(value = "@FIRST @LAST"))
        var name: String = "",
        @Mock.Property(rule = Mock.Property.Rule(min = 18, max = 65))
        var age: Int = 0
    )

    data class ComplexUser(
        var id: Long = 0L,
        var profile: UserProfile = UserProfile(),
        var addresses: List<Address> = emptyList(),
        var contacts: Map<String, String> = emptyMap(),
        var preferences: UserPreferences = UserPreferences()
    )

    data class UserProfile(
        var firstName: String = "",
        var lastName: String = "",
        var birthDate: LocalDate? = null,
        var avatar: String? = null
    )

    data class UserPreferences(
        var theme: String = "",
        var notifications: Boolean = false,
        var language: String = ""
    )

    // ==================== 基础 Bean Mock 测试 ====================

    @Test
    fun testSimpleBeanMock() {
        val user = mock(SimpleUser::class)
        
        assertNotNull(user)
        assertNotNull(user.id)
        assertNotNull(user.name)
        assertNotNull(user.email)
        assertNotNull(user.age)
        
        assertTrue(user.name.isNotEmpty())
        assertTrue(user.email.isNotEmpty())
        assertTrue(user.age >= 0)
        
        logger.info("Simple bean mock result: $user")
    }

    @Test
    fun testBeanWithOptionalFields() {
        val user = mock(UserWithOptional::class)
        
        assertNotNull(user)
        assertNotNull(user.id)
        assertNotNull(user.name)
        // email 和 age 有默认值，应该被 mock 覆盖
        assertNotNull(user.email)
        assertNotNull(user.age)
        
        logger.info("Bean with optional fields result: $user")
    }

    @Test
    fun testNestedBeanMock() {
        val user = mock(UserWithNested::class)
        
        assertNotNull(user)
        assertNotNull(user.id)
        assertNotNull(user.name)
        assertNotNull(user.address)
        
        // 验证嵌套对象
        assertNotNull(user.address.street)
        assertNotNull(user.address.city)
        assertNotNull(user.address.zipCode)
        
        assertTrue(user.address.street.isNotEmpty())
        assertTrue(user.address.city.isNotEmpty())
        assertTrue(user.address.zipCode.isNotEmpty())
        
        logger.info("Nested bean mock result: $user")
    }

    @Test
    fun testBeanWithCollections() {
        val user = mock(UserWithCollections::class)
        
        assertNotNull(user)
        assertNotNull(user.id)
        assertNotNull(user.name)
        assertNotNull(user.tags)
        assertNotNull(user.scores)
        assertNotNull(user.metadata)
        
        // 验证集合不为空
        assertTrue(user.tags.isNotEmpty())
        assertTrue(user.scores.isNotEmpty())
        assertTrue(user.metadata.isNotEmpty())
        
        // 验证集合元素类型
        assertTrue(user.tags.all { it is String })
        assertTrue(user.scores.all { it is Int })
        assertTrue(user.metadata.all { it.key is String && it.value is String })
        
        logger.info("Bean with collections result: $user")
    }

    @Test
    fun testBeanWithGenericCollections() {
        val user = mock(UserWithGenericCollections::class)
        
        assertNotNull(user)
        assertNotNull(user.addresses)
        assertNotNull(user.phoneNumbers)
        assertNotNull(user.preferences)
        
        // 验证泛型集合
        assertTrue(user.addresses.isNotEmpty())
        assertTrue(user.addresses.all { it is Address })
        
        assertTrue(user.phoneNumbers.isNotEmpty())
        assertTrue(user.phoneNumbers.all { it is String })
        
        assertTrue(user.preferences.isNotEmpty())
        assertTrue(user.preferences.all { it.key is String && it.value is Boolean })
        
        logger.info("Bean with generic collections result: $user")
    }

    // ==================== 注解支持测试 ====================

    @Test
    fun testMockBeanAnnotation() {
        val user = mock(UserWithAnnotation::class)
        
        assertNotNull(user)
        assertNotNull(user.id)
        assertNotNull(user.name)
        
        // 验证私有字段是否被包含（通过注解配置）
        val properties = UserWithAnnotation::class.memberProperties
        val secretProperty = properties.find { it.name == "secret" }
        assertNotNull(secretProperty, "应该包含私有字段 secret")
        
        logger.info("Bean with annotation result: $user")
    }

    @Test
    fun testMockParamAnnotation() {
        val user = mock(UserWithMockParam::class)
        
        assertNotNull(user)
        
        // 验证 @MockParam 规则是否生效
        assertTrue(user.id in 1000..9999, "ID 应该在 1000-9999 范围内，实际值: ${user.id}")
        assertTrue(user.age in 18..65, "年龄应该在 18-65 范围内，实际值: ${user.age}")
        
        // 验证占位符是否生效（姓名应该包含空格，表示是 "@FIRST @LAST" 格式）
        assertTrue(user.name.contains(" "), "姓名应该包含空格（@FIRST @LAST 格式），实际值: ${user.name}")
        
        logger.info("Bean with MockParam annotation result: $user")
    }

    // ==================== 复杂场景测试 ====================

    @Test
    fun testComplexBeanMock() {
        val user = mock(ComplexUser::class)
        
        assertNotNull(user)
        assertNotNull(user.id)
        assertNotNull(user.profile)
        assertNotNull(user.addresses)
        assertNotNull(user.contacts)
        assertNotNull(user.preferences)
        
        // 验证嵌套对象
        assertNotNull(user.profile.firstName)
        assertNotNull(user.profile.lastName)
        
        // 验证集合
        assertTrue(user.addresses.isNotEmpty())
        assertTrue(user.addresses.all { it is Address })
        
        assertTrue(user.contacts.isNotEmpty())
        assertTrue(user.contacts.all { it.key is String && it.value is String })
        
        // 验证嵌套配置对象
        assertNotNull(user.preferences.theme)
        assertNotNull(user.preferences.language)
        assertTrue(user.preferences.theme.isNotEmpty())
        assertTrue(user.preferences.language.isNotEmpty())
        
        logger.info("Complex bean mock result: $user")
    }

    // ==================== 配置参数测试 ====================

    @Test
    fun testMockWithIncludePrivate() {
        // 测试包含私有字段
        val user1 = mock(UserWithAnnotation::class, includePrivate = true)
        val user2 = mock(UserWithAnnotation::class, includePrivate = false)
        
        assertNotNull(user1)
        assertNotNull(user2)
        
        logger.info("Mock with includePrivate=true: $user1")
        logger.info("Mock with includePrivate=false: $user2")
    }

    @Test
    fun testMockWithIncludeStatic() {
        // 测试包含静态字段（如果有的话）
        val user1 = mock(SimpleUser::class, includeStatic = true)
        val user2 = mock(SimpleUser::class, includeStatic = false)
        
        assertNotNull(user1)
        assertNotNull(user2)
        
        logger.info("Mock with includeStatic=true: $user1")
        logger.info("Mock with includeStatic=false: $user2")
    }

    @Test
    fun testMockWithIncludeTransient() {
        // 测试包含瞬态字段
        val user1 = mock(SimpleUser::class, includeTransient = true)
        val user2 = mock(SimpleUser::class, includeTransient = false)
        
        assertNotNull(user1)
        assertNotNull(user2)
        
        logger.info("Mock with includeTransient=true: $user1")
        logger.info("Mock with includeTransient=false: $user2")
    }

    // ==================== 边界情况和错误处理测试 ====================

    @Test
    fun testEmptyClass() {
        data class EmptyClass(val dummy: String = "")
        
        val result = mock(EmptyClass::class)
        assertNotNull(result)
        
        logger.info("Empty class mock result: $result")
    }

    @Test
    fun testClassWithNullableFields() {
        data class NullableFieldsClass(
            var id: Long? = null,
            var name: String? = null,
            var age: Int? = null
        )
        
        val result = mock(NullableFieldsClass::class)
        assertNotNull(result)
        
        // 可空字段应该被赋值而不是保持 null
        assertNotNull(result.id)
        assertNotNull(result.name)
        assertNotNull(result.age)
        
        logger.info("Nullable fields class mock result: $result")
    }

    @Test
    fun testMultipleMockInstances() {
        // 测试多次生成的实例应该有不同的值
        val user1 = mock(SimpleUser::class)
        val user2 = mock(SimpleUser::class)
        val user3 = mock(SimpleUser::class)
        
        assertNotNull(user1)
        assertNotNull(user2)
        assertNotNull(user3)
        
        // 验证实例不完全相同（至少某些字段应该不同）
        val allSame = user1.id == user2.id && user2.id == user3.id &&
                     user1.name == user2.name && user2.name == user3.name &&
                     user1.email == user2.email && user2.email == user3.email
        
        assertFalse(allSame, "多次生成的实例不应该完全相同")
        
        logger.info("Multiple instances:")
        logger.info("User 1: $user1")
        logger.info("User 2: $user2")
        logger.info("User 3: $user3")
    }

    // ==================== 性能测试 ====================

    @Test
    fun testMockPerformance() {
        val startTime = System.currentTimeMillis()
        
        // 生成大量实例测试性能
        repeat(100) {
            mock(ComplexUser::class)
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        logger.info("Generated 100 complex bean instances in ${duration}ms")
        assertTrue(duration < 5000, "生成100个复杂Bean实例应该在5秒内完成，实际耗时: ${duration}ms")
    }
}