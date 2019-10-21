package com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.hottopic

import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.model.BaseModel
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.grabph.data.datasource.model.cache.data.entity.hottopic.HotTopicContent
import java.lang.IllegalArgumentException

@MockData
class HotTopicContentDataSource {
    private var hotTopicContent: HotTopicContent? = null

    internal fun setHotTopicContent() {
        val topicContent = CommonUtils.getJson("mock/hot_topic_content.json")

        this.hotTopicContent = BaseModel.gson().fromJson(topicContent, HotTopicContent::class.java)
    }

    internal fun getHotTopicContent(): HotTopicContent? {

        this.hotTopicContent?.let{
            return hotTopicContent
        }

        throw IllegalArgumentException("Profile should not be not null")
    }
}