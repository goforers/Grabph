package com.goforer.grabph.domain.usecase.feed.photo

import androidx.lifecycle.MutableLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.photosizes.PhotoSize
import com.goforer.grabph.data.repository.remote.feed.photo.PhotoSizeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadPhotoSizeUseCase
@Inject
constructor(private val repository: PhotoSizeRepository) {
    private val liveData by lazy { MutableLiveData<Query>() }

    private lateinit var videoSource: MutableLiveData<String>
    private lateinit var videoThumbnail: MutableLiveData<String>
    private lateinit var getSizeError: MutableLiveData<String>

    internal fun getPhotoSizes(photoId: String, viewModelScope: CoroutineScope) {
        videoSource = MutableLiveData()
        videoThumbnail = MutableLiveData()
        getSizeError = MutableLiveData()

        viewModelScope.launch {
            val response = repository.loadPhotoSizes(photoId)
            if (response.isSuccessful) {
                val size = response.body()
                if (size != null) {
                    size.sizes?.size?.let { setVideoLiveData(it) }
                } else {
                    getSizeError.value = "Sorry, couldn't find video source"
                }
            } else {
                getSizeError.value = "Request failed: " + response.message()
            }
        }
    }

    private fun setVideoLiveData(list: List<PhotoSize>) {
        var mobileSource: String? = null
        var siteSource: String? = null
        var thumbnail: String? = null
        for (item in list) {
            if (item.label == "Medium 640") thumbnail = item.source
            if (item.label == "Mobile MP4") mobileSource = item.source
            if (item.label == "Site MP4") siteSource = item.source
        }
        videoSource.value = mobileSource ?: siteSource
        thumbnail?.let { videoThumbnail.value = it }
    }

    internal fun videoSource() = videoSource
    internal fun videoThumbnail() = videoThumbnail
    internal fun sizeError() = getSizeError
}