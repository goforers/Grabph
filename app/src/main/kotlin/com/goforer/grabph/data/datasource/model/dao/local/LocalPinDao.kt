package com.goforer.grabph.data.datasource.model.dao.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.LocalPin
import com.goforer.grabph.data.datasource.model.dao.BaseDao

@Dao
interface LocalPinDao: BaseDao<LocalPin> {
    @Query("SELECT * FROM LocalPin where userId = :userId")
    fun loadPins(userId: String): LiveData<List<LocalPin>>

    @Query("DELETE FROM LocalPin WHERE photoId = :photoId")
    fun deletePin(photoId: String)

    @Query("SELECT * FROM LocalPin WHERE userId = :userId AND photoId = :photoId")
    suspend fun ifExists(userId: String, photoId: String): LocalPin?
}