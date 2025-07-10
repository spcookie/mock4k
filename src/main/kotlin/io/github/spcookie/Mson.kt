package io.github.spcookie

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Mson {

    private val gson = Gson()

    fun toMap(json: String): Map<String, Any> {
        return Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
    }

}