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

package com.goforer.grabph.presentation.common.utils.handler.watermark

import android.content.Context
import android.graphics.*
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class WatermarkHandler @Inject constructor() {
    fun putWatermark(context: Context, workHandler: CommonWorkHandler, bitmap: Bitmap, title: String?, description: String?) {
        val textPoint = Point(bitmap.width - (30 * context.resources.displayMetrics.density).toInt(),
                bitmap.height - (9 * context.resources.displayMetrics.density).toInt())
        val watermark = insertWaterMarkAsLogo(context, bitmap)
        val watermarkText
                = insertWatermarkAsText(watermark, "Searp", textPoint, Color.WHITE, 255,
                                            (7 * context.resources.displayMetrics.density).toInt(), false)
        workHandler.showAppListToShare(context, title, description, watermarkText)
    }

    private fun insertWaterMarkAsLogo(context: Context, src: Bitmap): Bitmap {
        val canvas: Canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)
        val watermark = BitmapFactory.decodeResource(context.resources, R.drawable.ic_searp_logo)
        val matrix = Matrix()
        val scale: Float
        val rectF: RectF
        val width = src.width
        val height = src.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        canvas = Canvas(bitmap)
        canvas.drawBitmap(src, 0.toFloat(), 0.toFloat(), paint)
        scale = (height.toFloat() * 0.15 / watermark.height.toFloat()).toFloat()
        matrix.postScale(scale, scale)
        // Determine the post-scaled size of the watermark
        rectF = RectF(0f, 0f, watermark.width.toFloat(), watermark.height.toFloat())
        matrix.mapRect(rectF)
        matrix.postTranslate(width/2 - rectF.width()/2, height/2 - rectF.height()/2)
        canvas.drawBitmap(watermark, matrix, paint)
        // Free up the bitmap memory
        watermark.recycle()

        return bitmap
    }

    private fun insertWatermarkAsText(src: Bitmap, watermark: String, location: Point, color: Int,
                                      alpha: Int, size: Int, underline: Boolean): Bitmap {
        val width = src.width
        val height = src.height
        val result = Bitmap.createBitmap(width, height, src.config)
        val canvas = Canvas(result)
        val paint = Paint()

        canvas.drawBitmap(src, 0.toFloat(), 0.toFloat(), null)
        paint.color = color
        paint.alpha = alpha
        paint.textSize = size.toFloat()
        paint.isAntiAlias = true
        paint.isUnderlineText = underline
        canvas.drawText(watermark, location.x.toFloat(), location.y.toFloat(), paint)

        return result
    }
}