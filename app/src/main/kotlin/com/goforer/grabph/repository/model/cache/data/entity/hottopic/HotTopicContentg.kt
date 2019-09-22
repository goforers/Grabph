package com.goforer.grabph.repository.model.cache.data.entity.hottopic

import androidx.room.Entity
import androidx.room.Ignore
import com.goforer.base.presentation.model.BaseModel

@Entity(primaryKeys = ["stat"])
class HotTopicContentg(val stat: String): BaseModel() {
    @Ignore
    val hotTopicContent: HotTopicContent? = null
}