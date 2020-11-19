package com.zj.api.interceptor

interface HeaderProvider {

    fun headers(): Map<out String, String>

}