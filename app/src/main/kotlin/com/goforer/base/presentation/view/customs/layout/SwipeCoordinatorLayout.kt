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

package com.goforer.base.presentation.view.customs.layout

import android.annotation.SuppressLint
import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import timber.log.Timber
import kotlin.math.abs

/**
 * SwipeCoordinatorLayout detect when user is trying to swipe out of bounds
 */
class SwipeCoordinatorLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet ? = null): CoordinatorLayout(context, attrs) {
    private var startDragX: Float = 0.toFloat()
    private var startDragY: Float = 0.toFloat()

    private lateinit var listener: OnSwipeOutListener

    private lateinit var activity: BaseActivity

    companion object {
        private const val SWIPE_THRESHOLD_VELOCITY_X = 250f
        private const val SWIPE_THRESHOLD_VELOCITY_Y = 50f
    }

    fun setOnSwipeOutListener(activity: BaseActivity, listener: OnSwipeOutListener) {
        this.listener = listener
        this.activity = activity
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action= ev.actionMasked
        val x= ev.x
        val y= ev.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                startDragX = ev.x
                startDragY = ev.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                listener.onSwipeDone()
            }

            MotionEvent.ACTION_SCROLL -> {
            }

            MotionEvent.ACTION_MOVE -> {
                val distanceY = y - startDragY
                val distanceX = x - startDragX

                if (abs(distanceY) > abs(distanceX)) {
                    if (distanceY < 0 && abs(distanceY) > SWIPE_THRESHOLD_VELOCITY_Y) {
                        Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeUp")

                        listener.onSwipeUp(x, y)

                    } else if (abs(distanceY) > SWIPE_THRESHOLD_VELOCITY_Y) {
                        Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeDown")

                        listener.onSwipeDown(x, y)
                    }
                } else {
                    if (distanceX > 0 && abs(distanceX) > SWIPE_THRESHOLD_VELOCITY_X) {
                        Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeRight")

                        listener.onSwipeRight(x, y)
                    } else if (abs(distanceX) > SWIPE_THRESHOLD_VELOCITY_X) {
                        Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeLeft")

                        listener.onSwipeLeft(x, y)
                    }
                }
            }
        }

        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action= ev.actionMasked
        val x= ev.x
        val y= ev.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                startDragX = ev.x
                startDragY = ev.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                listener.onSwipeDone()
            }

            MotionEvent.ACTION_SCROLL -> {
            }

            MotionEvent.ACTION_MOVE -> {
                val distanceY = y - startDragY
                val distanceX = x - startDragX

                if (abs(distanceY) > abs(distanceX)) {
                    if (distanceY < 0 && abs(distanceY) > SWIPE_THRESHOLD_VELOCITY_Y) {
                        Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeUp")

                        listener.onSwipeUp(x, y)

                    } else if (abs(distanceY) > SWIPE_THRESHOLD_VELOCITY_Y) {
                        Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeDown")

                        listener.onSwipeDown(x, y)
                    }
                } else {
                    if (distanceX > 0 && abs(distanceX) > SWIPE_THRESHOLD_VELOCITY_X) {
                        Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeRight")

                        listener.onSwipeRight(x, y)
                    } else if (abs(distanceX) > SWIPE_THRESHOLD_VELOCITY_X) {
                        Timber.e("onInterceptTouchEvent : ACTION_MOVE = onSwipeLeft")

                        listener.onSwipeLeft(x, y)
                    }
                }
            }
        }

        return super.onTouchEvent(ev)
    }
}
