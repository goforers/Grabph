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

package com.goforer.grabph.presentation.ui.category.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_BOLD
import com.goforer.base.presentation.view.helper.ItemTouchHelperListener
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.ui.category.fragment.CategoryFragment
import com.goforer.grabph.data.datasource.model.cache.data.entity.category.Category
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.recycler_view_container.*
import kotlinx.android.synthetic.main.snap_main_catergory_item.*

class CategoryAdapter(private val fragment: CategoryFragment): PagedListAdapter<Category, CategoryAdapter.PhotoTypeHolder>(DIFF_CALLBACK),
                                                               ItemTouchHelperListener {
    companion object {
        private const val PHOTO_RATIO_WIDTH = 114
        private const val PHOTO_RATIO_HEIGHT = 114

        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldCategory: Category, newCategory: Category): Boolean =
                    oldCategory.photo?.m ==  newCategory.photo?.m

            override fun areContentsTheSame(oldCategory: Category, newCategory: Category): Boolean =
                    oldCategory ==  newCategory

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoTypeHolder {
        val view = LayoutInflater.from(parent.context.applicationContext)
                .inflate(R.layout.snap_main_catergory_item, parent, false)

        return PhotoTypeHolder(view, this)
    }

    override fun onBindViewHolder(holder: PhotoTypeHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<Category>?, currentList: PagedList<Category>?) {
        fragment.stopRefreshing(true)
    }

    override fun onItemDrag(actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            fragment.swipe_layout.isRefreshing = false
            fragment.swipe_layout.isEnabled = false
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            fragment.swipe_layout.isEnabled = true
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return false
    }

    override fun onItemDismiss(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Suppress("NAME_SHADOWING", "DEPRECATION")
    class PhotoTypeHolder(override val containerView: View, private val adapter: CategoryAdapter): BaseViewHolder<Category>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Category, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            adapter.fragment.categoryActivity.setFontTypeface(tv_snap_category_item_title, FONT_TYPE_BOLD)
            iv_snap_category_item_content.requestLayout()
            tv_snap_category_item_title.requestLayout()
            adapter.fragment.categoryActivity.setFixedImageSize(PHOTO_RATIO_HEIGHT, PHOTO_RATIO_WIDTH)
            adapter.fragment.categoryActivity.setImageDraw(iv_snap_category_item_content,
                    snap_category_constraintLayoutContainer,
                    item.photo?.m!!, false)
            tv_snap_category_item_title.text = item.title
            snap_category_item_holder.visibility = View.VISIBLE
            card_snap_category_holder.visibility = View.VISIBLE
            iv_snap_category_item_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
            iv_snap_category_item_content.setOnClickListener {
            }

            if (adapter.fragment.swipe_layout.isRefreshing) {
                adapter.fragment.stopRefreshing(true)
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