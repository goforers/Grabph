package com.goforer.grabph.presentation.ui.home.quest.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.rubensousa.gravitysnaphelper.GravityPagerSnapHelper
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_BOLD
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.SnapItem
import com.goforer.grabph.presentation.ui.home.quest.adapter.snapadapter.FavoriteKeywordAdapter
import com.goforer.grabph.presentation.ui.home.quest.adapter.snapadapter.HotQuestAdapter
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.TopPortionQuest.FavoriteKeyword.Keyword
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
import com.goforer.grabph.data.repository.paging.datasource.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.snap_quest_item.*
import kotlinx.android.synthetic.main.snap_quest_item.indicator

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class HomeTopPortionQuestAdapter(private val activity: HomeActivity): RecyclerView.Adapter<HomeTopPortionQuestAdapter.ViewHolder>(),
                                                                      GravitySnapHelper.SnapListener {
    private val snapItems: MutableList<SnapItem> by lazy {
        ArrayList<SnapItem>()
    }

    internal var itemCount: Int = 0

    companion object {
        private const val VERTICAL = 0
        private const val HORIZONTAL = 1

        internal const val ADAPTER_HOT_MISSION_INDEX = 0
        internal const val ADAPTER_FAVORITE_KEYWORD_INDEX = 1
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
                .inflate(R.layout.snap_quest_item, parent, false)

        return ViewHolder(view, this)
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

    class ViewHolder(override val containerView: View, private val adapter: HomeTopPortionQuestAdapter): BaseViewHolder<SnapItem>(containerView), LayoutContainer {
        @Suppress("UNCHECKED_CAST")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: SnapItem, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            with(holder.itemView) {
                adapter.activity.setFontTypeface(tv_snap_quest_item_title, FONT_TYPE_BOLD)
                when(position) {
                    ADAPTER_HOT_MISSION_INDEX -> {
                        val padding = recycler_view_snap_hot_quest.resources.getDimensionPixelOffset(R.dimen.padding_8)

                        tv_snap_quest_item_title.text = item.title
                        if (item.padding) {
                            if (item.gravity == Gravity.START) {
                                recycler_view_snap_hot_quest.setPadding(padding, 0, padding, 0)
                            } else if (item.gravity === Gravity.END) {
                                recycler_view_snap_hot_quest.setPadding(padding, 0, padding, 0)
                            }
                        } else {
                            recycler_view_snap_hot_quest.setPadding(0, 0, 0, 0)
                        }

                        adapter.activity.closeFab(recycler_view_snap_hot_quest)
                        attachToRecyclerView(item, recycler_view_snap_hot_quest)

                        val hotMissionAdapter = HotQuestAdapter(adapter.activity)
                        val config = PagedList.Config.Builder()
                                .setInitialLoadSizeHint(1)
                                .setPageSize(5)
                                .setPrefetchDistance(4)
                                .build()

                        recycler_view_snap_hot_quest.adapter = hotMissionAdapter
                        LivePagedListBuilder(object : DataSource.Factory<Int, Quest>() {
                            override fun create(): DataSource<Int, Quest> {
                                return HotQuestListDataSource(item.items as List<Quest>)
                            }
                        },  /* PageList Config */ config).build().observe(adapter.activity, Observer {
                            hotMissionAdapter.submitList(it!!)
                            indicator.setMaxCount(it.size)
                            tv_snap_quest_item_title.visibility = View.VISIBLE
                            indicator.setDefaultColor(resources.getColor(R.color.colorHomeMainHottopicIndicatorUnselect, null))
                            indicator.setSelectedColor(resources.getColor(R.color.colorHomeMainHottopicIndicatorSelect, null))
                            indicator.setIndicatorSize((resources.getDimension(R.dimen.size_8) / resources.displayMetrics.density).toInt())
                            indicator.setIndicatorSpacing((resources.getDimension(R.dimen.space_8) / resources.displayMetrics.density).toInt())
                            indicator.attachTo(recycler_view_snap_hot_quest)
                            snap_quest_item_layout.visibility = View.VISIBLE
                            recycler_view_snap_hot_quest.visibility = View.VISIBLE
                            indicator.visibility = View.VISIBLE
                        })
                    }

                    ADAPTER_FAVORITE_KEYWORD_INDEX -> {
                        val padding = recycler_view_snap_favorite_keyword.resources.getDimensionPixelOffset(R.dimen.padding_8)

                        tv_snap_quest_item_title.visibility = View.GONE
                        indicator.visibility = View.GONE
                        snap_quest_item_layout.visibility = View.VISIBLE
                        recycler_view_snap_favorite_keyword.visibility = View.VISIBLE
                        tv_snap_quest_item_title.text = item.title
                        if (item.padding) {
                            if (item.gravity == Gravity.START) {
                                recycler_view_snap_favorite_keyword.setPadding(padding, 0, padding, 0)
                            } else if (item.gravity === Gravity.END) {
                                recycler_view_snap_favorite_keyword.setPadding(padding, 0, padding, 0)
                            }
                        } else {
                            recycler_view_snap_favorite_keyword.setPadding(0, 0, 0, 0)
                        }

                        adapter.activity.closeFab(recycler_view_snap_favorite_keyword)
                        attachToRecyclerView(item, recycler_view_snap_favorite_keyword)

                        val favoriteKeywordAdapter = FavoriteKeywordAdapter(adapter.activity)
                        val config = PagedList.Config.Builder()
                                .setInitialLoadSizeHint(1)
                                .setPageSize(1)
                                .setPrefetchDistance(1)
                                .build()

                        recycler_view_snap_favorite_keyword.adapter = favoriteKeywordAdapter
                        LivePagedListBuilder(object : DataSource.Factory<Int, Keyword>() {
                            override fun create(): DataSource<Int, Keyword> {
                                return FavoriteKeywordListDataSource(item.items as List<Keyword>)
                            }
                        },  /* PageList Config */ config).build().observe(adapter.activity, Observer {
                            favoriteKeywordAdapter.submitList(it!!)
                        })
                    }
                }
            }
        }

        override fun onItemSelected() {
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
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