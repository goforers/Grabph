package com.goforer.grabph.repository.interactor.remote.paging.boundarycallback

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.goforer.grabph.repository.model.cache.data.entity.Query
import com.goforer.grabph.repository.network.resource.NetworkBoundResource

class PagedListPeopleBoundaryCallback<T>(private val liveData: MutableLiveData<Query>,
                                         private val userId: String, private val pages: Int): PagedList.BoundaryCallback<T>() {
    companion object {
        private var requestPage = 0
    }

    override fun onZeroItemsLoaded() {
        requestPage = 1
        setQuery(Query())
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) {
        if (pages > requestPage) {
            requestPage ++
            setQuery(Query())
        }
    }

    private fun setQuery(query: Query) {
        query.query = userId
        query.pages = requestPage
        query.boundType = NetworkBoundResource.BOUND_FROM_BACKEND
        liveData.value = query

        val input = query.pages
        if (input == liveData.value?.pages) {
            return
        }

        liveData.value = query
    }
}