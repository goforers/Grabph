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

package com.goforer.grabph.repository.model.dao.remote.category.photo

import androidx.paging.DataSource
import androidx.room.*
import com.goforer.grabph.repository.model.cache.data.entity.category.CPhoto
import com.goforer.grabph.repository.model.dao.BaseDao

/**
 * Interface for database access on Photog's Picture related operations.
 */
@Dao
interface CPhotoDao: BaseDao<CPhoto> {
    @Query("SELECT * FROM CPhoto ORDER BY _idx ASC")
    fun getCPhotos(): DataSource.Factory<Int, CPhoto>

    @Query("DELETE FROM CPhoto WHERE _id = :photoId")
    fun deleteByCPhotoId(photoId: String)

    @Query("DELETE FROM Comment")
    fun clearAll()
}