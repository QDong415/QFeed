package com.dq.feed.tool

import com.dq.feed.net.BaseResponseEntity
import com.dq.feed.net.NetworkFailCallback
import com.dq.feed.net.ResponseException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

//通用的接口调用
fun requestCommon(scope: CoroutineScope, requestBlock: suspend () -> Unit, failCallback: NetworkFailCallback) {

    val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext: CoroutineContext, throwable: Throwable ->
        //访问网络异常的回调用, 这种方法可以省去try catch, 但不适用于async启动的协程
        //这里是主线程；
        if (throwable is ResponseException) {
            //进入这里，说明是服务器返回code错误
            val commonException: ResponseException = throwable

            failCallback?.onResponseFail(commonException.errorCode, commonException.errorMessage)
        } else {
            //进入这里，说明是服务器404
            failCallback?.onResponseFail(NET_ERROR_CODE, NET_ERROR)
        }
    }

    /*MainScope()是一个绑定到当前viewModel的作用域  当ViewModel被清除时会自动取消该作用域，所以不用担心内存泄漏为问题*/
    scope.launch(coroutineExceptionHandler) {

        requestBlock()
        //如果网络访问异常，代码会直接进入CoroutineExceptionHandler，不会走这里了
        //这里是主线程
    }
}

//检查服务器返回的数据的code是否是success，如果是失败，throw异常到CoroutineExceptionHandler里
//这个方法你可用也可以不用
fun checkResponseCodeAndThrow(responseEntity : BaseResponseEntity){
    if (responseEntity == null){
        throw Throwable()
    }
    if (!responseEntity.isSuccess){
        throw ResponseException(responseEntity.message, responseEntity.code)
    }
}