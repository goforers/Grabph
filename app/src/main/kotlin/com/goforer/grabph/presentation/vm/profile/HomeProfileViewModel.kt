package com.goforer.grabph.presentation.vm.profile

import androidx.lifecycle.*
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.LocalPin
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.profile.LoadMyProfileUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
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
    internal lateinit var profile: LiveData<Resource>
    internal lateinit var gallery: LiveData<Resource>
    internal lateinit var pin: LiveData<List<LocalPin>>
    internal lateinit var isPinned: MutableLiveData<Boolean>
    internal val selectedPagerFab = MutableLiveData<Int>()

    internal var calledFrom: Int = 0

    companion object {
        internal const val PAGER_POSITION_GALLERY = 0
        internal const val PAGER_POSITION_PIN = 1
    }

    override fun setParameters(parameters: Parameters, type: Int) {
        profile = useCase.execute(viewModelScope, parameters)
    }

    fun setParametersMyGallery(parameters: Parameters) {
        gallery = galleryUseCase.execute(viewModelScope, parameters)
    }

    fun setParametersMyPin(parameters: String) {
        pin = pinUseCase.execute(viewModelScope, parameters)
    }

    internal fun removeGallery() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            galleryUseCase.removeCache()
        }
    }

    internal fun setPagerFab(position: Int) {
        selectedPagerFab.value = position
    }
}