/*
 * Copyright 2019 Lukoh Nam, goForer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goforer.grabph.data.datasource.model.cache.data.entity.quest.info

import androidx.room.*
import com.goforer.base.presentation.model.BaseModel
import com.google.gson.reflect.TypeToken

@Entity(tableName = "QuestInfo")
data class QuestInfo(@field:PrimaryKey
                     @field:ColumnInfo(name = "id") val id: String,
                     @field:ColumnInfo(name = "counts") val counts: String,
                     @field:ColumnInfo(name = "type") val type: String,
                     @field:ColumnInfo(name = "favoriteCategory") val favoriteCategory: String,
                     @field:Embedded(prefix = "rules") val rules: Rules,
                     @field:Embedded(prefix = "bonus") val bonus: Bonus,
                     @field:ColumnInfo(name = "importantNotice") val importantNotice: String,
                     @field:Embedded(prefix = "photos") val photos: Photos): BaseModel() {

    data class Rules(val title: String,
                     @field:ColumnInfo(name = "firstRule") val firstRule: String,
                     @field:ColumnInfo(name = "secondRule") val secondRule: String)
    data class Bonus(val title: String,
                     @field:ColumnInfo(name = "firstBonus") val firstBonus: String,
                     @field:ColumnInfo(name = "secondBonus") val secondBonus: String)

    class Photos: BaseModel() {
        @TypeConverters(PhotoTypeConverters::class)
        var photo: List<Photo>? = null

        data class Photo(val image: String): BaseModel()
    }

    class PhotoTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<Photos.Photo>? {
            val type = object : TypeToken<List<Photos.Photo>>() {}.type

            return gson().fromJson<List<Photos.Photo>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<Photos.Photo>): String {
            val type = object : TypeToken<List<Photos.Photo>>() {}.type

            return gson().toJson(list, type)
        }
    }
}