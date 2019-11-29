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
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.feed.photo.LoadPopularPhotoUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.data.datasource.network.response.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

class PopularPhotoViewModel
@Inject
constructor(private val useCase: LoadPopularPhotoUseCase) : BaseViewModel<Parameters>() {
    internal lateinit var photos: LiveData<Resource>

    override fun setParameters(parameters: Parameters, type: Int) {
        photos = useCase.execute(parameters)
    }

    internal suspend fun removePhotos() = useCase.removePhotos()

    internal fun deleteByPhotoId(id: String) = viewModelScope.launch { useCase.deleteByPhotoId(id) }

    internal fun update(photo: Photo) = viewModelScope.launch { useCase.update(photo) }
}