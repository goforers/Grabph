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

package com.goforer.grabph.data.datasource.model.dao.remote.feed

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.data.datasource.model.dao.BaseDao

/**
 * Interface for database access on FlickrFeed related operations.
 */
@Dao
interface FeedItemDao: BaseDao<FeedItem> {
    @Query("SELECT * FROM FeedItem ORDER BY _idx DESC LIMIT :itemCount")
    fun getLatestFeedItems(itemCount: Int): DataSource.Factory<Int, FeedItem>

    @Query("SELECT * FROM FeedItem ORDER BY _idx DESC")
    fun getFeedItems(): DataSource.Factory<Int, FeedItem>

    @Query("SELECT * FROM FeedItem where pinned = 1 ORDER BY date_pinned DESC")
    fun getPinnedupItems() : DataSource.Factory<Int, FeedItem>

    @Query("SELECT * FROM FeedItem where _idx = :idx")
    fun getFeedItem(idx: Long): LiveData<FeedItem>

    @Query("SELECT * FROM FeedItem ORDER BY _idx DESC")
    fun getFeeds(): MutableList<FeedItem>

    @Query("SELECT * FROM FeedItem ORDER BY _idx DESC LIMIT :itemCount")
    fun getLatestFeeds(itemCount: Int): MutableList<FeedItem>

    @Query("DELETE FROM FeedItem where _idx < :size")
    fun removeLastSeenItems(size: Int)

    @Query("DELETE FROM FeedItem")
    suspend fun clearAll()
}