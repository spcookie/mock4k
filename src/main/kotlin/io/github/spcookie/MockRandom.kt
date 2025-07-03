package io.github.spcookie

import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

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
    fun natural(min: Int = 0, max: Int = Int.MAX_VALUE): Int {
        return random.nextInt(min, max + 1)
    }

    /**
     * Generate random integer
     */
    fun integer(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int {
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
    fun string(
        length: Int = 10,
        pool: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    ): String {
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
     * Generate random word with specified length
     */
    fun word(min: Int = 3, max: Int = 10): String {
        // For length-specific words, use the internationalized word() method
        // and adjust length if needed
        val baseWord = word()
        val targetLength = random.nextInt(min, max + 1)

        return if (baseWord.length >= targetLength) {
            baseWord.take(targetLength)
        } else {
            // Pad with random characters if word is too short
            baseWord + string(targetLength - baseWord.length, "abcdefghijklmnopqrstuvwxyz")
        }
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


    // Names

    // Names are now loaded from configuration files
    private fun getFirstNames(): List<String> {
        val names = getDataList("firstNames")
        return if (names.isNotEmpty()) names else listOf("John", "Jane", "Michael", "Sarah", "David", "Lisa")
    }

    private fun getLastNames(): List<String> {
        val names = getDataList("lastNames")
        return if (names.isNotEmpty()) names else listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia")
    }

    // Names are now loaded from configuration files

    /**
     * Generate random first name
     */
    fun first(): String {
        val names = getFirstNames()
        return names[random.nextInt(names.size)]
    }

    /**
     * Generate random last name
     */
    fun last(): String {
        val names = getLastNames()
        return names[random.nextInt(names.size)]
    }

    /**
     * Generate random full name
     */
    fun name(): String = "${first()} ${last()}"


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
    fun image(
        size: String = "200x200",
        background: String = "cccccc",
        foreground: String = "ffffff",
        text: String? = null
    ): String {
        val imageText = text ?: run {
            val imageTexts = getDataList("imageTexts")
            if (imageTexts.isNotEmpty()) {
                imageTexts[random.nextInt(imageTexts.size)]
            } else {
                "Mock" // fallback
            }
        }
        return "https://via.placeholder.com/$size/$background/$foreground?text=$imageText"
    }

    /**
     * Generate data image URL
     */
    fun dataImage(size: String = "200x200"): String {
        return "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2NjY2NjYyIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSIjZmZmZmZmIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+TW9jazwvdGV4dD48L3N2Zz4="
    }

    // Financial

    // Bank codes are now loaded from configuration files
    private fun getBankCodes(): Map<String, String> {
        val bankNames = getDataList("bankNames")
        val bankCodes = getDataList("bankCodes")
        return if (bankNames.size == bankCodes.size) {
            bankNames.zip(bankCodes).toMap()
        } else {
            // Fallback bank codes
            mapOf(
                "Bank of America" to "4147",
                "JPMorgan Chase" to "4000",
                "Wells Fargo" to "4512",
                "Citibank" to "4011"
            )
        }
    }

    /**
     * Generate random bank card number
     */
    fun bankCard(
        bankName: String? = null,
        bankCode: String? = null,
        length: Int = 19,
        cardType: String = "debit"
    ): String {
        val bankCodesMap = getBankCodes()
        // Determine bank code
        val finalBankCode = when {
            bankCode != null -> bankCode
            bankName != null -> bankCodesMap[bankName] ?: "6225"
            else -> bankCodesMap.values.random()
        }

        // Adjust prefix based on card type
        val prefix = when (cardType) {
            "credit" -> finalBankCode.substring(0, 3) + "1" // Credit cards usually have 1 as 4th digit
            else -> finalBankCode // Debit cards keep original
        }

        // Generate remaining digits (except last check digit)
        val remainingLength = length - prefix.length - 1
        val middlePart = (1..remainingLength).map {
            integer(0, 9).toString()
        }.joinToString("")

        // Front part (excluding check digit)
        val cardWithoutCheck = prefix + middlePart

        // Calculate Luhn check digit
        val checkDigit = calculateLuhnCheckDigit(cardWithoutCheck)

        return cardWithoutCheck + checkDigit
    }

    /**
     * Calculate Luhn check digit
     */
    private fun calculateLuhnCheckDigit(cardNumber: String): String {
        var sum = 0
        var alternate = true

        // Traverse from right to left (excluding last check digit)
        for (i in cardNumber.length - 1 downTo 0) {
            var digit = cardNumber[i].toString().toInt()

            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit = digit / 10 + digit % 10
                }
            }

            sum += digit
            alternate = !alternate
        }

        val checkDigit = (10 - (sum % 10)) % 10
        return checkDigit.toString()
    }

    // Identity

    // Province codes are now loaded from configuration files
    private fun getProvinceCodes(): Map<String, String> {
        val provinceNames = getDataList("provinceNames")
        val provinceCodes = getDataList("provinceCodes")
        return if (provinceNames.size == provinceCodes.size) {
            provinceNames.zip(provinceCodes).toMap()
        } else {
            // Fallback province codes
            mapOf(
                "California" to "06",
                "Texas" to "48",
                "New York" to "36",
                "Florida" to "12"
            )
        }
    }


    /**
     * Calculate ID card check code
     */
    private fun calculateIdCardCheckCode(first17: String): String {
        val weights = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
        val checkCodes = arrayOf("1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2")

        var sum = 0
        for (i in first17.indices) {
            sum += first17[i].toString().toInt() * weights[i]
        }

        val remainder = sum % 11
        return checkCodes[remainder]
    }

    // Phone Numbers

    // Phone prefixes are now loaded from configuration files


    private fun getAreaCodes(): List<String> {
        val areaCodes = getDataList("areaCodes")
        return if (areaCodes.isNotEmpty()) areaCodes else listOf("201", "202", "203", "205", "206", "207")
    }

    /**
     * Generate area code
     * @return Random area code
     */
    fun areaCode(): String {
        val areaCodes = getAreaCodes()
        return areaCodes[random.nextInt(areaCodes.size)]
    }

    /**
     * Generate phone number with enhanced phone type support
     * @param format Custom format string using # as digit placeholder
     * @param phoneType Specific phone type (MOBILE, LANDLINE, TOLL_FREE, PREMIUM)
     * @return Generated phone number string
     */
    fun phoneNumber(
        format: String? = null,
        phoneType: PhoneType? = null
    ): String {
        return when {
            format != null -> generatePhoneWithFormat(format, phoneType)
            else -> {
                // Use internationalized phone format
                val phoneFormats = getDataList("phoneFormats")
                val selectedFormat = if (phoneFormats.isNotEmpty()) {
                    phoneFormats[random.nextInt(phoneFormats.size)]
                } else {
                    "###-###-####" // fallback
                }
                generatePhoneWithFormat(selectedFormat, phoneType)
            }
        }
    }

    /**
     * Generate phone number with specific phone type
     * @param phoneType Phone type
     * @return Generated phone number string
     */
    fun phoneNumber(phoneType: PhoneType): String {
        return phoneNumber(format = null, phoneType = phoneType)
    }

    /**
     * Generate phone number with format and phone type support
     */
    private fun generatePhoneWithFormat(format: String, phoneType: PhoneType? = null): String {
        var result = format
        var prefixUsed = false

        // Replace # with appropriate digits, using phone type prefix if specified
        while (result.contains('#')) {
            val digit = if (!prefixUsed && phoneType != null && shouldUsePhoneTypePrefix(result)) {
                prefixUsed = true
                getPhoneTypePrefix(phoneType)
            } else {
                integer(0, 9).toString()
            }
            result = result.replaceFirst("#", digit)
        }

        return result
    }

    /**
     * Check if we should use phone type prefix based on format
     */
    private fun shouldUsePhoneTypePrefix(format: String): Boolean {
        // Use phone type prefix for mobile formats (starting with 1)
        return format.startsWith("1#") || format.startsWith("+86") && format.contains("1#")
    }

    /**
     * Get phone type prefix based on phone type
     */
    private fun getPhoneTypePrefix(phoneType: PhoneType): String {
        val prefixes = when (phoneType) {
            PhoneType.MOBILE -> getDataList("mobilePrefixes")
            PhoneType.LANDLINE -> getDataList("landlinePrefixes")
            PhoneType.TOLL_FREE -> getDataList("tollFreePrefixes")
            PhoneType.PREMIUM -> getDataList("premiumPrefixes")
        }

        return if (prefixes.isNotEmpty()) {
            prefixes[random.nextInt(prefixes.size)]
        } else {
            // Fallback to default prefixes if configuration is missing
            when (phoneType) {
                PhoneType.MOBILE -> listOf("134", "135", "136", "137", "138", "139")
                PhoneType.LANDLINE -> listOf("010", "020", "021", "022", "023", "024")
                PhoneType.TOLL_FREE -> listOf("800", "888", "877", "866", "855", "844")
                PhoneType.PREMIUM -> listOf("900", "976", "970", "540", "550", "560")
            }[random.nextInt(6)]
        }
    }


    // Internationalization

    /**
     * Set current locale
     * @param locale locale to set
     */
    fun setLocale(locale: Locale) {
        LocaleManager.setLocale(locale)
    }

    /**
     * Get current locale
     * @return current locale
     */
    fun getCurrentLocale(): Locale {
        return LocaleManager.getCurrentLocale()
    }

    /**
     * Get data list from properties
     * @param key property key
     * @return list of data
     */
    private fun getDataList(key: String): List<String> {
        return LocaleManager.getDataList(key)
    }

    // Locale-based data generation methods

    /**
     * Get random word from current locale
     * @return random word
     */
    fun word(): String {
        val words = getDataList("words")
        return if (words.isNotEmpty()) {
            words[random.nextInt(words.size)]
        } else {
            "word" // fallback
        }
    }

    /**
     * Get multiple random words from current locale
     * @param count number of words to generate
     * @return list of random words
     */
    fun words(count: Int): List<String> {
        val words = getDataList("words")
        return if (words.isNotEmpty()) {
            (1..count).map { words[random.nextInt(words.size)] }
        } else {
            (1..count).map { "word$it" } // fallback
        }
    }

    /**
     * Get random city from current locale
     * @return random city name
     */
    fun city(): String {
        val cities = getDataList("cities")
        return if (cities.isNotEmpty()) {
            cities[random.nextInt(cities.size)]
        } else {
            "City" // fallback
        }
    }

    /**
     * Get multiple random cities from current locale
     * @param count number of cities to generate
     * @return list of random city names
     */
    fun cities(count: Int): List<String> {
        val cities = getDataList("cities")
        return if (cities.isNotEmpty()) {
            (1..count).map { cities[random.nextInt(cities.size)] }
        } else {
            (1..count).map { "City$it" } // fallback
        }
    }

    /**
     * Get random company from current locale
     * @return random company name
     */
    fun company(): String {
        val companies = getDataList("companies")
        return if (companies.isNotEmpty()) {
            companies[random.nextInt(companies.size)]
        } else {
            "Company" // fallback
        }
    }

    /**
     * Get multiple random companies from current locale
     * @param count number of companies to generate
     * @return list of random company names
     */
    fun companies(count: Int): List<String> {
        val companies = getDataList("companies")
        return if (companies.isNotEmpty()) {
            (1..count).map { companies[random.nextInt(companies.size)] }
        } else {
            (1..count).map { "Company$it" } // fallback
        }
    }

    /**
     * Get random province from current locale
     * @return random province name
     */
    fun province(): String {
        val provinces = getDataList("provinces")
        return if (provinces.isNotEmpty()) {
            provinces[random.nextInt(provinces.size)]
        } else {
            "Province" // fallback
        }
    }

    /**
     * Get multiple random provinces from current locale
     * @param count number of provinces to generate
     * @return list of random province names
     */
    fun provinces(count: Int): List<String> {
        val provinces = getDataList("provinces")
        return if (provinces.isNotEmpty()) {
            (1..count).map { provinces[random.nextInt(provinces.size)] }
        } else {
            (1..count).map { "Province$it" } // fallback
        }
    }

    /**
     * Get random profession from current locale
     * @return random profession name
     */
    fun profession(): String {
        val professions = getDataList("professions")
        return if (professions.isNotEmpty()) {
            professions[random.nextInt(professions.size)]
        } else {
            "Profession" // fallback
        }
    }

    /**
     * Get multiple random professions from current locale
     * @param count number of professions to generate
     * @return list of random profession names
     */
    fun professions(count: Int): List<String> {
        val professions = getDataList("professions")
        return if (professions.isNotEmpty()) {
            (1..count).map { professions[random.nextInt(professions.size)] }
        } else {
            (1..count).map { "Profession$it" } // fallback
        }
    }

    /**
     * Get random street name from current locale
     * @return random street name
     */
    fun streetName(): String {
        val streetNames = getDataList("streetNames")
        return if (streetNames.isNotEmpty()) {
            streetNames[random.nextInt(streetNames.size)]
        } else {
            "Street" // fallback
        }
    }

    /**
     * Get multiple random street names from current locale
     * @param count number of street names to generate
     * @return list of random street names
     */
    fun streetNames(count: Int): List<String> {
        val streetNames = getDataList("streetNames")
        return if (streetNames.isNotEmpty()) {
            (1..count).map { streetNames[random.nextInt(streetNames.size)] }
        } else {
            (1..count).map { "Street$it" } // fallback
        }
    }

    /**
     * Get random email domain from current locale
     * @return random email domain
     */
    fun emailDomain(): String {
        val domains = getDataList("emailDomains")
        return if (domains.isNotEmpty()) {
            domains[random.nextInt(domains.size)]
        } else {
            "example.com" // fallback
        }
    }

    /**
     * Get multiple random email domains from current locale
     * @param count number of email domains to generate
     * @return list of random email domains
     */
    fun emailDomains(count: Int): List<String> {
        val domains = getDataList("emailDomains")
        return if (domains.isNotEmpty()) {
            (1..count).map { domains[random.nextInt(domains.size)] }
        } else {
            (1..count).map { "example$it.com" } // fallback
        }
    }

    /**
     * Phone number type enumeration for international use
     */
    enum class PhoneType {
        MOBILE,     // Mobile/Cell phone
        LANDLINE,   // Fixed line phone
        TOLL_FREE,  // Toll-free numbers
        PREMIUM     // Premium rate numbers
    }

}