package com.goforer.grabph.data.repository.remote.paging.boundarycallback

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_MY_GALLERYG_PHOTO

class PagedListMyGalleryBoundaryCallback<T>(private val liveData: MutableLiveData<Query>,
                                            private val userId: String,
                                            private val pages: Int): PagedList.BoundaryCallback<T>() {
    companion object {
        private var requestPage = 0
    }

    override fun onZeroItemsLoaded() {
        requestPage = 1
        setQuery(Query())
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) {
        if (pages > requestPage) {
            requestPage++
            setQuery(Query())
        }
    }

    private fun setQuery(query: Query) {
        query.query = userId
        query.pages = requestPage
        query.loadType = LOAD_MY_GALLERYG_PHOTO
        query.boundType = BOUND_FROM_BACKEND
        liveData.value = query

        val input = query.pages
        if (input == liveData.value?.pages) {
            return
        }

        liveData.value = query
    }
}