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

@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")

package com.goforer.grabph.presentation.caller

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import android.util.Pair
import android.view.View
import com.goforer.base.presentation.customtabsclient.shared.CustomTabsHelper
import com.goforer.base.presentation.customtabsclient.shared.ServiceConnection
import com.goforer.base.presentation.customtabsclient.shared.ServiceConnectionCallback
import com.goforer.base.presentation.utils.AUTHORIZE_URL
import com.goforer.base.presentation.utils.KEY_SELECTED_IMAGE_URI
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.presentation.ui.upload.AuthActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.ui.category.CategoryActivity
import com.goforer.grabph.presentation.ui.categoryphoto.CategoryPhotoActivity
import com.goforer.grabph.presentation.ui.comment.CommentActivity
import com.goforer.grabph.presentation.ui.login.LogInActivity
import com.goforer.grabph.presentation.ui.map.MapsActivity
import com.goforer.grabph.presentation.ui.photog.PhotogPhotoActivity
import com.goforer.grabph.presentation.ui.feed.photoinfo.PhotoInfoActivity
import com.goforer.grabph.presentation.ui.photoviewer.PhotoViewerActivity
import com.goforer.grabph.presentation.ui.feed.feedinfo.FeedInfoActivity
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.hottopic.HotTopicContentActivity
import com.goforer.grabph.presentation.ui.othersprofile.OthersProfileActivity
import com.goforer.grabph.presentation.ui.people.PeopleActivity
import com.goforer.grabph.presentation.ui.questinfo.QuestInfoActivity
import com.goforer.grabph.presentation.ui.pinnedlist.PinnedFeedsActivity
import com.goforer.grabph.presentation.ui.ranking.RankingActivity
import com.goforer.grabph.presentation.ui.search.FeedSearchActivity
import com.goforer.grabph.presentation.ui.searplegallery.SearpleGalleryActivity
import com.goforer.grabph.presentation.ui.setting.SettingListActivity
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
import com.goforer.grabph.presentation.ui.upload.UploadPhotoActivity
import java.io.ByteArrayOutputStream
import java.util.*

object Caller {
    const val EXTRA_SELECTED_ITEM_POSITION = "goforer:selected_item_position"
    const val EXTRA_FEED_INFO_POSITION = "goforer:pinned_photo_list_position"
    const val EXTRA_PHOTO_POSITION = "goforer:photo_position"
    const val EXTRA_PHOTO_FILE_NAME_LIST = "goforer:photo_file_name_list"
    const val EXTRA_PHOTO_FILE_ID_LIST = "goforer:photo_file_id_list"
    const val EXTRA_PHOTO_URL = "goforer:photo_url"
    const val EXTRA_FEED_INFO_CALLED_FROM = "goforer:feed_info_called_from"
    const val EXTRA_PHOTO_VIEWER_CALLED_FROM = "goforer:photo_viewer_called_from"
    const val EXTRA_PHOTO_LINK_BITMAP = "goforer:photo_link_bitmap"
    const val EXTRA_LATITUDE = "goforer:latitude"
    const val EXTRA_LONGITUDE = "goforer:longitude"
    const val EXTRA_ADDRESS = "goforer:address"
    const val EXTRA_SEARPER_NAME = "goforer:searpername"
    const val EXTRA_USER_NAME = "goforer:username"
    const val EXTRA_USER_ID = "goforer:userid"
    const val EXTRA_USER_ICONFARM = "goforer:usericonfarm"
    const val EXTRA_USER_ICONSERVER = "goforer:usericonserver"
    const val EXTRA_SEARPER_ID = "goforer:userid"
    const val EXTRA_SEARPER_ICONFARM = "gofore:graphericonfarm"
    const val EXTRA_SEARPER_ICONSERVER = "gofore:graphericonserver"
    const val EXTRA_PAGES = "goforer:pages"
    const val EXTRA_PHOTO_ID = "goforer:photoid"
    const val EXTRA_HOME_CATEGORY_ID = "goforer:home_category_id"
    const val EXTRA_HOME_CATEGORY_TITLE = "goforer:home_category_title"
    const val EXTRA_HOME_CATEGORY_IMAGE = "goforer:home_category_image"
    const val EXTRA_OWNER_ID = "goforer:ownerid"
    const val EXTRA_FEED_IDX = "goforer:feedidx"
    const val EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION = "goforer:photo_info_selected_item_position"
    const val EXTRA_PHOTO_TITLE = "goforer:photo_title"
    const val EXTRA_PHOTOG_PHOTO_TYPE = "goforer:photog_photo_type"
    const val EXTRA_CATEGORY_PHOTO_TYPE = "goforer:category_photo_type"
    const val EXTRA_SAVED_PHOTO_ERASED = "goforer:saved_photo_erased"
    const val EXTRA_HOME_BOTTOM_MENU_ID = "goferer:home_bottom_menu_id"
    const val EXTRA_SEARCH_QUERY = "goforer:search_query"
    const val EXTRA_SEARCH_COLLAPSED = "goforer:search_collapsed"
    const val EXTRA_QUEST_POSITION = "goforer:mission_list_position"
    const val EXTRA_QUEST_TITLE = "goforer:mission_title"
    const val EXTRA_QUEST_OWNER_NAME = "goforer:mission_owner_name"
    const val EXTRA_QUEST_OWNER_LOGO = "goforer:mission_owner_logo"
    const val EXTRA_QUEST_OWNER_IMAGE = "goforer:mission_owner_image"
    const val EXTRA_QUEST_DESCRIPTION = "goforer:mission_description"
    const val EXTRA_QUEST_STATE = "goforer:mission_state"
    const val EXTRA_QUEST_REWARD = "goforer:mission_reward"
    const val EXTRA_QUEST_DURATION = "goforer:mission_duration"
    const val EXTRA_QUEST_CALLED_FROM = "goforer:mission_calledfrom"
    const val EXTRA_CATEGORY_PHOTO_INFO_SELECTED_ITEM_POSITION = "goforer:category_photo_info_selected_item_position"
    const val EXTRA_CATEGORY_POSITION = "goforer:category_list_position"
    const val EXTRA_SEARPLE_GALLERY_PHOTO_POSITION = "goforer:searple_gallery_photo_position"
    const val EXTRA_PEOPLE_TYPE = "goforer:people_type"
    const val EXTRA_HOT_TOPIC_CONTENT_ID = "goforer:hot_topic_content_id"
    const val EXTRA_HOP_TOPIC_POSITION = "goforer:hot_topic_position"
    const val EXTRA_RANKING_TYPE = "goforer:ranking_type"
    const val EXTRA_SAVED_SALES_TAB_INDEX = "goforer:current_sales_tab_index"
    const val EXTRA_SALES_STATUS_TYPE = "goforer:sales_status_type"
    const val EXTRA_PROFILE_USER_ID = "goforer:profile_user_id"
    const val EXTRA_PROFILE_USER_NAME = "goforer:profile_user_name"
    const val EXTRA_PROFILE_USER_RANKING = "goforer:profile_user_ranking"
    const val EXTRA_PROFILE_USER_PHOTO_URL = "goforer:profile_user_photo_url"
    const val EXTRA_PLACE_CALLED_USER_PROFILE = "goforer:place_called_user_profile"

    const val FONT_SIZE = "fontSize"
    const val PADDING = "padding"
    const val TEXT_COLOR = "color"

    const val SELECTED_FEED_ITEM_POSITION = 1000
    const val SELECTED_SEARCH_ITEM_POSITION = 1001
    const val SELECTED_SEARPLE_GALLERY_ITEM_POSITION = 1002
    const val SELECTED_PINNED_FEED_ITEM_POSITION = 1003
    const val SELECTED_PHOTO_INFO_ITEM_POSITION = 1004
    const val SELECTED_QUEST_INFO_ITEM_POSITION = 1005
    const val SELECTED_FEED_INFO_PHOTO_VIEW = 1006
    const val SELECTED_PHOTO_INFO_PHOTO_VIEW = 1007
    const val SELECTED_PINNED_INFO_PHOTO_VIEW = 1008
    const val SELECTED_PHOTOG_PHOTO_VIEW = 1009
    const val SELECTED_QUEST_INFO_ITEM_FROM_HOME_MAIN_POSITION = 1010
    const val SELECTED_BEST_PICK_HOT_PHOTO_POSITION = 1011
    const val SELECTED_BEST_PICK_SEARPER_PHOTO_POSITION = 1012
    const val SELECTED_BEST_PICK_QUEST_POSITION = 1013
    const val SELECTED_BEST_PICK_CATEGORY_POSITION = 1014
    const val SELECTED_QUEST_INFO_ITEM_FROM_HOT_QUEST_POSITION = 1015
    const val SELECTED_QUEST_INFO_ITEM_FROM_FAVORITE_QUEST_POSITION = 1016
    const val SELECTED_CATEGORY_PHOTO_INFO_ITEM_POSITION = 1017
    const val SELECTED_CATEGORY_ITEM_POSITION = 1018
    const val SELECTED_BEST_PICK_HOT_TOPIC_POSITION = 1019

    const val CALLED_FROM_FEED_ITEM = 4000
    const val CALLED_FROM_SEARPLE_GALLERY_PHOTO = 4001
    const val CALLED_FROM_FEED = 4002
    const val CALLED_FROM_SEARCHED_FEED = 4003
    const val CALLED_FROM_PINNED_FEED = 4004
    const val CALLED_FROM_FEED_INFO = 4005
    const val CALLED_FROM_PHOTO_INFO = 4006
    const val CALLED_FROM_PHOTOG_PHOTO = 4007
    const val CALLED_FORM_HOME_FAVORITE_QUEST = 4008
    const val CALLED_FORM_HOME_FAVORITE_QUEST_INFO = 4009
    const val CALLED_FROM_PINNED_FEEDS = 4010
    const val CALLED_FROM_HOME_MAIN = 4011
    const val CALLED_FROM_HOME_MAIN_QUEST = 4012
    const val CALLED_FROM_CATEGORY_PHOTO = 4013
    const val CALLED_FROM_HOME_BEST_PICK_HOT_PHOTO = 4014
    const val CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO = 4015
    const val CALLED_FROM_HOME_BEST_PICK_QUEST = 4016
    const val CALLED_FORM_HOME_HOT_QUEST = 4017
    const val CALLED_FROM_HOME_PROFILE = 4018
    const val CALLED_FROM_PHOTO_TYPE = 4019
    const val CALLED_FROM_HOT_TOPIC_CONTENT = 4020
    const val CALLED_FROM_PEOPLE = 4021
    const val CALLED_FROM_RANKING = 4022
    const val CALLED_FROM_OTHERS_PROFILE = 4022
    const val CALLED_FROM_AUTH_ACTIVITY = 4023

    const val PHOTOG_PHOTO_POPULAR_TYPE = 5000
    const val PHOTOG_PHOTO_GENERAL_TYPE = 5001
    const val PHOTOG_PHOTO_FAVORITE_TYPE = 5002

    const val FIRST_PAGE = 1

    private var url: String? = null

    private var serviceConnection: ServiceConnection? = null

    private fun createIntent(context: Context?, cls: Class<*>, isNewTask: Boolean): Intent {
        val intent = Intent(context, cls)

        if (isNewTask && context !is Activity) {
            intent.flags = (FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        return intent
    }

    /**
     * @deprecated
     */
    private fun createIntent(activity: Activity?, cls: Class<*>, isNewTask: Boolean): Intent {
        val intent = Intent(activity, cls)

        if (isNewTask && activity == null) {
            intent.flags = (FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        return intent
    }

    fun callHome(context: Context) {
        val intent = createIntent(context, HomeActivity::class.java, true)

        context.startActivity(intent)
    }

    fun callLogIn(context: Context) {
        val intent = createIntent(context, LogInActivity::class.java, true)

        context.startActivity(intent)
    }

    /*
    @SuppressLint("RestrictedApi")
    fun callFeed(activity: Activity, imageView: View, textView: View, position: Int,
                                                                calledFrom: Int, requestCode: Int) {
        val context = activity as Context
        val intent = createIntent(context, FeedInfoActivity::class.java, false)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_FEED_PHOTO_POSITION, position)
        intent.putExtra(FONT_SIZE, (textView as AppCompatTextView).textSize)
        when (calledFrom) {
            CALLED_FROM_FEED -> intent.putExtra(EXTRA_PHOTO_FEED_VIEW_CALLED_FROM, CALLED_FROM_FEED)
            CALLED_FROM_PINNED_FEED -> intent.putExtra(EXTRA_PHOTO_FEED_VIEW_CALLED_FROM, CALLED_FROM_PINNED_FEED)
            CALLED_FROM_SEARCHED_FEED -> intent.putExtra(EXTRA_PHOTO_FEED_VIEW_CALLED_FROM, CALLED_FROM_SEARCHED_FEED)
            else -> intent.putExtra(EXTRA_PHOTO_FEED_VIEW_CALLED_FROM, CALLED_FROM_FEED)
        }

        intent.putExtra(PADDING,
                Rect(textView.getPaddingLeft(),
                        textView.getPaddingTop(),
                        textView.getPaddingRight(),
                        textView.getPaddingBottom()))
        intent.putExtra(TEXT_COLOR, textView.currentTextColor)

        val activityOptions = getFeedViewActivityOptions(context, imageView, textView, calledFrom)

        when (calledFrom) {
            CALLED_FROM_FEED, CALLED_FROM_PINNED_FEED -> (context as MoreViewActivity).startActivityForResult(intent, requestCode,
                    activityOptions.toBundle())
            CALLED_FROM_SEARCHED_FEED -> (context as MoreViewActivity).startActivityForResult(intent, requestCode,
                    activityOptions.toBundle())
            else -> (context as MoreViewActivity).startActivityForResult(intent, requestCode,
                    activityOptions.toBundle())
        }
    }
    */

    @SuppressLint("RestrictedApi")
    fun callFeedInfo(context: Context, imageView: View, textView: View,
                     feedIdx: Long, position: Int, authorId: String?,
                     photoId: String?, calledFrom: Int, requestCode: Int) {
        val intent = createIntent(context, FeedInfoActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_FEED_IDX, feedIdx)
        intent.putExtra(EXTRA_FEED_INFO_POSITION, position)
        intent.putExtra(EXTRA_SEARPER_ID, authorId)
        intent.putExtra(EXTRA_PHOTO_ID, photoId)
        intent.putExtra(EXTRA_FEED_INFO_CALLED_FROM, calledFrom)

        // This code was be blocked temporarily
        /*
        intent.putExtra(FONT_SIZE, (textView as AppCompatTextView).textSize)
        intent.putExtra(PADDING,
                Rect(textView.getPaddingLeft(),
                        textView.getPaddingTop(),
                        textView.getPaddingRight(),
                        textView.getPaddingBottom()))
        intent.putExtra(TEXT_COLOR, textView.currentTextColor)
        */

        when (calledFrom) {
            CALLED_FROM_FEED, CALLED_FROM_PINNED_FEED -> (context as HomeActivity).startActivityForResult(intent, requestCode,
                    getFeedViewActivityOptions(context, imageView).toBundle())
            CALLED_FROM_SEARCHED_FEED -> (context as FeedSearchActivity).startActivityForResult(intent, requestCode,
                    getSearchViewActivityOptions(context, imageView, textView).toBundle())
            CALLED_FROM_PINNED_FEEDS -> (context as HomeActivity).startActivityForResult(intent, requestCode,
                    getPinnedFeedViewActivityOptions(context, imageView, textView).toBundle())
            else -> (context as HomeActivity).startActivityForResult(intent, requestCode,
                    getFeedViewActivityOptions(context, imageView).toBundle())
        }
    }

    @SuppressLint("RestrictedApi")
    fun callFeedInfo(context: Context, imageView: View, feedIdx: Long, position:
                     Int, authorId: String?, photoId: String?, calledFrom: Int, requestCode: Int) {
        val intent = createIntent(context, FeedInfoActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_FEED_IDX, feedIdx)
        intent.putExtra(EXTRA_FEED_INFO_POSITION, position)
        intent.putExtra(EXTRA_SEARPER_ID, authorId)
        intent.putExtra(EXTRA_PHOTO_ID, photoId)
        intent.putExtra(EXTRA_FEED_INFO_CALLED_FROM, calledFrom)

        (context as HomeActivity).startActivityForResult(intent, requestCode,
                getHomeViewActivityOptions(context, imageView).toBundle())
    }

    @SuppressLint("RestrictedApi")
    fun callViewer(context: Context, imageView: View, position: Int,
                   fileNames: List<String>, ids: List<String>, requestCode: Int) {
        val intent = createIntent(context, PhotoViewerActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_PHOTO_VIEWER_CALLED_FROM, CALLED_FROM_SEARPLE_GALLERY_PHOTO)
        intent.putExtra(EXTRA_SEARPLE_GALLERY_PHOTO_POSITION, position)
        intent.putStringArrayListExtra(EXTRA_PHOTO_FILE_NAME_LIST, fileNames as ArrayList<String>)
        intent.putStringArrayListExtra(EXTRA_PHOTO_FILE_ID_LIST, ids as ArrayList<String>)

        val activityOptions = getSearpleGalleryViewerOptions(context, imageView)
            (context as SearpleGalleryActivity).startActivityForResult(intent, requestCode,
                activityOptions.toBundle())
    }

    @SuppressLint("RestrictedApi")
    fun callViewer(context: Context, imageView: View, position: Int, fromCalled: Int, bitmap: Bitmap,
                   url: String, requestCode: Int) {
        val stream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

        val byteArray = stream.toByteArray()
        val intent = createIntent(context, PhotoViewerActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_PHOTO_VIEWER_CALLED_FROM, fromCalled)
        intent.putExtra(EXTRA_PHOTO_LINK_BITMAP, byteArray)
        intent.putExtra(EXTRA_PHOTO_URL, url)
        intent.putExtra(EXTRA_PHOTO_POSITION, position)

        when(fromCalled) {
            CALLED_FROM_FEED_INFO -> {
                val activityOptions = getFeedInfoPhotoViewerOptions(context, imageView)
                (context as FeedInfoActivity).startActivityForResult(intent, requestCode,
                        activityOptions.toBundle())
            }
            CALLED_FROM_PHOTO_INFO -> {
                val activityOptions = getPhotoInfoPhotoViewerOptions(context, imageView)
                (context as PhotoInfoActivity).startActivityForResult(intent, requestCode,
                        activityOptions.toBundle())
            }
            CALLED_FROM_PHOTOG_PHOTO -> {
                val activityOptions = getPhotogPhotoViewerOptions(context, imageView)
                (context as PhotogPhotoActivity).startActivityForResult(intent, requestCode,
                        activityOptions.toBundle())
            }
        }

    }

    @SuppressLint("RestrictedApi")
    fun callViewer(context: Context, imageView: View, position: Int, fromCalled: Int, url: String, requestCode: Int) {
        val intent = createIntent(context, PhotoViewerActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_PHOTO_VIEWER_CALLED_FROM, fromCalled)
        intent.putExtra(EXTRA_PHOTO_URL, url)
        intent.putExtra(EXTRA_PHOTO_POSITION, position)

        when(fromCalled) {
            CALLED_FROM_FEED_INFO -> {
                val activityOptions = getFeedInfoPhotoViewerOptions(context, imageView)
                (context as FeedInfoActivity).startActivityForResult(intent, requestCode,
                        activityOptions.toBundle())
            }
            CALLED_FROM_PHOTO_INFO -> {
                val activityOptions = getPhotoInfoPhotoViewerOptions(context, imageView)
                (context as PhotoInfoActivity).startActivityForResult(intent, requestCode,
                        activityOptions.toBundle())
            }
            CALLED_FROM_PHOTOG_PHOTO -> {
                val activityOptions = getPhotogPhotoViewerOptions(context, imageView)
                (context as PhotogPhotoActivity).startActivityForResult(intent, requestCode,
                        activityOptions.toBundle())
            }
        }

    }

    fun callFeedSearch(context: Context) {
        val intent = createIntent(context, FeedSearchActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        context.startActivity(intent)
    }

    fun callSetting(context: Context) {
        val intent = createIntent(context, SettingListActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        context.startActivity(intent)
    }

    fun callViewMap(context: Context, title: String, latitude: Double,
                    longitude: Double, address: String) {
        val intent = createIntent(context, MapsActivity::class.java, true)

        intent.putExtra(EXTRA_PHOTO_TITLE, title)
        intent.putExtra(EXTRA_LATITUDE, latitude)
        intent.putExtra(EXTRA_LONGITUDE, longitude)
        intent.putExtra(EXTRA_ADDRESS, address)
        context.startActivity(intent)
    }

    fun callPhotogPhoto(context: Context, userName: String, iconFarm: Int, iconServer: String,
                        id: String, pages: Int, type: Int) {
        val intent = createIntent(context, PhotogPhotoActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(EXTRA_SEARPER_NAME, userName)
        intent.putExtra(EXTRA_SEARPER_ICONFARM, iconFarm)
        intent.putExtra(EXTRA_SEARPER_ICONSERVER, iconServer)
        intent.putExtra(EXTRA_SEARPER_ID, id)
        intent.putExtra(EXTRA_PAGES, pages)
        intent.putExtra(EXTRA_PHOTOG_PHOTO_TYPE, type)
        context.startActivity(intent)
    }

    @SuppressLint("RestrictedApi")
    fun callPhotoInfo(activity: Activity, imageView: View, textView: View, photoId: String,
                      ownerId: String, position: Int, requestCode: Int) {

        val context = activity as Context
        val intent = createIntent(context, PhotoInfoActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_PHOTO_ID, photoId)
        intent.putExtra(EXTRA_OWNER_ID, ownerId)
        intent.putExtra(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, position)

        // This code was be blocked temporarily
        /*
        intent.putExtra(FONT_SIZE, (textView as AppCompatTextView).textSize)
        intent.putExtra(PADDING,
                Rect(textView.getPaddingLeft(),
                        textView.getPaddingTop(),
                        textView.getPaddingRight(),
                        textView.getPaddingBottom()))
        intent.putExtra(TEXT_COLOR, textView.currentTextColor)
        */

        val activityOptions = getPhotogPhotoActivityOptions(context, imageView, textView)

        // temporarily use this code..for blocking activity transition animation
        (context as OthersProfileActivity).startActivity(intent)
        // (context as OthersProfileActivity).startActivityForResult(intent, requestCode, activityOptions.toBundle())
        // (context as PhotogPhotoActivity).startActivityForResult(intent, requestCode, activityOptions.toBundle())

    }

    fun callComment(context: Context, photoID: String) {
        val intent = createIntent(context, CommentActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_PHOTO_ID, photoID)
        context.startActivity(intent)
    }

    @SuppressLint("RestrictedApi")
    fun callQuestInfo(fragment: BaseFragment, imageView: View, logoView: View, titleView: View,
                        explanationView: View, ownerNameView: View, quest: Quest,
                        position: Int, calledFrom: Int, requestCode: Int) {
        val context = fragment.activity as Context
        val intent = createIntent(context, QuestInfoActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_QUEST_OWNER_NAME, quest.ownerName)
        intent.putExtra(EXTRA_QUEST_OWNER_LOGO, quest.ownerLogo)
        intent.putExtra(EXTRA_QUEST_OWNER_IMAGE, quest.ownerImage)
        intent.putExtra(EXTRA_QUEST_TITLE, quest.title)
        intent.putExtra(EXTRA_QUEST_DESCRIPTION, quest.description)
        intent.putExtra(EXTRA_QUEST_STATE, quest.state)
        intent.putExtra(EXTRA_QUEST_REWARD, quest.rewards)
        intent.putExtra(EXTRA_QUEST_DURATION, quest.duration)
        intent.putExtra(EXTRA_QUEST_POSITION, position)
        intent.putExtra(EXTRA_QUEST_CALLED_FROM, calledFrom)

        // This code is blocked temporarily
        /*
        intent.putExtra(FONT_SIZE, (titleView as AppCompatTextView).textSize)
        intent.putExtra(PADDING,
                Rect(textView.getPaddingLeft(),
                        textView.getPaddingTop(),
                        textView.getPaddingRight(),
                        textView.getPaddingBottom()))
        intent.putExtra(TEXT_COLOR, textView.currentTextColor)
        */


        fragment.activity!!.startActivityForResult(intent, requestCode,
                getQuestInfoViewActivityOptions(context, imageView, logoView, titleView, explanationView,
                        ownerNameView).toBundle())
    }

    @SuppressLint("RestrictedApi")
    fun callQuestInfo(context: Context, imageView: View, quest: Quest, position: Int,
                        calledFrom: Int, requestCode: Int) {
        val intent = createIntent(context, QuestInfoActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_QUEST_OWNER_NAME, quest.ownerName)
        intent.putExtra(EXTRA_QUEST_OWNER_LOGO, quest.ownerLogo)
        intent.putExtra(EXTRA_QUEST_OWNER_IMAGE, quest.ownerImage)
        intent.putExtra(EXTRA_QUEST_TITLE, quest.title)
        intent.putExtra(EXTRA_QUEST_DESCRIPTION, quest.description)
        intent.putExtra(EXTRA_QUEST_STATE, quest.state)
        intent.putExtra(EXTRA_QUEST_REWARD, quest.rewards)
        intent.putExtra(EXTRA_QUEST_DURATION, quest.duration)
        intent.putExtra(EXTRA_QUEST_POSITION, position)
        intent.putExtra(EXTRA_QUEST_CALLED_FROM, calledFrom)

        // This code is blocked temporarily
        /*
        intent.putExtra(FONT_SIZE, (titleView as AppCompatTextView).textSize)
        intent.putExtra(PADDING,
                Rect(textView.getPaddingLeft(),
                        textView.getPaddingTop(),
                        textView.getPaddingRight(),
                        textView.getPaddingBottom()))
        intent.putExtra(TEXT_COLOR, textView.currentTextColor)
        */

        (context as HomeActivity).startActivityForResult(intent, requestCode,
                getQuestInfoViewActivityOptions(context, imageView).toBundle())
    }

    fun callPinnedPix(context: Context, userName: String, userId: String, iconFarm: Int, iconServer: String) {
        val intent = createIntent(context, PinnedFeedsActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_USER_NAME, userName)
        intent.putExtra(EXTRA_USER_ID, userId)
        intent.putExtra(EXTRA_USER_ICONFARM, iconFarm)
        intent.putExtra(EXTRA_USER_ICONSERVER, iconServer)
        context.startActivity(intent)
    }

    fun callSearpleGallery(context: Context, userName: String, userId: String, iconFarm: Int, iconServer: String) {
        val intent = createIntent(context, SearpleGalleryActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_USER_NAME, userName)
        intent.putExtra(EXTRA_USER_ID, userId)
        intent.putExtra(EXTRA_USER_ICONFARM, iconFarm)
        intent.putExtra(EXTRA_USER_ICONSERVER, iconServer)
        context.startActivity(intent)
    }

    fun callCategoryPhoto(context: Context, imageView: View, image: String, id: String, title: String,
                          pages: Int, type: Int, position: Int, requestCode: Int) {
        val intent = createIntent(context, CategoryPhotoActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_HOME_CATEGORY_IMAGE, image)
        intent.putExtra(EXTRA_HOME_CATEGORY_ID, id)
        intent.putExtra(EXTRA_HOME_CATEGORY_TITLE, title)
        intent.putExtra(EXTRA_PAGES, pages)
        intent.putExtra(EXTRA_CATEGORY_PHOTO_TYPE, type)
        intent.putExtra(EXTRA_CATEGORY_POSITION, position)

        (context as HomeActivity).startActivityForResult(intent, requestCode,
                getCategoryPhotoViewActivityOptions(context, imageView).toBundle())
    }

    fun callCategory(context: Context, imageView: View, requestCode: Int) {
        val intent = createIntent(context, CategoryActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        context.startActivity(intent)
    }

    fun callHotTopicContent(context: Context, imageView: View, id: String, requestCode: Int) {
        val intent = createIntent(context, HotTopicContentActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_HOT_TOPIC_CONTENT_ID, id)

        (context as HomeActivity).startActivityForResult(intent, requestCode,
                getHotTopicContentViewActivityOptions(context, imageView).toBundle())
    }

    /**
     * Checks if all extras are present in an intent.
     *
     * @param intent The intent to check.
     * @param extras The extras to check for.
     * @return `true` if all extras are present, else `false`.
     */
    fun hasAll(intent: Intent, vararg extras: String): Boolean {
        for (extra in extras) {
            if (! intent.hasExtra(extra)) {
                return false
            }
        }
        return true
    }

    /**
     * Checks if any extra is present in an intent.
     *
     * @param intent The intent to check.
     * @param extras The extras to check for.
     * @return `true` if any checked extra is present, else `false`.
     *
     * @deprecated
     */
    fun hasAny(intent: Intent, vararg extras: String): Boolean {
        for (extra in extras) {
            if (intent.hasExtra(extra)) {
                return true
            }
        }
        return false
    }

    private fun getFeedViewActivityOptions(context: Context, imageView: View): ActivityOptions {
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val activity: BaseActivity

        activity = context as HomeActivity

        val decorView = activity.window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(activity, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(activity, imagePair, navPair)
        }
    }

    private fun getQuestInfoViewActivityOptions(context: Context, imageView: View, logoView: View,
                                           titleView: View, explanationView: View,
                                              ownerNameView: View): ActivityOptions {
        val titlePair = Pair.create(titleView, titleView.transitionName)
        val explanationPair = Pair.create(explanationView, explanationView.transitionName)
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val logoPair = Pair.create(logoView, logoView.transitionName)
        val ownerNamePair = Pair.create(ownerNameView, ownerNameView.transitionName)
        val activity: BaseActivity

        activity = context as HomeActivity

        val decorView = activity.window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(activity, titlePair, explanationPair, imagePair,
                                                                logoPair, ownerNamePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(activity, titlePair, explanationPair, imagePair,
                                                                logoPair, navPair, ownerNamePair)
        }
    }

    private fun getQuestInfoViewActivityOptions(context: Context, imageView: View): ActivityOptions {
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as HomeActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, imagePair, navPair)
        }
    }

    private fun getCategoryPhotoViewActivityOptions(context: Context, imageView: View): ActivityOptions {
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as HomeActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, imagePair, navPair)
        }
    }

    private fun getSearchViewActivityOptions(context: Context, imageView: View, textView: View): ActivityOptions {
        val titlePair = Pair.create(textView, textView.transitionName)
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as FeedSearchActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, titlePair, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, titlePair, imagePair, navPair)
        }
    }

    private fun getPinnedFeedViewActivityOptions(context: Context, imageView: View, textView: View): ActivityOptions {
        val titlePair = Pair.create(textView, textView.transitionName)
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as PinnedFeedsActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, titlePair, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, titlePair, imagePair, navPair)
        }
    }

    private fun getSearpleGalleryViewerOptions(context: Context, imageView: View): ActivityOptions {
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as SearpleGalleryActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
                   ActivityOptions.makeSceneTransitionAnimation(context, imagePair)
               } else {
                   val navPair = Pair.create(navBackground, navBackground.transitionName)

                   ActivityOptions.makeSceneTransitionAnimation(context, imagePair, navPair)
               }
    }

    private fun getFeedInfoPhotoViewerOptions(context: Context, imageView: View): ActivityOptions {
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as FeedInfoActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, imagePair, navPair)
        }
    }

    private fun getPhotoInfoPhotoViewerOptions(context: Context, imageView: View): ActivityOptions {
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as PhotoInfoActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, imagePair, navPair)
        }
    }

    private fun getPhotogPhotoViewerOptions(context: Context, imageView: View): ActivityOptions {
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as PhotogPhotoActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, imagePair, navPair)
        }
    }

    private fun getPhotogPhotoActivityOptions(context: Context, imageView: View, textView: View): ActivityOptions {
        val titlePair = Pair.create(textView, textView.transitionName)
        val imagePair = Pair.create(imageView, imageView.transitionName)
        // val decorView: View = (context as PhotogPhotoActivity).window.decorView
        val decorView: View = (context as OthersProfileActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, titlePair, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, titlePair, imagePair, navPair)
        }
    }

    private fun getHomeViewActivityOptions(context: Context, imageView: View): ActivityOptions {
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as HomeActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, imagePair, navPair)
        }
    }

    private fun getHotTopicContentViewActivityOptions(context: Context, imageView: View): ActivityOptions {
        val imagePair = Pair.create(imageView, imageView.transitionName)
        val decorView = (context as HomeActivity).window.decorView
        val navBackground = decorView.findViewById<View>(android.R.id.navigationBarBackground)

        return if (navBackground == null) {
            ActivityOptions.makeSceneTransitionAnimation(context, imagePair)
        } else {
            val navPair = Pair.create(navBackground, navBackground.transitionName)

            ActivityOptions.makeSceneTransitionAnimation(context, imagePair, navPair)
        }
    }

    /**
     * Call Chrome CustomTabs
     *
     * @deprecated used WebView instead.
     */
    @SuppressLint("ObsoleteSdkInt")
    fun callChromeCustomTabs(context: Context, url: String) {
        serviceConnection = ServiceConnection(object : ServiceConnectionCallback {
            override fun onServiceConnected(client: CustomTabsClient) {
                client.warmup(0)

                val session = client.newSession(CustomTabsCallback())
                session.mayLaunchUrl(Uri.parse(url), null, null)
            }

            override fun onServiceDisconnected() {}

        })

        this.url = url

        val builder = CustomTabsIntent.Builder()
        builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                context.resources, R.drawable.ic_close))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setToolbarColor(context.resources
                    .getColor(R.color.colorPrimary, null)).setShowTitle(true)
        } else {
            builder.setToolbarColor(context.resources
                    .getColor(R.color.colorPrimary, null)).setShowTitle(true)
        }

        builder.enableUrlBarHiding()
        val customTabsIntent = builder.build()
        val packageName = CustomTabsHelper.getPackageNameToUse(context)
        CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent)
        CustomTabsClient.bindCustomTabsService(context, packageName, serviceConnection)

        customTabsIntent.launchUrl(context, Uri.parse(url))
    }


    fun callPeopleList(activity: Activity, calledFrom: Int, peopleCode: Int) {
        val intent = createIntent(activity, PeopleActivity::class.java, true)

        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_PEOPLE_TYPE, peopleCode)
        activity.startActivity(intent)
    }

    fun callOtherUserProfile(activity: Activity, calledFrom: Int, userId: String, userName: String, ranking: Int, photoUrl: String) {

        val intent = createIntent(activity, OthersProfileActivity::class.java, true)
        intent.putExtra(EXTRA_PROFILE_USER_ID, userId)
        intent.putExtra(EXTRA_PROFILE_USER_NAME, userName)
        intent.putExtra(EXTRA_PROFILE_USER_RANKING, ranking)
        intent.putExtra(EXTRA_PROFILE_USER_PHOTO_URL, photoUrl)
        intent.putExtra(EXTRA_PLACE_CALLED_USER_PROFILE, calledFrom)

        activity.startActivity(intent)
    }

    fun callSearperRank(context: Context) {
        val intent = createIntent(context, RankingActivity::class.java, true)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun callAuthActivity(activity: Activity) {
        val context = activity as Context
        val intent = createIntent(context, AuthActivity::class.java, true)
        activity.startActivity(intent)
    }

    fun callPhotoGallery(activity: Activity, calledFrom: Int, requestCode: Int) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        when (calledFrom) {
            CALLED_FROM_HOME_MAIN -> {
                (activity as HomeActivity).startActivityForResult(galleryIntent, requestCode)
            }
            CALLED_FROM_AUTH_ACTIVITY -> {
                galleryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                (activity as AuthActivity).startActivityForResult(galleryIntent, requestCode)
            }
        }
    }

    fun callUploadPhoto(activity: Activity, imageUri: String, calledFrom: Int) {
        val context = activity as Context
        val intent = Intent(context, UploadPhotoActivity::class.java)
        intent.putExtra(KEY_SELECTED_IMAGE_URI, imageUri)

        when (calledFrom) {
            CALLED_FROM_HOME_MAIN -> {
                (activity as HomeActivity).startActivity(intent)
            }
            CALLED_FROM_AUTH_ACTIVITY -> {
                (activity as AuthActivity).startActivity(intent)
            }
        }
    }

    fun callVerifierUrl(activity: Activity, token:String, calledFrom: Int) {
        val authUrl = "$AUTHORIZE_URL?oauth_token=$token&perms=write&perms=delete"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)) // open web browser(probably Chrome)

        when (calledFrom) {
            CALLED_FROM_AUTH_ACTIVITY -> {
                (activity as AuthActivity).run {
                    startActivity(intent)
                    finish()
                }
            }
        }
    }


    /**
     * UnBinding service
     *
     * @deprecated
     */
    fun unBindService(context: Context?) {
        serviceConnection?.let {
            context?.unbindService(it)
            serviceConnection = null
        }
    }
}
