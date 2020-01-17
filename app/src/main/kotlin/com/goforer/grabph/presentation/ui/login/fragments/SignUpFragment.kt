package com.goforer.grabph.presentation.ui.login.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.goforer.base.presentation.utils.CommonUtils.isEmailFormatValid
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.login.LogInActivity
import com.goforer.grabph.presentation.vm.login.LoginViewModel
import kotlinx.android.synthetic.main.fragment_sign_up.*
import javax.inject.Inject

class SignUpFragment : BaseFragment() {
    private val loginActivity: LogInActivity by lazy { activity as LogInActivity }

    private var isUsernameValid = false
    private var isEmailValid = false
    private var isPassword1Valid = false
    private var isPassword2Valid = false
    private var selectedGender = -1

    @field:Inject
    lateinit var viewModel: LoginViewModel

    companion object {
        private const val MIN_USERNAME_LENGTH = 3
        private const val MAX_USERNAME_LENGTH = 14
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_PASSWORD_LENGTH = 14

        private const val GENDER_DEFAULT = -1
        private const val GENDER_MALE = 0
        private const val GENDER_FEMALE = 1
        private const val GENDER_CUSTOM = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView =
            AutoClearedValue(this, inflater.inflate(R.layout.fragment_sign_up, container, false))
        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        checkInformationInput()
    }

    private fun setViews() {
        setViewsClickListener()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.tv_forgot_password.text = Html.fromHtml(getString(R.string.forgot_password),
                Html.FROM_HTML_MODE_LEGACY)
            this.tv_login.text = Html.fromHtml(getString(R.string.have_an_account),
                Html.FROM_HTML_MODE_LEGACY)
        } else {
            this.tv_forgot_password.text = Html.fromHtml(getString(R.string.forgot_password))
            this.tv_login.text = Html.fromHtml(getString(R.string.have_an_account))
        }
    }

    private fun setViewsClickListener() {
        this.ib_btn_sign_up.setOnClickListener { checkSignUpInformationReady() }
        this.holder_male.setOnClickListener { selectGender(GENDER_MALE) }
        this.holder_female.setOnClickListener { selectGender(GENDER_FEMALE) }
        this.holder_custom_sex.setOnClickListener { selectGender(GENDER_CUSTOM) }
        this.tv_forgot_password.setOnClickListener { loginActivity.goToResetPassword() }
        this.tv_login.setOnClickListener { loginActivity.onBackPressed() }
        this.iv_back.setOnClickListener { loginActivity.onBackPressed() }
    }

    private fun checkSignUpInformationReady() {
        if (selectedGender == GENDER_DEFAULT) {
            loginActivity.showMessage(getString(R.string.choose_your_gender))
        } else {
            this.container_progress_bar_sign_up.visibility = View.VISIBLE

            viewModel.signUp()

            /* Test Code */
            withDelay(1000L) {
                this.container_progress_bar_sign_up.visibility = View.GONE
                showSignUpErrorMessage()
            }
        }
    }

    private fun showSignUpErrorMessage() {
        this.tv_sign_up_error_msg.text = getString(R.string.id_exists_already)
        val anim = AnimationUtils.loadAnimation(loginActivity, R.anim.shake_wrong)
        this.tv_sign_up_error_msg.startAnimation(anim)
    }

    private fun selectGender(gender: Int) {
        setDefaultGenderView(this.holder_male, this.tv_btn_male)
        setDefaultGenderView(this.holder_female, this.tv_btn_female)
        setDefaultGenderView(this.holder_custom_sex, this.tv_btn_custom)

        when (gender) {
            GENDER_MALE -> setSelectedGenderView(this.holder_male, this.tv_btn_male)
            GENDER_FEMALE -> setSelectedGenderView(this.holder_female, this.tv_btn_female)
            GENDER_CUSTOM -> setSelectedGenderView(this.holder_custom_sex, this.tv_btn_custom)
        }

        selectedGender = gender
    }

    @SuppressLint("ResourceAsColor")
    private fun setSelectedGenderView(holder: ConstraintLayout, tv: AppCompatTextView) {
        holder.setBackgroundResource(R.drawable.border_of_button_filled_white)
        tv.setTextColor(R.color.colorGenderSelected)
    }

    private fun setDefaultGenderView(holder: ConstraintLayout, tv: AppCompatTextView) {
        holder.setBackgroundResource(R.drawable.border_of_button_white)
        tv.setTextColor(Color.WHITE)
    }

    private fun checkInformationInput() {
        watchUserNameText()
        watchEmailText()
        watchPassword1Text()
        watchPassword2Text()
    }

    private fun watchUserNameText() {
        this.et_username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let {
                    if (it.isNotBlank()) {
                        if (it.length > MIN_USERNAME_LENGTH) {
                            setUserNameValid()
                        } else {
                            setUserNameInvalid(getString(R.string.min_username_length))
                        }
                    } else {
                        setUserNameDefault()
                    }
                    checkAllInformationValid()
                }
            }
        })
    }

    private fun setUserNameDefault() {
        setIconDefault(this.iv_icon_username)
        hideErrorMessage()
        setHolderDefault(this.constraint_holder_username)
        isUsernameValid = false
    }

    private fun setUserNameValid() {
        setIconValid(this.iv_icon_username)
        hideErrorMessage()
        setHolderDefault(this.constraint_holder_username)
        isUsernameValid = true
    }

    private fun setUserNameInvalid(msg: String) {
        setIconInvalid(this.iv_icon_username)
        showErrorMessage(msg)
        setHolderInvalid(this.constraint_holder_username)
        isUsernameValid = false
    }

    private fun watchEmailText() {
        var isEmailValid = false

        this.et_user_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isEmailValid = isEmailFormatValid(s.toString())

                s?.let {
                    if (it.isNotBlank()) {
                        if (isEmailValid) {
                            setEmailValidView()
                        } else {
                            setEmailInvalidView(getString(R.string.email_format_invalid))
                        }
                    } else {
                        setEmailDefaultView()
                    }
                    checkAllInformationValid()
                }
            }
        })
    }

    private fun setEmailDefaultView() {
        setIconDefault(this.iv_icon_email)
        hideErrorMessage()
        setHolderDefault(this.constraint_holder_email)
        isEmailValid = false
    }

    private fun setEmailValidView() {
        setIconValid(this.iv_icon_email)
        hideErrorMessage()
        setHolderDefault(this.constraint_holder_email)
        isEmailValid = true
    }

    private fun setEmailInvalidView(msg: String) {
        setIconInvalid(this.iv_icon_email)
        showErrorMessage(msg)
        setHolderInvalid(this.constraint_holder_email)
        isEmailValid = false
    }

    private fun watchPassword1Text() {
        var isPasswordValid = false

        this.et_password1_signup.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let {
                    isPasswordValid = it.length >= MIN_PASSWORD_LENGTH
                    if (it.isNotBlank()) {
                        if (isPasswordValid) {
                            setPassword1ValidView()
                        } else {
                            setPassword1InvalidView(getString(R.string.password_too_short))
                        }
                    } else {
                        setPassword1DefaultView()
                    }
                    checkAllInformationValid()
                }
            }
        })
    }

    private fun setPassword1DefaultView() {
        setIconDefault(this.iv_icon_password1)
        hideErrorMessage()
        setHolderDefault(this.constraint_holder_password_1)
        isPassword1Valid = false
    }

    private fun setPassword1ValidView() {
        setIconValid(this.iv_icon_password1)
        hideErrorMessage()
        setHolderDefault(this.constraint_holder_password_1)
        isPassword1Valid = true
    }

    private fun setPassword1InvalidView(msg: String) {
        setIconInvalid(this.iv_icon_password1)
        showErrorMessage(msg)
        setHolderInvalid(this.constraint_holder_password_1)
        isPassword1Valid = false
    }

    private fun watchPassword2Text() {
        val etPasswordFirst = this.et_password1_signup
        this.et_password2_signup.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let {
                    if (it.isNotBlank()) {
                        if (s.toString() == etPasswordFirst.text.toString()) {
                            setPassword2ValidView()
                        } else {
                            setPassword2InvalidView(getString(R.string.password_not_identified))
                        }
                    } else {
                        setPassword2DefaultView()
                    }
                    checkAllInformationValid()
                }
            }
        })
    }

    private fun setPassword2DefaultView() {
        setIconDefault(this.iv_icon_password2)
        hideErrorMessage()
        setHolderDefault(this.constraint_holder_password_2)
        isPassword2Valid = false
    }

    private fun setPassword2ValidView() {
        setIconValid(this.iv_icon_password2)
        hideErrorMessage()
        setHolderDefault(this.constraint_holder_password_2)
        isPassword2Valid = true
    }

    private fun setPassword2InvalidView(msg: String) {
        setIconInvalid(this.iv_icon_password2)
        showErrorMessage(msg)
        setHolderInvalid(this.constraint_holder_password_2)
        isPassword2Valid = false
    }

    private fun checkAllInformationValid() {
        if (isUsernameValid && isEmailValid && isPassword1Valid && isPassword2Valid) setSignUpButtonEnabled(true)
        else setSignUpButtonEnabled(false)
    }

    private fun setSignUpButtonEnabled(isEnabled: Boolean) {
        this.ib_btn_sign_up.isEnabled = isEnabled
        this.ib_btn_sign_up.setBackgroundResource(
            if (isEnabled) R.drawable.ic_border_of_login else R.drawable.ic_border_of_login_disabled
        )
    }

    private fun setIconDefault(view: AppCompatImageView) {
        view.setImageResource(0)
    }

    private fun setIconValid(view: AppCompatImageView) {
        view.setImageResource(R.drawable.ic_login_valid)
    }

    private fun setIconInvalid(view: AppCompatImageView) {
        view.setImageResource(R.drawable.ic_login_invalid)
    }

    private fun setHolderDefault(view: ConstraintLayout) {
        view.setBackgroundResource(0)
    }

    private fun setHolderInvalid(view: ConstraintLayout) {
        view.setBackgroundResource(R.drawable.ic_border_red_alert)
    }

    private fun showErrorMessage(msg: String) {
        this.tv_sign_up_error_msg.text = msg
    }

    private fun hideErrorMessage() {
        this.tv_sign_up_error_msg.text = ""
    }
}