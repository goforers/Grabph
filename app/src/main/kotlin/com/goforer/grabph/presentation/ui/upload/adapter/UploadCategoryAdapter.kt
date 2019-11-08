package com.goforer.grabph.presentation.ui.upload.adapter

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
    private val checkStatus = ArrayList<Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context.applicationContext)
        val view = inflater.inflate(R.layout.list_upload_category_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categories[position]
        holder.bindItemHolder(holder, item, position)

        holder.itemView.setOnClickListener {
            setSelectedButtonColor(position)
        }
    }

    internal fun addList(list: ArrayList<String>) {
        categories.clear()
        categories.addAll(list)
        notifyDataSetChanged()

        initCheckStatus(list.size)
    }

    private fun initCheckStatus(listSize: Int) {
        checkStatus.add(true)
        for (i in 1 until listSize) { checkStatus.add(false) }
        setSelectedButtonColor(0)
    }

    private fun setSelectedButtonColor(selectedAt: Int) {
        for (i in 0 until checkStatus.size) { checkStatus[i] = false }
        checkStatus[selectedAt] = true
    }

    class ViewHolder(override val containerView: View): BaseViewHolder<String>(containerView), LayoutContainer {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: String, position: Int) {
            tv_upload_category.requestLayout()
            tv_upload_category.text = item
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }
    }
}