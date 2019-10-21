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

package com.goforer.grabph.data.datasource.model.cache.data.entity.profile

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.DataColumns

@Entity(tableName = "Person")
data class Person(@field:PrimaryKey
                  @field:ColumnInfo(index = true, name = COLUMN_ID) val id: String,
                  @field:ColumnInfo(name = "iconserver") val iconserver: String,
                  @field:ColumnInfo(name = "iconfarm") val iconfarm: Int,
                  @field:Embedded(prefix = "username_") val username: Username?,
                  @field:Embedded(prefix = "realname_") val realname: Realname?,
                  @field:Embedded(prefix = "location_") val location: Location?,
                  @field:Embedded(prefix = "description_") val description: Description?,
                  @field:Embedded(prefix = "photosurl_") val photosurl: Photosurl?,
                  @field:Embedded(prefix = "profileurl_") val profileurl: Profileurl?,
                  @field:Embedded(prefix = "mobileurl_") val mobileurl: Mobileurl?,
                  @field:Embedded(prefix = "photos_") val photos: Photos?): BaseModel() {

    companion object {
        private const val COLUMN_ID = DataColumns.ID

        internal const val GRADE_BEGINNER = 0
        internal const val GRADE_1 = 1
        internal const val GRADE_2 = 2
        internal const val GRADE_3 = 3
        internal const val GRADE_4 = 4
        internal const val GRADE_EXPERT = 5
    }

    @ColumnInfo(name = "grade")
    var grade: String? = null

    @ColumnInfo(name = "followers")
    var followers: String? = null

    @ColumnInfo(name = "followings")
    var followings: String? = null

    data class Username(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Realname(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Location(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Description(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Photosurl(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Profileurl(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Mobileurl(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Photos(@Embedded(prefix = "firstdatetaken_") val firstdatetaken: Firstdatetaken?,
                      @Embedded(prefix = "firstdate_") val firstdate: Firstdate?,
                      @Embedded(prefix = "count_") val count: Count?) {
        data class Firstdatetaken(@field:ColumnInfo(name = "_content") val _content: String?)

        data class Firstdate(@field:ColumnInfo(name = "_content") val _content: String?)

        data class Count(@field:ColumnInfo(name = "_content") val _content: String?)
    }
}