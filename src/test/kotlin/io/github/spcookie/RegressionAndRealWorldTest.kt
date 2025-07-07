package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertNotNull

/**
 * 回归测试和真实世界应用场景测试
 */
class RegressionAndRealWorldTest {

    private val logger = LoggerFactory.getLogger(RegressionAndRealWorldTest::class.java)
    // 使用Mocks对象代替BeanMock实例

    // ==================== API响应模拟测试 ====================

    data class ApiResponse<T>(
        val success: Boolean,
        val code: Int,
        val message: String,
        val data: T?,
        val timestamp: String,
        val requestId: String,
        val pagination: Pagination?
    )

    data class Pagination(
        val page: Int,
        val size: Int,
        val total: Long,
        val totalPages: Int,
        val hasNext: Boolean,
        val hasPrevious: Boolean
    )

    data class UserListResponse(
        val users: List<ApiUser>
    )

    data class ApiUser(
        val id: Long,
        val username: String,
        val email: String,
        val status: String,
        val createdAt: String,
        val lastLoginAt: String?,
        val profile: UserProfile
    )

    data class UserProfile(
        val firstName: String,
        val lastName: String,
        val avatar: String?,
        val bio: String?
    )

    @Test
    fun testApiResponseMocking() {
        logger.info("测试API响应模拟...")

        val template = """
        {
            "success": "{{boolean}}",
            "code": "{{oneOf(200,201,400,401,403,404,500)}}",
            "message": "{{oneOf('Success','Created','Bad Request','Unauthorized','Forbidden','Not Found','Internal Server Error')}}",
            "data": {
                "users": [
                    {
                        "id": "{{long(1,10000)}}",
                        "username": "{{string(8,16)}}",
                        "email": "{{email}}",
                        "status": "{{oneOf('ACTIVE','INACTIVE','SUSPENDED','PENDING')}}",
                        "createdAt": "{{datetime}}",
                        "lastLoginAt": "{{datetime}}",
                        "profile": {
                            "firstName": "{{name}}",
                            "lastName": "{{name}}",
                            "avatar": "{{string(50)}}",
                            "bio": "{{string(100,200)}}"
                        }
                    },
                    {
                        "id": "{{long(10001,20000)}}",
                        "username": "{{string(8,16)}}",
                        "email": "{{email}}",
                        "status": "{{oneOf('ACTIVE','INACTIVE','SUSPENDED','PENDING')}}",
                        "createdAt": "{{datetime}}",
                        "lastLoginAt": null,
                        "profile": {
                            "firstName": "{{name}}",
                            "lastName": "{{name}}",
                            "avatar": null,
                            "bio": null
                        }
                    }
                ]
            },
            "timestamp": "{{datetime}}",
            "requestId": "REQ-{{int(100000,999999)}}",
            "pagination": {
                "page": "{{int(1,100)}}",
                "size": "{{int(10,50)}}",
                "total": "{{long(1,10000)}}",
                "totalPages": "{{int(1,200)}}",
                "hasNext": "{{boolean}}",
                "hasPrevious": "{{boolean}}"
            }
        }
        """.trimIndent()

        val response = mock<ApiResponse<UserListResponse>>(template)

        assertNotNull(response, "API响应不应为null")

        // 验证响应基本字段
        assertTrue(response.code in listOf(200, 201, 400, 401, 403, 404, 500), "响应码应在指定选项中")
        assertTrue(response.requestId.startsWith("REQ-"), "请求ID应以REQ-开头")

        // 验证数据字段
        assertNotNull(response.data, "数据字段不应为null")
        assertEquals(2, response.data!!.users.size, "应有2个用户")

        // 验证用户数据
        response.data.users.forEachIndexed { index: Int, user: ApiUser ->
            assertTrue(user.username.length >= 8 && user.username.length <= 16, "用户名长度应在8-16范围内")
            assertTrue(user.email.contains("@"), "邮箱应包含@符号")
            assertTrue(user.status in listOf("ACTIVE", "INACTIVE", "SUSPENDED", "PENDING"), "用户状态应在指定选项中")

            if (index == 0) {
                assertTrue(user.id >= 1 && user.id <= 10000, "第1个用户ID应在1-10000范围内")
                assertNotNull(user.lastLoginAt, "第1个用户应有最后登录时间")
                assertNotNull(user.profile.avatar, "第1个用户应有头像")
                assertNotNull(user.profile.bio, "第1个用户应有简介")
            } else {
                assertTrue(user.id >= 10001 && user.id <= 20000, "第2个用户ID应在10001-20000范围内")
                assertNull(user.lastLoginAt, "第2个用户不应有最后登录时间")
                assertNull(user.profile.avatar, "第2个用户不应有头像")
                assertNull(user.profile.bio, "第2个用户不应有简介")
            }
        }

        // 验证分页信息
        assertNotNull(response.pagination, "分页信息不应为null")
        assertTrue(response.pagination!!.page >= 1 && response.pagination.page <= 100, "页码应在1-100范围内")
        assertTrue(response.pagination.size >= 10 && response.pagination.size <= 50, "页面大小应在10-50范围内")

        logger.info("API响应模拟测试通过")
        logger.info("生成的API响应: code=${response.code}, users=${response.data?.users?.size}, requestId=${response.requestId}")
    }

    // ==================== 数据库实体模拟测试 ====================

    data class DatabaseEntity(
        val id: Long?,
        val createdAt: LocalDateTime?,
        val updatedAt: LocalDateTime?,
        val version: Int,
        val deleted: Boolean,
        val createdBy: String,
        val updatedBy: String?
    )

    data class ProductEntity(
        val id: Long?,
        val sku: String,
        val name: String,
        val description: String?,
        val price: BigDecimal,
        val categoryId: Long,
        val brandId: Long,
        val inventory: Int,
        val status: String,
        val attributes: Map<String, String>,
        val tags: List<String>,
        val createdAt: LocalDateTime?,
        val updatedAt: LocalDateTime?,
        val version: Int,
        val deleted: Boolean
    )

    @Test
    fun testDatabaseEntityMocking() {
        logger.info("测试数据库实体模拟...")

        val template = """
        {
            "id": "{{long(1,100000)}}",
            "sku": "SKU-{{int(10000,99999)}}",
            "name": "{{string(20,50)}}",
            "description": "{{string(100,300)}}",
            "price": "{{double(1.0,10000.0)}}",
            "categoryId": "{{long(1,1000)}}",
            "brandId": "{{long(1,500)}}",
            "inventory": "{{int(0,1000)}}",
            "status": "{{oneOf('ACTIVE','INACTIVE','DISCONTINUED','OUT_OF_STOCK')}}",
            "attributes": {
                "color": "{{oneOf('Red','Blue','Green','Black','White','Yellow')}}",
                "size": "{{oneOf('XS','S','M','L','XL','XXL')}}",
                "material": "{{oneOf('Cotton','Polyester','Wool','Silk','Leather')}}",
                "weight": "{{double(0.1,10.0)}} kg"
            },
            "tags": ["{{string(8)}}", "{{string(8)}}", "{{string(8)}}"],
            "createdAt": "{{datetime}}",
            "updatedAt": "{{datetime}}",
            "version": "{{int(1,100)}}",
            "deleted": false
        }
        """.trimIndent()

        val product = mock<ProductEntity>(template)

        assertNotNull(product, "产品实体不应为null")

        // 验证基本字段
        assertTrue(product.id!! >= 1 && product.id <= 100000, "产品ID应在1-100000范围内")
        assertTrue(product.sku.startsWith("SKU-"), "SKU应以SKU-开头")
        assertTrue(product.name.length >= 20 && product.name.length <= 50, "产品名称长度应在20-50范围内")
        assertTrue(
            product.price >= BigDecimal("1.0") && product.price <= BigDecimal("10000.0"),
            "价格应在1.0-10000.0范围内"
        )

        // 验证状态
        assertTrue(product.status in listOf("ACTIVE", "INACTIVE", "DISCONTINUED", "OUT_OF_STOCK"), "状态应在指定选项中")

        // 验证属性
        assertEquals(4, product.attributes.size, "应有4个属性")
        assertTrue(
            product.attributes["color"] in listOf("Red", "Blue", "Green", "Black", "White", "Yellow"),
            "颜色应在指定选项中"
        )
        assertTrue(product.attributes["size"] in listOf("XS", "S", "M", "L", "XL", "XXL"), "尺寸应在指定选项中")
        assertTrue(
            product.attributes["material"] in listOf("Cotton", "Polyester", "Wool", "Silk", "Leather"),
            "材质应在指定选项中"
        )
        assertTrue(product.attributes["weight"]!!.endsWith(" kg"), "重量应以' kg'结尾")

        // 验证标签
        assertEquals(3, product.tags.size, "应有3个标签")
        product.tags.forEach { tag: String ->
            assertEquals(8, tag.length, "标签长度应为8")
        }

        // 验证审计字段
        assertNotNull(product.createdAt, "创建时间不应为null")
        assertNotNull(product.updatedAt, "更新时间不应为null")
        assertTrue(product.version >= 1 && product.version <= 100, "版本号应在1-100范围内")
        assertFalse(product.deleted, "删除标志应为false")

        logger.info("数据库实体模拟测试通过")
        logger.info("生成的产品: ${product.name} (${product.sku}), 价格: ${product.price}")
    }

    // ==================== 配置文件模拟测试 ====================

    data class ApplicationConfig(
        val server: ServerConfig,
        val database: DatabaseConfig,
        val redis: RedisConfig,
        val logging: LoggingConfig,
        val security: SecurityConfig,
        val features: FeatureFlags
    )

    data class ServerConfig(
        val port: Int,
        val host: String,
        val contextPath: String,
        val maxThreads: Int,
        val connectionTimeout: Int,
        val ssl: SslConfig?
    )

    data class SslConfig(
        val enabled: Boolean,
        val keyStore: String,
        val keyStorePassword: String,
        val protocol: String
    )

    data class DatabaseConfig(
        val url: String,
        val username: String,
        val password: String,
        val driverClassName: String,
        val maxPoolSize: Int,
        val minPoolSize: Int,
        val connectionTimeout: Int,
        val idleTimeout: Int
    )

    data class RedisConfig(
        val host: String,
        val port: Int,
        val password: String?,
        val database: Int,
        val timeout: Int,
        val maxConnections: Int
    )

    data class LoggingConfig(
        val level: String,
        val pattern: String,
        val file: String,
        val maxFileSize: String,
        val maxHistory: Int
    )

    data class SecurityConfig(
        val jwtSecret: String,
        val jwtExpiration: Int,
        val passwordMinLength: Int,
        val maxLoginAttempts: Int,
        val lockoutDuration: Int
    )

    data class FeatureFlags(
        val enableNewUI: Boolean,
        val enableAdvancedSearch: Boolean,
        val enableNotifications: Boolean,
        val enableAnalytics: Boolean,
        val maintenanceMode: Boolean
    )

    @Test
    fun testApplicationConfigMocking() {
        logger.info("测试应用配置模拟...")

        val template = """
        {
            "server": {
                "port": "{{int(8080,9090)}}",
                "host": "{{oneOf('localhost','0.0.0.0','127.0.0.1')}}",
                "contextPath": "/{{string(5,10)}}",
                "maxThreads": "{{int(100,500)}}",
                "connectionTimeout": "{{int(5000,30000)}}",
                "ssl": {
                    "enabled": "{{boolean}}",
                    "keyStore": "{{string(20)}}.jks",
                    "keyStorePassword": "{{string(16)}}",
                    "protocol": "{{oneOf('TLSv1.2','TLSv1.3')}}"
                }
            },
            "database": {
                "url": "jdbc:{{oneOf('mysql','postgresql','h2')}}://{{oneOf('localhost','db.example.com')}}:{{int(3306,5432)}}/{{string(8)}}",
                "username": "{{string(8,16)}}",
                "password": "{{string(16,32)}}",
                "driverClassName": "{{oneOf('com.mysql.cj.jdbc.Driver','org.postgresql.Driver','org.h2.Driver')}}",
                "maxPoolSize": "{{int(10,50)}}",
                "minPoolSize": "{{int(1,10)}}",
                "connectionTimeout": "{{int(10000,60000)}}",
                "idleTimeout": "{{int(300000,600000)}}"
            },
            "redis": {
                "host": "{{oneOf('localhost','redis.example.com','127.0.0.1')}}",
                "port": "{{int(6379,6389)}}",
                "password": "{{string(20)}}",
                "database": "{{int(0,15)}}",
                "timeout": "{{int(1000,10000)}}",
                "maxConnections": "{{int(10,100)}}"
            },
            "logging": {
                "level": "{{oneOf('DEBUG','INFO','WARN','ERROR')}}",
                "pattern": "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n",
                "file": "logs/{{string(10)}}.log",
                "maxFileSize": "{{int(10,100)}}MB",
                "maxHistory": "{{int(7,30)}}"
            },
            "security": {
                "jwtSecret": "{{string(32)}}",
                "jwtExpiration": "{{int(3600,86400)}}",
                "passwordMinLength": "{{int(8,16)}}",
                "maxLoginAttempts": "{{int(3,10)}}",
                "lockoutDuration": "{{int(300,3600)}}"
            },
            "features": {
                "enableNewUI": "{{boolean}}",
                "enableAdvancedSearch": "{{boolean}}",
                "enableNotifications": "{{boolean}}",
                "enableAnalytics": "{{boolean}}",
                "maintenanceMode": false
            }
        }
        """.trimIndent()

        val config = mock<ApplicationConfig>(template)

        assertNotNull(config, "应用配置不应为null")

        // 验证服务器配置
        assertTrue(config.server.port >= 8080 && config.server.port <= 9090, "服务器端口应在8080-9090范围内")
        assertTrue(config.server.host in listOf("localhost", "0.0.0.0", "127.0.0.1"), "服务器主机应在指定选项中")
        assertTrue(config.server.contextPath.startsWith("/"), "上下文路径应以/开头")
        assertTrue(config.server.maxThreads >= 100 && config.server.maxThreads <= 500, "最大线程数应在100-500范围内")

        // 验证SSL配置
        assertNotNull(config.server.ssl, "SSL配置不应为null")
        assertTrue(config.server.ssl!!.keyStore.endsWith(".jks"), "密钥库文件应以.jks结尾")
        assertTrue(config.server.ssl.protocol in listOf("TLSv1.2", "TLSv1.3"), "SSL协议应在指定选项中")

        // 验证数据库配置
        assertTrue(config.database.url.startsWith("jdbc:"), "数据库URL应以jdbc:开头")
        assertTrue(
            config.database.maxPoolSize >= 10 && config.database.maxPoolSize <= 50,
            "最大连接池大小应在10-50范围内"
        )
        assertTrue(
            config.database.minPoolSize >= 1 && config.database.minPoolSize <= 10,
            "最小连接池大小应在1-10范围内"
        )

        // 验证Redis配置
        assertTrue(config.redis.port >= 6379 && config.redis.port <= 6389, "Redis端口应在6379-6389范围内")
        assertTrue(config.redis.database >= 0 && config.redis.database <= 15, "Redis数据库索引应在0-15范围内")

        // 验证日志配置
        assertTrue(config.logging.level in listOf("DEBUG", "INFO", "WARN", "ERROR"), "日志级别应在指定选项中")
        assertTrue(config.logging.file.startsWith("logs/"), "日志文件路径应以logs/开头")
        assertTrue(config.logging.file.endsWith(".log"), "日志文件应以.log结尾")
        assertTrue(config.logging.maxFileSize.endsWith("MB"), "最大文件大小应以MB结尾")

        // 验证安全配置
        assertEquals(32, config.security.jwtSecret.length, "JWT密钥长度应为32")
        assertTrue(
            config.security.jwtExpiration >= 3600 && config.security.jwtExpiration <= 86400,
            "JWT过期时间应在3600-86400范围内"
        )
        assertTrue(
            config.security.passwordMinLength >= 8 && config.security.passwordMinLength <= 16,
            "密码最小长度应在8-16范围内"
        )

        // 验证功能标志
        assertNotNull(config.features, "功能标志不应为null")
        assertFalse(config.features.maintenanceMode, "维护模式应为false")

        logger.info("应用配置模拟测试通过")
        logger.info("生成的配置: server.port=${config.server.port}, database.maxPoolSize=${config.database.maxPoolSize}, logging.level=${config.logging.level}")
    }

    // ==================== 测试数据生成场景 ====================

    data class TestDataSet(
        val users: List<TestUser>,
        val products: List<TestProduct>,
        val orders: List<TestOrder>,
        val metadata: TestMetadata
    )

    data class TestUser(
        val id: Long,
        val username: String,
        val email: String,
        val age: Int,
        val country: String,
        val registrationDate: String
    )

    data class TestProduct(
        val id: Long,
        val name: String,
        val price: Double,
        val category: String,
        val inStock: Boolean
    )

    data class TestOrder(
        val id: Long,
        val userId: Long,
        val productIds: List<Long>,
        val total: Double,
        val status: String,
        val orderDate: String
    )

    data class TestMetadata(
        val generatedAt: String,
        val version: String,
        val totalRecords: Int,
        val environment: String
    )

    @Test
    fun testTestDataGeneration() {
        logger.info("测试测试数据生成场景...")

        val template = """
        {
            "users": [
                {
                    "id": "{{long(1,1000)}}",
                    "username": "user_{{int(1000,9999)}}",
                    "email": "{{string(8)}}@test.com",
                    "age": "{{int(18,80)}}",
                    "country": "{{oneOf('US','CA','UK','DE','FR','JP','AU')}}",
                    "registrationDate": "{{date}}"
                },
                {
                    "id": "{{long(1001,2000)}}",
                    "username": "user_{{int(1000,9999)}}",
                    "email": "{{string(8)}}@test.com",
                    "age": "{{int(18,80)}}",
                    "country": "{{oneOf('US','CA','UK','DE','FR','JP','AU')}}",
                    "registrationDate": "{{date}}"
                },
                {
                    "id": "{{long(2001,3000)}}",
                    "username": "user_{{int(1000,9999)}}",
                    "email": "{{string(8)}}@test.com",
                    "age": "{{int(18,80)}}",
                    "country": "{{oneOf('US','CA','UK','DE','FR','JP','AU')}}",
                    "registrationDate": "{{date}}"
                }
            ],
            "products": [
                {
                    "id": "{{long(1,500)}}",
                    "name": "{{string(15,30)}}",
                    "price": "{{double(9.99,999.99)}}",
                    "category": "{{oneOf('Electronics','Clothing','Books','Home','Sports')}}",
                    "inStock": "{{boolean}}"
                },
                {
                    "id": "{{long(501,1000)}}",
                    "name": "{{string(15,30)}}",
                    "price": "{{double(9.99,999.99)}}",
                    "category": "{{oneOf('Electronics','Clothing','Books','Home','Sports')}}",
                    "inStock": "{{boolean}}"
                }
            ],
            "orders": [
                {
                    "id": "{{long(1,10000)}}",
                    "userId": "{{long(1,3000)}}",
                    "productIds": ["{{long(1,1000)}}", "{{long(1,1000)}}"],
                    "total": "{{double(19.98,1999.98)}}",
                    "status": "{{oneOf('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')}}",
                    "orderDate": "{{datetime}}"
                },
                {
                    "id": "{{long(10001,20000)}}",
                    "userId": "{{long(1,3000)}}",
                    "productIds": ["{{long(1,1000)}}"],
                    "total": "{{double(9.99,999.99)}}",
                    "status": "{{oneOf('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')}}",
                    "orderDate": "{{datetime}}"
                }
            ],
            "metadata": {
                "generatedAt": "{{datetime}}",
                "version": "{{oneOf('1.0.0','1.1.0','1.2.0','2.0.0')}}",
                "totalRecords": "{{int(100,10000)}}",
                "environment": "{{oneOf('TEST','STAGING','DEVELOPMENT')}}"
            }
        }
        """.trimIndent()

        @Suppress("UNCHECKED_CAST")
        val testData = mock(template) as Map<String, Any>

        // 手动转换为TestDataSet
        @Suppress("UNCHECKED_CAST")
        val users = (testData["users"] as List<Map<String, Any>>).map { userMap ->
            TestUser(
                id = (userMap["id"] as Number).toLong(),
                username = userMap["username"] as String,
                email = userMap["email"] as String,
                age = (userMap["age"] as Number).toInt(),
                country = userMap["country"] as String,
                registrationDate = userMap["registrationDate"] as String
            )
        }

        @Suppress("UNCHECKED_CAST")
        val products = (testData["products"] as List<Map<String, Any>>).map { productMap ->
            TestProduct(
                id = (productMap["id"] as Number).toLong(),
                name = productMap["name"] as String,
                price = (productMap["price"] as Number).toDouble(),
                category = productMap["category"] as String,
                inStock = productMap["inStock"] as Boolean
            )
        }

        @Suppress("UNCHECKED_CAST")
        val orders = (testData["orders"] as List<Map<String, Any>>).map { orderMap ->
            @Suppress("UNCHECKED_CAST")
            val productIds = (orderMap["productIds"] as List<Number>).map { it.toLong() }
            TestOrder(
                id = (orderMap["id"] as Number).toLong(),
                userId = (orderMap["userId"] as Number).toLong(),
                productIds = productIds,
                total = (orderMap["total"] as Number).toDouble(),
                status = orderMap["status"] as String,
                orderDate = orderMap["orderDate"] as String
            )
        }

        @Suppress("UNCHECKED_CAST")
        val metadataMap = testData["metadata"] as Map<String, Any>
        val metadata = TestMetadata(
            generatedAt = metadataMap["generatedAt"] as String,
            version = metadataMap["version"] as String,
            totalRecords = (metadataMap["totalRecords"] as Number).toInt(),
            environment = metadataMap["environment"] as String
        )

        val testDataSet = TestDataSet(users, products, orders, metadata)

        assertNotNull(testData, "测试数据集不应为null")

        // 验证用户数据
        assertEquals(3, testData.users.size, "应有3个测试用户")
        testData.users.forEachIndexed { index: Int, user: TestUser ->
            assertTrue(user.username.startsWith("user_"), "用户名应以user_开头")
            assertTrue(user.email.endsWith("@test.com"), "邮箱应以@test.com结尾")
            assertTrue(user.age >= 18 && user.age <= 80, "年龄应在18-80范围内")
            assertTrue(user.country in listOf("US", "CA", "UK", "DE", "FR", "JP", "AU"), "国家应在指定选项中")

            when (index) {
                0 -> assertTrue(user.id >= 1 && user.id <= 1000, "第1个用户ID应在1-1000范围内")
                1 -> assertTrue(user.id >= 1001 && user.id <= 2000, "第2个用户ID应在1001-2000范围内")
                2 -> assertTrue(user.id >= 2001 && user.id <= 3000, "第3个用户ID应在2001-3000范围内")
            }
        }

        // 验证产品数据
        assertEquals(2, testData.products.size, "应有2个测试产品")
        testData.products.forEachIndexed { index: Int, product: TestProduct ->
            assertTrue(product.name.length >= 15 && product.name.length <= 30, "产品名称长度应在15-30范围内")
            assertTrue(product.price >= 9.99 && product.price <= 999.99, "产品价格应在9.99-999.99范围内")
            assertTrue(
                product.category in listOf("Electronics", "Clothing", "Books", "Home", "Sports"),
                "产品分类应在指定选项中"
            )

            when (index) {
                0 -> assertTrue(product.id >= 1 && product.id <= 500, "第1个产品ID应在1-500范围内")
                1 -> assertTrue(product.id >= 501 && product.id <= 1000, "第2个产品ID应在501-1000范围内")
            }
        }

        // 验证订单数据
        assertEquals(2, testData.orders.size, "应有2个测试订单")
        testData.orders.forEachIndexed { index: Int, order: TestOrder ->
            assertTrue(order.userId >= 1 && order.userId <= 3000, "用户ID应在1-3000范围内")
            assertTrue(
                order.status in listOf("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"),
                "订单状态应在指定选项中"
            )

            when (index) {
                0 -> {
                    assertTrue(order.id >= 1 && order.id <= 10000, "第1个订单ID应在1-10000范围内")
                    assertEquals(2, order.productIds.size, "第1个订单应有2个产品")
                    assertTrue(order.total >= 19.98 && order.total <= 1999.98, "第1个订单总额应在19.98-1999.98范围内")
                }

                1 -> {
                    assertTrue(order.id >= 10001 && order.id <= 20000, "第2个订单ID应在10001-20000范围内")
                    assertEquals(1, order.productIds.size, "第2个订单应有1个产品")
                    assertTrue(order.total >= 9.99 && order.total <= 999.99, "第2个订单总额应在9.99-999.99范围内")
                }
            }
        }

        // 验证元数据
        assertNotNull(testData.metadata, "元数据不应为null")
        assertTrue(testData.metadata.version in listOf("1.0.0", "1.1.0", "1.2.0", "2.0.0"), "版本应在指定选项中")
        assertTrue(
            testData.metadata.totalRecords >= 100 && testData.metadata.totalRecords <= 10000,
            "总记录数应在100-10000范围内"
        )
        assertTrue(testData.metadata.environment in listOf("TEST", "STAGING", "DEVELOPMENT"), "环境应在指定选项中")

        logger.info("测试数据生成场景测试通过")
        logger.info("生成的测试数据: users=${testData.users.size}, products=${testData.products.size}, orders=${testData.orders.size}, environment=${testData.metadata.environment}")
    }

    // ==================== 回归测试 ====================

    @Test
    fun testRegressionScenarios() {
        logger.info("执行回归测试场景...")

        // 场景1: 基本数据类型回归
        val basicTemplate = """
        {
            "stringField": "{{string(10)}}",
            "intField": "{{int(1,100)}}",
            "doubleField": "{{double(1.0,100.0)}}",
            "booleanField": "{{boolean}}",
            "longField": "{{long(1,1000000)}}"
        }
        """.trimIndent()

        repeat(20) {
            @Suppress("UNCHECKED_CAST")
            val basicBean = mock(basicTemplate) as Map<String, Any>
            assertNotNull(basicBean, "基本数据类型Bean不应为null")

            val stringField = basicBean["stringField"] as String
            val intField = basicBean["intField"] as Int
            val doubleField = basicBean["doubleField"] as Double
            val booleanField = basicBean["booleanField"] as Boolean
            val longField = basicBean["longField"] as Long

            assertEquals(10, stringField.length, "字符串字段长度应为10")
            assertTrue(intField >= 1 && intField <= 100, "整数字段应在1-100范围内")
            assertTrue(doubleField >= 1.0 && doubleField <= 100.0, "双精度字段应在1.0-100.0范围内")
            assertTrue(longField >= 1 && longField <= 1000000, "长整数字段应在1-1000000范围内")
        }

        // 场景2: 集合类型回归
        val collectionTemplate = """
        {
            "stringList": ["{{string(5)}}", "{{string(5)}}", "{{string(5)}}"],
            "intList": ["{{int(1,10)}}", "{{int(11,20)}}", "{{int(21,30)}}"],
            "nestedObjects": [
                {
                    "id": "{{long(1,100)}}",
                    "name": "{{string(8)}}"
                },
                {
                    "id": "{{long(101,200)}}",
                    "name": "{{string(8)}}"
                }
            ]
        }
        """.trimIndent()

        repeat(15) {
            @Suppress("UNCHECKED_CAST")
            val collectionBean = mock(collectionTemplate) as Map<String, Any>
            assertNotNull(collectionBean, "集合类型Bean不应为null")

            @Suppress("UNCHECKED_CAST")
            val stringList = collectionBean["stringList"] as List<String>

            @Suppress("UNCHECKED_CAST")
            val intList = collectionBean["intList"] as List<Int>

            @Suppress("UNCHECKED_CAST")
            val nestedObjects = collectionBean["nestedObjects"] as List<Map<String, Any>>

            assertEquals(3, stringList.size, "字符串列表应有3个元素")
            assertEquals(3, intList.size, "整数列表应有3个元素")
            assertEquals(2, nestedObjects.size, "嵌套对象列表应有2个元素")

            stringList.forEach { item ->
                assertEquals(5, item.length, "字符串列表项长度应为5")
            }

            assertTrue(intList[0] >= 1 && intList[0] <= 10, "第1个整数应在1-10范围内")
            assertTrue(intList[1] >= 11 && intList[1] <= 20, "第2个整数应在11-20范围内")
            assertTrue(intList[2] >= 21 && intList[2] <= 30, "第3个整数应在21-30范围内")
        }

        // 场景3: 复杂嵌套回归
        val nestedTemplate = """
        {
            "level1": {
                "level2": {
                    "level3": {
                        "value": "{{string(20)}}",
                        "number": "{{int(1,1000)}}"
                    }
                }
            }
        }
        """.trimIndent()

        repeat(10) {
            @Suppress("UNCHECKED_CAST")
            val nestedBean = mock(nestedTemplate) as Map<String, Any>
            assertNotNull(nestedBean, "嵌套Bean不应为null")

            @Suppress("UNCHECKED_CAST")
            val level1 = nestedBean["level1"] as Map<String, Any>

            @Suppress("UNCHECKED_CAST")
            val level2 = level1["level2"] as Map<String, Any>

            @Suppress("UNCHECKED_CAST")
            val level3 = level2["level3"] as Map<String, Any>

            val value = level3["value"] as String
            val number = level3["number"] as Int

            assertEquals(20, value.length, "嵌套值长度应为20")
            assertTrue(number >= 1 && number <= 1000, "嵌套数字应在1-1000范围内")
        }

        logger.info("回归测试场景完成")
    }

    // ==================== 性能回归测试 ====================

    @Test
    fun testPerformanceRegression() {
        logger.info("执行性能回归测试...")

        val performanceTemplate = """
        {
            "users": [
                {
                    "id": "{{long(1,10000)}}",
                    "profile": {
                        "name": "{{string(20)}}",
                        "details": {
                            "address": "{{string(50)}}",
                            "phone": "{{string(15)}}"
                        }
                    }
                },
                {
                    "id": "{{long(10001,20000)}}",
                    "profile": {
                        "name": "{{string(20)}}",
                        "details": {
                            "address": "{{string(50)}}",
                            "phone": "{{string(15)}}"
                        }
                    }
                }
            ]
        }
        """.trimIndent()

        val iterations = 100
        val startTime = System.currentTimeMillis()

        repeat(iterations) {
            @Suppress("UNCHECKED_CAST")
            val bean = mock(performanceTemplate) as Map<String, Any>
            assertNotNull(bean, "性能测试Bean不应为null")
        }

        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        val avgTime = totalTime.toDouble() / iterations

        logger.info(
            "性能回归测试: $iterations 次迭代耗时 ${totalTime}ms (平均: ${
                String.format(
                    "%.2f",
                    avgTime as Any
                )
            }ms/次)"
        )

        // 性能基准: 平均每次调用应少于100ms
        assertTrue(avgTime < 100, "平均处理时间应少于100ms，实际为 ${avgTime}ms")

        // 总时间基准: 100次迭代应少于5秒
        assertTrue(totalTime < 5000, "100次迭代总时间应少于5秒，实际为 ${totalTime}ms")
    }

    // ==================== 内存使用回归测试 ====================

    @Test
    fun testMemoryUsageRegression() {
        logger.info("执行内存使用回归测试...")

        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        val memoryTemplate = """
        {
            "largeData": {
                "items": [
                    {
                        "data": "{{string(100)}}",
                        "metadata": {
                            "description": "{{string(200)}}",
                            "tags": ["{{string(10)}}", "{{string(10)}}", "{{string(10)}}", "{{string(10)}}", "{{string(10)}}"]
                        }
                    },
                    {
                        "data": "{{string(100)}}",
                        "metadata": {
                            "description": "{{string(200)}}",
                            "tags": ["{{string(10)}}", "{{string(10)}}", "{{string(10)}}", "{{string(10)}}", "{{string(10)}}"]
                        }
                    }
                ]
            }
        }
        """.trimIndent()

        val beans = mutableListOf<Map<String, Any>>()

        repeat(50) {
            @Suppress("UNCHECKED_CAST")
            val bean = mock(memoryTemplate) as Map<String, Any>
            beans.add(bean)
        }

        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = finalMemory - initialMemory
        val memoryUsedMB = memoryUsed / (1024 * 1024)

        logger.info("内存使用回归测试: 生成50个大型对象使用了 ${memoryUsedMB}MB 内存")

        // 内存使用基准: 50个大型对象应少于50MB
        assertTrue(memoryUsedMB < 50, "内存使用应少于50MB，实际为 ${memoryUsedMB}MB")

        // 验证生成的数据
        assertEquals(50, beans.size, "应生成50个Bean")
        beans.forEach { bean ->
            assertNotNull(bean, "Bean不应为null")
        }
    }
}