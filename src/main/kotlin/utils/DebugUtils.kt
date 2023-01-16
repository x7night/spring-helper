package utils

import kotlin.reflect.full.memberProperties

object DebugUtils {
    fun printInfo(o: Any) {
        println("================== ProjectInfo ====================")
        o.javaClass.kotlin.memberProperties.forEach { println(it.name + ":"+ it.getValue(o, it))}
    }
}