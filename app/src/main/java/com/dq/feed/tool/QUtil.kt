package com.dq.feed.tool

import java.text.SimpleDateFormat
import java.util.*

//如果请求服务器404了，那么给Activity回调的code和message就是这两个
const val NET_ERROR_CODE = -1
const val NET_ERROR = "Emm...服务器出问题了"

//服务端根路径
const val BASE_URL: String = "https://api.itopic.com.cn/api/"

//图片路径
const val QINIU_URL = "https://qiniu.itopic.com.cn/"


/**
 * @param filename 七牛的文件名
 * @param isThumbnail 是否返回缩略图
 * @return
 */
fun getQiniuUrlByFileName(filename: String?, isThumbnail: Boolean): String? {
    return getQiniuUrlWithParams(filename, if (isThumbnail) "?imageView2/1/w/240/h/240" else "")
}

/**
 * @param filename 七牛的文件名
 * @param paramsString 指定后缀 ,模糊效果："?imageMogr2/blur/16x10"
 * @return
 */
fun getQiniuUrlWithParams(filename: String?, paramsString: String?): String? {
    return if (filename == null || filename == "" || filename.startsWith("http")) {
        //为null 或者 “” 或者全路径。直接返回原始图片
        filename
    } else {
        val sb: StringBuilder = StringBuilder(QINIU_URL)
        sb.append(filename)
        if (paramsString != null) {
            sb.append(paramsString)
        }
        sb.toString()
    }
}


/**
 * 根据时间戳获取描述性时间，如3分钟前，1天前
 * @param timestamp
 * 时间戳 单位为毫秒
 * @return 时间字符串
 */
fun getTimeStringFromNow(timestamp: Long): String? {
    return getTimeStringFromNow(timestamp, false)
}

/**
 * 根据时间戳获取描述性时间，如3分钟前，1天前
 * @param timestamp
 * 时间戳 单位为毫秒
 * @return 3分钟前，1天前
 */
fun getTimeStringFromNow(timestamp: Long, isMillisecond: Boolean): String? {
    val timeGap: Long = if (isMillisecond) {
        val currentTime = System.currentTimeMillis()
        (currentTime - timestamp) / 1000 // 与现在时间相差秒数
    } else {
        System.currentTimeMillis() / 1000 - timestamp
    }
    var timeStr: String? = null
    if (timeGap > 86400) { // 1天以上
        timeStr = getSimpleDate(Date(if (isMillisecond) timestamp else timestamp * 1000))
    } else {
        val sdf = SimpleDateFormat("HH:mm")
        timeStr = sdf.format(Date(if (isMillisecond) timestamp else timestamp * 1000))
    }
    return timeStr
}

fun getSimpleDate(date: Date?): String? {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    return sdf.format(date)
}