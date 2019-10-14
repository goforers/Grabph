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

package com.goforer.base.presentation.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.IBinder
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import com.facebook.FacebookSdk.getApplicationContext
import com.goforer.grabph.R
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.goforer.base.domain.common.GeneralFunctions
import java.io.File

object CommonUtils {
    private const val MAX_SCROLL = 10

    fun showToastMessage(activity: Activity, text: String, duration: Int) {
        val inflater = activity.layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast,
                activity.findViewById(R.id.custom_Toast_container))

        val phrase = layout.findViewById<AppCompatTextView>(R.id.tv_text)

        phrase.text = text

        val toast = Toast(getApplicationContext())

        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        toast.duration = duration
        toast.view = layout
        toast.show()
    }

    @SuppressLint("TimberArgCount")
    fun getPackageName(context: Context): String {
        val manager = context.packageManager
        var packageName = context.packageName
        try {
            val info = manager.getPackageInfo(packageName, 0)
            packageName = info.applicationInfo.dataDir
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.w(e)
        }

        return packageName
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun convertDateToLong(text: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")

        return dateFormat.parse(text).time
    }

    fun convertDateToString(datestamp: Long): String {
        val dateFormat = "yyyy-MM-dd hh:mm:ss.SSS"
        // Create a DateFormatter object for displaying date in specified format.
        @SuppressLint("SimpleDateFormat")
        val formatter = SimpleDateFormat(dateFormat)
        val cal = GregorianCalendar.getInstance()
        cal.timeInMillis = datestamp

        return formatter.format(cal.timeInMillis)
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    fun dismissKeyboard(windowToken: IBinder, activity: FragmentActivity?) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    @SuppressLint("SimpleDateFormat")
    fun convertTime(time: Long): String {
        val posedDate = time * 1000L
        val date = Date(posedDate)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return format.format(date)
    }

    @SuppressLint("NewApi")
    fun getVersionNumber(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.WEEK_OF_YEAR) * 100 + calendar.get(Calendar.YEAR)
    }

    fun areDrawablesIdentical(source: Drawable?, target: Drawable?): Boolean {
        source ?: return false
        target ?: return false

        val sourceState = source.constantState
        val targetState = target.constantState

        // If the constant state is identical, they are using the same drawable resource.
        // However, the opposite is not necessarily true.
        return sourceState == targetState || getBitmap(source).sameAs(getBitmap(target))
    }

    fun getImagePath(context: Context): String {
        return Environment.getExternalStorageDirectory().toString() + File.separator + context.getString(R.string.folder_name)
    }

    fun betterSmoothScrollToPosition(recyclerView: RecyclerView, targetItem: Int) {
        recyclerView.layoutManager?.apply {
            when (this) {
                is LinearLayoutManager -> {
                    val topItem = findFirstVisibleItemPosition()
                    val distance = topItem - targetItem
                    val anchorItem = when {
                        distance > MAX_SCROLL -> targetItem + MAX_SCROLL
                        distance < -MAX_SCROLL -> targetItem - MAX_SCROLL
                        else -> topItem
                    }

                    if (anchorItem != topItem) {
                        scrollToPosition(anchorItem)
                    }

                    recyclerView.post {
                        recyclerView.smoothScrollToPosition(targetItem)
                    }
                }
                is StaggeredGridLayoutManager -> {
                    val topItem = GeneralFunctions.getFirstVisibleItem(findFirstVisibleItemPositions(null))

                    val distance = topItem - targetItem
                    val anchorItem = when {
                        distance > MAX_SCROLL -> targetItem + (MAX_SCROLL - 1)
                        distance < -MAX_SCROLL -> targetItem - (MAX_SCROLL - 1)
                        else -> topItem
                    }

                    if (anchorItem != topItem) {
                        scrollToPosition(anchorItem)
                    }

                    recyclerView.post {
                        recyclerView.smoothScrollToPosition(targetItem)
                    }
                }
                is GridLayoutManager -> {
                    val topItem = findFirstVisibleItemPosition()
                    val distance = topItem - targetItem
                    val anchorItem = when {
                        distance > MAX_SCROLL -> targetItem + MAX_SCROLL
                        distance < -MAX_SCROLL -> targetItem - MAX_SCROLL
                        else -> topItem
                    }

                    if (anchorItem != topItem) {
                        scrollToPosition(anchorItem)
                    }

                    recyclerView.post {
                        recyclerView.smoothScrollToPosition(targetItem)
                    }
                }
            }
        }
    }

    fun fail(message: String): Nothing {
        throw IllegalArgumentException(message)
    }

    fun withDelay(delay : Long, block : () -> Unit) {
        Handler().postDelayed(Runnable(block), delay)
    }

    private fun getBitmap(drawable: Drawable): Bitmap {
        val result: Bitmap

        if (drawable is BitmapDrawable) {
            result = drawable.bitmap
        } else {
            var width = drawable.intrinsicWidth
            var height = drawable.intrinsicHeight
            // Some drawables have no intrinsic width - e.g. solid colours.
            if (width <= 0) {
                width = 1
            }

            if (height <= 0) {
                height = 1
            }

            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }

        return result
    }

    internal fun getJson(mock: String): String {
        val json: String
        val inputStream = getApplicationContext().assets.open(mock)
        val buffer = ByteArray(inputStream.available())

        inputStream.read(buffer)
        inputStream.close()
        json = String(buffer)

        return json
    }
}

