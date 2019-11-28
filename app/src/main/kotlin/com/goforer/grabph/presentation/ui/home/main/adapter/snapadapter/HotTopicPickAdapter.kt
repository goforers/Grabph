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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_BOLD
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_MEDIUM
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_CATEGORY_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_HOT_TOPIC_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_SEARPER_PHOTO_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.data.datasource.model.cache.data.entity.category.Category
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.data.datasource.model.cache.data.entity.home.Home.HotTopic
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.snap_main_hottopic_category_item.*
import kotlinx.android.synthetic.main.snap_main_hottopic_hot_photo.*
import kotlinx.android.synthetic.main.snap_main_hottopic_quest_item.*
import kotlinx.android.synthetic.main.snap_main_hottopic_searper_photo.*

class HotTopicPickAdapter(private val activity: HomeActivity): RecyclerView.Adapter<HotTopicPickAdapter.ViewHolder>() {
    private var item: List<Any> = ArrayList()

    companion object {
        private const val RATIO_WIDTH = 360
        private const val RATIO_HEIGHT = 440

        const val BEST_HOT_TOPIC_TYPE = 0
        const val BEST_QUEST_TYPE = 1
        const val BEST_SEARPER_PHOTO_TYPE = 2
        const val BEST_CATEGORY_TYPE = 3
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when(viewType) {
            BEST_HOT_TOPIC_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.snap_main_hottopic_hot_photo, parent, false)

                return ViewHolder( view, activity)
            }

            BEST_QUEST_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.snap_main_hottopic_quest_item, parent, false)

                return ViewHolder(view, activity)
            }

            BEST_SEARPER_PHOTO_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.snap_main_hottopic_searper_photo, parent, false)

                return ViewHolder(view, activity)
            }

            BEST_CATEGORY_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.snap_main_hottopic_category_item, parent, false)

                return ViewHolder(view, activity)
            }

            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.snap_main_hottopic_hot_photo, parent, false)

                return ViewHolder(view, activity)
            }
        }
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder.itemViewType) {
            BEST_HOT_TOPIC_TYPE -> {
                holder.bindItemHolder(holder, item[BEST_HOT_TOPIC_TYPE], position)
            }

            BEST_QUEST_TYPE -> {
                holder.bindItemHolder(holder, item[BEST_QUEST_TYPE], position)
            }

            BEST_SEARPER_PHOTO_TYPE -> {
                holder.bindItemHolder(holder, item[BEST_SEARPER_PHOTO_TYPE], position)
            }

            BEST_CATEGORY_TYPE -> {
                holder.bindItemHolder(holder, item[BEST_CATEGORY_TYPE], position)
            }
        }
    }

    fun addItem(item: List<Any>) {
        this.item = item
    }

    class ViewHolder(override val containerView: View, private val activity: HomeActivity): BaseViewHolder<Any>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        @Suppress("UNCHECKED_CAST")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Any, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            when(position) {
                BEST_HOT_TOPIC_TYPE -> {
                    val hotTopic = item as HotTopic

                    setHotPhotoFontTypeface(holder.itemView)
                    iv_snap_best_pick_photo_content.requestLayout()
                    tv_snap_best_pick_photo_title.requestLayout()
                    activity.setFixedImageSize(RATIO_HEIGHT, RATIO_WIDTH)
                    activity.setImageDraw(iv_snap_best_pick_photo_content,
                        snap_best_pick_hot_photo_constraintLayoutContainer,
                        hotTopic.media.urls.regular!!, false)
                    tv_snap_best_pick_photo_tip_phrase.text = activity.getString(R.string.snap_home_main_hot_topic_photo_phrase)
                    tv_snap_best_pick_photo_title.text = item.title
                    snap_best_pick_hot_photo_holder.visibility = View.VISIBLE
                    card_best_pick_hot_photo_holder.visibility = View.VISIBLE
                    iv_snap_best_pick_photo_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
                    iv_snap_best_pick_photo_content.setOnClickListener {
                        activity.closeFab()
                        Caller.callHotTopicContent(activity, iv_snap_best_pick_photo_content, hotTopic.id, SELECTED_BEST_PICK_HOT_TOPIC_POSITION)
                    }
                }

                BEST_QUEST_TYPE -> {
                    val mission = item as Quest

                    setQuestFontTypeface(holder.itemView)
                    iv_snap_best_pick_quest_content.requestLayout()
                    tv_snap_best_pick_quest_duration.requestLayout()
                    tv_snap_best_pick_quest_title.requestLayout()
                    tv_snap_best_pick_quest_item_title.requestLayout()
                    tv_snap_best_pick_quest_item_rewards.requestLayout()
                    activity.setFixedImageSize(RATIO_HEIGHT, RATIO_WIDTH)
                    activity.setImageDraw(iv_snap_best_pick_quest_content,
                        snap_best_pick_quest_constraintLayoutContainer,
                        mission.ownerImage, false)
                    tv_snap_best_pick_quest_duration.text = activity.getString(R.string.snap_quest_duration_day_phrase) + mission.duration.toString()
                    tv_snap_best_pick_quest_item_title.text = mission.title
                    tv_snap_best_pick_quest_item_rewards.text = mission.rewards
                    snap_best_pick_quest_item_holder.visibility = View.VISIBLE
                    card_best_pick_quest_holder.visibility = View.VISIBLE
                    iv_snap_best_pick_quest_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
                    iv_snap_best_pick_quest_content.setOnClickListener {
                        activity.closeFab()
                        Caller.callQuestInfo(activity, iv_snap_best_pick_quest_content,
                            mission, holder.adapterPosition, Caller.CALLED_FROM_HOME_BEST_PICK_QUEST,
                            Caller.SELECTED_BEST_PICK_QUEST_POSITION, false)
                    }
                }

                BEST_SEARPER_PHOTO_TYPE -> {
                    val photo = item as FeedItem
                    val iconfarm = 8
                    val iconserver = "7812"
                    val searperId = "165264480@N04"
                    val photoPath= "http://farm8.staticflickr.com/7812/buddyicons/165264480@N04.jpg"

                    setSearperPhotoFontTypeface()
                    iv_searper_pic.requestLayout()
                    iv_snap_best_pick_searper_photo_content.requestLayout()
                    tv_searper_name_text.requestLayout()
                    activity.setImageDraw(iv_searper_pic, photoPath)
                    tv_searper_name_text.text = "Ashlea Watchman"
                    iv_searper_pic.setOnClickListener {
                        activity.closeFab()
                        Caller.callPhotogPhoto(context.applicationContext, "Ashlea Watchman",
                            iconfarm, iconserver, searperId,
                            Caller.FIRST_PAGE, Caller.PHOTOG_PHOTO_GENERAL_TYPE)
                    }

                    tv_searper_name_text.setOnClickListener {
                        activity.closeFab()
                        Caller.callPhotogPhoto(context.applicationContext, "Ashlea Watchman",
                            iconfarm, iconserver, searperId,
                            Caller.FIRST_PAGE, Caller.PHOTOG_PHOTO_GENERAL_TYPE)
                    }

                    activity.setFixedImageSize(RATIO_HEIGHT, RATIO_WIDTH)
                    activity.setImageDraw(iv_snap_best_pick_searper_photo_content,
                        snap_best_pick_searper_photo_constraintLayoutContainer,
                        photo.media.m!!, true)
                    snap_best_pick_searper_photo_holder.visibility = View.VISIBLE
                    card_best_pick_searper_photo_holder.visibility = View.VISIBLE
                    iv_snap_best_pick_searper_photo_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
                    val link = GeneralFunctions.removeLastCharRegex(item.link).toString()
                    val photoId = link.substring(link.lastIndexOf("/") + 1)
                    iv_snap_best_pick_searper_photo_content.setOnClickListener {
                        activity.closeFab()
                        Caller.callFeedInfo(activity, iv_snap_best_pick_searper_photo_content,
                            item.idx, holder.adapterPosition, item.authorId, photoId,
                            CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO, SELECTED_BEST_PICK_SEARPER_PHOTO_POSITION)
                    }
                }

                BEST_CATEGORY_TYPE -> {
                    val category = item as Category

                    setCategoryFontTypeface()
                    iv_snap_best_pick_category_item_content.requestLayout()
                    tv_snap_best_pick_category_item_title.requestLayout()
                    activity.setFixedImageSize(RATIO_HEIGHT, RATIO_WIDTH)
                    activity.setImageDraw(iv_snap_best_pick_category_item_content,
                        snap_best_pick_category_constraintLayoutContainer,
                        category.photo?.m!!, false)
                    tv_snap_best_pick_category_item_title.text = item.title
                    snap_best_pick_category_item_holder.visibility = View.VISIBLE
                    card_snap_best_pick_category_holder.visibility = View.VISIBLE
                    iv_snap_best_pick_category_item_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
                    iv_snap_best_pick_category_item_content.setOnClickListener {
                        activity.closeFab()
                        Caller.callCategoryPhoto(activity, iv_snap_best_pick_category_item_content,
                            category.photo.m, category.id, category.title, Caller.FIRST_PAGE,
                            category.type, holder.adapterPosition, SELECTED_BEST_PICK_CATEGORY_POSITION)
                    }
                }
            }
        }

        override fun onItemSelected() {
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }

        private fun setHotPhotoFontTypeface(view: View) {
            with(view) {
                activity.setFontTypeface(tv_snap_best_pick_photo_title, FONT_TYPE_BOLD)
                activity.setFontTypeface(tv_snap_best_pick_photo_tip_phrase, FONT_TYPE_MEDIUM)
            }
        }

        private fun setQuestFontTypeface(view: View) {
            with(view) {
                activity.setFontTypeface(tv_snap_best_pick_quest_title, FONT_TYPE_BOLD)
                activity.setFontTypeface(tv_snap_best_pick_quest_item_title, FONT_TYPE_BOLD)
                activity.setFontTypeface(tv_snap_best_pick_quest_duration, FONT_TYPE_MEDIUM)
                activity.setFontTypeface(tv_snap_best_pick_quest_item_rewards, FONT_TYPE_MEDIUM)
            }
        }

        private fun setSearperPhotoFontTypeface() {
            activity.setFontTypeface(tv_searper_name_text, FONT_TYPE_MEDIUM)
        }

        private fun setCategoryFontTypeface() {
            activity.setFontTypeface(tv_snap_best_pick_category_item_title, FONT_TYPE_BOLD)
        }
    }
}