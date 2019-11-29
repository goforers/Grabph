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

package com.goforer.grabph.domain.usecase.feed.exif

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.grabph.domain.usecase.BaseUseCase
import com.goforer.grabph.data.repository.local.LocalEXIFRepository
import com.goforer.grabph.data.datasource.model.cache.data.AbsentLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.LocalEXIF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadLocalEXIFUseCase
@Inject
constructor(private val repository: LocalEXIFRepository):  BaseUseCase<String, LocalEXIF>() {
    private val liveData by lazy {
        MutableLiveData<String>()
    }

    override fun execute(parameters: String): LiveData<LocalEXIF> {
        setFileName(parameters)

        return Transformations.switchMap(liveData) { filename ->
            filename ?: AbsentLiveData.create<LocalEXIF>()
            liveData {
                emitSource(repository.loadEXIFInfo(filename!!))
            }
        }
    }

    private fun setFileName(filename: String) {
        liveData.value = filename

        val input = filename.trim { it <= ' ' }
        if (input == liveData.value) {
            return
        }

        liveData.value = input
    }

    internal suspend fun deleteEXIFInfo(filename: String) = repository.deleteEXIFInfo(filename)

    internal suspend fun saveEXIFInfo(filename: String, localEXIF: LocalEXIF) = repository.saveEXIFInfo(filename, localEXIF)
}