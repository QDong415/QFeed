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

class FriendRecyclerAdapter(val context: Context, val list: List<TopicBean>, private val onSpanTextClickListener: OnSpanTextClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    //是否可以显示emptyView。因为刚进来第一次加载中的时候，虽然list是empty，但我不希望显示emptyView
    var showEmptyViewEnable = false

    //viewType分别为item以及空view
    private val VIEW_TYPE_ITEM: Int = 0
    private val VIEW_TYPE_EMPTY: Int = 1

    //item上的控件的点击事件
    private var onItemClickListener: OnItemClickListener? = null

    private var onTopicItemActionListener: TopicItemActionListener? = null

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
            Log.e("dq", "onCreateViewHolder")
            var view: View = mInflater.inflate(R.layout.listitem_topic, parent, false)
            val viewHolder = TopicViewHolder(view!!)

            viewHolder.link_textview.setNeedForceEventToParent(true)

//            viewHolder.itemView.setOnClickListener(object : View.OnClickListener{
//                override fun onClick(v: View?) {
//                    Log.e("dq","普通 setOnClickListener "+ v.hashCode());
////                    onItemClickListener?.onItemClick(viewHolder.itemView.tag as Int);
//                }
//            })

            viewHolder.pictureGridLayout.setOnPictureClickListener(onTopicItemActionListener)

            //居然被转为  viewHolder.itemView.setOnClickListener((OnClickListener)null.INSTANCE); 看一下日志能不能打印
            viewHolder.itemView.setOnClickListener {
                onItemClickListener?.onItemClick(it.tag as Int);
            }

            viewHolder.share_tv.setOnClickListener {
                onTopicItemActionListener?.onShareClick(it);
            }

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
            holder.avatar_iv.tag = position
            holder.itemView.tag = position
            holder.pictureGridLayout.tag = position
//            holder.like_tv.setTag(position)
//            holder.comment_tv.setTag(position)
            holder.share_tv.setTag(position)
//            holder.more_iv.setTag(position)
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
//        init {
//            name_tv.setTextColor(view.resources.getColor(R.color.sky_color))
//        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnTopicItemActionListener(onTopicItemActionListener: TopicItemActionListener) {
        this.onTopicItemActionListener = onTopicItemActionListener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

}