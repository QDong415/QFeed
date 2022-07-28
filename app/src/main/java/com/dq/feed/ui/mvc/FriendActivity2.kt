package com.dq.feed.ui.mvc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*

/**
 * 全部基于最传统的MVC，只涉及refresh这一个第3方
 */
class FriendActivity2 : AppCompatActivity(), INavBar, OnSpanTextClickListener,
    FriendRecyclerAdapter2.OnItemClickListener, TopicItemActionListener {

    private val title: String by lazy { intent.getStringExtra("title")!! }

    //View
    private lateinit var mAdapter: FriendRecyclerAdapter2

    //Data
    open val list: MutableList<TopicBean> = arrayListOf()
    //下次请求需要带上的页码参数
    private var page = 1

    private lateinit var recyclerView: RecyclerView
    
    //协程
    private val scope = MainScope()

    //为了预加载，里面存的是adapter的itemView，你可以不用
    private var cachedItemViewList: LinkedList<View>? = null
    //为了预加载，里面存的是adapter的9图的imageView，你可以不用
    private var cachedImageViewList: LinkedList<ImageView> = LinkedList<ImageView>()
    //为了预加载，里面存的是adapter的每个item的小评论textView，你可以不用
    private var cachedTextViewList: LinkedList<QMUILinkTextView> = LinkedList<QMUILinkTextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        initView()
        initListener()
    }

    private fun initView() {

        QMUILinkTextView.AUTO_LINK_MASK_REQUIRED = QMUILinkify.EMAIL_ADDRESSES xor QMUILinkify.WEB_URLS

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
        val itemDecoration = SpacesItemDecoration(this, SpacesItemDecoration.VERTICAL,0 ,0 )
            .setParam(android.R.color.transparent, 18, 0f, 0f)
        recyclerView.addItemDecoration(itemDecoration)

        //RecyclerView创建适配器，并且设置
        mAdapter = FriendRecyclerAdapter2(this, list, this)
        mAdapter.cachedImageViewList = cachedImageViewList
        mAdapter.cachedTextViewList = cachedTextViewList
        mAdapter.setOnItemClickListener(this)
        mAdapter.setOnTopicItemActionListener(this)
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
        val responseSuccess = responseEntry != null && responseEntry.isSuccess && responseEntry.data != null

        if (responseEntry == null) {
            //进入这里，说明是服务器崩溃，errorMessage是我们本地自定义的
            Toast.makeText(QApplication.instance, errorMessage , Toast.LENGTH_SHORT).show()
        } else if (!responseEntry.isSuccess) {
            //进入这里，说明是服务器验证参数错误，message是服务器返回的
            Toast.makeText(QApplication.instance, responseEntry.message , Toast.LENGTH_SHORT).show()
        }

        if (isRefresh) {

            //内部fun
            fun handleRefreshSuccess(beanList: MutableList<TopicBean>?){
                //收起下拉刷新中的UI状态
                refreshLayout.finishRefresh(0)
                //下拉刷新 -> 成功
                page = 2 //页面强制设置为下次请求第2页
                //处理列表数据
                list.clear()
                if (beanList != null){
                    list.addAll(beanList)
                }

                //检查显示\隐藏空布局。不同RV库对EmptyView有不同的实现，这是基于原生Adapter的方式。这一句需要写在notifyDataSetChanged前
                mAdapter.showEmptyViewEnable = true

                mAdapter.notifyDataSetChanged()
            }

            //如果是下拉刷新，无论是否成功，都要取消下拉刷新状态
            if (responseSuccess) {

                //下拉刷新 -> 成功
                if (cachedItemViewList == null && !responseEntry!!.data!!.items.isNullOrEmpty()){
                    //说明是第一次加载成功 && 有数据

                    val beanList: MutableList<TopicBean>? = responseEntry!!.data!!.items

                    cachedItemViewList = LinkedList<View>()

                    //在子线程中进行预加载
                    preloadCacheViewsInThread(beanList!!) {
                        //预加载完毕，回到主线程处理UI
                        runOnUiThread {
                            handleRefreshSuccess(beanList)
                        }
                    }

                } else {
                    //不是第一次加载成功
                    for (topicBean in responseEntry!!.data!!.items!!){
                        topicBean.findCitySubheadText()
                        topicBean.findFirstPicturePrimarySize()
                    }

                    handleRefreshSuccess(responseEntry!!.data!!.items)
                }
            } else {
                //下拉刷新 -> 失败。这里最好判断一下当前list是否为empty，empty的话显示errorView
                refreshLayout.finishRefresh(0)
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

                responseEntry.data?.items?.let {

                    for (topicBean in it){
                        topicBean.findCitySubheadText()
                        topicBean.findFirstPicturePrimarySize()
                    }

                    list.addAll(it)
                    mAdapter.notifyItemRangeInserted(list.size - it.size , it.size)
                }

            } else {
                //检查底部 -> 失败。设置为：还有更多数据
                refreshLayout.finishLoadMore()
            }
        }
    }

    private fun preloadCacheViewsInThread(topicBeanList: MutableList<TopicBean>, success: () -> Unit) {
        Thread {
            var pictureCountInFirstPage = 0 //第一页有多少个图片
            var commentCountInFirstPage = 0 //第一页有多少条小评论
            var nowTime = System.currentTimeMillis()
            for (topicBean in topicBeanList){
                topicBean.findCitySubheadText()
                topicBean.findFirstPicturePrimarySize()
                topicBean.findTotalSpanText(this@FriendActivity2, this@FriendActivity2)
                if (!topicBean.comments.isNullOrEmpty()){
                    for (commentBean in topicBean.comments){
                        commentBean.findCommentCompleteSpanText(this@FriendActivity2, this@FriendActivity2)
                    }
                    commentCountInFirstPage += topicBean.comments.count()
                }
                topicBean.pictures?.let { pictureCountInFirstPage += it.count() }
            }
            Log.e(
                "dq",
                "预处理Model耗时为：" + (System.currentTimeMillis() - nowTime) + "毫秒"
            ) //方法运行时间为：12毫秒

            val layoutInflater = LayoutInflater.from(this@FriendActivity2)

            //开始预加载每个Item的xml布局
            nowTime = System.currentTimeMillis()
            var i = 0
            while (i < 8 && i < topicBeanList.count()) {
                //这个10是预估的数字，也就是屏幕中的 + mCacheView（size == 2）+ pool里的（max是5，一般就是1）
                val itemView: View = layoutInflater.inflate(R.layout.listitem_topic, null)
                itemView.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                cachedItemViewList!!.add(itemView)
                i++
            }
            Log.e(
                "dq",
                "预加载耗时为：" + (System.currentTimeMillis() - nowTime) + "毫秒"
            ) //方法运行时间为：150毫秒


            //开始预加载每个Item中的图片的imageview
            i = 0
            while (i < 10 && i < pictureCountInFirstPage) {
                val pictureImageView = ImageView(this@FriendActivity2)
                pictureImageView.setBackgroundColor(resources.getColor(R.color.gray_color))
                pictureImageView.scaleType = ImageView.ScaleType.CENTER_CROP;
                cachedImageViewList.add(pictureImageView)
                i++
            }

            //开始预加载每个Item中的小评论的textview
            i = 0
            while (i < 7 && i < commentCountInFirstPage) {
                val emojiconTextView: QMUILinkTextView  =
                    layoutInflater.inflate(R.layout.listview_review_textview, null) as QMUILinkTextView
                cachedTextViewList.add(emojiconTextView)
                i++
            }

            Log.e(
                "dq",
                "预Image和Text耗时为：" + (System.currentTimeMillis() - nowTime) + "毫秒"
            ) //方法运行时间为：120毫秒

            success()

        }.start()
    }

    //FM.onPause -> AC.onPause -> FM.onStop -> AC.onStop -> FM.onDestroyView -> FM.onDestroy -> FM.onDetach -> AC.onDestroy
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel(null)
    }

    // Implements - INavBar
    override fun getTootBarTitle(): String {
        return "优化后的feed列表"
    }

    override fun onNavigationOnClick(view: View) {
        finish()
    }

    //点击 @张三
    override fun onUserSpanTextClick(userId: String?, name: String?, avatar: String?) {
        Toast.makeText(this, name , Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(position: Int) {
        Log.e("dq","onItemClick = "+position)
    }

    override fun onGridPictureClick(imageView: ImageView, gridLayoutIndex: Int, imageIndex: Int) {
        Log.e("dq","gridLayoutIndex = "+gridLayoutIndex +"   imageIndex "+imageIndex)
    }

    override fun onAvatarClick(view: View) {
        Log.e("dq","onAvatarClick = "+view.getTag())
    }

    override fun onLikeClick(view: View) {
        Log.e("dq","onLikeClick = "+view.getTag())
    }

    override fun onCommentClick(view: View) {
        Log.e("dq","onCommentClick = "+view.getTag())
    }

    override fun onShareClick(view: View) {
        Log.e("dq","onShareClick = "+view.getTag())
    }

    override fun onMoreClick(view: View) {
        Log.e("dq","onMoreClick = "+view.getTag())
    }
}