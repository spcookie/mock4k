package io.github.spcookie

import org.json.JSONArray
import org.json.JSONObject

object Mson {

    /** JSON → Kotlin Object（Map/List/Primitive） */
    fun parse(json: String): Any? {
        return when {
            json.trim().startsWith("{") -> toMap(JSONObject(json))
            json.trim().startsWith("[") -> toList(JSONArray(json))
            else -> null
        }
    }

    /** Kotlin Object（Map/List）→ JSON */
    fun stringify(value: Any?): String {
        return when (value) {
            is Map<*, *> -> JSONObject(value).toString()
            is List<*> -> JSONArray(value).toString()
            else -> JSONObject.wrap(value).toString()
        }
    }

    private fun toMap(obj: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        for (key in obj.keySet()) {
            val value = obj.get(key)
            map[key] = when (value) {
                is JSONObject -> toMap(value)
                is JSONArray -> toList(value)
                JSONObject.NULL -> null
                else -> value
            }
        }
        return map
    }

    private fun toList(array: JSONArray): List<Any?> {
        val list = mutableListOf<Any?>()
        for (i in 0 until array.length()) {
            val value = array.get(i)
            list += when (value) {
                is JSONObject -> toMap(value)
                is JSONArray -> toList(value)
                JSONObject.NULL -> null
                else -> value
            }
        }
        return list
    }
}
