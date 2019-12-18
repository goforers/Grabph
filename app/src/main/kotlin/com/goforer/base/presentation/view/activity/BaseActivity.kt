/*
 * Copyright (C)  2015 - 2019 Lukoh Nam, goForer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goforer.base.presentation.view.activity

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.goforer.base.presentation.model.event.ActivityStackClearEvent
import com.goforer.base.presentation.utils.CommonUtils.getVersionNumber
import com.goforer.base.presentation.utils.ConnectionUtils
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.cache.IntegerVersionSignature
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import it.sephiroth.android.library.xtooltip.Typefaces
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

abstract class BaseActivity: AppCompatActivity(), HasAndroidInjector {
    private var isResumed = false

    private var width = 0
    private var height = 0

    private var tooltip: Tooltip? = null

    private lateinit var glideRequestManager: RequestManager

    /**
     * Return currently set Activity
     *
     * @return The currently set Activity
     */
    private var currentActivity: Activity? = null

    private var newFragment: Fragment ? = null

    private var activeFragment: Fragment ? = null

    /**
     * Check if the network is available
     *
     * @return return true if the network is available
     */
    internal var isNetworkAvailable = false

    internal var photo: Bitmap? = null

    internal var photoWidth = 0
    internal var photoHeight = 0

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    companion object {
        internal const val NOTO_SANS_KR_BOLD = "fonts/NotoSansKR-Bold-Hestia.otf"
        internal const val NOTO_SANS_KR_MEDIUM = "fonts/NotoSansKR-Medium-Hestia.otf"
        internal const val NOTO_SANS_KR_REGULAR = "fonts/NotoSansKR-Regular-Hestia.otf"

        internal const val FONT_TYPE_BOLD = 0
        internal const val FONT_TYPE_MEDIUM = 1
        internal const val FONT_TYPE_REGULAR = 2

        internal const val PEOPLE_RANK_BEGINNER = 0
        internal const val PEOPLE_RANK_FIRST = 1
        internal const val PEOPLE_RANK_SECOND = 2
        internal const val PEOPLE_RANK_THIRD = 3
        internal const val PEOPLE_RANK_FOURTH = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentActivity = this
        EventBus.getDefault().register(this)
        @GlideModule
        glideRequestManager = Glide.with(this)
        setContentView()
        setActionBar()
        if (ConnectionUtils.isNetworkAvailable(this)) {
            isNetworkAvailable = true
            setViews(savedInstanceState)
        } else {
            isNetworkAvailable = false
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)

        super.onDestroy()

        tooltip?.dismiss()
    }

    public override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

        isResumed = true
    }

    override fun onPause() {
        super.onPause()

        isResumed = false

    }

    override fun androidInjector() = dispatchingAndroidInjector

    /**
     * Return true if this activity is resumed
     *
     * @return true if this activity is resumed
     */
    fun resumed(): Boolean {
        return isResumed
    }

    /**
     * Initialize the ActionBar and set options into it.
     *
     * @see ActionBar
     */
    protected open fun setActionBar() {
        val actionBar = supportActionBar

        actionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_SHOW_TITLE
    }

    /**
     * Set the activity content from a layout resource.  The resource will be
     * inflated, adding all top-level views to the activity.
     *
     *
     * All activity must implement this method to get the resource inflated like below example:
     *
     * Example :
     * @@Override
     * public void setContentView() {
     * setContentView(R.layout.activity_gallery);
     * }
     *
     *
     * @see .setContentView
     */
    protected abstract fun setContentView()

    /**
     * Initialize all views to set into the activity.
     *
     *
     * The activity which has no Fragment must override this method to set all views
     * into the activity.
     *
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [.onSaveInstanceState].  ***Note: Otherwise it is null.***
     */
    protected open fun setViews(savedInstanceState: Bundle?) {}

    /**
     * Add a new fragment that is added a fragment to the activity state and hides an existing fragment,
     * shows a previously hidden fragment or new fragment
     *
     * @param cls the component class that is to be used for BaseActivity
     * @param containerViewId Identifier of the container whose fragment(s) are to be replaced.
     */
    protected fun transactFragment(cls: Class<*>, @IdRes containerViewId: Int): Fragment? {
        val fragmentManager = supportFragmentManager
        val ft = fragmentManager.beginTransaction()
        var allowAddition = true

        newFragment = fragmentManager.findFragmentByTag(cls.name)
        newFragment?.let {
            allowAddition = false
        }

        newFragment = newFragment ?: fragmentManager.fragmentFactory.instantiate(cls.classLoader!!, cls.name)
        activeFragment = activeFragment ?: newFragment
        newFragment?.let {
            if (allowAddition) {
                ft.add(containerViewId, it, cls.name).hide(activeFragment!!).show(newFragment!!).commit()
            } else {
                ft.hide(activeFragment!!).show(newFragment!!).commit()
            }

            activeFragment = newFragment
        }

        return newFragment
    }

    /**
     * Replace an existing fragment that was add to a container or add a new fragment that is added
     * a fragment to the activity state.
     *
     * @param cls the component class that is to be used for BaseActivity
     * @param containerViewId Identifier of the container whose fragment(s) are to be replaced.
     * @param allowAddition Add a fragment to the activity state if allowAddition is true
     *                      Replace an existing fragment that was add to a container if allowAddition is false
     */
    internal fun transactFragment(cls: Class<*>, @IdRes containerViewId: Int, allowAddition: Boolean) {
        if (!isFinishing) {
            val fragmentManager = supportFragmentManager
            val ft = fragmentManager.beginTransaction()
            var fragment = fragmentManager.findFragmentByTag(cls.name)

            fragment = fragment ?: fragmentManager.fragmentFactory.instantiate(cls.classLoader!!, cls.name)
            if (allowAddition) {
                ft.add(containerViewId, fragment, cls.name)
            } else {
                ft.replace(containerViewId, fragment, cls.name)
            }

            if (cls.name.contains("FeedSearchFragment") || cls.name.contains("RecentKeywordFragment")) {
                ft.addToBackStack(null)
            }

            ft.commitAllowingStateLoss()
        }
    }

    /**
     * Sets the typeface and style in which the text should be displayed
     *
     * @param view the AppCompatTextView in which should be set the typeface
     * @param type the Font type
     *
     */
    internal fun setFontTypeface(view: AppCompatTextView, type: Int) {
        when(type) {
            FONT_TYPE_BOLD -> {
                view.typeface = Typeface.createFromAsset(currentActivity?.applicationContext?.assets, NOTO_SANS_KR_BOLD)
            }

            FONT_TYPE_MEDIUM -> {
                view.typeface = Typeface.createFromAsset(currentActivity?.applicationContext?.assets, NOTO_SANS_KR_MEDIUM)
            }

            FONT_TYPE_REGULAR -> {
                view.typeface = Typeface.createFromAsset(currentActivity?.applicationContext?.assets, NOTO_SANS_KR_REGULAR)
            }
        }
    }

    /**
     * Sets the typeface and style in which the text should be displayed
     *
     * @param view the AppCompatTextView in AppCompatButton in which should be set the typeface
     * @param type the Font type
     *
     */
    internal fun setFontTypeface(view: AppCompatButton, type: Int) {
        when(type) {
            FONT_TYPE_BOLD -> {
                view.typeface = Typeface.createFromAsset(currentActivity?.applicationContext?.assets, NOTO_SANS_KR_BOLD)
            }

            FONT_TYPE_MEDIUM -> {
                view.typeface = Typeface.createFromAsset(currentActivity?.applicationContext?.assets, NOTO_SANS_KR_MEDIUM)
            }

            FONT_TYPE_REGULAR -> {
                view.typeface = Typeface.createFromAsset(currentActivity?.applicationContext?.assets, NOTO_SANS_KR_REGULAR)
            }
        }
    }

    /**
     * Sets the fixed with and height to the AppCompatImageView
     *
     * @param height the fixed height
     * @param width the fixed width
     *
     */
    internal fun setFixedImageSize(height: Int, width: Int) {
        this.height = height
        this.width = width
    }

    internal fun setImageDraw(view: AppCompatImageView, layout: ConstraintLayout, path: String, skipCache: Boolean) {
        val set = ConstraintSet()

        val options = if (skipCache) {
            RequestOptions
                .placeholderOf(R.drawable.ic_imgbg)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
        } else {
            RequestOptions
                .placeholderOf(R.drawable.ic_imgbg)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
        }

        if (height == width) {
            glideRequestManager
                .asBitmap()
                .load(path)
                .apply(options)
                .thumbnail(0.5f)
                .into(view)
        } else {
            glideRequestManager
                .asBitmap()
                .load(path)
                .apply(options)
                .thumbnail(0.5f)

                .listener(object: RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                        isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                        dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        val ratio = if (width != 0) {
                            String.format("%d:%d", width, height)
                        } else {
                            String.format("%d:%d", resource?.width!!, resource.height)
                        }

                        photoWidth = resource?.width!!
                        photoHeight = resource.height

                        photo = resource
                        set.clone(layout)
                        set.setDimensionRatio(view.id, ratio)
                        set.applyTo(layout)
                        view.setImageBitmap(resource)

                        return false
                    }
                })
                .submit()
        }
    }

    /**
     * Draw the photo as Bitmap-Format into the given AppCompatImageView
     *
     * @param view the AppCompatImageView which is provided to be drawn the bitmap
     * @param layout the ConstraintLayout to which is applied the constraints
     * @param bitmap the bitmap to be drawn in the AppCompatImageView
     * @param skipCache True to allow the resource to skip the memory cache
     *
     */
    internal fun setImageDraw(view: AppCompatImageView, layout: ConstraintLayout, bitmap: Bitmap, skipCache: Boolean) {
        val set = ConstraintSet()

        val options = if (skipCache) {
            RequestOptions
                .fitCenterTransform()
                .placeholder(R.drawable.ic_imgbg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .signature(IntegerVersionSignature(getVersionNumber()))

        } else {
            RequestOptions
                .fitCenterTransform()
                .placeholder(R.drawable.ic_imgbg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(IntegerVersionSignature(getVersionNumber()))
        }

        glideRequestManager
                .asBitmap()
                .load(bitmap)
                .apply(options)
                .thumbnail(0.5f)
                .listener(object: RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        val ratio = if (width != 0) {
                            String.format("%d:%d", width, height)
                        } else {
                            String.format("%d:%d", resource?.width!!, resource.height)
                        }

                        photoWidth = resource?.width!!
                        photoHeight = resource.height

                        photo = resource
                        set.clone(layout)
                        set.setDimensionRatio(view.id, ratio)
                        set.applyTo(layout)
                        view.setImageBitmap(resource)

                        return false
                    }
                })
                .submit()
    }

    /**
     * Draw the photo as Bitmap-Format into the given AppCompatImageView
     *
     * @param view the AppCompatImageView which is provided to be drawn the bitmap
     * @param url the url of photo to be loaded into the AppCompatImageView
     *
     */
    internal fun setImageDraw(view: AppCompatImageView, url: String) {
        val options = RequestOptions
                .fitCenterTransform()
                .placeholder(R.drawable.ic_imgbg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .signature(IntegerVersionSignature(getVersionNumber()))

        glideRequestManager
                .asBitmap()
                .load(url)
                .apply(options)
                .thumbnail(0.5f)
                .listener(object: RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        view.setImageBitmap(resource)

                        return false
                    }
                })
                .submit()
    }

    /**
     * Show the tool-tip text at the bottom of the given view as an anchor
     *
     * @param anchor the view to use as an anchor.
     * @param toolTip the tool-tip text to be displayed
     * @param width the max width of the tool-tip
     * @param duration a custom duration in milliseconds
     *
     */
    internal fun showToolTip(anchor: View, toolTip: String, width: Int, duration: Long) {
        tooltip?.dismiss()
        tooltip = Tooltip.Builder(this)
                    .anchor(anchor, 0, 0, false)
                    .text(toolTip)
                    .styleId(R.style.ToolTipStyle)
                    .typeface(Typefaces[this, NOTO_SANS_KR_REGULAR])
                    .maxWidth(width)
                    .arrow(true)
                    .floatingAnimation(Tooltip.Animation.DEFAULT)
                    .closePolicy(getClosePolicy())
                    .showDuration(duration)
                    .overlay(true)
                    .create()

        tooltip?.doOnHidden {
                    tooltip = null
                }
                ?.doOnFailure { }
                ?.doOnShown {}
                ?.show(anchor, Tooltip.Gravity.BOTTOM, true)
    }

    /**
     * Return previously set Fragment with given the component class.
     *
     * @param cls The previously set the component class that is to be used for BaseActivity.
     *
     * @return The previously set Fragment
     */
    protected fun getFragment(cls: Class<*>): Fragment? {
        val fragmentManager = supportFragmentManager

        return fragmentManager.findFragmentByTag(cls.name)
    }

    /**
     * Return previously set Fragment with given the tag.
     *
     * @param tag The previously set the component class tag that is to be used for BaseActivity.
     *
     * @return The previously set Fragment
     */
    protected fun getFragment(tag: String): Fragment? {
        val fragmentManager = supportFragmentManager

        return fragmentManager.findFragmentByTag(tag)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getClosePolicy(): ClosePolicy {
        val builder = ClosePolicy.Builder()

        builder.inside(false)
        builder.outside(true)
        builder.consume(false)

        return builder.build()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(event: ActivityStackClearEvent) {
        finish()
    }
}
