package com.dq.feed.ui.base

import android.view.Gravity
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dq.feed.R
import com.gyf.immersionbar.ImmersionBar

interface INavBar {

    //传入xml里的ViewStub，返回Toolbar
    fun initToolbarView(toolbarViewStub: ViewStub) : Toolbar? {
        toolbarViewStub?.let {
            if (enableToolbar()) {
                it.layoutResource = getBindToolbarLayout()
                val mToolbar: Toolbar = it.inflate().findViewById(R.id.toolbar_root)

                //左边返回按钮
                val toolBarLeftIcon = getToolBarLeftIcon()
                if (toolBarLeftIcon == 0) {
                    mToolbar.navigationIcon = null
                } else {
                    mToolbar.setNavigationIcon(toolBarLeftIcon)
                }

                mToolbar.setNavigationOnClickListener { view ->
                    onNavigationOnClick(view)
                }

                mToolbar.title = getTootBarTitle()
//                setTitleCenter(mToolbar)

                return mToolbar
            }
        }
        return null
    }

    //设置状态栏
    fun initStatusBar(activity: FragmentActivity) {
        //设置共同沉浸式样式
        ImmersionBar.with(activity)
            .fitsSystemWindows(true)
            .statusBarColor(R.color.toolbar_color)
//            .statusBarDarkFont(true, 0.2f) //原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
            .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
            .navigationBarColor(R.color.white)
            .navigationBarDarkIcon(true).init()
    }

    fun initStatusBar(fragment: Fragment) {
        //设置共同沉浸式样式
        ImmersionBar.with(fragment)
            .fitsSystemWindows(true)
            .statusBarColor(R.color.toolbar_color)
            .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
            .navigationBarColor(R.color.white)
            .navigationBarDarkIcon(true).init()
    }


    fun getTootBarTitle(): String {
        return ""
    }

    /**
     * 设置返回按钮的图样，可以是Drawable ,也可以是ResId
     * 注：仅在 enableToolBarLeft 返回为 true 时候有效
     */
    fun getToolBarLeftIcon(): Int {
        return R.drawable.ic_white_black_24dp
    }

    fun enableToolbar(): Boolean {
        return true
    }

    //toolbar用什么布局
    fun getBindToolbarLayout(): Int {
        return R.layout.toolbar_common
    }

    fun onNavigationOnClick(view: View) {}

    //把状态栏的标题居中
    private fun setTitleCenter(mToolbar: Toolbar) {
        for (i in 0 until mToolbar.childCount) {
            val view: View = mToolbar.getChildAt(i)
            if (view is TextView) {
                view.gravity = Gravity.CENTER
                val params = Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,Toolbar.LayoutParams.MATCH_PARENT)
                params.gravity = Gravity.CENTER
                view.layoutParams = params
            }
        }
    }
}