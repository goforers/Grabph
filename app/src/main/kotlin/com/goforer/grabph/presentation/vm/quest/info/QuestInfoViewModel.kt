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

package com.goforer.grabph.presentation.vm.quest.info

import androidx.lifecycle.*
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.quest.info.LoadQuestInfoUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.info.QuestInfo
import com.goforer.grabph.data.datasource.network.response.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestInfoViewModel
@Inject
constructor(private val useCase: LoadQuestInfoUseCase): BaseViewModel<Parameters>() {
    internal lateinit var mission: LiveData<Resource>

    internal var calledFrom: Int = 0

    override fun setParameters(parameters: Parameters, type: Int) {
        mission = useCase.execute(viewModelScope, parameters)
    }

    @MockData
    internal fun getQuestInfo(): LiveData<QuestInfo> =
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitSource(useCase.loadQuestInfo())
        }

    @MockData
    internal suspend fun setQuestInfo(questInfo: QuestInfo) {
        useCase.setQuestInfo(questInfo)
    }

    internal fun deleteMissionInfo() {
        viewModelScope.launch {
            useCase.deleteQuestInfo()
        }
    }

    internal suspend fun deleteQuestInfo() = useCase.deleteQuestInfo()
}