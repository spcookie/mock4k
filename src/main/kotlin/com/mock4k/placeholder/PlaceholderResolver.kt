package com.mock4k.placeholder

import com.mock4k.random.MockRandom
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions

/**
 * Resolver for @placeholder syntax
 */
class PlaceholderResolver(private val random: MockRandom) {
    
    private val placeholderPattern = Regex("@([A-Z_]+)(?:\\(([^)]*)\\))?")
    
    /**
     * Resolve placeholders in a string
     */
    fun resolve(template: String): String {
        var result = template
        
        placeholderPattern.findAll(template).forEach { match ->
            val placeholder = match.value
            val methodName = match.groupValues[1].lowercase()
            val params = match.groupValues[2]
            
            val resolvedValue = resolvePlaceholder(methodName, params)
            result = result.replace(placeholder, resolvedValue.toString())
        }
        
        return result
    }
    
    private fun resolvePlaceholder(methodName: String, params: String): Any {
        return try {
            when (methodName) {
                // Basic
                "boolean" -> random.boolean()
                "natural" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.natural()
                        1 -> random.natural(max = args[0].toInt())
                        2 -> random.natural(args[0].toInt(), args[1].toInt())
                        else -> random.natural()
                    }
                }
                "integer" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.integer()
                        1 -> random.integer(max = args[0].toInt())
                        2 -> random.integer(args[0].toInt(), args[1].toInt())
                        else -> random.integer()
                    }
                }
                "float" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.float()
                        1 -> random.float(max = args[0].toDouble())
                        2 -> random.float(args[0].toDouble(), args[1].toDouble())
                        else -> random.float()
                    }
                }
                "character" -> {
                    val args = parseParams(params)
                    if (args.isNotEmpty()) {
                        random.character(args[0])
                    } else {
                        random.character()
                    }
                }
                "string" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.string()
                        1 -> random.string(args[0].toInt())
                        2 -> random.string(args[0].toInt(), args[1])
                        else -> random.string()
                    }
                }
                "range" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        2 -> random.range(args[0].toInt(), args[1].toInt())
                        3 -> random.range(args[0].toInt(), args[1].toInt(), args[2].toInt())
                        else -> random.range(1, 10)
                    }
                }
                
                // Date and time
                "date" -> {
                    val args = parseParams(params)
                    if (args.isNotEmpty()) {
                        random.date(args[0])
                    } else {
                        random.date()
                    }
                }
                "time" -> {
                    val args = parseParams(params)
                    if (args.isNotEmpty()) {
                        random.time(args[0])
                    } else {
                        random.time()
                    }
                }
                "datetime" -> {
                    val args = parseParams(params)
                    if (args.isNotEmpty()) {
                        random.datetime(args[0])
                    } else {
                        random.datetime()
                    }
                }
                "now" -> {
                    val args = parseParams(params)
                    if (args.isNotEmpty()) {
                        random.now(args[0])
                    } else {
                        random.now()
                    }
                }
                
                // Text
                "word" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.word()
                        1 -> random.word(max = args[0].toInt())
                        2 -> random.word(args[0].toInt(), args[1].toInt())
                        else -> random.word()
                    }
                }
                "sentence" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.sentence()
                        1 -> random.sentence(max = args[0].toInt())
                        2 -> random.sentence(args[0].toInt(), args[1].toInt())
                        else -> random.sentence()
                    }
                }
                "paragraph" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.paragraph()
                        1 -> random.paragraph(max = args[0].toInt())
                        2 -> random.paragraph(args[0].toInt(), args[1].toInt())
                        else -> random.paragraph()
                    }
                }
                "title" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.title()
                        1 -> random.title(max = args[0].toInt())
                        2 -> random.title(args[0].toInt(), args[1].toInt())
                        else -> random.title()
                    }
                }
                
                // Chinese text
                "cword" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.cword()
                        1 -> random.cword(max = args[0].toInt())
                        2 -> random.cword(args[0].toInt(), args[1].toInt())
                        else -> random.cword()
                    }
                }
                "csentence" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.csentence()
                        1 -> random.csentence(max = args[0].toInt())
                        2 -> random.csentence(args[0].toInt(), args[1].toInt())
                        else -> random.csentence()
                    }
                }
                "cparagraph" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.cparagraph()
                        1 -> random.cparagraph(max = args[0].toInt())
                        2 -> random.cparagraph(args[0].toInt(), args[1].toInt())
                        else -> random.cparagraph()
                    }
                }
                "ctitle" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.ctitle()
                        1 -> random.ctitle(max = args[0].toInt())
                        2 -> random.ctitle(args[0].toInt(), args[1].toInt())
                        else -> random.ctitle()
                    }
                }
                
                // Names
                "first" -> random.first()
                "last" -> random.last()
                "name" -> random.name()
                "cfirst" -> random.cfirst()
                "clast" -> random.clast()
                "cname" -> random.cname()
                
                // Web
                "url" -> random.url()
                "domain" -> random.domain()
                "email" -> random.email()
                "ip" -> random.ip()
                "tld" -> random.tld()
                
                // Color
                "color" -> random.color()
                
                // Image
                "image" -> {
                    val args = parseParams(params)
                    when (args.size) {
                        0 -> random.image()
                        1 -> random.image(args[0])
                        2 -> random.image(args[0], args[1])
                        3 -> random.image(args[0], args[1], args[2])
                        4 -> random.image(args[0], args[1], args[2], args[3])
                        else -> random.image()
                    }
                }
                "dataimage" -> {
                    val args = parseParams(params)
                    if (args.isNotEmpty()) {
                        random.dataImage(args[0])
                    } else {
                        random.dataImage()
                    }
                }
                
                // Helper
                "capitalize" -> {
                    val args = parseParams(params)
                    if (args.isNotEmpty()) {
                        random.capitalize(args[0])
                    } else {
                        ""
                    }
                }
                "upper" -> {
                    val args = parseParams(params)
                    if (args.isNotEmpty()) {
                        random.upper(args[0])
                    } else {
                        ""
                    }
                }
                "lower" -> {
                    val args = parseParams(params)
                    if (args.isNotEmpty()) {
                        random.lower(args[0])
                    } else {
                        ""
                    }
                }
                
                // Miscellaneous
                "guid" -> random.guid()
                "id" -> random.id()
                
                else -> "@$methodName" // Return original if not found
            }
        } catch (e: Exception) {
            "@$methodName" // Return original if error
        }
    }
    
    private fun parseParams(params: String): List<String> {
        if (params.isBlank()) return emptyList()
        
        return params.split(",")
            .map { it.trim() }
            .map { if (it.startsWith("'") && it.endsWith("'")) it.substring(1, it.length - 1) else it }
            .map { if (it.startsWith("\"") && it.endsWith("\"")) it.substring(1, it.length - 1) else it }
    }
}