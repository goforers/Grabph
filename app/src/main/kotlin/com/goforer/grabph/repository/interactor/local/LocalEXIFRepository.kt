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

package com.goforer.grabph.repository.interactor.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.annotation.WorkerThread
import com.goforer.grabph.repository.model.cache.data.entity.exif.LocalEXIF
import com.goforer.grabph.repository.model.dao.local.LocalEXIFDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalEXIFRepository @Inject constructor(private val dao: LocalEXIFDao) {
    private val result = MediatorLiveData<LocalEXIF>()

    @WorkerThread
    internal suspend fun saveEXIFInfo(filename: String, localEXIF: LocalEXIF): MediatorLiveData<LocalEXIF> {
        save(result, filename, localEXIF)

        return result
    }

    internal fun loadEXIFInfo(filename: String): LiveData<LocalEXIF> {
        return dao.getEXIFInfo(filename)
    }

    @WorkerThread
    internal suspend fun deleteEXIFInfo(filename: String) {
        delete(filename)
    }

    internal suspend fun save(result: MediatorLiveData<LocalEXIF>, fileName: String, localEXIF: LocalEXIF) {
        val exif = withContext(Dispatchers.IO) {
            dao.insert(localEXIF)
            loadEXIFInfo(fileName)
        }

        result.addSource(exif) {
            result.setValue(localEXIF)
        }
    }

    internal suspend fun delete(fileName: String) {
        withContext(Dispatchers.IO) {
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                dao.deleteEXIFInfo(fileName)
            }
        }
    }
}