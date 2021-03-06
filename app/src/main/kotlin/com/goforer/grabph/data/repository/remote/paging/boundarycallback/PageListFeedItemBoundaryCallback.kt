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

package com.goforer.grabph.data.repository.remote.paging.boundarycallback

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_FEED_UPDATE

class PageListFeedItemBoundaryCallback<T>(private val liveData: MutableLiveData<Query>,
                                          private val keyword: String, private val pages: Int): PagedList.BoundaryCallback<T>() {
    companion object {
        private var requestPage = 0
    }

    override fun onZeroItemsLoaded() {
        requestPage = 1
        setQuery(Query())
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) {
        // In this case, doesn't need to get items in next page, this body have to be implemented
        // if there are more page of Feed-SnapItem on Backend server
        /*
        if (pages > requestPage) {
            requestPage++
            load(NetworkBoundResource.LOAD_FEED_HAS_NEXT_PAGE)
        }
        */
    }

    private fun setQuery(query: Query) {
        query.query = keyword
        query.pages = requestPage
        query.loadType = LOAD_FEED_UPDATE
        query.boundType = BOUND_FROM_BACKEND

        liveData.value = query

        val input = query.pages
        if (input == liveData.value?.pages) {
            return
        }

        liveData.value = query
    }
}