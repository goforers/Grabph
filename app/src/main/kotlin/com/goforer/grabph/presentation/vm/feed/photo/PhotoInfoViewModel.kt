/*
 * Copyright 2019 Lukoh Nam, goForer
 *    
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License along with this program.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.goforer.grabph.presentation.vm.feed.photo

import androidx.lifecycle.*
import com.goforer.grabph.data.datasource.model.cache.data.entity.photosizes.PhotoSize
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.feed.photo.LoadPhotoInfoUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.domain.usecase.feed.exif.LoadEXIFUseCase
import com.goforer.grabph.domain.usecase.feed.photo.LoadPhotoSizeUseCase
import com.goforer.grabph.domain.usecase.people.person.LoadPersonUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoInfoViewModel
@Inject
constructor(
    private val photoUseCase: LoadPhotoInfoUseCase,
    private val personUseCase: LoadPersonUseCase,
    private val exifUseCase: LoadEXIFUseCase,
    private val photoSizeUseCase: LoadPhotoSizeUseCase
) : BaseViewModel<Parameters>() {
    internal lateinit var photoInfo: LiveData<Resource>
    internal lateinit var person: LiveData<Resource>
    internal lateinit var exifInfo: LiveData<Resource>
    internal lateinit var videoSource: MutableLiveData<String>
    internal lateinit var videoThumbnail: MutableLiveData<String>
    internal lateinit var getSizeError: MutableLiveData<String>

    override fun setParameters(parameters: Parameters, type:Int) {
        photoInfo = photoUseCase.execute(parameters)
    }

    internal fun setParametersForPerson(parameters: Parameters) {
        person = personUseCase.execute(parameters)
    }

    internal fun setParametersForEXIF(parameters: Parameters) {
        exifInfo = exifUseCase.execute(parameters)
    }

    internal suspend fun removePhotoInfo() = photoUseCase.removePhotoInfo()

    internal suspend fun removePerson() = personUseCase.removePerson()

    internal suspend fun removeEXIF() = exifUseCase.removeEXIF()

    internal fun getPhotoSizes(photoId: String) {
        videoSource = MutableLiveData()
        videoThumbnail = MutableLiveData()
        getSizeError = MutableLiveData()

        viewModelScope.launch {
            val response = photoSizeUseCase.getPhotoSizes(photoId)
            if (response.isSuccessful) {
                val size = response.body()
                if (size != null) {
                    size.sizes?.size?.let { setVideoLiveData(it) }
                } else {
                    getSizeError.value = "Sorry, couldn't find video source"
                }
            } else {
                getSizeError.value = "Request failed:" + response.message()
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
}
