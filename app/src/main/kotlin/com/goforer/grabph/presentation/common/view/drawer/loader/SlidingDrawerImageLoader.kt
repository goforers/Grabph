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

package com.goforer.grabph.presentation.common.view.drawer.loader

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView

open class SlidingDrawerImageLoader private constructor(var imageLoader: IDrawerImageLoader?) {
    private var handleAllUris=false

    enum class Tags {
        DESCRIPTION_ICON,
        MENU_PICTURE,
        PRIMARY_ICON
    }

    fun withHandleAllUris(handleAllUris: Boolean): SlidingDrawerImageLoader {
        this.handleAllUris=handleAllUris
        return this
    }

    /**
     * @param imageView Displays an arbitrary image, such as an icon
     * @param uri URI
     * @param tag Tag
     * @return false if not consumed
     */
    fun setImage(imageView: AppCompatImageView, uri: Uri, tag: String): Boolean {
        //if we do not handle all uris and are not http or https we keep the original behavior
        if (handleAllUris || "http" == uri.scheme || "https" == uri.scheme) {
            if (imageLoader != null) {
                val placeHolder=imageLoader!!.placeholder(imageView.context, tag)
                imageLoader!![imageView, uri]=placeHolder
            }
            return true
        }
        return false
    }

    fun cancelImage(imageView: AppCompatImageView) {
        if (imageLoader != null) {
            imageLoader!!.cancel(imageView)
        }
    }

    interface IDrawerImageLoader {
        operator fun set(imageView: AppCompatImageView, uri: Uri, placeholder: Drawable)

        fun cancel(imageView: AppCompatImageView)

        fun placeholder(context: Context): Drawable

        /**
         * @param context Context
         * @param tag current possible tags: "profile", "profileDrawerItem", "accountHeader"
         * @return Drawable
         */
        fun placeholder(context: Context, tag: String): Drawable
    }

    companion object {
        private var SINGLETON: SlidingDrawerImageLoader? =null

        fun init(loaderImpl: IDrawerImageLoader): SlidingDrawerImageLoader {
            SINGLETON=SlidingDrawerImageLoader(loaderImpl)
            return SINGLETON as SlidingDrawerImageLoader
        }

        val instance: SlidingDrawerImageLoader
            get() {
                if (SINGLETON == null) {
                    SINGLETON=SlidingDrawerImageLoader(object : AbstractSlidingDrawerImageLoader() {

                    })
                }
                return SINGLETON as SlidingDrawerImageLoader
            }
    }
}
