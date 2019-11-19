package com.goforer.grabph.presentation.vm.profile

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.profile.LoadMyProfileUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.MyPhoto
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.domain.usecase.profile.LoadMyGalleryUseCase
import com.goforer.grabph.domain.usecase.profile.LoadMyPinUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeProfileViewModel
@Inject
constructor(private val useCase: LoadMyProfileUseCase,
    private val galleryUseCase: LoadMyGalleryUseCase,
    private val pinUseCase: LoadMyPinUseCase
): BaseViewModel<Parameters>() {
    private val salesStatusLiveData = MutableLiveData<List<MyPhoto>>()

    internal lateinit var profile: LiveData<Resource>

    internal lateinit var gallery: LiveData<Resource>
    private lateinit var pagedGallery: LiveData<PagedList<MyGallery>>
    private val queryLiveData = MutableLiveData<Parameters>()

    internal val liveGallery: LiveData<PagedList<MyGallery>> = Transformations.switchMap(queryLiveData) {
        galleryUseCase.test(it)
    }

    internal lateinit var pin: LiveData<Resource>

    internal var calledFrom: Int = 0

    override fun setParameters(parameters: Parameters, type: Int) {
        profile = useCase.execute(viewModelScope, parameters)
    }

    fun setParametersMyGallery(parameters: Parameters) {
        // gallery = galleryUseCase.execute(viewModelScope, parameters)
        queryLiveData.postValue(parameters)
    }

    fun setParametersMyPin(parameters: Parameters) {
        pin = pinUseCase.execute(viewModelScope, parameters)
    }


    internal fun getSalesStatusLiveData(): LiveData<List<MyPhoto>> { return salesStatusLiveData }

    internal fun setSalesStatusLiveData(data: List<MyPhoto>) { salesStatusLiveData.value = data }

    internal fun removeGallery() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            galleryUseCase.removeCache()
        }
    }
}