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

package com.goforer.grabph.presentation.ui.home.main.adapter.snapadapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_BOLD
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.SELECTED_CATEGORY_PHOTO_INFO_ITEM_POSITION
import com.goforer.grabph.presentation.caller.Caller.callCategoryPhoto
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.data.datasource.model.cache.data.entity.category.Category
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.snap_main_catergory_item.*

class CategoryAdapter(private val activity: HomeActivity)
    : PagedListAdapter<Category, CategoryAdapter.CategoryViewHolder>(DIFF_CALLBACK) {
    companion object {
        private const val PHOTO_RATIO_WIDTH = 114
        private const val PHOTO_RATIO_HEIGHT = 114

        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK
                = object: DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldCategory: Category,
                                         newCategory: Category): Boolean = oldCategory.id == newCategory.id

            override fun areContentsTheSame(oldCategory: Category,
                                            newCategory: Category): Boolean = oldCategory == newCategory

            override fun getChangePayload(oldCategory: Category, newCategory: Category): Any? {
                return if (sameExceptTitle(oldCategory, newCategory)) {
                    PAYLOAD_TITLE
                } else {
                    null
                }
            }
        }

        private fun sameExceptTitle(oldCategory: Category, newCategory: Category): Boolean {
            return oldCategory.copy(title = newCategory.title) == newCategory
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.snap_main_catergory_item, parent, false)

        return CategoryViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    class CategoryViewHolder(override val containerView: View, private val activity: HomeActivity): BaseViewHolder<Category>(containerView), LayoutContainer {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Category, position: Int) {
            activity.setFontTypeface(tv_snap_category_item_title, FONT_TYPE_BOLD)
            iv_snap_category_item_content.requestLayout()
            tv_snap_category_item_title.requestLayout()
            activity.setFixedImageSize(PHOTO_RATIO_HEIGHT, PHOTO_RATIO_WIDTH)
            activity.setImageDraw(iv_snap_category_item_content, snap_category_constraintLayoutContainer, item.photo?.m!!, false)
            tv_snap_category_item_title.text = item.title
            snap_category_item_holder.visibility = View.VISIBLE
            card_snap_category_holder.visibility = View.VISIBLE
            iv_snap_category_item_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
            iv_snap_category_item_content.setOnClickListener {
                activity.closeFab()
                callCategoryPhoto(activity, iv_snap_category_item_content,
                        item.photo.m, item.id, item.title, Caller.FIRST_PAGE,
                        item.type, holder.adapterPosition, SELECTED_CATEGORY_PHOTO_INFO_ITEM_POSITION)
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