package com.goforer.grabph.presentation.vm.upload

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.upload.LoadAuthUseCase
import com.goforer.grabph.domain.usecase.upload.UploadPhotosUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthViewModel
@Inject
constructor(private val authUseCase: LoadAuthUseCase): BaseViewModel<Parameters>() {
    internal lateinit var requestStat: LiveData<Resource>

    override fun setParameters(parameters: Parameters, type: Int) {
        requestStat = authUseCase.execute(parameters)
    }

    internal fun getRequestToken() {

    }

    internal fun getVerifier() {

    }

    internal fun getAccessToken() {

    }
}