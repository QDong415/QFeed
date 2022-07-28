package com.dq.feed.tool

import android.app.Application
import com.didichuxing.doraemonkit.DoKit

class QApplication : Application(){

    companion object {
        open lateinit var instance: QApplication
    }

    //Tips: 将手机开发者选项里设置为后台进程为2，然后进入后台，点微信，再回来app，结果：
    // 这时候重新走了这里的Application onCreate，但是MainVC的onCreate方法中，是从tag启动FM（saveBundle!=null),且tabRootFM里的子FM也是如此
    override fun onCreate() {
        super.onCreate()
        instance = this

        DoKit.Builder(this).build()
    }
}