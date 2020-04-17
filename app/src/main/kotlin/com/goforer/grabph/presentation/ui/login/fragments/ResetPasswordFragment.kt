package com.goforer.grabph.presentation.ui.login.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import com.goforer.base.presentation.utils.CommonUtils.isEmailFormatValid
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.login.LogInActivity
import com.goforer.grabph.presentation.vm.login.LoginViewModel
import kotlinx.android.synthetic.main.fragment_reset_password.*
import kotlinx.android.synthetic.main.fragment_sign_up.iv_back
import javax.inject.Inject

class ResetPasswordFragment : BaseFragment() {
    private val loginActivity by lazy { activity as LogInActivity }

    private var isEmailValid = false

    @field:Inject
    lateinit var viewModel: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView =
            AutoClearedValue(this, inflater.inflate(R.layout.fragment_reset_password, container, false))
        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        setViewsClickListener()
        checkInformationInput()
        this.ib_btn_reset_password.isEnabled = false
    }

    private fun setViewsClickListener() {
        this.iv_back.setOnClickListener { loginActivity.onBackPressed() }
        this.ib_btn_reset_password.setOnClickListener { checkEmail() }
        this.iv_icon_email_reset.setOnClickListener { if (!isEmailValid) this.et_user_email_reset.setText("") }
    }

    private fun checkEmail() {
        loginActivity.showLoadingProgressBar(true, getString(R.string.check_email_now_kr))

        // viewModel.resetPassword()

        /* Test Code */
        withDelay(1000L) {
            loginActivity.showLoadingProgressBar(false)
            showMessageNonExistEmail()

            showDialog()
        }
    }

    private fun showMessageNonExistEmail() {
        this.tv_reset_error_msg.text = getString(R.string.id_no_exists)
        val anim = AnimationUtils.loadAnimation(loginActivity, R.anim.shake_wrong)
        this.tv_reset_error_msg.startAnimation(anim)
    }

    private fun showDialog() {
        val alertDialogBuilder= AlertDialog.Builder(loginActivity)

        alertDialogBuilder.setTitle("Check your email")
        alertDialogBuilder
            .setMessage("We sent you an email for a link to reset your password.")
            .setCancelable(false)
            .setNegativeButton("ok") { dialog, _ ->
                // if this button is clicked, just close
                // the dialog box and do nothing
                dialog.cancel()
                loginActivity.onBackPressed()
            }

        val alertDialog=alertDialogBuilder.create()

        alertDialog.show()
    }

    private fun checkInformationInput() {
        var isEmailValid = false
        this.et_user_email_reset.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isEmailValid = isEmailFormatValid(s.toString())

                s?.let {
                    if (it.isNotBlank()) {
                        if (isEmailValid) {
                            setEmailViewValid()
                            setResetButtonEnabled(true)
                        } else {
                            setEmailViewInvalid()
                            setResetButtonEnabled(false)
                        }
                    } else {
                        setEmailViewDefault()
                    }
                }
            }
        })
    }

    private fun setEmailViewDefault() {
        this.iv_icon_email_reset.setImageResource(R.drawable.ic_baseline_email_24)
        this.constraint_holder_email_reset.setBackgroundResource(0)
        hideErrorMessage()
        isEmailValid = false
    }

    private fun setEmailViewValid() {
        this.iv_icon_email_reset.setImageResource(R.drawable.ic_login_valid)
        this.constraint_holder_email_reset.setBackgroundResource(0)
        hideErrorMessage()
        isEmailValid = true
    }

    private fun setEmailViewInvalid() {
        this.iv_icon_email_reset.setImageResource(R.drawable.ic_login_invalid)
        this.constraint_holder_email_reset.setBackgroundResource(R.drawable.ic_border_red_alert)
        showErrorMessage(getString(R.string.email_format_invalid))
        isEmailValid = false
    }

    private fun showErrorMessage(msg: String) {
        this.tv_reset_error_msg.text = msg
    }

    private fun hideErrorMessage() {
        this.tv_reset_error_msg.text = ""
    }

    private fun setResetButtonEnabled(isEnabled: Boolean) {
        this.ib_btn_reset_password.isEnabled = isEnabled
        this.ib_btn_reset_password.setBackgroundResource(
            if (isEnabled) R.drawable.ic_border_of_login else R.drawable.ic_border_of_login_disabled
        )
    }
}