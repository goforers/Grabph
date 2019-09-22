package com.goforer.grabph.repository.interactor.remote.paging.boundarycallback

import androidx.paging.PagedList
import com.goforer.grabph.presentation.vm.people.PeopleViewModel
import com.goforer.grabph.repository.network.resource.NetworkBoundResource

class PagedListPeopleBoundaryCallback<T>(private val peopleViewModel: PeopleViewModel,
                                         private val userId: String, private val pages: Int,
                                         private val calledFrom: Int): PagedList.BoundaryCallback<T>() {

    companion object {
        private var requestPage = 0
    }

    override fun onZeroItemsLoaded() {
        requestPage = 1
        load(NetworkBoundResource.LOAD_PEOPLE)
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) {
        if (pages > requestPage) {
            requestPage ++
            load(NetworkBoundResource.LOAD_PEOPLE_HAS_NEXT_PAGE)
        }
    }

    private fun load(loadType: Int) {
        peopleViewModel.loadType = loadType
        peopleViewModel.boundType = NetworkBoundResource.BOUND_FROM_BACKEND
        peopleViewModel.calledFrom = calledFrom
        peopleViewModel.setId(userId)
    }
}