package com.goforer.grabph.data.datasource.model.dao.remote.profile

import androidx.room.*
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.data.datasource.model.dao.BaseDao

@Dao
interface OthersPhotosDao: BaseDao<Photo> {

    @Query("DELETE FROM Photo")
    fun clearAll()
}