package com.goforer.grabph.data.repository.paging.datasource

import androidx.paging.PageKeyedDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem

class FeedListDataSource(private val feeds: MutableList<FeedItem>): PageKeyedDataSource<Int, FeedItem>() {
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, FeedItem>) {
        callback.onResult(feeds, null, null)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, FeedItem>) {
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, FeedItem>) {
    }
}