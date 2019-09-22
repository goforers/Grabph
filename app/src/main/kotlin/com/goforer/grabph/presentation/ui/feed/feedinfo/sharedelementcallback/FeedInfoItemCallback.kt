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

package com.goforer.grabph.presentation.ui.feed.feedinfo.sharedelementcallback

import androidx.core.app.SharedElementCallback
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import java.util.*

class FeedInfoItemCallback: SharedElementCallback() {
    private var imageView: AppCompatImageView? = null


    override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
        names ?: return
        sharedElements ?: return

        val photoView = imageView ?: return

        removeObsoleteElements(names, sharedElements, mapObsoleteElements(names))
        mapSharedElement(names, sharedElements, photoView)
    }

    internal fun setViewBinding(imageView: AppCompatImageView) {
        this.imageView = imageView
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
