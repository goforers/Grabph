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
import com.goforer.grabph.repository.model.cache.data.entity.photog.PhotogQuery
import com.goforer.grabph.presentation.vm.profile.OthersPhotosViewModel
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.BOUND_FROM_BACKEND

class PageListProfilePhotoBoundaryCallback<T>(private val viewModel: OthersPhotosViewModel,
                                       private val userId: String, private val pages: Int,
                                       private val calledFrom: Int): PagedList.BoundaryCallback<T>() {
    companion object {
        private var requestPage = 0
    }

    override fun onZeroItemsLoaded() {
        requestPage = 1
        load(NetworkBoundResource.LOAD_PHOTOG_PHOTO)
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) {
        if (pages > requestPage) {
            requestPage++
            load(NetworkBoundResource.LOAD_PHOTOG_PHOTO_HAS_NEXT_PAGE)
        }
    }

    private fun load(loadType: Int) {
        val query = PhotogQuery()
        query.userID = userId
        query.pages = requestPage

        viewModel.loadType = loadType
        viewModel.boundType = BOUND_FROM_BACKEND
        viewModel.calledFrom = calledFrom
        viewModel.setQuery(query)
    }
}