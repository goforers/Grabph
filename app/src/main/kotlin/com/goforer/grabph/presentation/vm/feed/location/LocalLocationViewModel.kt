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

package com.goforer.grabph.presentation.vm.feed.location

import androidx.lifecycle.*
import com.goforer.grabph.domain.usecase.feed.location.LoadLocalLocationUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.LocalLocation
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalLocationViewModel
@Inject
constructor(private val useCase: LoadLocalLocationUseCase) : BaseViewModel<String>() {
    internal lateinit var locationInfo: LiveData<LocalLocation>

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
        locationInfo = useCase.execute(parameters)
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

    internal fun setLocationInfo(filename: String, localLocation: LocalLocation): MediatorLiveData<LocalLocation> {
        var location = MediatorLiveData<LocalLocation>()

        viewModelJob = Job()
        viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob!!)
        launchWork {
            location = useCase.saveLocationInfo(filename, localLocation)
        }

        closeWork(viewModelScope)

        return location
    }

    internal fun deleteLocationInfo(filename: String) {
        viewModelJob = Job()
        viewModelScope = CoroutineScope(Dispatchers.IO + viewModelJob!!)
        launchWork {
            useCase.deleteLocationInfo(filename)
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
