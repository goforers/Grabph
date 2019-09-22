package com.goforer.grabph.repository.interactor.remote.profile

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Transaction
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.profile.Owner
import com.goforer.grabph.repository.model.dao.remote.profile.OthersProfileDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OthersProfileRepository
@Inject
constructor(private val dao: OthersProfileDao): Repository() {

    companion object { const val METHOD = "searp.othersProfile.getOthersProfile" }

    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int,
                              boundType: Int, calledFrom: Int): LiveData<Resource> {

        return object: NetworkBoundResource<Owner, Owner, Owner>(loadType, boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) { }

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(query1)

            override suspend fun saveToCache(item: Owner) = withContext(Dispatchers.IO) { insert(item) }

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int) = dao.getUserProfile()

            override suspend fun loadFromNetwork() = searpService.getOthersProfile(KEY, query1, METHOD, FORMAT_JSON, INDEX)

            override suspend fun clearCache() = withContext(Dispatchers.IO) { dao.clearAll() }
        }.getAsLiveData()
    }

    @MockData
    internal fun loadUserProfile(): LiveData<Owner> = Transformations.map(dao.getUserProfile()) { it }

    @WorkerThread
    @MockData
    internal suspend fun setUserProfile(profile: Owner) = insert(profile)

    @MockData
    @Transaction
    private suspend fun insert(profile: Owner) = withContext(Dispatchers.IO) {
        dao.clearAll()
        dao.insert(profile)
    }

    @WorkerThread
    internal suspend fun loadUserProfileCache() = dao.loadUserProfile()

    internal fun removeCache() = dao.clearAll()
}