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

package com.goforer.grabph.presentation.ui.home.profile.adapter.sales

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_MEDIUM
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_REGULAR
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.profile.fragment.sales.HomeProfileSalesFragment.Companion.SALES_INVALID_INDEX
import com.goforer.grabph.repository.model.cache.data.entity.profile.MyPhoto
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_profile_sale_item.*
import kotlinx.android.synthetic.main.list_profile_sale_item.iv_profile_sale_item_cover
import kotlinx.android.synthetic.main.list_profile_sale_item.tv_profile_sale_item_status
import kotlinx.android.synthetic.main.list_profile_sale_rejected_item.*

class HomeProfileSaleStatusAdapter(private val activity: HomeActivity,
                                   private val status: Int): PagedListAdapter<MyPhoto, BaseViewHolder<MyPhoto>>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<MyPhoto>() {
            override fun areItemsTheSame(oldItem: MyPhoto, newItem: MyPhoto): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MyPhoto, newItem: MyPhoto): Boolean = oldItem == newItem
        }

        private const val PHOTO_RATIO_WIDTH = 104
        private const val PHOTO_RATIO_HEIGHT = 104
        private const val PHOTO_RATIO_WIDTH_REJECTED = 70
        private const val PHOTO_RATIO_HEIGHT_REJECTED = 70
    }

    override fun getItemViewType(position: Int): Int = status

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<MyPhoto> {
        val view = when (viewType) {
            SALES_INVALID_INDEX -> { LayoutInflater.from(activity).inflate(R.layout.list_profile_sale_rejected_item, parent, false) }

            else -> { LayoutInflater.from(activity).inflate(R.layout.list_profile_sale_item, parent, false) }
        }

        return if (viewType == SALES_INVALID_INDEX) SaleRejectedViewHolder(view, activity) else SaleViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<MyPhoto>, position: Int) {
        val item = getItem(position)
        item?.let { holder.bindItemHolder(holder, it, position) }
    }

    class SaleViewHolder(override val containerView: View, private val activity: HomeActivity): BaseViewHolder<MyPhoto>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: MyPhoto, position: Int) {
            var saleStatus = ""
            var statusBackground: Int = R.drawable.border_rounded_sale_status_filled_orange
            var statusTextColor: Int = ContextCompat.getColor(activity, R.color.white)

            when(item.status) {
                activity.getString(R.string.my_photo_status_verifying) -> {
                    saleStatus = activity.getString(R.string.sale_status_verifying)
                    statusBackground = R.drawable.border_rounded_sale_status_filled_orange
                    statusTextColor = ContextCompat.getColor(activity, R.color.white)
                }

                activity.getString(R.string.my_photo_status_approved) -> {
                    saleStatus = activity.getString(R.string.sale_status_approved)
                    statusBackground = R.drawable.border_rounded_sale_status_orange
                    statusTextColor = ContextCompat.getColor(activity, R.color.colorSearp)
                }

                activity.getString(R.string.my_photo_status_invalid) -> {
                    saleStatus = activity.getString(R.string.sale_status_invalid)
                    statusBackground = R.drawable.border_rounded_sale_status_grey
                    statusTextColor = ContextCompat.getColor(activity, R.color.colorSalesStatusNone)
                }
            }

            setFontTypeface(holder.itemView)

            tv_profile_sale_item_status.requestLayout()
            tv_profile_sale_item_price.requestLayout()
            iv_profile_sale_item_cover.requestLayout()

            tv_profile_sale_item_status.text = saleStatus
            tv_profile_sale_item_status.setTextColor(statusTextColor)
            tv_profile_sale_item_status.setBackgroundResource(statusBackground)
            tv_profile_sale_item_price.text = "$${item.price}"

            val imgUrl = item.media?.urls?.small

            activity.setFixedImageSize(PHOTO_RATIO_HEIGHT, PHOTO_RATIO_WIDTH)
            imgUrl?.let { activity.setImageDraw(iv_profile_sale_item_cover, _profile_sale_item_container, it, false) }
        }

        override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

        override fun onItemClear() { containerView.setBackgroundColor(0) }

        private fun setFontTypeface(view: View) {
            with(view) {
                activity.setFontTypeface(tv_profile_sale_item_status, FONT_TYPE_MEDIUM)
                activity.setFontTypeface(tv_profile_sale_item_price, FONT_TYPE_REGULAR)
            }
        }
    }

    class SaleRejectedViewHolder(override val containerView: View, private val activity: HomeActivity): BaseViewHolder<MyPhoto>(containerView), LayoutContainer{
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: MyPhoto, position: Int) {
            with (holder.itemView) {
                tv_sales_rejected_status.text = activity.getString(R.string.sale_status_invalid)
                tv_sale_rejected_price.text = "$${item.price}"
                tv_sales_rejected_reason.text = item.reasonRejected
                setFontTypefaceRejected()

                val imgUrl = item.media?.urls?.small

                activity.setFixedImageSize(PHOTO_RATIO_HEIGHT_REJECTED, PHOTO_RATIO_WIDTH_REJECTED)
                imgUrl?.let { activity.setImageDraw(iv_sale_rejected_item_cover, sale_rejected_item_container, it, false) }
            }
        }

        override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

        override fun onItemClear() { containerView.setBackgroundColor(0) }

        private fun setFontTypefaceRejected() {
            activity.setFontTypeface(tv_sales_rejected_status, FONT_TYPE_MEDIUM)
            activity.setFontTypeface(tv_sale_rejected_price, FONT_TYPE_REGULAR)
            activity.setFontTypeface(tv_sales_rejected_reason, FONT_TYPE_REGULAR)
        }
    }
}