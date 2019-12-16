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

package com.goforer.grabph.data.datasource.model.dao.remote.feed.photo

import androidx.paging.DataSource
import androidx.room.*
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.data.datasource.model.dao.BaseDao

/**
 * Interface for database access on Photog's Picture related operations.
 */
@Dao
interface PhotoDao: BaseDao<Photo> {
    @Query("SELECT * FROM Photo WHERE owner = :userId")
    fun getPhotos(userId: String): DataSource.Factory<Int, Photo>

    @Query("SELECT * FROM Photo WHERE owner = :userId")
    fun getPhotoList(userId: String): List<Photo>

    @Query("DELETE FROM Photo WHERE _id = :photoId")
    fun deleteByPhotoId(photoId: String)

    @Query("DELETE FROM Photo")
    suspend fun clearAll()
}