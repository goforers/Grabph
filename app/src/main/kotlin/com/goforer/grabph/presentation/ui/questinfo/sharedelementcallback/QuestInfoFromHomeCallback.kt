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

class QuestInfoFromHomeCallback(private val intent: Intent): SharedElementCallback() {
    private var targetTitleSize: Float = 0.toFloat()

    private var targetTitleTextColors: ColorStateList? = null

    private var titleView: AppCompatTextView? = null

    private var imageView: AppCompatImageView? = null

    private var targetTitlePadding: Rect? = null

    override fun onSharedElementStart(sharedElementNames: List<String>?,
                                      sharedElements: List<View>?,
                                      sharedElementSnapshots: List<View>?) {
        val titleTextView = titleView ?: return

        targetTitleSize = titleTextView.textSize
        targetTitleTextColors = titleTextView.textColors
        targetTitlePadding = Rect(titleTextView.paddingLeft,
                titleTextView.paddingTop,
                titleTextView.paddingRight,
                titleTextView.paddingBottom)

        if (Caller.hasAll(intent, Caller.TEXT_COLOR, Caller.FONT_SIZE, Caller.PADDING)) {
            val padding = intent.getParcelableExtra<Rect>(Caller.PADDING)

            titleTextView.setTextColor(intent.getIntExtra(Caller.TEXT_COLOR, Color.BLACK))
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, intent.getFloatExtra(Caller.FONT_SIZE, targetTitleSize))
            titleTextView.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        }
    }

    override fun onSharedElementEnd(sharedElementNames: List<String>?,
                                    sharedElements: List<View>?,
                                    sharedElementSnapshots: List<View>?) {
        val titleTextView = titleView ?: return

        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTitleSize)
        if (targetTitleTextColors != null) {
            titleTextView.setTextColor(targetTitleTextColors)
        }

        if (targetTitlePadding != null) {
            titleTextView.setPadding(targetTitlePadding!!.left, targetTitlePadding!!.top,
                    targetTitlePadding!!.right, targetTitlePadding!!.bottom)
        }
    }

    override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
        names ?: return
        sharedElements ?: return
        val photoView = imageView ?: return
        val textView  = titleView ?: return

        removeObsoleteElements(names, sharedElements, mapObsoleteElements(names))
        mapSharedElement(names, sharedElements, photoView)
        mapSharedElement(names, sharedElements, textView)
    }

    internal fun setViewBinding(imageView: AppCompatImageView, titleView: AppCompatTextView) {
        this.imageView = imageView
        this.titleView = titleView
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