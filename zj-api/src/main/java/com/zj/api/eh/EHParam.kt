package com.zj.api.eh


@Suppress("unused")
class EHParam {

    private val params: MutableMap<String, EHData<Any>> = mutableMapOf()

    internal fun addData(key: String, value: Any?) {
        if (value != null) {
            params[key] = EHData(value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> take(key: String): T? {
        return params.remove(key) as? T?
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> takeFist(): T? {
        return params.values.firstOrNull() as? T?
    }

    data class EHData<T : Any>(val data: T)

    override fun toString(): String {
        return params.toString()
    }
}