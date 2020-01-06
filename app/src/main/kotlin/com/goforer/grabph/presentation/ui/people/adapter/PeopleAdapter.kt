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

package com.goforer.grabph.presentation.ui.people.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_REGULAR
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.PEOPLE_RANK_BEGINNER
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.PEOPLE_RANK_1
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.PEOPLE_RANK_2
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.PEOPLE_RANK_3
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.PEOPLE_RANK_4
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.PEOPLE_RANK_EXPERT
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.ui.people.PeopleActivity
import com.goforer.grabph.presentation.ui.people.PeopleActivity.Companion.LAYOUT_SEARPER
import com.goforer.grabph.presentation.ui.people.PeopleActivity.Companion.LAYOUT_SEARPLE
import com.goforer.grabph.presentation.ui.people.PeopleActivity.Companion.LAYOUT_SELL
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Searper
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_people_item.*

class PeopleAdapter(private val activity: PeopleActivity,
                    private val peopleCode: Int): PagedListAdapter<Searper, PeopleAdapter.PeopleViewHolder>(DIFF_CALLBACK) {

    private val followStat by lazy { ArrayList<Boolean>() }

    companion object {
        private val DIFF_CALLBACK = object:DiffUtil.ItemCallback<Searper>() {
            override fun areItemsTheSame(oldItem: Searper, newItem: Searper): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Searper, newItem: Searper): Boolean = oldItem == newItem
        }
        private const val PHOTO_RATIO = 47
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.list_people_item, parent, false)

        return PeopleViewHolder(view, activity, peopleCode, followStat)
    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        val item = getItem(position)

        item?.let { holder.bindItemHolder(holder, it, position) }
    }

    internal fun setFollowListStatus(pagedList: PagedList<Searper>) {
        followStat.clear()
        followStat.addAll(pagedList.toList().map { searper -> searper.doFollow })
    }

    class PeopleViewHolder(override val containerView: View, private val activity: PeopleActivity,
                           private val peopleCode: Int, private val followStat: ArrayList<Boolean>): BaseViewHolder<Searper>(containerView), LayoutContainer {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Searper, position: Int) {
            tv_person_item_username.text = item.name

            when (peopleCode) {
                LAYOUT_SEARPER, LAYOUT_SEARPLE -> {
                    if (followStat[position]) {
                        tv_person_follow_status.setTextColor(ContextCompat.getColor(context, R.color.colorHomeMainQuestInfoText))
                        tv_person_follow_status.setBackgroundResource(R.drawable.border_of_button_filled_grey)
                        tv_person_follow_status.text = context.getString(R.string.status_following)
                    } else {
                        tv_person_follow_status.setTextColor(ContextCompat.getColor(context, R.color.white))
                        tv_person_follow_status.setBackgroundResource(R.drawable.border_of_button_grey)
                        tv_person_follow_status.text = context.getString(R.string.status_follow)
                    }
                }
                LAYOUT_SELL -> {
                    tv_person_follow_status.visibility = View.INVISIBLE
                }
            }

            when (item.rank) {
                PEOPLE_RANK_BEGINNER -> people_profile_image_holder.setBackgroundResource(R.drawable.border_rounded_rank_yellow)
                PEOPLE_RANK_1 -> people_profile_image_holder.setBackgroundResource(R.drawable.border_rounded_rank_blue)
                PEOPLE_RANK_2 -> people_profile_image_holder.setBackgroundResource(R.drawable.border_rounded_rank_orange)
                PEOPLE_RANK_3 -> people_profile_image_holder.setBackgroundResource(R.drawable.border_rounded_rank_purple)
                PEOPLE_RANK_4 -> people_profile_image_holder.setBackgroundResource(R.drawable.border_rounded_rank_red)
                PEOPLE_RANK_EXPERT -> people_profile_image_holder.setBackgroundResource(R.drawable.border_rounded_rank_gradient)
            }

            val imgUrl = item.profile_image.large

            activity.setFixedImageSize(PHOTO_RATIO, PHOTO_RATIO)
            activity.setImageDraw(iv_person_item_profile, people_profile_image_holder, imgUrl, false)
            setFontTypeFace()
            people_container.setOnClickListener {
                Caller.callOtherUserProfile(activity, Caller.CALLED_FROM_PEOPLE, item.id, item.name, item.rank, item.profile_image.large)
            }

            setFollowButtonListener(tv_person_follow_status, position)
        }

        override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

        override fun onItemClear() { containerView.setBackgroundColor(0) }

        private fun setFollowButtonListener(textView: TextView, position: Int) {

            textView.setOnClickListener { view ->

                val tv = view as TextView

                if (followStat[position]) {
                    followStat[position] = false
                    tv.setTextColor(ContextCompat.getColor(activity, R.color.white))
                    tv.setBackgroundResource(R.drawable.border_of_button_grey)
                    tv.text = activity.getString(R.string.status_follow)

                } else {
                    followStat[position] =true
                    tv.setTextColor(ContextCompat.getColor(activity, R.color.colorHomeMainQuestInfoText))
                    tv.setBackgroundResource(R.drawable.border_of_button_filled_grey)
                    tv.text = activity.getString(R.string.status_following)
                }
            }
        }

        private fun setFontTypeFace() {
            activity.setFontTypeface(tv_person_follow_status, FONT_TYPE_REGULAR)
            activity.setFontTypeface(tv_person_item_username, FONT_TYPE_REGULAR)
        }
    }
}