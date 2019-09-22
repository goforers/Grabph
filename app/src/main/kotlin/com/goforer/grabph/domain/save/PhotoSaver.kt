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

@file:Suppress("DEPRECATION")

package com.goforer.grabph.domain.save

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Environment
import com.goforer.grabph.presentation.event.action.UpdatePhotoListAction
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoSaver @Inject constructor(){
    companion object {
        const val SAVER_PHOTO_EXIST = 0
        const val SAVER_PHOTO_SAVE_SUCCESS = 1
        const val SAVER_PHOTO_SAVE_FAIL = 2

        const val FROM_FEED_ITEM = 0
        const val FROM_MAIN = 1
        const val FROM_PHOTOG_PHOTO = 2
        const val FROM_PHOTO_INFO = 3
        const val FROM_FEED_INFO = 4
    }

    @SuppressLint("ObsoleteSdkInt")
    fun savePhoto(bitmap: Bitmap, filename: String, folderName: String, fromWhere: Int,
                  position: Int): Int {

        val directory = File(Environment.getExternalStorageDirectory().toString()
                + File.separator + folderName)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val pictureFile = File(directory, filename)
        try {
            if (pictureFile.exists()) {
                return SAVER_PHOTO_EXIST
            } else {
                pictureFile.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Timber.d(e)
            return SAVER_PHOTO_SAVE_FAIL
        }

        try {
            val out = FileOutputStream(pictureFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()

            return SAVER_PHOTO_SAVE_FAIL
        }

        val action = UpdatePhotoListAction()
        action.fromWhere = fromWhere
        action.position = position
        action.fileName = filename
        action.pictureFile = pictureFile
        EventBus.getDefault().post(action)

        return SAVER_PHOTO_SAVE_SUCCESS
    }
}