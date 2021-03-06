package com.goforer.grabph.presentation.ui.uploadphoto.workManager

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.goforer.base.presentation.utils.ACCESS_SECRET
import com.goforer.base.presentation.utils.ACCESS_TOKEN
import com.goforer.base.presentation.utils.CONSUMER_KEY
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.utils.KEY_UPLOAD_IMAGE_DESC
import com.goforer.base.presentation.utils.KEY_UPLOAD_IMAGE_TITLE
import com.goforer.base.presentation.utils.KEY_UPLOAD_IMAGE_URI
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_ERROR_CODE
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_ERROR_MSG
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_PHOTO_ID
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_STAT
import com.goforer.base.presentation.utils.SIGN_METHOD
import com.goforer.grabph.data.datasource.network.api.SearpService
import com.goforer.grabph.di.module.AppModule
import com.goforer.grabph.presentation.ui.uploadphoto.data.RequestParams
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Singleton
import java.io.FileNotFoundException as FileNotFoundException1

@Singleton
class UploadWorker
@Inject
constructor(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    private var searpService: SearpService = AppModule.create()

    companion object {
        private const val CONTENT_TYPE_IMAGE = "image/*"
        private const val CONTENT_TYPE_TEXT = "text/plain"
    }

    override suspend fun doWork(): Result {
        val photoList = ArrayList<MultipartBody.Part>()
        val context = applicationContext
        val imagePath = inputData.getString(KEY_UPLOAD_IMAGE_URI)?.let { CommonUtils.getPathFromUri(context, Uri.parse(it)) }
        val file = File(imagePath!!)
        val title = inputData.getString(KEY_UPLOAD_IMAGE_TITLE) ?: ""
        val desc = inputData.getString(KEY_UPLOAD_IMAGE_DESC) ?: ""

        // val authToken = SharedPreference.getAccessToken(context)
        // val secret = SharedPreference.getAccessTokenSecret(context)
        // val params = CommonUtils.getParamsUpload(authToken, secret, title, desc)
        val params = CommonUtils.getParamsUpload(ACCESS_TOKEN, ACCESS_SECRET, title, desc)
        val requestFile = file.asRequestBody(CONTENT_TYPE_IMAGE.toMediaTypeOrNull())
        val photo = MultipartBody.Part.createFormData("photo", file.name, requestFile)
        val map = getRequestBodyMap(params, title, desc)

        photoList.add(photo)

        return try {
            val response = searpService.postFeed(photoList, map)
            val data = getUploadResponse(response)
            Result.success(data)
        } catch (e: FileNotFoundException1) {
            Timber.e(e)
            Result.failure()
            throw RuntimeException("Failed to decode input stream", e)
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            Result.failure()
        }
    }

    private fun getUploadResponse(response: Response<String>): Data {
        if (response.isSuccessful) {
            val responseBody = response.body()
            val uploadResponse = CommonUtils.parseXml(responseBody!!)

            val stat = uploadResponse.stat
            val errorCode = uploadResponse.errorCode ?: "null"
            val errorMsg = uploadResponse.errorMsg ?: "null"
            val photoId = uploadResponse.photoId ?: "null"
            val msg = if (stat != null && stat == "ok") "Uploaded Photo Successfully" else "Upload Failed"
            CommonUtils.makeStatusNotification(msg, applicationContext)

            val dataBuilder = Data.Builder()
                .putString(KEY_UPLOAD_RESPONSE_STAT, uploadResponse.stat)
                .putString(KEY_UPLOAD_RESPONSE_ERROR_CODE, errorCode)
                .putString(KEY_UPLOAD_RESPONSE_ERROR_MSG, errorMsg)
                .putString(KEY_UPLOAD_RESPONSE_PHOTO_ID, photoId)
            return dataBuilder.build()
        } else {
            return Data.Builder()
                .putString(KEY_UPLOAD_RESPONSE_STAT, "fail")
                .putString(KEY_UPLOAD_RESPONSE_ERROR_CODE, "0")
                .putString(KEY_UPLOAD_RESPONSE_ERROR_MSG, "")
                .putString(KEY_UPLOAD_RESPONSE_PHOTO_ID, "")
                .build()
        }
    }

    private fun getRequestBodyMap(params: RequestParams, title: String, desc: String): HashMap<String, RequestBody> {
        val map = HashMap<String, RequestBody>()
        map["oauth_consumer_key"] = CONSUMER_KEY.toRequestBody(CONTENT_TYPE_TEXT.toMediaTypeOrNull())
        // map["oauth_token"] = createPartFromString(authToken)
        map["oauth_token"] = ACCESS_TOKEN.toRequestBody(CONTENT_TYPE_TEXT.toMediaTypeOrNull()) // this is for testing auth
        map["oauth_signature_method"] = SIGN_METHOD.toRequestBody(CONTENT_TYPE_TEXT.toMediaTypeOrNull())
        map["oauth_timestamp"] = params.timeStamp.toRequestBody(CONTENT_TYPE_TEXT.toMediaTypeOrNull())
        map["oauth_nonce"] = params.nonce.toRequestBody(CONTENT_TYPE_TEXT.toMediaTypeOrNull())
        map["oauth_signature"] = params.signature.toRequestBody(CONTENT_TYPE_TEXT.toMediaTypeOrNull())
        map["title"] = title.toRequestBody(CONTENT_TYPE_TEXT.toMediaTypeOrNull())
        map["description"] = desc.toRequestBody(CONTENT_TYPE_TEXT.toMediaTypeOrNull())

        return map
    }
}