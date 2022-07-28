package com.dq.feed.net

//为了统一封装 404 和 服务器返回code错误。统一用这个类来处理
class ResponseException(val errorMessage: String?, val errorCode: Int) : Throwable()
