package com.goforer.grabph.data.repository.paging.datasource

import androidx.paging.DataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery
import com.goforer.grabph.data.datasource.network.api.SearpService
import com.goforer.grabph.domain.Parameters

class MyGalleryDataFactory(
    private val service: SearpService,
    private val parameters: Parameters,
    private val KEY: String,
    private val METHOD: String,
    private val FORMAT: String,
    private val PER_PAGE: Int
): DataSource.Factory<Int, MyGallery>() {

    override fun create(): DataSource<Int, MyGallery> {
        return MyGalleryDataSource(service, parameters, KEY, METHOD, FORMAT, PER_PAGE)
    }
}