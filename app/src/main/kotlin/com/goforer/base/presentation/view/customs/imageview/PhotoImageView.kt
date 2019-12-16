/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

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

package com.goforer.base.presentation.view.customs.imageview

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.github.chrisbanes.photoview.*

/**
 * A zoomable [ImageView]. See [PhotoViewAttacher] for most of the details on how the zooming
 * is accomplished
 */
class PhotoImageView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, defStyle: Int = 0): AppCompatImageView(context, attr, defStyle) {

    /**
     * Get the current [PhotoViewAttacher] for this view. Be wary of holding on to references
     * to this attacher, as it has a reference to this view, which, if a reference is held in the
     * wrong place, can cause memory leaks.
     *
     * @return the attacher.
     */
    private var attacher: PhotoViewAttacher? = null
    private var pendingScaleType: ScaleType? = null

    val isZoomEnabled: Boolean
        @Deprecated("")
        get() = attacher!!.isZoomEnabled

    var isZoomable: Boolean
        get() = attacher!!.isZoomable
        set(zoomable) {
            attacher!!.isZoomable = zoomable
        }

    val displayRect: RectF
        get() = attacher!!.displayRect

    var minimumScale: Float
        get() = attacher!!.minimumScale
        set(minimumScale) {
            attacher!!.minimumScale = minimumScale
        }

    var mediumScale: Float
        get() = attacher!!.mediumScale
        set(mediumScale) {
            attacher!!.mediumScale = mediumScale
        }

    var maximumScale: Float
        get() = attacher!!.maximumScale
        set(maximumScale) {
            attacher!!.maximumScale = maximumScale
        }

    var scale: Float
        get() = attacher!!.scale
        set(scale) {
            attacher!!.scale = scale
        }

    init {
        init()
    }

    private fun init() {
        attacher = PhotoViewAttacher(this)
        //We always pose as a Matrix scale type, though we can change to another scale type
        //via the attacher
        super.setScaleType(ScaleType.MATRIX)
        //apply the previously applied scale type
        if (pendingScaleType != null) {
            scaleType = pendingScaleType as ScaleType
            pendingScaleType = null
        }
    }

    override fun getScaleType(): ScaleType {
        return attacher!!.scaleType
    }

    override fun getImageMatrix(): Matrix {
        return attacher!!.imageMatrix
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {
        attacher!!.setOnLongClickListener(listener)
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        attacher!!.setOnClickListener(listener)
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (attacher == null) {
            pendingScaleType = scaleType
        } else {
            attacher!!.scaleType = scaleType
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        // setImageBitmap calls through to this method
        if (attacher != null) {
            attacher!!.update()
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (attacher != null) {
            attacher!!.update()
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        if (attacher != null) {
            attacher!!.update()
        }
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        if (changed) {
            attacher!!.update()
        }
        return changed
    }

    fun setRotationTo(rotationDegree: Float) {
        attacher!!.setRotationTo(rotationDegree)
    }

    fun setRotationBy(rotationDegree: Float) {
        attacher!!.setRotationBy(rotationDegree)
    }

    fun getDisplayMatrix(matrix: Matrix) {
        attacher!!.getDisplayMatrix(matrix)
    }

    fun setDisplayMatrix(finalRectangle: Matrix): Boolean {
        return attacher!!.setDisplayMatrix(finalRectangle)
    }

    fun getSuppMatrix(matrix: Matrix) {
        attacher!!.getSuppMatrix(matrix)
    }

    fun setSuppMatrix(matrix: Matrix): Boolean {
        return attacher!!.setDisplayMatrix(matrix)
    }

    fun setAllowParentInterceptOnEdge(allow: Boolean) {
        attacher!!.setAllowParentInterceptOnEdge(allow)
    }

    fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float) {
        attacher!!.setScaleLevels(minimumScale, mediumScale, maximumScale)
    }

    fun setOnMatrixChangeListener(listener: OnMatrixChangedListener) {
        attacher!!.setOnMatrixChangeListener(listener)
    }

    fun setOnPhotoTapListener(listener: OnPhotoTapListener) {
        attacher!!.setOnPhotoTapListener(listener)
    }

    fun setOnOutsidePhotoTapListener(listener: OnOutsidePhotoTapListener) {
        attacher!!.setOnOutsidePhotoTapListener(listener)
    }

    fun setOnViewTapListener(listener: OnViewTapListener) {
        attacher!!.setOnViewTapListener(listener)
    }

    fun setOnViewDragListener(listener: OnViewDragListener) {
        attacher!!.setOnViewDragListener(listener)
    }

    fun setScale(scale: Float, animate: Boolean) {
        attacher!!.setScale(scale, animate)
    }

    fun setScale(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        attacher!!.setScale(scale, focalX, focalY, animate)
    }

    fun setZoomTransitionDuration(milliseconds: Int) {
        attacher!!.setZoomTransitionDuration(milliseconds)
    }

    fun setOnDoubleTapListener(onDoubleTapListener: GestureDetector.OnDoubleTapListener) {
        attacher!!.setOnDoubleTapListener(onDoubleTapListener)
    }

    fun setOnScaleChangeListener(onScaleChangedListener: OnScaleChangedListener) {
        attacher!!.setOnScaleChangeListener(onScaleChangedListener)
    }

    fun setOnSingleFlingListener(onSingleFlingListener: OnSingleFlingListener) {
        attacher!!.setOnSingleFlingListener(onSingleFlingListener)
    }
}
