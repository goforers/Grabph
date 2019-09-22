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

@file:Suppress("NAME_SHADOWING")

package com.goforer.base.presentation.view.customs.drawable

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.widget.ImageView

internal class RoundDrawable private constructor(val bitmap: Bitmap): Drawable() {
    private val bounds = RectF()
    private val drawableRect = RectF()
    private val bitmapRect = RectF()
    private val borderRect = RectF()
    private val bitmapPaint: Paint
    private val borderPaint: Paint
    private val shaderMatrix = Matrix()

    /**
     * Gets a set of state spec / color pairs
     *
     * @return a set of state spec / color pairs
     * @see ColorStateList
     */
    private var borderColors=ColorStateList.valueOf(DEFAULT_BORDER_COLOR)

    private var scaleType: ImageView.ScaleType=ImageView.ScaleType.FIT_CENTER

    private val bitmapWidth: Int = bitmap.width
    private val mBitmapHeight: Int = bitmap.height

    private var tileModeX: Shader.TileMode=Shader.TileMode.CLAMP
    private var tileModeY: Shader.TileMode=Shader.TileMode.CLAMP

    private var rebuildShader = true
    private var oval = false
    private var onlyTopCorner = false

    private var cornerRadius = 0f
    private var borderWidth = 0f

    /**
     * Gets the RoundDrawable's basic border color
     *
     * @return the the RoundDrawable's default border color
     */
    val borderColor: Int
        get() = borderColors.defaultColor

    init {
        bitmapRect.set(0f, 0f, bitmapWidth.toFloat(), mBitmapHeight.toFloat())

        bitmapPaint = Paint()
        bitmapPaint.style = Paint.Style.FILL
        bitmapPaint.isAntiAlias = true

        borderPaint = Paint()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.isAntiAlias = true
        borderPaint.color = borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
        borderPaint.strokeWidth = borderWidth
    }

    override fun isStateful(): Boolean {
        return borderColors.isStateful
    }

    override fun onStateChange(state: IntArray): Boolean {
        val newColor=borderColors.getColorForState(state, 0)
        return if (borderPaint.color != newColor) {
            borderPaint.color = newColor
            true
        } else {
            super.onStateChange(state)
        }
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx: Float
        var dy: Float

        when (scaleType) {
            ImageView.ScaleType.CENTER -> {
                borderRect.set(bounds)
                borderRect.inset(borderWidth / 2, borderWidth / 2)

                shaderMatrix.reset()
                shaderMatrix.setTranslate(((borderRect.width() - bitmapWidth) * 0.5f + 0.5f).toInt().toFloat(),
                        ((borderRect.height() - mBitmapHeight) * 0.5f + 0.5f).toInt().toFloat())
            }

            ImageView.ScaleType.CENTER_CROP -> {
                borderRect.set(bounds)
                borderRect.inset(borderWidth / 2, borderWidth / 2)

                shaderMatrix.reset()

                dx=0f
                dy=0f

                if (bitmapWidth * borderRect.height() > borderRect.width() * mBitmapHeight) {
                    scale=borderRect.height() / mBitmapHeight.toFloat()
                    dx=(borderRect.width() - bitmapWidth * scale) * 0.5f
                } else {
                    scale=borderRect.width() / bitmapWidth.toFloat()
                    dy=(borderRect.height() - mBitmapHeight * scale) * 0.5f
                }

                shaderMatrix.setScale(scale, scale)
                shaderMatrix.postTranslate((dx + 0.5f).toInt() + borderWidth, (dy + 0.5f).toInt() + borderWidth)
            }

            ImageView.ScaleType.CENTER_INSIDE -> {
                shaderMatrix.reset()

                scale = if (bitmapWidth <= bounds.width() && mBitmapHeight <= bounds.height()) {
                    1.0f
                } else {
                    Math.min(bounds.width() / bitmapWidth.toFloat(),
                            bounds.height() / mBitmapHeight.toFloat())
                }

                dx=((bounds.width() - bitmapWidth * scale) * 0.5f + 0.5f).toInt().toFloat()
                dy=((bounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f).toInt().toFloat()

                shaderMatrix.setScale(scale, scale)
                shaderMatrix.postTranslate(dx, dy)

                borderRect.set(bitmapRect)
                shaderMatrix.mapRect(borderRect)
                borderRect.inset(borderWidth / 2, borderWidth / 2)
                shaderMatrix.setRectToRect(bitmapRect, borderRect, Matrix.ScaleToFit.FILL)
            }

            ImageView.ScaleType.FIT_CENTER -> {
                borderRect.set(bitmapRect)
                shaderMatrix.setRectToRect(bitmapRect, bounds, Matrix.ScaleToFit.CENTER)
                shaderMatrix.mapRect(borderRect)
                borderRect.inset(borderWidth / 2, borderWidth / 2)
                shaderMatrix.setRectToRect(bitmapRect, borderRect, Matrix.ScaleToFit.FILL)
            }

            ImageView.ScaleType.FIT_END -> {
                borderRect.set(bitmapRect)
                shaderMatrix.setRectToRect(bitmapRect, bounds, Matrix.ScaleToFit.END)
                shaderMatrix.mapRect(borderRect)
                borderRect.inset(borderWidth / 2, borderWidth / 2)
                shaderMatrix.setRectToRect(bitmapRect, borderRect, Matrix.ScaleToFit.FILL)
            }

            ImageView.ScaleType.FIT_START -> {
                borderRect.set(bitmapRect)
                shaderMatrix.setRectToRect(bitmapRect, bounds, Matrix.ScaleToFit.START)
                shaderMatrix.mapRect(borderRect)
                borderRect.inset(borderWidth / 2, borderWidth / 2)
                shaderMatrix.setRectToRect(bitmapRect, borderRect, Matrix.ScaleToFit.FILL)
            }

            ImageView.ScaleType.FIT_XY -> {
                borderRect.set(bounds)
                borderRect.inset(borderWidth / 2, borderWidth / 2)
                shaderMatrix.reset()
                shaderMatrix.setRectToRect(bitmapRect, borderRect, Matrix.ScaleToFit.FILL)
            }

            else -> {
                borderRect.set(bitmapRect)
                shaderMatrix.setRectToRect(bitmapRect, bounds, Matrix.ScaleToFit.CENTER)
                shaderMatrix.mapRect(borderRect)
                borderRect.inset(borderWidth / 2, borderWidth / 2)
                shaderMatrix.setRectToRect(bitmapRect, borderRect, Matrix.ScaleToFit.FILL)
            }
        }

        drawableRect.set(borderRect)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        bounds.set(bounds)
        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {
        val mBitmapShader: BitmapShader

        if (rebuildShader) {
            mBitmapShader=BitmapShader(bitmap, tileModeX, tileModeY)
            if (tileModeX == Shader.TileMode.CLAMP && tileModeY == Shader.TileMode.CLAMP) {
                mBitmapShader.setLocalMatrix(shaderMatrix)
            }
            bitmapPaint.shader = mBitmapShader
            rebuildShader = false
        }

        if (oval) {
            if (borderWidth > 0) {
                canvas.drawOval(drawableRect, bitmapPaint)
                canvas.drawOval(borderRect, borderPaint)
            } else {
                canvas.drawOval(drawableRect, bitmapPaint)
            }
        } else {
            if (onlyTopCorner) {
                canvas.drawRect(RectF(drawableRect.left, drawableRect.bottom / 2, drawableRect.right, drawableRect.bottom), bitmapPaint)
                canvas.drawRoundRect(drawableRect, cornerRadius, cornerRadius, bitmapPaint)
            } else {
                if (borderWidth > 0) {
                    canvas.drawRoundRect(drawableRect, Math.max(cornerRadius, 0f),
                            Math.max(cornerRadius, 0f), bitmapPaint)
                    canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)
                } else {
                    canvas.drawRoundRect(drawableRect, cornerRadius, cornerRadius, bitmapPaint)
                }
            }
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getAlpha(): Int {
        return bitmapPaint.alpha
    }

    override fun setAlpha(alpha: Int) {
        bitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? {
        return bitmapPaint.colorFilter
    }

    override fun setColorFilter(cf: ColorFilter?) {
        bitmapPaint.colorFilter = cf
        invalidateSelf()
    }

    @Deprecated("")
    override fun setDither(dither: Boolean) {
        bitmapPaint.isDither = dither
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        bitmapPaint.isFilterBitmap = filter
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return bitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    fun getCornerRadius(): Float {
        return cornerRadius
    }

    fun setCornerRadius(radius: Float): RoundDrawable {
        cornerRadius = radius
        return this
    }

    fun setOnlyTopCorner(onlyTopCorner: Boolean) {
        this.onlyTopCorner = onlyTopCorner
    }

    fun getBorderWidth(): Float {
        return borderWidth
    }

    fun setBorderWidth(width: Float): RoundDrawable {
        borderWidth = width
        borderPaint.strokeWidth = borderWidth
        return this
    }

    fun setBorderColor(color: Int): RoundDrawable {
        return setBorderColor(ColorStateList.valueOf(color))
    }

    /**
     * Sets the CircularImageView's basic border color.
     *
     * @param colors a set of state spec / color pairs
     * @see ColorStateList
     *
     *
     * @return The current Drawable that will be used by this drawable. For simple Drawables, this
     * is just the Drawable itself.
     */
    fun setBorderColor(colors: ColorStateList?): RoundDrawable {
        borderColors = colors ?: ColorStateList.valueOf(0)
        borderPaint.color = borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
        return this
    }

    fun isOval(): Boolean {
        return oval
    }

    fun setOval(oval: Boolean): RoundDrawable {
        this.oval = oval
        return this
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @see ImageView.ScaleType
     *
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     *
     * @return Option for scaling the bounds of an image to the bounds of this view
     */
    fun getScaleType(): ImageView.ScaleType {
        return scaleType
    }

    /**
     * Controls how the image should be resized or moved to match the size
     * of this ImageView.
     *
     * @param scaleType The desired scaling mode.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     *
     * @return The current Drawable that will be used by this drawable. For simple Drawables, this
     * is just the Drawable itself.
     */
    fun setScaleType(scaleType: ImageView.ScaleType?): RoundDrawable {
        var scaleType= scaleType
        scaleType ?: ImageView.ScaleType.FIT_CENTER

        if (scaleType != scaleType) {
            scaleType = scaleType!!
            updateShaderMatrix()
        }
        return this
    }

    /**
     * Gets the shader mode for X coordinate
     *
     * @return the shader mode
     * @see Shader.TileMode
     */
    fun getTileModeX(): Shader.TileMode {
        return tileModeX
    }

    /**
     * Sets the shader mode to draw for X coordinate
     *
     * @param tileModeX the shader mode to draw
     * @see Shader.TileMode
     *
     *
     * @return The current Drawable that will be used by this drawable. For simple Drawables, this
     * is just the Drawable itself.
     */
    fun setTileModeX(tileModeX: Shader.TileMode): RoundDrawable {
        if (tileModeX != tileModeX) {
            this.tileModeX = tileModeX
            rebuildShader=true
            invalidateSelf()
        }
        return this
    }

    /**
     * Gets the shader mode for Y coordinate
     *
     * @return the shader mode
     * @see Shader.TileMode
     */
    fun getTileModeY(): Shader.TileMode {
        return tileModeY
    }

    /**
     * Sets the shader mode to draw for Y coordinate
     *
     * @param tileModeY the shader mode to draw
     * @see Shader.TileMode
     *
     *
     * @return The current Drawable that will be used by this drawable. For simple Drawables, this
     * is just the Drawable itself.
     */
    fun setTileModeY(tileModeY: Shader.TileMode): RoundDrawable {
        if (tileModeY != tileModeY) {
            this.tileModeY = tileModeY
            rebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun toBitmap(): Bitmap? {
        return drawableToBitmap(this)
    }

    companion object {
        const val DEFAULT_BORDER_COLOR = Color.BLACK

        /**
         * Create the new RoundDrawable with the given Bitmap
         *
         * @param bitmap the given Bitmap to create the new RoundDrawable
         * @return RoundDrawable
         */
        fun fromBitmap(bitmap: Bitmap?): RoundDrawable? {
            return if (bitmap != null) {
                RoundDrawable(bitmap)
            } else {
                null
            }
        }

        /**
         * Read the drawable after checking the given drawable
         *
         * @param drawable the Drawable to set, or `null` to clear the content
         *
         * @return the checked drawable
         */
        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable is RoundDrawable?) {
                // just return if it's already a RoundDrawable
                return drawable
            } else if (drawable is LayerDrawable?) {
                val num= drawable?.numberOfLayers!!

                // loop through layers to and change to RoundedDrawables if possible
                for (i in 0 until num) {
                    val d= drawable?.getDrawable(i)
                    drawable?.setDrawableByLayerId(drawable.getId(i), fromDrawable(d))
                }
                return drawable
            }

            // try to get a bitmap from the drawable and
            val bm= drawableToBitmap(drawable!!)
            bm ?: return drawable

            return RoundDrawable(bm)
        }

        /**
         * Convert a drawable object into a Bitmap.
         *
         * @param drawable Drawable to extract a Bitmap from.
         *
         * @return A Bitmap created from the drawable parameter.
         */
        private fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            var bitmap: Bitmap?
            val width= Math.max(drawable.intrinsicWidth, 2)
            val height= Math.max(drawable.intrinsicHeight, 2)
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas=Canvas(bitmap!!)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
                bitmap = null
            }

            return bitmap
        }
    }
}