package com.goforer.grabph.domain.usecase.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.goforer.grabph.data.datasource.model.cache.data.AbsentLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.remote.feed.photo.OthersPhotosRepository
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.BaseUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadMyPinUseCase
@Inject
constructor(private val repository: OthersPhotosRepository): BaseUseCase<Parameters, Resource>() {
    private val liveData by lazy { MutableLiveData<Query>() }

    override fun execute(parameters: Parameters): LiveData<Resource> {
        setQuery(parameters, Query())

        return liveData.switchMap { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData {
                emitSource(repository.load(liveData,
                    Parameters(
                        query.query,
                        liveData.value?.pages!!,
                        liveData.value?.loadType!!,
                        liveData.value?.boundType!!
                    )
                ))
            }
        }
    }

    private fun setQuery(parameters: Parameters, query: Query) {
        query.query = parameters.query1 as String
        query.pages = parameters.query2 as Int
        query.boundType = parameters.boundType
        query.loadType = parameters.loadType
        liveData.value = query

        val input = query.pages
        if (input == liveData.value?.pages) return

        liveData.value = query
    }

    internal suspend fun removeCache() = repository.removeCache()
}