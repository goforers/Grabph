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

package com.goforer.grabph.data.datasource.model.cache.data.entity.profile

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.DataColumns

@Entity(tableName = "Owner")
data class Owner(@field:PrimaryKey
                 @field:ColumnInfo(index = true, name = COLUMN_ID) val id: String,
                 @field:ColumnInfo(name = "iconserver") val iconserver: String,
                 @field:ColumnInfo(name = "iconfarm") val iconfarm: Int,
                 @field:ColumnInfo(name = "username") val username: String?,
                 @field:ColumnInfo(name = "realname") val realname: String?,
                 @field:ColumnInfo(name = "location") val location: String?,
                 @field:ColumnInfo(name = "description") val description: String?,
                 @field:Embedded(prefix = "photourl") val photourl: Photourl?,
                 @field:ColumnInfo(name = "grade") val grade: String?,
                 @field:ColumnInfo(name = "pictures_count") val pictures: String?,
                 @field:ColumnInfo(name = "pinned_count") val pinned: String?,
                 @field:ColumnInfo(name = "gallery_count") val galleryCount: String?,
                 @field:ColumnInfo(name = "purchased_count") val purchased: String?,
                 @field:ColumnInfo(name = "follower_count") val followers: String?,
                 @field:ColumnInfo(name = "followings_count") val followings: String?,
                 @field:ColumnInfo(name = "background_photo") val backgroundPhoto: String?): BaseModel() {
    companion object {
        private const val COLUMN_ID = DataColumns.ID
    }

    data class Photourl(val _content: String?)

}