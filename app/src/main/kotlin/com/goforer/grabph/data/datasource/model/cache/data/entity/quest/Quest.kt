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

package com.goforer.grabph.data.datasource.model.cache.data.entity.quest

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.DataColumns

@Entity(tableName = "Quest")
data class Quest(@field:ColumnInfo(name = COLUMN_IDX)
                 @PrimaryKey val idx: Long,
                 @field:ColumnInfo(name = COLUMN_ID) val id: String,
                 @field:ColumnInfo(name = "owner") val owner: String,
                 @field:ColumnInfo(name = "ownerName") val ownerName: String,
                 @field:ColumnInfo(name = "ownerLogo") val ownerLogo: String,
                 @field:ColumnInfo(name = "ownerImage")val ownerImage: String,
                 @field:ColumnInfo(name = "favoriteCategory") val favoriteCategory: String,
                 @field:ColumnInfo(name = "title") val title: String,
                 @field:ColumnInfo(name = "state") val state: String,
                 @field:ColumnInfo(name = "description") val description: String,
                 @field:ColumnInfo(name = "rewards") val rewards: String,
                 @field:ColumnInfo(name = "duration") val duration: Int,
                 @field:ColumnInfo(name = "photos") val photos: String
): BaseModel() {
    companion object {
        private const val COLUMN_ID = DataColumns.ID
        private const val COLUMN_IDX = DataColumns.IDX

        internal const val FAVORITE_QUEST_SORT_ALL = 0
        internal const val FAVORITE_QUEST_SORT_ONGOING = 1
        internal const val FAVORITE_QUEST_SORT_EXAMINATION = 2
        internal const val FAVORITE_QUEST_SORT_FINISHED = 3

        internal const val KEYWORD_QUEST_ALL = "All"

        internal const val SORT_QUEST_ONGOING = "ongoing"
        internal const val SORT_QUEST_UNDER_EXAMINATION = "examination"
        internal const val SORT_QUEST_FINISHED = "finished"
    }
}