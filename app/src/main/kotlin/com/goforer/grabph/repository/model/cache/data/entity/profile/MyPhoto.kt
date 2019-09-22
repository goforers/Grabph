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

package com.goforer.grabph.repository.model.cache.data.entity.profile

import androidx.room.*
import com.goforer.base.presentation.model.BaseModel

@Entity(tableName = "MyPhoto")
data class MyPhoto(@field:PrimaryKey val id: String,
                   @field:ColumnInfo(name = "title") val title: String?,
                   @field:ColumnInfo(name = "price") val price: Int,
                   @field:ColumnInfo(name = "free") val free: Boolean,
                   @field:ColumnInfo(name = "status") val status: String?,
                   @field:ColumnInfo(name = "reasonRejected") val reasonRejected: String?,
                   @field:Embedded(prefix = "media_") val media: Media?
                   ): BaseModel() {

    data class Media(@field:ColumnInfo(name = "type") val type: String,
                     @field:Embedded(prefix = "urls_") val urls: PhotoUrls)

    data class PhotoUrls(@field:ColumnInfo(name = "raw") val raw: String,
                         @field:ColumnInfo(name = "full") val full: String,
                         @field:ColumnInfo(name = "regular") val regular: String,
                         @field:ColumnInfo(name = "small") val small: String,
                         @field:ColumnInfo(name = "thumb") val thumb: String)
}