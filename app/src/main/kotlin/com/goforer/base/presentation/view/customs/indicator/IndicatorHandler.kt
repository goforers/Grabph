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

class IndicatorHandler(count: Int, private val indicatorMaxCount: Int, private val indicatorSize: Int, private val indicatorSpacing: Int,
                       private val indicatorBound: Int, private val listener: ItemScrollListener? = null) {

    internal var indicators = IntArray(count)
    internal var selectedIndex = 0

    private var scrollAmount = 0

    companion object {
        internal const val GO_TO_NEXT = 0
        internal const val GO_TO_PREVIOUS = 1

        internal const val INDICATOR_DISPLAY = 6
        private const val INDICATOR_NOT_DISPLAY = INDICATOR_DISPLAY - 1
    }

    init {
        if (count > 0) {
            indicators[0] = INDICATOR_DISPLAY
        }

        (1 until count).forEach { i -> indicators[i] = INDICATOR_NOT_DISPLAY }
    }

    internal fun goTo(direction: Int) {
        if (direction == GO_TO_NEXT) {
            if (selectedIndex >= indicators.size - 1) {
                return
            }

            ++selectedIndex

            if (indicators.size <= indicatorMaxCount) {
                goToNextWithInMax()
            } else {
                goToNextWithOverMax()
            }
        } else {
            if (selectedIndex == 0) {
                return
            }

            --selectedIndex

            if (indicators.size <= indicatorMaxCount) {
                goToPreviousSmall()
            } else {
                goToPreviousLarge()
            }
        }
    }

    private fun goToNextWithInMax() {
        indicators[selectedIndex] = INDICATOR_DISPLAY
        indicators[selectedIndex - 1] = INDICATOR_NOT_DISPLAY
    }

    private fun goToNextWithOverMax() {
        var needScroll = false

        // swap current indicator and previous indicator
        indicators[selectedIndex] = INDICATOR_DISPLAY
        indicators[selectedIndex - 1] = INDICATOR_NOT_DISPLAY

        // no more than 3 5's in a row backward
        if (selectedIndex > 3
                && indicators[selectedIndex - 1] == INDICATOR_NOT_DISPLAY
                && indicators[selectedIndex - 2] == INDICATOR_NOT_DISPLAY
                && indicators[selectedIndex - 3] == INDICATOR_NOT_DISPLAY
                && indicators[selectedIndex - 4] == INDICATOR_NOT_DISPLAY) {
            indicators[selectedIndex - 4] = INDICATOR_NOT_DISPLAY - 1
            needScroll = true
            if (selectedIndex - INDICATOR_NOT_DISPLAY >= 0) {
                indicators[selectedIndex - 5] = INDICATOR_NOT_DISPLAY - 3
                (selectedIndex - 6 downTo 0).takeWhile { indicators[it] != 0 }
                                            .forEach { indicators[it] = 0 }
            }
        }

        // 6's indicator must around around 3's indicator or higher
        if (selectedIndex + 1 < indicators.size && indicators[selectedIndex + 1] < INDICATOR_NOT_DISPLAY - 2) {
            indicators[selectedIndex + 1] = INDICATOR_NOT_DISPLAY - (INDICATOR_NOT_DISPLAY - 2)
            needScroll = true
            // set the next one to 1 if any
            if (selectedIndex + (INDICATOR_NOT_DISPLAY - 2) < indicators.size && indicators[selectedIndex + 2] < INDICATOR_NOT_DISPLAY - 4) {
                indicators[selectedIndex + 2] = INDICATOR_NOT_DISPLAY - 4
            }
        }

        // Scroll to keep the selected dot within bound
        if (needScroll) {
            val endBound = selectedIndex * (indicatorSize + indicatorSpacing) + indicatorSize
            if (endBound > indicatorBound) {
                scrollAmount = endBound - indicatorBound
                listener?.scrollToItem(scrollAmount)
            }
        }
    }

    private fun goToPreviousSmall() {
        indicators[selectedIndex] = INDICATOR_DISPLAY
        indicators[selectedIndex + 1] = INDICATOR_NOT_DISPLAY
    }

    private fun goToPreviousLarge() {
        var needScroll = false
        // swap 6 and 5
        indicators[selectedIndex] = INDICATOR_DISPLAY
        indicators[selectedIndex + 1] = INDICATOR_NOT_DISPLAY

        // no more than 3's indicator and 5's indicator in a row backward
        if (selectedIndex < indicators.size - (INDICATOR_NOT_DISPLAY - 1)
                && indicators[selectedIndex + 1] == INDICATOR_NOT_DISPLAY
                && indicators[selectedIndex + 2] == INDICATOR_NOT_DISPLAY
                && indicators[selectedIndex + 3] == INDICATOR_NOT_DISPLAY
                && indicators[selectedIndex + 4] == INDICATOR_NOT_DISPLAY) {
            indicators[selectedIndex + 4] = INDICATOR_NOT_DISPLAY - 1
            needScroll = true
            if (selectedIndex + 5 < indicators.size) {
                indicators[selectedIndex + 5] = INDICATOR_NOT_DISPLAY - 3
                (selectedIndex + 6 until indicators.size)
                        .takeWhile { indicators[it] != 0 }
                        .forEach { i -> indicators[i] = 0 }
            }
        }

        if (selectedIndex - 1 >= 0 && indicators[selectedIndex - 1] <  INDICATOR_NOT_DISPLAY - 2) {
            needScroll = true
            indicators[selectedIndex - 1] = INDICATOR_NOT_DISPLAY - 2
            // set the next one to 1 if any
            if (selectedIndex - (INDICATOR_NOT_DISPLAY - 3) >= 0 && indicators[selectedIndex - 2] < INDICATOR_NOT_DISPLAY - 4) {
                indicators[selectedIndex - 2] = INDICATOR_NOT_DISPLAY - 4
            }
        }

        if (needScroll) {
            val startBound = selectedIndex * (indicatorSize + indicatorSpacing)
            if (startBound < scrollAmount) {
                scrollAmount = selectedIndex * (indicatorSize + indicatorSpacing)
                listener?.scrollToItem(scrollAmount)
            }
        }
    }

    interface ItemScrollListener {
        fun scrollToItem(item: Int)
    }
}