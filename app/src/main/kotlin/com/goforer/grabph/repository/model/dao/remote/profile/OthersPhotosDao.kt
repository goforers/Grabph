package com.goforer.grabph.repository.model.dao.remote.profile

import androidx.room.*
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photo
import com.goforer.grabph.repository.model.dao.BaseDao

@Dao
interface OthersPhotosDao: BaseDao<Photo> {

    @Query("DELETE FROM Photo")
    fun clearAll()
}