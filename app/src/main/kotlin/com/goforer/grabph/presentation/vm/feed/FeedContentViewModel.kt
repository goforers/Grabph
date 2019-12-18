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

package com.goforer.grabph.presentation.vm.feed

import androidx.lifecycle.*
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.feed.LoadFeedContentUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.mock.entity.feed.FeedsContent
import com.goforer.grabph.data.datasource.network.response.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedContentViewModel
@Inject
constructor(private val useCase: LoadFeedContentUseCase): BaseViewModel<Parameters>() {
    internal lateinit var feedsContent: LiveData<Resource>

    override fun setParameters(parameters: Parameters, type: Int) {
        feedsContent = useCase.execute(viewModelScope, parameters)
    }

    @MockData
    internal val loadFeedsContent = useCase.getFeedContent()

    @MockData
    internal fun setFeedsContent(feedsContent: FeedsContent) {
        viewModelScope.launch {
            useCase.setFeedsContent(feedsContent)
        }
    }
}