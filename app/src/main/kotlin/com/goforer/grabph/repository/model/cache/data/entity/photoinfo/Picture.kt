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

package com.goforer.grabph.repository.model.cache.data.entity.photoinfo

import androidx.room.*
import com.goforer.base.presentation.model.BaseModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "Picture")
data class Picture(@field:PrimaryKey
              @field:ColumnInfo(name = "id") val id: String,
              @field:ColumnInfo(name = "secret") val secret: String?,
              @field:ColumnInfo(name = "server") val server: String,
              @field:ColumnInfo(name = "farm") val farm: Int,
              @field:ColumnInfo(name = "dateuploaded") var dateuploaded: String?,
              @field:Embedded(prefix = "owner_") val owner: Owner,
              @field:Embedded(prefix = "title_") val title: Title,
              @field:Embedded(prefix = "description_") val description: Description,
              @field:Embedded(prefix = "dates_") val dates: Dates,
              @field:ColumnInfo(name = "views") val views: String?,
              @field:Embedded(prefix = "comments_") val comments: CommentCount,
              @field:Embedded(prefix = "tags_") val tags: Tags,
              @field:Embedded(prefix = "urls_") val urls: Urls,
              @field:ColumnInfo(name = "media") val media: String?): BaseModel() {

    @ColumnInfo(name = "like")
    var like: Int = 0

    data class Owner(@field:ColumnInfo(name = "nsid")
                var nsid: String, @field:ColumnInfo(name = "username") val username: String?,
                @field:ColumnInfo(name = "realname") val realname: String?,
                @field:ColumnInfo(name = "location") val location: String?,
                @field:ColumnInfo(name = "iconserver") val iconserver: String?,
                @field:ColumnInfo(name = "iconfarm") val iconfarm: Int): BaseModel()

    data class Title(val _content: String): BaseModel()

    data class Description(val _content: String): BaseModel()

    data class Dates(@field:ColumnInfo(name = "posted") val posted: String,
                @field:ColumnInfo(name = "taken") val taken: String?,
                @field:ColumnInfo(name = "takengranularity") val takengranularity: Int,
                @field:ColumnInfo(name = "takenunknown") val takenunknown: Int,
                @field:ColumnInfo(name = "lastupdate") val lastupdate: String?): BaseModel()

    data class CommentCount(var _content: String?): BaseModel()

    class Tags: BaseModel() {
        @TypeConverters(TagTypeConverters::class)
        var tag: List<Tag>? = null

        data class Tag(@field:ColumnInfo(name = "id") val id: String,
                  @field:ColumnInfo(name = "authorname") val authorname: String?,
                  @field:ColumnInfo(name = "raw") val raw: String?,
                  @field:ColumnInfo(name = "_content") val content: String?): BaseModel()
    }

    data class Urls(@field:ColumnInfo(name = "id") val id: Int) {
        @TypeConverters(UrlTypeConverters::class)
        var url: List<Url>? = null

        class Url(@field:ColumnInfo(name = "type")
                  val type: String?, @field:ColumnInfo(name = "_content")
                  val content: String?) : BaseModel()
    }

    class TagTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<Tags.Tag>? {
            val gson = Gson()
            val type = object : TypeToken<List<Tags.Tag>>() {}.type

            return gson.fromJson<List<Tags.Tag>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<Tags.Tag>): String {
            val gson = Gson()
            val type = object : TypeToken<List<Tags.Tag>>() {}.type

            return gson.toJson(list, type)
        }
    }

    class UrlTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<Urls.Url>? {
            val gson = Gson()
            val type = object : TypeToken<List<Urls.Url>>() {}.type

            return gson.fromJson<List<Urls.Url>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<Urls.Url>): String {
            val gson = Gson()
            val type = object : TypeToken<List<Urls.Url>>() {}.type

            return gson.toJson(list, type)
        }
    }
}