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

package com.goforer.grabph.presentation.ui.searplegallery

object SavedPhoto {
    private val photoFileNames = ArrayList<String>()
    private val photoIds = ArrayList<String>()

    fun getPhotoFileNames(): List<String> {
        return photoFileNames
    }

    fun getPhotoIds(): List<String> {
        return photoIds
    }

    fun setPhotoFileNames(fileNames: List<String>) {
        if (photoFileNames.size > 0) {
            photoFileNames.clear()
        }

        photoFileNames.addAll(fileNames)
    }

    fun setPhotoIds(ids: List<String>) {
        if (photoIds.size > 0) {
            photoIds.clear()
        }

        photoIds.addAll(ids)
    }
}