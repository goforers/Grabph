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

package com.goforer.grabph.presentation.ui.photog.sharedElementCallback

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import androidx.core.app.SharedElementCallback
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.FONT_SIZE
import com.goforer.grabph.presentation.caller.Caller.PADDING
import com.goforer.grabph.presentation.caller.Caller.TEXT_COLOR
import java.util.*

class PhotoListCallback(private val intent: Intent): SharedElementCallback() {
    private var targetTextSize: Float = 0.toFloat()

    private var titleView: AppCompatTextView? = null

    private var imageView: AppCompatImageView? = null

    private var targetPadding: Rect? = null

    private var targetTitleTextColors: ColorStateList? = null

    override fun onSharedElementStart(sharedElementNames: List<String>?,
                                      sharedElements: List<View>?,
                                      sharedElementSnapshots: List<View>?) {
        val titleView = titleView

        targetTextSize = titleView!!.textSize
        targetTitleTextColors = titleView.textColors
        targetPadding = Rect(titleView.paddingLeft,
                titleView.paddingTop,
                titleView.paddingRight,
                titleView.paddingBottom)

        if (Caller.hasAll(intent, TEXT_COLOR, FONT_SIZE, PADDING)) {
            titleView.setTextColor(intent.getIntExtra(TEXT_COLOR, Color.BLACK))

            val textSize = intent.getFloatExtra(FONT_SIZE, targetTextSize)

            titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

            val padding = intent.getParcelableExtra<Rect>(PADDING)

            titleView.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        }
    }

    override fun onSharedElementEnd(sharedElementNames: List<String>?,
                                    sharedElements: List<View>?,
                                    sharedElementSnapshots: List<View>?) {
        val titleView = titleView

        titleView!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize)
        if (targetTitleTextColors != null) {
            titleView.setTextColor(targetTitleTextColors)
        }

        if (targetPadding != null) {
            titleView.setPadding(targetPadding!!.left, targetPadding!!.top,
                    targetPadding!!.right, targetPadding!!.bottom)
        }
    }

    override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
        removeObsoleteElements(names, sharedElements, mapObsoleteElements(names!!))
        mapSharedElement(names, sharedElements!!, titleView!!)
        mapSharedElement(names, sharedElements, imageView!!)
    }

    internal fun setViewBinding(imageView: AppCompatImageView, textView: AppCompatTextView) {
        this.imageView = imageView
        this.titleView = textView
    }

    /**
     * Maps all views that don't start with "android" namespace.
     *
     * @param names All shared element names.
     * @return The obsolete shared element names.
     */
    private fun mapObsoleteElements(names: List<String>): List<String> {
        val elementsToRemove = ArrayList<String>(names.size)

        for (name in names) {
            if (name.startsWith("android")) continue
            elementsToRemove.add(name)
        }

        return elementsToRemove
    }

    /**
     * Removes obsolete elements from names and shared elements.
     *
     * @param names Shared element names.
     * @param sharedElements Shared elements.
     * @param elementsToRemove The elements that should be removed.
     */
    private fun removeObsoleteElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?,
                                       elementsToRemove: List<String>) {
        if (elementsToRemove.isNotEmpty()) {
            names!!.removeAll(elementsToRemove)
            for (elementToRemove in elementsToRemove) {
                sharedElements!!.remove(elementToRemove)
            }
        }
    }

    /**
     * Puts a shared element to transitions and names.
     *
     * @param names The names for this transition.
     * @param sharedElements The elements for this transition.
     * @param view The view to add.
     */
    private fun mapSharedElement(names: MutableList<String>, sharedElements: MutableMap<String, View>, view: View) {
        val transitionName = view.transitionName

        names.add(transitionName)
        sharedElements[transitionName] = view
    }
}
