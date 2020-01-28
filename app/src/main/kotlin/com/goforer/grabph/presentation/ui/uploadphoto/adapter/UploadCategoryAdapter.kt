package com.goforer.grabph.presentation.ui.uploadphoto.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_upload_category_item.*

class UploadCategoryAdapter: RecyclerView.Adapter<UploadCategoryAdapter.ViewHolder>() {

    private val categories = ArrayList<String>()
    internal var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context.applicationContext)
        val view = inflater.inflate(R.layout.list_upload_category_item, parent, false)
        return ViewHolder(view, this)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categories[position]
        holder.bindItemHolder(holder, item, position)

        // this Click Listener is for selecting a category.
        // When user clicks an item,  notifyDataSetChanged() will rearrange colors of all buttons.
        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    internal fun addList(list: ArrayList<String>) {
        categories.clear()
        categories.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(
        override val containerView: View,
        private val adapter: UploadCategoryAdapter
    ): BaseViewHolder<String>(containerView), LayoutContainer {

        override fun bindItemHolder(holder: BaseViewHolder<*>, item: String, position: Int) {
            tv_upload_category.requestLayout()
            tv_upload_category.text = item

            if (adapter.selectedPosition == position) {
                tv_upload_category.setBackgroundResource(R.drawable.border_of_upload_category_selected)
            } else {
                tv_upload_category.setBackgroundResource(R.drawable.border_of_upload_category_white)
            }
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }
    }
}