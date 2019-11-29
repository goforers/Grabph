/*
 * Copyright 2019 Lukoh Nam, goForer
 *    
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License along with this program.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.goforer.grabph.presentation.vm.feed.photo

import androidx.paging.PagedList
import androidx.lifecycle.*
import com.goforer.grabph.domain.usecase.feed.photo.LoadSavedPhotoUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.savedphoto.LocalSavedPhoto
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSavedPhotoViewModel
@Inject
constructor(private val useCase: LoadSavedPhotoUseCase) : BaseViewModel<String>() {
    internal lateinit var photo: LiveData<LocalSavedPhoto>

    internal val photoFileNames: LiveData<PagedList<String>> by lazy {
        useCase.loadPhotoFileNames()
    }

    internal val photoIds: LiveData<PagedList<String>> by lazy {
        useCase.loadPhotoIds()
    }

    internal val photoItems: List<LocalSavedPhoto> by lazy {
        useCase.loadPhotoItems()
    }

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private var viewModelJob: Job? = null

    /**
     * This is the main scope for all coroutines launched by OwnerViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by viewModelScope by calling
     * viewModelJob.cancel()
     */
    private var viewModelScope: CoroutineScope? = null

    override fun setParameters(parameters: String, type: Int) {
        photo = useCase.execute(parameters)
    }

    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()

        viewModelJob?.cancel()
    }

    private fun closeWork(viewModelScope: CoroutineScope?) {
        viewModelScope?.coroutineContext?.cancelChildren()
    }

    internal fun setSavedPhotoInfo(filename: String, user: LocalSavedPhoto): MediatorLiveData<LocalSavedPhoto> {
        var photo = MediatorLiveData<LocalSavedPhoto>()

        viewModelJob = Job()
        viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob!!)
        launchWork {
            photo = useCase.savePhotoInfo(filename, user)
        }

        closeWork(viewModelScope)

        return photo
    }

    internal fun deleteSavedPhotoInfo(filename: String) {
        viewModelJob = Job()
        viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob!!)
        launchWork {
            useCase.deletePhotoInfo(filename)
        }

        closeWork(viewModelScope)
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the defaultScope.
     *              lambda the some work will do
     */
    internal inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return viewModelScope!!.launch {
            block()
        }
    }
}
