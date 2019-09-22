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

package com.goforer.grabph.repository.model.cache.data.entity.savedphoto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.grabph.repository.model.cache.data.entity.DataColumns

@Entity(tableName = "LocalSavedPhoto")
class LocalSavedPhoto(@field:PrimaryKey(autoGenerate = true)
                    @field:ColumnInfo(index = true, name = COLUMN_IDX) val idx: Long,
                      @field:ColumnInfo(name = "filename") val filename: String,
                      @field:ColumnInfo(name = "id") val id: String,
                      @field:ColumnInfo(name = "username") val username: String,
                      @field:ColumnInfo(name = "realname") val realname: String,
                      @field:ColumnInfo(name = "description") val description: String,
                      @field:ColumnInfo(name = "photourl") val photourl: String) {
    companion object {
        private const val COLUMN_IDX = DataColumns.IDX
    }
}