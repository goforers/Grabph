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

@file:Suppress("DEPRECATION")

package com.goforer.grabph.presentation.ui.comment.adapter

import android.annotation.SuppressLint
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import android.graphics.Color
import androidx.recyclerview.widget.DiffUtil
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goforer.base.presentation.utils.CommonUtils.convertTime
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.ui.comment.fragment.CommentFragment
import com.goforer.grabph.data.datasource.model.cache.data.entity.comments.Comment
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_photo_comment.*
import kotlinx.android.synthetic.main.list_comment_item.*

class CommentAdapter(private val fragment: CommentFragment, private val workHandler: CommonWorkHandler)
                            : PagedListAdapter<Comment, CommentAdapter.CommentViewHolder>(DIFF_CALLBACK) {
    companion object {
        private const val PIC_WIDTH = 64
        private const val PIC_HEIGHT = 64

        private const val PHOTO = "photos/"

        private var commenter: Map<String, Comment>? = null

        fun getCommenter(): MutableMap<String, Comment> = commenter as MutableMap<String, Comment>

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(oldComment: Comment,
                                         newComment: Comment): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                return oldComment.id == newComment.id
            }

            override fun areContentsTheSame(oldComment: Comment,
                                            newComment: Comment): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.

                return oldComment.datecreate == newComment.datecreate
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_comment_item, parent, false)
        return CommentViewHolder(view, fragment, workHandler)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item: Comment? = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<Comment>?, currentList: PagedList<Comment>?) {
        fragment.stopMoreLoading(300)
        commenter = ArrayMap(currentList!!.size)
    }

    class CommentViewHolder(override val containerView: View, private val fragment: CommentFragment,
                            private val workHandler: CommonWorkHandler): BaseViewHolder<Comment>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Comment, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            iv_picture.requestLayout()
            tv_commenter.requestLayout()
            webview_comment.requestLayout()
            tv_date.requestLayout()

            val photoUrl = workHandler.getProfilePhotoURL(item.iconfarm, item.iconserver, item.author)

            fragment.commentActivity.setFixedImageSize(PIC_HEIGHT, PIC_WIDTH)
            fragment.commentActivity.setImageDraw(iv_picture, picture_container, photoUrl, false)
            getCommenter()[item.authorname] = item

            val name: String = if (item.realname.isEmpty()) {
                item.authorname
            } else {
                item.realname
            }

            tv_commenter.text = name

            var content: String

            content = item._content.toString()
            if (content.contains("[") && content.contains("]")) {
                var commenter: String
                commenter = if (content.contains("/]")) {
                    content.substring(content.indexOf("[") + 1, content.indexOf("/]"))
                } else {
                    content.substring(content.indexOf("[") + 1, content.indexOf("]"))
                }

                commenter = commenter.substring(commenter.indexOf(PHOTO) + PHOTO.length,
                        commenter.length)
                if (getCommenter()[commenter] != null) {
                    commenter = if (!getCommenter()[commenter]?.realname?.isEmpty()!!) {
                        getCommenter()[commenter]?.realname!!
                    } else {
                        getCommenter()[commenter]?.authorname!!
                    }
                }

                content = content.replace("[", "<a href=" + "")
                        .replace("]", ">@$commenter</a>")
            }

            content = "<html><head><style type=\"text/css\">body{color: #fff;}</style></head><body>$content</body></html>"

            val settings = webview_comment.settings

            settings.defaultTextEncodingName = "utf-8"
            webview_comment.loadData(content, "text/html; charset=utf-8", settings.defaultTextEncodingName)
            webview_comment.setBackgroundColor(0)

            val createdDate = java.lang.Long.parseLong(item.datecreate)
            val posted = convertTime(createdDate)

            tv_date.text = posted

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
    }
}

