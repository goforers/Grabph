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

package com.goforer.grabph.presentation.vm.feed.exif

import androidx.lifecycle.*
import com.goforer.grabph.domain.usecase.feed.exif.LoadLocalEXIFUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.LocalEXIF
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalEXIFViewModel
@Inject
constructor(private val useCase: LoadLocalEXIFUseCase) : BaseViewModel<String>() {
    internal lateinit var exifInfo: LiveData<LocalEXIF>

    override fun setParameters(parameters: String, type: Int) {
        exifInfo = useCase.execute(viewModelScope, parameters)
    }

    internal fun setEXIFInfo(filename: String, LocalEXIF: LocalEXIF): MediatorLiveData<LocalEXIF> {
        val exif = liveData(viewModelScope.coroutineContext + Dispatchers.Main) {
             emitSource(useCase.saveEXIFInfo(filename, LocalEXIF))
        }

        return exif as MediatorLiveData<LocalEXIF>
    }

    internal fun deleteEXIFInfo(filename: String) {
        viewModelScope.launch {
            useCase.deleteEXIFInfo(filename)
        }
    }
}
