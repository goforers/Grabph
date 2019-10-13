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

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.goforer.grabph.repository.model.cache.data.entity.Query
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.BOUND_FROM_BACKEND
import javax.inject.Inject

class PageListFavoritePhotoBoundaryCallback<T>(private val liveData: MutableLiveData<Query>,
                                               private val userId: String, private val pages: Int): PagedList.BoundaryCallback<T>() {
    @field:Inject
    internal lateinit var query: Query

    companion object {
        private var requestPage = 0
    }

    override fun onZeroItemsLoaded() {
        requestPage = 1
        setQuery(query)
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) {
        if (pages > requestPage) {
            requestPage++
            setQuery(query)
        }
    }

    private fun setQuery(query: Query) {
        query.query = userId
        query.pages = requestPage
        query.boundType = BOUND_FROM_BACKEND
        liveData.value = query

        val input = query.pages
        if (input == liveData.value?.pages) {
            return
        }

        liveData.value = query
    }
}