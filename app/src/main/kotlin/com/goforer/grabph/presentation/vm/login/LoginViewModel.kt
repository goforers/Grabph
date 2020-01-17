package com.goforer.grabph.presentation.vm.login

import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.login.LoginUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginViewModel
@Inject
constructor(private val loginUseCase: LoginUseCase): BaseViewModel<Parameters>() {

    override fun setParameters(parameters: Parameters, type: Int) {}

    internal fun signIn() {}

    internal fun signUp() {}

    internal fun resetPassword() {}
}