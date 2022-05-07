package com.zj.apiTest.mock

import com.zj.api.eh.EHParam
import com.zj.api.mock.MockAble

class MockTest : MockAble<String> {

    override fun getMockData(ehParam: EHParam?): String {
        return "this is mock data! ${ehParam.toString()}"
    }
}