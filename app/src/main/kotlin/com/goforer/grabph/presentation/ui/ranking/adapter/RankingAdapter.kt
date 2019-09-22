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

package com.goforer.grabph.presentation.ui.ranking.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.ui.ranking.RankingActivity
import com.goforer.grabph.repository.model.cache.data.entity.ranking.Ranking
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_rank_bottom_item.*
import kotlinx.android.synthetic.main.list_rank_bottom_item.view.*
import kotlinx.android.synthetic.main.list_rank_top_item.*
import kotlinx.android.synthetic.main.list_rank_top_item.view.*

class RankingAdapter(private val activity: RankingActivity,
                     private val rankType: Int): RecyclerView.Adapter<BaseViewHolder<List<Ranking.Rank>>>(){
    private val rankings = ArrayList<Ranking.Rank>()
    private val topRanking = ArrayList<Ranking.Rank>()

    companion object {
        private const val RANK_TYPE_PHOTO = 1
        private const val RANK_TYPE_MONEY = 2
        private const val RANK_TYPE_LIKE = 3

        private const val VIEW_TYPE_TOP_RANK = 1
        private const val VIEW_TYPE_BOTTOM_RANK = 2

        private const val RANK_TOP_MIDDLE = 0
        private const val RANK_TOP_LEFT = 1
        private const val RANK_TOP_RIGHT = 2

        private const val PHOTO_RATIO_TOP = 54
        private const val PHOTO_RATIO_BOTTOM = 35
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_TOP_RANK else VIEW_TYPE_BOTTOM_RANK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<List<Ranking.Rank>> {
        val view = if (viewType == VIEW_TYPE_TOP_RANK) {
            LayoutInflater.from(activity).inflate(R.layout.list_rank_top_item, parent, false)
        } else {
            LayoutInflater.from(activity).inflate(R.layout.list_rank_bottom_item, parent, false)
        }

        return if (viewType == VIEW_TYPE_TOP_RANK) TopViewHolder(view, activity, rankType) else BottomViewHolder(view, activity, rankType)
    }

    override fun getItemCount(): Int = rankings.size

    override fun onBindViewHolder(holder: BaseViewHolder<List<Ranking.Rank>>, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_TOP_RANK) {
            (holder as TopViewHolder).bindItemHolder(holder, topRanking, position)
        } else {
            (holder as BottomViewHolder).bindItemHolder(holder, rankings, position)
        }
    }

    internal fun addList(ranking: List<Ranking.Rank>){
        rankings.clear()
        rankings.addAll(ranking)

        for (i in 1..3) {
            topRanking.add(rankings[0])
            if (i < 3) rankings.removeAt(0)
        }

        notifyDataSetChanged()
    }

    class TopViewHolder(override val containerView: View,
                        private val activity: RankingActivity,
                        private val rankType: Int): BaseViewHolder<List<Ranking.Rank>>(containerView), LayoutContainer {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: List<Ranking.Rank>, position: Int) {
            setTopRankViewByPosition(RANK_TOP_LEFT, rankType, item, tv_name_left, tv_point_left, iv_profile_top_left, holder_profile_top_left)
            setTopRankViewByPosition(RANK_TOP_MIDDLE, rankType, item, tv_name_middle, tv_point_middle, iv_profile_top_middle, holder_profile_top_middle)
            setTopRankViewByPosition(RANK_TOP_RIGHT, rankType, item, tv_name_right, tv_point_right, iv_profile_top_right, holder_profile_top_right)

            setFavoriteIconVisibility(rankType, ic_favorite_orange_left, ic_favorite_orange_middle, ic_favorite_orange_right)

            setTopRankTextType(containerView)

            constraint_holder_left.setOnClickListener {
                item[RANK_TOP_LEFT].let { activity.moveToOthersProfileActivity(it.id, it.name, it.rank_color, it.profile_image.large) }
            }
            constraint_holder_middle.setOnClickListener {
                item[RANK_TOP_MIDDLE].let { activity.moveToOthersProfileActivity(it.id, it.name, it.rank_color, it.profile_image.large) }
            }
            constraint_holder_right.setOnClickListener {
                item[RANK_TOP_RIGHT].let { activity.moveToOthersProfileActivity(it.id, it.name, it.rank_color, it.profile_image.large) }
            }
        }

        override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

        override fun onItemClear() { containerView.setBackgroundColor(0) }

        private fun setTopRankViewByPosition(position: Int, rankType: Int, item: List<Ranking.Rank>, tvName: TextView,
                                             tvPoint: TextView, ivProfile: AppCompatImageView, ivHolder: ConstraintLayout) {
            tvName.text = item[position].name

            val point = item[position].point
            tvPoint.text = if (rankType == RANK_TYPE_MONEY) "$$point" else "$point"

            val profileUrl = item[position].profile_image.large
            activity.setFixedImageSize(PHOTO_RATIO_TOP, PHOTO_RATIO_TOP)
            activity.setImageDraw(ivProfile, ivHolder, profileUrl, false)

            activity.setRankColor(item[position].rank_color, ivHolder, VIEW_TYPE_TOP_RANK)
        }

        private fun setFavoriteIconVisibility(rankType: Int, favoriteLeft: ImageView, favoriteMiddle: ImageView, favoriteRight: ImageView) {
            if (rankType == RANK_TYPE_LIKE) {
                favoriteLeft.visibility = View.VISIBLE
                favoriteMiddle.visibility = View.VISIBLE
                favoriteRight.visibility = View.VISIBLE
            } else {
                favoriteLeft.visibility = View.INVISIBLE
                favoriteMiddle.visibility = View.INVISIBLE
                favoriteRight.visibility = View.INVISIBLE
            }
        }

        private fun setTopRankTextType(view: View) {
            BaseActivity.FONT_TYPE_MEDIUM.run {
                activity.setFontTypeface(view.tv_rank_text_left, this)
                activity.setFontTypeface(view.tv_rank_text_right, this)

                activity.setFontTypeface(view.tv_name_left, this)
                activity.setFontTypeface(view.tv_name_middle, this)
                activity.setFontTypeface(view.tv_name_right, this)

                activity.setFontTypeface(view.tv_point_left, this)
                activity.setFontTypeface(view.tv_point_middle, this)
                activity.setFontTypeface(view.tv_point_right, this)
            }
        }
    }

    class BottomViewHolder(override val containerView: View,
                           private val activity: RankingActivity,
                           private val rankType: Int): BaseViewHolder<List<Ranking.Rank>>(containerView), LayoutContainer {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: List<Ranking.Rank>, position: Int) {
            tv_text_rank.text = "${item[position].rank}"
            tv_name_bottom_ranker.text = item[position].name

            val point = item[position].point
            tv_point_bottom_ranker.text = if (rankType == RANK_TYPE_MONEY) "$$point" else "$point"

            val profileUrl = item[position].profile_image.medium
            activity.setFixedImageSize(PHOTO_RATIO_BOTTOM, PHOTO_RATIO_BOTTOM)
            activity.setImageDraw(iv_profile_bottom_ranker, holder_image_bottom_ranker, profileUrl, false)

            activity.setRankColor(item[position].rank_color, holder_image_bottom_ranker, VIEW_TYPE_BOTTOM_RANK)

            setFavoriteIconVisibility(rankType, ic_favorite_white_bottom, ic_favorite_white_bottom, ic_favorite_white_bottom)

            setBottomRankTextType(containerView)

            constraint_bottom_rank_item.setOnClickListener {
                item[position].let {  activity.moveToOthersProfileActivity(it.id, it.name, it.rank_color, it.profile_image.large) }
            }
        }
        override fun onItemSelected() { containerView.setBackgroundColor(Color.LTGRAY) }

        override fun onItemClear() { containerView.setBackgroundColor(0) }

        private fun setFavoriteIconVisibility(rankType: Int, favoriteLeft: ImageView, favoriteMiddle: ImageView, favoriteRight: ImageView) {
            if (rankType == RANK_TYPE_LIKE) {
                favoriteLeft.visibility = View.VISIBLE
                favoriteMiddle.visibility = View.VISIBLE
                favoriteRight.visibility = View.VISIBLE
            } else {
                favoriteLeft.visibility = View.INVISIBLE
                favoriteMiddle.visibility = View.INVISIBLE
                favoriteRight.visibility = View.INVISIBLE
            }
        }

        private fun setBottomRankTextType(view: View) {
            view.run {
                activity.setFontTypeface(tv_text_rank, BaseActivity.FONT_TYPE_MEDIUM)
                activity.setFontTypeface(tv_name_bottom_ranker, BaseActivity.FONT_TYPE_MEDIUM)
                activity.setFontTypeface(tv_point_bottom_ranker, BaseActivity.FONT_TYPE_REGULAR)
            }
        }
    }
}