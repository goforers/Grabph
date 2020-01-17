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
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.LocalPin
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.feed.photo.LoadPhotoInfoUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.domain.usecase.feed.exif.LoadEXIFUseCase
import com.goforer.grabph.domain.usecase.feed.location.LoadLocationUseCase
import com.goforer.grabph.domain.usecase.feed.photo.LoadPhotoSizeUseCase
import com.goforer.grabph.domain.usecase.people.person.LoadPersonUseCase
import com.goforer.grabph.domain.usecase.profile.LoadMyPinUseCase
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
    private val locationUseCase: LoadLocationUseCase,
    private val photoSizeUseCase: LoadPhotoSizeUseCase,
    private val pinUseCase: LoadMyPinUseCase
) : BaseViewModel<Parameters>() {
    internal lateinit var photoInfo: LiveData<Resource>
    internal lateinit var person: LiveData<Resource>
    internal lateinit var exifInfo: LiveData<Resource>
    internal lateinit var location: LiveData<Resource>
    internal lateinit var isPinned: MutableLiveData<Boolean>

    override fun setParameters(parameters: Parameters, type:Int) {
        photoInfo = photoUseCase.execute(viewModelScope, parameters)
    }

    internal fun setParametersForPerson(parameters: Parameters) {
        person = personUseCase.execute(viewModelScope, parameters)
    }

    internal fun setParametersForEXIF(parameters: Parameters) {
        exifInfo = exifUseCase.execute(viewModelScope, parameters)
    }

    internal fun setParametersForLocation(parameters: Parameters) {
        location = locationUseCase.execute(viewModelScope, parameters)
    }

    internal suspend fun removePhotoInfo() = photoUseCase.removePhotoInfo()

    internal suspend fun removePerson() = personUseCase.removePerson()

    internal suspend fun removeEXIF() = exifUseCase.removeEXIF()

    internal suspend fun removeLocation() = locationUseCase.removeLocation()

    internal fun getPhotoSizes(photoId: String) {
        photoSizeUseCase.getPhotoSizes(photoId, viewModelScope)
    }

    internal fun videoSource() = photoSizeUseCase.videoSource()

    internal fun videoThumbnail() = photoSizeUseCase.videoThumbnail()

    internal fun sizeError() = photoSizeUseCase.sizeError()

    internal fun checkPinStatus(userId: String, photoId: String) = viewModelScope.launch {
        isPinned = MutableLiveData()
        isPinned.value = pinUseCase.checkLocalPin(userId, photoId)
    }

    internal fun savePin(localPin: LocalPin) = viewModelScope.launch{
        pinUseCase.saveLocalPin(localPin)
    }

    internal fun deletePin(photoId: String) = viewModelScope.launch {
        pinUseCase.deleteLocalPin(photoId)
    }
}
