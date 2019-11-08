package com.goforer.grabph.data.datasource.model.dao.remote.profile

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.MyProfile
import com.goforer.grabph.data.datasource.model.dao.BaseDao

@Dao
interface MyProfileDao: BaseDao<MyProfile> {
    @Query("SELECT * FROM MyProfile WHERE nsid = :userId")
    fun getMyProfile(userId: String): LiveData<MyProfile>

    @Query("DELETE FROM MyProfile")
    suspend fun clearAll()
}