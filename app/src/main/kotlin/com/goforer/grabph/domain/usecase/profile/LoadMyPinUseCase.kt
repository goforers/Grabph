package com.goforer.grabph.domain.usecase.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.goforer.grabph.data.datasource.model.cache.data.AbsentLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.LocalPin
import com.goforer.grabph.data.repository.local.LocalPinRepository
import com.goforer.grabph.domain.usecase.BaseUseCase
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadMyPinUseCase
@Inject
constructor(private val repository: LocalPinRepository): BaseUseCase<String, List<LocalPin>>() {
    // private val liveData by lazy { MutableLiveData<Query>() }
    private val liveData by lazy { MutableLiveData<String>() }

    override fun execute(viewModelScope: CoroutineScope, parameters: String): LiveData<List<LocalPin>> {
        setPhotoId(parameters)
        return Transformations.switchMap(liveData) { photoId ->
            photoId ?: AbsentLiveData.create<LocalPin>()
            repository.loadLocalPin(photoId)
        }
    }

    private fun setPhotoId(photoId: String) {
        liveData.value = photoId
    }

    internal suspend fun checkLocalPin(userId: String, photoId: String) = repository.checkLocalPin(userId, photoId)

    internal suspend fun saveLocalPin(pin: LocalPin) = repository.saveLocalPin(pin)

    internal suspend fun deleteLocalPin(photoId: String) = repository.deleteLocalPin(photoId)

    // override fun execute(viewModelScope: CoroutineScope, parameters: Parameters): LiveData<Resource> {
    //     setQuery(parameters, Query())
    //
    //     return liveData.switchMap { query ->
    //         query ?: AbsentLiveData.create<Resource>()
    //         liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
    //             emitSource(repository.load(liveData,
    //                 Parameters(
    //                     query.query,
    //                     liveData.value?.pages!!,
    //                     liveData.value?.loadType!!,
    //                     liveData.value?.boundType!!
    //                 )
    //             ))
    //         }
    //     }
    // }
    //
    // private fun setQuery(parameters: Parameters, query: Query) {
    //     query.query = parameters.query1 as String
    //     query.pages = parameters.query2 as Int
    //     query.boundType = parameters.boundType
    //     query.loadType = parameters.loadType
    //     liveData.value = query
    //
    //     val input = query.pages
    //     if (input == liveData.value?.pages) return
    //
    //     liveData.value = query
    // }
    //
    // internal suspend fun removeCache(userId: String) = repository.deleteByUserId(userId)
}