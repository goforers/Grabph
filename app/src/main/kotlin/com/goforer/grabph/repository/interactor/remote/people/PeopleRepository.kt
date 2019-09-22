package com.goforer.grabph.repository.interactor.remote.people

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.room.Transaction
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.presentation.vm.people.PeopleViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.profile.People
import com.goforer.grabph.repository.model.cache.data.entity.profile.Searper
import com.goforer.grabph.repository.model.dao.remote.people.PeopleDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.paging.boundarycallback.PagedListPeopleBoundaryCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRepository
@Inject
constructor(private val peopleDao: PeopleDao): Repository() {
    companion object { const val METHOD = "searp.people.getPeople" }

    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int,
                              boundType: Int, calledFrom: Int): LiveData<Resource> {

        return object: NetworkBoundResource<MutableList<Searper>, PagedList<Searper>, People>(loadType, boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) { }

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(query1)

            override suspend fun saveToCache(item: MutableList<Searper>) = insert(item)

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int,
                                               pages: Int): LiveData<PagedList<Searper>> {
                val config = PagedList.Config.Builder()
                        .setInitialLoadSizeHint(10)
                        .setPageSize(20)
                        .setPrefetchDistance(10)
                        .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(peopleDao.getPeoplePaged(),  config)
                            .setBoundaryCallback(PagedListPeopleBoundaryCallback<Searper>(
                                    viewModel as PeopleViewModel, query1, pages, calledFrom)).build()
                }
            }

            override suspend fun loadFromNetwork() = searpService.getPeople(KEY, query1, METHOD, FORMAT_JSON, INDEX)

            override suspend fun clearCache() = peopleDao.clearAll()
        }.getAsLiveData()
    }

    @MockData
    internal fun loadPeople(): LiveData<List<Searper>> = Transformations.map(peopleDao.getPeople()) { it }

    @WorkerThread
    @MockData
    internal suspend fun setPeople(people: MutableList<Searper>) = insert(people)

    @MockData
    @Transaction
    private suspend fun insert(people: MutableList<Searper>) = withContext(Dispatchers.IO) {
        peopleDao.clearAll()
        peopleDao.insert(people)
    }

    internal suspend fun update(searper: Searper) = peopleDao.update(searper)

    internal fun removeCache() = peopleDao.clearAll()

    internal fun deleteUser(id: String) = peopleDao.delete(id)

    @WorkerThread
    internal suspend fun loadPeopleCache() = peopleDao.loadPeopleCache()

}