package com.goforer.grabph.data.repository.paging.datasource

import androidx.paging.PageKeyedDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo

class OthersProfileDataSource(private val photos: List<Photo>): PageKeyedDataSource<Int, Photo>() {
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Photo>) {
        callback.onResult(photos as MutableList<Photo>, null, null)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {
    }
}