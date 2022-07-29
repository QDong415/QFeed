# 安装体验

![1659104674017.jpg](https://upload-images.jianshu.io/upload_images/26002059-1539f775a7a59eb7.png)


![ezgif-1-4516d51ebf.gif](https://upload-images.jianshu.io/upload_images/26002059-891cd1211e3feeb9.gif?imageMogr2/auto-orient/strip)

事先说明：我在demo中一进入Activity就立刻触发下拉刷新，所以你看到帧率可能掉到了40，是因为系统的startActivity本身就掉帧非常厉害。想真实测出帧率，需要进入Activity后等帧率稳定在60了，再手动下拉刷新

## 包含功能：
- 9张图。如果只有一张图，那么单张图的宽高根据图片原始宽高等比例缩放
- 只有一张图的时候，这个图可能是视频，图中间有播放按钮
- 内容支持表情。[微笑]要显示为图片😊
- 内容有@人功能，@人有点击事件
- 每个Item带有评论，XXX回复XXX：你好[微笑]

## 传统做法的效果：
- 首次进入Activity后触发下拉刷新，请求成功后setAdpater，这时候帧率会掉到`49帧`左右。丢失11帧
- 手指往下滚动，滚动过程中，帧率在`57帧 - 60帧`徘徊
- 退出Activity再次进入，由于java底层的代码优化，执行效率会上升。首次setAdpater帧率为`53帧`左右
<br/>
![165910467401.jpg](https://upload-images.jianshu.io/upload_images/26002059-60631e2c5368e1e9.png)

## 我优化后的效果：
- 首次进入Activity后触发下拉刷新，请求成功后setAdpater，这时候帧率会掉到`57帧`左右。丢失3帧左右
- 手指往下滚动，滚动过程中，全程60帧
<br/>
![165910474648.jpg](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/df6c5668068d4ef6a29ebc90e18f4c58~tplv-k3u1fbpfcp-watermark.image)


## 我的基础优化方案（别人帖子也会讲的）：
- ✅ 1、每个item中众多元素的点击事件不要每次都new。应该是`onCreateViewHolder`中imageView.setOnClickListener(this)。然后在`onBindViewHolder`中imageView.setTag。避免滚动过程中频繁new ClickListener()
- ✅ 2、SpanText做好缓存，避免每次滚动都解析。总之，检查Adpater中所有new Object()的代码，能不new就不new
- ✅ 3、手写`DrawableCenterTextView`，解决系统的Button的DrawableLeft会贴边问题。否则要多一层LinearLayout包裹
- ✅ 4、需要重复计算的size要设置为全局变量static，避免每次计算。比如每张图片的宽高，表情图片的15sp大小
- ✅ 5、底部加载更多：使用notifyItemRangeInserted代替notifyDataSetChanged。后者会触发2、3次onCreateViewHolder

## 我的进阶优化方案（你在别的帖子看不到的）：
- ✅ 1、glide首次加载图片会创建线程池，耗时约50ms，可以移到App打开时的欢迎界面就创建好。节省50ms
- ✅ 2、首次setAdpater前先不着急结束下拉刷新状态。先开启Thread，在Thread中解析文字的表情和@人解析，组成SpanText并缓存到model中，节省约12ms
- ✅ 3、采用`LruCache`缓存最新的32个表情的drawable，这样可以加快常见表情的解析速度
- ✅ 4、在Thread中按List<Model>.count() 解析item的xml布局，并存放在LinkedList<View> 中（为了节省内存，我最多限制8条）。在onCreateViewHolder中进行 .poll，节省150ms左右
- ✅ 5、在Thread中按List<Model>.count() 预创建图片和评论的`缓存池`：LinkedList<ImageView> 和 LinkedList<评论TextView>。在item显示的时候。从缓冲池中取，而不是new。节省100ms左右
- ✅ 6、在Adpater的 `onViewRecycled` 中把图片和评论remove后存入缓存池。这一步主要为了滚动流畅
- ✅ 7、九张图采用`GridLayout`而不是 UnScrollView或者GridManager。后者会太重量级且会带来更多的内存消耗。这里其实最好自己写一个ViewGroup

## 本方案用到的基础常识：
- 1、ListView或RecyclerView如果当前是不可见状态，你去setAdpater不会起到任何效果，代码不会走onCreateViewHolder等回调。当你设置为VISIBLE的时候，才会触发Adapter里的回调。
- 2、RecyclerView的第3级缓存`ViewCacheExtension`，用起来还要去反射ViewHolder的Layout.Params，挺不方便的，所以我没用他。
- 3、`RecyclerViewPool` 缓存池每种type的最大值默认为5。如果item是固定高度，那么缓存池的size总是在0和1之间徘徊，因为第一个item刚被回收，底部item就进来了。
- 4、所以`RecyclerViewPool` 缓存池只有在非常极限情况下才会size == 5 （即：顶部的几个item的height非常小，但是底部的item高度非常大）。
- 5、`onViewRecycled(holder: RecyclerView.ViewHolder)` 是Item进入RecyclerViewPool的回调，进入之后，vh的data就等于无用了
- 6、`LinkedList` 要比`ArrayList`在增删的时候更快，尤其是增删第0个和最后一个object。且由于ArrayList底层用的是int[10]， 所以存在内存浪费。所以用LinkedList做缓存池优于ArrayList

## 本方案部分技术细节：
```
Activity：

      //在子线程中进行预加载
                    preloadCacheViewsInThread(beanList) {
                        //预加载完毕，回到主线程处理UI
                        runOnUiThread {
                            handleRefreshSuccess(beanList)
                        }
                    }

private fun preloadCacheViewsInThread(topicBeanList: MutableList<TopicBean>, success: () -> Unit) {
        Thread {
            var pictureCountInFirstPage = 0 //第一页有多少个图片
            var commentCountInFirstPage = 0 //第一页有多少条小评论
            var nowTime = System.currentTimeMillis()
            for (topicBean in topicBeanList){
                topicBean.findTotalSpanText(this@FriendActivity2, this@FriendActivity2)
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
                //这个8是预估的数字，也就是屏幕中的 + mCacheView（size == 2）+ pool里的（max是5，一般就是1）
                val itemView: View = layoutInflater.inflate(R.layout.listitem_topic, null)
               //缓存起来，放到onCreateHolder中使用
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
                cachedImageViewList.add(pictureImageView)
                i++
            }
            Log.e(
                "dq",
                "预Image和Text耗时为：" + (System.currentTimeMillis() - nowTime) + "毫秒"
            ) //方法运行时间为：120毫秒

            success()

        }.start()
    }

```

```
Adpater:

//创建ViewHolder并绑定上itemview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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

            return viewHolder
    }


    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is TopicViewHolder) {
            for (view in holder.pictureGridLayout.children) {
                if (view is ImageView){
                    //必须要移除，不然会：You must call removeView()
                    cachedImageViewList.add(view);
                    view.setImageDrawable(null)
                    Log.e("dq","缓冲池回收图片 " + cachedImageViewList.size);
                }
            }
            holder.pictureGridLayout.removeAllViews()

            for (view in holder.linearLayoutForListView.children) {
                if (view is QMUILinkTextView){
                    //必须要移除，不然会：The specified child already has a parent. You must call removeView()
                    cachedTextViewList.add(view);
                    Log.e("dq","缓冲池回收评论 " + cachedTextViewList.size);
                }
            }
            //必须要移除，不然会： You must call removeView()
            holder.linearLayoutForListView.removeAllViews()
        }
    }

```


## Author：DQ  285275534@qq.com

我的其他开源库，给个Star鼓励我写更多好库：

[Android 朋友圈列表Feed流的最优化方案，让你的RecyclerView从49帧 -> 57帧](https://github.com/QDong415/QFeed)

[Android 仿大众点评、仿小红书 下拉拖拽关闭Activity](https://github.com/QDong415/QDragClose)

[Android 仿快手直播间手画礼物，手绘礼物](https://github.com/QDong415/QDrawGift)

[Android 直播间聊天消息列表RecyclerView。一秒内收到几百条消息依然不卡顿](https://github.com/QDong415/QLiveMessageHelper)

[Android 仿快手直播界面加载中，顶部的滚动条状LoadingView](https://github.com/QDong415/QStripeView)

[Android Kotlin MVVM框架，全世界最优化的分页加载接口、最接地气的封装](https://github.com/QDong415/QKotlin)

[Android 基于个推+华为push的一整套完善的android IM聊天系统](https://github.com/QDong415/iTopicChat)

[IOS1:1完美仿微信聊天表情键盘](https://github.com/QDong415/QKeyboardEmotionView)
