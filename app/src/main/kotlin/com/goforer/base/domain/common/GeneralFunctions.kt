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

package com.goforer.base.domain.common

import java.util.*

object GeneralFunctions {
    fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }

        return maxSize
    }

    fun getFirstVisibleItem(firstVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in firstVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = firstVisibleItemPositions[i]
            } else if (firstVisibleItemPositions[i] > maxSize) {
                maxSize = firstVisibleItemPositions[i]
            }
        }

        return maxSize
    }

    fun rand(from: Int, to: Int) : Int {
        return Random().nextInt(to - from) + from
    }

    fun removeLastCharRegex(text: String?): String? {
        return text?.replace(".$".toRegex(), "")
    }

    // This function is for loading the image into DrawerHead as background image temporarily
    // After backend server will be set up, this function will be erased....
    fun getHeaderBackgroundUrl(): String {
        val baseUrl = "https://raw.githubusercontent.com/Lukoh/Grabph_Header_Image/master/Background/header_background_image"
        val baseRestUrl = ".jpg"

        return baseUrl + GeneralFunctions.rand(1, 62).toString() + baseRestUrl
    }

    fun getSplashBackgroundUrl(): String {
        val baseUrl = "https://raw.githubusercontent.com/Lukoh/Grabph_Splash/master/grabph_splash_image"
        val baseRestUrl = ".jpg"
        val list = arrayListOf(1,4,8,9); list.shuffle()
        return baseUrl + list[0].toString() + baseRestUrl
    }
}