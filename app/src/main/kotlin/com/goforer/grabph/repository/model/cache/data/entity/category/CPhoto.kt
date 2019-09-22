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

package com.goforer.grabph.repository.model.cache.data.entity.category

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.repository.model.cache.data.entity.DataColumns

@Entity(tableName = "CPhoto")
data class CPhoto(@field:ColumnInfo(name = COLUMN_ID) val id: String,
                  @field:ColumnInfo(name = "secret") val secret: String?,
                  @field:ColumnInfo(name = "server") val server: String?,
                  @field:ColumnInfo(name = "farm") val farm: Int,
                  @field:ColumnInfo(name = "camera") val camera: String?,
                  @field:ColumnInfo(name = "searper") val searper: String?,
                  @field:ColumnInfo(name = "path") val path: String?,
                  @field:ColumnInfo(name = "title") var title: String?,
                  @field:ColumnInfo(name = "ispublic") val ispublic: Int,
                  @field:ColumnInfo(name = "isfriend") val isfriend: Int,
                  @field:ColumnInfo(name = "isfamily") val isfamily: Int): BaseModel() {
    companion object {
        private const val COLUMN_ID = DataColumns.ID
        private const val COLUMN_IDX = DataColumns.IDX
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_IDX)
    var idx: Long = 0
}