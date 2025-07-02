package com.mock4k.random

import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

/**
 * Random data generator utility
 * Provides methods for generating various types of random data
 */
object MockRandom {
    
    private val random = Random.Default
    
    // Basic types
    
    /**
     * Generate random boolean
     */
    fun boolean(): Boolean = random.nextBoolean()
    
    /**
     * Generate random boolean with probability
     */
    fun boolean(probability: Double): Boolean = random.nextDouble() < probability
    
    /**
     * Generate random natural number (positive integer)
     */
    fun natural(min: Int = 0, max: Int = 9007199254740992): Int {
        return random.nextInt(min, max + 1)
    }
    
    /**
     * Generate random integer
     */
    fun integer(min: Int = -9007199254740992, max: Int = 9007199254740992): Int {
        return random.nextInt(min, max + 1)
    }
    
    /**
     * Generate random float
     */
    fun float(min: Double = -9007199254740992.0, max: Double = 9007199254740992.0): Double {
        return min + random.nextDouble() * (max - min)
    }
    
    /**
     * Generate random character
     */
    fun character(pool: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"): Char {
        return pool[random.nextInt(pool.length)]
    }
    
    /**
     * Generate random string
     */
    fun string(length: Int = 10, pool: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"): String {
        return (1..length).map { character(pool) }.joinToString("")
    }
    
    /**
     * Generate range of integers
     */
    fun range(start: Int, stop: Int, step: Int = 1): List<Int> {
        return (start..stop step step).toList()
    }
    
    // Date and time
    
    /**
     * Generate random date
     */
    fun date(format: String = "yyyy-MM-dd"): String {
        val start = Calendar.getInstance().apply {
            set(1970, 0, 1)
        }.timeInMillis
        val end = System.currentTimeMillis()
        val randomTime = start + (random.nextDouble() * (end - start)).toLong()
        
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(Date(randomTime))
    }
    
    /**
     * Generate random time
     */
    fun time(format: String = "HH:mm:ss"): String {
        val hour = random.nextInt(24)
        val minute = random.nextInt(60)
        val second = random.nextInt(60)
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
        }
        
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(calendar.time)
    }
    
    /**
     * Generate random datetime
     */
    fun datetime(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val start = Calendar.getInstance().apply {
            set(1970, 0, 1)
        }.timeInMillis
        val end = System.currentTimeMillis()
        val randomTime = start + (random.nextDouble() * (end - start)).toLong()
        
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(Date(randomTime))
    }
    
    /**
     * Get current datetime
     */
    fun now(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(Date())
    }
    
    // Text
    
    /**
     * Generate random word
     */
    fun word(min: Int = 3, max: Int = 10): String {
        val length = random.nextInt(min, max + 1)
        return string(length, "abcdefghijklmnopqrstuvwxyz")
    }
    
    /**
     * Generate random sentence
     */
    fun sentence(min: Int = 12, max: Int = 18): String {
        val wordCount = random.nextInt(min, max + 1)
        val words = (1..wordCount).map { word() }
        return words.joinToString(" ").replaceFirstChar { it.uppercase() } + "."
    }
    
    /**
     * Generate random paragraph
     */
    fun paragraph(min: Int = 3, max: Int = 7): String {
        val sentenceCount = random.nextInt(min, max + 1)
        return (1..sentenceCount).map { sentence() }.joinToString(" ")
    }
    
    /**
     * Generate random title
     */
    fun title(min: Int = 3, max: Int = 7): String {
        val wordCount = random.nextInt(min, max + 1)
        return (1..wordCount).map { word().replaceFirstChar { it.uppercase() } }.joinToString(" ")
    }
    
    // Chinese text
    
    /**
     * Generate random Chinese word
     */
    fun cword(min: Int = 1, max: Int = 4): String {
        val chineseChars = "的一是在不了有和人这中大为上个国我以要他时来用们生到作地于出就分对成会可主发年动同工也能下过子说产种面而方后多定行学法所民得经十三之进着等部度家电力里如水化高自二理起小物现实加量都两体制机当使点从业本去把性好应开它合还因由其些然前外天政四日那社义事平形相全表间样与关各重新线内数正心反你明看原又么利比或但质气第向道命此变条只没结解问意建月公无系军很情者最立代想已通并提直题党程展五果料象员革位入常文总次品式活设及管特件长求老头基资边流路级少图山统接知较将组见计别她手角期根论运农指几九区强放决西被干做必战先回则任取据处队南给色光门即保治北造百规热领七海口东导器压志世金增争济阶油思术极交受联什认六共权收证改清己美再采转更单风切打白教速花带安场身车例真务具万每目至达走积示议声报斗完类八离华名确才科张信马节话米整空元况今集温传土许步群广石记需段研界拉林律叫且究观越织装影算低持音众书布复容儿须际商非验连断深难近矿千周委素技备半办青省列习响约支般史感劳便团往酸历市克何除消构府称太准精值号率族维划选标写存候毛亲快效斯院查江型眼王按格养易置派层片始却专状育厂京识适属圆包火住调满县局照参红细引听该铁价严"
        val length = random.nextInt(min, max + 1)
        return (1..length).map { chineseChars[random.nextInt(chineseChars.length)] }.joinToString("")
    }
    
    /**
     * Generate random Chinese sentence
     */
    fun csentence(min: Int = 12, max: Int = 18): String {
        val length = random.nextInt(min, max + 1)
        return cword(length, length) + "。"
    }
    
    /**
     * Generate random Chinese paragraph
     */
    fun cparagraph(min: Int = 3, max: Int = 7): String {
        val sentenceCount = random.nextInt(min, max + 1)
        return (1..sentenceCount).map { csentence() }.joinToString("")
    }
    
    /**
     * Generate random Chinese title
     */
    fun ctitle(min: Int = 3, max: Int = 7): String {
        val length = random.nextInt(min, max + 1)
        return cword(length, length)
    }
    
    // Names
    
    private val firstNames = listOf(
        "James", "John", "Robert", "Michael", "William", "David", "Richard", "Charles", "Joseph", "Thomas",
        "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen"
    )
    
    private val lastNames = listOf(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"
    )
    
    private val chineseFirstNames = listOf(
        "伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀英", "霞", "平"
    )
    
    private val chineseLastNames = listOf(
        "王", "李", "张", "刘", "陈", "杨", "赵", "黄", "周", "吴", "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗"
    )
    
    /**
     * Generate random first name
     */
    fun first(): String = firstNames[random.nextInt(firstNames.size)]
    
    /**
     * Generate random last name
     */
    fun last(): String = lastNames[random.nextInt(lastNames.size)]
    
    /**
     * Generate random full name
     */
    fun name(): String = "${first()} ${last()}"
    
    /**
     * Generate random Chinese first name
     */
    fun cfirst(): String = chineseFirstNames[random.nextInt(chineseFirstNames.size)]
    
    /**
     * Generate random Chinese last name
     */
    fun clast(): String = chineseLastNames[random.nextInt(chineseLastNames.size)]
    
    /**
     * Generate random Chinese full name
     */
    fun cname(): String = "${clast()}${cfirst()}"
    
    // Web
    
    private val domains = listOf("example.com", "test.org", "sample.net", "demo.io", "mock.dev")
    private val tlds = listOf("com", "org", "net", "edu", "gov", "io", "co", "me")
    
    /**
     * Generate random URL
     */
    fun url(): String {
        val protocol = if (random.nextBoolean()) "http" else "https"
        val domain = domains[random.nextInt(domains.size)]
        return "$protocol://$domain"
    }
    
    /**
     * Generate random domain
     */
    fun domain(): String = domains[random.nextInt(domains.size)]
    
    /**
     * Generate random email
     */
    fun email(): String {
        val username = string(random.nextInt(5, 12), "abcdefghijklmnopqrstuvwxyz")
        val domain = domains[random.nextInt(domains.size)]
        return "$username@$domain"
    }
    
    /**
     * Generate random IP address
     */
    fun ip(): String {
        return (1..4).map { random.nextInt(0, 256) }.joinToString(".")
    }
    
    /**
     * Generate random TLD
     */
    fun tld(): String = tlds[random.nextInt(tlds.size)]
    
    // Helper methods
    
    /**
     * Capitalize string
     */
    fun capitalize(str: String): String = str.replaceFirstChar { it.uppercase() }
    
    /**
     * Convert to uppercase
     */
    fun upper(str: String): String = str.uppercase()
    
    /**
     * Convert to lowercase
     */
    fun lower(str: String): String = str.lowercase()
    
    /**
     * Pick random element from list
     */
    fun <T> pick(list: List<T>): T = list[random.nextInt(list.size)]
    
    /**
     * Shuffle list
     */
    fun <T> shuffle(list: List<T>): List<T> = list.shuffled(random)
    
    // Miscellaneous
    
    /**
     * Generate random GUID
     */
    fun guid(): String = UUID.randomUUID().toString()
    
    /**
     * Generate random ID
     */
    fun id(): String = string(24, "abcdefghijklmnopqrstuvwxyz0123456789")
    
    // Color
    
    /**
     * Generate random color
     */
    fun color(): String {
        val r = random.nextInt(256)
        val g = random.nextInt(256)
        val b = random.nextInt(256)
        return String.format("#%02x%02x%02x", r, g, b)
    }
    
    // Image
    
    /**
     * Generate random image URL
     */
    fun image(size: String = "200x200", background: String = "cccccc", foreground: String = "ffffff", text: String = "Mock"): String {
        return "https://via.placeholder.com/$size/$background/$foreground?text=$text"
    }
    
    /**
     * Generate data image URL
     */
    fun dataImage(size: String = "200x200"): String {
        return "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2NjY2NjYyIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSIjZmZmZmZmIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+TW9jazwvdGV4dD48L3N2Zz4="
    }
}