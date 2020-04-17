package com.goforer.grabph.presentation.ui.uploadquest.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.base.presentation.view.helper.OnBackPressed
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.uploadquest.UploadQuestActivity
import com.goforer.grabph.presentation.ui.uploadquest.UploadQuestActivity.Companion.CURRENT_FRAGMENT_QUEST_DESC
import com.goforer.grabph.presentation.vm.uploadquest.UploadQuestViewModel
import kotlinx.android.synthetic.main.fragment_upload_quest_desc.*
import javax.inject.Inject

class QuestDescFragment : BaseFragment(), OnBackPressed {

    private val uploadActivity: UploadQuestActivity by lazy { activity as UploadQuestActivity }
    private var initialDesc: String? = ""

    @field:Inject
    lateinit var viewModel: UploadQuestViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this, inflater.inflate(R.layout.fragment_upload_quest_desc, container, false))
        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadActivity.setCurrentFragment(CURRENT_FRAGMENT_QUEST_DESC)
        getDescription()
        setViewsClickListener()
        checkDescriptionLimit()
    }

    override fun onBackPressed() {
        val currentDesc = this.et_quest_desc.text.toString()

        if (initialDesc == currentDesc) {
            uploadActivity.goBackStack()
        } else {
            showDialogForChangedText()
        }
    }

    private fun setViewsClickListener() {
        this.tv_remove_desc.setOnClickListener { showDialogToCheckRemovingAll() }
        this.btn_save_desc.setOnClickListener { saveDescriptionAndGoBack() }
    }

    private fun getDescription() {
        initialDesc = viewModel.description.value
        initialDesc?.let { this.et_quest_desc.setText(it) }
    }

    private fun saveDescriptionAndGoBack() {
        val desc = this.et_quest_desc.text.toString()
        uploadActivity.goBackStack()
        viewModel.setDescription(desc)
    }

    private fun showDialogToCheckRemovingAll() {
        val alertDialogBuilder= AlertDialog.Builder(uploadActivity)

        alertDialogBuilder.setTitle("작성중인 글을 모두 지울까요?")
        alertDialogBuilder
            .setMessage("작성 중인 글을 모두 지우려면 확인을 눌러주세요.")
            .setCancelable(true)
            .setNegativeButton("취소") { dialog, _ ->
                // if this button is clicked, just close
                // the dialog box and do nothing
                dialog.cancel()
            }
            .setPositiveButton("확인") { dialog, _ ->
                dialog.cancel()
                this.et_quest_desc.setText("")
            }

        val alertDialog= alertDialogBuilder.create()

        if (this.et_quest_desc.text.toString().isNotEmpty()) alertDialog.show()
    }

    private fun showDialogForChangedText() {
        val alertDialogBuilder= AlertDialog.Builder(uploadActivity)

        alertDialogBuilder.setTitle("작성 중인 글을 저장하지 않고 나갈까요?")
        alertDialogBuilder
            .setMessage("글이 수정되었습니다.")
            .setCancelable(true)
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("확인") { dialog, _ ->
                // if this button is clicked, just close
                // the dialog box and do nothing
                dialog.cancel()
                this.et_quest_desc.setText(initialDesc)
                uploadActivity.goBackStack()
            }

        val alertDialog=alertDialogBuilder.create()

        alertDialog.show()
    }

    private fun checkDescriptionLimit() {
        this.et_quest_desc.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val limit = 300
                val length = s?.length
                this@QuestDescFragment.tv_desc_limit.text = "$length/$limit"
            }
        })
    }
}