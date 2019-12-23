package com.goforer.grabph.presentation.ui.home.profile.adapter.photos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_PROFILE
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_PROFILE_MY_GALLERY
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_PROFILE_MY_PIN
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject.TRANSITION_NAME_FOR_IMAGE
import com.goforer.grabph.presentation.ui.home.HomeActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_profile_photos_item.*

class ProfilePinAdapter(private val activity: HomeActivity)
    : PagedListAdapter<Photo, MyPinViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val STOP_LOADING_TIME_OUT = 50L

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPinViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.list_profile_photos_item, parent, false)

        return MyPinViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: MyPinViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bindItemHolder(holder, it, position) }
    }
}


class MyPinViewHolder(override val containerView: View, private val activity: HomeActivity): BaseViewHolder<Photo>(containerView), LayoutContainer {
    override fun bindItemHolder(holder: BaseViewHolder<*>, item: Photo, position: Int) {
        activity.setFontTypeface(tv_profile_mission_price, BaseActivity.FONT_TYPE_BOLD)
        iv_profile_my_photo.requestLayout()
        tv_profile_mission_price.requestLayout()
        activity.setFixedImageSize(400, 400) // original value: 0, 0

        val url = CommonUtils.getFlickrPhotoURL(item.server!!, item.id, item.secret!!)
        // activity.setImageDraw(iv_profile_my_photo, url)

        val options = RequestOptions.placeholderOf(R.drawable.ic_imgbg)
        Glide.with(activity).load(url).apply(options).into(iv_profile_my_photo)

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
                CALLED_FROM_HOME_PROFILE_MY_GALLERY,
                CALLED_FROM_HOME_PROFILE_MY_PIN,
                url
            )
        }
    }

    override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

    override fun onItemClear() { containerView.setBackgroundColor(0) }
}