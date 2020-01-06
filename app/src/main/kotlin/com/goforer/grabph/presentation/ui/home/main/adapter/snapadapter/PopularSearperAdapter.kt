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
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_MAIN
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.data.datasource.model.cache.data.entity.home.Home
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.snap_main_popular_searper_item.*

class PopularSearperAdapter(private val activity: HomeActivity, val workHandler: CommonWorkHandler)
                        : PagedListAdapter<Home.PopSearper.Searper, PopularSearperAdapter.SearperViewHolder>(DIFF_CALLBACK) {
    companion object {
        private const val RATIO_WIDTH = 54
        private const val RATIO_HEIGHT = 54

        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK
                                    = object : DiffUtil.ItemCallback<Home.PopSearper.Searper>() {
            override fun areItemsTheSame(oldSearpler: Home.PopSearper.Searper,
                                         newSearpler: Home.PopSearper.Searper): Boolean =
                    oldSearpler.id == newSearpler.id

            override fun areContentsTheSame(oldSearpler: Home.PopSearper.Searper,
                                            newSearpler: Home.PopSearper.Searper): Boolean =
                    oldSearpler == newSearpler

            override fun getChangePayload(oldSearpler: Home.PopSearper.Searper,
                                          newSearpler: Home.PopSearper.Searper): Any? {
                return if (sameExceptTitle(oldSearpler, newSearpler)) {
                    PAYLOAD_TITLE
                } else {
                    null
                }
            }
        }

        private fun sameExceptTitle(oldSearpler: Home.PopSearper.Searper,
                                    newSearpler: Home.PopSearper.Searper): Boolean {
            return oldSearpler.copy(username = newSearpler.username) == newSearpler
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearperViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.snap_main_popular_searper_item, parent, false)

        return SearperViewHolder(view, activity, workHandler)
    }

    override fun onBindViewHolder(holder: SearperViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    class SearperViewHolder(override val containerView: View, private val activity: HomeActivity,
                            val workHandler: CommonWorkHandler): BaseViewHolder<Home.PopSearper.Searper>(containerView), LayoutContainer {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Home.PopSearper.Searper, position: Int) {
            val searperPhotoUrl = workHandler.getProfilePhotoURL(item.iconfarm, item.iconserver, item.id)

            iv_snap_searper_picture.requestLayout()
            when(item.grade.toInt()) {
                Home.GRADE_BEGINNER -> snap_searper_item_holder.setBackgroundResource(R.drawable.border_rounded_rank_yellow)
                Home.GRADE_1 -> snap_searper_item_holder.setBackgroundResource(R.drawable.border_rounded_rank_blue)
                Home.GRADE_2 -> snap_searper_item_holder.setBackgroundResource(R.drawable.border_rounded_rank_orange)
                Home.GRADE_3 -> snap_searper_item_holder.setBackgroundResource(R.drawable.border_rounded_rank_purple)
                Home.GRADE_4 -> snap_searper_item_holder.setBackgroundResource(R.drawable.border_rounded_rank_red)
                Home.GRADE_EXPERT -> snap_searper_item_holder.setBackgroundResource(R.drawable.border_rounded_rank_gradient)
                else -> snap_searper_item_holder.setBackgroundResource(R.drawable.border_rounded_grade_etc)
            }

            activity.setFixedImageSize(RATIO_HEIGHT, RATIO_WIDTH)
            activity.setImageDraw(iv_snap_searper_picture, snap_searper_constraintLayoutContainer,
                    searperPhotoUrl, false)
            iv_snap_searper_picture.setOnClickListener { // move to OthersProfileActivity
                activity.closeFab()

                // Caller.callPhotogPhoto(context.applicationContext, getUserName(holder.itemView, item.username!!),
                //         item.iconfarm, item.iconserver, item.id,
                //         FIRST_PAGE, PHOTOG_PHOTO_POPULAR_TYPE)

                Caller.callOtherUserProfile(
                    activity,
                    CALLED_FROM_HOME_MAIN,
                    item.id,
                    getUserName(holder.itemView, item.username!!),
                    item.grade.toInt(),
                    workHandler.getProfilePhotoURL(item.iconfarm, item.iconserver, item.id))
            }
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }

        private fun getUserName(view: View, userName: Home.PopSearper.Username): String {
            userName._content?.let {
                return userName._content
            }

            return view.resources.getString(R.string.no_username)
        }
    }
}