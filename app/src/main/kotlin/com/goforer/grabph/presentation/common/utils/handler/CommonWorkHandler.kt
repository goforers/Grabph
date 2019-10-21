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

@file:Suppress("UNREACHABLE_CODE", "DEPRECATION")

package com.goforer.grabph.presentation.common.utils.handler

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Parcelable
import android.text.Html
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.erase.PhotoEraser
import com.goforer.grabph.domain.usecase.save.PhotoSaver
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.EXIF
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.LocalEXIF
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.LocalLocation
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.Location
import com.goforer.grabph.data.datasource.model.cache.data.entity.savedphoto.LocalSavedPhoto
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Person
import java.util.*
import android.net.Uri
import android.provider.MediaStore.Images.Media.insertImage
import androidx.fragment.app.Fragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CommonWorkHandler @Inject constructor() {
    companion object {
        const val EXIF_ITEM_INDEX_MAKE = 0
        const val EXIF_ITEM_INDEX_MODEL = 1
        const val EXIF_ITEM_INDEX_EXPOSURE = 2
        const val EXIF_ITEM_INDEX_APERTURE = 3
        const val EXIF_ITEM_INDEX_ISO_SPEED = 4
        const val EXIF_ITEM_INDEX_FLASH = 5
        const val EXIF_ITEM_INDEX_WHITE_BALANCE = 6
        const val EXIF_ITEM_INDEX_FOCAL_LENGTH = 7
    }

    fun deletePhoto(eraser: PhotoEraser, path: String, position: Int): Boolean {
        return eraser.deletePhoto(path, position)
    }

    fun savePhoto(saver: PhotoSaver, bitmap: Bitmap, filename: String, folderName: String,
                  fromWhere: Int, position: Int): Int {
        return saver.savePhoto(bitmap, filename, folderName, fromWhere, position)
    }

    fun shareToFacebook(bitmap: Bitmap, activity: Activity) {
        if (ShareDialog.canShow(SharePhotoContent::class.java)) {
            val photo = SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build()
            val content = SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build()
            val dialog = ShareDialog(activity)
            dialog.show(content, ShareDialog.Mode.AUTOMATIC)
        }
    }

    fun shareToFacebook(bitmap: Bitmap, fragment: Fragment) {
        if (ShareDialog.canShow(SharePhotoContent::class.java)) {
            val photo = SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build()
            val content = SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build()
            val dialog = ShareDialog(fragment)
            dialog.show(content, ShareDialog.Mode.AUTOMATIC)
        }
    }

    fun showAppListToShare(context: Context, title: String?, description: String?,
                           photo: Bitmap) {
        var appDescription = "No Description"
        var appTitle = "No Title"
        val targetedShareIntents = ArrayList<Intent>()
        if (title != "") {
            appTitle = title.toString()
        }

        if (description != "") {
            appDescription = description.toString()
        }

        val googlePlusIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "com.google.android.apps.plus",
                    context.resources.getString(R.string.app_name) +
                    "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "com.google.android.apps.plus",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        googlePlusIntent?.let { targetedShareIntents.add(it) }

        val twitterIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "twitter",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "twitter",
                    context.resources.getString(R.string.app_name) +"\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        twitterIntent?.let { targetedShareIntents.add(it) }

        val whatsAppIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "com.whatsapp",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "com.whatsapp",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        whatsAppIntent?.let { targetedShareIntents.add(it) }

        val hangoutIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "com.google.android.talk",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "com.google.android.talk",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        hangoutIntent?.let { targetedShareIntents.add(it) }

        val facebookmessengerIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "com.facebook.orca",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "com.facebook.orca",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        facebookmessengerIntent?.let { targetedShareIntents.add(it) }

        val instagrmInt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "com.instagram.android",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "com.instagram.android",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        instagrmInt?.let { targetedShareIntents.add(it) }

        val pinterestInt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "com.pinterest",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "com.pinterest",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        pinterestInt?.let { targetedShareIntents.add(it) }

        val kakaotalkIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "com.kakao.talk",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "com.kakao.talk",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        kakaotalkIntent?.let { targetedShareIntents.add(it) }

        val lineIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "jp.naver.line.android",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "jp.naver.line.android",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        lineIntent?.let { targetedShareIntents.add(it) }

        val gmailIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getShareIntent(context, "gmail",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription, Html.FROM_HTML_MODE_LEGACY) + "\n\n", photo)
        } else {
            TODO("VERSION.SDK_INT < N")
            getShareIntent(context, "gmail",
                    context.resources.getString(R.string.app_name) + "\n\n" + context.resources.getString(R.string.phrase_title) + "\n" + appTitle
                            + "\n\n" + context.resources.getString(R.string.phrase_description) + "\n"
                            + Html.fromHtml(appDescription) + "\n\n", photo)
        }

        gmailIntent?.let { targetedShareIntents.add(it) }

        val chooser = Intent.createChooser(targetedShareIntents.removeAt(0),
                context.resources.getString(R.string.menu_share))
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toTypedArray<Parcelable>())
        chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(chooser)
    }

    fun copyEXIFItems(filename: String, items: List<EXIF>): LocalEXIF {
        val exif = arrayOf("None", "None", "None", "None", "None", "None", "None")

        exif[0] = (items[EXIF_ITEM_INDEX_MAKE].raw._content + " "
                                                        + items[EXIF_ITEM_INDEX_MODEL].raw._content)
        exif[1] = items[EXIF_ITEM_INDEX_EXPOSURE].raw._content
        exif[2] = items[EXIF_ITEM_INDEX_APERTURE].raw._content
        exif[3] = items[EXIF_ITEM_INDEX_ISO_SPEED].raw._content
        exif[4] = items[EXIF_ITEM_INDEX_FLASH].raw._content
        exif[5] = items[EXIF_ITEM_INDEX_WHITE_BALANCE].raw._content
        exif[6] = items[EXIF_ITEM_INDEX_FOCAL_LENGTH].raw._content

        return LocalEXIF(0, filename, exif[0], exif[1], exif[2], exif[3],
                exif[4], exif[5], exif[6])
    }

    fun copyLocation(filename: String, title: String,
                     location: Location): LocalLocation {
        val addressArray = arrayOf("", "", "", "", "", "")

        addressArray[0] = location.neighbourhood?._content ?: ""
        addressArray[1] = location.locality?._content ?: ""
        addressArray[2] = location.county?._content ?: ""
        addressArray[3] = location.region?._content ?: ""
        addressArray[4] = location.country?._content ?: ""

        val address = addressArray[0] + ", " + addressArray[1] + ", " + addressArray[2] + ", " + addressArray[3] + ", " + addressArray[4]

        return LocalLocation(0, filename, title, location.latitude!!, location.longitude!!, address)
    }

    fun copyUserInfo(filename: String, userInfo: Person, searperPhotoUrl: String): LocalSavedPhoto {
        val username = userInfo.username?._content ?: "No Name"
        val realname = userInfo.realname?._content ?: "No Real Name"
        val description = userInfo.description?._content ?: "No Description"
        return LocalSavedPhoto(0, filename, userInfo.id, username,
                realname, description, searperPhotoUrl)
    }

    private fun getShareIntent(context: Context, type: String, subject: String, photo: Bitmap): Intent? {
        var found = false
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/*"

        val path = insertImage(context.contentResolver, photo, "photo", null)
        val photoUri = Uri.parse(path)
        // gets the list of intents that can be loaded.
        val resInfo = context.packageManager.queryIntentActivities(share, 0)
        if (resInfo.isNotEmpty()) {
            for (info in resInfo) {
                if (info.activityInfo.packageName.toLowerCase(Locale.getDefault()).contains(type)
                        || info.activityInfo.name.toLowerCase(Locale.getDefault()).contains(type)) {
                    val activity = info.activityInfo
                    val name = ComponentName(activity.applicationInfo.packageName, activity.name)
                    share.component = name
                    share.addCategory(Intent.CATEGORY_DEFAULT)
                    if (type == "com.pinterest") {
                        share.putExtra("com.pinterest.EXTRA_DESCRIPTION", subject)
                    } else {
                        share.putExtra(Intent.EXTRA_TITLE, subject)
                    }

                    share.type = "image/*"
                    share.putExtra(Intent.EXTRA_STREAM, photoUri)
                    found = true
                    break
                }
            }

            return if (!found) {
                null
            } else
                return share
        }

        return null
    }

    fun getProfilePhotoURL(iconFarm: Int, iconServer: String, id: String): String {
         return "https://farm$iconFarm.staticflickr.com/$iconServer/buddyicons/$id.jpg"
    }
}
