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

package com.goforer.grabph.repository.model.cache.data.entity.comments

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.repository.model.cache.data.entity.DataColumns

@Entity(tableName = "Comment")
data class Comment(@field:PrimaryKey
                   @field:ColumnInfo(index = true, name = COLUMN_ID) val id: String,
                   @field:ColumnInfo(name = "author") val author: String,
                   @field:ColumnInfo(name = "author_is_deleted") val author_is_deleted: Int,
                   @field:ColumnInfo(name = "authorname") val authorname: String,
                   @field:ColumnInfo(name = "iconserver") val iconserver: String,
                   @field:ColumnInfo(name = "iconfarm") val iconfarm: Int,
                   @field:ColumnInfo(name = "datecreate") val datecreate: String,
                   @field:ColumnInfo(name = "permalink") val permalink: String?,
                   @field:ColumnInfo(name = "path_alias") val path_alias: String?,
                   @field:ColumnInfo(name = "realname") val realname: String,
                   @field:ColumnInfo(name = "_content") val _content: String?) : BaseModel() {
    companion object {
        private const val COLUMN_ID = DataColumns.ID
    }
}