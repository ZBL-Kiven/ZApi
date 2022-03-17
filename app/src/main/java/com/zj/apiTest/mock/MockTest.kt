package com.zj.apiTest.mock

import com.zj.api.mock.MockAble

class MockTest : MockAble {

    override fun getMockData(): Any {
        return "this is mock data!"
    }
}