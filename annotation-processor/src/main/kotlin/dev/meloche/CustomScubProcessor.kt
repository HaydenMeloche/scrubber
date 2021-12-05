package dev.meloche

import kotlin.reflect.KClass

interface CustomScubProcessor {

    fun <T : Any> getValue(value: T, type: KClass<T>): T?
}