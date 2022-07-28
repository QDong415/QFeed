package com.dq.feed.net

/**
 * 回调信息统一封装类
 * 服务器返回的列表json的标准格式为：
 * "code":1
 * "message":成功
 * "data":{
 *  "total":1000
 *  "totalpage":50
 *  "currentpage":1
 *  "items": [{
 *      "name":"小涨"
 *      "age":20
 *      }
 *      {...}
 *  ]
 * }
 */
class ResponseEntity<T> : BaseResponseEntity() {

    //显示数据（用户需要关心的数据）
    var data: T? = null
        private set

    fun setData(data: T) {
        this.data = data
    }

}