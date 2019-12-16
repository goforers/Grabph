package com.goforer.grabph.data.datasource.model.dao.remote.feed.photo

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery
import com.goforer.grabph.data.datasource.model.dao.BaseDao

@Dao
interface MyGalleryDao: BaseDao<MyGallery> {
    @Query("SELECT * FROM MyGallery WHERE owner = :userId")
    fun getPhotos(userId: String): DataSource.Factory<Int, MyGallery>

    @Query("SELECT * FROM MyGallery WHERE owner = :userId")
    fun getPhotoList(userId: String): List<MyGallery>

    @Query("DELETE FROM MyGallery WHERE _id = :photoId")
    fun deleteByPhotoId(photoId: String)

    @Query("DELETE FROM MyGallery")
    suspend fun clearAll()
}