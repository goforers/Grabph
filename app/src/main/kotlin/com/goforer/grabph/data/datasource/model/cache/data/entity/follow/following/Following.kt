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

package com.goforer.grabph.data.datasource.model.cache.data.entity.follow.following

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.DataColumns

@Entity(tableName = "Following")
data class Following(@field:PrimaryKey
                     @field:ColumnInfo(index = true, name = COLUMN_ID)
                     val id: String,
                     @field:ColumnInfo(name = "picture") val picture: String?,
                     @field:ColumnInfo(name = "name")val name: Name?,
                     @field:ColumnInfo(name = "realname") val realname: Realname?,
                     @field:ColumnInfo(name = "location") val location: Location?,
                     @field:ColumnInfo(name = "description") val description: Description?,
                     @field:ColumnInfo(name = "date_following") val followerDate: String?): BaseModel() {
    companion object {
        private const val COLUMN_ID = DataColumns.ID
    }

    data class Name(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Realname(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Location(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Description(@field:ColumnInfo(name = "_content") val _content: String?)
}