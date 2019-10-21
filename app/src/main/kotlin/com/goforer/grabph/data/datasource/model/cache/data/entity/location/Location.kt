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

package com.goforer.grabph.data.datasource.model.cache.data.entity.location

import androidx.room.*
import com.goforer.base.presentation.model.BaseModel
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Location")
data class Location(@field:ColumnInfo(name = "latitude") val latitude: String?,
                    @field:ColumnInfo(name = "longitude") val longitude: String?,
                    @field:ColumnInfo(name = "accuracy") val accuracy: String?,
                    @field:PrimaryKey
                    @field:ColumnInfo(name = "context") val context: String,
                    @field:Embedded(prefix = "neighbourhood") var neighbourhood: Neighbourhood?,
                    @field:Embedded(prefix = "locality") var locality: Locality?,
                    @field:Embedded(prefix = "county") val county: County?,
                    @field:Embedded(prefix = "region") var region: Region?,
                    @field:Embedded(prefix = "country") var country: Country?,
                    @field:ColumnInfo(name = "place_id")
                    @field:SerializedName("place_id") val placeId: String?,
                    @field:ColumnInfo(name = "woeid") val woeid: String?): BaseModel() {
    data class Neighbourhood(val _content: String?,
                        @field:SerializedName("place_id") val placeId: String?, val woeid: String?)

    data class Locality(val _content: String?,
                        @field:SerializedName("place_id") val placeId: String?, val woeid: String?)

    data class County(val _content: String?, @field:SerializedName("place_id") val placeId: String?,
                      val woeid: String?)

    data class Region(val _content: String?, @field:SerializedName("place_id") val placeId: String?,
                      val woeid: String?)

    data class Country(val _content: String?, @field:SerializedName("place_id") val placeId: String?,
                       val woeid: String?)
}