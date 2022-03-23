package com.zj.api.exception

import retrofit2.HttpException

@Suppress("unused")
class ApiException(val httpException: HttpException?, case: Throwable?) : Throwable(case)