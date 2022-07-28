package com.dq.feed.net

open class BaseResponseEntity {

    //  判断标示
    var code = 0

    //    提示信息
    var message: String? = null

    val isSuccess: Boolean
        get() = code == 1
}