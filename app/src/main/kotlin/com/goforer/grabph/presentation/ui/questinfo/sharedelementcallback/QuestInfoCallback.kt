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

package com.goforer.grabph.presentation.ui.questinfo.sharedelementcallback

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.SharedElementCallback
import com.goforer.grabph.presentation.caller.Caller
import java.util.ArrayList

class QuestInfoCallback(private val intent: Intent): SharedElementCallback() {
    private var targetExplanationSize: Float = 0.toFloat()
    private var targetOwnerNameSize: Float = 0.toFloat()

    private var targetExplanationTextColors: ColorStateList? = null
    private var targetOwnerNameTextColors: ColorStateList? = null

    private var explanationView: AppCompatTextView? = null
    private var ownerNameView: AppCompatTextView? = null

    private var imageView: AppCompatImageView? = null
    private var logoView: AppCompatImageView? = null

    private var targetExplanationPadding: Rect? = null
    private var targetOwnerNamePadding: Rect? = null

    override fun onSharedElementStart(sharedElementNames: List<String>?,
                                      sharedElements: List<View>?,
                                      sharedElementSnapshots: List<View>?) {
        val explanationTextView = explanationView ?: return
        val ownerNameTextView = ownerNameView ?: return

        targetExplanationSize = explanationTextView.textSize
        targetExplanationTextColors = explanationTextView.textColors
        targetExplanationPadding = Rect(explanationTextView.paddingLeft,
                explanationTextView.paddingTop,
                explanationTextView.paddingRight,
                explanationTextView.paddingBottom)

        if (Caller.hasAll(intent, Caller.TEXT_COLOR, Caller.FONT_SIZE, Caller.PADDING)) {
            val padding = intent.getParcelableExtra<Rect>(Caller.PADDING)

            explanationTextView.setTextColor(intent.getIntExtra(Caller.TEXT_COLOR, Color.GRAY))
            explanationTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, intent.getFloatExtra(Caller.FONT_SIZE, targetExplanationSize))
            explanationTextView.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        }

        targetOwnerNameSize = ownerNameTextView.textSize
        targetOwnerNameTextColors = ownerNameTextView.textColors
        targetOwnerNamePadding = Rect(ownerNameTextView.paddingLeft,
                ownerNameTextView.paddingTop,
                ownerNameTextView.paddingRight,
                ownerNameTextView.paddingBottom)

        if (Caller.hasAll(intent, Caller.TEXT_COLOR, Caller.FONT_SIZE, Caller.PADDING)) {
            val padding = intent.getParcelableExtra<Rect>(Caller.PADDING)

            ownerNameTextView.setTextColor(intent.getIntExtra(Caller.TEXT_COLOR, Color.WHITE))
            ownerNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, intent.getFloatExtra(Caller.FONT_SIZE, targetOwnerNameSize))
            ownerNameTextView.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        }
    }

    override fun onSharedElementEnd(sharedElementNames: List<String>?,
                                    sharedElements: List<View>?,
                                    sharedElementSnapshots: List<View>?) {
        val explanationTextView = explanationView ?: return
        val ownerNameTextView = ownerNameView ?: return

        explanationTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetExplanationSize)
        if (targetExplanationTextColors != null) {
            explanationTextView.setTextColor(targetExplanationTextColors)
        }

        if (targetExplanationPadding != null) {
            explanationTextView.setPadding(targetExplanationPadding!!.left, targetExplanationPadding!!.top,
                    targetExplanationPadding!!.right, targetExplanationPadding!!.bottom)
        }

        ownerNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetOwnerNameSize)
        if (targetOwnerNameTextColors != null) {
            ownerNameTextView.setTextColor(targetOwnerNameTextColors)
        }

        if (targetOwnerNamePadding != null) {
            ownerNameTextView.setPadding(targetOwnerNamePadding!!.left, targetOwnerNamePadding!!.top,
                    targetOwnerNamePadding!!.right, targetOwnerNamePadding!!.bottom)
        }
    }

    override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
        names ?: return
        sharedElements ?: return
        val photoView = imageView ?: return
        val logoView = this.logoView ?: return
        val explanationView = this.explanationView ?: return
        val ownerNameView = this.ownerNameView ?: return

        removeObsoleteElements(names, sharedElements, mapObsoleteElements(names))
        mapSharedElement(names, sharedElements, photoView)
        mapSharedElement(names, sharedElements, logoView)
        mapSharedElement(names, sharedElements, explanationView)
        mapSharedElement(names, sharedElements, ownerNameView)
    }

    internal fun setViewBinding(imageView: AppCompatImageView) {
        this.imageView = imageView
    }

    internal fun setViewBinding(imageView: AppCompatImageView, logoView: AppCompatImageView,
                       explanationView: AppCompatTextView, ownerNameView: AppCompatTextView) {
        this.imageView = imageView
        this.logoView = logoView
        this.explanationView = explanationView
        this.ownerNameView = ownerNameView
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
