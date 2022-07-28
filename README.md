# 安装体验

https://upload-images.jianshu.io/upload_images/26002059-1539f775a7a59eb7.png

## 包含功能：
- 9张图。如果只有一张图，那么单张图的宽高根据图片原始宽高等比例缩放
- 只有一张图的时候，这个图可能是视频，图中间有播放按钮
- 内容支持表情。[微笑]要显示为😊
- 内容有@人功能，@人有点击事件
- 每个Item带有评论，XXX回复XXX

## 传统做法的效果：
- 首次进入Activity后触发下拉刷新，请求成功后setAdpater，这时候帧率会掉到49帧左右。丢失11帧
- 手指往下滚动，滚动过程中，帧率在57帧 - 60帧徘徊
- 退出Activity再次进入，由于java底层的代码优化，执行效率会上升。首次setAdpater帧率为53左右
![349be342f7149268de53b2f9c1eed7a.jpg](https://upload-images.jianshu.io/upload_images/26002059-e02535c5f1985679.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/400)


## 我优化后的效果：
- 首次进入Activity后触发下拉刷新，请求成功后setAdpater，这时候帧率会掉到57帧左右。丢失3帧左右
- 手指往下滚动，滚动过程中，帧率全称60帧
![84330a3939703026fca5cf11cae2a3f.jpg](https://upload-images.jianshu.io/upload_images/26002059-2d66b56ae270cc20.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/400)

![ezgif-4-e790b5f7b4.gif](https://upload-images.jianshu.io/upload_images/26002059-98570aebb282f2fd.gif?imageMogr2/auto-orient/strip)


## 我的基础优化方案（别的帖子也会讲的）：
- ✅ 1、每个item中众多元素的点击事件不要每次都new。应该是onCreateViewHolder中imageView.setOnClickListener(this)。然后在onBindViewHolder中imageView.setTag。避免滚动过程中频繁new ClickListener()
- ✅ 2、SpanText做好缓存，避免每次滚动都解析。总之，检查Adpater中所有new Object()的代码，能不new就不new
- ✅ 3、手写DrawableCenterTextView，解决系统的Button的DrawableLeft会贴边问题。否则要多一层LinearLayout包裹
- ✅ 4、需要重复计算的size要设置为全局变量static，避免每次计算。比如每张图片的宽高，表情图片的15sp大小
- ✅ 5、底部加载更多：使用notifyItemRangeInserted代替notifyDataSetChanged。后者会触发2、3次onCreateViewHolder

## 我的进阶优化方案（你在别的帖子看不到的）：
- ✅ 1、glide首次加载图片会创建线程池，耗时约50ms，可以移到App打开时的欢迎界面就创建好。节省50ms
- ✅ 2、首次setAdpater前先不着急结束下拉刷新状态。先开启Thread，在Thread中解析文字的表情和@人解析，组成SpanText并缓存到model中，节省约12ms
- ✅ 3、采用LruCache缓存最新的32个表情的drawable，这样可以加快常见表情的解析速度
- ✅ 4、在Thread中按List<Model>.count() 解析item的xml布局，并存放在LinkedList<View> 中（为了节省内存，我最多限制8条）。在onCreateViewHolder中进行 .poll，节省150ms左右
- ✅ 5、在Thread中按List<Model>.count() 预创建图片和评论的缓存池：LinkedList<ImageView> 和 LinkedList<评论TextView>。在item显示的时候。从缓冲池中取，而不是new。节省100ms左右
- ✅ 6、在Adpater的 onViewRecycled 中把图片和评论remove后存入缓存池。这一步主要为了滚动流畅
