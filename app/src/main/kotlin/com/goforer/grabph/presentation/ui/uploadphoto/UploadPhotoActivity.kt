package com.goforer.grabph.presentation.ui.uploadphoto

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.utils.KEY_SELECTED_IMAGE_URI
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_ERROR_CODE
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_ERROR_MSG
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_PHOTO_ID
import com.goforer.base.presentation.utils.KEY_UPLOAD_RESPONSE_STAT
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.presentation.ui.uploadphoto.adapter.UploadCategoryAdapter
import com.goforer.grabph.presentation.vm.uploadphoto.UploadPhotoViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_upload_photo.*
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

class UploadPhotoActivity : BaseActivity() {
    @MockData val userId = "184804690@N02"
    @field:Inject internal lateinit var viewModel: UploadPhotoViewModel
    private lateinit var adapterCategory: UploadCategoryAdapter
    private lateinit var checkBoxes: BooleanArray
    private val list = ArrayList<String>()

    companion object {
        const val CHECKBOX_AGREE_FIRST = 0
        const val CHECKBOX_AGREE_SECOND = 1
        const val CHECKBOX_AGREE_THIRD = 2

        const val KEY_CHECKBOX_AGREEMENT = "searp:key_checkboxd_agreement"
    }

    override fun setContentView() { setContentView(R.layout.activity_upload_photo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
            networkStatusVisible(true)
        } else {
            networkStatusVisible(false)
        }
        Timber.plant(Timber.DebugTree())

        this@UploadPhotoActivity.et_desc_upload_image.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val fcs = ForegroundColorSpan(getColor(R.color.colorHashTag))

                val pattern = Pattern.compile("#\\s*(\\w+)")
                val matcher = pattern.matcher(s.toString())

                // keeps on searching unless there is no more function string found
                while (matcher.find()) {
                    s.setSpan(
                        fcs,
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun setViews(savedInstanceState: Bundle?) {
        super.setViews(savedInstanceState)
        setSelectedPhotoView()
        createAdapter()
        getSelectedKeyword()
        setViewClickListener()
        setCheckboxClickBehavior(savedInstanceState)
        viewModel.initWork()
        viewModel.uploadWorkInfo.observe(this, workInfoObserver())
    }

    override fun setActionBar() {
        setSupportActionBar(this@UploadPhotoActivity.toolbar_upload)
        val actionBar = supportActionBar

        actionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            setDisplayShowTitleEnabled(true)
            elevation = 0f
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        this@UploadPhotoActivity.toolbar_upload.setNavigationOnClickListener { finishAfterTransition() }
        this@UploadPhotoActivity.toolbar_upload.hideOverflowMenu()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray(KEY_CHECKBOX_AGREEMENT, checkBoxes)
    }

    private fun setViewClickListener() {
        this.btn_upload.setOnClickListener {
            val title = ""
            val selectedCategory = list[adapterCategory.selectedPosition]
            val desc = this.et_desc_upload_image.text.toString()
            viewModel.upload(title, desc)
        }
    }

    private fun createAdapter() {
        adapterCategory = UploadCategoryAdapter(viewModel, this)
        this.recycler_upload_category.adapter = adapterCategory
        this.recycler_upload_category.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val list = makeCategoryList()
        adapterCategory.addList(list)
    }

    @MockData
    private fun makeCategoryList(): ArrayList<String> {
        list.add("All")
        list.add("푸드")
        list.add("스포츠")
        list.add("미디어")
        list.add("K뷰티")
        list.add("KPOP")
        list.add("여행")
        list.add("자연")
        list.add("건축물")

        return list
    }

    private fun getSelectedKeyword() {
        viewModel.selectedKeyword.observe(this, Observer { keyword ->

        })
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

            Timber.d("woogear workInfo ${workInfo.state}")

            if (workInfo.state.isFinished) {
                showWorkIsFinished()
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {

                    viewModel.refreshGallery()

                    val stat = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_STAT)
                    if (stat != null && stat == "ok") {
                        val photoId = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_PHOTO_ID)
                        Timber.d("woogear.. stat=ok, photoid=$photoId")
                        finish()
                    } else {
                        val errorCode = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_ERROR_CODE)
                        val errorMsg = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_ERROR_MSG)
                        showErrorMsg(errorCode, errorMsg)
                    }
                } else {
                    val errorCode = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_ERROR_CODE)
                    val errorMsg = workInfo.outputData.getString(KEY_UPLOAD_RESPONSE_ERROR_MSG)
                    showErrorMsg(errorCode, errorMsg)
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    private fun showWorkInProgress() {
        this.btn_upload.isEnabled = false
        this.container_progress_upload.visibility = View.VISIBLE
    }

    private fun showWorkIsFinished() {
        this.btn_upload.isEnabled = true
        this.container_progress_upload.visibility = View.GONE
    }

    private fun setCheckboxClickBehavior(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            checkBoxes = booleanArrayOf(false, false, false)
        } else {
            checkBoxes = savedInstanceState.getBooleanArray(KEY_CHECKBOX_AGREEMENT) ?: booleanArrayOf(false, false, false)
        }

        val cbAll = this.cb_check_all_upload

        cbAll.setOnClickListener { it as CheckBox
            checkAll(it.isChecked, checkBoxes)
        }

        this.cb_upload_agree_1.setOnClickListener { it as CheckBox
            checkBoxes[CHECKBOX_AGREE_FIRST] = it.isChecked
            cbAll.isChecked = isCheckedAll(checkBoxes)
        }

        this.cb_upload_agree_2.setOnClickListener { it as CheckBox
            checkBoxes[CHECKBOX_AGREE_SECOND] = it.isChecked
            cbAll.isChecked = isCheckedAll(checkBoxes)
        }

        this.cb_upload_agree_3.setOnClickListener { it as CheckBox
            checkBoxes[CHECKBOX_AGREE_THIRD] = it.isChecked
            cbAll.isChecked = isCheckedAll(checkBoxes)
        }
    }

    private fun isCheckedAll(boxes: BooleanArray): Boolean {
        var isAll = true

        for (checkBox in boxes) {
            if (!checkBox) {
                isAll = false
                break
            }
        }
        return isAll
    }

    private fun checkAll(value: Boolean, boxes: BooleanArray) {
        this.cb_upload_agree_1.isChecked = value
        this.cb_upload_agree_2.isChecked = value
        this.cb_upload_agree_3.isChecked = value

        boxes[CHECKBOX_AGREE_FIRST] = value
        boxes[CHECKBOX_AGREE_SECOND] = value
        boxes[CHECKBOX_AGREE_THIRD] = value
    }

    private fun showErrorMsg(code: String?, msg: String?) {
        Timber.d("woogear. errorCode=$code, msg=$msg")
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@UploadPhotoActivity.disconnect_container_upload.visibility = View.GONE
        this@UploadPhotoActivity.appbar_layout_upload.visibility = View.VISIBLE
        this@UploadPhotoActivity.toolbar_upload.visibility = View.VISIBLE
        this@UploadPhotoActivity.tv_upload_title.visibility = View.VISIBLE
        this@UploadPhotoActivity.nested_scroll_view_upload.visibility = View.VISIBLE
    } else {
        this@UploadPhotoActivity.disconnect_container_upload.visibility = View.VISIBLE
        this@UploadPhotoActivity.appbar_layout_upload.visibility = View.GONE
        this@UploadPhotoActivity.toolbar_upload.visibility = View.GONE
        this@UploadPhotoActivity.tv_upload_title.visibility = View.GONE
        this@UploadPhotoActivity.nested_scroll_view_upload.visibility = View.GONE
    }

    private fun showNetworkError(resource: Resource) {
        when(resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(this.upload_constraint_layout, getString(R.string.phrase_client_wrong_request), Snackbar.LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(this.upload_constraint_layout, getString(R.string.phrase_server_wrong_response), Snackbar.LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(this.upload_constraint_layout, resource.getMessage().toString(), Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
