/*
 * Copyright 2019 Lukoh Nam, goForer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.qeust

import com.goforer.base.presentation.utils.CommonUtils.getJson
import com.goforer.grabph.data.datasource.model.cache.data.mock.entity.TopPortionQuestIn
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.TopPortionQuest
import com.google.gson.GsonBuilder

class TopPortionQuestDataSource {
    private var topPortionQuestIn: TopPortionQuestIn? = null

    internal fun setTopPortionQuest() {
        val topPortionQuest = getJson("mock/top_portion_quest.json")
        topPortionQuestIn = GsonBuilder().serializeNulls().create().fromJson(topPortionQuest, TopPortionQuestIn::class.java)
    }

    internal fun getTopPortionQuest(): TopPortionQuest? {
        this.topPortionQuestIn?.topPortionQuest?.let {
            return this.topPortionQuestIn?.topPortionQuest
        }

        throw IllegalArgumentException("QuestInfo have to be not null")
    }
}