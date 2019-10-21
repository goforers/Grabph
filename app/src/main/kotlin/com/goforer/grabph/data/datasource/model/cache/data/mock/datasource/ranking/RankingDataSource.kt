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

package com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.ranking

import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.model.BaseModel
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.grabph.data.datasource.model.cache.data.entity.ranking.Ranking
import java.lang.IllegalArgumentException

@MockData
class RankingDataSource {
    private var ranking: Ranking? = null

    internal fun setRanking() {
        val json = CommonUtils.getJson("mock/ranking.json")

        ranking = BaseModel.gson().fromJson(json, Ranking::class.java)
    }

    internal fun getRanking(): Ranking? {
        this.ranking?.let {

            return it
        }

        throw IllegalArgumentException("Ranking should not be null")
    }
}