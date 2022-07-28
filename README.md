# QFeed

Kotlin+MVVM框架，最符合实际接口情况、最接地气的封装

大家都已经看过很多MVVM的开发框架了，各式各样的都有，高star的几个项目我也基本都消化一遍，但是都感觉差了点什么。
要么封装的太过复杂，别人很难上手，实际也用不上那么复杂的封装；
要么就是为了封装而封装，实际情况很难变通；
要么就是光顾着搭建架子，实际的restful api接口根本对接不上

1、本框架主要技术关键词：
协程suspend、retrofit、smart下拉刷新、BaseRecyclerViewAdapterHelper、ViewBinding、ViewModel

2、本框架优点：
非常贴合实际项目需求，用的个别第3方的也都是最前沿的技术。不用sleep，wait模拟服务器接口，本框架直接拿实际网络接口演示

本框架针对下拉刷新、底部加载更多、判断是否有更多页、判断空布局、内存重启时候Fragment处理、、等等问题重点封装，其他无所谓的东西能不封装的就不封装，更方便接入你的项目

ViewModel里监听的接口返回情况，封装的明明白白：

//具体的网络接口返回情况
```kotlin
enum class LoadState {
    None,
    Loading, //下拉刷新开始请求接口 or 普通开始请求接口
    SuccessHasMore, //下拉刷新请求成功且服务器告诉我还有下一页 or 普通请求成功
    SuccessNoMore,  //下拉刷新请求成功且服务器告诉我已经没有下一页了
    CodeError, //下拉刷新请求成功但是服务器给我返回了错误的code码 or 普通请求成功但是服务器给我返回了错误的code码
    NetworkFail, //下拉刷新请求失败 or 普通请求失败，原因是压根就没访问到服务器
    PageLoading,  //底部翻页开始请求接口
    PageSuccessHasMore, //底部翻页请求成功且服务器告诉我还有下一页
    PageSuccessNoMore, //底部翻页请求成功且服务器告诉我已经没有下一页了
    PageCodeError, //底部翻页请求成功但是服务器给我返回了错误的code码
    PageNetworkFail, //底部翻页请求失败，原因是压根就没访问到服务器
}
```


服务器返回的接口往往是这样的：
```
 "code":1
  "message":成功
  "data":{
   "total":1000 //一共有多少条数据
   "totalpage":50 //一共多少页
   "currentpage":1 //当前请求的是第几页
   "items": [{ //具体的T对象
       "name":"小涨"
      "age":20
     }
      {...}
   ]
}
```
下面看一下代码：

基于RecyclerView的界面对应的 BaseRVPagerViewModel：
```kotlin

/**
 * 场景：如果你的列表界面用的是RecyclerView，那么Activity或Fragment里的 MyViewModel 继承这个VM，（T是列表的实体类）
 *
 * 特点：不监听list，只监听网络访问状态loadStatus，然后根据不同的loadStatus来直接用list；轻便简单容易理解
 * 为什么还有tempList：因为recyclerview有notifyItemRangeInserted,所以翻页的时候要用到这一页的templist，然后用templist做局部刷新
 */
open class BaseRVPagerViewModel<T>: ViewModel() {

    //内部使用可变的Mutable
    protected val _loadStatus = MutableLiveData<LoadState>()

    //对外开放的是final，这是谷歌官方的写法
    open val loadStatus: LiveData<LoadState> = _loadStatus

    //下拉刷新的错误信息，服务器给我返回的 也可以自定义
    var errorMessage:String? = null

    //最核心的数据列表，我的做法是：不监听他，直接get他
    //当然也有人的做法是 LiveData<MutableList<T>> 然后onChange里无脑notityDataChanged，个人觉得那样做反而限制很多
    //特别注明：如果使用的是BaseRecyclerViewAdapterHelper，他的adapter里有会有个list的指针，我们这里也有个指针，但是内存共用一个
    open val list: MutableList<T> = arrayListOf()

    //下拉刷新请求返回的临时templist：
    var tempRefreshlist: List<T>? = null

    //翻页请求返回的临时templist：
    //为什么分别定义两个temp：因为极端情况下，下拉刷新和底部翻页同时请求网络，只用一个temp的话就不知道应该setList还是addList
    //注意：这样做分成两个也不会造成占用内存增加，因为我addList(tempList)之后, 立即templist = null
    var tempPagelist: List<T>? = null

    //下次请求需要带上的页码参数
    private var page = 1

    /**
     * 功能：万能的列表请求接口
     * @params get请求参数，无需page字段
     * @loadmore true = 是底部翻页，false = 下拉刷新
     * @block 具体的那两行suspend协程请求网络的代码块，其返回值是网络接口返回值
     */
    open fun requestList(params : HashMap<String,String>, loadmore : Boolean , block:suspend() -> BasePageEntity<T>){


        _loadStatus.value = (if (loadmore) LoadState.PageLoading else LoadState.Loading)

        //如果是加载更多，就加上参数page；否则（下拉刷新）就强制设为1，如果服务器要求是0，就改成"0"
        params["page"] = if (loadmore) page.toString() else "1"

        //访问网络异常的回调用, 这种方法可以省去try catch, 但不适用于async启动的协程
        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            //这里是主线程；
            errorMessage = "Emm..服务器出小差了";
            _loadStatus.setValue(
                    if (loadmore) LoadState.PageNetworkFail else LoadState.NetworkFail
            )
        }

        /*viewModelScope是一个绑定到当前viewModel的作用域  当ViewModel被清除时会自动取消该作用域，所以不用担心内存泄漏为问题*/
        viewModelScope.launch(coroutineExceptionHandler) {

            //具体的那两行suspend协程请求网络的代码 由VM子类来实现
            val response: BasePageEntity<T> = block();
            //如果网络访问异常，代码会直接进入CoroutineExceptionHandler，不会走这里了

            if (loadmore) {
                //加载更多
                if (response.isSuccess) {//加载更多服务器返回成功
                    page++

                    //这次底部翻页接口返回的具体List<Bean>
                    tempPagelist = response.data?.items

                    //触发activity的onChanged，让activity处理界面
                    _loadStatus.setValue(
                        if (response.data!!.hasMore()) LoadState.PageSuccessHasMore else LoadState.PageSuccessNoMore
                    )

                    //代码走到这里，tempPagelist已经用完了（把他addAll了），就立即释放掉temp的内存
                    tempPagelist = null;

                } else {
                    _loadStatus.setValue(LoadState.PageCodeError)
                }
            } else { //下拉刷新请求完毕
                if (response.isSuccess) {
                    page = 2 //页面强制设置为下次请求第2页

                    //这次下拉刷新接口返回的具体List<Bean>
                    tempRefreshlist = response.data?.items

                    //触发activity的onChanged，让activity处理界面
                    _loadStatus.setValue(
                        if (response.data!!.hasMore()) LoadState.SuccessHasMore else LoadState.SuccessNoMore
                    )

                    //代码走到这里，界面已经用过了tempRefreshlist（把他addAll了），就立即释放掉temp的内存
                    tempRefreshlist = null;

                } else {
                    //服务器告诉我参数错误
                    _loadStatus.setValue(LoadState.CodeError)
                    errorMessage = response.message
                }
            }
        }
    }
}
```

以上代码是BaseRVPagerViewModel<T>，其中T是列表的每一行的具体实体类；下面代码是列表界面Activity需要继承自 BaseRVActivity：
```kotlin

/**
 * 场景：如果Activity里有RecyclerView，那么就继承BaseRVActivity，T是列表数据的每条的Bean，VM 是BaseRVPagerViewModel子类
 */
open abstract class BaseRVActivity<T ,VM : BaseRVPagerViewModel<T>> : BaseAppCompatActivity() {

    protected val viewModel: VM by lazy { ViewModelProvider(this).get(onBindViewModel()) }

    override fun initView() {
        super.initView()
        initRVObservable()
    }

    //子类自己写获取adapter的方法（比如new ） 然后通过这个方法返回就行了
    //out 就是java里的<? extends BaseViewHolder> 就是可以兼容BaseViewHolder的子类
    abstract fun adapter(): BaseQuickAdapter<T, out BaseViewHolder>

    //子类自己写获取refreshLayout的方法（比如findViewById或者binding.） 然后通过这个方法返回就行了
    abstract fun refreshLayout(): SmartRefreshLayout

    //子类重写
    abstract fun onBindViewModel(): Class<VM>

    protected open fun initRVObservable() {
        //监听网络返回值
        viewModel.loadStatus
                .observe(this, Observer<Any> { loadState ->
                    when (loadState) {
                        LoadState.None -> {
                        }
                        LoadState.Loading -> {
                        }
                        LoadState.SuccessNoMore, LoadState.SuccessHasMore -> {
                            refreshLayout().finishRefresh(0)

                            adapter().setList(viewModel.tempRefreshlist!!)

                            if (loadState === LoadState.SuccessHasMore)
                                refreshLayout().finishLoadMore()
                            else refreshLayout().finishLoadMoreWithNoMoreData()

                            if (viewModel.list.isNullOrEmpty()) {
                                emptyLayout.findViewById<TextView>(R.id.empty_tv).setText("空空如也~")
                                adapter().setEmptyView(emptyLayout)
                            }
                        }
                        LoadState.CodeError, LoadState.NetworkFail -> {
                            refreshLayout().finishRefresh(0)
                            refreshLayout().finishLoadMoreWithNoMoreData()

                            if (viewModel.list.isNullOrEmpty()) {
                                emptyLayout.findViewById<TextView>(R.id.empty_tv).setText(viewModel.errorMessage)
                                adapter().setEmptyView(emptyLayout)
                            }
                        }
                        LoadState.PageLoading -> {
                        }
                        LoadState.PageSuccessHasMore , LoadState.PageSuccessNoMore-> {
                            adapter().addData(viewModel.tempPagelist!!)

                            if (loadState === LoadState.PageSuccessHasMore)
                                refreshLayout().finishLoadMore()
                            else refreshLayout().finishLoadMoreWithNoMoreData()
                        }
                        LoadState.PageCodeError, LoadState.PageNetworkFail ->
                            refreshLayout().finishLoadMoreWithNoMoreData()
                    }
                })
    }

    //空布局
    private val emptyLayout: View by lazy {
        LayoutInflater.from(this).inflate(R.layout.listview_empty, null)
    }
}
```
以上是BaseRVActivity，下面就是具体的Activity的实现方式，我想了很久，到底Adapter实体类 和 ViewModel实体类 和 RefreshLayout实体类 到底是放到BaseRVActivity类里合适，还是放到具体的子类Activity里，最后决定是:
ViewModel实体类 放在Base里，因为毕竟是要封装框架，ViewModel是框架级的东西，Base里经常会用到他；
而RefreshLayout 和 Adapter 放到具体的子类Activity，因为他们往往会因为界面的个性化，做出具体的调整

以下是具体的子类 UserListActivity 实现方式

```kotlin

/**
 * RecyclerView的Demo，具体每一条的bean是UserBaseBean，VM是UserArrayViewModel
 */
class UserListActivity : BaseRVActivity<UserBaseBean, UserListActivity.UserArrayViewModel>() {

    private lateinit var adapter: UserQuickAdapter

    private lateinit var binding: ActivityRecycleviewBinding

    override fun initView() {
        super.initView()

        adapter = UserQuickAdapter(viewModel.list)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recyclerview)
        initView();

        binding.refreshLayout.setOnRefreshListener {
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            viewModel.requestUserList(params, false)
        }

        binding.refreshLayout.setOnLoadMoreListener {
            val params = HashMap<String, String>()
            params["keyword"] = "小"
            viewModel.requestUserList(params, true)
        }


        //demo 添加的 Header
        //Header 是自行添加进去的 View，所以 Adapter 不管理 Header 的 DataBinding。
        //请在外部自行完成数据的绑定
//        val view: View = layoutInflater.inflate(R.layout.listitem_follower, null, false)
//        view.findViewById(R.id.iv).setVisibility(View.GONE)
//        adapter.addHeaderView(view)

        binding.refreshLayout.autoRefresh(100,200,1f,false);//延迟100毫秒后自动刷新

        //item 点击事件
//        adapter.setOnItemClickListener(object : OnItemClickListener() {
//            fun onItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
//            }
//        })
    }

    override fun getTootBarTitle(): String {
        return "RecyclerView列表"
    }

    //本界面对应的VM类，如果VM复杂的话，也可以独立成一个外部文件
    class UserArrayViewModel: BaseRVPagerViewModel<UserBaseBean>() {

        //按MVVM设计原则，请求网络应该放到更下一层的"仓库类"里，但是我感觉如果你只做网络不做本地取数据，没必要
        //请求用户列表接口
        fun requestUserList(params : HashMap<String,String> , loadmore : Boolean){

            //调用"万能列表接口封装"
            super.requestList(params, loadmore){

                //用kotlin高阶函数，传入本Activity的"请求用户列表接口的代码块" 就是这3行代码
                var apiService : UserApiService = RetrofitInstance.instance.create(UserApiService::class.java)
                val response: BasePageEntity<UserBaseBean> = apiService.userList(params)
                response
            }
        }
    }

    override fun adapter(): UserQuickAdapter = adapter

    override fun refreshLayout(): SmartRefreshLayout = binding.refreshLayout

    override fun onBindViewModel(): Class<UserArrayViewModel> = UserArrayViewModel::class.java
}
```

此外，本框架还做了对网络请求的封装，这个并不是本框架最大亮点，就不再贴代码了