/*
 * Copyright 2019 Lukoh Nam, goForer
 *    
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License along with this program.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.goforer.grabph.presentation.ui.home.quest.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.annotation.MockData
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.view.decoration.RemoverItemDecoration
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.quest.adapter.HomeFavoriteQuestAdapter
import com.goforer.grabph.presentation.ui.home.quest.adapter.HomeTopPortionQuestAdapter
import com.goforer.grabph.presentation.ui.home.SnapItem
import com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.qeust.TopPortionQuestDataSource
import com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.quests.FavoriteQuestDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.TopPortionQuest
import com.goforer.grabph.data.datasource.network.response.Status
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_quest.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.full.findAnnotation
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.NOTO_SANS_KR_MEDIUM
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.NOTO_SANS_KR_REGULAR
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.common.menu.MenuHandler
import com.goforer.grabph.presentation.vm.quest.QuestViewModel
import com.goforer.grabph.presentation.vm.quest.QuestViewModel.Companion.FAVORITE_QUEST_TYPE
import com.goforer.grabph.presentation.vm.quest.QuestViewModel.Companion.HOT_QUEST_TYPE
import com.goforer.grabph.data.repository.paging.datasource.FavoriteQuestSortDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.FAVORITE_MISSION_SORT_DURATION
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.FAVORITE_MISSION_SORT_LATEST
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.FAVORITE_MISSION_SORT_PRIZE_MONEY
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_FAVORITE_QUESTS
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_QUEST_TOP_PORTION
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@RunWithMockData(true)
class HomeQuestFragment: BaseFragment() {
    private var visible = true

    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private lateinit var acAdapterTopPortionQuest: AutoClearedValue<HomeTopPortionQuestAdapter>

    private lateinit var favoriteQuests: PagedList<Quest>

    private lateinit var acvAdapterFavorite: AutoClearedValue<HomeFavoriteQuestAdapter>

    internal val homeActivity: HomeActivity by lazy {
        activity as HomeActivity
    }

    @field:Inject
    lateinit var questViewModel: QuestViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_home_quest, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this@HomeQuestFragment.swipe_quest_layout?.post {
            this@HomeQuestFragment.swipe_quest_layout?.isRefreshing = true
        }

        this@HomeQuestFragment.recycler_top_quest_view.setHasFixedSize(true)
        this@HomeQuestFragment.recycler_top_quest_view.setItemViewCacheSize(6)
        this@HomeQuestFragment.recycler_top_quest_view.isVerticalScrollBarEnabled = false
        this@HomeQuestFragment.recycler_top_quest_view.layoutManager = LinearLayoutManager(this.context.applicationContext, RecyclerView.VERTICAL, false)
        this@HomeQuestFragment.swipe_quest_layout?.setColorSchemeResources(R.color.redLight)
        this@HomeQuestFragment.swipe_quest_layout?.setOnRefreshListener {
            this@HomeQuestFragment.swipe_quest_layout?.isRefreshing = false
            // Implement update data below...
        }

        this@HomeQuestFragment.recycler_top_quest_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> { }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        homeActivity.closeFab()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {}
                    else -> { }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val vnView = homeActivity.bottom_navigation_view

                vnView.translationY = max(0f, min(vnView.height.toFloat(), vnView.translationY + dy))
                homeActivity.closeFab()
            }
        })

        this@HomeQuestFragment.recycler_bottom_quest_view.setHasFixedSize(true)
        this@HomeQuestFragment.recycler_bottom_quest_view.setItemViewCacheSize(10)
        this@HomeQuestFragment.recycler_bottom_quest_view.isVerticalScrollBarEnabled = false
        this@HomeQuestFragment.recycler_bottom_quest_view.layoutManager = LinearLayoutManager(this.context.applicationContext, RecyclerView.VERTICAL, false)
        this@HomeQuestFragment.recycler_bottom_quest_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> { }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        homeActivity.closeFab()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {}
                    else -> { }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val metrics = DisplayMetrics()
                val vnView = homeActivity.bottom_navigation_view

                vnView.translationY = max(0f, min(vnView.height.toFloat(), vnView.translationY + dy))
                if (dy > 0 && visible) {
                    vnView.y > metrics.heightPixels
                    if (!visible) {
                        vnView.y = metrics.heightPixels.toFloat()
                    }
                } else {
                    visible = vnView.y > metrics.heightPixels
                }

                homeActivity.closeFab()
            }
        })

        this@HomeQuestFragment.home_quest_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            homeActivity.closeFab()
        })

        this@HomeQuestFragment.recycler_bottom_quest_view.addItemDecoration(RemoverItemDecoration(Color.TRANSPARENT))
        getMission()
        this@HomeQuestFragment.sort_holder.setOnClickListener {
            homeActivity.closeFab()
            doSort(this@HomeQuestFragment.iv_down_arrow)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onDestroyView() {
        super.onDestroyView()

        EventBus.getDefault().unregister(this)
    }

    private fun createTopPortionQuestAdapter() {
        val topPortionMissionAdapter = HomeTopPortionQuestAdapter(homeActivity)
        acAdapterTopPortionQuest = AutoClearedValue(this, topPortionMissionAdapter)

        this@HomeQuestFragment.recycler_top_quest_view.adapter = acAdapterTopPortionQuest.get()

    }

    private fun createFavoriteQuestAdapter() {
        val adapter = HomeFavoriteQuestAdapter(this)

        acvAdapterFavorite = AutoClearedValue(this, adapter)
        this@HomeQuestFragment.recycler_bottom_quest_view.adapter = acvAdapterFavorite.get()
    }

    private fun submitQuests(adapter: HomeFavoriteQuestAdapter, quests: PagedList<Quest>, sortType: Int) {
        val favoriteQuests = sortBy(quests, sortType)

        adapter.submitList(favoriteQuests)
    }

    private fun setSnapItemCount(count: Int) {
        acAdapterTopPortionQuest.get()?.itemCount = count
    }

    private fun setSnapItem(adapter: HomeTopPortionQuestAdapter, topPortionQuest: TopPortionQuest, count: Int) {
        var title = ""
        var items: List<Any> = ArrayList()

        setSnapItemCount(count)
        for (i in 0 until count) {
            when(i) {
                HomeTopPortionQuestAdapter.ADAPTER_HOT_MISSION_INDEX -> {
                    title = topPortionQuest.hotQuest.title
                    items = topPortionQuest.hotQuest.quests
                }

                HomeTopPortionQuestAdapter.ADAPTER_FAVORITE_KEYWORD_INDEX -> {
                    title = topPortionQuest.favoriteKeyword.title
                    items = topPortionQuest.favoriteKeyword.keywords
                }
            }

            adapter.addSnapItem(SnapItem(Gravity.START, title, false, items))
        }
    }

    private fun getMission() {
        when(mock) {
            @MockData
            true -> transactMockData()
            false -> transactRealData()
        }
    }

    @MockData
    private fun transactMockData() {
        transactTopMockData()
        transactBottomMockData()
    }

    @MockData
    private fun transactTopMockData() {
        val topPortionQuest = TopPortionQuestDataSource()

        questViewModel.loadHotQuest().observe(this, Observer {
            it?.let { topPortionMission ->
                createTopPortionQuestAdapter()
                acAdapterTopPortionQuest.get()?.let { it1 -> setSnapItem(it1, topPortionMission, topPortionMission.itemcount) }
                withDelay(800L) {
                    this@HomeQuestFragment.swipe_quest_layout?.let {
                        this@HomeQuestFragment.swipe_quest_layout.post {
                            this@HomeQuestFragment.swipe_quest_layout.isRefreshing = false
                        }

                        setSortView()
                        this@HomeQuestFragment.home_quest_item_constraintLayoutContainer.visibility = View.VISIBLE
                    }
                }
            }
        })

        topPortionQuest.setTopPortionQuest()
        questViewModel.setTopPortionQuest(topPortionQuest.getTopPortionQuest()!!)
    }

    @MockData
    private fun transactBottomMockData() {
        val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .setPrefetchDistance(5)
                .build()

        LivePagedListBuilder(object : androidx.paging.DataSource.Factory<Int, Quest>() {
            override fun create(): androidx.paging.DataSource<Int, Quest> {
                return FavoriteQuestDataSource()
            }
        }, config).build().observe(this, Observer {
            favoriteQuests = it
            createFavoriteQuestAdapter()
            acvAdapterFavorite.get()?.let { it1 -> submitQuests(it1, it, FAVORITE_MISSION_SORT_LATEST) }
        })
    }

    private fun transactRealData() {
        transactTopRealData()
        transactBottomRealData()
    }

    private fun transactTopRealData() {
        val liveData = questViewModel.quest

        questViewModel.setParameters(
            Parameters(
                "",
                -1,
                LOAD_QUEST_TOP_PORTION,
                BOUND_FROM_LOCAL
            ), HOT_QUEST_TYPE)
        liveData.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { topPortionQuest ->
                        val quest = topPortionQuest as TopPortionQuest

                        createTopPortionQuestAdapter()
                        setSnapItemCount(quest.itemcount)
                        acAdapterTopPortionQuest.get()?.let { setSnapItem(it, quest, quest.itemcount) }
                        this@HomeQuestFragment.swipe_quest_layout?.let {
                            this@HomeQuestFragment.swipe_quest_layout.isRefreshing = false
                            setSortView()
                            this@HomeQuestFragment.home_quest_item_constraintLayoutContainer.visibility = View.VISIBLE
                        }
                    }

                    resource.getMessage()?.let {
                        homeActivity.showNetworkError(resource)
                        liveData.removeObservers(this)
                    }

                    this@HomeQuestFragment.swipe_quest_layout?.let {
                        this@HomeQuestFragment.swipe_quest_layout.isRefreshing = false
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    homeActivity.showNetworkError(resource)
                    liveData.removeObservers(this)
                }

                else -> {
                    homeActivity.showNetworkError(resource)
                    liveData.removeObservers(this)
                }
            }
        })
    }

    private fun transactBottomRealData() {
        val liveData = questViewModel.quest

        questViewModel.setParameters(
            Parameters(
                "",
                -1,
                LOAD_FAVORITE_QUESTS,
                BOUND_FROM_LOCAL
            ), FAVORITE_QUEST_TYPE)
        liveData.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData() ?: return@Observer
                    @Suppress("UNCHECKED_CAST")
                    val quests = resource.getData() as? PagedList<Quest>?

                    quests?.let {
                        when {
                            it.size > 0 -> {
                                favoriteQuests = it
                                createFavoriteQuestAdapter()
                                acvAdapterFavorite.get()?.let { it1 -> submitQuests(it1, it, FAVORITE_MISSION_SORT_LATEST) }
                            }

                            resource.getMessage() != null -> {
                                homeActivity.showNetworkError(resource)
                                liveData.removeObservers(this)
                            }
                            else -> {
                                homeActivity.showNetworkError(resource)
                                liveData.removeObservers(this)
                            }
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    homeActivity.showNetworkError(resource)
                    liveData.removeObservers(this)
                }

                else -> {
                    homeActivity.showNetworkError(resource)
                    liveData.removeObservers(this)
                }
            }
        })
    }

    private fun sortBy(quests: PagedList<Quest>, type: Int): PagedList<Quest> {
        val list: List<Quest>

        when(type) {
            FAVORITE_MISSION_SORT_LATEST -> {
                list = quests.sortedWith(compareByDescending<Quest> { it.idx }.thenByDescending { it.idx })
            }

            FAVORITE_MISSION_SORT_DURATION -> {
                list = quests.sortedWith(compareBy<Quest> { it.duration }.thenBy { it.duration })
            }

            FAVORITE_MISSION_SORT_PRIZE_MONEY -> {
                list = quests.sortedWith(compareByDescending<Quest> { it.rewards }.thenByDescending { it.rewards })
            }

            else -> {
                list = quests.sortedWith(compareByDescending<Quest> { it.idx }.thenByDescending { it.idx })
            }
        }

        val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(1)
                .setPageSize(1)
                .build()

        return PagedList.Builder(FavoriteQuestSortDataSource(list), config)
                        .setInitialKey(0)
                        .setFetchExecutor(Executors.newSingleThreadExecutor())
                        .setNotifyExecutor(Executors.newSingleThreadExecutor())
                        .build()
    }

    private fun doSort(view: View) {
        val wrapper = ContextThemeWrapper(homeActivity, R.style.PopupMenu)
        val popup = PopupMenu(wrapper, view, Gravity.CENTER)

        popup.menuInflater.inflate(R.menu.menu_favorite_quest_sort, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_latest -> {
                    if (this@HomeQuestFragment.tv_sort_text.text != getString(R.string.home_quest_sort_latest_keyword)) {
                        setSortKeyword(getString(R.string.home_quest_sort_latest_keyword))
                        acvAdapterFavorite.get()?.let {
                            submitQuests(it, favoriteQuests, FAVORITE_MISSION_SORT_LATEST)
                        }

                    }
                }

                R.id.menu_deadline -> {
                    if (this@HomeQuestFragment.tv_sort_text.text != getString(R.string.home_quest_sort_deadline_keyword)) {
                        setSortKeyword(getString(R.string.home_quest_sort_deadline_keyword))
                        acvAdapterFavorite.get()?.let {
                            submitQuests(it, favoriteQuests, FAVORITE_MISSION_SORT_DURATION)
                        }
                    }
                }

                R.id.menu_prize_money -> {
                    if (this@HomeQuestFragment.tv_sort_text.text != getString(R.string.home_quest_sort_prize_money_keyword)) {
                        setSortKeyword(getString(R.string.home_quest_sort_prize_money_keyword))
                        acvAdapterFavorite.get()?.let {
                            submitQuests(it, favoriteQuests, FAVORITE_MISSION_SORT_PRIZE_MONEY)
                        }
                    }
                }

                else -> {
                }
            }

            true
        }

        MenuHandler().applyFontToMenuItem(popup, Typeface.createFromAsset(homeActivity.applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                            resources.getColor(R.color.colorHomeQuestFavoriteKeyword, homeActivity.theme))

        val menuHelper: Any
        val argTypes: Array<Class<*>>

        try {
            val fMenuHelper= PopupMenu::class.java.getDeclaredField("mPopup")
            fMenuHelper.isAccessible = true
            menuHelper=fMenuHelper.get(popup)
            argTypes=arrayOf(Boolean::class.javaPrimitiveType!!)
            menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes).invoke(menuHelper, true)
        } catch (e: Exception) {
            // Possible exceptions are NoSuchMethodError and NoSuchFieldError
            //
            // In either case, an exception indicates something is wrong with the reflection code, or the
            // structure of the PopupMenu class or its dependencies has changed.
            //
            // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
            // but in the case that they do, we simply can't force icons to display, so log the error and
            // show the menu normally.
        }

        popup.show()
    }

    private fun setSortKeyword(keyword: String) {
        this@HomeQuestFragment.tv_sort_text.text = keyword
    }

    private fun setFontTypeface(view: AppCompatTextView) {
        val krRegularTypeface = Typeface.createFromAsset(homeActivity.applicationContext?.assets, NOTO_SANS_KR_REGULAR)

        view.typeface = krRegularTypeface
    }

    private fun setSortView() {
        this@HomeQuestFragment.tv_sort_text.text = getString(R.string.home_quest_sort_latest_keyword)
        setFontTypeface(this@HomeQuestFragment.tv_sort_text)
    }
}