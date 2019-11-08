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
import com.goforer.grabph.domain.usecase.erase.PhotoEraser
import com.goforer.grabph.domain.usecase.save.PhotoSaver
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.exif.EXIFHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.data.datasource.model.cache.Cache
import com.goforer.grabph.data.datasource.network.api.SearpService
import com.goforer.grabph.data.datasource.network.factory.LiveDataCallAdapterFactory
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
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

        fun create(): SearpService {
            val cookieManager = CookieManager()

            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

            val cookieJar = JavaNetCookieJar(cookieManager)
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logger)
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
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()
                .create(SearpService::class.java)
        }
    }

    @Singleton
    @Provides
    internal fun provideSearpService(): SearpService {
        val cookieManager = CookieManager()

        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

        val cookieJar = JavaNetCookieJar(cookieManager)
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BASIC

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logger)
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
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(SearpService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideCommonWorkHandler() = CommonWorkHandler()

    @Singleton
    @Provides
    internal fun provideWatermarkHandler() =  WatermarkHandler()

    @Singleton
    @Provides
    internal fun provideEXIFHandler() = EXIFHandler()

    @Singleton
    @Provides
    internal fun providePhotoSaver() = PhotoSaver()

    @Singleton
    @Provides
    internal fun providePhotoEraser() = PhotoEraser()

    @Singleton
    @Provides
    internal fun provideCache(app: Application) = databaseBuilder(app, Cache::class.java, "grabph.db").build()

    @Singleton
    @Provides
    internal fun provideFeedDao(cache: Cache) = cache.feedItemDao()

    @Singleton
    @Provides
    internal fun providePersonDao(cache: Cache) = cache.personDao()

    @Singleton
    @Provides
    internal fun provideEXIFDao(cache: Cache) = cache.exifDao()

    @Singleton
    @Provides
    internal fun provideLocalEXIFDao(cache: Cache) = cache.localEXIFDao()

    @Singleton
    @Provides
    internal fun provideOwnerDao(cache: Cache) = cache.ownerDao()

    @Singleton
    @Provides
    internal fun provideLocalSavedPhotoDao(cache: Cache) = cache.localSavedPhotoDao()

    @Singleton
    @Provides
    internal fun provideSearchKeywordDao(cache: Cache) = cache.searchKeywordDao()

    @Singleton
    @Provides
    internal fun provideLocationDao(cache: Cache) = cache.locationDao()

    @Singleton
    @Provides
    internal fun providePhotoDao(cache: Cache) = cache.photoDao()

    @Singleton
    @Provides
    internal fun provideCommentDao(cache: Cache) = cache.commentDao()

    @Singleton
    @Provides
    internal fun providePhotoInfoDao(cache: Cache) = cache.photoInfoDao()

    @Singleton
    @Provides
    internal fun provideLocalLocationDao(cache: Cache) = cache.localLocationDao()

    @Singleton
    @Provides
    internal fun provideFavoriteQuestDao(cache: Cache) = cache.favoriteQuestDao()

    @Singleton
    @Provides
    internal fun provideQuestInfoDao(cache: Cache) = cache.questInfoDao()

    @Singleton
    @Provides
    internal fun provideHomeDao(cache: Cache) = cache.homeDao()

    @Singleton
    @Provides
    internal fun provideCPhotoDao(cache: Cache) = cache.cphotoDao()

    @Singleton
    @Provides
    internal fun provideHotTopicContentDao(cache: Cache) = cache.hotTopicContentDao()

    @Singleton
    @Provides
    internal fun provideTopPortionQuestDao(cache: Cache) = cache.topPortionQuestDao()

    @Singleton
    @Provides
    internal fun providePhotoTypeDao(cache: Cache) = cache.photoTypeDao()

    @Singleton
    @Provides
    internal fun provideHomeProfileDao(cache: Cache) = cache.homeProfileDao()

    @Singleton
    @Provides
    internal fun provideMyPhotoDao(cache: Cache) = cache.myPhotoDao()

    @Singleton
    @Provides
    internal fun providePeopleDao(cache: Cache) = cache.peopleDao()

    @Singleton
    @Provides
    internal fun provideRankingDao(cache: Cache) = cache.rankingDao()

    @Singleton
    @Provides
    internal fun provideFeedsContentDao(cache: Cache) = cache.feedsContentDao()

    @Singleton
    @Provides
    internal fun provideMyGalleryDao(cache: Cache) = cache.myGalleryDao()

    @Singleton
    @Provides
    internal fun provideMyProfileDao(cache: Cache) = cache.myProfileDao()
}
