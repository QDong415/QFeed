package com.dq.feed.ui.mvc

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dq.feed.R
import com.dq.feed.bean.CommentBean
import com.dq.feed.bean.TopicBean
import com.dq.feed.emojitextview.OnSpanTextClickListener
import com.dq.feed.net.NetworkResponseCallback
import com.dq.feed.net.ResponsePageEntity
import com.dq.feed.net.RetrofitInstance
import com.dq.feed.net.TopicApiService
import com.dq.feed.tool.NET_ERROR
import com.dq.feed.tool.QApplication
import com.dq.feed.ui.base.INavBar
import com.dq.feed.view.SpacesItemDecoration
import com.dq.feed.view.linktextview.QMUILinkTextView
import com.dq.feed.view.linktextview.QMUILinkify
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.*

class FriendActivity : AppCompatActivity(), INavBar, OnSpanTextClickListener {

    private val title: String by lazy { intent.getStringExtra("title")!! }

    //View
    private lateinit var mAdapter: FriendRecyclerAdapter

    //Data
    open val list: MutableList<TopicBean> = arrayListOf()
    //下次请求需要带上的页码参数
    private var page = 1

    private lateinit var recyclerView: RecyclerView
    
    //协程
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        initView()
        initListener()
    }

    private fun initView() {

        //只解析web网址，不解析手机号和email ：QMUILinkify.EMAIL_ADDRESSES xor QMUILinkify.WEB_URLS
        QMUILinkTextView.AUTO_LINK_MASK_REQUIRED = QMUILinkify.WEB_URLS

        TopicBean.emotionSize = getResources().getDimension(R.dimen.topic_emoji_size)
        CommentBean.emotionSize = getResources().getDimension(R.dimen.topic_emoji_size)

        initStatusBar(this)
        initToolbarView(findViewById(R.id.view_stub_toolbar))

        //设置RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager

        // 设置颜色、高度、间距等
        val itemDecoration = SpacesItemDecoration(this, SpacesItemDecoration.VERTICAL)
            .setParam(android.R.color.transparent, 18, 0f, 0f)
        recyclerView.addItemDecoration(itemDecoration)

        //RecyclerView创建适配器，并且设置
        mAdapter = FriendRecyclerAdapter(this, list, this)
        recyclerView.adapter = mAdapter
    }

    private fun initListener() {
        //下拉刷新底部加载控件
        val refreshLayout: SmartRefreshLayout = findViewById(R.id.refresh_layout);

        refreshLayout.setOnRefreshListener {
            //触发了下拉刷新
            val params = HashMap<String, String>()
            params["page"] = "1"

            requestFriendList(params, object :
                NetworkResponseCallback<ResponsePageEntity<TopicBean>> {
                //网络请求返回
                override fun onResponse(responseEntry: ResponsePageEntity<TopicBean>?, errorMessage: String?) {
                    //处理
                    handleListResponse(it, true , responseEntry, errorMessage)
                }
            })
        }

        refreshLayout.setOnLoadMoreListener {
            //触发了底部加载更多
            val params = HashMap<String, String>()
            params["page"] = page.toString()

            requestFriendList(params, object :
                NetworkResponseCallback<ResponsePageEntity<TopicBean>> {
                //网络请求返回
                override fun onResponse(responseEntry: ResponsePageEntity<TopicBean>?, errorMessage: String?) {
                    handleListResponse(it, false , responseEntry, errorMessage)
                }
            })
        }

        //立即开始刷新
        refreshLayout.autoRefresh(350,200,1f,false);//延迟350毫秒后自动刷新
    }

    //请求列表，这个方法进行了初步的解耦
    private fun requestFriendList(params : HashMap<String,String>, responseCallback: NetworkResponseCallback<ResponsePageEntity<TopicBean>>){

        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            //访问网络异常的回调用, 这种方法可以省去try catch, 但不适用于async启动的协程
            //这里是主线程；
            Log.e("dq",throwable.toString())
            responseCallback?.onResponse(null, NET_ERROR)
        }

        /*MainScope()是一个绑定到当前viewModel的作用域  当ViewModel被清除时会自动取消该作用域，所以不用担心内存泄漏为问题*/
        scope.launch(coroutineExceptionHandler) {

            var apiService : TopicApiService = RetrofitInstance.instance.create(TopicApiService::class.java)
            //suspend 是这一步
            val response: ResponsePageEntity<TopicBean> = apiService.getList(params)
            //如果网络访问异常，代码会直接进入CoroutineExceptionHandler，不会走这里了
            //这里是主线程
            responseCallback?.onResponse(response, null)
        }
    }

    private fun handleListResponse(refreshLayout: RefreshLayout, isRefresh: Boolean = false, responseEntry: ResponsePageEntity<TopicBean>?, errorMessage: String?){

        //请求成功
        val responseSuccess = responseEntry != null && responseEntry.isSuccess

        if (responseEntry == null) {
            //进入这里，说明是服务器崩溃，errorMessage是我们本地自定义的
            Toast.makeText(QApplication.instance, errorMessage , Toast.LENGTH_SHORT).show()
        } else if (!responseEntry.isSuccess) {
            //进入这里，说明是服务器验证参数错误，message是服务器返回的
            Toast.makeText(QApplication.instance, responseEntry.message , Toast.LENGTH_SHORT).show()
        }

        if (isRefresh) {
            //如果是下拉刷新，无论是否成功，都要取消下拉刷新状态
            refreshLayout.finishRefresh(0)
            if (responseSuccess) {
                //下拉刷新 -> 成功
                page = 2 //页面强制设置为下次请求第2页
                responseEntry!!.data?.let {
                    //处理列表数据
                    handleTopicList(it.items!!)
                    list.clear()
                    list.addAll(it.items!!)

                    //检查显示\隐藏空布局。不同RV库对EmptyView有不同的实现，这是基于原生Adapter的方式。这一句需要写在notifyDataSetChanged前
                    mAdapter.showEmptyViewEnable = true

                    mAdapter.notifyDataSetChanged()
                }
            } else {
                //下拉刷新 -> 失败。这里最好判断一下当前list是否为empty，empty的话显示errorView
            }
        } else {
            //底部加载更多
            if (responseSuccess) {
                //检查底部 -> 成功。
                if (responseEntry!!.data!!.hasMore()) {
                    //还有更多数据
                    refreshLayout.finishLoadMore()
                } else {
                    //没有更多数据
                    refreshLayout.finishLoadMoreWithNoMoreData()
                }

                page++

                responseEntry.data?.let {

                    handleTopicList(it.items!!)

                    list.addAll(it.items!!)
                    //这里也可以改成：notifyItemRangeInserted
                    mAdapter.notifyDataSetChanged()
                }

            } else {
                //检查底部 -> 失败。设置为：还有更多数据
                refreshLayout.finishLoadMore()
            }
        }
    }

    private fun handleTopicList(topicBeanList: List<TopicBean>){
        for (topicBean in topicBeanList){
            topicBean.findCitySubheadText()
            topicBean.findFirstPicturePrimarySize()
        }
    }
    
    //FM.onPause -> AC.onPause -> FM.onStop -> AC.onStop -> FM.onDestroyView -> FM.onDestroy -> FM.onDetach -> AC.onDestroy
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel(null)
    }

    // Implements - INavBar
    override fun getTootBarTitle(): String {
        return "没优化的feed列表"
    }

    override fun onNavigationOnClick(view: View) {
        finish()
    }

    //点击 @张三
    override fun onUserSpanTextClick(userId: String?, name: String?, avatar: String?) {
        Toast.makeText(this, name , Toast.LENGTH_SHORT).show()
    }
}