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

package com.goforer.grabph.presentation.ui.home.profile.adapter.photos

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_BOLD
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery
import com.goforer.grabph.presentation.ui.home.HomeActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_profile_photos_item.*

class ProfileGalleryAdapter(private val activity: HomeActivity)
    : PagedListAdapter<MyGallery, ProfileGalleryAdapter.MyPhotosViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val STOP_LOADING_TIME_OUT = 50L

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<MyGallery>() {
            override fun areItemsTheSame(oldItem: MyGallery, newItem: MyGallery): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MyGallery, newItem: MyGallery): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPhotosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.list_profile_photos_item, parent, false)

        return MyPhotosViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: MyPhotosViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bindItemHolder(holder, it, position) }
    }

    override fun onCurrentListChanged(previousList: PagedList<MyGallery>?, currentList: PagedList<MyGallery>?) {
        super.onCurrentListChanged(previousList, currentList)
        withDelay(STOP_LOADING_TIME_OUT) {
            previousList?.let {

            }
        }
    }
    class MyPhotosViewHolder(override val containerView: View, private val activity: HomeActivity): BaseViewHolder<MyGallery>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: MyGallery, position: Int) {
            activity.setFontTypeface(tv_profile_mission_price, FONT_TYPE_BOLD)
            iv_profile_my_photo.requestLayout()
            tv_profile_mission_price.requestLayout()
            activity.setFixedImageSize(400, 400) // original value: 0, 0

            val url = ("https://farm" + item.farm + ".staticflickr.com/" + item.server + "/" + item.id + "_" + item.secret + ".jpg")
            activity.setImageDraw(iv_profile_my_photo, url)
            // tv_profile_mission_price.visibility = View.VISIBLE

            iv_profile_my_photo.setOnClickListener {
            }
        }

        override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

        override fun onItemClear() { containerView.setBackgroundColor(0) }
    }
}