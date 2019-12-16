package com.goforer.grabph.data.repository.paging.datasource

import androidx.paging.PageKeyedDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery

class MyGalleryDataSource(private val gallery: List<MyGallery>): PageKeyedDataSource<Int, MyGallery>() {
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, MyGallery>) {
        callback.onResult(gallery as MutableList<MyGallery>, null, null)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, MyGallery>) {
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, MyGallery>) {
    }
}