package com.goforer.grabph.data.datasource.model.cache.data.entity.photosizes

import androidx.room.Entity
import androidx.room.Ignore
import com.goforer.base.presentation.model.BaseModel

@Entity(primaryKeys = ["stat"])
data class PhotoSizeg(val stat: String): BaseModel() {
    @Ignore
    val sizes: Sizes? = null
}