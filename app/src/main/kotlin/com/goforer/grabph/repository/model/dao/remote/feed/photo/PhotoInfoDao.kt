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

package com.goforer.grabph.repository.model.dao.remote.feed.photo

import androidx.lifecycle.LiveData
import androidx.room.*
import com.goforer.grabph.repository.model.cache.data.entity.photoinfo.Picture
import com.goforer.grabph.repository.model.dao.BaseDao

/**
 * Interface for database access on PhotoInfo related operations.
 */
@Dao
interface PhotoInfoDao: BaseDao<Picture> {
    @Query("SELECT * FROM Picture")
    fun getPhotoInfo(): LiveData<Picture>

    @Query("DELETE FROM Picture WHERE id = :photoInfoId")
    fun deleteByPhotoInfoId(photoInfoId: String)

    @Query("DELETE FROM Picture")
    suspend fun clearAll()
}