package com.goforer.grabph.data.repository.local

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.LocalPin
import com.goforer.grabph.data.datasource.model.dao.local.LocalPinDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPinRepository
@Inject constructor(private val dao: LocalPinDao) {
    private val result = MediatorLiveData<LocalPin>()

    @WorkerThread
    internal suspend fun saveLocalPin(localPin: LocalPin): MediatorLiveData<LocalPin> {
        save(result, localPin)
        return result
    }

    internal fun loadLocalPin(userId: String): LiveData<List<LocalPin>> {
        return dao.loadPins(userId)
    }

    @WorkerThread
    internal suspend fun deleteLocalPin(photoId: String) {
        delete(photoId)
    }

    internal suspend fun checkLocalPin(userId: String, photoId: String): Boolean {
        return (dao.ifExists(userId, photoId) != null)
    }

    private suspend fun save(result: MediatorLiveData<LocalPin>, localPin: LocalPin) {
        val pin = withContext(Dispatchers.IO) {
            dao.insert(localPin)
            loadLocalPin(localPin.photoId)
        }

        result.addSource(pin) {
            result.setValue(localPin)
        }
    }

    private suspend fun delete(photoId: String) {
        withContext(Dispatchers.IO) {
            dao.deletePin(photoId)
        }
    }
}