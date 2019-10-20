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

package com.goforer.grabph.repository.network.api

import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import com.goforer.grabph.repository.model.cache.data.entity.ranking.Ranking
import com.goforer.grabph.repository.model.cache.data.entity.comments.PhotoComments
import com.goforer.grabph.repository.model.cache.data.entity.cphotog.CPhotog
import com.goforer.grabph.repository.model.cache.data.entity.exif.PhotoEXIF
import com.goforer.grabph.repository.model.cache.data.entity.feed.FlickrFeed
import com.goforer.grabph.repository.model.cache.data.entity.follow.follower.Followerg
import com.goforer.grabph.repository.model.cache.data.entity.follow.following.Followingg
import com.goforer.grabph.repository.model.cache.data.entity.home.Homeg
import com.goforer.grabph.repository.model.cache.data.entity.location.PhotoGEO
import com.goforer.grabph.repository.model.cache.data.entity.quest.TopPortionQuestg
import com.goforer.grabph.repository.model.cache.data.entity.quest.info.QuestInfog
import com.goforer.grabph.repository.model.cache.data.entity.quest.info.QuestsInfog
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photog
import com.goforer.grabph.repository.model.cache.data.entity.photoinfo.PhotoInfo
import com.goforer.grabph.repository.model.cache.data.entity.category.Categoryg
import com.goforer.grabph.repository.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.repository.model.cache.data.entity.hottopic.HotTopicContentg
import com.goforer.grabph.repository.model.cache.data.mock.entity.feed.FeedsContentg
import com.goforer.grabph.repository.model.cache.data.entity.profile.HomeProfile
import com.goforer.grabph.repository.model.cache.data.entity.profile.OwnerProfile
import com.goforer.grabph.repository.model.cache.data.entity.profile.SearperProfile
import com.goforer.grabph.repository.model.cache.data.entity.profile.*
import com.goforer.grabph.repository.network.response.ApiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap
import retrofit2.http.Query

import java.io.File

interface SearpService {
    companion object {
        const val MULTIPART_FORM_DATA = "multipart/form-data"

        fun createRequestBody(@NonNull file: File): RequestBody {
            return RequestBody.create(
                MULTIPART_FORM_DATA.toMediaTypeOrNull(), file
            )
        }

        fun createRequestBody(@NonNull s: String): RequestBody {
            return RequestBody.create(
                MULTIPART_FORM_DATA.toMediaTypeOrNull(), s
            )
        }
    }

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/feeds/photos_public.gne")
    fun getFeed(
            @Query("tags") query: String,
            @Query("format") format: String,
            @Query("lang") langEng: String,
            @Query("lang") langKor: String,
            @Query("lang") langGer: String,
            @Query("lang") langSpa: String,
            @Query("lang") langFra: String,
            @Query("lang") langIta: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<FlickrFeed>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/rest/")
    fun getSearperProfile(
            @Query("api_key") apikey: String,
            @Query("user_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<SearperProfile>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/rest/")
    fun getOwnerProfile(
            @Query("api_key") apikey: String,
            @Query("owner_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<OwnerProfile>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/rest/")
    fun getPhotoEXIF(
            @Query("api_key") apikey: String,
            @Query("photo_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<PhotoEXIF>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("services/rest/")
    fun getLocation(
            @Query("api_key") apikey: String,
            @Query("photo_id") photoId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<PhotoGEO>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/rest/")
    fun getPhotos(
            @Query("api_key") apikey: String,
            @Query("user_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("page") page: Int,
            @Query("per_page") perPage: Int,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<Photog>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/rest/")
    fun getComments(
            @Query("api_key") apikey: String,
            @Query("photo_id") photoId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<PhotoComments>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/rest/")
    fun getPhotoInfo(
            @Query("api_key") apikey: String,
            @Query("photo_id") photoId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<PhotoInfo>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/rest/")
    fun getPopularPhotos(
            @Query("api_key") apikey: String,
            @Query("user_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("page") page: Int,
            @Query("per_page") perPage: Int,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<Photog>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/rest/")
    fun getFavoritePhotos(
            @Query("api_key") apikey: String,
            @Query("user_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("page") page: Int,
            @Query("per_page") perPage: Int,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<Photog>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.searp.com/services/rest/")
    fun getQuests(
            @Query("api_key") apikey: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("page") page: Int,
            @Query("per_page") perPage: Int,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<QuestsInfog>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.searp.com/rest/")
    fun getQuestInfo(
            @Query("api_key") apikey: String,
            @Query("mission_id") missionId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<QuestInfog>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.searp.com/rest/")
    fun getFollowers(
            @Query("api_key") apikey: String,
            @Query("user_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<Followerg>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.searp.com/rest/")
    fun getFollowings(
            @Query("api_key") apikey: String,
            @Query("user_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<Followingg>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.searp.com/rest/")
    fun getHome(
            @Query("api_key") apikey: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<Homeg>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.searp.com/rest/")
    fun getCategoryPhotos(
            @Query("api_key") apikey: String,
            @Query("category_id") categoryId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("page") page: Int,
            @Query("per_page") perPage: Int,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<CPhotog>>

    @GET("https://api.searp.com/rest/")
    fun getHotQuest(
            @Query("api_key") apikey: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<TopPortionQuestg>>

    @GET("https://api.searp.com/rest/")
    fun getCategories(
            @Query("api_key") apikey: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<Categoryg>>

    @GET("https://api.searp.com/rest/")
    fun getMyProfile(
            @Query("api_key") apikey: String,
            @Query("user_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<HomeProfile>>

    @GET("https://api.searp.com/rest/")
    fun getHotTopicContent(
            @Query("api_key") apikey: String,
            @Query("topic_id") topicId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<HotTopicContentg>>

    @GET("https://api.searp.com/rest/")
    fun getFeedsContent(
            @Query("api_key") apikey: String,
            @Query("photo_id") topicId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<FeedsContentg>>

    @GET("https://api.searp.com/services/rest/")
    fun getPeople(
            @Query("api_key") apikey: String,
            @Query("user_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<People>>

    @GET("https://api.searp.com/services/rest/")
    fun getRanking(
            @Query("api_key") apikey: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<Ranking>>

    /**
     * @GET declares an HTTP GET request
     * @Query("") annotation on the parameters marks it as a
     * replacement for the placeholder in the @GET query
     */
    @GET("https://api.flickr.com/services/rest/")
    fun getOthersProfile(
            @Query("api_key") apikey: String,
            @Query("owner_id") userId: String,
            @Query("method") method: String,
            @Query("format") format: String,
            @Query("nojsoncallback") index: Int
    ): LiveData<ApiResponse<Owner>>

    // We've to get it fixed via Searp REST API. That is, We must substitute this API for the Searp API.
    /**
     * @POST declares an HTTP GET request
     * @PartMap annotation on the parameters to denote name and value parts of a multi-part request
     */
    @Multipart
    @POST("https://up.flickr.com/services/upload/")
    fun postFeed(
        @PartMap feedInto: Map<String, RequestBody>,
        @PartMap photos: Map<String, RequestBody>
    ): LiveData<ApiResponse<FeedItem>>
}