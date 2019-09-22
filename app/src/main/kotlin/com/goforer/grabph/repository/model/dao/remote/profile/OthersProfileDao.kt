package com.goforer.grabph.repository.model.dao.remote.profile

import androidx.lifecycle.LiveData
import androidx.room.*
import com.goforer.grabph.repository.model.cache.data.entity.profile.Owner
import com.goforer.grabph.repository.model.dao.BaseDao

@Dao
interface OthersProfileDao:BaseDao<Owner> {

    @Query("SELECT * FROM Owner")
    fun getUserProfile(): LiveData<Owner>

    @Query("SELECT * FROM Owner")
    suspend fun loadUserProfile(): Owner

    @Query("DELETE FROM Owner")
    fun clearAll()
}