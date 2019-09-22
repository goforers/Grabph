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

package com.goforer.grabph.presentation.common.effect.transition

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.transition.TransitionValues
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView

/**
 * Constructor used from XML.
 */
class TextResize(context: Context, attrs: AttributeSet): Transition(context, attrs) {
    init {
        addTarget(AppCompatTextView::class.java)
    }

    override fun getTransitionProperties(): Array<String> {
        return PROPERTIES
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        if (transitionValues.view !is AppCompatTextView) {
            return
        }

        val view = transitionValues.view as AppCompatTextView
        val fontSize = view.textSize
        transitionValues.values[FONT_SIZE] = fontSize
        val data = TextResizeData(view)
        transitionValues.values[DATA] = data
    }

    @SuppressLint("AnimatorKeep")
    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?,
                                endValues: TransitionValues?): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }

        val startData = startValues.values[DATA] as TextResizeData
        val endData = endValues.values[DATA] as TextResizeData
        if (startData.gravity != endData.gravity) {
            return null // Can't deal with changes in gravity
        }

        val textView = endValues.view as AppCompatTextView
        var startFontSize = startValues.values[FONT_SIZE] as Float
        // Capture the start bitmap -- we need to set the values to the start values first
        setTextViewData(textView, startData, startFontSize)
        val startWidth = textView.paint.measureText(textView.text.toString())

        val startBitmap = captureTextBitmap(textView)

        if (startBitmap == null) {
            startFontSize = 0f
        }

        var endFontSize = endValues.values[FONT_SIZE] as Float

        // Set the values to the end values
        setTextViewData(textView, endData, endFontSize)

        val endWidth = textView.paint.measureText(textView.text.toString())

        // Capture the end bitmap
        val endBitmap = captureTextBitmap(textView)
        if (endBitmap == null) {
            endFontSize = 0f
        }

        if (startFontSize == 0f && endFontSize == 0f) {
            return null // Can't animate null bitmaps
        }

        // Set the colors of the TextView so that nothing is drawn.
        // Only draw the bitmaps in the overlay.
        val textColors = textView.textColors
        val hintColors = textView.hintTextColors
        val highlightColor = textView.highlightColor
        val linkColors = textView.linkTextColors
        textView.setTextColor(Color.TRANSPARENT)
        textView.setHintTextColor(Color.TRANSPARENT)
        textView.highlightColor = Color.TRANSPARENT
        textView.setLinkTextColor(Color.TRANSPARENT)

        // Create the drawable that will be animated in the TextView's overlay.
        // Ensure that it is showing the start state now.
        val drawable = SwitchBitmapDrawable(textView, startData.gravity,
                startBitmap!!, startFontSize, startWidth, endBitmap!!, endFontSize, endWidth)
        textView.overlay.add(drawable)

        // Properties: left, top, font size, text color
        val leftProp = PropertyValuesHolder.ofFloat("left",
                                    startData.paddingLeft.toFloat(), endData.paddingLeft.toFloat())
        val topProp = PropertyValuesHolder.ofFloat("top",
                                    startData.paddingTop.toFloat(), endData.paddingTop.toFloat())
        val rightProp = PropertyValuesHolder.ofFloat("right",
                            startData.width.toFloat() - startData.paddingRight.toFloat(),
                                        endData.width.toFloat() - endData.paddingRight.toFloat())
        val bottomProp = PropertyValuesHolder.ofFloat("bottom",
                            startData.height.toFloat() - startData.paddingBottom.toFloat(),
                                        endData.height.toFloat() - endData.paddingBottom.toFloat())
        val fontSizeProp = PropertyValuesHolder.ofFloat("fontSize", startFontSize, endFontSize)
        val animator: ObjectAnimator
        animator = if (startData.textColor != endData.textColor) {
            val textColorProp = PropertyValuesHolder.ofObject("textColor",
                    ArgbEvaluator(), startData.textColor, endData.textColor)
            ObjectAnimator.ofPropertyValuesHolder(drawable,
                    leftProp, topProp, rightProp, bottomProp, fontSizeProp, textColorProp)
        } else {
            ObjectAnimator.ofPropertyValuesHolder(drawable,
                    leftProp, topProp, rightProp, bottomProp, fontSizeProp)
        }

        val finalFontSize = endFontSize
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                textView.overlay.remove(drawable)
                textView.setTextColor(textColors)
                textView.setHintTextColor(hintColors)
                textView.highlightColor = highlightColor
                textView.setLinkTextColor(linkColors)
            }

            override fun onAnimationPause(animation: Animator) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, drawable.fontSize)
                val paddingLeft = Math.round(drawable.left)
                val paddingTop = Math.round(drawable.top)
                val fraction = animator.animatedFraction
                val paddingRight = Math.round(interpolate(startData.paddingRight.toFloat(),
                        endData.paddingRight.toFloat(), fraction))
                val paddingBottom = Math.round(interpolate(startData.paddingBottom.toFloat(),
                        endData.paddingBottom.toFloat(), fraction))
                textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                textView.setTextColor(drawable.textColor)
            }

            override fun onAnimationResume(animation: Animator) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalFontSize)
                textView.setPadding(endData.paddingLeft, endData.paddingTop,
                        endData.paddingRight, endData.paddingBottom)
                textView.setTextColor(endData.textColor)
            }
        }
        animator.addListener(listener)
        animator.addPauseListener(listener)
        return animator
    }

    /**
     * This Drawable is used to scale the start and end bitmaps and switch between them
     * at the appropriate progress.
     */
    private class SwitchBitmapDrawable(private val view: AppCompatTextView, gravity: Int,
                                       private val startBitmap: Bitmap, private val startFontSize: Float, private val startWidth: Float,
                                       private val endBitmap: Bitmap, private val endFontSize: Float, private val endWidth: Float) : Drawable() {
        private val horizontalGravity: Int = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
        private val verticalGravity: Int = gravity and Gravity.VERTICAL_GRAVITY_MASK
        private val paint = Paint()
        /**
         * @return The font size of the text in the displayed bitmap.
         */
        /**
         * Sets the font size that the text should be displayed at.
         *
         * @param fontSize The font size in pixels of the scaled bitmap text.
         */
        var fontSize: Float = 0.toFloat()
            set(fontSize) {
                field = fontSize
                invalidateSelf()
            }
        /**
         * @return The left side of the text.
         */
        /**
         * Sets the left side of the text. This should be the same as the left padding.
         *
         * @param left The left side of the text in pixels.
         */
        var left: Float = 0.toFloat()
            set(left) {
                field = left
                invalidateSelf()
            }
        /**
         * @return The top of the text.
         */
        /**
         * Sets the top of the text. This should be the same as the top padding.
         *
         * @param top The top of the text in pixels.
         */
        var top: Float = 0.toFloat()
            set(top) {
                field = top
                invalidateSelf()
            }
        /**
         * @return The right side of the text.
         */
        /**
         * Sets the right of the drawable.
         *
         * @param right The right pixel of the drawn area.
         */
        var right: Float = 0.toFloat()
            set(right) {
                field = right
                invalidateSelf()
            }
        /**
         * @return The bottom of the text.
         */
        /**
         * Sets the bottom of the drawable.
         *
         * @param bottom The bottom pixel of the drawn area.
         */
        var bottom: Float = 0.toFloat()
            set(bottom) {
                field = bottom
                invalidateSelf()
            }
        /**
         * @return The color of the text being displayed.
         */
        /**
         * Sets the color of the text to be displayed.
         *
         * @param textColor The color of the text to be displayed.
         */
        var textColor: Int = 0
            set(textColor) {
                field = textColor
                setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
                invalidateSelf()
            }

        override fun invalidateSelf() {
            super.invalidateSelf()
            view.invalidate()
        }

        override fun draw(canvas: Canvas) {
            val saveCount = canvas.save()
            // The threshold changes depending on the target font sizes. Because scaled-up
            // fonts look bad, we want to switch when closer to the smaller font size. This
            // algorithm ensures that null bitmaps (font size = 0) are never used.
            val threshold = startFontSize / (startFontSize + endFontSize)
            val fontSize = fontSize
            val progress = (fontSize - startFontSize) / (endFontSize - startFontSize)

            // The drawn text width is a more accurate scale than font size. This avoids
            // jump when switching bitmaps.
            val expectedWidth = interpolate(startWidth, endWidth, progress)
            if (progress < threshold) {
                // draw start bitmap
                val scale = expectedWidth / startWidth
                val tx = getTranslationPoint(horizontalGravity, this.left, this.right,
                        startBitmap.width.toFloat(), scale)
                val ty = getTranslationPoint(verticalGravity, this.top, this.bottom,
                        startBitmap.height.toFloat(), scale)
                canvas.translate(tx, ty)
                canvas.scale(scale, scale)
                canvas.drawBitmap(startBitmap, 0f, 0f, paint)
            } else {
                // draw end bitmap
                val scale = expectedWidth / endWidth
                val tx = getTranslationPoint(horizontalGravity, this.left, this.right,
                        endBitmap.width.toFloat(), scale)
                val ty = getTranslationPoint(verticalGravity, this.top, this.bottom,
                        endBitmap.height.toFloat(), scale)
                canvas.translate(tx, ty)
                canvas.scale(scale, scale)
                canvas.drawBitmap(endBitmap, 0f, 0f, paint)
            }
            canvas.restoreToCount(saveCount)
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }

        @SuppressLint("RtlHardcoded")
        private fun getTranslationPoint(gravity: Int, start: Float, end: Float, dim: Float,
                                        scale: Float): Float {
            return when (gravity) {
                Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL -> (start + end - dim * scale) / 2f
                Gravity.RIGHT, Gravity.BOTTOM -> end - dim * scale
                Gravity.LEFT, Gravity.TOP -> start
                else -> start
            }
        }
    }

    /**
     * Contains all the non-font-size data used by the TextResize transition.
     * None of these values should trigger the transition, so they are not listed
     * in PROPERTIES. These are captured together to avoid boxing of all the
     * primitives while adding to TransitionValues.
     */
    internal class TextResizeData(textView: AppCompatTextView) {
        val paddingLeft: Int = textView.paddingLeft
        val paddingTop: Int = textView.paddingTop
        val paddingRight: Int = textView.paddingRight
        val paddingBottom: Int = textView.paddingBottom
        val width: Int = textView.width
        val height: Int = textView.height
        val gravity: Int = textView.gravity
        val textColor: Int = textView.currentTextColor
    }

    companion object {
        private const val FONT_SIZE = "TextResize:fontSize"
        private const val DATA = "TextResize:data"

        private val PROPERTIES = arrayOf(
                // We only care about FONT_SIZE. If anything else changes, we don't
                // want this transition to be called to create an Animator.
                FONT_SIZE)

        private fun setTextViewData(view: AppCompatTextView, data: TextResizeData, fontSize: Float) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            view.setPadding(data.paddingLeft, data.paddingTop, data.paddingRight, data.paddingBottom)
            view.right = view.left + data.width
            view.bottom = view.top + data.height
            view.setTextColor(data.textColor)
            val widthSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
            view.measure(widthSpec, heightSpec)
            view.layout(view.left, view.top, view.right, view.bottom)
        }

        private fun captureTextBitmap(textView: AppCompatTextView): Bitmap? {
            val background = textView.background
            textView.background = null
            val width = textView.width - textView.paddingLeft - textView.paddingRight
            val height = textView.height - textView.paddingTop - textView.paddingBottom
            if (width == 0 || height == 0) {
                return null
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.translate((-textView.paddingLeft).toFloat(), (-textView.paddingTop).toFloat())
            textView.draw(canvas)
            textView.background = background

            return bitmap
        }

        private fun interpolate(start: Float, end: Float, fraction: Float): Float {
            return start + fraction * (end - start)
        }
    }
}
