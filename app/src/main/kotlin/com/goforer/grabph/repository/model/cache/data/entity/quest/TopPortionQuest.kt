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

package com.goforer.grabph.repository.model.cache.data.entity.quest

import androidx.room.*
import com.goforer.base.presentation.model.BaseModel
import com.google.gson.reflect.TypeToken

@Entity(tableName = "TopPortionQuest")
data class TopPortionQuest(@field:PrimaryKey val itemcount: Int,
                           @field:Embedded(prefix = "hotQuest") val hotQuest: HotQuest,
                           @field:Embedded(prefix = "favoriteKeyword") val favoriteKeyword: FavoriteKeyword): BaseModel() {

    data class HotQuest(val title: String) : BaseModel() {
        @TypeConverters(MissionListTypeConverters::class)
        lateinit var quests: List<Quest>
    }

    data class FavoriteKeyword(val title: String) : BaseModel() {
        @TypeConverters(KeywordTypeConverters::class)
        lateinit var keywords: List<Keyword>

        data class Keyword(val id: String, val title: String, val image: String) : BaseModel()
    }

    class KeywordTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<FavoriteKeyword.Keyword>? {
            val type = object : TypeToken<List<FavoriteKeyword.Keyword>>() {}.type

            return gson().fromJson<List<FavoriteKeyword.Keyword>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<FavoriteKeyword.Keyword>): String {
            val type = object : TypeToken<List<FavoriteKeyword.Keyword>>() {}.type

            return gson().toJson(list, type)
        }
    }

    class MissionListTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<Quest>? {
            val type = object : TypeToken<List<Quest>>() {}.type

            return gson().fromJson<List<Quest>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<Quest>): String {
            val type = object : TypeToken<List<Quest>>() {}.type

            return gson().toJson(list, type)
        }
    }
}