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

package com.goforer.grabph.repository.model.dao.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.goforer.grabph.repository.model.cache.data.entity.location.LocalLocation
import com.goforer.grabph.repository.model.dao.BaseDao

/**
 * Interface for database access on LocalLocation related saving LocalLocation information operations.
 */
@Dao
interface LocalLocationDao: BaseDao<LocalLocation> {
    @Query("SELECT * FROM LocalLocation where filename = :name")
    fun getLocationInfo(name: String): LiveData<LocalLocation>

    @Query("SELECT * FROM LocalLocation")
    fun getLocationInfo(): LiveData<LocalLocation>

    @Query("delete from LocalLocation where filename = :name")
    fun deleteLocationInfo(name: String)
}