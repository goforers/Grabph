package com.goforer.grabph.presentation.ui.home.profile.adapter.photos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.LocalPin
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_PROFILE_MY_GALLERY
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_PROFILE_MY_PIN
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject.TRANSITION_NAME_FOR_IMAGE
import com.goforer.grabph.presentation.ui.home.HomeActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_profile_photos_item.*

class ProfilePinAdapter(private val activity: HomeActivity) : RecyclerView.Adapter<MyPinViewHolder>() {

    private val pins = ArrayList<LocalPin>()

    companion object {
        private const val STOP_LOADING_TIME_OUT = 50L

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<LocalPin>() {
            override fun areItemsTheSame(oldItem: LocalPin, newItem: LocalPin): Boolean = oldItem.photoId == newItem.photoId
            override fun areContentsTheSame(oldItem: LocalPin, newItem: LocalPin): Boolean = oldItem == newItem
        }
    }

    override fun getItemCount() = pins.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPinViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.list_profile_photos_item, parent, false)

        return MyPinViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: MyPinViewHolder, position: Int) {
        val item = pins[position]
        holder.bindItemHolder(holder, item, position)
    }

    internal fun addList(list: List<LocalPin>) {
        pins.clear()
        pins.addAll(list.asReversed())
        notifyDataSetChanged()
    }
}

class MyPinViewHolder(override val containerView: View, private val activity: HomeActivity): BaseViewHolder<LocalPin>(containerView), LayoutContainer {
    override fun bindItemHolder(holder: BaseViewHolder<*>, item: LocalPin, position: Int) {
        activity.setFontTypeface(tv_profile_mission_price, BaseActivity.FONT_TYPE_BOLD)
        iv_profile_my_photo.requestLayout()
        tv_profile_mission_price.requestLayout()
        activity.setFixedImageSize(400, 400) // original value: 0, 0

        val options = RequestOptions.placeholderOf(R.drawable.ic_placeholder_image)
        Glide.with(activity).load(item.url).apply(options).into(iv_profile_my_photo)

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
                item.photoId,
                item.authorId,
                holder.adapterPosition,
                CALLED_FROM_HOME_PROFILE_MY_GALLERY,
                CALLED_FROM_HOME_PROFILE_MY_PIN,
                item.url
            )
        }
    }

    override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

    override fun onItemClear() { containerView.setBackgroundColor(0) }
}