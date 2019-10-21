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

package com.goforer.grabph.data.repository.local

import androidx.lifecycle.MediatorLiveData
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.search.RecentKeyword
import com.goforer.grabph.data.datasource.model.dao.remote.search.RecentKeywordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchKeywordRepository
@Inject constructor(private val dao: RecentKeywordDao) {
    private val result = MediatorLiveData<RecentKeyword>()

    @WorkerThread
    internal suspend fun saveSearchKeyword(keyword: String, recentKeyword: RecentKeyword): MediatorLiveData<RecentKeyword> {
        save(result, keyword, recentKeyword)

        return result
    }

    internal fun loadSearchKeywords(): LiveData<List<RecentKeyword>> {
        return dao.getSearchKeywords()
    }

    @WorkerThread
    internal suspend fun deleteSearchKeyword(keyword: String) {
        delete(keyword)
    }

    private fun loadSearchKeyword(keyword: String): LiveData<RecentKeyword> {
        return dao.getSearchKeyword(keyword)
    }

    internal suspend fun delete(keyword: String) {
        withContext(Dispatchers.IO) {
            dao.deleteSearchKeyword(keyword)
        }
    }

    internal suspend fun save(result: MediatorLiveData<RecentKeyword>, searchKeyword: String, recentKeyword: RecentKeyword) {
        val keyword = withContext(Dispatchers.IO) {
            dao.insert(recentKeyword)
            loadSearchKeyword(searchKeyword)
        }

        result.addSource(keyword) {
            result.setValue(recentKeyword)
        }
    }
}