package com.goforer.grabph.domain.usecase.feed.photo

import androidx.lifecycle.MutableLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.repository.remote.feed.photo.PhotoSizeRepository
import com.goforer.grabph.domain.Parameters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadPhotoSizeUseCase
@Inject
constructor(private val repository: PhotoSizeRepository) {
    private val liveData by lazy { MutableLiveData<Query>() }

    internal suspend fun getPhotoSizes(photoId: String) = repository.loadPhotoSizes(photoId)

}