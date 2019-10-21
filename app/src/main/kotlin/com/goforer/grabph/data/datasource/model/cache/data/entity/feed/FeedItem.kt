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

@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.goforer.grabph.data.datasource.model.cache.data.entity.feed

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.DataColumns
import com.google.gson.annotations.SerializedName

@Entity(tableName = "FeedItem")
data class FeedItem(var title: String, val link: String?,
                    @field:Embedded(prefix = "media_") val media: Media,
                    @field:SerializedName("date_taken") val takenDate: String?,
                    val description: String?, @field:ColumnInfo(name = "published")
                    @field:SerializedName("published") val publishedDate: String?,
                    val author: String?,
                    @field:SerializedName("author_id") val authorId: String?,
                    val tags: String?): BaseModel() {
    companion object {
        private const val COLUMN_IDX = DataColumns.IDX
        private const val COLUMN_ID = DataColumns.ID
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_IDX)
    var idx: Long = 0
    @ColumnInfo(name = COLUMN_ID)
    var id: String = ""

    @ColumnInfo(name = "pinned")
    var isPinned: Boolean = false

    @ColumnInfo(name = "searper_photo_url")
    var searperPhotoUrl: String? = null

    @ColumnInfo(name = "searper_name")
    var searperName: String? = null

    @ColumnInfo(name = "like")
    var like: Int = 1

    @SerializedName("date_pinned")
    @ColumnInfo(name = "date_pinned")
    var pinnedDate: Long = 0

    val width: Int?
        get() {
            val description = description?.let { StringBuilder(it) }

            return description?.indexOf("\" height")?.let {
                description.substring(description.indexOf("width=") + 7,
                        it)
            }?.let { Integer.parseInt(it) }
        }

    val height: Int?
        get() {
            val description = description?.let { StringBuilder(it) }

            return description?.substring(description.indexOf("height=") + 8,
                    description.indexOf("\" alt="))?.let { Integer.parseInt(it) }
        }

    data class Media(@field:ColumnInfo(name = "m") var m: String?)
}
