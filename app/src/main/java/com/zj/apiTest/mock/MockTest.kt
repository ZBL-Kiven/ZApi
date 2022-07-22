package com.zj.apiTest.mock

import com.zj.api.eh.EHParam
import com.zj.api.mock.MockAble

class MockTest : MockAble<List<String>> {

    override fun getMockData(ehParam: EHParam?): List<String> {
        return mutableListOf<String>().apply {
            add("this is mock data! ${ehParam.toString()}")
            add("this is mock data! ${ehParam.toString()}")
        }
    }
}