package io.github.spcookie

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KType

abstract class TypeRef<T> {

    // JVM Type
    val javaType: Type

    // Kotlin Type
    val type: KType

    init {
        val superClass = this::class.java.genericSuperclass
        require(superClass is ParameterizedType) {
            "TypeRef must be created with actual type parameter"
        }
        javaType = superClass.actualTypeArguments[0]

        // Kotlin 类型
        type = (this::class.supertypes.firstOrNull()?.arguments?.firstOrNull()?.type)
            ?: throw IllegalStateException("Cannot extract KType from TypeRef")
    }

    override fun toString(): String = javaType.typeName
}
