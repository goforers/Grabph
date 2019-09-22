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

package com.goforer.base.presentation.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.graphics.Bitmap
import com.goforer.grabph.R

object DisplayUtils {
    private const val ACTION_BAR_HEIGHT = 56

    private var slidingDrawerWidth: Int = 0
    private var statusBarHeight: Int = 0

    fun setSlidingDrawerWidth(width: Int) {
        slidingDrawerWidth = width
    }

    fun getSlidingDrawerWidth(): Int {
        return slidingDrawerWidth
    }

    fun dpToPx(context: Context, dp: Float): Int {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
    }

    private fun pxToDp(context: Context, px: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertToDp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    fun getSlidingDrawerWidth(activity: Activity): Int {
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels

        val screenWidth = width / activity.resources.displayMetrics.density
        var navWidth = screenWidth - ACTION_BAR_HEIGHT

        navWidth = Math.min(navWidth, 320f)

        return navWidth.toInt()

    }

    fun getStatusBarHeight(activity: Activity): Int {
        var height = 0
        val resourceId = activity.resources.getIdentifier("height_24", "dimen", "android")
        if (resourceId > 0) {
            height = pxToDp(activity.applicationContext, activity.resources.getDimensionPixelSize(resourceId).toFloat()).toInt()
        }

        return height
    }

    fun getToolBarHeight(context: Context): Int {
        val typedValue = TypedValue()
        var actionBarHeight = 0

        if (context.theme.resolveAttribute(R.attr.actionBarSize, typedValue, true)) {

            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)

        }

        return actionBarHeight
    }

    fun scaleImageKeepAspectRatio(source: Bitmap, width: Int): Bitmap {
        val imageWidth = source.width
        val imageHeight = source.height
        val newHeight = imageHeight * width / imageWidth

        return Bitmap.createScaledBitmap(source, width, newHeight, false)
    }
}
