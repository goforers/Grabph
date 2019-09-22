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

package com.goforer.grabph.presentation.ui.othersprofile.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.repository.model.cache.data.entity.profile.MyPhoto
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_profile_photos_item.*

class OthersProfileAdapter(private val activity: BaseActivity): PagedListAdapter<MyPhoto, OthersProfileAdapter.PhotoViewHolder>(DIFF_CALLBACK) {

    private val photos by lazy { ArrayList<MyPhoto>() }

    companion object {
        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<MyPhoto>() {
            override fun areItemsTheSame(oldItem: MyPhoto, newItem: MyPhoto): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MyPhoto, newItem: MyPhoto): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.list_profile_photos_item, parent, false)

        return PhotoViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = getItem(position)

        item?.let { holder.bindItemHolder(holder, it, position) }
    }

    class PhotoViewHolder(override val containerView: View, private val activity: BaseActivity): BaseViewHolder<MyPhoto>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: MyPhoto, position: Int) {
            iv_profile_my_photo.requestLayout()
            tv_profile_mission_price.requestLayout()
            activity.setFixedImageSize(0, 0)
            item.media?.urls?.small?.let { activity.setImageDraw(iv_profile_my_photo, constraint_profile_photos, it, false) }
            item.price.let { tv_profile_mission_price.text = "$$it" }
            tv_profile_mission_price.visibility = View.VISIBLE
        }

        override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

        override fun onItemClear() { containerView.setBackgroundColor(0) }
    }
}