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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
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
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Xml
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.Location
import com.goforer.grabph.presentation.ui.upload.data.RequestParams
import com.goforer.grabph.presentation.ui.upload.data.UploadResponse
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.io.UnsupportedEncodingException
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.SignatureException
import java.sql.Timestamp
import java.util.regex.Pattern
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CommonUtils {
    private const val MAX_SCROLL = 10
    private const val REQUEST_TOKEN_PARAMS = 1
    private const val ACCESS_TOKEN_PARAMS = 2
    private const val TEST_LOGIN_PARAMS = 3
    private const val UPDATE_PARAMS = 4
    private const val GET = "GET"
    private const val POST = "POST"

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

    @SuppressLint("NewApi")
    fun getPathFromUri(context: Context, uri: Uri): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return context.externalMediaDirs.toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong())

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return contentUri?.let { getDataColumn(context, it, selection, selectionArgs) }
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }

        return null
    }

    fun fail(message: String): Nothing {
        throw IllegalArgumentException(message)
    }

    fun withDelay(delay : Long, block : () -> Unit) {
        Handler().postDelayed(Runnable(block), delay)
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
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

    fun getParamsForRequestToken(): RequestParams {
        val nonce = getNonce()
        val timeStamp = getTimestamp()
        val params = getOauthParams(REQUEST_TOKEN_PARAMS, nonce, timeStamp)
        val baseString = getBaseString(GET, REQUEST_TOKEN_URL, params)
        val signature = makeSignature(baseString, "")
        val encodedSignature = urlEncoding(signature)
        return RequestParams(nonce, timeStamp, encodedSignature)
    }

    fun getParamsForAccessToken(token: String, verifier: String, secret: String): RequestParams {
        val nonce = getNonce()
        val timeStamp = getTimestamp()
        val params = getOauthParams(ACCESS_TOKEN_PARAMS, nonce, timeStamp, requestToken = token,verifier = verifier)
        val baseString = getBaseString(GET, ACCESS_TOKEN_URL, params)
        val signature = makeSignature(baseString, secret)
        val encodedSignature = urlEncoding(signature)

        return RequestParams(nonce, timeStamp, encodedSignature)
    }

    fun getParamsForTestLogin(accessToken: String, secret: String): RequestParams {
        val nonce = getNonce()
        val timeStamp = getTimestamp()
        val params = getOauthParams(TEST_LOGIN_PARAMS, nonce, timeStamp, accessToken = accessToken)
        val baseString = getBaseString(GET, TEST_LOGIN_URL, params)
        val signature = makeSignature(baseString, secret)
        val encodedSignature = urlEncoding(signature)

        return RequestParams(nonce, timeStamp, encodedSignature)
    }

    fun getParamsUpload(accessToken: String, secret: String, title: String, desc: String): RequestParams {
        val nonce = getNonce()
        val timeStamp = getTimestamp()
        val params = getOauthParams(
            UPDATE_PARAMS,
            nonce,
            timeStamp,
            accessToken = accessToken,
            title = urlEncoding(title),
            description = urlEncoding(desc)
        )
        val baseString = getBaseString(POST, UPLOAD_URL, params)
        val signature = makeSignature(baseString, secret)

        return RequestParams(nonce, timeStamp, signature)
    }

    @Throws(SignatureException::class, NoSuchAlgorithmException::class, InvalidKeyException::class)
    private fun makeSignature(data: String, secret: String): String {
        val key = "$CONSUMER_SECRET&$secret"
        val signKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(signKey)
        val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun getBaseString(httpMethod: String, baseUrl: String, params: String): String {
        val sb = StringBuilder()
        sb.append(httpMethod)
            .append('&').append(urlEncoding(baseUrl))
            .append('&').append(urlEncoding(params))
        return sb.toString()
    }

    private fun getOauthParams(
        type: Int,
        nonce: String,
        timeStamp: String,
        requestToken: String = "",
        verifier: String = "",
        accessToken: String = "",
        title: String = "",
        description: String = ""
    ): String {

        return when (type) {
            REQUEST_TOKEN_PARAMS -> {
                "oauth_callback=$CALLBACK_URL" +
                    "&oauth_consumer_key=$CONSUMER_KEY" +
                    "&oauth_nonce=$nonce" +
                    "&oauth_signature_method=$SIGN_METHOD" +
                    "&oauth_timestamp=$timeStamp" +
                    "&oauth_version=$OAUTH_VERSION"
            }
            ACCESS_TOKEN_PARAMS -> {
                "oauth_consumer_key=$CONSUMER_KEY" +
                    "&oauth_nonce=$nonce" +
                    "&oauth_signature_method=$SIGN_METHOD" +
                    "&oauth_timestamp=$timeStamp" +
                    "&oauth_token=$requestToken" +
                    "&oauth_verifier=$verifier" +
                    "&oauth_version=$OAUTH_VERSION"
            }
            TEST_LOGIN_PARAMS -> {
                "format=json" +
                    "&method=flickr.test.login" +
                    "&nojsoncallback=1" +
                    "&oauth_consumer_key=$CONSUMER_KEY" +
                    "&oauth_nonce=$nonce" +
                    "&oauth_signature_method=$SIGN_METHOD" +
                    "&oauth_timestamp=$timeStamp" +
                    "&oauth_token=$accessToken" +
                    "&oauth_version=$OAUTH_VERSION"
            }
            UPDATE_PARAMS -> {
                "description=$description" +
                    "&oauth_consumer_key=$CONSUMER_KEY" +
                    "&oauth_nonce=$nonce" +
                    "&oauth_signature_method=$SIGN_METHOD" +
                    "&oauth_timestamp=$timeStamp" +
                    "&oauth_token=$accessToken" +
                    "&title=$title"
            }

            else -> "null"
        }
    }

    private fun urlEncoding(value: String): String {
        try {
            return URLEncoder.encode(value, "UTF-8").replace("%2B", "%2520") // encoding %2B(empty space) to %2520
        } catch (ex: UnsupportedEncodingException) {
            throw RuntimeException(ex.cause)
        }
    }

    private fun getNonce(): String {
        val secureRandom = SecureRandom()
        val stringBuilder = StringBuilder()
        for (i in 0..7) {
            stringBuilder.append(secureRandom.nextInt(10))
        }
        return stringBuilder.toString()
    }

    private fun getTimestamp(): String {
        return Timestamp(System.currentTimeMillis()).time.toString()
    }

    /**
     * This series of functions below for XML is to parse XML text response from Uploading Photo to Flickr service.
     * */
    @Throws(XmlPullParserException::class, IOException::class)
    fun parseXml(xml: String): UploadResponse {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))
        parser.nextTag()
        return readTag(parser)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTag(parser: XmlPullParser): UploadResponse {
        val stat = parser.getAttributeValue(null, "stat")
        var response = UploadResponse(null, null, null, null)
        parser.require(XmlPullParser.START_TAG, null, "rsp")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            response = when (stat) {
                "ok" -> getSuccessMsg(parser, stat)
                "fail" -> getErrorMsg(parser, stat)
                else -> getErrorMsg(parser, stat)
            }
        }
        return response
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun getSuccessMsg(parser: XmlPullParser, stat: String): UploadResponse {
        parser.require(XmlPullParser.START_TAG, null, "photoid")
        val response = readText(parser, stat)
        parser.require(XmlPullParser.END_TAG, null, "photoid")
        return response
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun getErrorMsg(parser: XmlPullParser, stat: String): UploadResponse {
        parser.require(XmlPullParser.START_TAG, null, "err")
        val errCode = parser.getAttributeValue(null, "code")
        val errMsg = parser.getAttributeValue(null, "msg")
        readText(parser, stat)
        parser.require(XmlPullParser.END_TAG, null, "err")
        return UploadResponse(stat, errCode, errMsg, null)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser, stat: String): UploadResponse {
        var photoId = ""
        if (parser.next() == XmlPullParser.TEXT) {
            photoId = parser.text
            parser.nextTag()
        }
        return UploadResponse(stat, null, null, photoId)
    }

    fun makeStatusNotification(message: String, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
            val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    fun getFlickrPhotoURL(server: String, id: String, secret: String): String {
        return "https://live.staticflickr.com/" + server + "/" + id + "_" + secret + "_z.jpg"
    }

    fun getFlickrPhotoUrlForViewer(server: String, id: String, secret: String): String {
        return "https://live.staticflickr.com/" + server + "/" + id + "_" + secret + "_c.jpg"
    }

    fun isEmailFormatValid(input: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(input)

        return matcher.matches()
    }

    fun getLocation(location: Location): String {
        val country = location.country?._content ?: "unknown"
        val region = location.region?._content
        val county = location.county?._content
        var loc = country

        if (country != "unknown") {

            region?.let { reg ->
                if (reg.isNotBlank()) loc = "$reg, $loc"

                county?.let { ct ->
                    if (ct.isNotBlank()) loc = "$ct, $loc"
                }
            }
        }

        return loc
    }
}

