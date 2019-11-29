package com.goforer.grabph.domain.usecase.upload

import androidx.lifecycle.LiveData
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.BaseUseCase
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadAuthUseCase
@Inject
constructor(): BaseUseCase<Parameters, Resource>() {
    override fun execute(parameters: Parameters): LiveData<Resource> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}