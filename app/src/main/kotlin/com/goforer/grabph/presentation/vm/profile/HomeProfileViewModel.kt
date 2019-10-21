package com.goforer.grabph.presentation.vm.profile

import androidx.lifecycle.*
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.profile.LoadHomeProfileUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.HomeProfile
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.MyPhoto
import com.goforer.grabph.data.datasource.network.response.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeProfileViewModel
@Inject
constructor(private val useCase: LoadHomeProfileUseCase): BaseViewModel<Parameters>() {
    private val salesStatusLiveData = MutableLiveData<List<MyPhoto>>()

    internal lateinit var profile: LiveData<Resource>

    internal var calledFrom: Int = 0

    override fun setParameters(parameters: Parameters, type: Int) {
        profile = useCase.execute(viewModelScope, parameters)
    }

    @MockData
    internal suspend fun loadHomeProfile(): LiveData<HomeProfile>? = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) { useCase.loadHomeProfile()?.let {
        emitSource(it)
    } }

    @MockData
    internal fun setHomeProfile(homeProfile: HomeProfile) {
        viewModelScope.launch { useCase.setHomeProfile(homeProfile) }
    }

    internal fun setSalesStatusLiveData(data: List<MyPhoto>) { salesStatusLiveData.value = data }

    internal fun getSalesStatusLiveData(): LiveData<List<MyPhoto>> { return salesStatusLiveData }

    internal fun setHomeProfileLiveData(data: HomeProfile) = useCase.setHomeProfileLiveData(data)

    internal fun getHomeProfileLiveData(): LiveData<HomeProfile> = useCase.getHomeProfileLiveData()

    internal fun loadProfileFromCache() {
        viewModelScope.launch { useCase.loadProfileFromCache() }
    }
}