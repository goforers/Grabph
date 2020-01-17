package com.goforer.grabph.domain.usecase.login

import com.goforer.grabph.data.repository.remote.login.LoginRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUseCase
@Inject
constructor(private val repository: LoginRepository) {

    internal fun signIn() {}

    internal fun signUp() {}

    internal fun resetPassword() {}

}
