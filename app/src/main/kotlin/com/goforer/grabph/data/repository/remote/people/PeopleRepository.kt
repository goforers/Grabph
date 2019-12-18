package com.goforer.grabph.data.repository.remote.people

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.room.Transaction
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.People
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Searper
import com.goforer.grabph.data.datasource.model.dao.remote.people.PeopleDao
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.remote.paging.boundarycallback.PagedListPeopleBoundaryCallback
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRepository
@Inject
constructor(private val peopleDao: PeopleDao): Repository<Query>() {
    companion object { const val METHOD = "searp.people.getPeople" }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {

        return object: NetworkBoundResource<MutableList<Searper>, PagedList<Searper>, People>(parameters.loadType, parameters.boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) { }

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

            override suspend fun handleToCache(item: MutableList<Searper>) = insert(item)

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int,
                                               pages: Int): LiveData<PagedList<Searper>> {
                val config = PagedList.Config.Builder()
                        .setInitialLoadSizeHint(itemCount * 2)
                        .setPageSize(itemCount)
                        .setPrefetchDistance(itemCount - 2)
                        .setEnablePlaceholders(true)
                        .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(peopleDao.getPeoplePaged(),  config)
                            .setBoundaryCallback(PagedListPeopleBoundaryCallback<Searper>(
                                liveData, parameters.query1 as String, pages)).build()
                }
            }

            override suspend fun loadFromNetwork() = searpService.getPeople(KEY, parameters.query1 as String, METHOD, FORMAT_JSON, INDEX)

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

    internal suspend fun removeCache() = peopleDao.clearAll()

    internal fun deleteUser(id: String) = peopleDao.delete(id)

    @WorkerThread
    internal suspend fun loadPeopleCache() = peopleDao.loadPeopleCache()

}