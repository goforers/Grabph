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

package com.goforer.grabph.presentation.ui.home.main.adapter

import android.os.Build
import android.text.Html
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import android.view.Gravity
import com.github.rubensousa.gravitysnaphelper.GravityPagerSnapHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.*
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_BOLD
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_MEDIUM
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.SnapItem
import com.goforer.grabph.presentation.ui.home.main.adapter.snapadapter.*
import com.goforer.grabph.repository.model.cache.data.entity.category.Category
import com.goforer.grabph.repository.model.cache.data.entity.home.Home
import com.goforer.grabph.repository.model.cache.data.entity.quest.Quest
import com.goforer.grabph.repository.interactor.paging.datasource.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.snap_main_catergory_item.*
import kotlinx.android.synthetic.main.snap_main_item.*

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class HomeMainAdapter(private val activity: HomeActivity, private val workHandler: CommonWorkHandler)
    : RecyclerView.Adapter<HomeMainAdapter.ViewHolder>(), GravitySnapHelper.SnapListener {
    private val snapItems: MutableList<SnapItem> by lazy {
        ArrayList<SnapItem>()
    }

    internal var itemCount: Int = 0

    companion object {
        private const val VERTICAL = 0
        private const val HORIZONTAL = 1

        internal const val ADAPTER_HOT_TOPIC_INDEX = 0
        internal const val ADAPTER_POPULAR_SEARPER_INDEX = 1
        internal const val ADAPTER_POPULAR_QUEST_INDEX = 2
        internal const val ADAPTER_CATEGORY_INDEX = 3

        private const val GRID_SPAN_COUNT = 3
    }

    override fun getItemViewType(position: Int): Int {
        val item = snapItems[position]

        when (item.gravity) {
            Gravity.CENTER_VERTICAL -> return VERTICAL
            Gravity.CENTER_HORIZONTAL -> return HORIZONTAL
            Gravity.START -> return HORIZONTAL
            Gravity.TOP -> return VERTICAL
            Gravity.END -> return HORIZONTAL
            Gravity.BOTTOM -> return VERTICAL
        }

        return HORIZONTAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context.applicationContext)
                .inflate(R.layout.snap_main_item, parent, false)

        return ViewHolder(view, this, workHandler)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItemHolder(holder, snapItems[position], position)
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    override fun onSnap(position: Int) {
    }

    internal fun addSnapItem(snapItem: SnapItem) {
        snapItems.add(snapItem)
    }

    class ViewHolder(override val containerView: View, private val adapter: HomeMainAdapter,
                     private val workHandler: CommonWorkHandler): BaseViewHolder<SnapItem>(containerView), LayoutContainer {
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: SnapItem, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            setFontTypeface(holder.itemView)
            when(position) {
                ADAPTER_HOT_TOPIC_INDEX -> {
                    val padding = recycler_view_snap_hottopic_pick.resources.getDimensionPixelOffset(R.dimen.padding_8)

                    tv_snap_item_title.visibility = View.GONE
                    if (item.padding) {
                        if (item.gravity == Gravity.START) {
                            recycler_view_snap_hottopic_pick.setPadding(padding, 0, padding, 0)
                        } else if (item.gravity === Gravity.END) {
                            recycler_view_snap_hottopic_pick.setPadding(padding, 0, padding, 0)
                        }
                    } else {
                        recycler_view_snap_hottopic_pick.setPadding(0, 0, 0, 0)
                    }

                    adapter.activity.closeFab(recycler_view_snap_hottopic_pick)
                    attachToRecyclerView(item, recycler_view_snap_hottopic_pick)
                    val adapter = HotTopicPickAdapter(adapter.activity)

                    adapter.addItem(item.items)
                    recycler_view_snap_hottopic_pick.adapter = adapter
                    indicator.setMaxCount(item.items.size)
                    indicator.setDefaultColor(containerView.resources.getColor(R.color.colorHomeMainHottopicIndicatorUnselect, null))
                    indicator.setSelectedColor(containerView.resources.getColor(R.color.colorHomeMainHottopicIndicatorSelect, null))
                    indicator.setIndicatorSize((containerView.resources.getDimension(R.dimen.size_8) / containerView.resources.displayMetrics.density).toInt())
                    indicator.setIndicatorSpacing((containerView.resources.getDimension(R.dimen.space_8) / containerView.resources.displayMetrics.density).toInt())
                    indicator.attachTo(recycler_view_snap_hottopic_pick)
                    snap_item_layout.visibility = View.VISIBLE
                    recycler_view_snap_hottopic_pick.visibility = View.VISIBLE
                    indicator.visibility = View.VISIBLE
                    btn_snap_view_all.visibility = View.GONE
                    space_layout.visibility = View.GONE
                }

                ADAPTER_POPULAR_SEARPER_INDEX -> {
                    val padding = recycler_view_snap_searper.resources.getDimensionPixelOffset(R.dimen.padding_8)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tv_snap_item_title.text = Html.fromHtml(adapter.activity.getString(R.string.snap_featured_searper),
                                Html.FROM_HTML_MODE_LEGACY)
                    } else {
                        tv_snap_item_title.text = Html.fromHtml(adapter.activity.getString(R.string.snap_featured_searper))
                    }

                    tv_snap_item_title.setOnClickListener { // move to RankingActivity
                        Caller.callSearperRank(context)
                    }

                    if (item.padding) {
                        if (item.gravity == Gravity.START) {
                            recycler_view_snap_searper.setPadding(padding, 0, padding, 0)
                        } else if (item.gravity === Gravity.END) {
                            recycler_view_snap_searper.setPadding(padding, 0, padding, 0)
                        }
                    } else {
                        recycler_view_snap_searper.setPadding(0, 0, 0, 0)
                    }

                    adapter.activity.closeFab(recycler_view_snap_searper)
                    attachToRecyclerView(item, recycler_view_snap_searper)

                    val popularSearperAdapter = PopularSearperAdapter(adapter.activity, workHandler)
                    val config = PagedList.Config.Builder()
                            .setInitialLoadSizeHint(1)
                            .setPageSize(1)
                            .setPrefetchDistance(1)
                            .build()

                    recycler_view_snap_searper.adapter = popularSearperAdapter
                    LivePagedListBuilder(object : DataSource.Factory<Int, Home.PopSearper.Searper>() {
                        override fun create(): DataSource<Int, Home.PopSearper.Searper> {
                            return PopSearperListDataSource(item.items as List<Home.PopSearper.Searper>)
                        }
                    }, config).build().observe(adapter.activity, Observer {
                        popularSearperAdapter.submitList(it!!)
                        snap_item_layout.visibility = View.VISIBLE
                        recycler_view_snap_searper.visibility = View.VISIBLE
                        indicator.visibility = View.GONE
                        btn_snap_view_all.visibility = View.GONE
                        space_layout.visibility = View.GONE
                    })
                }

                ADAPTER_POPULAR_QUEST_INDEX -> {
                    val padding = recycler_view_snap_quest.resources.getDimensionPixelOffset(R.dimen.padding_8)

                    tv_snap_item_title.text = item.title
                    tv_snap_item_title.setOnClickListener {
                        adapter.activity.selectItem(adapter.activity.bottom_navigation_view
                                .menu.getItem(3),
                                R.id.navigation_quest)
                    }

                    btn_snap_view_all.setOnClickListener {
                        adapter.activity.selectItem(adapter.activity.bottom_navigation_view
                                .menu.getItem(3),
                                R.id.navigation_quest)
                    }

                    if (item.padding) {
                        if (item.gravity == Gravity.START) {
                            recycler_view_snap_quest.setPadding(padding, 0, padding, 0)
                        } else if (item.gravity === Gravity.END) {
                            recycler_view_snap_quest.setPadding(padding, 0, padding, 0)
                        }
                    } else {
                        recycler_view_snap_quest.setPadding(0, 0, 0, 0)
                    }

                    adapter.activity.closeFab(recycler_view_snap_quest)
                    attachToRecyclerView(item, recycler_view_snap_quest)

                    val missionAdapter = QuestAdapter(adapter.activity)
                    val config = PagedList.Config.Builder()
                            .setInitialLoadSizeHint(1)
                            .setPageSize(1)
                            .setPrefetchDistance(1)
                            .build()

                    recycler_view_snap_quest.adapter = missionAdapter
                    LivePagedListBuilder(object : DataSource.Factory<Int, Quest>() {
                        override fun create(): DataSource<Int, Quest> {
                            return PopQuestListDataSource(item.items as List<Quest>)
                        }
                    },  /* PageList Config */ config).build().observe(adapter.activity, Observer {
                        missionAdapter.submitList(it!!)
                        snap_item_layout.visibility = View.VISIBLE
                        recycler_view_snap_quest.visibility = View.VISIBLE
                        indicator.visibility = View.GONE
                        btn_snap_view_all.visibility = View.VISIBLE
                        space_layout.visibility = View.GONE
                    })
                }

                ADAPTER_CATEGORY_INDEX -> {
                    val padding = recycler_view_snap_category.resources.getDimensionPixelOffset(R.dimen.padding_8)

                    tv_snap_item_title.text = item.title
                    tv_snap_item_title.setOnClickListener {
                        Caller.callCategory(adapter.activity, iv_snap_category_item_content, Caller.SELECTED_CATEGORY_ITEM_POSITION)
                    }

                    btn_snap_view_all.text = adapter.activity.getString(R.string.snap_item_view_more_catagories)
                    btn_snap_view_all.setOnClickListener {
                        Caller.callCategory(adapter.activity, iv_snap_category_item_content, Caller.SELECTED_CATEGORY_ITEM_POSITION)
                    }

                    if (item.padding) {
                        if (item.gravity == Gravity.START) {
                            recycler_view_snap_category.setPadding(padding, 0, padding, 0)
                        } else if (item.gravity === Gravity.END) {
                            recycler_view_snap_category.setPadding(padding, 0, padding, 0)
                        }
                    } else {
                        recycler_view_snap_category.setPadding(0, 0, 0, 0)
                    }

                    when(item.gravity) {
                        Gravity.START, Gravity.END -> {
                            recycler_view_snap_category.layoutManager = GridLayoutManager(recycler_view_snap_category.context, GRID_SPAN_COUNT)
                            GravitySnapHelper(item.gravity, false, adapter).attachToRecyclerView(recycler_view_snap_category)
                        }

                        Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL -> {
                            recycler_view_snap_category.layoutManager = GridLayoutManager(recycler_view_snap_category.context, GRID_SPAN_COUNT, if (item.gravity === Gravity.CENTER_HORIZONTAL)
                                RecyclerView.HORIZONTAL
                            else
                                RecyclerView.VERTICAL, false)
                            LinearSnapHelper().attachToRecyclerView(recycler_view_snap_category)
                        }

                        Gravity.CENTER -> {
                            recycler_view_snap_category.layoutManager = GridLayoutManager(recycler_view_snap_category.context, GRID_SPAN_COUNT)
                            GravityPagerSnapHelper(Gravity.START).attachToRecyclerView(recycler_view_snap_category)
                        }

                        else -> {
                            recycler_view_snap_category.layoutManager = GridLayoutManager(recycler_view_snap_category.context, GRID_SPAN_COUNT)
                            GravitySnapHelper(item.gravity).attachToRecyclerView(recycler_view_snap_category)
                        }
                    }

                    adapter.activity.closeFab(recycler_view_snap_category)

                    val categoryAdapter = CategoryAdapter(adapter.activity)
                    val config = PagedList.Config.Builder()
                            .setInitialLoadSizeHint(1)
                            .setPageSize(1)
                            .setPrefetchDistance(1)
                            .build()

                    recycler_view_snap_category.adapter = categoryAdapter
                    LivePagedListBuilder(object : DataSource.Factory<Int, Category>() {
                        override fun create(): DataSource<Int, Category> {
                            return CategoryListDataSource(item.items as List<Category>)
                        }
                    }, /* PageList Config */ config).build().observe(adapter.activity, Observer {
                        categoryAdapter.submitList(it!!)
                        snap_item_layout.visibility = View.VISIBLE
                        recycler_view_snap_category.visibility = View.VISIBLE
                        indicator.visibility = View.GONE
                        space_layout.visibility = View.VISIBLE
                        btn_snap_view_all.visibility = View.VISIBLE
                        divider.visibility = View.GONE
                    })
                }
            }
        }

        override fun onItemSelected() {
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }

        private fun setFontTypeface(view: View) {
            with(view) {
                adapter.activity.setFontTypeface(tv_snap_item_title, FONT_TYPE_BOLD)
                adapter.activity.setFontTypeface(btn_snap_view_all, FONT_TYPE_MEDIUM)
            }
        }

        private fun attachToRecyclerView(item: SnapItem, recyclerView: RecyclerView) {
            when(item.gravity) {
                Gravity.START, Gravity.END -> {
                    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false)
                    GravitySnapHelper(item.gravity, false, adapter).attachToRecyclerView(recyclerView)
                }

                Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL -> {
                    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, if (item.gravity === Gravity.CENTER_HORIZONTAL)
                        RecyclerView.HORIZONTAL
                    else
                        RecyclerView.VERTICAL, false)

                    LinearSnapHelper().attachToRecyclerView(recyclerView)
                }

                Gravity.CENTER -> {
                    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false)
                    GravityPagerSnapHelper(Gravity.START).attachToRecyclerView(recyclerView)
                }

                else -> {
                    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
                    GravitySnapHelper(item.gravity).attachToRecyclerView(recyclerView)
                }
            }
        }
    }
}
