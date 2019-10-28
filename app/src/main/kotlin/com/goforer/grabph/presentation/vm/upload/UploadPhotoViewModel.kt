package com.goforer.grabph.presentation.vm.upload

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.goforer.base.presentation.utils.KEY_UPLOAD_IMAGE_DESC
import com.goforer.base.presentation.utils.KEY_UPLOAD_IMAGE_TITLE
import com.goforer.base.presentation.utils.KEY_UPLOAD_IMAGE_URI
import com.goforer.base.presentation.utils.TAG_OUTPUT
import com.goforer.base.presentation.utils.UPLOADING_WORK_NAME
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.ui.upload.workManager.UploadWorker
import com.goforer.grabph.presentation.vm.BaseViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadPhotoViewModel
@Inject
constructor(application: Application): BaseViewModel<Parameters>() {
    internal val uploadWorkInfo: LiveData<List<WorkInfo>>
    internal var imageUri: Uri? = null
    private val workManager: WorkManager = WorkManager.getInstance((application))

    init {
        uploadWorkInfo = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }

    override fun setParameters(parameters: Parameters, type: Int) {}

    @SuppressLint("EnqueueWork")
    internal fun upload(title: String, desc: String) {
        val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(createInputDataForUri(title, desc))
            .addTag(TAG_OUTPUT)
            .build()

        val continuation: WorkContinuation = workManager
            .beginUniqueWork(
                UPLOADING_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        continuation.enqueue()
    }

    internal fun setImageUri(uri: String) {
        imageUri = uriOrNull(uri)
    }

    private fun createInputDataForUri(title: String, desc: String): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_UPLOAD_IMAGE_URI, it.toString())
            builder.putString(KEY_UPLOAD_IMAGE_TITLE, title)
            builder.putString(KEY_UPLOAD_IMAGE_DESC, desc)
        }
        return builder.build()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    internal fun pruneWork() {
        workManager.pruneWork()
    }
}