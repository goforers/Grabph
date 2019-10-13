package com.goforer.grabph.domain.usecase.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.grabph.domain.usecase.BaseUseCase
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.repository.interactor.remote.comment.CommentRepository
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.model.cache.data.entity.Query
import com.goforer.grabph.repository.network.response.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadCommentUseCase
@Inject
constructor(private val repository: CommentRepository):  BaseUseCase<Parameters, Resource>() {
    private val liveData by lazy { MutableLiveData<Query>() }

    override fun execute(viewModelScope: CoroutineScope, parameters: Parameters): LiveData<Resource> {
        setQuery(parameters, Query())

        return Transformations.switchMap(liveData) { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(repository.load(liveData, Parameters(query.query, -1, query.loadType, liveData.value?.boundType!!)))
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

    internal fun getCommentList() = repository.loadComments()

    internal suspend fun removeComments() = repository.removeComments()
}