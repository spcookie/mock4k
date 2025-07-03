package io.github.spcookie

import com.google.gson.GsonBuilder

/**
 * Example usage of Mock4K library
 */
fun main() {
    val gson = GsonBuilder().setPrettyPrinting().create()
    
    println("=== Mock4K Example ===")
    println()
    
    // Example 1: Basic usage like Mock.js
    println("1. Basic usage (like Mock.js example):")
    val basicTemplate = mapOf(
        "list|1-10" to listOf(
            mapOf(
                "id|+1" to 1
            )
        )
    )
    val basicResult = Mock.mock(basicTemplate)
    println(gson.toJson(basicResult))
    println()
    
    // Example 2: String rules
    println("2. String rules:")
    val stringTemplate = mapOf(
        "title|1-3" to "Hello ",
        "description|5" to "*"
    )
    val stringResult = Mock.mock(stringTemplate)
    println(gson.toJson(stringResult))
    println()
    
    // Example 3: Number rules
    println("3. Number rules:")
    val numberTemplate = mapOf(
        "age|18-65" to 25,
        "price|100-999.2" to 199.99,
        "score|1-100.1-3" to 85.5,
        "counter|+1" to 1000
    )
    val numberResult = Mock.mock(numberTemplate)
    println(gson.toJson(numberResult))
    println()
    
    // Example 4: Boolean rules
    println("4. Boolean rules:")
    val booleanTemplate = mapOf(
        "isActive|1" to true,
        "isEnabled|1-3" to false
    )
    val booleanResult = Mock.mock(booleanTemplate)
    println(gson.toJson(booleanResult))
    println()
    
    // Example 5: Array rules
    println("5. Array rules:")
    val arrayTemplate = mapOf(
        "colors|1" to listOf("red", "green", "blue", "yellow"),
        "numbers|3-5" to listOf(1, 2, 3),
        "tags|2" to listOf("kotlin", "mock", "data", "test")
    )
    val arrayResult = Mock.mock(arrayTemplate)
    println(gson.toJson(arrayResult))
    println()
    
    // Example 6: Object rules
    println("6. Object rules:")
    val objectTemplate = mapOf(
        "config|2-3" to mapOf(
            "debug" to true,
            "timeout" to 5000,
            "retries" to 3,
            "cache" to false,
            "logging" to true
        )
    )
    val objectResult = Mock.mock(objectTemplate)
    println(gson.toJson(objectResult))
    println()
    
    // Example 7: Placeholders
    println("7. Placeholders:")
    val placeholderTemplate = mapOf(
        "name" to "@NAME",
        "email" to "@EMAIL",
        "website" to "@URL",
        "birthday" to "@DATE(yyyy-MM-dd)",
        "bio" to "@SENTENCE(10, 20)",
        "avatar" to "@IMAGE(100x100)",
        "id" to "@GUID",
        "ip" to "@IP",
        "color" to "@COLOR"
    )
    val placeholderResult = Mock.mock(placeholderTemplate)
    println(gson.toJson(placeholderResult))
    println()
    
    // Example 8: Complex nested structure
    println("8. Complex nested structure:")
    val complexTemplate = mapOf(
        "users|3-5" to listOf(
            mapOf(
                "id|+1" to 1,
                "name" to "@NAME",
                "email" to "@EMAIL",
                "age|18-65" to 25,
                "isActive|1" to true,
                "profile" to mapOf(
                    "avatar" to "@IMAGE(64x64)",
                    "bio" to "@SENTENCE(5, 15)",
                    "website" to "@URL",
                    "location" to mapOf(
                        "city" to "@WORD",
                        "country" to "@WORD"
                    )
                ),
                "posts|1-3" to listOf(
                    mapOf(
                        "id|+1" to 100,
                        "title" to "@TITLE(3, 8)",
                        "content" to "@PARAGRAPH(2, 5)",
                        "tags|1-4" to listOf("kotlin", "programming", "mock", "data", "test"),
                        "createdAt" to "@DATETIME",
                        "likes|0-100" to 10
                    )
                )
            )
        ),
        "meta" to mapOf(
            "total|+1" to 0,
            "page|1-10" to 1,
            "timestamp" to "@NOW"
        )
    )
    val complexResult = Mock.mock(complexTemplate)
    println(gson.toJson(complexResult))
    println()
    
    // Example 9: Chinese content
    println("9. Chinese content:")
    val chineseTemplate = mapOf(
        "name" to "@CNAME",
        "title" to "@CTITLE(3, 8)",
        "content" to "@CPARAGRAPH(2, 4)",
        "description" to "@CSENTENCE(10, 20)"
    )
    val chineseResult = Mock.mock(chineseTemplate)
    println(gson.toJson(chineseResult))
    println()
    
    // Example 10: Using Mock.Random directly
    println("10. Using Mock.Random directly:")
    println("Random name: ${Mock.Random.name()}")
    println("Random email: ${Mock.Random.email()}")
    println("Random integer (1-100): ${Mock.Random.integer(1, 100)}")
    println("Random string (8 chars): ${Mock.Random.string(8)}")
    println("Random date: ${Mock.Random.date()}")
    println("Random boolean: ${Mock.Random.boolean()}")
    println("Random color: ${Mock.Random.color()}")
    println("Random GUID: ${Mock.Random.guid()}")
    println("Random name: ${Mock.Random.name()}")
    println("Random pick from list: ${Mock.Random.pick(listOf("apple", "banana", "orange"))}")
}