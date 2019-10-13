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

@file:Suppress("DEPRECATION")

package com.goforer.grabph.presentation.ui.search.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.goforer.base.presentation.utils.CommonUtils.dismissKeyboard
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.view.adatper.BaseListAdapter.Companion.TYPE_FOR_SEARCH_KEYWORD
import com.goforer.base.presentation.view.decoration.RemoverItemDecoration
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.base.presentation.view.helper.RecyclerItemTouchHelperCallback
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.event.action.DeleteKeywordAction
import com.goforer.grabph.presentation.event.action.SearchKeywordSubmitAction
import com.goforer.grabph.presentation.ui.search.FeedSearchActivity
import com.goforer.grabph.presentation.ui.search.adapter.RecentKeywordAdapter
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.presentation.vm.search.SearchKeywordViewModel
import com.goforer.grabph.repository.model.cache.data.entity.search.RecentKeyword
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.LOAD_SEARCH_KEYWORD
import kotlinx.android.synthetic.main.fragment_search_filter.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class RecentKeywordFragment : RecyclerFragment<RecentKeyword>() {
    private val job = Job()

    private val defaultScope = CoroutineScope(Dispatchers.Main + job)

    private val feedSearchActivity: FeedSearchActivity by lazy {
        activity as FeedSearchActivity
    }

    private lateinit var adapter: RecentKeywordAdapter
    private lateinit var acvAdapter: AutoClearedValue<RecentKeywordAdapter>

    @field:Inject
    lateinit var searchKeywordViewModel: SearchKeywordViewModel

    companion object {
        private const val SEARCH_KEYWORD_ITEM_MAX_SIZE = 15
        private const val DELAY_TIMER = 100
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        val autoClearedValue = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_search_filter, container, false))
        return autoClearedValue.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresh(true)
        setItemHasFixedSize(true)
    }

    @ExperimentalCoroutinesApi
    override fun onDestroyView() {
        super.onDestroyView()

        EventBus.getDefault().unregister(this)
        defaultScope.cancel()
        job.cancel()
    }

    override fun createLayoutManager(): RecyclerView.LayoutManager {
        super.setOnProcessListener(object : OnProcessListener {
            override fun onScrolledToLast(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolledToLast")
            }

            override fun onScrolling(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolling")

                Timber.i("onScrolling")
                val view = feedSearchActivity.getSearchView()
                view.clearFocus()
                dismissKeyboard(view.windowToken, baseActivity)
            }

            override fun onScrollIdle(position: Int) {
            }

            override fun onScrollSetting() {
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolled")
            }

            override fun onError(message: String) {
                showToastMessage(baseActivity, message, Toast.LENGTH_SHORT)
            }
        })

        recycler_view.setHasFixedSize(false)
        recycler_view.setItemViewCacheSize(20)
        recycler_view.isDrawingCacheEnabled = true
        recycler_view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        val linearLayoutManager = LinearLayoutManager(baseActivity,
                                                        RecyclerView.VERTICAL, false)
        linearLayoutManager.isItemPrefetchEnabled = true
        linearLayoutManager.initialPrefetchItemCount = SEARCH_KEYWORD_ITEM_MAX_SIZE

        return linearLayoutManager
    }

    override fun createItemDecoration(): RecyclerView.ItemDecoration {
        return RemoverItemDecoration(Color.RED)
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        adapter = RecentKeywordAdapter(this)
        acvAdapter = AutoClearedValue(this, adapter)
        adapter.setEnableLoadingImage(true)
        recycler_view.adapter = adapter

        return adapter
    }

    override fun onSorted(items: List<RecentKeyword>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createItemTouchHelper(): ItemTouchHelper.Callback {
        return RecyclerItemTouchHelperCallback(this.context.applicationContext, adapter, Color.BLUE)
    }

    override fun requestData(isNew: Boolean) {
        getSearchKeywords()

        Timber.i("requestData")
    }

    override fun updateData() {
        stopLoading(STOP_REFRESHING_TIMEOUT)
        // Don't implement this code... Because does not need to get updated all search keyword.
    }

    override fun isItemDecorationVisible(): Boolean {
        return false
    }

    override fun reachToLastPage() {

    }

    override fun onFirstVisibleItem(position: Int) {}

    override fun onLastVisibleItem(position: Int) {}

    private fun getSearchKeywords() {
        val keyword = feedSearchActivity.queryKeyword

        keyword.let {
            searchKeywordViewModel.setParameters(Parameters(keyword, -1, LOAD_SEARCH_KEYWORD, BOUND_FROM_LOCAL), NONE_TYPE)
            getKeywords()
        }
    }

    private fun putKeyword(keyword: String): String {
        searchKeywordViewModel.setParameters(Parameters(keyword, -1, LOAD_SEARCH_KEYWORD, BOUND_FROM_LOCAL), NONE_TYPE)
        getKeywords()

        return ""
    }

    private fun getKeywords() {
        val liveData = searchKeywordViewModel.searchKeywords

        liveData.observe(this, androidx.lifecycle.Observer {
            it?.let { list ->
                acvAdapter.get()?.addItems(list as MutableList<RecentKeyword>, TYPE_FOR_SEARCH_KEYWORD, false)
                stopLoading(STOP_REFRESHING_TIMEOUT)
            }
        })

        launchWork {
            delay(DELAY_TIMER.toLong())
            removeObserver(searchKeywordViewModel.searchKeywords)
        }
    }

    private fun removeObserver(liveData: LiveData<List<RecentKeyword>>) {
        liveData?.removeObservers(this)
    }

    private fun isKeywordExisted(keyword: String?): Boolean {
        val keywords = acvAdapter.get()?.items ?: return false

        for ((_, keyword1) in keywords) {
            if (keyword1 == keyword) {
                return true
            }
        }

        return false
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAction(action: SearchKeywordSubmitAction) {
        if (!isKeywordExisted(action.keyword)) {
            val keyword = RecentKeyword(0, action.keyword, System.currentTimeMillis())

            acvAdapter.get()?.items?.let {
                if (it.size >= SEARCH_KEYWORD_ITEM_MAX_SIZE) {
                    searchKeywordViewModel.deleteSearchKeyword(
                            it[SEARCH_KEYWORD_ITEM_MAX_SIZE - 1].keyword)
                   it.removeAt(SEARCH_KEYWORD_ITEM_MAX_SIZE - 1)
                }
            }

            Timber.d("SearchKeywordSubmitAction - SearchKeywordSubmitAction")

            val liveData = searchKeywordViewModel.setSearchKeyword(action.keyword, keyword)
            liveData.observe(this, androidx.lifecycle.Observer {
                if (it != null) {
                    Timber.d("Complete - successfully saved the search keyword")
                    //recycler_view.scrollToPosition(0)
                } else {
                    Timber.d("Error - the search keyword")
                    showToastMessage(baseActivity, getString(R.string.phrase_fail_save_search_keyword), Toast.LENGTH_SHORT)
                }
            })
        }
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the defaultScope.
     *              lambda the some work will do
     */
    private inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return defaultScope.launch {
            block()
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAction(action: DeleteKeywordAction) {
        searchKeywordViewModel.deleteSearchKeyword(action.keyword)
    }
}
