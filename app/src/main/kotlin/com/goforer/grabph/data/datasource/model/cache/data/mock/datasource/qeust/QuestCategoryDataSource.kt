package com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.qeust

import com.goforer.base.presentation.utils.CommonUtils.getJson
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.QuestCategoryg
import com.google.gson.GsonBuilder
import java.lang.IllegalArgumentException

class QuestCategoryDataSource {
    private var questCategory: QuestCategoryg? = null

    internal fun setQuestCategory() {
        val json = getJson("mock/quest_category.json")
        questCategory = GsonBuilder().serializeNulls().create().fromJson(json, QuestCategoryg::class.java)
    }

    internal fun getQuestCategory(): QuestCategoryg {
        this.questCategory?.let {
            return it
        }

        throw IllegalArgumentException("Quest category should not be null ")
    }
}