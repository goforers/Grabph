package com.goforer.grabph.data.datasource.model.cache.data.entity.profile

import androidx.room.Entity
import androidx.room.Ignore
import com.goforer.base.presentation.model.BaseModel

@Entity(primaryKeys = ["stat"])
data class OwnerProfile(val stat: String) : BaseModel() {
    @Ignore
    val owner: Owner? = null
}