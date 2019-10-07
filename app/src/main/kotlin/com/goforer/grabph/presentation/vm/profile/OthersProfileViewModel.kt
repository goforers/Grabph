package com.goforer.grabph.presentation.vm.profile

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.interactor.remote.people.person.PersonRepository
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.network.response.Resource
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OthersProfileViewModel
@Inject
constructor(val interactor: PersonRepository): BaseViewModel() {
    @VisibleForTesting
    private val liveData by lazy {
        MutableLiveData<String>()
    }

    internal val person: LiveData<Resource>

    init {
        person = Transformations.switchMap(liveData) { userId ->
            userId ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(interactor.load(this@OthersProfileViewModel, userId, -1, loadType, boundType, -1))
            }
        }
    }

    internal fun setSearperId(userId: String) {
        liveData.value = userId
    }
}