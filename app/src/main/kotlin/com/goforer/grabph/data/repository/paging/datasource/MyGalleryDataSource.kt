package com.goforer.grabph.data.repository.paging.datasource

import androidx.paging.PageKeyedDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery
import com.goforer.grabph.data.datasource.network.api.SearpService
import com.goforer.grabph.domain.Parameters
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

class MyGalleryDataSource(
    private val service: SearpService,
    private val parameters: Parameters,
    private val KEY: String,
    private val METHOD: String,
    private val FORMAT: String,
    private val PER_PAGE: Int
): PageKeyedDataSource<Int, MyGallery>() {
    private var totalPage = 0
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, MyGallery>) {
        val page = 1

        GlobalScope.launch {
            try {
                val response = service.getMyGalleryTest(KEY, parameters.query1 as String, METHOD, FORMAT, page, PER_PAGE, -1)
                if (response.isSuccessful) {
                    Timber.d("woogear success")
                    val result = response.body()
                    val gallery = result?.photos?.photo ?: emptyList()
                    totalPage = result?.photos?.pages!!
                    callback.onResult(gallery, null, page)
                } else {
                    Timber.d("woogear error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, MyGallery>) {
        if (totalPage > params.key) {
            val nextKey = params.key + 1

            GlobalScope.launch {
                try {
                    val response = service.getMyGalleryTest(KEY, parameters.query1 as String, METHOD, FORMAT, nextKey, PER_PAGE, -1)
                    if (response.isSuccessful) {
                        val result = response.body()
                        val gallery = result?.photos?.photo ?: emptyList()
                        totalPage = result?.photos?.pages!!
                        callback.onResult(gallery, nextKey)
                    } else {

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, MyGallery>) {
    }
}