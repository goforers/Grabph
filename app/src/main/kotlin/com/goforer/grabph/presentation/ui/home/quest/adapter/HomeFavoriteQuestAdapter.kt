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

package com.goforer.grabph.presentation.ui.home.quest.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_BOLD
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_MEDIUM
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_REGULAR
import com.goforer.base.presentation.view.helper.ItemTouchHelperListener
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
import kotlinx.android.synthetic.main.recycler_view_container.*
import com.goforer.grabph.presentation.ui.home.quest.fragment.HomeQuestFragment
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.snap_quest_favorite_item.*

class HomeFavoriteQuestAdapter(private val fragment: HomeQuestFragment): PagedListAdapter<Quest, HomeFavoriteQuestAdapter.HomeMissionViewHolder>(DIFF_CALLBACK),
    ItemTouchHelperListener {
    companion object {
        private const val PHOTO_RATIO_WIDTH = 328
        private const val PHOTO_RATIO_HEIGHT = 216

        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Quest>() {
            override fun areItemsTheSame(oldQuest: Quest, newQuest: Quest): Boolean =
                oldQuest.id == newQuest.id

            override fun areContentsTheSame(oldQuest: Quest, newQuest: Quest): Boolean =
                oldQuest == newQuest

            override fun getChangePayload(oldQuest: Quest, newQuest: Quest): Any? {
                return if (sameExceptTitle(oldQuest, newQuest)) {
                    PAYLOAD_TITLE
                } else {
                    null
                }
            }
        }

        private fun sameExceptTitle(oldQuest: Quest, newQuest: Quest): Boolean {
            return oldQuest.copy(title = newQuest.title) == newQuest
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeMissionViewHolder {
        val view = LayoutInflater.from(parent.context.applicationContext)
            .inflate(R.layout.snap_quest_favorite_item, parent, false)

        return HomeMissionViewHolder(view, fragment)
    }

    override fun onBindViewHolder(holder: HomeMissionViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return true
    }

    override fun onItemDismiss(position: Int) {
    }

    override fun onItemDrag(actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            fragment.swipe_layout.isRefreshing = false
            fragment.swipe_layout.isEnabled = false
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            fragment.swipe_layout.isEnabled = true
        }
    }

    class HomeMissionViewHolder(override val containerView: View, private val fragment: HomeQuestFragment): BaseViewHolder<Quest>(containerView), LayoutContainer {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Quest, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            var isPlayerVisible = false
            iv_play_btn_favorite_quest_item.requestLayout()
            iv_quest_item_content.requestLayout()
            tv_quest_owner_name.requestLayout()
            iv_quest_reward.requestLayout()
            tv_quest_reward_price.requestLayout()
            tv_quest_title.requestLayout()
            tv_quest_duration.requestLayout()
            tv_quest_description.requestLayout()

            iv_quest_item_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
            tv_quest_title.transitionName = TransitionObject.TRANSITION_NAME_FOR_TITLE + position
            iv_quest_owner_logo.transitionName = TransitionObject.TRANSITION_NAME_FOR_LOGO + position
            tv_quest_description.transitionName = TransitionObject.TRANSITION_NAME_FOR_EXPLANATION + position
            tv_quest_owner_name.transitionName = TransitionObject.TRANSITION_NAME_FOR_OWNER_NAME + position

            fragment.homeActivity.setImageDraw(iv_quest_owner_logo, item.ownerLogo)
            fragment.homeActivity.setFixedImageSize(PHOTO_RATIO_HEIGHT, PHOTO_RATIO_WIDTH)
            fragment.homeActivity.setImageDraw(iv_quest_item_content, quest_image_Container, item.ownerImage, false)
            quest_item_holder.visibility = View.VISIBLE
            card_quest_holder.visibility = View.VISIBLE

            @MockData
            when (position) {
                0, 2, 9 -> {
                    iv_play_btn_favorite_quest_item.visibility = View.VISIBLE
                    isPlayerVisible = true
                }
                else -> {
                    iv_play_btn_favorite_quest_item.visibility = View.GONE
                    isPlayerVisible = false
                }
            }

            iv_quest_item_content.setOnClickListener {
                // iv_quest_item_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
                // tv_quest_title.transitionName = TransitionObject.TRANSITION_NAME_FOR_TITLE + position
                // iv_quest_owner_logo.transitionName = TransitionObject.TRANSITION_NAME_FOR_LOGO + position
                // tv_quest_description.transitionName = TransitionObject.TRANSITION_NAME_FOR_EXPLANATION + position
                // tv_quest_owner_name.transitionName = TransitionObject.TRANSITION_NAME_FOR_OWNER_NAME + position
                Caller.callQuestInfo(fragment, iv_quest_item_content, iv_quest_owner_logo,
                    tv_quest_title,  tv_quest_description, tv_quest_owner_name,
                    item, holder.adapterPosition, Caller.CALLED_FORM_HOME_FAVORITE_QUEST,
                    Caller.SELECTED_QUEST_INFO_ITEM_POSITION, isPlayerVisible)
            }

            tv_quest_owner_name.text = item.ownerName
            setFontTypeface()

            if (item.title == "" || item.title == " ") {
                tv_quest_title.visibility = View.VISIBLE
                tv_quest_title.text = fragment.getString(R.string.no_title)
            } else {
                tv_quest_title.visibility = View.VISIBLE
                tv_quest_title.text = item.title
            }

            tv_quest_reward_price.text = item.rewards
            tv_quest_duration.text = (fragment.getString(R.string.snap_quest_duration_day_phrase) + item.duration)
            tv_quest_description.text = item.description
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }

        private fun setFontTypeface() {
            fragment.homeActivity.setFontTypeface(tv_quest_title, FONT_TYPE_BOLD)
            fragment.homeActivity.setFontTypeface(tv_quest_description, FONT_TYPE_REGULAR)
            fragment.homeActivity.setFontTypeface(tv_quest_reward_price, FONT_TYPE_MEDIUM)
            fragment.homeActivity.setFontTypeface(tv_quest_duration, FONT_TYPE_MEDIUM)
        }
    }
}