package luyao.wanandroid.ui.home

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.youth.banner.BannerConfig
import dp2px
import kotlinx.android.synthetic.main.fragment_home.*
import luyao.wanandroid.R
import luyao.wanandroid.adapter.HomeArticleAdapter
import luyao.wanandroid.base.BaseFragment
import luyao.wanandroid.bean.Article
import luyao.wanandroid.bean.ArticleList
import luyao.wanandroid.bean.Banner
import luyao.wanandroid.ui.BrowserActivity
import luyao.wanandroid.ui.login.LoginActivity
import luyao.wanandroid.util.GlideImageLoader
import luyao.wanandroid.util.Preference
import luyao.wanandroid.view.CustomLoadMoreView
import luyao.wanandroid.view.SpaceItemDecoration


/**
 * Created by luyao
 * on 2018/3/13 14:15
 */
class HomeFragment : BaseFragment(), HomeContract.View {

    override lateinit var mPresenter: HomeContract.Presenter

    private val isLogin by Preference(Preference.IS_LOGIN, false)
    private val homeArticleAdapter by lazy { HomeArticleAdapter() }
    private val bannerImages = mutableListOf<String>()
    private val bannerTitles = mutableListOf<String>()
    private val bannerUrls = mutableListOf<String>()
    private var currentPage = 0
    private val banner by lazy { com.youth.banner.Banner(activity) }

    override fun getLayoutResId() = R.layout.fragment_home

    override fun initView() {
        homeRecycleView.run {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(SpaceItemDecoration(homeRecycleView.dp2px(10f)))
        }

        initBanner()
        initAdapter()

        homeRefreshLayout.run {
            setOnRefreshListener { refresh() }
            isRefreshing = true
        }
        refresh()
    }

    private fun initAdapter() {
        homeArticleAdapter.run {
            setOnItemClickListener { _, _, position ->
                Intent(activity, BrowserActivity::class.java).run {
                    putExtra(BrowserActivity.URL, homeArticleAdapter.data[position].link)
                    startActivity(this)
                }

            }
            onItemChildClickListener = this@HomeFragment.onItemChildClickListener
            addHeaderView(banner)
            setLoadMoreView(CustomLoadMoreView())
            setOnLoadMoreListener({ loadMore() }, homeRecycleView)
        }
        homeRecycleView.adapter = homeArticleAdapter
    }

    private val onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
        when (view.id) {
            R.id.articleStar -> {
                if (isLogin) {
                    homeArticleAdapter.run {
                        data[position].run {
                            collect = !collect
                            if (collect) mPresenter.collectArticle(this)
                            else mPresenter.cancelCollectArticle(this)
                        }
                        notifyDataSetChanged()
                    }
                } else {
                    Intent(activity, LoginActivity::class.java).run { startActivity(this) }
                }
            }
        }
    }

    private fun loadMore() {
        mPresenter.getArticles(currentPage)
    }

    private fun initBanner() {

        banner.run {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, banner.dp2px(200f))
            setBannerStyle(BannerConfig.NUM_INDICATOR_TITLE)
            setImageLoader(GlideImageLoader())
            setOnBannerListener { position ->
                run {
                    val intent = Intent(activity, BrowserActivity::class.java)
                    intent.putExtra(BrowserActivity.URL, bannerUrls[position])
                    startActivity(intent)
                }
            }
        }
    }

    private fun refresh() {
        homeArticleAdapter.setEnableLoadMore(false)
        homeRefreshLayout.isRefreshing = true
        currentPage = 0
        mPresenter.getArticles(currentPage)
    }

    override fun initData() {
        mPresenter.getBanners()
    }

    override fun getArticles(articleList: ArticleList) {
        homeArticleAdapter.run {
            if (homeRefreshLayout.isRefreshing) replaceData(articleList.datas)
            else addData(articleList.datas)
            setEnableLoadMore(true)
            loadMoreComplete()
        }
        homeRefreshLayout.isRefreshing = false
        currentPage++
    }

    override fun getBanner(bannerList: List<Banner>) {
        for (banner in bannerList) {
            bannerImages.add(banner.imagePath)
            bannerTitles.add(banner.title)
            bannerUrls.add(banner.url)
        }
        banner.setImages(bannerImages)
                .setBannerTitles(bannerTitles)
                .setBannerStyle(BannerConfig.NUM_INDICATOR_TITLE)
                .setDelayTime(3000)
        banner.start()
    }

    override fun collectArticle(article: Article) {

    }

    override fun cancleCollectArticle(article: Article) {

    }

    override fun onStart() {
        super.onStart()
        banner.startAutoPlay()
    }

    override fun onStop() {
        super.onStop()
        banner.stopAutoPlay()
    }
}