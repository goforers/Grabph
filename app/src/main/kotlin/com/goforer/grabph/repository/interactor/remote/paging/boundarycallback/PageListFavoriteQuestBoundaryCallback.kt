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

package com.goforer.grabph.repository.interactor.remote.paging.boundarycallback

import androidx.paging.PagedList
import com.goforer.grabph.presentation.vm.quest.FavoriteQuestViewModel
import com.goforer.grabph.repository.network.resource.NetworkBoundResource

class PageListFavoriteQuestBoundaryCallback<T>(private val viewModelFavorite: FavoriteQuestViewModel,
                                               private val userId: String, private val pages: Int,
                                               private val calledFrom: Int): PagedList.BoundaryCallback<T>() {
    companion object {
        private var requestPage = 0
    }

    override fun onZeroItemsLoaded() {
        requestPage = 1
        load(NetworkBoundResource.LOAD_FAVORITE_QUESTS)
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) {
        if (pages > requestPage) {
            requestPage++
            load(NetworkBoundResource.LOAD_FAVORITE_QUESTS_HAS_NEXT_PAGE)
        }
    }

    private fun load(loadType: Int) {
        viewModelFavorite.loadType = loadType
        viewModelFavorite.boundType = NetworkBoundResource.BOUND_FROM_BACKEND
        viewModelFavorite.calledFrom = calledFrom
        viewModelFavorite.setId(userId)
    }
}