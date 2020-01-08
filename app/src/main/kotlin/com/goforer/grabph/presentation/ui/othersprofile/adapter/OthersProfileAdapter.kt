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

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_OTHERS_PROFILE
import com.goforer.grabph.presentation.caller.Caller.SELECTED_FEED_ITEM_POSITION
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_profile_photos_item.*
import timber.log.Timber
import kotlin.random.Random

class OthersProfileAdapter(private val activity: BaseActivity) : PagedListAdapter<Photo, OthersProfileAdapter.PhotoViewHolder>(DIFF_CALLBACK) {

    private val photos by lazy { ArrayList<Photo>() }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
        }

        private const val TRANSITION_NAME_FOR_IMAGE = "Image "
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.list_profile_photos_item, parent, false)
        return PhotoViewHolder(view, activity)
    }

    override fun onCurrentListChanged(previousList: PagedList<Photo>?, currentList: PagedList<Photo>?) {
        Timber.d("check list")

        super.onCurrentListChanged(previousList, currentList)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bindItemHolder(holder, it, position) }
    }

    inner class PhotoViewHolder(
        override val containerView: View,
        private val activity: BaseActivity) : BaseViewHolder<Photo>(containerView), LayoutContainer {

        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Photo, position: Int) {
            iv_profile_my_photo.requestLayout()
            tv_profile_mission_price.requestLayout()
            activity.setFixedImageSize(400, 400)
            iv_profile_my_photo.transitionName = TRANSITION_NAME_FOR_IMAGE + position

            val url = item.url_z ?: CommonUtils.getFlickrPhotoURL(item.server!!, item.id, item.secret!!)
            setRandomBackground(iv_profile_my_photo)

            activity.setImageDraw(iv_profile_my_photo, constraint_profile_photos, url, true)
            val options = RequestOptions.placeholderOf(R.drawable.ic_placeholder_image)
            Glide.with(activity).load(url).apply(options).into(iv_profile_my_photo)

            tv_profile_mission_price.text = ""

            iv_play_btn.visibility = when (item.media) {
                activity.getString(R.string.media_type_video) -> View.VISIBLE
                activity.getString(R.string.media_type_photo) -> View.GONE
                else -> View.GONE
            }

            iv_profile_my_photo.setOnClickListener {
                Caller.callPhotoInfo(
                    activity,
                    iv_profile_my_photo,
                    item.id,
                    item.owner!!,
                    holder.adapterPosition,
                    SELECTED_FEED_ITEM_POSITION,
                    CALLED_FROM_OTHERS_PROFILE,
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
