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

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_MEDIUM
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_REGULAR
import com.goforer.base.presentation.view.customs.imageview.ThreeTwoImageView
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_MAIN_QUEST
import com.goforer.grabph.presentation.caller.Caller.SELECTED_QUEST_INFO_ITEM_FROM_HOME_MAIN_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.snap_common_quest_item.*

class QuestAdapter(private val activity: HomeActivity): PagedListAdapter<Quest, QuestAdapter.QuestViewHolder>(DIFF_CALLBACK) {
    companion object {
        private const val PHOTO_RATIO_WIDTH = 290
        private const val PHOTO_RATIO_HEIGHT = 166

        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK
            = object: DiffUtil.ItemCallback<Quest>() {
            override fun areItemsTheSame(oldQuest: Quest,
                newQuest: Quest): Boolean = oldQuest.id == newQuest.id

            override fun areContentsTheSame(oldQuest: Quest,
                newQuest: Quest): Boolean = oldQuest == newQuest

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.snap_common_quest_item, parent, false)

        return QuestViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    class QuestViewHolder(override val containerView: View, private val activity: HomeActivity): BaseViewHolder<Quest>(containerView), LayoutContainer {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Quest, position: Int) {
            setFontTypeface()
            iv_snap_quest_content.requestLayout()
            tv_snap_quest_duration.requestLayout()
            tv_snap_quest_title.requestLayout()
            tv_snap_quest_title.requestLayout()
            tv_snap_quest_reward_price.requestLayout()
            tv_snap_quest_photos.requestLayout()
            // activity.setImageDraw(iv_snap_quest_owner_logo, item.ownerLogo)
            snap_quest_item_holder.visibility = View.VISIBLE
            activity.setFixedImageSize(PHOTO_RATIO_HEIGHT, PHOTO_RATIO_WIDTH)
            activity.setImageDraw(iv_snap_quest_content, item.ownerImage)
            snap_quest_item_holder.visibility = View.VISIBLE
            card_quest_holder.visibility = View.VISIBLE
            iv_snap_quest_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
            iv_snap_quest_content.setOnClickListener {
                callQuestInfo(iv_snap_quest_content, item, holder)
            }

            card_quest_holder.setOnClickListener {
                callQuestInfo(iv_snap_quest_content, item, holder)
            }

            tv_snap_quest_duration.text = (activity.getString(R.string.snap_quest_duration_day_phrase) + item.duration)
            // tv_snap_quest_owner_name.text = item.ownerName
            tv_snap_quest_title.text = item.title
            tv_snap_quest_reward_price.text = (activity.getString(R.string.currency_us_dollar) + " " + item.rewards)
            tv_snap_quest_photos.text = item.contents
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }

        private fun callQuestInfo(viewContent: ThreeTwoImageView, item: Quest, holder: BaseViewHolder<*>) {
            activity.closeFab()
            Caller.callQuestInfo(activity, viewContent,
                item, holder.adapterPosition, CALLED_FROM_HOME_MAIN_QUEST,
                SELECTED_QUEST_INFO_ITEM_FROM_HOME_MAIN_POSITION)
        }

        private fun setFontTypeface() {
            activity.setFontTypeface(tv_snap_quest_title, FONT_TYPE_REGULAR)
            // activity.setFontTypeface(tv_snap_quest_owner_name, FONT_TYPE_MEDIUM)
            activity.setFontTypeface(tv_snap_quest_duration, FONT_TYPE_MEDIUM)
            activity.setFontTypeface(tv_snap_quest_reward_price, FONT_TYPE_MEDIUM)
        }
    }
}