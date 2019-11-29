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

package com.goforer.grabph.presentation.vm.othersprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.othersprofile.LoadOthersProfileUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.domain.usecase.othersprofile.LoadOthersPhotoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OthersProfileViewModel
@Inject
constructor(private val useCaseProfile: LoadOthersProfileUseCase,
            private val useCasePhotos: LoadOthersPhotoUseCase
): BaseViewModel<Parameters>() {
    internal lateinit var profile: LiveData<Resource>
    internal lateinit var photos: LiveData<Resource>

    override fun setParameters(parameters: Parameters, type: Int) {
        profile = useCaseProfile.execute(parameters)
    }

    internal fun setParametersPhotos(parameters: Parameters) {
        photos = useCasePhotos.execute(parameters)
    }

    internal fun removeCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                useCaseProfile.removeCache()
                useCasePhotos.removeCache()
            }
        }
    }
}