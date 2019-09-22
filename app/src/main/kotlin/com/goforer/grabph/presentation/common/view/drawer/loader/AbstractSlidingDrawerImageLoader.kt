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
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.mikepenz.materialdrawer.util.DrawerUIUtils

open class AbstractSlidingDrawerImageLoader: SlidingDrawerImageLoader.IDrawerImageLoader {
    override fun set(imageView: AppCompatImageView, uri: Uri, placeholder: Drawable) {
        //this won't do anything
        Log.i("MaterialDrawer", "you have not specified a ImageLoader implementation through " + "the DrawerImageLoader.init(IDrawerImageLoader) method")
    }

    override fun cancel(imageView: AppCompatImageView) {}

    override fun placeholder(context: Context): Drawable {
        return DrawerUIUtils.getPlaceHolder(context)
    }

    override fun placeholder(context: Context, tag: String): Drawable {
        return placeholder(context)
    }
}