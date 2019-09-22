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
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.goforer.grabph.repository.model.cache.data.entity.savedphoto.LocalSavedPhoto
import com.goforer.grabph.repository.model.dao.BaseDao

/**
 * Interface for database access on UserInfo related operations.
 */
@Dao
interface LocalSavedPhotoDao: BaseDao<LocalSavedPhoto> {
    @Query("SELECT * FROM LocalSavedPhoto where filename = :name")
    fun getPhotoInfo(name: String): LiveData<LocalSavedPhoto>

    @Query("SELECT filename FROM LocalSavedPhoto ORDER BY _idx DESC")
    fun getPhotoFilenames(): DataSource.Factory<Int, String>

    @Query("SELECT id FROM LocalSavedPhoto ORDER BY _idx DESC")
    fun getPhotoIds(): DataSource.Factory<Int, String>

    @Query("SELECT * FROM LocalSavedPhoto ORDER BY _idx DESC")
    fun getPhotoItems(): List<LocalSavedPhoto>

    @Query("DELETE FROM LocalSavedPhoto where filename = :name")
    fun deletePhotoInfo(name: String)
}