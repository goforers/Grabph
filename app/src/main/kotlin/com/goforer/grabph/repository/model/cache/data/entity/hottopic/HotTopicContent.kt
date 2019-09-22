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

package com.goforer.grabph.repository.model.cache.data.entity.hottopic

import androidx.room.*
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.repository.model.cache.data.entity.media.Media
import com.google.gson.reflect.TypeToken

@Entity(tableName = "HotTopicContent")
data class HotTopicContent(@field:PrimaryKey
                           @field:ColumnInfo(name = "id") val id: String,
                           @field:Embedded(prefix = "media") val media: Media,
                           @field:ColumnInfo(name = "title") val title: String,
                           @field:ColumnInfo(name = "subTitle") val subTitle: String,
                           @field:Embedded(prefix = "topicContent") val topicContent: TopicContent): BaseModel() {
    data class TopicContent(val count: Int): BaseModel() {
        @TypeConverters(TopicContentItemTypeConverters::class)
        lateinit var contents: List<Content>
    }

    class TopicContentItemTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<Content>? {
            val type = object : TypeToken<List<Content>>() {}.type

            return gson().fromJson<List<Content>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<Content>): String {
            val type = object : TypeToken<List<Content>>() {}.type

            return gson().toJson(list, type)
        }
    }

    data class Content(@field:ColumnInfo(name = "id") val id: String,
                       @field:ColumnInfo(name = "description") val description: String,
                       @field:Embedded(prefix = "media") val media: Media)
}