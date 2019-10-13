package com.goforer.grabph.repository.model.dao.remote.ranking

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.goforer.grabph.repository.model.cache.data.entity.ranking.Ranking
import com.goforer.grabph.repository.model.dao.BaseDao

@Dao
interface RankingDao: BaseDao<Ranking> {
    @Query("SELECT * FROM Ranking")
    fun getRanking(): LiveData<Ranking>

    @Query("SELECT * FROM Ranking")
    suspend fun loadRanking(): Ranking

    @Query("DELETE FROM Ranking")
    suspend fun clearAll()
}