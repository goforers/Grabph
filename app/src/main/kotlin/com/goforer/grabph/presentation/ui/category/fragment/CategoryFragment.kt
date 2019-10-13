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

package com.goforer.grabph.presentation.ui.category.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.annotation.MockData
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.category.CategoryActivity
import com.goforer.grabph.presentation.ui.category.adapter.CategoryAdapter
import com.goforer.grabph.repository.model.cache.data.mock.datasource.category.CategoryDataSource
import com.goforer.grabph.repository.model.cache.data.entity.category.Category
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Status
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.interactor.paging.datasource.CategoryListDataSource
import kotlinx.android.synthetic.main.recycler_view_container.*
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.reflect.full.findAnnotation

@Suppress("SameParameterValue")
@RunWithMockData(true)
class CategoryFragment: RecyclerFragment<Category>() {
    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private lateinit var adapter: CategoryAdapter
    private lateinit var acvAdapter: AutoClearedValue<CategoryAdapter>

    private var isOverFirst = false

    private val job = Job()

    private val defaultScope = CoroutineScope(Dispatchers.Default + job)

    internal val categoryActivity: CategoryActivity by lazy {
        activity as CategoryActivity
    }

    internal lateinit var category: Category

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_category, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresh(true)
        setItemHasFixedSize(true)
    }

    @ExperimentalCoroutinesApi
    override fun onDestroyView() {
        super.onDestroyView()

        defaultScope.cancel()
        job.cancel()
    }

    override fun onSorted(items: List<Category>) {}

    override fun onFirstVisibleItem(position: Int) {
        // To Do::Implement playing the video file with position in case of video item
        Timber.i("onFirstVisibleItem : $position")
    }

    override fun onLastVisibleItem(position: Int) {
        // To Do::Implement playing the video file with position in case of video item
        Timber.i("onLastVisibleItem : $position")
    }

    override fun createLayoutManager(): RecyclerView.LayoutManager {
        super.setOnProcessListener(object : OnProcessListener {
            override fun onScrolledToLast(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolledToLast")
            }

            override fun onScrolling(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolling")
            }

            override fun onScrollIdle(position: Int) {
                if (position >= HomeActivity.VISIBLE_UPTO_ITEMS) {
                    isOverFirst = true
                }
            }

            override fun onScrollSetting() {

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolled")
            }

            override fun onError(message: String) {
                CommonUtils.showToastMessage(baseActivity, message, Toast.LENGTH_SHORT)
            }
        })

        this@CategoryFragment.recycler_view.setItemViewCacheSize(20)
        this@CategoryFragment.recycler_view.isVerticalScrollBarEnabled = false

        return GridLayoutManager(recycler_view.context, 3)
    }

    override fun createItemDecoration(): RecyclerView.ItemDecoration {
        return object : GapItemDecoration(VERTICAL_LIST,
                resources.getDimensionPixelSize(R.dimen.space_4)) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                outRect.left = 0
                outRect.right = 0
                outRect.bottom = gap

                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildAdapterPosition(view) == 0
                        || parent.getChildAdapterPosition(view) == 1) {
                    outRect.top = gap
                }
            }
        }
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        adapter = CategoryAdapter(this)
        acvAdapter = AutoClearedValue(this, adapter)
        this@CategoryFragment.recycler_view.adapter = acvAdapter.get()

        return adapter
    }

    override fun createItemTouchHelper(): ItemTouchHelper.Callback? {
        return null

        //If the drag-drop function has to be opened, then below code could be unlocked.
        //return new RecyclerItemTouchHelperCallback(getContext(), mAdapter, Color.BLUE);
    }

    override fun isItemDecorationVisible(): Boolean {
        return true
    }

    override fun requestData(isNew: Boolean) {
        Timber.i("requestData")

        getCategories()
    }

    override fun updateData() {
        /*
         * Please put some module to update new data here, instead of doneRefreshing() method if
         * there is some data to be updated from the backend side.
         * I just put doneRefreshing() method because there is no data to be updated from
         * the backend side in this app-architecture project.
         */
        Timber.i("updateData")
        this@CategoryFragment.swipe_layout?.isRefreshing = false
            // Implement update data below...
    }

    override fun reachToLastPage() {}

    private fun getCategories() {
        when(mock) {
            @MockData
            true -> transactMockCategoryList()
            false -> transactRealCategoryList("", NetworkBoundResource.LOAD_CATEGORY, Repository.BOUND_FROM_LOCAL, Caller.CALLED_FROM_PHOTO_TYPE)
        }
    }

    @MockData
    private fun transactMockCategoryList() {
        val category = CategoryDataSource()
        val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(1)
                .setPrefetchDistance(10)
                .build()

        categoryActivity.categoryViewModel.loadCategories()?.observe(this, Observer {
            it?.let {
                this@CategoryFragment.swipe_layout.visibility = View.VISIBLE
                if (it.isNotEmpty()) {
                    LivePagedListBuilder(object : DataSource.Factory<Int, Category>() {
                        override fun create(): DataSource<Int, Category> {
                            return CategoryListDataSource(it)
                        }
                    }, /* PageList Config */ config).build().observe(this, Observer { categories ->
                        acvAdapter.get()?.submitList(categories!!)
                    })
                }
            }
        })

        category.setCategories()
        categoryActivity.categoryViewModel.setCategories(category.getCategories()!!)
    }

    private fun transactRealCategoryList(userId: String, loadType: Int, boundType: Int, calledFrom: Int) {
        setLoadParam(loadType, boundType, userId)
        categoryActivity.categoryViewModel.category.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData() ?: return@Observer
                    this@CategoryFragment.swipe_layout.visibility = View.VISIBLE
                    @Suppress("UNCHECKED_CAST")
                    val categories = resource.getData() as? PagedList<Category>?
                    when {
                        categories?.size!! > 0 -> {
                            acvAdapter.get()?.submitList(categories)
                        }

                        resource.getMessage() != null -> {
                            categoryActivity.showNetworkError(resource)
                            categoryActivity.categoryViewModel.category.removeObservers(this)
                        }
                        else -> {
                            categoryActivity.showNetworkError(resource)
                            categoryActivity.categoryViewModel.category.removeObservers(this)
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    categoryActivity.showNetworkError(resource)
                    categoryActivity.categoryViewModel.category.removeObservers(this)
                }

                else -> {
                    categoryActivity.showNetworkError(resource)
                    categoryActivity.categoryViewModel.category.removeObservers(this)
                }
            }
        })
    }

    private fun setLoadParam(loadType: Int, boundType: Int, userId: String) {
        categoryActivity.categoryViewModel.setParameters(Parameters(userId, -1, loadType, boundType), -1)
    }
}