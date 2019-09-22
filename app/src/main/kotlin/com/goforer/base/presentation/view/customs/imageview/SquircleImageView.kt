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

@file:Suppress("KDocUnresolvedReference", "DEPRECATION", "NAME_SHADOWING")

package com.goforer.base.presentation.view.customs.imageview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.goforer.base.presentation.utils.CommonUtils.getVersionNumber
import com.goforer.base.presentation.view.customs.drawable.RoundDrawable
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.cache.IntegerVersionSignature

class SquircleImageView: AppCompatImageView {
    private var defaultCornerRadius = DEFAULT_RADIUS
    private var borderWidth = DEFAULT_BORDER_WIDTH

    /**
     * Gets a set of state spec / color pairs
     *
     * @return a set of state spec / color pairs
     * @see ColorStateList
     */
    private var borderColors: ColorStateList? =ColorStateList.valueOf(RoundDrawable.DEFAULT_BORDER_COLOR)

    private var isDefaultOval = false
    private var mutateBackground = false
    private var onlyTopCorner = false
    private var hasColorFilter = false
    private var colorMod = false

    /**
     * Gets the shader mode for X coordinate
     *
     * @return the shader mode
     * @see Shader.TileMode
     */
    /**
     * Sets the shader mode to draw for X coordinate
     *
     * @param tileModeX the shader mode to draw
     * @see Shader.TileMode
     */
    private var tileModeX: Shader.TileMode ? = DEFAULT_TILE_MODE
        set(tileModeX) {
            if (this.tileModeX == tileModeX) {
                return
            }

            field=tileModeX
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    /**
     * Gets the shader mode for Y coordinate
     *
     * @return the shader mode
     * @see Shader.TileMode
     */
    /**
     * Sets the shader mode to draw for Y coordinate
     *
     * @param tileModeY the shader mode to draw
     * @see Shader.TileMode
     */
    private var tileModeY: Shader.TileMode ? = DEFAULT_TILE_MODE
        set(tileModeY) {
            if (this.tileModeY == tileModeY) {
                return
            }

            field = tileModeY
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }

    private var mColorFilter: ColorFilter? =null

    private var resource: Int = 0
    private var mDrawable: Drawable ? = null
    private var backgroundDrawable: Drawable ? = null

    private var scaleType: ScaleType ? = null
    private var defaultImage: Drawable ? = null

    private var cornerRadius: Float
        get() = defaultCornerRadius
        set(radius) {
            if (defaultCornerRadius == radius) {
                return
            }

            defaultCornerRadius = radius
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }

    /**
     * Gets the SquircleImageView's basic border color
     *
     * @return the the SquircleImageView's basic border color
     */
    /**
     * Sets the SquircleImageView's basic border color.
     *
     * @param color The new color (including alpha) to set the border.
     */
    var borderColor: Int
        get() = borderColors!!.defaultColor
        set(color) = setBorderColor(ColorStateList.valueOf(color))

    private var isOval: Boolean
        get() = isDefaultOval
        set(oval) {
            isDefaultOval = oval
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }

    constructor(context: Context) : super(context)

    @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyle: Int=0) : super(context, attrs, defStyle) {
        val a= context.obtainStyledAttributes(attrs, R.styleable.SquircleImageView, defStyle, 0)
        val index= a.getInt(R.styleable.SquircleImageView_android_scaleType, -1)

        scaleType = if (index >= 0) {
            SCALE_TYPES[index]
        } else {
            ScaleType.FIT_CENTER
        }

        defaultCornerRadius = a.getDimensionPixelSize(R.styleable.SquircleImageView_riv_corner_radius, -1).toFloat()
        borderWidth = a.getDimensionPixelSize(R.styleable.SquircleImageView_riv_border_width, -1).toFloat()

        if (defaultCornerRadius < 0) {
            defaultCornerRadius = DEFAULT_RADIUS
        }
        if (borderWidth < 0) {
            borderWidth = DEFAULT_BORDER_WIDTH
        }

        borderColors = a.getColorStateList(R.styleable.SquircleImageView_riv_border_color)
        if (borderColors == null) {
            borderColors=ColorStateList.valueOf(RoundDrawable.DEFAULT_BORDER_COLOR)
        }

        mutateBackground = a.getBoolean(R.styleable.SquircleImageView_riv_mutate_background, false)
        isOval = a.getBoolean(R.styleable.SquircleImageView_riv_oval, false)

        onlyTopCorner = a.getBoolean(R.styleable.SquircleImageView_only_top_corner, false)

        val tileMode= a.getInt(R.styleable.SquircleImageView_riv_tile_mode, TILE_MODE_UNDEFINED)

        if (tileMode != TILE_MODE_UNDEFINED) {
            tileModeX = parseTileMode(tileMode)
            tileModeY = parseTileMode(tileMode)
        }

        /*
        var tileModeX= a.getInt(R.styleable.SquircleImageView_riv_tile_mode_x, TILE_MODE_UNDEFINED)
        if (tileModeX != TILE_MODE_UNDEFINED) {
            tileModeX = parseTileMode(tileModeX)
        }

        var tileModeY=a.getInt(R.styleable.SquircleImageView_riv_tile_mode_y, TILE_MODE_UNDEFINED)
        if (tileModeY != TILE_MODE_UNDEFINED) {
            tileModeY = parseTileMode(tileModeY)
        }
        */

        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(true)
        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        invalidate()
    }

    override fun getScaleType(): ScaleType? {
        return scaleType
    }

    override fun setScaleType(scaleType: ScaleType?) {
        assert(scaleType != null)

        if (scaleType != scaleType) {
            this.scaleType = scaleType

            when (scaleType) {
                ScaleType.CENTER, ScaleType.CENTER_CROP,
                ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER,
                ScaleType.FIT_START, ScaleType.FIT_END,
                ScaleType.FIT_XY -> super.setScaleType(ScaleType.FIT_XY)
                else -> super.setScaleType(scaleType)
            }

            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    }

    /**
     * Sets the content of this ImageView to the default bitmap image
     *
     * @param defaultImage the Drawable to set, or `null` to clear the content
     */
    private fun setDefaultImage(defaultImage: Drawable) {
        this.defaultImage = defaultImage
    }

    fun loadImage(url: String) {
        val options= RequestOptions()
                .placeholder(defaultImage)
                .error(defaultImage)

        Glide.with(context)
                .load(url)
                .apply(options)
                .into(this)
    }

    /**
     * Sets the content of this ImageView to the specified Image.
     *
     *
     *
     * Set the target the resource will be loaded into.
     *
     *
     * @param imageUrl the desired Image URL
     */
    fun setImage(imageUrl: String?) {
        imageUrl ?: setImageDrawable(defaultImage)

        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(RequestOptions.fitCenterTransform())
                .apply(RequestOptions().signature(IntegerVersionSignature(getVersionNumber())))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .listener(object: RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        setImageBitmap(resource)

                        return false
                    }
                })
                .submit()
    }

    /**
     * Sets the content of this ImageView to the specified Image.
     *
     *
     *
     * Sets some additional data to be mixed in to the memory and disk cache keys allowing
     * the caller more control over when cached data is invalidated.
     *
     *
     *
     *
     * Note - The signature does not replace the cache key, it is purely additive.
     *
     *
     * @param imageUrl the desired Image URL
     */
    fun setImageNewCache(imageUrl: String?) {
        imageUrl ?: setImageDrawable(defaultImage)

        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(RequestOptions.fitCenterTransform())
                .apply(RequestOptions().signature(IntegerVersionSignature(getVersionNumber())))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .listener(object: RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        setImageBitmap(resource)

                        return false
                    }
                })
                .submit()
    }

    /**
     * Sets the content of this ImageView to the specified Url.
     *
     * @param url the Url of an image
     */
    fun setImageUrl(url: String) {
        if (url.isNotEmpty()) {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(RequestOptions.fitCenterTransform())
                    .apply(RequestOptions().signature(IntegerVersionSignature(getVersionNumber())))
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .listener(object: RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                                  isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                     dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            setImageBitmap(resource)

                            return false
                        }
                    })
                    .submit()
        }
    }

    /**
     * Sets the content of this ImageView to the specified Image.
     *
     * @param imageUrl the desired Image URL
     * @param resizeWidth the width to resize
     * @param resizeHeight the height to resize
     * @param defaultImage the default image
     */
    fun setImage(imageUrl: String, resizeWidth: Int, resizeHeight: Int, defaultImage: Drawable) {
        setDefaultImage(defaultImage)
        setImage(imageUrl, resizeWidth, resizeHeight)
    }

    /**
     * Sets the content of this ImageView to the specified Image.
     *
     * @param imageUrl the desired Image URL
     * @param resizeWidth the width to resize
     * @param resizeHeight the height to resize
     */
    private fun setImage(imageUrl: String?, resizeWidth: Int, resizeHeight: Int) {
        imageUrl ?: setImageDrawable(defaultImage)
        val options= RequestOptions().override(resizeWidth, resizeHeight)

        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(options)
                .apply(RequestOptions.fitCenterTransform())
                .apply(RequestOptions().signature(IntegerVersionSignature(getVersionNumber())))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .listener(object: RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        var resource= resource
                        var scale = 1f
                        val width= resource?.width
                        val height= resource?.height
                        val max= Math.max(width!!, height!!).toFloat()
                        if (max > 1024) {
                            scale = 1024 / max
                        }
                        if (scale < 1f) {
                            val matrix = Matrix()
                            matrix.postScale(scale, scale)
                            resource = resource?.let {
                                Bitmap.createBitmap(it, 0, 0, width, height, matrix, true)
                            }
                        }

                        setImageBitmap(resource)

                        return false
                    }
                })
                .submit()
    }

    /**
     * Sets the content of this ImageView to the specified Image.
     *
     * @param imageUrl the desired Image URL
     * @param resId the resource identifier of the drawable
     */
    fun setImage(imageUrl: String?, @DrawableRes resId: Int) {
        setImageResource(resId)

        Handler(Looper.myLooper()).post {
            imageUrl?.let {
                Glide.with(context)
                        .asBitmap()
                        .load(it)
                        .apply(RequestOptions.fitCenterTransform())
                        .apply(RequestOptions().signature(IntegerVersionSignature(getVersionNumber())))
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .listener(object: RequestListener<Bitmap> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                                      isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                         dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                setImageBitmap(resource)

                                return false
                            }
                        })
                        .submit()
            }
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        resource = 0
        mDrawable = RoundDrawable.fromDrawable(drawable)
        updateDrawableAttrs()

        super.setImageDrawable(mDrawable)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        resource = 0
        mDrawable = RoundDrawable.fromBitmap(bm)
        updateDrawableAttrs()

        super.setImageDrawable(mDrawable)
    }

    override fun setImageResource(resId: Int) {
        if (resource != resId) {
            resource = resId
            mDrawable = resolveResource()
            updateDrawableAttrs()

            super.setImageDrawable(mDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)

        setImageDrawable(drawable)
    }

    private fun resolveResource(): Drawable? {
        val rsrc= resources ?: return null
        var drawable: Drawable ? = null

        if (resource != 0) {
            try {
                drawable = rsrc.getDrawable(resource)
            } catch (e: Exception) {
                resource = 0
            }
        }

        return RoundDrawable.fromDrawable(drawable)
    }

    override fun setBackground(background: Drawable) {
        setBackgroundDrawable(background)
    }

    /**
     * Re-initializes the Drawable-attribute used to fill in
     * the square upon drawing.
     */
    private fun updateDrawableAttrs() {
        updateAttrs(mDrawable)
    }

    /**
     * Re-initializes the Drawable-attribute of background used to fill in
     * the square upon drawing.
     *
     * @param convert get the background-drawable from RoundDrawable class if the convert is true
     */
    private fun updateBackgroundDrawableAttrs(convert: Boolean) {
        if (mutateBackground) {
            if (convert) {
                backgroundDrawable = RoundDrawable.fromDrawable(backgroundDrawable)
            }

            updateAttrs(backgroundDrawable)
        }
    }

    override fun setColorFilter(cf: ColorFilter) {
        if (mColorFilter !== cf) {
            mColorFilter = cf
            hasColorFilter = true
            colorMod = true
            applyColorMod()
            invalidate()
        }
    }

    private fun applyColorMod() {
        if (mDrawable != null && colorMod) {
            mDrawable = mDrawable!!.mutate()
            if (hasColorFilter) {
                mDrawable!!.colorFilter = mColorFilter
            }
        }
    }

    private fun updateAttrs(drawable: Drawable?) {
        drawable?.let {
            if (it is RoundDrawable) {
                it.setScaleType(scaleType)
                        .setCornerRadius(defaultCornerRadius)
                        .setBorderWidth(borderWidth)
                        .setBorderColor(borderColors)
                        .setOval(isOval)
                        .setOval(isOval)
                        .setTileModeX(tileModeX!!)
                        .setTileModeY(tileModeY!!)
                        .setOnlyTopCorner(onlyTopCorner)
                applyColorMod()
            } else if (it is LayerDrawable) {
                val ld=it as LayerDrawable?
                var i=0
                val layers=ld!!.numberOfLayers

                while (i < layers) {
                    updateAttrs(ld.getDrawable(i))
                    i++
                }
            }
        }
    }

    @Deprecated("")
    override fun setBackgroundDrawable(background: Drawable) {
        backgroundDrawable = background
        updateBackgroundDrawableAttrs(true)

        super.setBackgroundDrawable(backgroundDrawable)
    }

    private fun setCornerRadiusDimension(resId: Int) {
        cornerRadius=resources.getDimension(resId)
    }

    private fun getBorderWidth(): Float {
        return borderWidth
    }

    private fun setBorderWidth(resId: Int) {
        setBorderWidth(resources.getDimension(resId))
    }

    private fun setBorderWidth(width: Float) {
        if (borderWidth == width) {
            return
        }

        borderWidth = width
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    /**
     * Sets the CircularImageView's basic border color.
     *
     * @param colors a set of state spec / color pairs
     * @see ColorStateList
     */
    private fun setBorderColor(colors: ColorStateList?) {
        if (borderColors == colors) {
            return
        }

        borderColors = colors ?: ColorStateList.valueOf(RoundDrawable.DEFAULT_BORDER_COLOR)
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        if (borderWidth > 0) {
            invalidate()
        }
    }

    companion object {
        private const val TILE_MODE_UNDEFINED = -2
        private const val TILE_MODE_CLAMP = 0
        private const val TILE_MODE_REPEAT = 1
        private const val TILE_MODE_MIRROR = 2

        const val DEFAULT_RADIUS = 0f
        const val DEFAULT_BORDER_WIDTH = 0f

        val DEFAULT_TILE_MODE: Shader.TileMode=Shader.TileMode.CLAMP

        private val SCALE_TYPES = arrayOf(
                ScaleType.MATRIX, ScaleType.FIT_XY, ScaleType.FIT_START,
                ScaleType.FIT_CENTER, ScaleType.FIT_END, ScaleType.CENTER,
                ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE)

        private fun parseTileMode(tileMode: Int): Shader.TileMode {
            return when (tileMode) {
                TILE_MODE_CLAMP -> Shader.TileMode.CLAMP
                TILE_MODE_REPEAT -> Shader.TileMode.REPEAT
                TILE_MODE_MIRROR -> Shader.TileMode.MIRROR
                else -> {
                    Shader.TileMode.CLAMP
                }
            }
        }
    }
}