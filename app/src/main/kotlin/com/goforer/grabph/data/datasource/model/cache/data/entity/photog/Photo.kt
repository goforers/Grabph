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

package com.goforer.grabph.data.datasource.model.cache.data.entity.photog

//import android.annotation.SuppressLint
import androidx.room.*
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.EXIF
//import androidx.room.TypeConverters
import androidx.room.ColumnInfo
import com.goforer.grabph.data.datasource.model.cache.data.entity.DataColumns

//import java.text.ParseException
//import java.text.SimpleDateFormat
//import java.util.*


@Entity(tableName = "Photo")
data class Photo(@field:PrimaryKey
                 @field:ColumnInfo(name = COLUMN_ID) var id: String,
                 @field:ColumnInfo(name = "secret") var secret: String?,
                 @field:ColumnInfo(name = "server") var server: String?,
                 @field:ColumnInfo(name = "farm") var farm: Int,
                 @field:ColumnInfo(name = "camera") var camera: String?,
                 @field:ColumnInfo(name = "owner") var owner: String?,
                 @field:ColumnInfo(name = "title") var title: String?,
                 @field:ColumnInfo(name = "ispublic") var ispublic: Int,
                 @field:ColumnInfo(name = "isfriend") var isfriend: Int,
                 @field:ColumnInfo(name = "isfamily") var isfamily: Int) : BaseModel() {
    companion object {
        private const val COLUMN_ID = DataColumns.ID
    }

    @ColumnInfo(name = "like")
    var like: Int = 0

    /*
    @ColumnInfo(name = "created_at")
    @TypeConverters(TimestampConverter::class)
    var createdAt: Date? = null
    */

    @Ignore
    val exif: List<EXIF>? = null

    /*
    class TimestampConverter {
        @SuppressLint("SimpleDateFormat")
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        @TypeConverter
        fun fromTimestamp(value: String?): Date? {
            if (value != null) {
                try {
                    val timeZone = TimeZone.getTimeZone("IST")
                    dateFormat.timeZone = timeZone
                    return dateFormat.parse(value)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                return null
            } else {
                return null
            }
        }


        @TypeConverter
        fun dateToTimestamp(value: Date?): String? {
            val timeZone = TimeZone.getTimeZone("IST")
            dateFormat.timeZone = timeZone
            return if (value == null) null else dateFormat.format(value)
        }
    }
    */
}
