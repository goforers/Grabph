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

package com.goforer.grabph.repository.model.cache.data.entity.home

import androidx.room.*
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.repository.model.cache.data.entity.DataColumns
import com.goforer.grabph.repository.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.repository.model.cache.data.entity.category.Category
import com.goforer.grabph.repository.model.cache.data.entity.media.Media
import com.goforer.grabph.repository.model.cache.data.entity.quest.Quest
import com.google.gson.reflect.TypeToken

@Entity(tableName = "Home")
data class Home(@field:PrimaryKey val itemcount: Int, val background: String,
                @field:Embedded(prefix = "hotSearp") val hotSearp: HotSearp,
                @field:Embedded(prefix = "popQuest") val popQuest: PopQuest,
                @field:Embedded(prefix = "popSearper") val popSearper: PopSearper,
                @field:Embedded(prefix = "phototype") val phototype: PhotoType): BaseModel() {
    companion object {
        private const val COLUMN_ID = DataColumns.ID

        internal const val GRADE_BEGINNER = 0
        internal const val GRADE_1 = 1
        internal const val GRADE_2 = 2
        internal const val GRADE_3 = 3
        internal const val GRADE_4 = 4
        internal const val GRADE_EXPERT = 5
    }

    data class HotSearp(val title: String,
                        @field:Embedded(prefix = "hotTopic") val hotTopic: HotTopic,
                        @field:Embedded(prefix = "quest") val quest: Quest,
                        @field:Embedded(prefix = "searperPhoto") val searperPhoto: FeedItem,
                        @field:Embedded(prefix = "category") val category: Category): BaseModel()

    data class HotTopic(@field:ColumnInfo(name = "title") val title: String,
                        @field:ColumnInfo(name = COLUMN_ID) val id: String,
                        @field:Embedded(prefix = "media") val media: Media): BaseModel()

    data class PopQuest(val title: String): BaseModel() {
        @TypeConverters(QuestItemTypeConverters::class)
        lateinit var quest: List<Quest>
    }

    class QuestItemTypeConverters {
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

    data class PopSearper(val title: String): BaseModel() {
        @TypeConverters(SearperTypeConverters::class)
        lateinit var searper: List<Searper>

        data class Searper(val id: String, val iconserver: String, val iconfarm: Int, val grade: String,
                           @field:Embedded(prefix = "username_") val username: Username?,
                           @field:Embedded(prefix = "realname_") val realname: Realname?) : BaseModel()

        data class Username(val _content: String?)

        data class Realname(val _content: String?)
    }

    class SearperTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<PopSearper.Searper>? {
            val type = object : TypeToken<List<PopSearper.Searper>>() {}.type

            return gson().fromJson<List<PopSearper.Searper>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<PopSearper.Searper>): String {
            val type = object : TypeToken<List<PopSearper.Searper>>() {}.type

            return gson().toJson(list, type)
        }
    }

    data class PhotoType(val title: String): BaseModel() {
        @TypeConverters(CategoryTypeConverters::class)
        lateinit var category: List<Category>
    }

    class CategoryTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<Category>? {
            val type = object : TypeToken<List<Category>>() {}.type

            return gson().fromJson<List<Category>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<Category>): String {
            val type = object : TypeToken<List<Category>>() {}.type

            return gson().toJson(list, type)
        }
    }
}