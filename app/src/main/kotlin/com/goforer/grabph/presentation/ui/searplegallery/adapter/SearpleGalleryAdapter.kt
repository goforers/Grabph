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

package com.goforer.grabph.presentation.ui.searplegallery.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.view.fragment.RecyclerFragment.Companion.STOP_REFRESHING_TIMEOUT
import com.goforer.base.presentation.view.helper.ItemTouchHelperListener
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.ui.searplegallery.fragment.SearpleGalleryFragment
import com.goforer.grabph.presentation.ui.searplegallery.SavedPhoto
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.grid_searple_gallery_item.view.*
import kotlinx.android.synthetic.main.recycler_view_container.*
import kotlinx.coroutines.delay
import java.io.File

class SearpleGalleryAdapter(private val fragment: SearpleGalleryFragment): PagedListAdapter<String, SearpleGalleryAdapter.ViewHolder>(DIFF_CALLBACK),
                                                                           ItemTouchHelperListener {
    companion object {
        private const val DELAY_TIMER_INTERVAL = 500

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldFileName: String, newFileName: String): Boolean =
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    newFileName == oldFileName


            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldFileName: String, newFileName: String): Boolean =
            // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    oldFileName == newFileName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.grid_searple_gallery_item, parent, false)

        return ViewHolder(view, fragment)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<String>?,
                                      currentList: PagedList<String>?) {
        fragment.launchWork {
            fragment.stopLoading(STOP_REFRESHING_TIMEOUT)
            delay(DELAY_TIMER_INTERVAL.toLong())
            CommonUtils.betterSmoothScrollToPosition(fragment.recycler_view, 0)
        }
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
        return true
    }

    override fun onItemDismiss(position: Int) {
    }

    @Suppress("DEPRECATION")
    class ViewHolder(override val containerView: View, private val fragment: SearpleGalleryFragment): BaseViewHolder<String>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: String, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            with(holder.itemView) {
                iv_photo_content.requestLayout()
                val bitmap = BitmapFactory.decodeFile(
                        CommonUtils.getImagePath(fragment.searpleGalleryActivity.applicationContext) + File.separator + item)

                fragment.searpleGalleryActivity.setFixedImageSize(0 ,0)
                fragment.searpleGalleryActivity.setImageDraw(iv_photo_content, searple_galleryConstraintLayoutContainer, bitmap, true)
                searple_gallery_item_holder.visibility = View.VISIBLE
                iv_photo_content.setOnClickListener {
                    iv_photo_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
                    Caller.callViewer(fragment.searpleGalleryActivity, iv_photo_content, holder.adapterPosition,
                            SavedPhoto.getPhotoFileNames(),
                            SavedPhoto.getPhotoIds(),
                            Caller.SELECTED_SEARPLE_GALLERY_ITEM_POSITION)
                }
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