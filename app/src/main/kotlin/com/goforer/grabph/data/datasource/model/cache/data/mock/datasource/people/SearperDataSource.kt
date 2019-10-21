package com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.people

import androidx.paging.PageKeyedDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Searper

class SearperDataSource(private val searpers: List<Searper>): PageKeyedDataSource<Int, Searper>() {

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Searper>) {
        callback.onResult(searpers as MutableList<Searper>, null, null)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Searper>) {
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Searper>) {
    }
}