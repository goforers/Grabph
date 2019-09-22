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

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Searper")
data class Searper(@field:PrimaryKey
                   @field:ColumnInfo(name = "id") val id: String,
                   @field:ColumnInfo(name = "username") val username: String,
                   @field:ColumnInfo(name = "name") val name: String,
                   @field:ColumnInfo(name = "rank") val rank: Int,
                   @field:SerializedName("do_i_follow") val doFollow: Boolean,
                   @Embedded(prefix = "profile_image") val profile_image: ProfileUrls): BaseModel() {

    data class ProfileUrls(@field:ColumnInfo(name = "small") val small: String,
                           @field:ColumnInfo(name = "medium") val medium: String,
                           @field:ColumnInfo(name = "large") val large: String)
}