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

package com.goforer.grabph.data.datasource.model.cache.data.entity.search

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.grabph.data.datasource.model.cache.data.entity.DataColumns
import com.google.gson.annotations.SerializedName

@Entity(tableName = "RecentKeyword")
data class RecentKeyword(@field:PrimaryKey(autoGenerate = true)
                    @field:ColumnInfo(index = true, name = COLUMN_ID)
                    val id: Long, @field:ColumnInfo(name = "keyword")
                    val keyword: String, @field:SerializedName("date_searched")
                    @field:ColumnInfo(name = "date_searched")
                    val searchedDate: Long) {
    companion object {
        const val COLUMN_ID = DataColumns.ID
    }
}