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
import com.google.gson.reflect.TypeToken

@Entity(tableName = "HomeProfile")
data class HomeProfile(@field:PrimaryKey val itemCount: Int,
                       @field:ColumnInfo(name = "id") val id: String?,
                       @field:ColumnInfo(name = "profilePhoto") val profilePhoto: String?,
                       @field:ColumnInfo(name = "grade") val grade: Int,
                       @field:ColumnInfo(name = "photo") val photo: Int,
                       @field:ColumnInfo(name = "revenue") val revenue: Int,
                       @field:ColumnInfo(name = "point") val point: Int,
                       @field:ColumnInfo(name = "follower") val follower: Int,
                       @field:ColumnInfo(name = "following") val following: Int,
                       @field:ColumnInfo(name = "like") val like: Int,
                       @field:ColumnInfo(name = "sold") val sold: Int,
                       @field:ColumnInfo(name = "realname") val realname: String?,
                       @field:ColumnInfo(name = "nickname") val nickname: String?,
                       @field:ColumnInfo(name = "coverletter") val coverletter: String?,
                       @field:Embedded(prefix = "myPhotos_") val myPhotos: MyPagePhotos,
                       @field:Embedded(prefix = "sellPhotos_") val sellPhotos: MyPagePhotos
                       ): BaseModel() {

    data class MyPagePhotos(@field:ColumnInfo(name = "count") val count: Int): BaseModel() {
        @TypeConverters(MyPhotoListTypeConverters::class)
        lateinit var photos: List<MyPhoto>
    }

    class MyPhotoListTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<MyPhoto>? {
            val type = object : TypeToken<List<MyPhoto>>() {}.type

            return gson().fromJson<List<MyPhoto>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<MyPhoto>): String {
            val type = object : TypeToken<List<MyPhoto>>() {}.type

            return gson().toJson(list, type)
        }
    }
}