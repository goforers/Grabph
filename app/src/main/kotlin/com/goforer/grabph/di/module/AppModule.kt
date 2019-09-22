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

package com.goforer.grabph.di.module

import android.app.Application
import android.content.Context
import androidx.room.Room.databaseBuilder
import com.goforer.grabph.domain.erase.PhotoEraser
import com.goforer.grabph.domain.save.PhotoSaver
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.exif.EXIFHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.repository.model.cache.data.entity.category.CPhotogQuery
import com.goforer.grabph.repository.model.cache.data.entity.photog.PhotogQuery
import com.goforer.grabph.repository.model.cache.Cache
import com.goforer.grabph.repository.model.dao.local.LocalEXIFDao
import com.goforer.grabph.repository.model.dao.local.LocalLocationDao
import com.goforer.grabph.repository.model.dao.local.LocalSavedPhotoDao
import com.goforer.grabph.repository.model.dao.remote.category.CategoryDao
import com.goforer.grabph.repository.model.dao.remote.category.photo.CPhotoDao
import com.goforer.grabph.repository.model.dao.remote.comment.CommentDao
import com.goforer.grabph.repository.model.dao.remote.feed.exif.EXIFDao
import com.goforer.grabph.repository.model.dao.remote.feed.FeedItemDao
import com.goforer.grabph.repository.model.dao.remote.feed.FeedsContentDao
import com.goforer.grabph.repository.model.dao.remote.feed.location.LocationDao
import com.goforer.grabph.repository.model.dao.remote.feed.photo.MyPhotoDao
import com.goforer.grabph.repository.model.dao.remote.feed.photo.PhotoDao
import com.goforer.grabph.repository.model.dao.remote.feed.photo.PhotoInfoDao
import com.goforer.grabph.repository.model.dao.remote.home.HomeDao
import com.goforer.grabph.repository.model.dao.remote.hottopic.HotTopicContentDao
import com.goforer.grabph.repository.model.dao.remote.people.PeopleDao
import com.goforer.grabph.repository.model.dao.remote.people.owner.OwnerDao
import com.goforer.grabph.repository.model.dao.remote.people.person.PersonDao
import com.goforer.grabph.repository.model.dao.remote.profile.HomeProfileDao
import com.goforer.grabph.repository.model.dao.remote.profile.OthersProfileDao
import com.goforer.grabph.repository.model.dao.remote.quest.FavoriteQuestDao
import com.goforer.grabph.repository.model.dao.remote.quest.QuestInfoDao
import com.goforer.grabph.repository.model.dao.remote.quest.TopPortionQuestDao
import com.goforer.grabph.repository.model.dao.remote.ranking.RankingDao
import com.goforer.grabph.repository.model.dao.remote.search.RecentKeywordDao
import com.goforer.grabph.repository.network.api.SearpService
import com.goforer.grabph.repository.network.factory.LiveDataCallAdapterFactory
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * A module for Android-specific dependencies which require a [Context] or
 * [android.app.Application] to create.
 */
@Module
class AppModule {
    companion object {
        private const val READ_TIME_OUT: Long = 5
        private const val WRITE_TIME_OUT: Long = 5
        private const val CONNECT_TIME_OUT: Long = 5

        private const val BASE_URL = "https://api.flickr.com/"

        var rawResponseBody: String? = null
            private set
    }

    @Singleton
    @Provides
    internal fun provideSearpService(): SearpService {
        val cookieManager = CookieManager()

        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

        val cookieJar = JavaNetCookieJar(cookieManager)
        val okHttpClient = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)

        okHttpClient.addInterceptor { chain ->
            val original = chain.request()

            val builder = original.newBuilder()

            val request = builder
                    .header("Connection", "keep-alive")
                    .method(original.method, original.body)
                    .build()

            val response = chain.proceed(request)

            rawResponseBody = response.body?.string()

            response.newBuilder().body(
                    rawResponseBody!!.toResponseBody(response.body?.contentType())).build()
        }

        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()
                .create(SearpService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideCommonWorkHandler(): CommonWorkHandler {
        return CommonWorkHandler()
    }

    @Singleton
    @Provides
    internal fun provideWatermarkHandler(): WatermarkHandler {
        return WatermarkHandler()
    }

    @Singleton
    @Provides
    internal fun provideEXIFHandler(): EXIFHandler {
        return EXIFHandler()
    }

    @Singleton
    @Provides
    internal fun providePhotoSaver(): PhotoSaver {
        return PhotoSaver()
    }

    @Singleton
    @Provides
    internal fun providePhotoEraser(): PhotoEraser {
        return PhotoEraser()
    }

    @Singleton
    @Provides
    internal fun provideCache(app: Application): Cache {
        return databaseBuilder(app, Cache::class.java, "grabph.db").build()
    }

    @Singleton
    @Provides
    internal fun provideFeedDao(cache: Cache): FeedItemDao {
        return cache.feedItemDao()
    }

    @Singleton
    @Provides
    internal fun providePersonDao(cache: Cache): PersonDao {
        return cache.personDao()
    }

    @Singleton
    @Provides
    internal fun provideEXIFDao(cache: Cache): EXIFDao {
        return cache.exifDao()
    }

    @Singleton
    @Provides
    internal fun provideLocalEXIFDao(cache: Cache): LocalEXIFDao {
        return cache.localEXIFDao()
    }

    @Singleton
    @Provides
    internal fun provideOwnerDao(cache: Cache): OwnerDao {
        return cache.ownerDao()
    }

    @Singleton
    @Provides
    internal fun provideLocalSavedPhotoDao(cache: Cache): LocalSavedPhotoDao {
        return cache.localSavedPhotoDao()
    }

    @Singleton
    @Provides
    internal fun provideSearchKeywordDao(cache: Cache): RecentKeywordDao {
        return cache.searchKeywordDao()
    }

    @Singleton
    @Provides
    internal fun provideLocationDao(cache: Cache): LocationDao {
        return cache.locationDao()
    }

    @Singleton
    @Provides
    internal fun providePhotoDao(cache: Cache): PhotoDao {
        return cache.photoDao()
    }

    @Singleton
    @Provides
    internal fun provideCommentDao(cache: Cache): CommentDao {
        return cache.commentDao()
    }

    @Singleton
    @Provides
    internal fun providePhotoInfoDao(cache: Cache): PhotoInfoDao {
        return cache.photoInfoDao()
    }

    @Singleton
    @Provides
    internal fun provideLocalLocationDao(cache: Cache): LocalLocationDao {
        return cache.localLocationDao()
    }

    @Singleton
    @Provides
    internal fun provideFavoriteQuestDao(cache: Cache): FavoriteQuestDao {
        return cache.favoriteQuestDao()
    }

    @Singleton
    @Provides
    internal fun provideQuestInfoDao(cache: Cache): QuestInfoDao {
        return cache.questInfoDao()
    }

    @Singleton
    @Provides
    internal fun provideHomeDao(cache: Cache): HomeDao {
        return cache.homeDao()
    }

    @Singleton
    @Provides
    internal fun provideCPhotoDao(cache: Cache): CPhotoDao {
        return cache.cphotoDao()
    }

    @Singleton
    @Provides
    internal fun provideHotTopicContentDao(cache: Cache): HotTopicContentDao {
        return cache.hotTopicContentDao()
    }

    @Singleton
    @Provides
    internal fun provideTopPortionQuestDao(cache: Cache): TopPortionQuestDao {
        return cache.topPortionQuestDao()
    }

    @Singleton
    @Provides
    internal fun providePhotoTypeDao(cache: Cache): CategoryDao {
        return cache.photoTypeDao()
    }

    @Singleton
    @Provides
    internal fun provideHomeProfileDao(cache: Cache): HomeProfileDao {
        return cache.homeProfileDao()
    }

    @Singleton
    @Provides
    internal fun provideMyPhotoDao(cache: Cache): MyPhotoDao {
        return cache.myPhotoDao()
    }

    @Singleton
    @Provides
    internal fun providePeopleDao(cache: Cache): PeopleDao {
        return cache.peopleDao()
    }

    @Singleton
    @Provides
    internal fun provideRankingDao(cache: Cache): RankingDao {
        return cache.rankingDao()
    }

    @Singleton
    @Provides
    internal fun provideFeedsContentDao(cache: Cache): FeedsContentDao {
        return cache.feedsContentDao()
    }

    @Singleton
    @Provides
    internal fun provideOthersProfileDao(cache: Cache): OthersProfileDao {
        return cache.othersProfileDao()
    }

    @Singleton
    @Provides
    internal fun providePhotogQuery(): PhotogQuery {
        return PhotogQuery()
    }

    @Singleton
    @Provides
    internal fun provideCPhotogQuery(): CPhotogQuery {
        return CPhotogQuery()
    }
}
