package com.goforer.grabph.presentation.vm.login

import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.login.LoginUseCase
import com.goforer.grabph.domain.usecase.login.ResetPasswordUseCase
import com.goforer.grabph.domain.usecase.login.SignUpUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginViewModel
@Inject
constructor(private val loginUseCase: LoginUseCase,
            private val signUpUseCase: SignUpUseCase,
            private val resetPasswordUseCase: ResetPasswordUseCase): BaseViewModel<Parameters>() {

    override fun setParameters(parameters: Parameters, type: Int) {
    }
}