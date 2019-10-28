package com.goforer.grabph.presentation.ui.upload

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import com.goforer.base.presentation.utils.KEY_SELECTED_IMAGE_URI
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_ERROR_CODE
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_ERROR_MSG
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_PHOTO_ID
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_STAT
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.vm.upload.UploadPhotoViewModel
import kotlinx.android.synthetic.main.activity_upload_photo.*
import timber.log.Timber
import javax.inject.Inject

class UploadPhotoActivity : BaseActivity() {
    @field:Inject internal lateinit var viewModel: UploadPhotoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSelectedPhotoView()
        setViewClickListener()

        Timber.plant(Timber.DebugTree())

        viewModel.pruneWork()
        viewModel.uploadWorkInfo.observe(this, workInfoObserver())
    }

    override fun setContentView() {
        setContentView(R.layout.activity_upload_photo)
    }

    private fun setViewClickListener() {
        this.btn_upload.setOnClickListener {
            val title = this.et_title_upload_image.text.toString()
            val desc = this.et_title_upload_image.text.toString()
            viewModel.upload(title, desc)
        }
    }

    private fun setSelectedPhotoView() {
        val imageUri = intent.getStringExtra(KEY_SELECTED_IMAGE_URI)
        imageUri?.let { viewModel.setImageUri(it) }
        viewModel.imageUri?.let { uri ->
            Glide.with(this).load(uri).into(this.iv_photo_upload)
        }
    }

    private fun workInfoObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) return@Observer

            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                showWorkIsFinished()
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {

                    val stat = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_STAT)
                    if (stat != null && stat == "ok") {
                        val photoId = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_PHOTO_ID)
                        println("woogear.. stat=ok, photoid=$photoId")
                    } else {
                        val errorCode = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_ERROR_CODE)
                        val errorMsg = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_ERROR_MSG)
                        showErrorMsg()
                    }
                    // finish()
                } else {
                    val errorCode = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_ERROR_CODE)
                    val errorMsg = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_ERROR_MSG)
                    showErrorMsg()
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    private fun showWorkInProgress() {
        this.iv_photo_upload.alpha = 0.5f
        this.btn_upload.isEnabled = false
        this.container_progress_upload.visibility = View.VISIBLE
    }

    private fun showWorkIsFinished() {
        this.iv_photo_upload.alpha = 1f
        this.btn_upload.isEnabled = true
        this.container_progress_upload.visibility = View.GONE
    }

    private fun showSuccessUI() {

    }

    private fun showErrorMsg() {
        Timber.d("woogear. error")
    }
}
