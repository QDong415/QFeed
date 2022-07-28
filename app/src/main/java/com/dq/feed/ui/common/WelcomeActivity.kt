package com.dq.feed.ui.common

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dq.feed.RootActivity
import com.dq.feed.ui.base.INavBar

class WelcomeActivity : AppCompatActivity() ,INavBar {

    private val handler = Handler(Looper.getMainLooper())

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initStatusBar(this)

        //延时是为了让onCreate先走完，耗时代码放到Runnable里，这样可以防止耗时代码导致的白屏
        handler.postDelayed(initRun, 150)
    }

    private val initRun = Runnable {
        //这里可以写耗时操作，比如读取数据，初始化表情，读取缓存，设置全局变量等

        val start = System.currentTimeMillis()
        //Glide首次加载需要开启线程池，耗时50ms。我们放在这里就加载好。这样真实是用glide的时候就免去50ms
        Glide.with(this@WelcomeActivity)
        Log.e("dq", "Glide first load = " + (System.currentTimeMillis() - start))

        //1.3秒后跳转到主页 或者 登录页，看具体需求
        handler.postDelayed(launchHome, 1000)
    }

    private val launchHome = Runnable {
        val intent = Intent()
        intent.setClass(this@WelcomeActivity, RootActivity::class.java)
        startActivity(intent)
        finish()
    }
}