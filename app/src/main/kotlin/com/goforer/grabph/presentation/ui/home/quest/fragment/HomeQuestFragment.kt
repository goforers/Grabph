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
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.annotation.MockData
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.model.BaseModel
import com.goforer.base.presentation.utils.CommonUtils.getJson
import com.goforer.base.presentation.view.decoration.RemoverItemDecoration
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.quest.adapter.HomeFavoriteQuestAdapter
import com.goforer.grabph.presentation.ui.home.quest.adapter.HomeTopPortionQuestAdapter
import com.goforer.grabph.presentation.ui.home.SnapItem
import com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.qeust.TopPortionQuestDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.TopPortionQuest
import com.goforer.grabph.data.datasource.network.response.Status
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
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.FAVORITE_QUEST_SORT_ALL
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.FAVORITE_QUEST_SORT_CLOSED
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.FAVORITE_QUEST_SORT_EXAMINATION
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.FAVORITE_QUEST_SORT_ONGOING
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.KEYWORD_QUEST_ALL
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.SORT_QUEST_CLOSED
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.SORT_QUEST_ONGOING
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.SORT_QUEST_UNDER_EXAMINATION
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Questg
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_FAVORITE_QUESTS
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_QUEST_TOP_PORTION
import kotlinx.android.synthetic.main.activity_home.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@RunWithMockData(true)
class HomeQuestFragment: BaseFragment() {
    private var visible = true

    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private lateinit var acAdapterTopPortionQuest: AutoClearedValue<HomeTopPortionQuestAdapter>

    private lateinit var favoriteQuests: List<Quest> // original list initialized once only at the beginning.
    private lateinit var questsByKeyword: List<Quest> // changeable list initialized every time user selects a category.

    private lateinit var acvAdapterFavorite: AutoClearedValue<HomeFavoriteQuestAdapter>

    private var selectedSorType = 0

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

        removeCache()
        initRecyclerViewTop()
        initRecyclerViewBottom()
        initSwipeLayout()
        getQuestAfterClearCache()
        getQuestByKeyword()

        this@HomeQuestFragment.home_quest_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            homeActivity.closeFab()
        })

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

    private fun initRecyclerViewTop() {
        this@HomeQuestFragment.recycler_top_quest_view.setHasFixedSize(true)
        this@HomeQuestFragment.recycler_top_quest_view.setItemViewCacheSize(6)
        this@HomeQuestFragment.recycler_top_quest_view.isVerticalScrollBarEnabled = false
        this@HomeQuestFragment.recycler_top_quest_view.layoutManager = LinearLayoutManager(this.context.applicationContext, RecyclerView.VERTICAL, false)

        this@HomeQuestFragment.recycler_top_quest_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> { }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        homeActivity.closeFab()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> { }
                    else -> { }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // val vnView = homeActivity.bottom_navigation_view
                val vnView = homeActivity.layout_bottom_navigation

                vnView.translationY = max(0f, min(vnView.height.toFloat(), vnView.translationY + dy))
                // this@HomeQuestFragment.recycler_top_quest_view.invalidateItemDecorations()

                homeActivity.closeFab()
            }
        })
    }

    private fun initRecyclerViewBottom() {
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
                // val vnView = homeActivity.bottom_navigation_view
                val vnView = homeActivity.layout_bottom_navigation

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

        this@HomeQuestFragment.recycler_bottom_quest_view.addItemDecoration(RemoverItemDecoration(Color.TRANSPARENT))
    }

    private fun initSwipeLayout() {
        this@HomeQuestFragment.swipe_quest_layout?.post {
            this@HomeQuestFragment.swipe_quest_layout?.isRefreshing = true
        }

        this@HomeQuestFragment.swipe_quest_layout?.setColorSchemeResources(R.color.redLight)
        this@HomeQuestFragment.swipe_quest_layout?.setOnRefreshListener {
            this@HomeQuestFragment.swipe_quest_layout?.isRefreshing = false
            // Implement update data below...
        }
    }

    private fun showEmptyQuestMessage(isEmpty: Boolean) {
        this.tv_empty_quest.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun createTopPortionQuestAdapter() {
        val topPortionMissionAdapter = HomeTopPortionQuestAdapter(homeActivity, questViewModel)
        acAdapterTopPortionQuest = AutoClearedValue(this, topPortionMissionAdapter)

        this@HomeQuestFragment.recycler_top_quest_view.adapter = acAdapterTopPortionQuest.get()
    }

    private fun createFavoriteQuestAdapter() {
        val adapter = HomeFavoriteQuestAdapter(this)

        acvAdapterFavorite = AutoClearedValue(this, adapter)
        this@HomeQuestFragment.recycler_bottom_quest_view.adapter = acvAdapterFavorite.get()
    }

    private fun submitQuests(sortType: Int) {
        val favoriteQuests = sortBy(sortType)
        createFavoriteQuestAdapter()
        acvAdapterFavorite.get()?.submitList(favoriteQuests)
        showEmptyQuestMessage(favoriteQuests.isEmpty())
        showNumberOfQuest(favoriteQuests)
    }

    private fun showNumberOfQuest(list: List<Quest>) {
        this.tv_number_of_quest.text = "${list.size}개의 퀘스트"
    }

    private fun setSnapItemCount(count: Int) {
        acAdapterTopPortionQuest.get()?.itemCount = count
    }

    private fun setSnapItem(adapter: HomeTopPortionQuestAdapter, topPortionQuest: TopPortionQuest, count: Int) {
        var title = ""
        var items: List<Any> = ArrayList()

        setSnapItemCount(count)
        for (i in 0 until count) {
            when (i) {
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

    private fun getQuestByKeyword() {
        questViewModel.selectedKeyword.observe(homeActivity, Observer { keyword ->
            questsByKeyword = when (keyword) {
                KEYWORD_QUEST_ALL -> favoriteQuests
                else -> favoriteQuests.filter { it.favoriteCategory == keyword }
            }

            submitQuests(selectedSorType)
        })
    }

    private fun getQuestAfterClearCache() {
        questViewModel.isCleared.observe(homeActivity, Observer { isCleared ->
            if (isCleared) {
                when(mock) {
                    @MockData
                    true -> transactMockData()
                    false -> transactRealData()
                }
            }
        })
    }

    @MockData
    private fun transactMockData() {
        transactTopMockData()
        transactBottomMockData()
    }

    @MockData
    private fun transactTopMockData() {
        val topPortionQuest = TopPortionQuestDataSource()

        questViewModel.loadHotQuest().observe(homeActivity, Observer {
            it?.let { topPortionMission ->
                createTopPortionQuestAdapter()
                acAdapterTopPortionQuest.get()?.let { adapter -> setSnapItem(adapter, topPortionMission, topPortionMission.itemcount) }

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
        questViewModel.loadBottomQuest().observe(homeActivity, Observer {
            favoriteQuests = it
            questsByKeyword = it
            submitQuests(FAVORITE_QUEST_SORT_ALL)
        })

        val json = getJson("mock/favorite_quest.json")
        val list = BaseModel.gson().fromJson(json, Questg::class.java)
        questViewModel.setBottomPortionQuest(list.quest?.toMutableList()!!)
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

        liveData.observe(homeActivity, Observer { resource ->
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
                        liveData.removeObservers(homeActivity)
                    }

                    this@HomeQuestFragment.swipe_quest_layout?.let {
                        this@HomeQuestFragment.swipe_quest_layout.isRefreshing = false
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    homeActivity.showNetworkError(resource)
                    liveData.removeObservers(homeActivity)
                }

                else -> {
                    homeActivity.showNetworkError(resource)
                    liveData.removeObservers(homeActivity)
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
        liveData.observe(homeActivity, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData() ?: return@Observer
                    @Suppress("UNCHECKED_CAST")
                    val quests = resource.getData() as? PagedList<Quest>?

                    quests?.let {
                        when {
                            it.size > 0 -> {
                                favoriteQuests = it
                                questsByKeyword = it
                                submitQuests(FAVORITE_QUEST_SORT_ALL)
                            }

                            resource.getMessage() != null -> {
                                homeActivity.showNetworkError(resource)
                                liveData.removeObservers(homeActivity)
                            }
                            else -> {
                                homeActivity.showNetworkError(resource)
                                liveData.removeObservers(homeActivity)
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
                    liveData.removeObservers(homeActivity)
                }
            }
        })
    }

    private fun sortBy(type: Int): PagedList<Quest> {
        val list: List<Quest>

        when (type) {
            FAVORITE_QUEST_SORT_ALL -> {
                list = questsByKeyword
            }

            FAVORITE_QUEST_SORT_ONGOING -> {
                list = questsByKeyword.filter { it.state == SORT_QUEST_ONGOING }
            }

            FAVORITE_QUEST_SORT_EXAMINATION -> {
                list = questsByKeyword.filter { it.state == SORT_QUEST_UNDER_EXAMINATION }
            }

            FAVORITE_QUEST_SORT_CLOSED -> {
                list = questsByKeyword.filter { it.state == SORT_QUEST_CLOSED }
            }

            else -> {
                list = questsByKeyword
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
                R.id.menu_all -> {
                    selectedSorType = FAVORITE_QUEST_SORT_ALL
                    setSortKeyword(getString(R.string.home_quest_sort_all))
                    submitQuests(FAVORITE_QUEST_SORT_ALL)
                }

                R.id.menu_ongoing -> {
                    if (this.tv_sort_text.text != getString(R.string.home_quest_sort_on_going)) {
                        selectedSorType = FAVORITE_QUEST_SORT_ONGOING
                        setSortKeyword(getString(R.string.home_quest_sort_on_going))
                        submitQuests(FAVORITE_QUEST_SORT_ONGOING)
                    }
                }

                R.id.menu_examination -> {
                    if (this.tv_sort_text.text != getString(R.string.home_quest_sort_examination)) {
                        selectedSorType = FAVORITE_QUEST_SORT_EXAMINATION
                        setSortKeyword(getString(R.string.home_quest_sort_examination))
                        submitQuests(FAVORITE_QUEST_SORT_EXAMINATION)
                    }
                }

                R.id.menu_closed -> {
                    if (this.tv_sort_text.text != getString(R.string.home_quest_sort_closed)) {
                        selectedSorType = FAVORITE_QUEST_SORT_CLOSED
                        setSortKeyword(getString(R.string.home_quest_sort_closed))
                        submitQuests(FAVORITE_QUEST_SORT_CLOSED)
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
        this@HomeQuestFragment.tv_sort_text.text = getString(R.string.home_quest_sort_all)
        setFontTypeface(this@HomeQuestFragment.tv_sort_text)
    }

    private fun removeCache() = questViewModel.removeCache()
}