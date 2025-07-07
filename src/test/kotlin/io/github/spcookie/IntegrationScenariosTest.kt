package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertNotNull

/**
 * 集成场景测试 - 模拟真实应用中的复杂使用场景
 */
class IntegrationScenariosTest {

    private val logger = LoggerFactory.getLogger(IntegrationScenariosTest::class.java)

    // ==================== 电商系统场景 ====================

    data class ECommerceUser(
        val id: Long,
        val username: String,
        val email: String,
        val profile: UserProfile,
        val addresses: List<Address>,
        val paymentMethods: List<PaymentMethod>,
        val orders: List<Order>,
        val preferences: UserPreferences,
        val createdAt: LocalDateTime,
        val lastLoginAt: LocalDateTime?
    )

    data class UserProfile(
        val firstName: String,
        val lastName: String,
        val phone: String,
        val dateOfBirth: String,
        val gender: String,
        val avatar: String?
    )

    data class Address(
        val id: Long,
        val type: String, // HOME, WORK, OTHER
        val street: String,
        val city: String,
        val state: String,
        val zipCode: String,
        val country: String,
        val isDefault: Boolean
    )

    data class PaymentMethod(
        val id: Long,
        val type: String, // CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER
        val provider: String,
        val lastFourDigits: String,
        val expiryDate: String?,
        val isDefault: Boolean
    )

    data class Order(
        val id: Long,
        val orderNumber: String,
        val status: String, // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
        val items: List<OrderItem>,
        val shippingAddress: Address,
        val paymentMethod: PaymentMethod,
        val subtotal: BigDecimal,
        val tax: BigDecimal,
        val shipping: BigDecimal,
        val total: BigDecimal,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    data class OrderItem(
        val id: Long,
        val product: Product,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val totalPrice: BigDecimal
    )

    data class Product(
        val id: Long,
        val sku: String,
        val name: String,
        val description: String,
        val category: Category,
        val brand: String,
        val price: BigDecimal,
        val discountPrice: BigDecimal?,
        val images: List<String>,
        val attributes: Map<String, String>,
        val inventory: Inventory,
        val ratings: ProductRatings
    )

    data class Category(
        val id: Long,
        val name: String,
        val parentId: Long?,
        val level: Int
    )

    data class Inventory(
        val quantity: Int,
        val reserved: Int,
        val available: Int,
        val lowStockThreshold: Int
    )

    data class ProductRatings(
        val averageRating: Double,
        val totalReviews: Int,
        val fiveStars: Int,
        val fourStars: Int,
        val threeStars: Int,
        val twoStars: Int,
        val oneStar: Int
    )

    data class UserPreferences(
        val language: String,
        val currency: String,
        val timezone: String,
        val emailNotifications: Boolean,
        val smsNotifications: Boolean,
        val marketingEmails: Boolean
    )

    @Test
    fun testECommerceUserScenario() {
        logger.info("测试电商用户场景...")

        val template = """
        {
            "id": "{{long(1,100000)}}",
            "username": "{{string(8,16)}}",
            "email": "{{email}}",
            "profile": {
                "firstName": "{{name}}",
                "lastName": "{{name}}",
                "phone": "{{phone}}",
                "dateOfBirth": "{{date}}",
                "gender": "{{oneOf('MALE','FEMALE','OTHER')}}",
                "avatar": "{{string(50)}}"
            },
            "addresses": [
                {
                    "id": "{{long(1,1000)}}",
                    "type": "{{oneOf('HOME','WORK','OTHER')}}",
                    "street": "{{string(20)}} Street",
                    "city": "{{string(10)}} City",
                    "state": "{{oneOf('CA','NY','TX','FL','WA')}}",
                    "zipCode": "{{int(10000,99999)}}",
                    "country": "{{oneOf('US','CA','UK','DE','FR')}}",
                    "isDefault": "{{boolean}}"
                },
                {
                    "id": "{{long(1001,2000)}}",
                    "type": "{{oneOf('HOME','WORK','OTHER')}}",
                    "street": "{{string(20)}} Avenue",
                    "city": "{{string(10)}} Town",
                    "state": "{{oneOf('CA','NY','TX','FL','WA')}}",
                    "zipCode": "{{int(10000,99999)}}",
                    "country": "{{oneOf('US','CA','UK','DE','FR')}}",
                    "isDefault": "{{boolean}}"
                }
            ],
            "paymentMethods": [
                {
                    "id": "{{long(1,500)}}",
                    "type": "{{oneOf('CREDIT_CARD','DEBIT_CARD','PAYPAL','BANK_TRANSFER')}}",
                    "provider": "{{oneOf('VISA','MASTERCARD','AMEX','DISCOVER')}}",
                    "lastFourDigits": "{{int(1000,9999)}}",
                    "expiryDate": "{{string(7)}}",
                    "isDefault": "{{boolean}}"
                }
            ],
            "orders": [
                {
                    "id": "{{long(1,10000)}}",
                    "orderNumber": "ORD-{{int(100000,999999)}}",
                    "status": "{{oneOf('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')}}",
                    "items": [
                        {
                            "id": "{{long(1,5000)}}",
                            "product": {
                                "id": "{{long(1,1000)}}",
                                "sku": "SKU-{{int(10000,99999)}}",
                                "name": "{{string(20)}}",
                                "description": "{{string(100)}}",
                                "category": {
                                    "id": "{{long(1,100)}}",
                                    "name": "{{string(15)}}",
                                    "parentId": "{{long(1,50)}}",
                                    "level": "{{int(1,5)}}"
                                },
                                "brand": "{{string(10)}}",
                                "price": "{{double(10.0,1000.0)}}",
                                "discountPrice": "{{double(5.0,500.0)}}",
                                "images": ["{{string(30)}}", "{{string(30)}}"],
                                "attributes": {
                                    "color": "{{oneOf('Red','Blue','Green','Black','White')}}",
                                    "size": "{{oneOf('XS','S','M','L','XL','XXL')}}"
                                },
                                "inventory": {
                                    "quantity": "{{int(0,1000)}}",
                                    "reserved": "{{int(0,100)}}",
                                    "available": "{{int(0,900)}}",
                                    "lowStockThreshold": "{{int(5,50)}}"
                                },
                                "ratings": {
                                    "averageRating": "{{double(1.0,5.0)}}",
                                    "totalReviews": "{{int(0,1000)}}",
                                    "fiveStars": "{{int(0,500)}}",
                                    "fourStars": "{{int(0,300)}}",
                                    "threeStars": "{{int(0,150)}}",
                                    "twoStars": "{{int(0,50)}}",
                                    "oneStar": "{{int(0,25)}}"
                                }
                            },
                            "quantity": "{{int(1,10)}}",
                            "unitPrice": "{{double(10.0,500.0)}}",
                            "totalPrice": "{{double(10.0,5000.0)}}"
                        }
                    ],
                    "shippingAddress": {
                        "id": "{{long(1,1000)}}",
                        "type": "HOME",
                        "street": "{{string(20)}} Street",
                        "city": "{{string(10)}} City",
                        "state": "CA",
                        "zipCode": "{{int(10000,99999)}}",
                        "country": "US",
                        "isDefault": true
                    },
                    "paymentMethod": {
                        "id": "{{long(1,500)}}",
                        "type": "CREDIT_CARD",
                        "provider": "VISA",
                        "lastFourDigits": "{{int(1000,9999)}}",
                        "expiryDate": "12/25",
                        "isDefault": true
                    },
                    "subtotal": "{{double(50.0,2000.0)}}",
                    "tax": "{{double(5.0,200.0)}}",
                    "shipping": "{{double(0.0,50.0)}}",
                    "total": "{{double(55.0,2250.0)}}",
                    "createdAt": "{{datetime}}",
                    "updatedAt": "{{datetime}}"
                }
            ],
            "preferences": {
                "language": "{{oneOf('en','zh','es','fr','de')}}",
                "currency": "{{oneOf('USD','EUR','GBP','JPY','CNY')}}",
                "timezone": "{{oneOf('UTC','EST','PST','GMT','CET')}}",
                "emailNotifications": "{{boolean}}",
                "smsNotifications": "{{boolean}}",
                "marketingEmails": "{{boolean}}"
            },
            "createdAt": "{{datetime}}",
            "lastLoginAt": "{{datetime}}"
        }
        """.trimIndent()

        val startTime = System.currentTimeMillis()
        val user = mock<ECommerceUser>(template)
        val endTime = System.currentTimeMillis()

        assertNotNull(user, "电商用户不应为null")

        // 验证用户基本信息
        assertTrue(user.id > 0, "用户ID应大于0")
        assertTrue(user.username.length >= 8, "用户名长度应至少8位")
        assertTrue(user.email.isNotEmpty(), "邮箱不应为空")

        // 验证用户档案
        assertNotNull(user.profile, "用户档案不应为null")
        assertTrue(user.profile.firstName.isNotEmpty(), "名字不应为空")
        assertTrue(user.profile.lastName.isNotEmpty(), "姓氏不应为空")
        assertTrue(user.profile.gender in listOf("MALE", "FEMALE", "OTHER"), "性别应在指定选项中")

        // 验证地址
        assertNotNull(user.addresses, "地址列表不应为null")
        assertEquals(2, user.addresses.size, "应有2个地址")
        user.addresses.forEach { address ->
            assertTrue(address.type in listOf("HOME", "WORK", "OTHER"), "地址类型应在指定选项中")
            assertTrue(address.country in listOf("US", "CA", "UK", "DE", "FR"), "国家应在指定选项中")
        }

        // 验证支付方式
        assertNotNull(user.paymentMethods, "支付方式列表不应为null")
        assertTrue(user.paymentMethods.isNotEmpty(), "应至少有一个支付方式")
        user.paymentMethods.forEach { payment ->
            assertTrue(
                payment.type in listOf("CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER"),
                "支付类型应在指定选项中"
            )
        }

        // 验证订单
        assertNotNull(user.orders, "订单列表不应为null")
        assertTrue(user.orders.isNotEmpty(), "应至少有一个订单")
        user.orders.forEach { order ->
            assertTrue(order.orderNumber.startsWith("ORD-"), "订单号应以ORD-开头")
            assertTrue(
                order.status in listOf("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"),
                "订单状态应在指定选项中"
            )

            // 验证订单项
            assertNotNull(order.items, "订单项不应为null")
            assertTrue(order.items.isNotEmpty(), "应至少有一个订单项")
            order.items.forEach { item ->
                assertNotNull(item.product, "产品不应为null")
                assertTrue(item.product.sku.startsWith("SKU-"), "SKU应以SKU-开头")
                assertTrue(item.quantity > 0, "数量应大于0")

                // 验证产品分类
                assertNotNull(item.product.category, "产品分类不应为null")
                assertTrue(item.product.category.level >= 1, "分类级别应至少为1")

                // 验证库存
                assertNotNull(item.product.inventory, "库存信息不应为null")
                assertTrue(item.product.inventory.quantity >= 0, "库存数量应非负")

                // 验证评分
                assertNotNull(item.product.ratings, "评分信息不应为null")
                assertTrue(
                    item.product.ratings.averageRating >= 1.0 && item.product.ratings.averageRating <= 5.0,
                    "平均评分应在1-5范围内"
                )
            }
        }

        // 验证用户偏好
        assertNotNull(user.preferences, "用户偏好不应为null")
        assertTrue(user.preferences.language in listOf("en", "zh", "es", "fr", "de"), "语言应在指定选项中")
        assertTrue(user.preferences.currency in listOf("USD", "EUR", "GBP", "JPY", "CNY"), "货币应在指定选项中")

        val processingTime = endTime - startTime
        logger.info("电商用户场景处理时间: ${processingTime}ms")
        logger.info("生成的电商用户: ${user.username} (${user.email})")

        assertTrue(processingTime < 2000, "电商用户场景处理时间应少于2秒")
    }

    // ==================== 社交媒体场景 ====================

    data class SocialMediaUser(
        val id: Long,
        val handle: String,
        val displayName: String,
        val bio: String,
        val avatar: String,
        val coverImage: String,
        val followers: List<Follower>,
        val following: List<Following>,
        val posts: List<Post>,
        val stats: UserStats,
        val settings: SocialSettings,
        val verificationStatus: VerificationStatus
    )

    data class Follower(
        val userId: Long,
        val handle: String,
        val displayName: String,
        val followedAt: String
    )

    data class Following(
        val userId: Long,
        val handle: String,
        val displayName: String,
        val followedAt: String
    )

    data class Post(
        val id: Long,
        val content: String,
        val media: List<MediaItem>,
        val hashtags: List<String>,
        val mentions: List<String>,
        val likes: Int,
        val retweets: Int,
        val comments: List<Comment>,
        val createdAt: String,
        val visibility: String
    )

    data class MediaItem(
        val id: Long,
        val type: String, // IMAGE, VIDEO, GIF
        val url: String,
        val thumbnail: String?,
        val altText: String?
    )

    data class Comment(
        val id: Long,
        val userId: Long,
        val content: String,
        val likes: Int,
        val createdAt: String
    )

    data class UserStats(
        val postsCount: Int,
        val followersCount: Int,
        val followingCount: Int,
        val likesReceived: Int,
        val retweetsReceived: Int,
        val joinDate: String
    )

    data class SocialSettings(
        val profileVisibility: String, // PUBLIC, PRIVATE, FRIENDS_ONLY
        val allowDirectMessages: Boolean,
        val showOnlineStatus: Boolean,
        val emailNotifications: Boolean,
        val pushNotifications: Boolean
    )

    data class VerificationStatus(
        val isVerified: Boolean,
        val verifiedAt: String?,
        val verificationBadge: String? // BLUE, GOLD, GRAY
    )

    @Test
    fun testSocialMediaUserScenario() {
        logger.info("测试社交媒体用户场景...")

        val template = """
        {
            "id": "{{long(1,1000000)}}",
            "handle": "@{{string(5,15)}}",
            "displayName": "{{name}} {{name}}",
            "bio": "{{string(50,200)}}",
            "avatar": "{{string(40)}}",
            "coverImage": "{{string(40)}}",
            "followers": [
                {
                    "userId": "{{long(1,100000)}}",
                    "handle": "@{{string(5,15)}}",
                    "displayName": "{{name}}",
                    "followedAt": "{{datetime}}"
                },
                {
                    "userId": "{{long(100001,200000)}}",
                    "handle": "@{{string(5,15)}}",
                    "displayName": "{{name}}",
                    "followedAt": "{{datetime}}"
                }
            ],
            "following": [
                {
                    "userId": "{{long(200001,300000)}}",
                    "handle": "@{{string(5,15)}}",
                    "displayName": "{{name}}",
                    "followedAt": "{{datetime}}"
                }
            ],
            "posts": [
                {
                    "id": "{{long(1,10000)}}",
                    "content": "{{string(50,280)}}",
                    "media": [
                        {
                            "id": "{{long(1,5000)}}",
                            "type": "{{oneOf('IMAGE','VIDEO','GIF')}}",
                            "url": "{{string(50)}}",
                            "thumbnail": "{{string(40)}}",
                            "altText": "{{string(30)}}"
                        }
                    ],
                    "hashtags": ["#{{string(8)}}", "#{{string(6)}}"],
                    "mentions": ["@{{string(10)}}"],
                    "likes": "{{int(0,10000)}}",
                    "retweets": "{{int(0,5000)}}",
                    "comments": [
                        {
                            "id": "{{long(1,50000)}}",
                            "userId": "{{long(1,100000)}}",
                            "content": "{{string(20,100)}}",
                            "likes": "{{int(0,1000)}}",
                            "createdAt": "{{datetime}}"
                        }
                    ],
                    "createdAt": "{{datetime}}",
                    "visibility": "{{oneOf('PUBLIC','PRIVATE','FRIENDS_ONLY')}}"
                },
                {
                    "id": "{{long(10001,20000)}}",
                    "content": "{{string(30,280)}}",
                    "media": [],
                    "hashtags": ["#{{string(10)}}"],
                    "mentions": [],
                    "likes": "{{int(0,5000)}}",
                    "retweets": "{{int(0,2000)}}",
                    "comments": [],
                    "createdAt": "{{datetime}}",
                    "visibility": "{{oneOf('PUBLIC','PRIVATE','FRIENDS_ONLY')}}"
                }
            ],
            "stats": {
                "postsCount": "{{int(0,10000)}}",
                "followersCount": "{{int(0,1000000)}}",
                "followingCount": "{{int(0,5000)}}",
                "likesReceived": "{{int(0,100000)}}",
                "retweetsReceived": "{{int(0,50000)}}",
                "joinDate": "{{date}}"
            },
            "settings": {
                "profileVisibility": "{{oneOf('PUBLIC','PRIVATE','FRIENDS_ONLY')}}",
                "allowDirectMessages": "{{boolean}}",
                "showOnlineStatus": "{{boolean}}",
                "emailNotifications": "{{boolean}}",
                "pushNotifications": "{{boolean}}"
            },
            "verificationStatus": {
                "isVerified": "{{boolean}}",
                "verifiedAt": "{{datetime}}",
                "verificationBadge": "{{oneOf('BLUE','GOLD','GRAY')}}"
            }
        }
        """.trimIndent()

        val user = mock<SocialMediaUser>(template)

        assertNotNull(user, "社交媒体用户不应为null")

        // 验证用户基本信息
        assertTrue(user.handle.startsWith("@"), "用户名应以@开头")
        assertTrue(user.displayName.isNotEmpty(), "显示名称不应为空")
        assertTrue(user.bio.length >= 50, "个人简介长度应至少50字符")

        // 验证关注者和关注
        assertEquals(2, user.followers.size, "应有2个关注者")
        assertEquals(1, user.following.size, "应关注1个用户")

        user.followers.forEach { follower ->
            assertTrue(follower.handle.startsWith("@"), "关注者用户名应以@开头")
        }

        // 验证帖子
        assertEquals(2, user.posts.size, "应有2个帖子")
        user.posts.forEach { post ->
            assertTrue(post.content.length >= 30, "帖子内容长度应至少30字符")
            assertTrue(post.likes >= 0, "点赞数应非负")
            assertTrue(post.retweets >= 0, "转发数应非负")
            assertTrue(post.visibility in listOf("PUBLIC", "PRIVATE", "FRIENDS_ONLY"), "可见性应在指定选项中")

            // 验证标签
            post.hashtags.forEach { hashtag ->
                assertTrue(hashtag.startsWith("#"), "标签应以#开头")
            }

            // 验证提及
            post.mentions.forEach { mention ->
                assertTrue(mention.startsWith("@"), "提及应以@开头")
            }
        }

        // 验证统计信息
        assertNotNull(user.stats, "统计信息不应为null")
        assertTrue(user.stats.postsCount >= 0, "帖子数应非负")
        assertTrue(user.stats.followersCount >= 0, "关注者数应非负")
        assertTrue(user.stats.followingCount >= 0, "关注数应非负")

        // 验证设置
        assertNotNull(user.settings, "设置不应为null")
        assertTrue(
            user.settings.profileVisibility in listOf("PUBLIC", "PRIVATE", "FRIENDS_ONLY"),
            "档案可见性应在指定选项中"
        )

        // 验证认证状态
        assertNotNull(user.verificationStatus, "认证状态不应为null")
        if (user.verificationStatus.isVerified) {
            assertNotNull(user.verificationStatus.verifiedAt, "已认证用户应有认证时间")
            assertTrue(
                user.verificationStatus.verificationBadge in listOf("BLUE", "GOLD", "GRAY"),
                "认证徽章应在指定选项中"
            )
        }

        logger.info("生成的社交媒体用户: ${user.handle} (${user.displayName})")
    }

    // ==================== 企业管理系统场景 ====================

    data class Employee(
        val id: Long,
        val employeeNumber: String,
        val personalInfo: PersonalInfo,
        val jobInfo: JobInfo,
        val department: Department,
        val manager: Manager?,
        val directReports: List<DirectReport>,
        val salary: SalaryInfo,
        val benefits: Benefits,
        val performance: List<PerformanceReview>,
        val timeOff: TimeOffInfo,
        val skills: List<Skill>,
        val certifications: List<Certification>
    )

    data class PersonalInfo(
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val address: Address,
        val emergencyContact: EmergencyContact,
        val dateOfBirth: String,
        val nationality: String
    )

    data class JobInfo(
        val title: String,
        val level: String,
        val employmentType: String, // FULL_TIME, PART_TIME, CONTRACT, INTERN
        val startDate: String,
        val endDate: String?,
        val workLocation: String, // OFFICE, REMOTE, HYBRID
        val workSchedule: String
    )

    data class Department(
        val id: Long,
        val name: String,
        val code: String,
        val parentDepartmentId: Long?,
        val headOfDepartment: String,
        val budget: BigDecimal,
        val location: String
    )

    data class Manager(
        val id: Long,
        val name: String,
        val title: String,
        val email: String
    )

    data class DirectReport(
        val id: Long,
        val name: String,
        val title: String,
        val startDate: String
    )

    data class SalaryInfo(
        val baseSalary: BigDecimal,
        val currency: String,
        val payFrequency: String, // MONTHLY, BI_WEEKLY, WEEKLY
        val lastRaiseDate: String?,
        val nextReviewDate: String
    )

    data class Benefits(
        val healthInsurance: Boolean,
        val dentalInsurance: Boolean,
        val visionInsurance: Boolean,
        val retirementPlan: Boolean,
        val paidTimeOff: Int,
        val sickLeave: Int,
        val lifeInsurance: Boolean
    )

    data class PerformanceReview(
        val id: Long,
        val reviewPeriod: String,
        val overallRating: Double,
        val goals: List<Goal>,
        val feedback: String,
        val reviewDate: String,
        val reviewerId: Long
    )

    data class Goal(
        val id: Long,
        val description: String,
        val status: String, // NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED
        val targetDate: String,
        val completionDate: String?
    )

    data class TimeOffInfo(
        val availableVacationDays: Int,
        val usedVacationDays: Int,
        val availableSickDays: Int,
        val usedSickDays: Int,
        val pendingRequests: List<TimeOffRequest>
    )

    data class TimeOffRequest(
        val id: Long,
        val type: String, // VACATION, SICK, PERSONAL, MATERNITY, PATERNITY
        val startDate: String,
        val endDate: String,
        val days: Int,
        val status: String, // PENDING, APPROVED, REJECTED
        val reason: String
    )

    data class Skill(
        val id: Long,
        val name: String,
        val category: String,
        val proficiencyLevel: String, // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
        val yearsOfExperience: Int,
        val lastUsed: String
    )

    data class Certification(
        val id: Long,
        val name: String,
        val issuingOrganization: String,
        val issueDate: String,
        val expiryDate: String?,
        val credentialId: String
    )

    data class EmergencyContact(
        val name: String,
        val relationship: String,
        val phone: String,
        val email: String
    )

    @Test
    fun testEmployeeManagementScenario() {
        logger.info("测试员工管理系统场景...")

        val template = """
        {
            "id": "{{long(1,100000)}}",
            "employeeNumber": "EMP{{int(10000,99999)}}",
            "personalInfo": {
                "firstName": "{{name}}",
                "lastName": "{{name}}",
                "email": "{{email}}",
                "phone": "{{phone}}",
                "address": {
                    "id": "{{long(1,1000)}}",
                    "type": "HOME",
                    "street": "{{string(20)}} Street",
                    "city": "{{string(10)}} City",
                    "state": "{{oneOf('CA','NY','TX','FL','WA')}}",
                    "zipCode": "{{int(10000,99999)}}",
                    "country": "US",
                    "isDefault": true
                },
                "emergencyContact": {
                    "name": "{{name}} {{name}}",
                    "relationship": "{{oneOf('Spouse','Parent','Sibling','Friend','Other')}}",
                    "phone": "{{phone}}",
                    "email": "{{email}}"
                },
                "dateOfBirth": "{{date}}",
                "nationality": "{{oneOf('US','CA','UK','DE','FR','IN','CN')}}"
            },
            "jobInfo": {
                "title": "{{oneOf('Software Engineer','Product Manager','Data Scientist','UX Designer','DevOps Engineer')}}",
                "level": "{{oneOf('Junior','Mid','Senior','Lead','Principal')}}",
                "employmentType": "{{oneOf('FULL_TIME','PART_TIME','CONTRACT','INTERN')}}",
                "startDate": "{{date}}",
                "endDate": null,
                "workLocation": "{{oneOf('OFFICE','REMOTE','HYBRID')}}",
                "workSchedule": "{{oneOf('9-5','Flexible','Shift Work')}}"
            },
            "department": {
                "id": "{{long(1,50)}}",
                "name": "{{oneOf('Engineering','Product','Design','Marketing','Sales','HR','Finance')}}",
                "code": "{{string(3,5)}}",
                "parentDepartmentId": "{{long(1,10)}}",
                "headOfDepartment": "{{name}} {{name}}",
                "budget": "{{double(100000,10000000)}}",
                "location": "{{oneOf('San Francisco','New York','Austin','Seattle','Remote')}}"
            },
            "manager": {
                "id": "{{long(1,1000)}}",
                "name": "{{name}} {{name}}",
                "title": "{{oneOf('Engineering Manager','Product Director','VP Engineering','CTO')}}",
                "email": "{{email}}"
            },
            "directReports": [
                {
                    "id": "{{long(1,10000)}}",
                    "name": "{{name}} {{name}}",
                    "title": "{{oneOf('Junior Engineer','Associate Designer','Intern')}}",
                    "startDate": "{{date}}"
                }
            ],
            "salary": {
                "baseSalary": "{{double(50000,300000)}}",
                "currency": "{{oneOf('USD','EUR','GBP','CAD')}}",
                "payFrequency": "{{oneOf('MONTHLY','BI_WEEKLY','WEEKLY')}}",
                "lastRaiseDate": "{{date}}",
                "nextReviewDate": "{{date}}"
            },
            "benefits": {
                "healthInsurance": "{{boolean}}",
                "dentalInsurance": "{{boolean}}",
                "visionInsurance": "{{boolean}}",
                "retirementPlan": "{{boolean}}",
                "paidTimeOff": "{{int(15,30)}}",
                "sickLeave": "{{int(5,15)}}",
                "lifeInsurance": "{{boolean}}"
            },
            "performance": [
                {
                    "id": "{{long(1,5000)}}",
                    "reviewPeriod": "{{oneOf('Q1 2023','Q2 2023','Q3 2023','Q4 2023')}}",
                    "overallRating": "{{double(1.0,5.0)}}",
                    "goals": [
                        {
                            "id": "{{long(1,10000)}}",
                            "description": "{{string(50,200)}}",
                            "status": "{{oneOf('NOT_STARTED','IN_PROGRESS','COMPLETED','CANCELLED')}}",
                            "targetDate": "{{date}}",
                            "completionDate": "{{date}}"
                        }
                    ],
                    "feedback": "{{string(100,500)}}",
                    "reviewDate": "{{date}}",
                    "reviewerId": "{{long(1,1000)}}"
                }
            ],
            "timeOff": {
                "availableVacationDays": "{{int(15,30)}}",
                "usedVacationDays": "{{int(0,20)}}",
                "availableSickDays": "{{int(5,15)}}",
                "usedSickDays": "{{int(0,10)}}",
                "pendingRequests": [
                    {
                        "id": "{{long(1,1000)}}",
                        "type": "{{oneOf('VACATION','SICK','PERSONAL','MATERNITY','PATERNITY')}}",
                        "startDate": "{{date}}",
                        "endDate": "{{date}}",
                        "days": "{{int(1,14)}}",
                        "status": "{{oneOf('PENDING','APPROVED','REJECTED')}}",
                        "reason": "{{string(20,100)}}"
                    }
                ]
            },
            "skills": [
                {
                    "id": "{{long(1,1000)}}",
                    "name": "{{oneOf('Java','Python','JavaScript','React','Node.js','AWS','Docker','Kubernetes')}}",
                    "category": "{{oneOf('Programming','Cloud','DevOps','Database','Frontend','Backend')}}",
                    "proficiencyLevel": "{{oneOf('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT')}}",
                    "yearsOfExperience": "{{int(1,15)}}",
                    "lastUsed": "{{date}}"
                },
                {
                    "id": "{{long(1001,2000)}}",
                    "name": "{{oneOf('Project Management','Leadership','Communication','Problem Solving')}}",
                    "category": "{{oneOf('Soft Skills','Management','Communication')}}",
                    "proficiencyLevel": "{{oneOf('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT')}}",
                    "yearsOfExperience": "{{int(1,10)}}",
                    "lastUsed": "{{date}}"
                }
            ],
            "certifications": [
                {
                    "id": "{{long(1,500)}}",
                    "name": "{{oneOf('AWS Certified Solutions Architect','PMP','Scrum Master','Google Cloud Professional')}}",
                    "issuingOrganization": "{{oneOf('AWS','PMI','Scrum Alliance','Google')}}",
                    "issueDate": "{{date}}",
                    "expiryDate": "{{date}}",
                    "credentialId": "CERT-{{int(100000,999999)}}"
                }
            ]
        }
        """.trimIndent()

        val employee = mock<Employee>(template)

        assertNotNull(employee, "员工信息不应为null")

        // 验证员工基本信息
        assertTrue(employee.employeeNumber.startsWith("EMP"), "员工编号应以EMP开头")
        assertNotNull(employee.personalInfo, "个人信息不应为null")
        assertTrue(employee.personalInfo.firstName.isNotEmpty(), "名字不应为空")
        assertTrue(
            employee.personalInfo.nationality in listOf("US", "CA", "UK", "DE", "FR", "IN", "CN"),
            "国籍应在指定选项中"
        )

        // 验证工作信息
        assertNotNull(employee.jobInfo, "工作信息不应为null")
        assertTrue(
            employee.jobInfo.employmentType in listOf("FULL_TIME", "PART_TIME", "CONTRACT", "INTERN"),
            "雇佣类型应在指定选项中"
        )
        assertTrue(employee.jobInfo.workLocation in listOf("OFFICE", "REMOTE", "HYBRID"), "工作地点应在指定选项中")

        // 验证部门信息
        assertNotNull(employee.department, "部门信息不应为null")
        assertTrue(employee.department.budget > BigDecimal.ZERO, "部门预算应大于0")

        // 验证薪资信息
        assertNotNull(employee.salary, "薪资信息不应为null")
        assertTrue(employee.salary.baseSalary >= BigDecimal("50000"), "基本薪资应至少50000")
        assertTrue(employee.salary.payFrequency in listOf("MONTHLY", "BI_WEEKLY", "WEEKLY"), "薪资频率应在指定选项中")

        // 验证福利信息
        assertNotNull(employee.benefits, "福利信息不应为null")
        assertTrue(employee.benefits.paidTimeOff >= 15, "带薪假期应至少15天")

        // 验证绩效评估
        assertNotNull(employee.performance, "绩效评估不应为null")
        assertTrue(employee.performance.isNotEmpty(), "应至少有一次绩效评估")
        employee.performance.forEach { review ->
            assertTrue(review.overallRating >= 1.0 && review.overallRating <= 5.0, "总体评分应在1-5范围内")
            assertTrue(review.goals.isNotEmpty(), "应至少有一个目标")
        }

        // 验证技能
        assertNotNull(employee.skills, "技能列表不应为null")
        assertEquals(2, employee.skills.size, "应有2个技能")
        employee.skills.forEach { skill ->
            assertTrue(
                skill.proficiencyLevel in listOf("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"),
                "技能水平应在指定选项中"
            )
            assertTrue(skill.yearsOfExperience > 0, "经验年数应大于0")
        }

        // 验证认证
        assertNotNull(employee.certifications, "认证列表不应为null")
        assertTrue(employee.certifications.isNotEmpty(), "应至少有一个认证")
        employee.certifications.forEach { cert ->
            assertTrue(cert.credentialId.startsWith("CERT-"), "认证ID应以CERT-开头")
        }

        logger.info("生成的员工: ${employee.personalInfo.firstName} ${employee.personalInfo.lastName} (${employee.employeeNumber})")
    }

    // ==================== 性能基准测试 ====================

    @Test
    fun testComplexScenarioPerformance() {
        logger.info("测试复杂场景性能...")

        val scenarios = listOf(
            "ECommerceUser" to 10,
            "SocialMediaUser" to 15,
            "Employee" to 20
        )

        scenarios.forEach { (scenarioName, iterations) ->
            val template = when (scenarioName) {
                "ECommerceUser" -> getECommerceTemplate()
                "SocialMediaUser" -> getSocialMediaTemplate()
                "Employee" -> getEmployeeTemplate()
                else -> "{}"
            }

            val startTime = System.currentTimeMillis()

            repeat(iterations) {
                val bean = mock<Map<String, Any>>(template)
                assertNotNull(bean, "$scenarioName Bean不应为null")
            }

            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime
            val avgTime = totalTime.toDouble() / iterations

            logger.info(
                "$scenarioName 场景性能: $iterations 次迭代耗时 ${totalTime}ms (平均: ${
                    String.format(
                        "%.2f",
                        avgTime
                    )
                }ms/次)"
            )

            assertTrue(avgTime < 1000, "$scenarioName 平均处理时间应少于1秒，实际为 ${avgTime}ms")
        }
    }

    private fun getECommerceTemplate(): String =
        "{\"id\": \"{{long}}\", \"username\": \"{{string(10)}}\", \"email\": \"{{email}}\"}"

    private fun getSocialMediaTemplate(): String =
        "{\"id\": \"{{long}}\", \"handle\": \"@{{string(10)}}\", \"displayName\": \"{{name}}\"}"

    private fun getEmployeeTemplate(): String =
        "{\"id\": \"{{long}}\", \"employeeNumber\": \"EMP{{int(10000,99999)}}\", \"personalInfo\": {\"firstName\": \"{{name}}\", \"lastName\": \"{{name}}\"}}"
}