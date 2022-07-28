package com.dq.feed.net

import android.util.Log
import com.dq.feed.BuildConfig
import com.dq.feed.tool.BASE_URL
import com.ihsanbal.logging.LoggingInterceptor
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitInstance {

    private constructor() {

        //RetrofitInstance创建单例, 生命周期比Application还长，即使内存重启也不会重新进入这里

        val builder = OkHttpClient.Builder().connectTimeout(DEFAULT_TIMEOUT.toLong(),TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT.toLong(),TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(8, 15, TimeUnit.SECONDS))

        if (BuildConfig.DEBUG) {
            //打印网络请求日志
            builder.addInterceptor(
                LoggingInterceptor.Builder()
                    .setLevel(com.ihsanbal.logging.Level.BASIC)
                    .log(Platform.INFO)
                    .request("Request")
                    .response("Response")
                    .build()
            )
        }

        Log.e("dq","RetrofitInstance constructor");

        okHttpClient = builder.build()

        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()

    }

    companion object {

        private val DEFAULT_TIMEOUT:Int = 5

        private var okHttpClient: OkHttpClient? = null
        private var retrofit: Retrofit? = null

        val instance: RetrofitInstance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitInstance()
        }
    }

    //这样调用，可以把retrofit设置为private
    fun <T> create(service: Class<T>): T {
        return retrofit!!.create(service)
    }
}