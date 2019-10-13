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

package com.goforer.grabph.repository.model.dao.remote.quest

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.goforer.grabph.repository.model.cache.data.entity.quest.Quest
import com.goforer.grabph.repository.model.dao.BaseDao

/**
 * Interface for database access on Quest related operations.
 */
@Dao
interface FavoriteQuestDao: BaseDao<Quest> {
    @Query("SELECT * FROM Quest ORDER BY _idx ASC")
    fun getQuests(): DataSource.Factory<Int, Quest>

    @Query("SELECT * FROM Quest ORDER BY _idx DESC LIMIT :itemCount")
    fun getLatestMissions(itemCount: Int): DataSource.Factory<Int, Quest>

    @Query("SELECT * FROM Quest where _idx = :idx")
    fun getQuest(idx: Long): LiveData<Quest>

    @Query("SELECT * FROM Quest ORDER BY _idx DESC")
    suspend fun getQuestItems(): List<Quest>

    @Query("DELETE FROM Quest WHERE _id = :questId")
    fun deleteByQuestId(questId: String)

    @Query("DELETE FROM Quest")
    suspend fun clearAll()
}