package com.goforer.grabph.data.datasource.model.cache.data.entity.photog

import androidx.room.Entity
import androidx.room.Ignore
import com.goforer.base.presentation.model.BaseModel

@Entity(primaryKeys = ["total"])
data class Gallery(val page: Int, val pages: Int, val perpage: Int, val total: String): BaseModel() {
    @Ignore
    val photo: List<MyGallery>? = null
}