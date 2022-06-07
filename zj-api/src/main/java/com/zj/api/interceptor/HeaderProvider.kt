package com.zj.api.interceptor

interface HeaderProvider {

    companion object {

        fun createStatic(h: Map<String, String?>?): HeaderProvider {
            return object : HeaderProvider {
                override fun headers(): Map<String, String?>? {
                    return h
                }
            }
        }

        fun create(h: () -> Map<String, String?>?): HeaderProvider {
            return object : HeaderProvider {
                override fun headers(): Map<String, String?>? {
                    return h.invoke()
                }
            }
        }
    }

    fun headers(): Map<out String, String?>?
}

operator fun HeaderProvider.plus(map: Map<String, String?>): HeaderProvider {
    return HeaderProvider.create {
        this@plus.headers()?.plus(map)
    }
}

fun HeaderProvider.toMap(): MutableMap<String, String?> {
    val map = mutableMapOf<String, String?>()
    headers()?.let {
        map.putAll(it)
    }
    return map
}

operator fun HeaderProvider.plus(pair: Pair<String, String?>): HeaderProvider {
    return HeaderProvider.create {
        this@plus.headers()?.plus(pair)
    }
}