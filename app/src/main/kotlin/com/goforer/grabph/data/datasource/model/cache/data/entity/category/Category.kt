package com.goforer.grabph.data.datasource.model.cache.data.entity.category

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel

@Entity(tableName = "Category")
data class Category(@field:PrimaryKey
                    @field:ColumnInfo(name = "id") val id: String,
                    @field:ColumnInfo(name = "type") val type: Int,
                    @field:ColumnInfo(name = "title") val title: String,
                    @field:Embedded(prefix = "photo") val photo: Photo?): BaseModel() {
    data class Photo(@field:ColumnInfo(name = "m") val m: String?)
}