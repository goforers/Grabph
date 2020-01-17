package com.goforer.grabph.data.repository.remote.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.domain.Parameters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository
@Inject
constructor(): Repository<Query>() {

    companion object {
        const val METHOD_SIGN_IN = "searp.login.signIn"
        const val METHOD_SIGN_UP = "searp.login.signUp"
        const val METHOD_RESET_PASSWORD = "searp.login.resetPassword"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return MutableLiveData<Resource>()
    }

    internal suspend fun signIn() {}

    internal suspend fun signUp() {}

    internal suspend fun resetPassword() {}

}