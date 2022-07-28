package com.dq.feed.ui.mvc

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dq.feed.R
import com.dq.feed.bean.TopicBean
import com.dq.feed.tool.dp2px
import com.dq.feed.tool.getQiniuUrlByFileName
import com.dq.feed.view.LinearLayoutForListView
import com.dq.feed.view.PictureGridLayout
import com.dq.feed.emojitextview.OnSpanTextClickListener
import com.dq.feed.view.linktextview.QMUILinkTextView
import java.util.*

class FriendRecyclerAdapter2(val context: Context, val list: List<TopicBean>, private val onSpanTextClickListener: OnSpanTextClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    View.OnClickListener {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    //是否可以显示emptyView。因为刚进来第一次加载中的时候，虽然list是empty，但我不希望显示emptyView
    var showEmptyViewEnable = false

    //viewType分别为item以及空view
    private val VIEW_TYPE_ITEM: Int = 0
    private val VIEW_TYPE_EMPTY: Int = 1

    //item上的控件的点击事件
    private var onItemClickListener: OnItemClickListener? = null

    private var onTopicItemActionListener: TopicItemActionListener? = null

    //为了预加载，里面存的是adapter的itemView
    var cachedItemViewList: LinkedList<View>? = null
    //为了预加载，里面存的是adapter的9图的imageView
    lateinit var cachedImageViewList: LinkedList<ImageView>
    //为了预加载，里面存的是adapter的每个item的小评论textView
    lateinit var cachedTextViewList: LinkedList<QMUILinkTextView>
    
    //itemview数量
    override fun getItemCount(): Int {
        //这里也需要添加判断，如果list.size()为0的话，只引入一个布局，就是emptyView，此时 这个recyclerView的itemCount为1
        if (showEmptyViewEnable && list.isNullOrEmpty()) {
            return 1;
        }
        //如果不为0，按正常的流程跑
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        //在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
        return if (showEmptyViewEnable && list.isNullOrEmpty()) {
            VIEW_TYPE_EMPTY
        } else VIEW_TYPE_ITEM
        //如果有数据，则使用ITEM的布局
    }

    //创建ViewHolder并绑定上itemview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //在这里根据不同的viewType进行引入不同的布局
        if (viewType == VIEW_TYPE_EMPTY) {
            //空布局
            val emptyView: View = mInflater.inflate(R.layout.listview_empty, parent, false)
            return object : RecyclerView.ViewHolder(emptyView) {}
        } else {
            //正常item
            var view: View? = null
            cachedItemViewList?.let {
                view = it.poll()
                Log.e("dq", "onCreateViewHolder = 用上缓存")
            }
            if (view == null){
                view = mInflater.inflate(R.layout.listitem_topic, parent, false)
                Log.e("dq", "onCreateViewHolder = 没用上缓存")
            }
            val viewHolder = TopicViewHolder(view!!)

            if (this::cachedImageViewList.isInitialized) {
                //是用了预加载
                viewHolder.pictureGridLayout.cachedImageViewList = cachedImageViewList
                viewHolder.linearLayoutForListView.cachedTextViewList = cachedTextViewList
            }

            viewHolder.link_textview.setNeedForceEventToParent(true)

            viewHolder.pictureGridLayout.setOnPictureClickListener(onTopicItemActionListener)

            //居然被转为  viewHolder.itemView.setOnClickListener((OnClickListener)null.INSTANCE); 看一下日志能不能打印
            viewHolder.itemView.setOnClickListener {
                onItemClickListener?.onItemClick(it.tag as Int);
            }

            viewHolder.avatar_iv.setOnClickListener(this)
            viewHolder.like_tv.setOnClickListener(this)
            viewHolder.comment_tv.setOnClickListener(this)
            viewHolder.share_tv.setOnClickListener(this)
            viewHolder.more_iv.setOnClickListener(this)

            return viewHolder
        }
    }

    //ViewHolder的view控件设置数据
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TopicViewHolder) {
            val bean: TopicBean = list.get(position);
            holder.name_tv.text = bean.name

            Glide.with(holder.avatar_iv.context)//.context是MainActivity
                .load(getQiniuUrlByFileName(list.get(position).avatar, true))
                .placeholder(R.drawable.user_photo)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(dp2px(holder.avatar_iv.context ,4))))
                .into(holder.avatar_iv)

            if (TextUtils.isEmpty(bean.content)) {
                holder.link_textview.visibility = View.GONE;
            } else {
                holder.link_textview.visibility = View.VISIBLE;
                holder.link_textview.text = bean.findTotalSpanText(context, onSpanTextClickListener);
            }

            holder.linearLayoutForListView.setComments(context, bean.comments, onSpanTextClickListener)
            holder.name_tv.text = bean.name
            holder.subhead_tv.text = bean.findCitySubheadText()
            holder.linearLayoutForListView.visibility =
                if (bean.comments == null || bean.comments.isEmpty()) View.GONE else View.VISIBLE
            holder.pictureGridLayout.setPictures(context, bean.pictures, bean)
            holder.pictureGridLayout.setOnPictureClickListener(onTopicItemActionListener)
            holder.comment_tv.text = bean.commentcount.toString()
            holder.like_tv.text = bean.likecount.toString()

            holder.avatar_iv.tag = position
            holder.itemView.tag = position
            holder.pictureGridLayout.tag = position
            holder.like_tv.tag = position
            holder.comment_tv.tag = position
            holder.share_tv.tag = position
            holder.more_iv.tag = position
        }
    }

    //kotlin 内部类默认是static ,前面加上inner为非静态
    //自定义的RecyclerView.ViewHolder，构造函数需要传入View参数。相当于java的构造函数第一句的super(view);
    class TopicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name_tv: TextView = view.findViewById(R.id.name_tv)
        val avatar_iv: ImageView = view.findViewById(R.id.avatar_iv)
        val subhead_tv: TextView = view.findViewById(R.id.subhead_tv)
        val link_textview: QMUILinkTextView = view.findViewById(R.id.link_textview)
        val pictureGridLayout: PictureGridLayout = view.findViewById(R.id.picture_gridlayout)
        val linearLayoutForListView: LinearLayoutForListView = view.findViewById(R.id.linearLayoutForListView)
        val share_tv: TextView = view.findViewById(R.id.share_tv)
        val like_tv: TextView = view.findViewById(R.id.like_tv)
        val comment_tv: TextView = view.findViewById(R.id.comment_tv)
        val more_iv: View = view.findViewById(R.id.more_iv)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is TopicViewHolder) {
            for (view in holder.pictureGridLayout.children) {
                if (view is ImageView){
                    //必须要移除，不然会：You must call removeView()
                    cachedImageViewList.add(view);
                    view.setImageDrawable(null)
                    Log.e("dq","缓冲回收图片 " + cachedImageViewList.size);
                }
            }
            holder.pictureGridLayout.removeAllViews()

            for (view in holder.linearLayoutForListView.children) {
                if (view is QMUILinkTextView){
                    //必须要移除，不然会：The specified child already has a parent. You must call removeView()
                    cachedTextViewList.add(view);
                    Log.e("dq","缓冲回收评论评论 " + cachedTextViewList.size);
                }
            }
            //必须要移除，不然会： You must call removeView()
            holder.linearLayoutForListView.removeAllViews()

        }
    }

    fun setOnItemClickListener(onItemClickListener: FriendActivity2) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnTopicItemActionListener(onTopicItemActionListener: TopicItemActionListener) {
        this.onTopicItemActionListener = onTopicItemActionListener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.avatar_iv -> {
                onTopicItemActionListener?.onAvatarClick(v)
            }
            R.id.like_tv -> {
                onTopicItemActionListener?.onLikeClick(v)
            }
            R.id.comment_tv -> {
                onTopicItemActionListener?.onCommentClick(v)
            }
            R.id.share_tv -> {
                onTopicItemActionListener?.onShareClick(v)
            }
            R.id.more_iv -> {
                onTopicItemActionListener?.onMoreClick(v)
            }
        }
    }


}