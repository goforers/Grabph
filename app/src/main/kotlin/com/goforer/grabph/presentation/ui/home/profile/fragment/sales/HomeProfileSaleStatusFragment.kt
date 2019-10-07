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

package com.goforer.grabph.presentation.ui.home.profile.fragment.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SALES_STATUS_TYPE
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.profile.adapter.sales.HomeProfileSaleStatusAdapter
import com.goforer.grabph.presentation.ui.home.profile.fragment.sales.HomeProfileSalesFragment.Companion.SALES_ALL_INDEX
import com.goforer.grabph.presentation.ui.home.profile.fragment.sales.HomeProfileSalesFragment.Companion.SALES_APPROVED_INDEX
import com.goforer.grabph.presentation.ui.home.profile.fragment.sales.HomeProfileSalesFragment.Companion.SALES_INVALID_INDEX
import com.goforer.grabph.presentation.ui.home.profile.fragment.sales.HomeProfileSalesFragment.Companion.SALES_VERIFYING_INDEX
import com.goforer.grabph.presentation.vm.profile.HomeProfileViewModel
import com.goforer.grabph.repository.model.cache.data.entity.profile.MyPhoto
import com.goforer.grabph.repository.model.cache.data.mock.datasource.profile.MyPhotoDataSource
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_profile_sale_status.*

class HomeProfileSaleStatusFragment : BaseFragment() {
    private lateinit var adapter: HomeProfileSaleStatusAdapter
    private lateinit var acvAdapterSales: AutoClearedValue<HomeProfileSaleStatusAdapter>

    private var mySalesPaged: PagedList<MyPhoto>? = null

    private var statusType = 0

    @field:Inject
    lateinit var homeProfileViewModel: HomeProfileViewModel

    internal val homeActivity: HomeActivity by lazy { activity as HomeActivity }

    companion object {
        private const val SPAN_COUNT = 3
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val acvView = AutoClearedValue(
            this,
            inflater.inflate(R.layout.fragment_home_profile_sale_status, container, false)
        )

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.getInt(EXTRA_SALES_STATUS_TYPE)?.let { statusType = it }

        setSalesStatus()
        setScrollListener()
        createSalesAdapter()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(EXTRA_SALES_STATUS_TYPE, statusType)
    }

    internal fun setStatusType(type: Int) {
        statusType = type
    }

    private fun createSalesAdapter() {
        adapter = HomeProfileSaleStatusAdapter(homeActivity, statusType)
        acvAdapterSales = AutoClearedValue(this, adapter)
        this@HomeProfileSaleStatusFragment.recycler_profile_sale_all.adapter = acvAdapterSales.get()

        if (statusType == SALES_INVALID_INDEX) { // status = 3
            this@HomeProfileSaleStatusFragment.recycler_profile_sale_all.layoutManager =
                LinearLayoutManager(this.context)

            val dividerItemDecoration =
                DividerItemDecoration(context, LinearLayoutManager(context).orientation)

            homeActivity.getDrawable(R.drawable.recycer_divider)
                ?.let { dividerItemDecoration.setDrawable(it) }
            this@HomeProfileSaleStatusFragment.recycler_profile_sale_all.addItemDecoration(
                dividerItemDecoration
            )
        } else { // status = 0,1,2
            this@HomeProfileSaleStatusFragment.recycler_profile_sale_all.layoutManager =
                GridLayoutManager(context, SPAN_COUNT)
        }

        this.mySalesPaged?.let { adapter.submitList(it) }
    }

    private fun setSalesStatus() {
        homeProfileViewModel.getSalesStatusLiveData().observe(this, Observer { sales ->
            val filteredSales: List<MyPhoto> = when (statusType) {
                SALES_ALL_INDEX -> sales

                SALES_VERIFYING_INDEX -> sales.filter { it.status == "verifying" }

                SALES_APPROVED_INDEX -> sales.filter { it.status == "approved" }

                else -> sales.filter { it.status == "invalid" }
            }

            val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .setPrefetchDistance(5)
                .build()

            LivePagedListBuilder(object : DataSource.Factory<Int, MyPhoto>() {
                override fun create(): DataSource<Int, MyPhoto> {
                    return MyPhotoDataSource(filteredSales)
                }
            }, config).build().observe(this, Observer { pagedList ->
                mySalesPaged = pagedList
                acvAdapterSales.get()?.submitList(pagedList)
                adapter.submitList(pagedList)
            })
        })
    }

    private fun setScrollListener() {
        this@HomeProfileSaleStatusFragment.recycler_profile_sale_all.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        homeActivity.closeFab()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                    }
                    else -> {
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val vnView = homeActivity.bottom_navigation_view

                vnView.translationY =
                    max(0f, min(vnView.height.toFloat(), vnView.translationY + dy))
                homeActivity.closeFab()
            }
        })
    }
}
