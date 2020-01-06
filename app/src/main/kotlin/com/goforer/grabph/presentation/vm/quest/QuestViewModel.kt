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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.goforer.base.annotation.MockData
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
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
constructor(private val topQuestUseCase: LoadHotQuestUseCase,
            private val bottomQuestUseCase: LoadFavoriteQuestUseCase
): BaseViewModel<Parameters>() {
    internal lateinit var quest: LiveData<Resource>
    internal val selectedKeyword = MutableLiveData<String>()
    internal val isCleared = MutableLiveData<Boolean>()

    companion object {
        internal const val HOT_QUEST_TYPE = 0
        internal const val FAVORITE_QUEST_TYPE = 1
    }

    override fun setParameters(parameters: Parameters, type: Int) {
        when(type) {
            HOT_QUEST_TYPE -> quest = topQuestUseCase.execute(viewModelScope, parameters)
            FAVORITE_QUEST_TYPE -> quest = bottomQuestUseCase.execute(viewModelScope, parameters)
        }
    }

    internal fun loadHotQuest() = liveData {
        topQuestUseCase.loadHotQuest()?.let {
            emitSource(it)
        }
    }

    internal fun setTopPortionQuest(topPortionQuest: TopPortionQuest) = viewModelScope.launch {
        topQuestUseCase.setTopPortionQuest(topPortionQuest)
    }

    internal fun loadBottomQuest() = liveData { emitSource(bottomQuestUseCase.loadLiveQuests()) }

    internal fun setKeyword(keyword: String) {
        selectedKeyword.value = keyword
    }

    @MockData
    internal fun setBottomPortionQuest(quests: MutableList<Quest>) = viewModelScope.launch { bottomQuestUseCase.insert(quests) }

    internal fun removeCache() = viewModelScope.launch {
        topQuestUseCase.removeCache()
        bottomQuestUseCase.removeCache()
        notifyCacheCleared()
    }

    private fun notifyCacheCleared() = viewModelScope.launch(Dispatchers.Main) {
        isCleared.value = true
    }
}