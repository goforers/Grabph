/*
 * Copyright (C) 2018 Lukoh Nam, goForer
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

package com.goforer.grabph.presentation.ui.feed.common

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.domain.save.PhotoSaver
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.vm.feed.exif.LocalEXIFViewModel
import com.goforer.grabph.presentation.vm.feed.location.LocalLocationViewModel
import com.goforer.grabph.presentation.vm.feed.photo.LocalSavedPhotoViewModel
import com.goforer.grabph.repository.model.cache.data.entity.exif.EXIF
import com.goforer.grabph.repository.model.cache.data.entity.location.Location
import com.goforer.grabph.repository.model.cache.data.entity.profile.Person
import timber.log.Timber

open class SavePhoto(private val workHandler: CommonWorkHandler, private val saver: PhotoSaver) {
    private lateinit var activity: BaseActivity

    internal var bitmapDrawable: BitmapDrawable? = null

    internal var fileName: String? = null
    internal var title: String? = null
    internal var searperPhotoUrl: String? = null

    internal var enabledSaveEXIF: Boolean = false
    internal var enabledSaveLocation: Boolean = false

    internal var exifItems: MutableList<EXIF>? = null

    internal var location: Location? = null

    internal var searperInfo: Person? = null

    private lateinit var localEXIFViewModel: LocalEXIFViewModel
    private lateinit var localLocationViewModel: LocalLocationViewModel
    private lateinit var localSavedPhotoViewModel: LocalSavedPhotoViewModel

    fun showSaveDialog(activity: BaseActivity, localEXIFViewModel: LocalEXIFViewModel,
                       localLocationViewModel: LocalLocationViewModel, localSavedPhotoViewModel: LocalSavedPhotoViewModel,
                       savePhoto: SavePhoto) {
        this.activity = activity
        this.localEXIFViewModel = localEXIFViewModel
        this.localLocationViewModel = localLocationViewModel
        this.localSavedPhotoViewModel = localSavedPhotoViewModel

        val alertDialogBuilder = AlertDialog.Builder(activity)

        alertDialogBuilder.setTitle(activity.applicationContext.getString(R.string.phrase_dialog_title_save))
        alertDialogBuilder
                .setMessage(activity.applicationContext.getString(R.string.phrase_download))
                .setCancelable(false)
                .setIcon(R.drawable.ic_dialog_save)
                .setPositiveButton(activity.applicationContext.getString(R.string.dialog_button_yes)) { _, _ ->
                    savePhoto(savePhoto.bitmapDrawable?.bitmap!!,
                            savePhoto.fileName, savePhoto.title, savePhoto.exifItems, savePhoto.location,
                            savePhoto.searperInfo, savePhoto.searperPhotoUrl, PhotoSaver.FROM_FEED_INFO)
                }
                .setNegativeButton(activity.applicationContext.getString(R.string.dialog_button_no)) {
                    dialog, _ -> dialog.cancel() }

        val alertDialog = alertDialogBuilder.create()

        alertDialog.show()
    }

    private fun savePhoto(bitmap: Bitmap, filename: String?, title: String?, items: List<EXIF>?,
                          location: Location?, userInfo: Person?, searperPhotoUrl: String?, fromWhere: Int) {
        filename ?: return
        searperPhotoUrl ?: return
        title ?: return

        when (workHandler.savePhoto(saver, bitmap, filename, activity.applicationContext.getString(R.string.folder_name), fromWhere, -1)) {
            PhotoSaver.SAVER_PHOTO_EXIST -> showToastMessage(activity,
                    activity.applicationContext.getString(R.string.phrase_same_photo_exist),
                    Toast.LENGTH_SHORT)
            PhotoSaver.SAVER_PHOTO_SAVE_SUCCESS -> {
                showToastMessage(activity, activity.applicationContext.getString(R.string.phrase_save_photo),
                        Toast.LENGTH_SHORT)
                if (enabledSaveEXIF) {
                    saveLocalEXIF(filename, items)
                }

                if (enabledSaveLocation) {
                    saveLocalLocation(filename, title, location)
                }

                saveLocalPhotoInfo(filename, userInfo, searperPhotoUrl)
            }
            PhotoSaver.SAVER_PHOTO_SAVE_FAIL -> showToastMessage(activity,
                    activity.applicationContext.getString(R.string.phrase_fail_save_photo),
                    Toast.LENGTH_SHORT)
            else -> {
            }
        }
    }

    private fun saveLocalEXIF(filename: String, items: List<EXIF>?) {
        items?.let {
            localEXIFViewModel.setEXIFInfo(filename,
                    workHandler.copyEXIFItems(filename, it)).observe(activity,
                    Observer {
                        if (it != null) {
                            Timber.d("Complete - successfully saved EXIF Info")
                        } else {
                            Timber.d("Error - not saved EXIF Info")
                            showToastMessage(activity,
                                   activity.applicationContext.getString(R.string.phrase_fail_save_photo_exif), Toast.LENGTH_SHORT)
                        }
                    })
        }
    }

    private fun saveLocalLocation(filename: String, title: String, location: Location?) {
        location?.let {
            localLocationViewModel.setLocationInfo(filename,
                    workHandler.copyLocation(filename, title, it)).observe(activity,
                    Observer {
                        if (it != null) {
                            Timber.d("Complete - successfully saved Location Info")
                        } else {
                            Timber.d("Error - not saved Location Info")
                            showToastMessage(activity,
                                    activity.applicationContext.getString(R.string.phrase_fail_save_photo_location), Toast.LENGTH_SHORT)
                        }
                    })
        }
    }

    private fun saveLocalPhotoInfo(filename: String, userInfo: Person?, searperPhotoUrl: String) {
        userInfo?.let { user ->
            localSavedPhotoViewModel.setSavedPhotoInfo(filename,
                    workHandler.copyUserInfo(filename, user, searperPhotoUrl)).observe(activity,
                    Observer { photo ->
                        photo ?: showToastMessage(activity,
                                activity.applicationContext.getString(R.string.phrase_fail_save_photo_photo_searper), Toast.LENGTH_SHORT)
                    })
        }
    }
}