package com.goforer.grabph.presentation.ui.uploadquest.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.goforer.base.presentation.utils.CommonUtils.getLongFromStringDate
import com.goforer.base.presentation.utils.CommonUtils.getStringDateFromDatePicker
import com.goforer.base.presentation.utils.CommonUtils.getStringFromLongDate
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.base.presentation.view.helper.OnBackPressed
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.uploadquest.UploadQuestActivity
import com.goforer.grabph.presentation.ui.uploadquest.UploadQuestActivity.Companion.CURRENT_FRAGMENT_QUEST_INFO
import com.goforer.grabph.presentation.ui.uploadquest.adapter.QuestCategoryAdapter
import com.goforer.grabph.presentation.vm.uploadquest.UploadQuestViewModel
import kotlinx.android.synthetic.main.dialog_input_reward.view.*
import kotlinx.android.synthetic.main.fragment_upload_quest_info.*
import java.text.DecimalFormat
import java.util.Calendar
import javax.inject.Inject

class QuestInfoFragment : BaseFragment(), OnBackPressed {
    private val uploadActivity: UploadQuestActivity by lazy { activity as UploadQuestActivity }
    private lateinit var acvAdapterCategory: AutoClearedValue<QuestCategoryAdapter>

    private var adapter: QuestCategoryAdapter? = null
    private val list = ArrayList<String>()

    private lateinit var calendar: Calendar
    private var year = 0
    private var month = 0
    private var day = 0

    @field:Inject
    lateinit var viewModel: UploadQuestViewModel

    companion object {
        private const val REWARD_LIMIT_AMOUNT = 1000000 // a million won
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDates()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this, inflater.inflate(R.layout.fragment_upload_quest_info, container, false))
        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadActivity.setCurrentFragment(CURRENT_FRAGMENT_QUEST_INFO)

        createAdapter()
        getLiveData()
        setViewsClickListener()
        setViews()
    }

    override fun onBackPressed() {
        val isTitleEmpty = this.et_upload_quest_title.text.toString().isNotEmpty()
        val isDescriptionEmpty = this.tv_quest_desc.text.toString().isNotEmpty()
        val isRewardEmpty = this.tv_quest_reward.text.toString().isNotEmpty()

        if (isTitleEmpty || isDescriptionEmpty || isRewardEmpty) {
            showExistingTextAlarmDialog()
        }  else {
            uploadActivity.goBackStack()
        }
    }

    private fun setViews() {
        viewModel.setDescription(this.tv_quest_desc.text.toString())
    }

    private fun setViewsClickListener() {
        this.tv_quest_desc.setOnClickListener { uploadActivity.transactQuestDesc() }
        this.tv_quest_reward.setOnClickListener { showInputRewardDialog() }
        this.tv_upload_quest_duration.setOnClickListener { showDatePickerDialog() }
    }

    private fun setDates() {
        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun getLiveData() {
        viewModel.selectedKeyword.observe(uploadActivity, Observer { keyword ->
            // println("woogear keyword=$keyword")
        })

        viewModel.description.observe(uploadActivity, Observer { desc ->
            this.tv_quest_desc?.let { it.text = desc }
        })

        viewModel.reward.observe(uploadActivity, Observer { reward ->
            this.tv_quest_reward?.let { it.text = reward }
        })

        viewModel.duration.observe(uploadActivity, Observer { duration ->
            this.tv_upload_quest_duration?.let { it.text = getStringFromLongDate(duration) }
        })
    }

    @SuppressLint("InflateParams")
    private fun showInputRewardDialog() {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_input_reward, null)

        val et = view.findViewById<EditText>(R.id.et_input_reward)
        val tvAlert = view.tv_reward_notice

        et.setText(this.tv_quest_reward.text.toString())
        setEditTextWithComma(et, tvAlert)
        et.setSelection(et.text.toString().length)


        view.iv_clear_reward.setOnClickListener { et.setText("") }

        val alertDialogBuilder = AlertDialog.Builder(uploadActivity)
        alertDialogBuilder
            .setTitle("보상금액 입력")
            .setIcon(R.drawable.ic_price)
            .setCancelable(true)
            .setView(view)
            .setPositiveButton("확인") { dialog, _ ->
                val reward = et.text.toString()
                this.tv_quest_reward.text = reward
                viewModel.setReward(reward)
                dialog.cancel()
            }

        alertDialogBuilder.create().show()
    }

    private fun setEditTextWithComma(et: EditText, alert: TextView) {
        val format = DecimalFormat("###,###.####")
        var result = ""
        et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (s.toString() != result && s.toString().isNotEmpty()) {
                    val inputLong = s.toString().replace(",", "").toLong()

                    if (inputLong <= REWARD_LIMIT_AMOUNT) { // if bigger than 100,000
                        result = format.format(s.toString().replace(",", "").toLong())
                        alert.visibility = View.GONE
                    } else {
                        result = format.format(REWARD_LIMIT_AMOUNT.toString().replace(",", "").toLong())
                        alert.visibility = View.VISIBLE
                    }

                    et.setText(result)
                    et.setSelection(result.length)
                }
            }
        })
    }

    private fun showDatePickerDialog() {
        val dialog = DatePickerDialog(uploadActivity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val strDate = getStringDateFromDatePicker(year, monthOfYear, dayOfMonth)
            viewModel.setDuration(getLongFromStringDate(strDate))
        }, year, month, day)

        dialog.show()
    }

    private fun showExistingTextAlarmDialog() {
        val alertDialogBuilder = AlertDialog.Builder(uploadActivity)

        alertDialogBuilder.setTitle("퀘스트 등록을 취소할까요?")
        alertDialogBuilder
            .setMessage("작성 중인 항목이 있습니다.")
            .setCancelable(true)
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("확인") { dialog, _ ->
                dialog.cancel()
                uploadActivity.goBackStack()
            }

        alertDialogBuilder.create().show()
    }

    private fun createAdapter() {
        adapter = adapter ?: QuestCategoryAdapter(uploadActivity, viewModel)
        acvAdapterCategory = AutoClearedValue(this, adapter)
        this.recycler_upload_quest_category.adapter = acvAdapterCategory.get()
        this.recycler_upload_quest_category.layoutManager =
            LinearLayoutManager(uploadActivity, LinearLayoutManager.HORIZONTAL, false)

        if (list.isEmpty()) acvAdapterCategory.get()?.addList(makeCategoryList())
    }

    private fun makeCategoryList(): ArrayList<String> {
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
}