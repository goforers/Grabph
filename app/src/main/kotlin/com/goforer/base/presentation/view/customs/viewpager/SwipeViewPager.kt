/*
 * Copyright (C)  2015 - 2019 Lukoh Nam, goForer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goforer.base.presentation.view.customs.viewpager

import android.annotation.SuppressLint
import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import timber.log.Timber
import kotlin.math.abs

/**
 * SwipeViewPager detect when user is trying to swipe out of bounds
 */
class SwipeViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet ? = null): ViewPager(context, attrs) {
    private var startDragX: Float = 0.toFloat()
    private var startDragY: Float = 0.toFloat()

    private var listener: OnSwipeOutListener? =null

    private var type: Int = 0

    private var isPageScrolled: Boolean = false
    private var mEnabled: Boolean = false
    private var isScrollEnabled: Boolean = true

    fun setViewPagerType(type: Int) {
        this.type = type
    }

    fun setOnSwipeOutListener(listener: OnSwipeOutListener) {
        this.listener = listener
    }

    fun setScrollEnabled(flag: Boolean) {
        this.isScrollEnabled = flag
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if (mEnabled) super.onTouchEvent(ev) else false
    }

    @SuppressLint("BinaryOperationInTimber")
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (type) {
            PAGER_PHOTO_FEED_TYPE, PAGER_PHOTO_VIEWER_TYPE -> {
                val action= ev.action and MotionEvent.ACTION_MASK

                val x= ev.x
                val y= ev.y

                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        startDragX = ev.x
                        startDragY = ev.y
                    }

                    MotionEvent.ACTION_SCROLL -> {
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val distanceY= y - startDragY
                        val distanceX= x - startDragX
                        if (startDragX < x && currentItem == 0) {
                            listener!!.onSwipeOutAtStart()
                        } else if (startDragX > x && currentItem == adapter!!.count - 1) {
                            listener!!.onSwipeOutAtEnd()
                        } else if (abs(distanceY) > abs(distanceX)) {
                            Timber.e("onInterceptTouchEvent : ACTION_MOVE = "
                                    + (y - startDragY).toString() + "Y : " + abs(y))
                            if (distanceY > SWIPE_MIN_DISTANCE && abs(y) > SWIPE_THRESHOLD_VELOCITY) {
                                listener!!.onSwipeDown(x, y)
                            } else {
                                listener!!.onSwipeUp(x, y)
                            }
                        } else {
                            if (distanceX > SWIPE_MIN_DISTANCE && abs(x) > SWIPE_THRESHOLD_VELOCITY) {
                                listener!!.onSwipeRight(x, y)
                            } else {
                                listener!!.onSwipeLeft(x, y)
                            }
                        }
                    }
                }

                return if (mEnabled) super.onInterceptTouchEvent(ev) else false
            }
            else -> return try {
                super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                false
            }

        }
    }

    override fun executeKeyEvent(ev: KeyEvent): Boolean {
        return if (mEnabled) super.executeKeyEvent(ev) else false
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return isScrollEnabled && canScrollVertically(direction)
    }

    fun setPageScrolled(isPageScrolled: Boolean) {
        this.isPageScrolled = isPageScrolled
    }

    fun setSwipeEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    interface OnSwipeOutListener {
        fun onSwipeOutAtStart()

        fun onSwipeOutAtEnd()

        fun onSwipeLeft(x: Float, y: Float)

        fun onSwipeRight(x: Float, y: Float)

        fun onSwipeDown(x: Float, y: Float)

        fun onSwipeUp(x: Float, y: Float)
    }

    companion object {
        const val PAGER_PHOTO_FEED_TYPE = 0
        const val PAGER_PHOTO_VIEWER_TYPE = 1

        private const val SWIPE_MIN_DISTANCE = 125f
        private const val SWIPE_THRESHOLD_VELOCITY = 150f
    }
}
