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

@file:Suppress("DEPRECATION", "UNUSED_EXPRESSION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.goforer.grabph.presentation.common.view

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import android.text.Html
import android.view.View
import android.view.View.FOCUS_UP
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.utils.CommonUtils.getVersionNumber
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.utils.DisplayUtils
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.FIRST_PAGE
import com.goforer.grabph.presentation.caller.Caller.PHOTOG_PHOTO_FAVORITE_TYPE
import com.goforer.grabph.presentation.caller.Caller.PHOTOG_PHOTO_GENERAL_TYPE
import com.goforer.grabph.presentation.caller.Caller.PHOTOG_PHOTO_POPULAR_TYPE
import com.goforer.grabph.presentation.caller.Caller.callPinnedPix
import com.goforer.grabph.presentation.caller.Caller.callSearpleGallery
import com.goforer.grabph.presentation.common.utils.cache.IntegerVersionSignature
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.view.drawer.item.AdvancePrimaryDrawerItem
import com.goforer.grabph.presentation.common.view.drawer.item.CustomPanelDrawableItem
import com.goforer.grabph.presentation.ui.feed.feedinfo.FeedInfoActivity
import com.goforer.grabph.presentation.ui.feed.photoinfo.PhotoInfoActivity
import com.goforer.grabph.repository.model.cache.data.entity.profile.Owner
import com.goforer.grabph.repository.model.cache.data.entity.profile.Person
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import kotlinx.android.synthetic.main.activity_feed_info.*
import javax.inject.Singleton

@Singleton
class SlidingDrawer(val builder: SlidingDrawerBuilder) {
    var drawerHeader: AccountHeader? =null

    internal var searperProfileDrawerForFeedViewDrawer: Drawer? =null
    internal var searperProfileDrawerForDownloadViewDrawer: Drawer? =null

    internal var ownerProfileDrawer: Drawer? =null

    private lateinit var owner: Owner

    private val context: Context

    private val activity: BaseActivity

    private var bundle: Bundle? = null

    private var workHandler: CommonWorkHandler

    private var followPanelDrawableItem: CustomPanelDrawableItem? =null
    private var locationDrawerItem: AdvancePrimaryDrawerItem? =null
    private var descriptionDrawerItem: AdvancePrimaryDrawerItem? =null
    private var takeOnDateDrawerItem: AdvancePrimaryDrawerItem? =null
    private var viewMorePhotoDrawerItem: CustomPanelDrawableItem? =null
    private var viewGradePhotoDrawerItem: CustomPanelDrawableItem? =null
    private var viewFollowerCountPhotoDrawerItem: CustomPanelDrawableItem? =null
    private var viewFollowingCountPhotoDrawerItem: CustomPanelDrawableItem? =null
    private var viewPopularPhotoDrawerItem: CustomPanelDrawableItem? =null
    private var viewFavoritesPhotoDrawerItem: CustomPanelDrawableItem? =null
    private var viewPinnedFeedCountDrawerItem: CustomPanelDrawableItem? =null
    private var viewSearpleGalleryPhotoCountDrawerItem: CustomPanelDrawableItem? =null
    private var viewPurchasedPhotoCountDrawerItem: CustomPanelDrawableItem? =null
    private var viewMyPhotoCountDrawerItem: CustomPanelDrawableItem? =null
    // Temporarily is blocked
    //private var mViewMoreProfileDrawerItem: LinkDrawerItem? =null

    private var profile: IProfile<ProfileDrawerItem>? =null

    private val type: Int
    private val rootViewRes: Int

    private var ownerProfileCalledFrom: Int = 0

    internal lateinit var searper: Person

    private var searperProfileBackgroundUrl: String? = null

    init {
        context=builder.drawerActivity.applicationContext
        activity=builder.drawerActivity
        bundle= builder.drawerBundle
        rootViewRes=builder.drawerRootViewRes
        type=builder.drawerType
        workHandler=builder.workHandler
    }

    @Singleton
    class SlidingDrawerBuilder {
        lateinit var drawerActivity: BaseActivity
        var drawerBundle: Bundle? = null
        var drawerRootViewRes: Int=0
        var drawerType: Int=0

        lateinit var workHandler: CommonWorkHandler

        fun setActivity(activity: BaseActivity): SlidingDrawerBuilder {
            drawerActivity = activity

            return this
        }

        fun setRootView(rootViewRes: Int): SlidingDrawerBuilder {
            drawerRootViewRes=rootViewRes

            return this
        }

        fun setBundle(bundle: Bundle?): SlidingDrawerBuilder {
            drawerBundle = bundle

            return this
        }

        fun setType(type: Int): SlidingDrawerBuilder {
            drawerType = type

            return this
        }

        fun setWorkHandler(workHandler: CommonWorkHandler): SlidingDrawerBuilder {
            this.workHandler = workHandler

            return this
        }

        fun build(): SlidingDrawer=SlidingDrawer(this)
    }

    companion object {
        internal const val DRAWER_SEARPER_PROFILE_TYPE = 0
        internal const val DRAWER_OWNER_PROFILE_TYPE = 1

        internal const val PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER = 200
        internal const val PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER = 201

        private const val PROFILE_OWNER_TYPE = 202
        private const val DRAWER_PROFILE_ITEM_IDENTIFIER_ID = 300

        internal const val CALLED_FROM_HOME = 400
        internal const val CALLED_FROM_VIEW_ALL = 401
    }

    fun setHeaderBackground(url: String) {
        searperProfileBackgroundUrl = url
    }

    fun setSearperProfileDrawer(searper: Person?, type: Int) {
        searper?.let {
             this.searper = searper
            setDrawer(type)
        }
    }

    fun setOwnerProfileDrawer(owner: Owner?, calledFrom: Int) {
        owner?.let {
            this.owner = owner
            this.ownerProfileCalledFrom = calledFrom
            setDrawer(PROFILE_OWNER_TYPE)
        }
    }

    private fun setDrawer(type: Int) {
        when(type) {
            PROFILE_OWNER_TYPE -> {
                ownerProfileDrawer?.let {
                    updateOwnerProfileDrawer()

                    return
                }

                ownerProfileDrawer ?: createOwnerProfileDrawer(activity, rootViewRes, ownerProfileCalledFrom, bundle)
            }

            PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER -> {
                searperProfileDrawerForFeedViewDrawer?.let {
                    updateSearperProfileDrawer(activity, bundle, type)

                    return
                }

                searperProfileDrawerForFeedViewDrawer ?: setDrawerItem(activity, rootViewRes, bundle, type)
            }

            PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                searperProfileDrawerForDownloadViewDrawer?.let {
                    updateSearperProfileDrawer(activity, bundle, type)

                    return
                }

                searperProfileDrawerForDownloadViewDrawer ?: setDrawerItem(activity, rootViewRes, bundle, type)
            }
        }
    }

    private fun buildOwnerHeader(activity: Activity, profile: IProfile<ProfileDrawerItem>,
                                 savedInstanceState: Bundle?, headerBackground: Drawable) {
        // Create the AccountHeader
        drawerHeader=AccountHeaderBuilder()
                .withActivity(activity)
                .withTranslucentStatusBar(true)
                .withDividerBelowHeader(true)
                .withCompactStyle(false)
                .withCurrentProfileHiddenInList(true)
                .withHeaderBackground(headerBackground)
                .withTextColor(context.applicationContext.resources
                        .getColor(R.color.whiteLight))
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .withHeightRes(R.dimen.height_254)
                .addProfiles(
                        profile,
                        ProfileSettingDrawerItem()
                                .withName(context.resources.getString(R.string.drawer_item_edit_profile))
                                .withTextColor(context.applicationContext.resources
                                        .getColor(R.color.whiteLight))
                                .withIcon(R.drawable.ic_drawer_edit_profile)
                )
                .withOnAccountHeaderListener { _, _, _ -> true }
                .withProfileImagesVisible(true)
                .withSavedInstance(savedInstanceState)
                .build()
    }

    private fun buildSearperHeader(activity: Activity, profile: IProfile<ProfileDrawerItem>,
                                   savedInstanceState: Bundle?, headerBackground: Bitmap) {
        val drawable = BitmapDrawable(activity.applicationContext.resources, headerBackground)

        // Create the AccountHeader
        drawerHeader=AccountHeaderBuilder()
                .withActivity(activity)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(drawable)
                .withTextColor(context.applicationContext.resources
                        .getColor(R.color.whiteLight))
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .withHeightDp(headerBackground.height)
                .addProfiles(
                        profile
                )
                .withOnAccountHeaderListener { _, _, _ -> false }
                .withProfileImagesVisible(true)
                .withSavedInstance(savedInstanceState)
                .build()
    }

    private fun setFollowDrawableItem(followDrawerItem: CustomPanelDrawableItem?, type: Int) {
        followDrawerItem?.let {
            val drawer = followDrawerItem
                    .withName(R.string.follow_condition)
                    .withIcon(R.drawable.ic_drawer_follow)
                    .withCaption("FOLLOW")
                    .withArrowVisible(false)
                    .withCaptionTextColor(context.applicationContext.resources
                            .getColor(R.color.colorMaterialDrawablePhotoCount))
                    .withTextColor(context.applicationContext.resources
                            .getColor(R.color.colorMaterialDrawableText))
                    .withCaptionBackgroundColor(R.color.colorFollow)
                    .withSelectable(false)
            when (type) {
                PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER -> {
                    drawer
                            .withOnDrawerItemClickListener { _, _, drawerItem ->
                                drawerItem ?: false
                                followDrawerItem.withSetSelected(false)
                                showToastMessage(activity, "Will be implemented....",
                                        Toast.LENGTH_SHORT)
                                searperProfileDrawerForFeedViewDrawer?.closeDrawer()
                                false
                            }

                }
                PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                    drawer
                            .withOnDrawerItemClickListener { _, _, drawerItem ->
                                drawerItem ?: false
                                followDrawerItem.withSetSelected(false)
                                showToastMessage(activity, "Will be implemented....",
                                        Toast.LENGTH_SHORT)
                                searperProfileDrawerForDownloadViewDrawer?.closeDrawer()
                                false
                            }
                }
                else -> {
                }
            }
        }
    }

    private fun setLocationPrimaryDrawerItem(locationPrimaryDrawerItem: AdvancePrimaryDrawerItem?,
                                             type: Int) {
        var location: String?

        when(type) {
            PROFILE_OWNER_TYPE -> {
                location = owner.location ?: activity.getString(R.string.no_location)
            }
            else -> {
                searper.location ?: return
                location = searper.location?._content ?: activity.getString(R.string.no_location)
                searper.location?._content?.let {
                    location = searper.location?._content
                }
            }
        }

        locationPrimaryDrawerItem
                ?.withName(R.string.drawer_item_location)
                ?.withMaxLines(2)
                ?.withDescription(location!!)
                ?.withIcon(R.drawable.ic_drawer_location)
                ?.withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                ?.withDescriptionTextColor(context.applicationContext
                        .resources
                        .getColor(R.color.colorMaterialDrawableText))
                ?.withSelectable(false)
    }

    private fun setDescriptionPrimaryDrawerItem(descriptionDrawerItem: AdvancePrimaryDrawerItem?,
                                                type: Int) {
        var description: String?

        when(type) {
            PROFILE_OWNER_TYPE -> {
                description = owner.description ?: activity.getString(R.string.no_description)
            }
            else -> {
                searper.description ?: return
                description = searper.description?._content ?: activity.getString(R.string.no_description)
                searper.description?._content?.let {
                    description = searper.description?._content
                }
            }
        }

        descriptionDrawerItem
                ?.withName(R.string.drawer_item_description)
                ?.withMaxLines(12)
                ?.withDescription(Html.fromHtml(description).toString())
                ?.withIcon(R.drawable.ic_drawer_description)
                ?.withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                ?.withDescriptionTextColor(context.applicationContext
                        .resources
                        .getColor(R.color.colorMaterialDrawableText))
                ?.withSelectable(false)
    }

    private fun setTakeOnDatePrimaryDrawerItem(takeOnDateDrawerItem: AdvancePrimaryDrawerItem?) {
        var date: String? = null

        searper.photos?.firstdatetaken?.let {
            searper.photos?.firstdatetaken?._content?.let {
                date = searper.photos?.firstdatetaken?._content
            }

            date = searper.photos?.firstdatetaken?._content ?: activity.getString(R.string.no_taken_on_date)
        }

        date = date ?: activity.getString(R.string.no_taken_on_date)

        takeOnDateDrawerItem
                ?.withName(R.string.drawer_item_taken_on_date)
                ?.withMaxLines(2)
                ?.withDescription(date!!)
                ?.withIcon(R.drawable.ic_drawer_date)
                ?.withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableCount))
                ?.withDescriptionTextColor(context.applicationContext
                        .resources
                        .getColor(R.color.colorMaterialDrawableText))
                ?.withSelectable(false)
    }

    private fun setViewMorePhotoPrimaryDrawerItem(viewMoreDrawerItem: CustomPanelDrawableItem?) {
        val count: String? = if (searper.photos?.count == null || searper.photos?.count?._content == null
                || searper.photos?.count?._content?.isEmpty()!!) {
            "0"
        } else {
            searper.photos?.count?._content
        }

        viewMoreDrawerItem
                ?.withName(R.string.view_more_photo)
                ?.withIcon(R.drawable.ic_drawer_view_more_photo)
                ?.withCaption(count.toString())
                ?.withCaptionBackgroundColor(R.color.colorCustomPanel)
                ?.withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawablePhotoCount))
                ?.withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                ?.withSelectable(false)
                ?.withArrowVisible(false)
                ?.withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    val name: String? = if (searper.realname != null && searper.realname?._content != null
                            && !searper.realname!!._content!!.isEmpty()) {
                        searper.realname?._content
                    } else {
                        if (searper.username == null) {
                            if (searper.username?._content != null) {
                                searper.username?._content
                            }
                        }

                        searper.username?._content
                    }

                    if (count != "0") {
                        Caller.callPhotogPhoto(context.applicationContext, name!!,
                                searper.iconfarm, searper.iconserver, searper.id,
                                FIRST_PAGE, PHOTOG_PHOTO_GENERAL_TYPE)
                        viewMoreDrawerItem.withSetSelected(false)
                        searperProfileDrawerForFeedViewDrawer?.closeDrawer()
                    }
                    false
                }
    }

    private fun  getUserName(userName: Person.Username?): String {
        userName?.let {
            userName._content?.let {
                return userName._content
            }

            return activity.getString(R.string.no_username)
        }

        return activity.getString(R.string.no_username)
    }

    private fun setViewPopularPhotoPrimaryDrawerItem(viewMoreDrawerItem: CustomPanelDrawableItem?) {
        viewMoreDrawerItem
                ?.withName(R.string.drawer_item_searper_popular_photo_list)
                ?.withIcon(R.drawable.ic_drawer_popular_photos)
                ?.withCaption(GeneralFunctions.rand(1, 12000).toString())
                ?.withCaptionBackgroundColor(R.color.colorCustomPanel)
                ?.withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawablePhotoCount))
                ?.withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                ?.withSelectable(false)
                ?.withArrowVisible(false)
                ?.withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    var name: String? = null

                    searper.realname?.let {
                        if (!searper.realname?._content?.isEmpty()!!) {
                            name = searper.realname?._content
                        }
                    }

                    name = name ?: getUserName(searper.username)

                    Caller.callPhotogPhoto(context.applicationContext, name!!,
                            searper.iconfarm, searper.iconserver, searper.id,
                            FIRST_PAGE, PHOTOG_PHOTO_POPULAR_TYPE)
                    false
                }
    }

    private fun setViewFavoritesPhotoPrimaryDrawerItem(viewMoreDrawerItem: CustomPanelDrawableItem?) {
        viewMoreDrawerItem
                ?.withName(R.string.drawer_item_searper_favorites_photo_list)
                ?.withIcon(R.drawable.ic_drawer_favorite_photos)
                ?.withCaption(GeneralFunctions.rand(1, 12000).toString())
                ?.withCaptionBackgroundColor(R.color.colorCustomPanel)
                ?.withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawablePhotoCount))
                ?.withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                ?.withSelectable(false)
                ?.withArrowVisible(false)
                ?.withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    var name: String? = null

                    searper.realname?.let {
                        if (!searper.realname?._content?.isEmpty()!!) {
                            name = searper.realname?._content
                        }
                    }

                    name = name ?: getUserName(searper.username)

                    Caller.callPhotogPhoto(context.applicationContext, name!!,
                            searper.iconfarm, searper.iconserver, searper.id,
                            FIRST_PAGE, PHOTOG_PHOTO_FAVORITE_TYPE)
                    false
                }
    }

    /*
    // Temporarily is blocked
    private fun setViewMoreProfilePrimaryDrawerItem(viewMoreDrawerItem: LinkDrawerItem, type: Int) {
        when (type) {
            PROFILE_PHOTO_SEARPER_TYPE_FROM_FEED_VIEWER -> {
                viewMoreDrawerItem
                        .withName(R.string.view_more_profile)
                        .withIcon(R.drawable.ic_drawer_view_more_profile)
                        .withTextColor(mContext.applicationContext.resources
                                .getColor(R.color.primary))
                        .withSelectable(false)
                        .withOnDrawerItemClickListener { _, _, drawerItem ->
                            drawerItem ?: false
                            if (searper.profileurl != null
                                    || searper.profileurl?._content != null
                                    || !searper.profileurl?._content?.isEmpty()!!) {
                                Caller.INSTANCE.callChromeCustomTabs(
                                        mContext, searper.profileurl?._content!!)
                                searperProfileDrawerForFeedViewDrawer?.closeDrawer()
                            } else {
                                showToastMessage(mActivity,
                                        mContext.getString(R.string.no_more_photo), Toast.LENGTH_SHORT)
                            }
                            viewMoreDrawerItem.withSetSelected(false)
                            grapherProfileDrawerForFeedViewDrawer?.closeDrawer()
                            false
                        }
            }
            PROFILE_PHOTO_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                viewMoreDrawerItem
                        .withName(R.string.view_more_profile)
                        .withIcon(R.drawable.ic_drawer_view_more_profile)
                        .withTextColor(mContext.applicationContext.resources
                                .getColor(R.color.primary))
                        .withSelectable(false)
                        .withOnDrawerItemClickListener { _, _, drawerItem ->
                            drawerItem ?: false
                            if (grapher.profileurl != null
                                    || grapher.profileurl?._content != null
                                    || !grapher.profileurl?._content?.isEmpty()!!) {
                                Caller.INSTANCE.callChromeCustomTabs(
                                        mContext, grapher.profileurl?._content!!)
                                grapherProfileDrawerForDownloadViewDrawer?.closeDrawer()
                            } else {
                                showToastMessage(mActivity,
                                        mContext.getString(R.string.no_more_photo), Toast.LENGTH_SHORT)
                            }
                            viewMoreDrawerItem.withSetSelected(false)
                            grapherProfileDrawerForDownloadViewDrawer?.closeDrawer()
                            false
                        }
            }
        }
    }
    */

    private fun setHeader(profile: IProfile<ProfileDrawerItem>?, type: Int) {
        setInfo(profile, type)
    }

    private fun setInfo(profile: IProfile<ProfileDrawerItem>?, type: Int) {
        val photoPath: String?
        val realName: String?
        val userName: String?

        when (type) {
            PROFILE_OWNER_TYPE -> {
                photoPath = owner.photourl?._content
                realName = if (owner.realname == null || owner.realname?.isEmpty()!!) {
                    activity.getString(R.string.no_realname)
                } else {
                    owner.realname!!
                }

                userName = if (owner.username == null || owner.username?.isEmpty()!!) {
                    activity.getString(R.string.no_username)
                } else {
                    owner.username!!
                }

                if (owner.photourl!!._content == "") {
                    profile?.withIcon(context.applicationContext.getDrawable(R.drawable.ic_default_profile))
                } else {
                    profile?.withIcon(photoPath)
                }
            }

            else -> {
                photoPath = workHandler.getProfilePhotoURL(searper.iconfarm, searper.iconserver, searper.id)
                realName = if (searper.realname == null || searper.realname?._content == null
                        || searper.realname?._content?.isEmpty()!!) {
                    activity.getString(R.string.no_realname)
                } else {
                    searper.realname?._content
                }

                userName = if (searper.username == null || searper.username?._content == null
                        || searper.username?._content?.isEmpty()!!) {
                    activity.getString(R.string.no_username)
                } else {
                    searper.username?._content
                }

                if (searper.iconserver == "0") {
                    profile?.withIcon(context.applicationContext.getDrawable(R.drawable.ic_default_profile))
                } else {
                    profile?.withIcon(photoPath)
                }
            }
        }

        profile!!.withName(realName)
                .withEmail(userName)
                .withIdentifier(DRAWER_PROFILE_ITEM_IDENTIFIER_ID.toLong())
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    drawerItem.withSetSelected(false)
                    showToastMessage(activity, "Edit Profile Will be implemented....",
                            Toast.LENGTH_SHORT)

                    ownerProfileDrawer?.closeDrawer()

                    false
                }
    }

    private fun setDrawerItem(activity: BaseActivity, @IdRes rootViewRes: Int,
                              savedInstanceState: Bundle?, type: Int) {
        val options = RequestOptions
                .fitCenterTransform()
                .placeholder(R.color.colorDefaultDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .signature(IntegerVersionSignature(getVersionNumber()))

        profile = ProfileDrawerItem()
        setHeader(profile, type)
        searperProfileBackgroundUrl ?: fail("The url for the header's background in SlidingDrawer required.")

        if (!activity.isFinishing) {
            Glide.with(activity.applicationContext)
                    .asBitmap()
                    .apply(options)
                    .load(searperProfileBackgroundUrl)
                    .thumbnail(0.5f)
                    .listener(object: RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                                  isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                     dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            buildSearperHeader(activity, profile as ProfileDrawerItem, savedInstanceState,
                                    resource!!)
                            //assert toolbar != null;
                            followPanelDrawableItem = CustomPanelDrawableItem()
                            setFollowDrawableItem(followPanelDrawableItem, type)
                            locationDrawerItem = AdvancePrimaryDrawerItem()
                            setLocationPrimaryDrawerItem(locationDrawerItem, type)
                            descriptionDrawerItem = AdvancePrimaryDrawerItem()
                            setDescriptionPrimaryDrawerItem(descriptionDrawerItem, type)
                            takeOnDateDrawerItem = AdvancePrimaryDrawerItem()
                            setTakeOnDatePrimaryDrawerItem(takeOnDateDrawerItem)
                            viewMorePhotoDrawerItem = CustomPanelDrawableItem()
                            setViewMorePhotoPrimaryDrawerItem(viewMorePhotoDrawerItem)
                            viewPopularPhotoDrawerItem = CustomPanelDrawableItem()
                            setViewPopularPhotoPrimaryDrawerItem(viewPopularPhotoDrawerItem)
                            viewFavoritesPhotoDrawerItem = CustomPanelDrawableItem()
                            setViewFavoritesPhotoPrimaryDrawerItem(viewFavoritesPhotoDrawerItem)
                            viewGradePhotoDrawerItem = CustomPanelDrawableItem()
                            setViewGradePrimaryDrawerItem(viewGradePhotoDrawerItem, type)
                            viewFollowerCountPhotoDrawerItem = CustomPanelDrawableItem()
                            setViewFollowerCountPrimaryDrawerItem(viewFollowerCountPhotoDrawerItem, type)
                            viewFollowingCountPhotoDrawerItem = CustomPanelDrawableItem()
                            setViewFollowingCountPrimaryDrawerItem(viewFollowingCountPhotoDrawerItem, type)

                            /*
                            // Temporarily is blocked
                            mViewMoreProfileDrawerItem=LinkDrawerItem()
                            setViewMoreProfilePrimaryDrawerItem(mViewMoreProfileDrawerItem!!, type)
                            */

                            when (type) {
                                PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER -> {
                                    searperProfileDrawerForFeedViewDrawer = createSearperDrawer(activity, savedInstanceState)
                                }
                                PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                                    searperProfileDrawerForDownloadViewDrawer = createSearperDrawer(activity, savedInstanceState)
                                }
                            }

                            savedInstanceState ?: let {
                                profile?.let {
                                    drawerHeader?.activeProfile = profile
                                }
                            }

                            return false
                        }
                    })
                    .submit()
        }
    }

    private fun fail(message: String): Nothing {
        throw IllegalArgumentException(message)
    }

    private fun createSearperDrawer(activity: BaseActivity, savedInstanceState: Bundle?): Drawer? {
        return DrawerBuilder()
                .withActivity(activity)
                .withActionBarDrawerToggle(true)
                .withTranslucentNavigationBar(true)
                .withTranslucentStatusBar(true)
                .withSliderBackgroundColor(context.resources.getColor(R.color.colorSearperTransparent))
                .withActionBarDrawerToggleAnimated(true)
                .withHasStableIds(true)
                .withSelectedItem(-1)
                .withAccountHeader(drawerHeader!!) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        followPanelDrawableItem,
                        locationDrawerItem,
                        descriptionDrawerItem,
                        takeOnDateDrawerItem,
                        viewGradePhotoDrawerItem,
                        viewFollowerCountPhotoDrawerItem,
                        viewFollowingCountPhotoDrawerItem,
                        viewMorePhotoDrawerItem,
                        viewPopularPhotoDrawerItem,
                        viewFavoritesPhotoDrawerItem
                        // Temporarily is blocked
                        //mViewMoreProfileDrawerItem
                ) // Add the items we want to use with our SlidingDr
                .withOnDrawerNavigationListener {
                    // This method is only called if the Arrow icon is shown.
                    // The hamburger is automatically managed by the MaterialDrawer if the back
                    // arrow is shown. Close the activity
                    activity.finish()
                    // Return true if we have consumed the event
                    true
                }
                .withOnDrawerListener(object : Drawer.OnDrawerListener {
                    override fun onDrawerOpened(drawerView: View) {}

                    override fun onDrawerClosed(drawerView: View) {}

                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        if (activity is FeedInfoActivity || activity is PhotoInfoActivity) {
                            activity.scroll_view.fullScroll(FOCUS_UP)
                        }
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withOnDrawerItemClickListener { _, _, _ -> false }
                .withShowDrawerOnFirstLaunch(true)
                .build()
    }

    private fun updateOwnerProfileDrawer() {
        setHeader(profile, PROFILE_OWNER_TYPE)
        drawerHeader?.updateProfile(profile!!)
        setLocationPrimaryDrawerItem(locationDrawerItem, PROFILE_OWNER_TYPE)
        ownerProfileDrawer?.updateItem(locationDrawerItem!!)
        setDescriptionPrimaryDrawerItem(descriptionDrawerItem, PROFILE_OWNER_TYPE)
        ownerProfileDrawer?.updateItem(descriptionDrawerItem!!)
        setViewGradePrimaryDrawerItem(viewGradePhotoDrawerItem, PROFILE_OWNER_TYPE)
        ownerProfileDrawer?.updateItem(viewGradePhotoDrawerItem!!)
        setViewFollowerCountPrimaryDrawerItem(viewFollowerCountPhotoDrawerItem, PROFILE_OWNER_TYPE)
        ownerProfileDrawer?.updateItem(viewFollowerCountPhotoDrawerItem!!)
        setViewFollowingCountPrimaryDrawerItem(viewFollowingCountPhotoDrawerItem, PROFILE_OWNER_TYPE)
        ownerProfileDrawer?.updateItem(viewFollowingCountPhotoDrawerItem!!)
        setViewPinnedFeedCountPrimaryDrawerItem(viewPinnedFeedCountDrawerItem)
        ownerProfileDrawer?.updateItem(viewPinnedFeedCountDrawerItem!!)
        setViewSearpleGalleryPhotoCountPrimaryDrawerItem(viewSearpleGalleryPhotoCountDrawerItem)
        ownerProfileDrawer?.updateItem(viewSearpleGalleryPhotoCountDrawerItem!!)
        setViewPurchasedPhotoCountPrimaryDrawerItem(viewPurchasedPhotoCountDrawerItem)
        ownerProfileDrawer?.updateItem(viewPurchasedPhotoCountDrawerItem!!)
        setViewMyPhotoCountPrimaryDrawerItem(viewMyPhotoCountDrawerItem)
        ownerProfileDrawer?.updateItem(viewMyPhotoCountDrawerItem!!)
    }

    private fun updateSearperProfileDrawer(activity: BaseActivity, savedInstanceState: Bundle?, type: Int) {
        val options = RequestOptions
                .fitCenterTransform()
                .placeholder(R.color.colorDefaultDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .signature(IntegerVersionSignature(getVersionNumber()))

        searperProfileBackgroundUrl ?: fail("The url for the header's background in SlidingDrawer required.")
        when (type) {
            PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER -> {
                if (!activity.isFinishing) {
                    setHeader(profile, type)
                    Glide.with(activity.applicationContext)
                            .asBitmap()
                            .apply(options)
                            .load(searperProfileBackgroundUrl)
                            .thumbnail(0.5f)
                            .listener(object: RequestListener<Bitmap> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                                          isFirstResource: Boolean): Boolean {
                                    return false
                                }

                                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                             dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    buildSearperHeader(activity, profile as ProfileDrawerItem, savedInstanceState,
                                            resource!!)
                                    //assert toolbar != null;
                                    followPanelDrawableItem = CustomPanelDrawableItem()
                                    setFollowDrawableItem(followPanelDrawableItem, type)
                                    locationDrawerItem = AdvancePrimaryDrawerItem()
                                    setLocationPrimaryDrawerItem(locationDrawerItem, type)
                                    descriptionDrawerItem = AdvancePrimaryDrawerItem()
                                    setDescriptionPrimaryDrawerItem(descriptionDrawerItem, type)
                                    takeOnDateDrawerItem = AdvancePrimaryDrawerItem()
                                    setTakeOnDatePrimaryDrawerItem(takeOnDateDrawerItem)
                                    viewGradePhotoDrawerItem=CustomPanelDrawableItem()
                                    setViewGradePrimaryDrawerItem(viewGradePhotoDrawerItem, type)
                                    viewFollowerCountPhotoDrawerItem=CustomPanelDrawableItem()
                                    setViewFollowerCountPrimaryDrawerItem(viewFollowerCountPhotoDrawerItem, type)
                                    viewFollowingCountPhotoDrawerItem=CustomPanelDrawableItem()
                                    setViewFollowingCountPrimaryDrawerItem(viewFollowingCountPhotoDrawerItem, type)
                                    viewMorePhotoDrawerItem = CustomPanelDrawableItem()
                                    setViewMorePhotoPrimaryDrawerItem(viewMorePhotoDrawerItem)
                                    viewPopularPhotoDrawerItem = CustomPanelDrawableItem()
                                    setViewPopularPhotoPrimaryDrawerItem(viewPopularPhotoDrawerItem)
                                    viewFavoritesPhotoDrawerItem = CustomPanelDrawableItem()
                                    setViewFavoritesPhotoPrimaryDrawerItem(viewFavoritesPhotoDrawerItem)

                                    /*
                                    // Temporarily is blocked
                                    mViewMoreProfileDrawerItem=LinkDrawerItem()
                                    setViewMoreProfilePrimaryDrawerItem(mViewMoreProfileDrawerItem!!, type)
                                    */

                                    when (type) {
                                        PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER -> {
                                            searperProfileDrawerForFeedViewDrawer = createSearperDrawer(activity, savedInstanceState)
                                        }
                                        PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                                            searperProfileDrawerForDownloadViewDrawer = createSearperDrawer(activity, savedInstanceState)
                                        }
                                    }

                                    savedInstanceState ?: let {
                                        profile?.let {
                                            drawerHeader?.activeProfile = profile
                                        }
                                    }

                                    return false
                                }
                            })
                            .submit()

                    Glide.with(activity.applicationContext)
                            .asBitmap()
                            .apply(options)
                            .load(searperProfileBackgroundUrl)
                            .thumbnail(0.5f)
                            .listener(object: RequestListener<Bitmap> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                                          isFirstResource: Boolean): Boolean {
                                    return false
                                }

                                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                             dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    drawerHeader?.view?.layoutParams?.height = DisplayUtils.convertToDp(activity.applicationContext, resource?.height!!)
                                    drawerHeader?.headerBackgroundView?.layoutParams?.height = DisplayUtils.convertToDp(activity.applicationContext, resource.height)
                                    drawerHeader?.headerBackgroundView?.setImageBitmap(resource)

                                    return false
                                }
                            })
                            .submit()

                    drawerHeader?.updateProfile(profile!!)
                    setFollowDrawableItem(followPanelDrawableItem, type)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(followPanelDrawableItem!!)
                    setLocationPrimaryDrawerItem(locationDrawerItem, type)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(locationDrawerItem!!)
                    setDescriptionPrimaryDrawerItem(descriptionDrawerItem, type)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(descriptionDrawerItem!!)
                    setTakeOnDatePrimaryDrawerItem(takeOnDateDrawerItem)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(takeOnDateDrawerItem!!)
                    setViewGradePrimaryDrawerItem(viewGradePhotoDrawerItem, type)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(viewGradePhotoDrawerItem!!)
                    setViewFollowerCountPrimaryDrawerItem(viewFollowerCountPhotoDrawerItem, type)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(viewFollowerCountPhotoDrawerItem!!)
                    setViewFollowingCountPrimaryDrawerItem(viewFollowingCountPhotoDrawerItem, type)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(viewFollowingCountPhotoDrawerItem!!)
                    setViewMorePhotoPrimaryDrawerItem(viewMorePhotoDrawerItem)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(viewMorePhotoDrawerItem!!)
                    setViewPopularPhotoPrimaryDrawerItem(viewPopularPhotoDrawerItem)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(viewPopularPhotoDrawerItem!!)
                    setViewFavoritesPhotoPrimaryDrawerItem(viewFavoritesPhotoDrawerItem)
                    searperProfileDrawerForFeedViewDrawer?.updateItem(viewFavoritesPhotoDrawerItem!!)

                    /*
                    // Temporarily is blocked
                    //setViewMoreProfilePrimaryDrawerItem(mViewMoreProfileDrawerItem!!, type)
                    //grapherProfileDrawerForFeedViewDrawer!!.updateItem(mViewMoreProfileDrawerItem!!)
                    */
                }
            }
            PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                if (!activity.isFinishing) {
                    setHeader(profile, type)
                    Glide.with(activity.applicationContext)
                            .asBitmap()
                            .apply(options)
                            .load(searperProfileBackgroundUrl)
                            .thumbnail(0.5f)
                            .listener(object: RequestListener<Bitmap> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                                          isFirstResource: Boolean): Boolean {
                                    return false
                                }

                                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                             dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    drawerHeader?.view?.layoutParams?.height = DisplayUtils.convertToDp(activity.applicationContext, resource?.height!!)
                                    drawerHeader?.headerBackgroundView?.layoutParams?.height = DisplayUtils.convertToDp(activity.applicationContext, resource.height)
                                    drawerHeader?.headerBackgroundView?.setImageBitmap(resource)

                                    return false
                                }
                            })
                            .submit()

                    drawerHeader!!.updateProfile(profile!!)
                    setFollowDrawableItem(followPanelDrawableItem, type)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(followPanelDrawableItem!!)
                    setLocationPrimaryDrawerItem(locationDrawerItem, type)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(locationDrawerItem!!)
                    setDescriptionPrimaryDrawerItem(descriptionDrawerItem, type)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(descriptionDrawerItem!!)
                    setTakeOnDatePrimaryDrawerItem(takeOnDateDrawerItem)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(takeOnDateDrawerItem!!)
                    setViewGradePrimaryDrawerItem(viewGradePhotoDrawerItem, type)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(viewGradePhotoDrawerItem!!)
                    setViewFollowerCountPrimaryDrawerItem(viewFollowerCountPhotoDrawerItem, type)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(viewFollowerCountPhotoDrawerItem!!)
                    setViewFollowingCountPrimaryDrawerItem(viewFollowingCountPhotoDrawerItem, type)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(viewFollowingCountPhotoDrawerItem!!)
                    setViewMorePhotoPrimaryDrawerItem(viewMorePhotoDrawerItem)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(viewMorePhotoDrawerItem!!)
                    setViewPopularPhotoPrimaryDrawerItem(viewPopularPhotoDrawerItem)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(viewPopularPhotoDrawerItem!!)
                    setViewFavoritesPhotoPrimaryDrawerItem(viewFavoritesPhotoDrawerItem)
                    searperProfileDrawerForDownloadViewDrawer?.updateItem(viewFavoritesPhotoDrawerItem!!)

                    /*
                    // Temporarily is blocked
                    //setViewMoreProfilePrimaryDrawerItem(mViewMoreProfileDrawerItem!!, type)
                    //grapherProfileDrawerForDownloadViewDrawer!!.updateItem(mViewMoreProfileDrawerItem!!)
                    */
                }
            }
        }
    }

    private fun setViewGradePrimaryDrawerItem(gradeDrawerItem: CustomPanelDrawableItem?, type: Int) {
        val count: String?
        when(type) {
            PROFILE_OWNER_TYPE -> {
                count = if (owner.grade.equals(null)
                        || owner.grade?.isEmpty()!!) {
                    "0"
                } else {
                    owner.grade
                }
            }

            PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER,
            PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                count = if (searper.grade.equals(null)
                        || searper.grade?.isEmpty()!!) {

                    // Now I've to use rand value because our Back-end server does not set up.
                    // The below code have to be removed since our Back-end server will be set up...
                    GeneralFunctions.rand(1, 5).toString()
                } else {
                    searper.grade
                }

            }

            else -> {
                count = if (owner.grade.equals(null)
                        || owner.grade?.isEmpty()!!) {
                    "0"
                } else {
                    owner.grade
                }
            }
        }

        gradeDrawerItem!!
                .withName(R.string.grade_count)
                .withIcon(R.drawable.ic_drawer_view_grade)
                .withCaption(count.toString())
                .withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withSelectable(false)
                .withArrowVisible(false)
                .withCaptionBackgroundColor(R.color.colorGrade)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    if (count != "0") {
                        gradeDrawerItem.withSetSelected(false)
                        when(type) {
                            PROFILE_OWNER_TYPE -> {
                                ownerProfileDrawer?.closeDrawer()
                            }

                            PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER -> {
                                searperProfileDrawerForFeedViewDrawer?.closeDrawer()
                            }

                            PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                                searperProfileDrawerForDownloadViewDrawer?.closeDrawer()
                            }

                            else -> {
                                ownerProfileDrawer?.closeDrawer()
                            }
                        }
                    }

                    false
                }
    }

    private fun setViewFollowerCountPrimaryDrawerItem(followerCountDrawerItem: CustomPanelDrawableItem?, type: Int) {
        val count: String?
        when(type) {
            PROFILE_OWNER_TYPE -> {
                count = if (owner.followers.equals(null)
                        || owner.followers?.isEmpty()!!) {
                    "0"
                } else {
                    owner.followers
                }
            }
            PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER,
            PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                count = if (searper.grade.equals(null)
                        || searper.grade?.isEmpty()!!) {

                    // Now I've to use rand value because our Back-end server does not set up.
                    // The below code have to be removed since our Back-end server will be set up...
                    GeneralFunctions.rand(0, 12000).toString()
                } else {
                    searper.followers
                }

            }
            else -> {
                count = if (owner.followers.equals(null)
                        || owner.followers?.isEmpty()!!) {
                    "0"
                } else {
                    owner.followers
                }
            }
        }

        followerCountDrawerItem!!
                .withName(R.string.follower_count)
                .withIcon(R.drawable.ic_drawer_view_folllower_counting)
                .withCaption(count.toString())
                .withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withSelectable(false)
                .withArrowVisible(false)
                .withCaptionBackgroundColor(R.color.colorGrade)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    if (count != "0") {
                        followerCountDrawerItem.withSetSelected(false)
                        when(type) {
                            PROFILE_OWNER_TYPE -> {
                                ownerProfileDrawer?.closeDrawer()
                            }

                            PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER -> {
                                searperProfileDrawerForFeedViewDrawer?.closeDrawer()
                            }

                            PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                                searperProfileDrawerForDownloadViewDrawer?.closeDrawer()
                            }

                            else -> {
                                ownerProfileDrawer?.closeDrawer()
                            }
                        }
                    }

                    false
                }
    }

    private fun setViewFollowingCountPrimaryDrawerItem(followingCountDrawerItem: CustomPanelDrawableItem?, type: Int) {
        val count: String?
        when(type) {
            PROFILE_OWNER_TYPE -> {
                count = if (owner.followings.equals(null)
                        || owner.followings?.isEmpty()!!) {
                    "0"
                } else {
                    owner.followings
                }
            }
            PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER,
            PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                count = if (searper.followings.equals(null)
                        || searper.followings?.isEmpty()!!) {

                    // Now I've to use rand value because our Back-end server does not set up.
                    // The below code have to be removed since our Back-end server will be set up...
                    GeneralFunctions.rand(0, 12000).toString()
                } else {
                    searper.followings
                }

            }
            else -> {
                count = if (owner.followings.equals(null)
                        || owner.followings?.isEmpty()!!) {
                    "0"
                } else {
                    owner.followings
                }
            }
        }

        followingCountDrawerItem!!
                .withName(R.string.following_count)
                .withIcon(R.drawable.ic_drawer_view_following_counting)
                .withCaption(count.toString())
                .withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withSelectable(false)
                .withArrowVisible(false)
                .withCaptionBackgroundColor(R.color.colorGrade)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    if (count != "0") {
                        followingCountDrawerItem.withSetSelected(false)
                        when(type) {
                            PROFILE_OWNER_TYPE -> {
                                ownerProfileDrawer?.closeDrawer()
                            }

                            PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER -> {
                                searperProfileDrawerForFeedViewDrawer?.closeDrawer()
                            }

                            PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER -> {
                                searperProfileDrawerForDownloadViewDrawer?.closeDrawer()
                            }

                            else -> {
                                ownerProfileDrawer?.closeDrawer()
                            }
                        }
                    }

                    false
                }
    }

    private fun setViewPinnedFeedCountPrimaryDrawerItem(pinnedCountDrawerItem: CustomPanelDrawableItem?) {
        val count = if (owner.pinned.equals(null)
                || owner.pinned?.isEmpty()!!) {
            "0"
        } else {
            owner.pinned
        }

        pinnedCountDrawerItem!!
                .withName(R.string.pinned_count)
                .withIcon(R.drawable.ic_drawer_my_pinned_list)
                .withCaption(count.toString())
                .withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withSelectable(false)
                .withArrowVisible(false)
                .withCaptionBackgroundColor(R.color.colorCustomPanel)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    if (count != "0") {
                        pinnedCountDrawerItem.withSetSelected(false)
                        callPinnedPix(context.applicationContext, owner.realname!!, owner.id, owner.iconfarm, owner.iconserver)
                        ownerProfileDrawer!!.closeDrawer()
                    }

                    false
                }
    }

    private fun setViewSearpleGalleryPhotoCountPrimaryDrawerItem(searpleGalleryPhotoDrawerItem: CustomPanelDrawableItem?) {
        val count = if (owner.galleryCount.equals(null)
                || owner.galleryCount?.isEmpty()!!) {
            "0"
        } else {
            owner.galleryCount
        }

        searpleGalleryPhotoDrawerItem!!
                .withName(R.string.gallery_count)
                .withIcon(R.drawable.ic_drawer_my_gallery)
                .withCaption(count.toString())
                .withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withSelectable(false)
                .withArrowVisible(false)
                .withCaptionBackgroundColor(R.color.colorCustomPanel)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    if (count != "0") {
                        searpleGalleryPhotoDrawerItem.withSetSelected(false)
                        callSearpleGallery(context.applicationContext, owner.realname!!, owner.id, owner.iconfarm, owner.iconserver)
                        ownerProfileDrawer!!.closeDrawer()
                    }

                    false
                }
    }

    private fun setViewPurchasedPhotoCountPrimaryDrawerItem(purchasedCountDrawerItem: CustomPanelDrawableItem?) {
        val count = if (owner.purchased.equals(null)
                || owner.purchased?.isEmpty()!!) {
            "0"
        } else {
            owner.purchased
        }

        purchasedCountDrawerItem!!
                .withName(R.string.purchased_count)
                .withIcon(R.drawable.ic_drawer_shopping_list)
                .withCaption(count.toString())
                .withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withSelectable(false)
                .withArrowVisible(false)
                .withCaptionBackgroundColor(R.color.colorCustomPanel)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    if (count != "0") {
                        purchasedCountDrawerItem.withSetSelected(false)
                        ownerProfileDrawer!!.closeDrawer()
                    }

                    false
                }
    }

    private fun setViewMyPhotoCountPrimaryDrawerItem(myPhotoCountDrawerItem: CustomPanelDrawableItem?) {
        val count = if (owner.pictures.equals(null)
                || owner.pictures?.isEmpty()!!) {
            "0"
        } else {
            owner.pictures
        }

        myPhotoCountDrawerItem!!
                .withName(R.string.my_photo_count)
                .withIcon(R.drawable.ic_drawer_my_upload_list)
                .withCaption(count.toString())
                .withCaptionTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableCount))
                .withTextColor(context.applicationContext.resources
                        .getColor(R.color.colorMaterialDrawableText))
                .withSelectable(false)
                .withArrowVisible(false)
                .withCaptionBackgroundColor(R.color.colorCustomPanel)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    drawerItem ?: false
                    if (count != "0") {
                        myPhotoCountDrawerItem.withSetSelected(false)
                        Caller.callPhotogPhoto(context.applicationContext, owner.realname!!,
                                owner.iconfarm, owner.iconserver, owner.id,
                                FIRST_PAGE, PHOTOG_PHOTO_GENERAL_TYPE)
                        ownerProfileDrawer!!.closeDrawer()
                    }

                    false
                }
    }

    private fun createOwnerDrawer(savedInstanceState: Bundle?, calledFrom: Int, toolbar: Toolbar): Drawer? {
        val drawerBuilder =  when(calledFrom) {
            CALLED_FROM_HOME -> DrawerBuilder()
                    .withActivity(activity)
                    .withToolbar(toolbar)

            CALLED_FROM_VIEW_ALL -> DrawerBuilder()
                    .withActivity(activity)

            else -> DrawerBuilder()
                    .withActivity(activity)
                    .withToolbar(toolbar)
        }

        return drawerBuilder
                .withActionBarDrawerToggle(true)
                .withTranslucentNavigationBar(true)
                .withTranslucentStatusBar(true)
                .withSliderBackgroundColor(context.resources.getColor(R.color.colorSliderOwnerTransparent))
                .withStickyFooterShadow(true)
                .withActionBarDrawerToggleAnimated(true)
                .withHasStableIds(true)
                .withSelectedItem(-1)
                .withAccountHeader(drawerHeader!!) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        locationDrawerItem,
                        descriptionDrawerItem,
                        viewGradePhotoDrawerItem,
                        viewFollowerCountPhotoDrawerItem,
                        viewFollowingCountPhotoDrawerItem,
                        viewPinnedFeedCountDrawerItem,
                        viewSearpleGalleryPhotoCountDrawerItem,
                        viewPurchasedPhotoCountDrawerItem,
                        viewMyPhotoCountDrawerItem,
                        SectionDrawerItem()
                                .withName(R.string.section_more)
                                .withTextColor(context.applicationContext.resources
                                        .getColor(R.color.colorMaterialDrawableText)),
                        SecondaryDrawerItem()
                                .withName(R.string.setting_title)
                                .withTextColor(context.applicationContext.resources
                                        .getColor(R.color.colorMaterialDrawableText))
                                .withIcon(R.drawable.ic_drawer_settings),
                        SecondaryDrawerItem()
                                .withName(R.string.help_title)
                                .withTextColor(context.applicationContext.resources
                                        .getColor(R.color.colorMaterialDrawableText))
                                .withIcon(R.drawable.ic_drawer_help)
                ) // Add the items we want to use with our SlidingDr
                .withOnDrawerNavigationListener {
                    // This method is only called if the Arrow icon is shown.
                    // The hamburger is automatically managed by the MaterialDrawer if the back
                    // arrow is shown. Close the activity
                    activity.finish()
                    // Return true if we have consumed the event
                    true
                }
                .withOnDrawerListener(object : Drawer.OnDrawerListener {
                    override fun onDrawerOpened(drawerView: View) {}

                    override fun onDrawerClosed(drawerView: View) {
                        setOwnerProfileDrawer(owner, calledFrom)
                    }

                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build()
    }

    private fun createOwnerProfileDrawer(activity: BaseActivity, @IdRes rootViewRes: Int,
                                         calledFrom: Int, savedInstanceState: Bundle?): Drawer {
        profile=ProfileDrawerItem()
        setHeader(profile, PROFILE_OWNER_TYPE)
        activity.getDrawable(R.drawable.header_owner)?.let {
            buildOwnerHeader(activity, profile as ProfileDrawerItem, savedInstanceState,
                    it)
        }
        //assert toolbar != null;
        locationDrawerItem=AdvancePrimaryDrawerItem()
        setLocationPrimaryDrawerItem(locationDrawerItem, PROFILE_OWNER_TYPE)
        descriptionDrawerItem=AdvancePrimaryDrawerItem()
        setDescriptionPrimaryDrawerItem(descriptionDrawerItem, PROFILE_OWNER_TYPE)
        viewGradePhotoDrawerItem=CustomPanelDrawableItem()
        setViewGradePrimaryDrawerItem(viewGradePhotoDrawerItem, PROFILE_OWNER_TYPE)
        viewFollowerCountPhotoDrawerItem=CustomPanelDrawableItem()
        setViewFollowerCountPrimaryDrawerItem(viewFollowerCountPhotoDrawerItem, PROFILE_OWNER_TYPE)
        viewFollowingCountPhotoDrawerItem=CustomPanelDrawableItem()
        setViewFollowingCountPrimaryDrawerItem(viewFollowingCountPhotoDrawerItem, PROFILE_OWNER_TYPE)
        viewPinnedFeedCountDrawerItem=CustomPanelDrawableItem()
        setViewPinnedFeedCountPrimaryDrawerItem(viewPinnedFeedCountDrawerItem)
        viewSearpleGalleryPhotoCountDrawerItem= CustomPanelDrawableItem()
        setViewSearpleGalleryPhotoCountPrimaryDrawerItem(viewSearpleGalleryPhotoCountDrawerItem)
        viewPurchasedPhotoCountDrawerItem= CustomPanelDrawableItem()
        setViewPurchasedPhotoCountPrimaryDrawerItem(viewPurchasedPhotoCountDrawerItem)
        viewMyPhotoCountDrawerItem= CustomPanelDrawableItem()
        setViewMyPhotoCountPrimaryDrawerItem(viewMyPhotoCountDrawerItem)

        ownerProfileDrawer=createOwnerDrawer(savedInstanceState, calledFrom, activity.findViewById(R.id.toolbar) as Toolbar)

        savedInstanceState ?: let {
            profile?.let {
                drawerHeader?.activeProfile = profile
            }
        }

        return ownerProfileDrawer as Drawer
    }
}