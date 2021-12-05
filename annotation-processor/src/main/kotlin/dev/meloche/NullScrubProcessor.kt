package dev.meloche

import kotlin.reflect.KClass

class NullScrubProcessor : CustomScubProcessor {
    override fun <T : Any> getValue(value: T, type: KClass<T>): T? {
        return null
    }
}