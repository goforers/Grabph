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
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.repository.model.cache.data.entity.DataColumns

@Entity(tableName = "LocalEXIF")
data class LocalEXIF(@field:PrimaryKey(autoGenerate = true)
                @field:ColumnInfo(index = true, name = COLUMN_IDX) val idx: Long,
                @field:ColumnInfo(name = "filename") val filename: String,
                @field:ColumnInfo(name = "model") val model: String,
                @field:ColumnInfo(name = "exposure") val exposure: String,
                @field:ColumnInfo(name = "aperture") val aperture: String,
                @field:ColumnInfo(name = "iso") val iso: String,
                @field:ColumnInfo(name = "flash") val flash: String,
                @field:ColumnInfo(name = "whitebalance") val whitebalance: String,
                @field:ColumnInfo(name = "focallength") val focallength: String): BaseModel() {
    companion object {
        private const val COLUMN_IDX = DataColumns.IDX
    }

    override fun toString(): String {
        return "LocalEXIF{" +
                "id=" + idx +
                ", filename='" + filename + '\''.toString() +
                ", model='" + model + '\''.toString() +
                ", exposure='" + exposure + '\''.toString() +
                ", aperture='" + aperture + '\''.toString() +
                ", iso='" + iso + '\''.toString() +
                ", flash'" + flash + '\''.toString() +
                ", whitebalance'" + whitebalance + '\''.toString() +
                ", focallength'" + focallength +
                '}'.toString()
    }
}