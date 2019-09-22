package com.goforer.grabph.presentation.vm.profile

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.model.cache.data.entity.profile.HomeProfile
import com.goforer.grabph.repository.model.cache.data.entity.profile.MyPhoto
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.profile.HomeProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeProfileViewModel
@Inject
constructor(private val interactor: HomeProfileRepository): BaseViewModel() {
    @VisibleForTesting
    private val liveData by lazy {
        MutableLiveData<String>()
    }

    private val homeProfileLiveData = MutableLiveData<HomeProfile>()

    private val salesStatusLiveData = MutableLiveData<List<MyPhoto>>()

    internal val profile: LiveData<Resource>

    internal var calledFrom: Int = 0

    init {
        profile = Transformations.switchMap(liveData) { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(interactor.load(this@HomeProfileViewModel, query!!, -1, loadType, boundType, calledFrom))
            }
        }
    }

    @MockData
    internal suspend fun loadHomeProfile(): LiveData<HomeProfile>? = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) { emitSource(interactor.loadHomeProfile()) }

    internal fun setId(id: String) {
        liveData.value = id

        val input = id.toLowerCase(Locale.getDefault()).trim { it <= ' '}

        if (input == liveData.value) {
            return
        }
        liveData.value = input
    }

    @MockData
    internal fun setHomeProfile(homeProfile: HomeProfile) {
        viewModelScope.launch { interactor.setHomeProfile(homeProfile) }
    }

    internal fun setSalesStatusLiveData(data: List<MyPhoto>) { salesStatusLiveData.value = data }

    internal fun getSalesStatusLiveData(): LiveData<List<MyPhoto>> { return salesStatusLiveData }

    internal fun setHomeProfileLiveData(data: HomeProfile) { homeProfileLiveData.value = data }

    internal fun getHomeProfileLiveData(): LiveData<HomeProfile> { return homeProfileLiveData }

    internal fun loadProfileFromCache() {
        viewModelScope.launch { interactor.loadProfileCache()?.let { setHomeProfileLiveData(it) } }
    }
}