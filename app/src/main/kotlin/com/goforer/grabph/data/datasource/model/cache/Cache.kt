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

package com.goforer.grabph.data.datasource.model.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import com.goforer.grabph.data.datasource.model.cache.data.entity.category.CPhoto
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.data.datasource.model.cache.data.entity.ranking.Ranking
import com.goforer.grabph.data.datasource.model.cache.data.entity.search.RecentKeyword
import com.goforer.grabph.data.datasource.model.cache.data.entity.category.Category
import com.goforer.grabph.data.datasource.model.cache.data.entity.comments.Comment
import com.goforer.grabph.data.datasource.model.cache.data.entity.comments.Comments
import com.goforer.grabph.data.datasource.model.cache.data.entity.comments.PhotoComments
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.EXIF
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.LocalEXIF
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.data.datasource.model.cache.data.entity.home.Home
import com.goforer.grabph.data.datasource.model.cache.data.entity.hottopic.HotTopicContent
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.LocalLocation
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.Location
import com.goforer.grabph.data.datasource.model.cache.data.mock.entity.feed.FeedsContent
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photog
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photos
import com.goforer.grabph.data.datasource.model.cache.data.entity.photoinfo.PhotoInfo
import com.goforer.grabph.data.datasource.model.cache.data.entity.photoinfo.Picture
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.*
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Questg
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.TopPortionQuest
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.info.QuestInfo
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.info.QuestsInfog
import com.goforer.grabph.data.datasource.model.cache.data.entity.savedphoto.LocalSavedPhoto
import com.goforer.grabph.data.datasource.model.dao.local.LocalEXIFDao
import com.goforer.grabph.data.datasource.model.dao.local.LocalLocationDao
import com.goforer.grabph.data.datasource.model.dao.local.LocalSavedPhotoDao
import com.goforer.grabph.data.datasource.model.dao.remote.category.CategoryDao
import com.goforer.grabph.data.datasource.model.dao.remote.category.photo.CPhotoDao
import com.goforer.grabph.data.datasource.model.dao.remote.comment.CommentDao
import com.goforer.grabph.data.datasource.model.dao.remote.feed.exif.EXIFDao
import com.goforer.grabph.data.datasource.model.dao.remote.feed.FeedItemDao
import com.goforer.grabph.data.datasource.model.dao.remote.feed.FeedsContentDao
import com.goforer.grabph.data.datasource.model.dao.remote.feed.location.LocationDao
import com.goforer.grabph.data.datasource.model.dao.remote.feed.photo.MyPhotoDao
import com.goforer.grabph.data.datasource.model.dao.remote.feed.photo.PhotoDao
import com.goforer.grabph.data.datasource.model.dao.remote.feed.photo.PhotoInfoDao
import com.goforer.grabph.data.datasource.model.dao.remote.home.HomeDao
import com.goforer.grabph.data.datasource.model.dao.remote.hottopic.HotTopicContentDao
import com.goforer.grabph.data.datasource.model.dao.remote.people.PeopleDao
import com.goforer.grabph.data.datasource.model.dao.remote.people.owner.OwnerDao
import com.goforer.grabph.data.datasource.model.dao.remote.people.person.PersonDao
import com.goforer.grabph.data.datasource.model.dao.remote.profile.HomeProfileDao
import com.goforer.grabph.data.datasource.model.dao.remote.profile.OthersPhotosDao
import com.goforer.grabph.data.datasource.model.dao.remote.quest.FavoriteQuestDao
import com.goforer.grabph.data.datasource.model.dao.remote.quest.QuestInfoDao
import com.goforer.grabph.data.datasource.model.dao.remote.quest.TopPortionQuestDao
import com.goforer.grabph.data.datasource.model.dao.remote.ranking.RankingDao
import com.goforer.grabph.data.datasource.model.dao.remote.search.RecentKeywordDao

/**
 * Main cache description.
 */
@Database(entities = [
        FeedItem::class, Person::class, EXIF::class, LocalEXIF::class,
        RecentKeyword::class, Location::class, Photog::class, Photos::class, Photo::class,
        PhotoComments::class, Comments::class, Comment::class, PhotoInfo::class, Picture::class,
        LocalLocation::class, Owner::class, LocalSavedPhoto::class, Questg::class, Quest::class,
        QuestsInfog::class, QuestInfo::class, Home::class, CPhoto::class, TopPortionQuest::class,
        Category::class, HomeProfile::class, MyPhoto::class, Searper::class, People::class,
        HotTopicContent::class, Ranking::class, FeedsContent::class], version = 1)
abstract class Cache : RoomDatabase() {
    abstract fun feedItemDao(): FeedItemDao
    abstract fun personDao(): PersonDao
    abstract fun exifDao(): EXIFDao
    abstract fun localEXIFDao(): LocalEXIFDao
    abstract fun searchKeywordDao(): RecentKeywordDao
    abstract fun locationDao(): LocationDao
    abstract fun photoDao(): PhotoDao
    abstract fun commentDao(): CommentDao
    abstract fun photoInfoDao(): PhotoInfoDao
    abstract fun localLocationDao(): LocalLocationDao
    abstract fun ownerDao(): OwnerDao
    abstract fun localSavedPhotoDao(): LocalSavedPhotoDao
    abstract fun favoriteQuestDao(): FavoriteQuestDao
    abstract fun topPortionQuestDao(): TopPortionQuestDao
    abstract fun questInfoDao(): QuestInfoDao
    abstract fun homeDao(): HomeDao
    abstract fun cphotoDao(): CPhotoDao
    abstract fun photoTypeDao(): CategoryDao
    abstract fun homeProfileDao(): HomeProfileDao
    abstract fun myPhotoDao(): MyPhotoDao
    abstract fun hotTopicContentDao(): HotTopicContentDao
    abstract fun peopleDao(): PeopleDao
    abstract fun rankingDao(): RankingDao
    abstract fun feedsContentDao(): FeedsContentDao
    abstract fun othersProfileDao(): OthersPhotosDao
}
