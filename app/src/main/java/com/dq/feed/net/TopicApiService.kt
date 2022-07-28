package com.dq.feed.net

import com.dq.feed.bean.TopicBean
import retrofit2.http.*

interface TopicApiService {

    //获取动态列表
    @GET("topic/getlist")
    suspend fun getList(@QueryMap map: HashMap<String, String>) : ResponsePageEntity<TopicBean>

}