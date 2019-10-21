package com.goforer.grabph.data.datasource.model.dao.remote.hottopic

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.hottopic.HotTopicContent
import com.goforer.grabph.data.datasource.model.dao.BaseDao

/**
 * Interface for database access on Home-Information related operations.
 */
@Dao
interface HotTopicContentDao: BaseDao<HotTopicContent> {
    @Query("SELECT * FROM HotTopicContent")
    fun getHotTopicContent(): LiveData<HotTopicContent>

    @Query("DELETE FROM HotTopicContent")
    suspend fun clearAll()
}