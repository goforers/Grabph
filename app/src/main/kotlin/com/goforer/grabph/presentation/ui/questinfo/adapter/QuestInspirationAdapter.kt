package com.goforer.grabph.presentation.ui.questinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.info.QuestInfo
import com.goforer.grabph.presentation.ui.questinfo.QuestInfoActivity
import kotlinx.android.synthetic.main.grid_feed_item.view.*

class QuestInspirationAdapter(private val activity: QuestInfoActivity): RecyclerView.Adapter<QuestInspirationAdapter.ViewHolder>() {
    private var items = ArrayList<QuestInfo.Photos.Photo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_feed_item, parent, false)
        return ViewHolder(activity, view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItemHolder(holder, items[position], position)
    }

    fun addList(list: List<QuestInfo.Photos.Photo>) {
        this.items.clear()
        this.items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val activity: QuestInfoActivity, private val holderItemView: View): BaseViewHolder<Any>(holderItemView) {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Any, position: Int) {
            val content = item as QuestInfo.Photos.Photo

            with (holder.itemView) {
                iv_feed_item_content.requestLayout()
                tv_feed_item_title.requestLayout()
                activity.setFixedImageSize(0, 0)
                activity.setImageDraw(iv_feed_item_content, feedConstraintLayoutContainer, content.image, true)
            }
        }

        override fun onItemSelected() {
        }

        override fun onItemClear() {
            holderItemView.setBackgroundColor(0)
        }
    }
}