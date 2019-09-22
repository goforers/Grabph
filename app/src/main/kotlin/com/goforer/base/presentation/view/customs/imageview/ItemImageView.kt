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
import android.util.AttributeSet
import android.util.DisplayMetrics

class ItemImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? =null): ForegroundImageView(context, attrs) {
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var screenDensity: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val multiple: Float

        var width: Double
        val height: Double

        when (screenDensity) {
            DisplayMetrics.DENSITY_LOW ->
                // LDPI
                multiple=0.55f
            DisplayMetrics.DENSITY_MEDIUM ->
                // MDPI
                multiple=0.95f
            DisplayMetrics.DENSITY_TV, DisplayMetrics.DENSITY_HIGH ->
                // HDPI
                multiple=1.65f
            DisplayMetrics.DENSITY_XHIGH, DisplayMetrics.DENSITY_280 ->
                // XHDPI
                multiple=2.45f
            DisplayMetrics.DENSITY_XXHIGH, DisplayMetrics.DENSITY_360, DisplayMetrics.DENSITY_400, DisplayMetrics.DENSITY_420 ->
                // XXHDPI
                multiple=2.95f
            DisplayMetrics.DENSITY_XXXHIGH, DisplayMetrics.DENSITY_560 ->
                // XXXHDPI
                multiple=3.8f
            else -> multiple=1.0f
        }

        if (imageHeight > imageWidth && imageWidth < 200) {
            width = (MeasureSpec.getSize(imageWidth) * multiple).toDouble()
            val result= imageHeight - imageWidth
            val rate: Float
            rate = when {
                result < 60 -> 1.05f
                result < 90 -> 1.4f
                result < 120 -> 1.8f
                result < 180 -> 2.25f
                result < 210 -> 2.8f
                else -> 3.3f
            }

            width *= rate
        } else {
            width = (MeasureSpec.getSize(imageWidth) * multiple).toDouble()
        }

        height = (MeasureSpec.getSize(imageHeight) * multiple).toDouble()

        super.onMeasure(MeasureSpec.makeMeasureSpec(width.toInt(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY))
    }
}