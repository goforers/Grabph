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

package com.goforer.base.presentation.view.adatper

import android.annotation.SuppressLint
import androidx.paging.PagedListAdapter
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.recyclerview.widget.DiffUtil
import androidx.appcompat.view.ViewPropertyAnimatorCompatSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.goforer.base.presentation.view.holder.BaseViewHolder

abstract class BaseAdapter<T>(private val layoutResId: Int, DIFF_CALLBACK: DiffUtil.ItemCallback<T>):
                                            PagedListAdapter<T, BaseViewHolder<*>>(DIFF_CALLBACK) {

    private var lastAnimatedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): BaseViewHolder<*> {
        val view = LayoutInflater.from(parent.context.applicationContext)
                                                    .inflate(layoutResId, parent, false)
        return createViewHolder(parent, view, type)
    }

    /**
     * Create a ViewHolder when BaseAdapter needs a new ViewHolder of the given type to represent
     * an item.
     *
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     *
     *
     * @param viewGroup The ViewGroup into which the new View will be added after it is bound to
     * an mainAdapter position.
     * @param view The view this LayoutManager is bound to
     * @param type The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type
     */
    protected abstract fun createViewHolder(viewGroup: ViewGroup, view: View, type: Int): BaseViewHolder<*>

    fun resetAnimation() {
        lastAnimatedPosition = -1
    }

    /**
     * Set animation ViewHolder's item if need be.
     *
     * @param viewHolder the ViewHolder to set animation ViewHolder's item
     */
    @SuppressLint("RestrictedApi")
    fun setAnimateViewIfNecessary(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        val position = viewHolder.adapterPosition

        if (position <= lastAnimatedPosition) return

        val itemView = viewHolder.itemView

        ViewCompat.setPivotX(itemView, 0f)
        ViewCompat.setPivotY(itemView, 0f)
        ViewCompat.setTranslationY(itemView, VIEW_VERTICAL_POSITION)
        ViewCompat.postOnAnimation(itemView) {
            val translation = ViewCompat.animate(itemView).translationY(ANIMATED_VALUE)
            @SuppressLint("RestrictedApi")
            val set = ViewPropertyAnimatorCompatSet()
            set.play(translation)
            val interpolator = DecelerateInterpolator(ANIMATION_DEGREE)
            set.setInterpolator(interpolator)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(object : ViewPropertyAnimatorListener {
                        override fun onAnimationStart(view: View) {}

                        override fun onAnimationEnd(view: View) {
                            lastAnimatedPosition = position
                        }

                        override fun onAnimationCancel(view: View) {}
                    }).start()
        }
    }

    companion object {
        private const val VIEW_VERTICAL_POSITION = 100f
        private const val ANIMATED_VALUE = 0f
        private const val ANIMATION_DURATION: Long = 250
        private const val ANIMATION_DEGREE = 1.0f

        const val VIEW_TYPE_HEADER = 10000
        const val VIEW_TYPE_FOOTER = 10001
        const val VIEW_TYPE_LOADING = 10002
        const val VIEW_TYPE_ITEM = 10003
        const val VIEW_TYPE_NONE_COMMENT = 10004
    }
}
