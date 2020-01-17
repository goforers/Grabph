package com.goforer.grabph.presentation.ui.login.fragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.goforer.base.presentation.utils.CommonUtils.isEmailFormatValid
import com.goforer.base.presentation.utils.CommonUtils.setTextViewGradient
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.utils.SharedPreference
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_REGULAR
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.login.LogInActivity
import com.goforer.grabph.presentation.ui.login.LogInActivity.Companion.SNS_NAME_FACEBOOK
import com.goforer.grabph.presentation.vm.login.LoginViewModel
import kotlinx.android.synthetic.main.fragment_sign_in.*
import java.util.Arrays
import javax.inject.Inject

class SignInFragment : BaseFragment() {
    private val loginActivity: LogInActivity by lazy { activity as LogInActivity }

    @field:Inject
    lateinit var viewModel: LoginViewModel

    companion object {
        private const val EMAIL = "email"
        private const val PUBLIC_PROFILE = "public_profile"
        private const val AUTH_TYPE = "rerequest"
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_PASSWORD_LENGTH = 14
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this, inflater.inflate(R.layout.fragment_sign_in, container, false))
        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFacebookLogin()
        setViews()
    }

    private fun setViews() {
        setViewsClickListener()
        checkAccountInput()

        setTextViewGradient(loginActivity, this.tv_guest_mode)
        loginActivity.setFontTypeface(this.tv_guest_mode, FONT_TYPE_REGULAR)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.tv_forgot_password.text = Html.fromHtml(getString(R.string.forgot_password),
                Html.FROM_HTML_MODE_LEGACY)
            this.tv_sign_up.text = Html.fromHtml(getString(R.string.no_account),
                Html.FROM_HTML_MODE_LEGACY)
        } else {
            this.tv_forgot_password.text = Html.fromHtml(getString(R.string.forgot_password))
            this.tv_sign_up.text = Html.fromHtml(getString(R.string.no_account))
        }
    }

    private fun setViewsClickListener() {
        this.ib_login.setOnClickListener {
            if (isInputFormatValid()) {
                this.container_progress_bar_sign_in.visibility = View.VISIBLE
                verifyAccount()
            }
        }

        this.btn_guest_mode.setOnClickListener { loginActivity.goToHome() }
        this.tv_forgot_password.setOnClickListener { loginActivity.goToResetPassword() }
        this.tv_sign_up.setOnClickListener { loginActivity.showSignUp() }
    }

    private fun verifyAccount() {

        viewModel.signIn()

        /* Test Code */
        withDelay(1000L) {
            this.container_progress_bar_sign_in.visibility = View.GONE
            showInvalidAccountMessage()
        }
    }

    private fun isInputFormatValid(): Boolean {
        if (this.et_email_login.text.toString().isBlank()) {
            loginActivity.showMessage(getString(R.string.request_input_id))
            return false
        }

        return if (this.et_password_login.text.toString().isNotBlank()) {
            true
        } else {
            loginActivity.showMessage(getString(R.string.request_input_password))
            false
        }
    }

    private fun checkAccountInput() {
        var isEmailValid = false
        var isPasswordValid = false

        this.et_email_login.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isEmailValid = isEmailFormatValid(s.toString())

                s?.let {
                    if (it.isNotBlank()) {
                        if (isEmailValid) {
                            setEmailViewValid()
                            setSignInButtonEnabled(isPasswordValid)
                        } else {
                            setEmailViewInvalid()
                            setSignInButtonEnabled(false)
                        }
                    } else {
                        setEmailViewDefault()
                    }
                }
            }
        })

        this.et_password_login.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let {
                    isPasswordValid = it.length >= MIN_PASSWORD_LENGTH // length = 8

                    if (it.isNotBlank()) {
                        if (isPasswordValid) {
                            setPasswordViewValid()
                            setSignInButtonEnabled(isEmailValid)
                        } else {
                            setPasswordViewInvalid()
                            setSignInButtonEnabled(false)
                        }
                    } else {
                        setPasswordViewDefault()
                    }
                }
            }
        })
    }

    private fun setEmailViewDefault() {
        this.iv_icon_email.setImageResource(R.drawable.ic_baseline_email_24)
        hideErrorMessage()
        setHolderDefaultView(this.constraint_holder_email)
    }

    private fun setEmailViewValid() {
        this.iv_icon_email.setImageResource(R.drawable.ic_login_valid)
        hideErrorMessage()
        setHolderDefaultView(this.constraint_holder_email)
    }

    private fun setEmailViewInvalid() {
        this.iv_icon_email.setImageResource(R.drawable.ic_login_invalid)
        showErrorMessage(getString(R.string.email_format_invalid))
        setHolderInvalidView(this.constraint_holder_email)
    }

    private fun setPasswordViewDefault() {
        this.iv_icon_password.setImageResource(R.drawable.ic_baseline_lock_24)
        hideErrorMessage()
        setHolderDefaultView(this.constraint_holder_password)
    }

    private fun setPasswordViewValid() {
        this.iv_icon_password.setImageResource(R.drawable.ic_login_valid)
        hideErrorMessage()
        setHolderDefaultView(this.constraint_holder_password)
    }

    private fun setPasswordViewInvalid() {
        this.iv_icon_password.setImageResource(R.drawable.ic_login_invalid)
        showErrorMessage(getString(R.string.password_too_short))
        setHolderInvalidView(this.constraint_holder_password)
    }

    private fun setSignInButtonEnabled(isEnabled: Boolean) {
        this.ib_login.isEnabled = isEnabled
        this.ib_login.setBackgroundResource(
            if (isEnabled) R.drawable.ic_border_of_login else R.drawable.ic_border_of_login_disabled
        )
    }

    private fun showInvalidAccountMessage() {
        this.tv_login_error_msg.text = getString(R.string.wrong_account_input)
        val anim = AnimationUtils.loadAnimation(loginActivity, R.anim.shake_wrong)
        this.tv_login_error_msg.startAnimation(anim)
    }

    private fun setHolderDefaultView(view: ConstraintLayout) {
        view.setBackgroundResource(0)
    }

    private fun setHolderInvalidView(view: ConstraintLayout) {
        view.setBackgroundResource(R.drawable.ic_border_red_alert)
    }

    private fun showErrorMessage(msg: String) {
        this.tv_login_error_msg.text = msg
    }

    private fun hideErrorMessage() {
        this.tv_login_error_msg.text = ""
    }

    private fun initFacebookLogin() {
        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        loginActivity.callbackManager = CallbackManager.Factory.create()
        this.login_button.setReadPermissions(Arrays.asList(EMAIL, PUBLIC_PROFILE))
        this.login_button.authType = AUTH_TYPE
        // Register a callback to respond to the user
        this.login_button.registerCallback(loginActivity.callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                loginActivity.setResult(Activity.RESULT_OK)
                loginActivity.goToHome()
            }

            override fun onCancel() {
                loginActivity.setResult(Activity.RESULT_CANCELED)
                // loginActivity.finish()
            }

            override fun onError(e: FacebookException) {
                // Handle exception
                loginActivity.showMessage(e.toString())
            }
        })

        this.cardview_holder_facebook.setOnClickListener {
            SharedPreference.saveSharePreferenceSocialLogin(loginActivity, SNS_NAME_FACEBOOK)
            this.login_button.performClick()
        }
    }
}