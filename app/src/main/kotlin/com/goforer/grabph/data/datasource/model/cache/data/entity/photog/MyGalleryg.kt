package com.goforer.grabph.data.datasource.model.cache.data.entity.photog

import androidx.room.Entity
import androidx.room.Ignore
import com.goforer.base.presentation.model.BaseModel

@Entity(primaryKeys = ["stat"])
data class MyGalleryg(val stat: String): BaseModel() {
    @Ignore
    val photos: Gallery? = null


}