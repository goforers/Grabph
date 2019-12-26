package com.goforer.grabph.data.datasource.model.cache.data.entity.profile

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel

@Entity(tableName = "LocalPin")
data class LocalPin(
    @field:PrimaryKey val photoId: String,
    @field:ColumnInfo val authorId: String,
    @field:ColumnInfo val userId: String,
    @field:ColumnInfo val url: String,
    @field:ColumnInfo val media: String): BaseModel()