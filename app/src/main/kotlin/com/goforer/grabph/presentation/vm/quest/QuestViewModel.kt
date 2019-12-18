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

package com.goforer.grabph.presentation.vm.quest

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.quest.LoadFavoriteQuestUseCase
import com.goforer.grabph.domain.usecase.quest.LoadHotQuestUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.TopPortionQuest
import com.goforer.grabph.data.datasource.network.response.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestViewModel
@Inject
constructor(private val loadHotQuestUseCase: LoadHotQuestUseCase, private val loadFavoriteQuestUseCase: LoadFavoriteQuestUseCase): BaseViewModel<Parameters>() {
    internal lateinit var quest: LiveData<Resource>

    companion object {
        internal const val HOT_QUEST_TYPE = 0
        internal const val FAVORITE_QUEST_TYPE = 1
    }

    override fun setParameters(parameters: Parameters, type: Int) {
        when(type) {
            HOT_QUEST_TYPE -> {
                quest = loadHotQuestUseCase.execute(viewModelScope, parameters)
            }

            FAVORITE_QUEST_TYPE -> {
                quest = loadFavoriteQuestUseCase.execute(viewModelScope, parameters)
            }
        }
    }

    internal fun loadHotQuest() = liveData { loadHotQuestUseCase.loadHotQuest()?.let {
        emitSource(it)
    } }


    internal fun setTopPortionQuest(topPortionQuest: TopPortionQuest) = viewModelScope.launch { loadHotQuestUseCase.setTopPortionQuest(topPortionQuest) }

    internal fun loadFavoriteQuest(id: Long) = liveData { emitSource(loadFavoriteQuestUseCase.loadFavoriteQuest(id)) }
}