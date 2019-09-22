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

package com.goforer.grabph.repository.model.cache.data.entity.exif

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel

@Entity(tableName = "EXIF")
data class EXIF(@field:ColumnInfo(name = "tagspace") val tagspace: String,
                @field:ColumnInfo(name = "tagspaceid") val tagspaceid: String,
                @field:PrimaryKey
                @field:ColumnInfo(name = "label") val label: String,
                @field:Embedded(prefix = "raw_") val raw: Raw): BaseModel() {
    data class Raw(val _content: String)
}