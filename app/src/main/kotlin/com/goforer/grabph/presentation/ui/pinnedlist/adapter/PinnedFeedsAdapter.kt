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

package com.goforer.grabph.presentation.ui.pinnedlist.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.NOTO_SANS_KR_MEDIUM
import com.goforer.base.presentation.view.customs.imageview.ThreeTwoImageView
import com.goforer.base.presentation.view.helper.ItemTouchHelperListener
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.common.menu.MenuHandler
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.presentation.ui.pinnedlist.fragment.PinnedFeedsFragment
import com.goforer.grabph.repository.model.cache.data.entity.feed.FeedItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_photo_item.*
import kotlinx.android.synthetic.main.recycler_view_container.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PinnedFeedsAdapter(private val fragment: PinnedFeedsFragment, private var waterMarkHandler: WatermarkHandler,
                         private val workHandler: CommonWorkHandler): PagedListAdapter<FeedItem, PinnedFeedsAdapter.PinnedupViewHolder>(DIFF_CALLBACK),
                                                                      ItemTouchHelperListener {
    private var currentPinnedList: PagedList<FeedItem>? = null

    companion object {
        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<FeedItem>() {
            override fun areItemsTheSame(oldFeedItem: FeedItem, newFeedItem: FeedItem): Boolean =
                    oldFeedItem.media.m ==  newFeedItem.media.m

            override fun areContentsTheSame(oldFeedItem: FeedItem, newFeedItem: FeedItem): Boolean =
                    oldFeedItem ==  newFeedItem

            override fun getChangePayload(oldFeedItem: FeedItem, newFeedItem: FeedItem): Any? {
                return if (sameExceptTitle(oldFeedItem, newFeedItem)) {
                    PAYLOAD_TITLE
                } else {
                    null
                }
            }
        }

        private fun sameExceptTitle(oldFeedItem: FeedItem, newFeedItem: FeedItem): Boolean {
            return oldFeedItem.copy(title = newFeedItem.title) == newFeedItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinnedupViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.list_photo_item, parent, false)

        return PinnedupViewHolder(view, fragment, waterMarkHandler, workHandler)
    }

    override fun onBindViewHolder(holder: PinnedupViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<FeedItem>?, currentList: PagedList<FeedItem>?) {
        currentPinnedList = currentList
        CommonUtils.betterSmoothScrollToPosition(fragment.recycler_view, 0)

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
        bePinned(currentPinnedList?.get(position)!!, position)
    }

    private fun bePinned(feedItem: FeedItem, position: Int) {
        val isPinned = feedItem.isPinned

        feedItem.isPinned = !isPinned
        updatePinnedState(feedItem, position)
    }

    private fun updatePinnedState(feedItem: FeedItem, position: Int) {
        feedItem.isPinned = false
        feedItem.pinnedDate = -2
        fragment.isUpdateNotified = true
        fragment.feedViewModel.updateFeedItem(feedItem)
    }

    class PinnedupViewHolder(override val containerView: View, private val fragment: PinnedFeedsFragment,
                             private var waterMarkHandler: WatermarkHandler, private val workHandler: CommonWorkHandler): BaseViewHolder<FeedItem>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: FeedItem, position: Int) {
            val photoPath = item.media.m?.substring(0, item.media.m!!.indexOf("_m")) + ".jpg"

            // In case of applying transition effect to views, have to use findViewById method
            iv_photo_item_content.requestLayout()
            tv_photo_item_title.requestLayout()
            ib_viewer.requestLayout()
            ib_share.requestLayout()
            item.searperPhotoUrl?.let {
                if (item.searperPhotoUrl?.contains("/0/")!!) {
                    iv_searper_pic.setImageDrawable(fragment.context.applicationContext
                            .getDrawable(R.drawable.ic_default_profile))
                } else {
                    fragment.pinnedFeedsActivity.setImageDraw(iv_searper_pic, item.searperPhotoUrl!!)
                }
            }

            item.searperPhotoUrl ?: iv_searper_pic.setImageDrawable(fragment.context.applicationContext
                    .getDrawable(R.drawable.ic_default_profile))
            tv_searper_name.text = item.searperName
            tv_searper_posted_date.text = "Posted at " + item.publishedDate?.substring(0, 10)
            fragment.pinnedFeedsActivity.setFixedImageSize(0, 0)
            fragment.pinnedFeedsActivity.setImageDraw(iv_photo_item_content, pinnedConstraintLayoutContainer, photoPath, false)
            photo_item_holder.visibility = View.VISIBLE
            card_holder.visibility = View.VISIBLE
            iv_photo_item_content.setOnClickListener {
                val link = getLink(item.link.toString())
                val photoId = link.substring(link.lastIndexOf("/") + 1)

                iv_photo_item_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
                tv_photo_item_title.transitionName = TransitionObject.TRANSITION_NAME_FOR_TITLE + position
                Caller.callFeedInfo(fragment.pinnedFeedsActivity, iv_photo_item_content,
                        tv_photo_item_title, item.idx, holder.adapterPosition,
                        item.authorId, photoId, Caller.CALLED_FROM_PINNED_FEEDS, Caller.SELECTED_PINNED_FEED_ITEM_POSITION)
            }

            if (item.title == "" || item.title == " ") {
                tv_photo_item_title.visibility = View.VISIBLE
                tv_photo_item_title.text = fragment.getString(R.string.no_title)
            } else {
                tv_photo_item_title.visibility = View.VISIBLE
                tv_photo_item_title.text = item.title
            }

            val drawable: Drawable = when (item.like) {
                0 -> {
                    ContextCompat.getDrawable(fragment.context.applicationContext, R.drawable.ic_fab_normal)!!
                }
                1 -> {
                    ContextCompat.getDrawable(fragment.context.applicationContext, R.drawable.ic_fab_good)!!
                }
                2 -> {
                    ContextCompat.getDrawable(fragment.context.applicationContext, R.drawable.ic_fab_bad)!!
                }
                else -> {
                    ContextCompat.getDrawable(fragment.context.applicationContext, R.drawable.ic_fab_normal)!!
                }
            }

            ib_like.setImageDrawable(drawable)
            ib_like.backgroundTintList = when (item.like) {
                0 -> {
                    ColorStateList.valueOf(Color.WHITE)
                }
                1 -> {
                    ColorStateList.valueOf(Color.RED)
                }
                2 -> {
                    ColorStateList.valueOf(Color.BLUE)
                }
                else -> {
                    ColorStateList.valueOf(Color.WHITE)
                }
            }

            ib_like.setOnClickListener { view -> doLike(view as AppCompatImageButton, item) }
            ib_viewer.setOnClickListener {
                Caller.callViewer(fragment.pinnedFeedsActivity, iv_photo_item_content, position,
                        Caller.CALLED_FROM_PINNED_FEED, (iv_photo_item_content.drawable as BitmapDrawable).bitmap,
                        item.media.m.toString(), Caller.SELECTED_PINNED_INFO_PHOTO_VIEW) }
            ib_share.setOnClickListener { doShare(ib_share, iv_photo_item_content, item) }

            if (fragment.swipe_layout.isRefreshing) {
                fragment.stopMoreLoading(300)
            }
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }

        private fun getLink(link: String): String {
            return GeneralFunctions.removeLastCharRegex(link).toString()
        }

        private fun callShareToFacebook(drawable: BitmapDrawable, caption: String) {
            workHandler.shareToFacebook(drawable.bitmap, fragment)
        }

        private fun doShare(view: View, photoView: ThreeTwoImageView, feedItem: FeedItem) {
            val wrapper = ContextThemeWrapper(fragment.pinnedFeedsActivity, R.style.PopupMenu)
            val popup = PopupMenu(wrapper, view, Gravity.CENTER)

            popup.menuInflater.inflate(R.menu.menu_share_popup, popup.menu)
            MenuHandler().applyFontToMenuItem(popup,
                    Typeface.createFromAsset(fragment.pinnedFeedsActivity.applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                    fragment.pinnedFeedsActivity.resources.getColor(R.color.colorHomeQuestFavoriteKeyword, fragment.pinnedFeedsActivity.theme))
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_share_facebook -> callShareToFacebook(
                            photoView.drawable as BitmapDrawable,
                            fragment.getString(R.string.phrase_title) + "\n\n" +
                                    item.title + "\n\n" +
                                    fragment.getString(R.string.phrase_description) + "\n\n")
                    R.id.menu_share_ect -> waterMarkHandler.putWatermark(fragment.context.applicationContext,
                            workHandler, (photoView.drawable as BitmapDrawable).bitmap, feedItem.title, "")
                    else -> {
                    }
                }

                true
            }

            popup.show()
        }

        private fun doLike(view: View, feedItem: FeedItem) {
            val wrapper = ContextThemeWrapper(fragment.pinnedFeedsActivity, R.style.PopupMenu)
            val popup = PopupMenu(wrapper, view, Gravity.CENTER)

            popup.menuInflater.inflate(R.menu.menu_like_popup, popup.menu)
            MenuHandler().applyFontToMenuItem(popup,
                    Typeface.createFromAsset(fragment.pinnedFeedsActivity.applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                    fragment.pinnedFeedsActivity.resources.getColor(R.color.colorHomeQuestFavoriteKeyword, fragment.pinnedFeedsActivity.theme))
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_like_good -> {
                        (view as AppCompatImageButton).setImageResource(R.drawable.ic_fab_good)
                        view.backgroundTintList = ColorStateList.valueOf(Color.RED)
                        feedItem.like = 1
                    }
                    R.id.menu_like_normal -> {
                        (view as AppCompatImageButton).setImageResource(R.drawable.ic_fab_normal)
                        view.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        feedItem.like = 0
                    }
                    R.id.menu_like_bad -> {
                        (view as AppCompatImageButton).setImageResource(R.drawable.ic_fab_bad)
                        view.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
                        feedItem.like = 2
                    }
                    else -> {
                    }
                }

                true
            }

            val menuHelper: Any
            val argTypes: Array<Class<*>>
            try {
                val fMenuHelper= PopupMenu::class.java.getDeclaredField("mPopup")
                fMenuHelper.isAccessible = true
                menuHelper=fMenuHelper.get(popup)
                argTypes=arrayOf(Boolean::class.javaPrimitiveType!!)
                menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes).invoke(menuHelper, true)
            } catch (e: Exception) {
                // Possible exceptions are NoSuchMethodError and NoSuchFieldError
                //
                // In either case, an exception indicates something is wrong with the reflection code, or the
                // structure of the PopupMenu class or its dependencies has changed.
                //
                // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
                // but in the case that they do, we simply can't force icons to display, so log the error and
                // show the menu normally.
            }

            popup.show()
        }
    }
}