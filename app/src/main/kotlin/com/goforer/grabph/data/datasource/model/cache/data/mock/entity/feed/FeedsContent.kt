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

package com.goforer.grabph.data.datasource.model.cache.data.mock.entity.feed

import androidx.room.*
import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.media.Media
import com.google.gson.reflect.TypeToken

@MockData
@Entity(tableName = "FeedsContent")
data class FeedsContent(@field:PrimaryKey
                        @field:ColumnInfo(name = "count") val count: Int): BaseModel() {
    @TypeConverters(FeedsContentItemTypeConverters::class)
    lateinit var contents: List<Content>

    class FeedsContentItemTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<Content>? {
            val type = object : TypeToken<List<Content>>() {}.type

            return BaseModel.gson().fromJson<List<Content>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<Content>): String {
            val type = object : TypeToken<List<Content>>() {}.type

            return BaseModel.gson().toJson(list, type)
        }
    }

    data class Content(@field:ColumnInfo(name = "id") val id: String,
                       @field:ColumnInfo(name = "title") val title: String,
                       @field:Embedded(prefix = "media") val media: Media)
}