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

package com.goforer.base.presentation.view.customs.indicator

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.goforer.base.presentation.view.customs.indicator.IndicatorHandler.Companion.GO_TO_NEXT
import com.goforer.base.presentation.view.customs.indicator.IndicatorHandler.Companion.GO_TO_PREVIOUS
import com.goforer.base.presentation.view.customs.indicator.IndicatorHandler.Companion.INDICATOR_DISPLAY
import com.goforer.base.presentation.view.customs.indicator.IndicatorHandler.ItemScrollListener
import com.goforer.grabph.R
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class ItemIndicator @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, defStyle: Int = 0)
                                                            : View(context, attr, defStyle), ItemScrollListener {
    private lateinit var indicatorSizes: IntArray

    private lateinit var indicatorAnimators: Array<ValueAnimator>

    private val unselectedPaint = Paint().apply { isAntiAlias = true }
    private val selectedPaint = Paint().apply { isAntiAlias = true }

    private var indicatorBound: Int = 0

    private var duration: Long = 0L

    private var animInterpolator: Interpolator? = null

    private val typedArray: TypedArray by lazy {
        getContext().obtainStyledAttributes(attr, R.styleable.ItemIndicator)
    }

    private var indicatorSize: Int = 0
    private var scrollAmount: Int = 0
    private var initialPadding: Int = 0
    private var indicatorSpacing: Int = 0
    private var indicatorMaxCount: Int = 0

    private var isDefaultColor = false
    private var isSelectedColor = false

    private var indicatorHandler: IndicatorHandler? = null

    private var scrollAnimator: ValueAnimator? = null

    private lateinit var scrollListener: OnScrollListener
    private lateinit var pageChangeListener: OnPageChangeListener

    var count: Int = 0
        set(value) {
            indicatorHandler = IndicatorHandler(value, indicatorMaxCount, indicatorSize, indicatorSpacing, indicatorBound, this)
            indicatorSizes = IntArray(value)
            indicatorHandler?.let {
                it.indicators.forEachIndexed {
                    index, _ -> indicatorSizes[index] = indicatorSize
                }
            }

            indicatorAnimators = Array(value) {
                ValueAnimator()
            }

            initialPadding = when (value) {
                in 0..indicatorMaxCount -> (indicatorBound + (indicatorMaxCount - value) * (indicatorSize + indicatorSpacing) + indicatorSpacing) / 2
                else -> 2 * (indicatorSize + indicatorSpacing)
            }

            invalidate()
        }

    companion object {
        private const val INDICATOR_DEFAULT_SIZE = 4
        private const val INDICATOR_DEFAULT_SPACING = 3
        private const val INDICATOR_DEFAULT_BOUND = 30

        private const val INDICATOR_MAX_COUNT = 4

        private const val MOST_VISIBLE_COUNT = 10
        private const val DEFAULT_ANIM_DURATION = 200

        internal const val SWIPE_DIRECTION_PREVIOUS = 0
        internal const val SWIPE_DIRECTION_NEXT = 1

        private val DEFAULT_INTERPOLATOR by lazy {
            DecelerateInterpolator()
        }
    }

    init {
        indicatorMaxCount = typedArray.getDimensionPixelSize(R.styleable.ItemIndicator_indicator_max_count, INDICATOR_MAX_COUNT.dp)
        indicatorSize = typedArray.getDimensionPixelSize(R.styleable.ItemIndicator_indicator_size, INDICATOR_DEFAULT_SIZE.dp)
        indicatorSpacing = typedArray.getDimensionPixelSize(R.styleable.ItemIndicator_indicator_spacing, INDICATOR_DEFAULT_SPACING.dp)
        unselectedPaint.color = typedArray.getColor(R.styleable.ItemIndicator_unselected_color, ContextCompat.getColor(context, R.color.colorIndicatorSelected))
        selectedPaint.color = typedArray.getColor(R.styleable.ItemIndicator_selected_color, ContextCompat.getColor(context, R.color.colorIndicatorUnselected))
        indicatorBound = typedArray.getDimensionPixelSize(R.styleable.ItemIndicator_indicator_bound, INDICATOR_DEFAULT_BOUND.dp)
        duration = typedArray.getInteger(R.styleable.ItemIndicator_anim_duration, DEFAULT_ANIM_DURATION).toLong()
        animInterpolator = AnimationUtils.loadInterpolator(context, typedArray.getResourceId(R.styleable.ItemIndicator_anim_interpolator, R.anim.indicator_default_interpolator))
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(indicatorMaxCount * (indicatorSize + indicatorSpacing) + indicatorBound, indicatorSize)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var paddingStart = initialPadding
        val (start, end) = getDrawingRange()

        paddingStart += (indicatorSize + indicatorSpacing) * start
        (start until end).forEach {
            canvas?.drawCircle(
                    paddingStart + indicatorSize / 2F - scrollAmount,
                    indicatorSize / 2F,
                    indicatorSizes[it] / 2F,
                    when (indicatorHandler?.indicators?.get(it)) {
                        INDICATOR_DISPLAY -> selectedPaint
                        else -> unselectedPaint
                    })

            paddingStart += indicatorSize + indicatorSpacing
        }
    }

    override fun scrollToItem(item: Int) {
        scrollAnimator?.cancel()
        scrollAnimator = ValueAnimator.ofInt(scrollAmount, item).apply {
            duration = duration
            interpolator = DEFAULT_INTERPOLATOR
            addUpdateListener { animation ->
                scrollAmount = animation.animatedValue as Int
                invalidate()
            }

            start()
        }
    }

    infix fun attachTo(recyclerView: RecyclerView) {
        if (::scrollListener.isInitialized) {
            recyclerView.removeOnScrollListener(scrollListener)
        }

        count = recyclerView.adapter?.itemCount ?: 0
        scrollListener = ScrollListener(this)
        recyclerView.addOnScrollListener(scrollListener)
        scrollToItem(0)
    }

    infix fun attachTo(viewPager: ViewPager) {
        if (::pageChangeListener.isInitialized) {
            viewPager.removeOnPageChangeListener(pageChangeListener)
        }

        count = (viewPager.adapter as PagerAdapter).count
        pageChangeListener = ItemChangeListener(this)
        viewPager.addOnPageChangeListener(pageChangeListener)
        scrollToItem(0)
    }

    infix fun setSelectedColor(color: Int) {
        isSelectedColor = true
        selectedPaint.color = color
    }

    infix fun setDefaultColor(color: Int) {
        isDefaultColor = true
        unselectedPaint.color = color
    }

    infix fun setMaxCount(maxCount: Int) {
        indicatorMaxCount = maxCount
    }

    infix fun setDuration(duration: Long) {
        this.duration = duration
    }

    infix fun setIndicatorSpacing(spacing: Int) {
        indicatorSpacing = spacing.dp
    }

    infix fun setIndicatorSize(size: Int) {
        indicatorSize = size.dp
    }

    internal fun swipe(direction: Int) {
        if (direction == SWIPE_DIRECTION_PREVIOUS) {
            indicatorHandler?.goTo(GO_TO_PREVIOUS)
        } else {
            indicatorHandler?.goTo(GO_TO_NEXT)
        }

        animateIndicators()
    }

    private fun animateIndicators() {
        indicatorHandler?.let {
            val (start, end) = getDrawingRange()
            (start until end).forEach { index ->
                indicatorAnimators[index].cancel()
                indicatorAnimators[index] = ValueAnimator.ofInt(indicatorSizes[index], indicatorSize)
                        .apply {
                            duration = duration
                            interpolator = DEFAULT_INTERPOLATOR
                            addUpdateListener { animation ->
                                indicatorSizes[index] = animation.animatedValue as Int
                                invalidate()
                            }
                        }

                indicatorAnimators[index].start()
            }
        }
    }

    private fun getDrawingRange(): Pair<Int, Int> {
        val start = max(0, (indicatorHandler?.selectedIndex ?: 0) - MOST_VISIBLE_COUNT)
        val end = min(indicatorHandler?.indicators?.size ?: 0, (indicatorHandler?.selectedIndex ?: 0) + MOST_VISIBLE_COUNT)

        return Pair(start, end)
    }

    private inline val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    inner class ScrollListener(private val indicator: ItemIndicator): OnScrollListener() {
        private var midPos = 0
        private var scrollX = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            scrollX += dx
            recyclerView.getChildAt(0)?.width?.let {
                val midPos = floor(((scrollX + it / 2F) / it).toDouble()).toInt()

                if (this.midPos != midPos) {
                    when {
                        this.midPos < midPos -> indicator.swipe(SWIPE_DIRECTION_NEXT)
                        else -> indicator.swipe(SWIPE_DIRECTION_PREVIOUS)
                    }
                }

                this.midPos = midPos
            }
        }
    }

    inner class ItemChangeListener (private val indicator: ItemIndicator): OnPageChangeListener {
        private var selectedPosition = 0

        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            if (position != selectedPosition) {
                when {
                    this.selectedPosition < position -> indicator.swipe(SWIPE_DIRECTION_NEXT)
                    else -> indicator.swipe(SWIPE_DIRECTION_PREVIOUS)
                }
            }

            selectedPosition = position
        }
    }
}