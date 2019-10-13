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

package com.goforer.grabph.repository.model.dao.remote.people

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.goforer.grabph.repository.model.cache.data.entity.profile.Searper
import com.goforer.grabph.repository.model.dao.BaseDao

@Dao
interface PeopleDao: BaseDao<Searper> {
    @Query("SELECT * FROM Searper")
    fun getPeople(): LiveData<List<Searper>>

    @Query("SELECT * FROM Searper WHERE doFollow = :doFollow")
    fun getPeopleMyFollowing(doFollow: Boolean): LiveData<List<Searper>>

    @Query("SELECT * FROM Searper")
    fun getPeoplePaged(): DataSource.Factory<Int, Searper>

    @Query("DELETE FROM Searper")
    suspend fun clearAll()

    @Query("DELETE FROM Searper WHERE id = :id")
    fun delete(id: String)

    @Query("SELECT * FROM Searper")
    suspend fun loadPeopleCache(): List<Searper>

}