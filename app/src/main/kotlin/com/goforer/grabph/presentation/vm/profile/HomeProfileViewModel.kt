package com.goforer.grabph.presentation.vm.profile

import androidx.lifecycle.*
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
    internal lateinit var pin: LiveData<Resource>

    internal var calledFrom: Int = 0

    override fun setParameters(parameters: Parameters, type: Int) {
        profile = useCase.execute(parameters)
    }

    fun setParametersMyGallery(parameters: Parameters) {
        gallery = galleryUseCase.execute(parameters)
    }

    fun setParametersMyPin(parameters: Parameters) {
        pin = pinUseCase.execute(parameters)
    }

    internal fun removeGallery() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            galleryUseCase.removeCache()
        }
    }
}