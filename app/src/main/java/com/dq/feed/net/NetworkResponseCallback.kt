package com.dq.feed.net

//这是下面两个的二合一
interface NetworkResponseCallback<R> {

    /**
     * 网络+解析json之后的回调( 成功 or 失败都可以用这个)
     * @param responseEntry是 最大的那层\我们自己定义的\json解析出来的Model。如果网络请求失败，那么是null
     * @param errorMessage是网络请求失败的时候的错误信息
     */
    fun onResponse(responseEntry: R?, errorMessage: String?)
}

interface NetworkFailCallback {

    /**
     * 网络请求<失败>回调，原因可能是404，也可能是服务器返回code错误
     */
    fun onResponseFail(code: Int, errorMessage: String?)
}

interface NetworkSuccessCallback<R> {

    /**
     * 网络请求<成功>回调，这个成功是网络请求必定有返回值，但是code是否为success不一定
     * R 可能是具体的Model，也可以是ResponseEntity<Model>, 看你具体的写法。
     */
    fun onResponseSuccess(responseEntry: R?)
}