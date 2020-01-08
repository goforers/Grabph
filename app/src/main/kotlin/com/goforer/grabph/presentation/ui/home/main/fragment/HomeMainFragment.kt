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

package com.goforer.grabph.presentation.ui.home.main.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.main.adapter.HomeMainAdapter
import com.goforer.grabph.presentation.ui.home.main.adapter.HomeMainAdapter.Companion.ADAPTER_HOT_TOPIC_INDEX
import com.goforer.grabph.presentation.ui.home.main.adapter.HomeMainAdapter.Companion.ADAPTER_CATEGORY_INDEX
import com.goforer.grabph.presentation.ui.home.main.adapter.HomeMainAdapter.Companion.ADAPTER_POPULAR_SEARPER_INDEX
import com.goforer.grabph.presentation.ui.home.main.adapter.HomeMainAdapter.Companion.ADAPTER_POPULAR_QUEST_INDEX
import com.goforer.grabph.presentation.ui.home.SnapItem
import com.goforer.grabph.data.datasource.model.cache.data.entity.home.Home
import com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.home.HomeDataSource
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.datasource.network.response.Status
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_main.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.full.findAnnotation

@RunWithMockData(true)
class HomeMainFragment: BaseFragment() {
    private lateinit var home: Home

    private var isPinnedUpdate = false

    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private lateinit var mainAdapter: HomeMainAdapter

    internal val homeActivity: HomeActivity by lazy {
        activity as HomeActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainAdapter = getAdapter()

        val acvView = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_home_main, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this@HomeMainFragment.swipe_main_layout?.post {
            this@HomeMainFragment.swipe_main_layout?.isRefreshing = true
        }

        this@HomeMainFragment.recycler_main_view.setHasFixedSize(true)
        this@HomeMainFragment.recycler_main_view.setItemViewCacheSize(6)
        this@HomeMainFragment.recycler_main_view.isVerticalScrollBarEnabled = false
        this@HomeMainFragment.recycler_main_view.layoutManager = LinearLayoutManager(this.context.applicationContext, RecyclerView.VERTICAL, false)

        this@HomeMainFragment.swipe_main_layout?.setColorSchemeResources(R.color.redLight)
        this@HomeMainFragment.swipe_main_layout?.setOnRefreshListener {
            this@HomeMainFragment.swipe_main_layout?.isRefreshing = false
            // Implement update data below...
        }

        this@HomeMainFragment.recycler_main_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                // val vnView = homeActivity.bottom_navigation_view
                val vnView = homeActivity.layout_bottom_navigation


                vnView.translationY = max(0f, min(vnView.height.toFloat(), vnView.translationY + dy))
                homeActivity.closeFab()
            }
        })

        getHome()
    }

    @ExperimentalCoroutinesApi
    override fun onDestroyView() {
        super.onDestroyView()

        EventBus.getDefault().unregister(this)
    }

    private fun getAdapter(): HomeMainAdapter {
        return HomeMainAdapter(homeActivity, homeActivity.workHandler)
    }

    private fun setSnapItemCount(count: Int) {
        mainAdapter.itemCount = count
    }

    private fun setSnapItem(mainAdapter: HomeMainAdapter, Home: Home, count: Int) {
        var title = ""
        var items: List<Any> = ArrayList()

        for (i in 0 until count) {
            when(i) {
                ADAPTER_HOT_TOPIC_INDEX -> {
                    title = ""
                    items = getBestPicks(Home.hotSearp)
                }

                ADAPTER_POPULAR_SEARPER_INDEX -> {
                    title = Home.popSearper.title
                    items = Home.popSearper.searper
                }

                ADAPTER_POPULAR_QUEST_INDEX -> {
                    title = Home.popQuest.title
                    items = Home.popQuest.quest
                }

                ADAPTER_CATEGORY_INDEX -> {
                    title = Home.phototype.title
                    items = Home.phototype.category
                }
            }

            mainAdapter.addSnapItem(SnapItem(Gravity.START, title, false, items))
        }
    }

    private fun getHome() {
        when(mock) {
            @MockData
            true -> {
                homeActivity.launchUIWork {
                    transactMockData()
                }
            }
            false -> transactRealData()
        }
    }

    @MockData
    private fun transactMockData() {
        val home = HomeDataSource()

        homeActivity.homeViewModel.loadHome()?.observe(homeActivity, Observer {
            it?.let { home ->
                if (isPinnedUpdate.not()) {
                    this.home = home
                    setSnapItemCount(this.home.itemcount)
                    setSnapItem(mainAdapter, this.home, this.home.itemcount)
                    this@HomeMainFragment.recycler_main_view.adapter = mainAdapter
                } else {
                    isPinnedUpdate = false
                }

                withDelay(800L) {
                    this@HomeMainFragment.swipe_main_layout?.let {
                        this@HomeMainFragment.swipe_main_layout.post {
                            this@HomeMainFragment.swipe_main_layout.isRefreshing = false
                        }
                    }

                    this@HomeMainFragment.recycler_main_view.visibility = View.VISIBLE
                }
            }
        })

        home.setHome()
        homeActivity.homeViewModel.setHome(home.getHome()!!)
    }

    private fun transactRealData() {
        val liveData =  homeActivity.homeViewModel.home

        homeActivity.setHomeMainLoadParam(NetworkBoundResource.LOAD_HOME, NetworkBoundResource.BOUND_FROM_LOCAL, Caller.CALLED_FROM_HOME_MAIN, "")
        liveData.observe(homeActivity, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { home ->
                        if (isPinnedUpdate.not()) {
                            this.home = home as Home
                            setSnapItemCount(this.home.itemcount)
                            setSnapItem(mainAdapter, this.home, this.home.itemcount)
                            this@HomeMainFragment.recycler_main_view.adapter = mainAdapter
                        } else {
                            isPinnedUpdate = false
                        }

                        this@HomeMainFragment.swipe_main_layout?.let {
                            this@HomeMainFragment.swipe_main_layout?.isRefreshing = false
                        }
                    }

                    resource.getMessage()?.let {
                        homeActivity.showNetworkError(resource)
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
                }
            }
        })
    }

    private fun getBestPicks(bestpick: Home.HotSearp): List<Any> {
        val bestPicks = ArrayList<Any>()

        bestPicks.add(bestpick.hotTopic)
        bestPicks.add(bestpick.quest)
        bestPicks.add(bestpick.searperPhoto)
        bestPicks.add(bestpick.category)

        return bestPicks
    }
}