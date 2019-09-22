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

@file:Suppress("DEPRECATION")

package com.goforer.base.presentation.view.customs.viewpager

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.huanhailiuxin.coolviewpager.CoolViewPager
import timber.log.Timber
import kotlin.math.abs

class SwipeCoolViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet ? = null): CoolViewPager(context, attrs) {
    private var startDragX: Float = 0.toFloat()
    private var startDragY: Float = 0.toFloat()

    private var listenerY: OnSwipeOutListener? =null

    private var type: Int = 0

    internal var isPageScrolled: Boolean = false

    fun setViewPagerType(type: Int) {
        this.type = type
    }

    fun setOnSwipeOutListener(listener: OnSwipeOutListener) {
        listenerY = listener
    }

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
                            listenerY!!.onSwipeOutAtStart()
                        } else if (startDragX > x && currentItem == adapter!!.count - 1) {
                            listenerY!!.onSwipeOutAtEnd()
                        } else if (abs(distanceY) > abs(distanceX)) {
                            if (distanceY < 0 && abs(distanceY) > SWIPE_THRESHOLD_VELOCITY) {
                                Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeUp")
                                listenerY!!.onSwipeUp(x, y)
                            } else if (abs(distanceY) > SWIPE_THRESHOLD_VELOCITY) {
                                Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeDown")
                                listenerY!!.onSwipeDown(x, y)
                            }
                        } else {
                            if (distanceX > 0 && abs(distanceX) > SWIPE_THRESHOLD_VELOCITY) {
                                Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeRight")
                                listenerY!!.onSwipeRight(x, y)
                            } else if (abs(distanceX) > SWIPE_THRESHOLD_VELOCITY) {
                                Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeLeft")
                                listenerY!!.onSwipeLeft(x, y)
                            }
                        }
                    }
                }

                /*
                 * It's the android ViewPager's bug
                 * stackoverflow's report: http://stackoverflow.com/questions/6919292/pointerindex-out-of-range-android-multitouch
                 * android's report: http://code.google.com/p/android/issues/detail?id=18990
                 *
                 * My simple's method to fix this bug:
                 * You can extends the ViewPager class, your own ViewPager should override the onTouchEvent and the onInterceptTouchEvent methods, and try-catch the IllegalArgumentException exception. Then use your own ViewPager class in layout or others you want.
                 */
                try {
                    return super.onInterceptTouchEvent(ev)
                } catch (ex: IllegalArgumentException) {
                    ex.printStackTrace()
                }

                return false
            }
            else -> return try {
                super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (type) {
            PAGER_PHOTO_FEED_TYPE, PAGER_PHOTO_VIEWER_TYPE -> {
                val action = ev.action and MotionEvent.ACTION_MASK

                val x = ev.x
                val y = ev.y

                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        startDragX = ev.x
                        startDragY = ev.y
                    }

                    MotionEvent.ACTION_SCROLL -> {
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val distanceY = y - startDragY
                        val distanceX = x - startDragX
                        if (startDragX < x && currentItem == 0) {
                            listenerY!!.onSwipeOutAtStart()
                        } else if (startDragX > x && currentItem == adapter!!.count - 1) {
                            listenerY!!.onSwipeOutAtEnd()
                        } else if (abs(distanceY) > abs(distanceX)) {
                            if (distanceY < 0 && abs(distanceY) > SWIPE_THRESHOLD_VELOCITY) {
                                Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeUp")
                                listenerY!!.onSwipeUp(x, y)
                            } else if (abs(distanceY) > SWIPE_THRESHOLD_VELOCITY) {
                                Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeDown")
                                listenerY!!.onSwipeDown(x, y)
                            }
                        } else {
                            if (distanceX > 0 && abs(distanceX) > SWIPE_THRESHOLD_VELOCITY) {
                                Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeRight")
                                listenerY!!.onSwipeRight(x, y)
                            } else if (abs(distanceX) > SWIPE_THRESHOLD_VELOCITY) {
                                Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeLeft")
                                listenerY!!.onSwipeLeft(x, y)
                            }
                        }
                    }
                }

                /*
                 * It's the android ViewPager's bug
                 * stackoverflow's report: http://stackoverflow.com/questions/6919292/pointerindex-out-of-range-android-multitouch
                 * android's report: http://code.google.com/p/android/issues/detail?id=18990
                 *
                 * My simple's method to fix this bug:
                 * You can extends the ViewPager class, your own ViewPager should override the onTouchEvent and the onInterceptTouchEvent methods, and try-catch the IllegalArgumentException exception. Then use your own ViewPager class in layout or others you want.
                 */
                try {
                    return super.onTouchEvent(ev)
                } catch (ex: IllegalArgumentException) {
                    ex.printStackTrace()
                }

                return false
            }
            else -> return try {
                super.onTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                false
            }
        }
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

        private const val SWIPE_THRESHOLD_VELOCITY = 50f
    }
}