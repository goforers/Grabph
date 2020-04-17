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

package com.goforer.grabph.presentation.ui.home.quest.adapter.snapadapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.TopPortionQuest.FavoriteKeyword.Keyword
import kotlinx.android.synthetic.main.snap_quest_favorite_keyword.*
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_MEDIUM
import com.goforer.grabph.presentation.vm.quest.QuestViewModel
import kotlinx.android.extensions.LayoutContainer

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class FavoriteKeywordAdapter(
    private val activity: HomeActivity,
    private val questViewModel: QuestViewModel
): RecyclerView.Adapter<FavoriteKeywordAdapter.FavoriteKeywordViewHolder>() {

    private val keywords = ArrayList<Keyword>()
    private var selectedPosition = 0

    companion object {
        private const val WIDTH_MORE_SPACE = 160
        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK
                = object: DiffUtil.ItemCallback<Keyword>() {
            override fun areItemsTheSame(oldKeyword: Keyword,
                                         newKeyword: Keyword): Boolean = oldKeyword.title == newKeyword.title

            override fun areContentsTheSame(oldKeyword: Keyword, newKeyword: Keyword): Boolean = oldKeyword == newKeyword

            override fun getChangePayload(oldKeyword: Keyword, newKeyword: Keyword): Any? {
                return if (sameExceptTitle(oldKeyword, newKeyword)) {
                    PAYLOAD_TITLE
                } else {
                    null
                }
            }
        }

        private fun sameExceptTitle(oldKeyword: Keyword, newKeyword: Keyword): Boolean {
            return oldKeyword.copy(title = newKeyword.title) == newKeyword
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteKeywordViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.snap_quest_favorite_keyword, parent, false)

        return FavoriteKeywordViewHolder(view, activity, this)
    }

    override fun getItemCount(): Int = keywords.size

    override fun onBindViewHolder(holder: FavoriteKeywordViewHolder, position: Int) {
        val item = keywords[position]
        holder.bindItemHolder(holder, item, position)

        holder.itemView.setOnClickListener {
            selectedPosition = position
            questViewModel.setKeyword(item.title) // item.title = selectedKeyword
            notifyDataSetChanged()
        }
    }

    internal fun addList(list: List<Keyword>) {
        keywords.clear()
        keywords.addAll(list)
        notifyDataSetChanged()
    }

    class FavoriteKeywordViewHolder(
        override val containerView: View,
        private val activity: HomeActivity,
        private val adapter: FavoriteKeywordAdapter
    ): BaseViewHolder<Keyword>(containerView), LayoutContainer {
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Keyword, position: Int) {
            activity.setFontTypeface(tv_quest_favorite_keyword, FONT_TYPE_MEDIUM)
            tv_quest_favorite_keyword.requestLayout()
            tv_quest_favorite_keyword.text = item.title

            if (adapter.selectedPosition == position) {
                tv_quest_favorite_keyword.setBackgroundResource(R.drawable.border_of_upload_category_selected)
            } else {
                tv_quest_favorite_keyword.setBackgroundResource(R.drawable.border_of_upload_category_white)
            }
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }
    }
}