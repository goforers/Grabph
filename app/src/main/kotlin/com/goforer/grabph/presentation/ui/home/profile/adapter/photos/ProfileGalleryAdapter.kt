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
import androidx.appcompat.widget.AppCompatImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_PROFILE
import com.goforer.grabph.presentation.caller.Caller.SELECTED_HOME_PROFILE_ITEM_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject.TRANSITION_NAME_FOR_IMAGE
import com.goforer.grabph.presentation.ui.home.HomeActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_profile_photos_item.*
import kotlin.random.Random

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

    class MyPhotosViewHolder(override val containerView: View, private val activity: HomeActivity): BaseViewHolder<MyGallery>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: MyGallery, position: Int) {
            iv_profile_my_photo.requestLayout()
            activity.setFixedImageSize(400, 400) // original value: 0, 0
            setRandomBackground(iv_profile_my_photo)

            val url = CommonUtils.getFlickrPhotoURL(item.server!!, item.id, item.secret!!)
            val options = RequestOptions.placeholderOf(R.drawable.ic_placeholder_image)
            Glide.with(activity).load(url).apply(options).into(iv_profile_my_photo)
            // activity.setImageDraw(iv_profile_my_photo, url)

            iv_play_btn.visibility = when (item.media) {
                activity.getString(R.string.media_type_video) -> View.VISIBLE
                activity.getString(R.string.media_type_photo) -> View.GONE
                else -> View.GONE
            }

            iv_profile_my_photo.transitionName = TRANSITION_NAME_FOR_IMAGE + position
            iv_profile_my_photo.setOnClickListener {
                Caller.callPhotoInfo(
                    activity,
                    iv_profile_my_photo,
                    item.id,
                    item.owner!!,
                    holder.adapterPosition,
                    SELECTED_HOME_PROFILE_ITEM_POSITION,
                    CALLED_FROM_HOME_PROFILE,
                    url
                )
            }
        }

        override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

        override fun onItemClear() { containerView.setBackgroundColor(0) }

        private fun setRandomBackground(iv: AppCompatImageView) {
            when (Random.nextInt(6)) {
                0 -> iv.setBackgroundColor(activity.getColor(R.color.colorImageBackgroundA))
                1 -> iv.setBackgroundColor(activity.getColor(R.color.colorImageBackgroundB))
                2 -> iv.setBackgroundColor(activity.getColor(R.color.colorImageBackgroundC))
                3 -> iv.setBackgroundColor(activity.getColor(R.color.colorImageBackgroundD))
                4 -> iv.setBackgroundColor(activity.getColor(R.color.colorImageBackgroundE))
                5 -> iv.setBackgroundColor(activity.getColor(R.color.colorImageBackgroundF))
            }
        }
    }
}