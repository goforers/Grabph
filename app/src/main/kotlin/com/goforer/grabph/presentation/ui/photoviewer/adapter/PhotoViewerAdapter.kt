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

@file:Suppress("DEPRECATION")

package com.goforer.grabph.presentation.ui.photoviewer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.SparseArray
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.goforer.base.presentation.utils.CommonUtils.getImagePath
import com.goforer.base.presentation.utils.CommonUtils.getVersionNumber
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.customs.imageview.PhotoImageView
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_SEARPLE_GALLERY_PHOTO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_FEED_ITEM
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.common.utils.cache.IntegerVersionSignature
import com.goforer.grabph.presentation.ui.photoviewer.PhotoViewerActivity
import com.goforer.grabph.presentation.ui.photoviewer.fragment.PhotoViewerFragment
import com.goforer.grabph.presentation.ui.photoviewer.sharedelementcallback.SearpleGalleryPhotoCallback
import com.goforer.grabph.presentation.ui.photoviewer.sharedelementcallback.PhotoViewerCallback
import com.goforer.grabph.repository.model.cache.data.entity.savedphoto.LocalSavedPhoto
import kotlinx.android.synthetic.main.view_searple_gallery_photo.view.*
import java.io.ByteArrayOutputStream
import java.io.File

class PhotoViewerAdapter: PagerAdapter {
    private var context: Context

    private var fragment: PhotoViewerFragment

    private val pageArray: SparseArray<View> by lazy {
        SparseArray<View>()
    }

    private lateinit var rootView: View

    private lateinit var photoList: List<String>

    private var type: Int = 0

    private var url = ""

    internal var position: Int = -1

    internal lateinit var bitmap: Bitmap

    constructor(context: Context, fragment: PhotoViewerFragment, photoList: List<String>, type: Int) {
        this.context = context
        this.fragment = fragment
        this.photoList = photoList
        this.type = type
    }

    constructor(context: Context, fragment: PhotoViewerFragment, url: String, type: Int) {
        this.context = context
        this.fragment = fragment
        this.url = url
        this.type = type
    }

    override fun getCount(): Int {
        return if (type == CALLED_FROM_FEED_ITEM) {
            1
        } else {
            photoList.size
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        rootView = LayoutInflater.from(container.context)
                                    .inflate(R.layout.view_searple_gallery_photo, container, false)

        if (type == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
            bitmap = BitmapFactory.decodeFile(getImagePath(context) + File.separator + photoList[position])
            loadPhoto(this, rootView, bitmap, position)
        } else {
            loadPhoto(this, rootView, url, position)
        }


        pageArray.put(position, rootView)
        container.addView(rootView)

        return rootView
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {}

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
        pageArray.remove(position)
    }

    private fun loadPhoto(adapter: PhotoViewerAdapter, view: View, url: String, position: Int) {
        val activity = fragment.photoViewerActivity

        activity.setImageDraw(view.pv_photo, url)
        view.pv_photo.setOnClickListener {
            activity.finishAfterTransition()
        }

        withDelay(50L) {
            setViewBinding(activity, view.pv_photo, type)
        }
    }

    private fun loadPhoto(adapter: PhotoViewerAdapter, view: View, bitmap: Bitmap, position: Int) {
        val stream = ByteArrayOutputStream()
        val options = RequestOptions
                .fitCenterTransform()
                .placeholder(R.color.colorDefaultDrawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .signature(IntegerVersionSignature(getVersionNumber()))

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        with (view) {
            Glide.with(fragment)
                    .asBitmap()
                    .apply(options)
                    .load(stream.toByteArray())
                    .thumbnail(0.5f)
                    .listener(object: RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                                  isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                     dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            val activity = fragment.photoViewerActivity

                            if (adapter.position == position) {
                                setViewBinding(activity, pv_photo, type)
                            }

                            pv_photo.setImageBitmap(resource)
                            pv_photo.setOnClickListener {
                                activity.finishAfterTransition()
                            }

                            return false
                        }
                    })
                    .submit()
        }
    }

    private fun setViewBinding(activity: PhotoViewerActivity, photoView: PhotoImageView, type: Int) {
        if (type == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
            photoView.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
            activity.sharedElementCallback = SearpleGalleryPhotoCallback()
            (activity.sharedElementCallback as SearpleGalleryPhotoCallback).setViewBinding(photoView)

        } else {
            photoView.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + 0
            activity.sharedElementCallback = PhotoViewerCallback()
            (activity.sharedElementCallback as PhotoViewerCallback).setViewBinding(photoView)
        }

        activity.setEnterSharedElementCallback(activity.sharedElementCallback)
        activity.supportStartPostponedEnterTransition()
    }

    @SuppressLint("CheckResult")
    private fun displayUserInfo(view: View, photo: LocalSavedPhoto) {
        val options = RequestOptions
                .fitCenterTransform()
                .placeholder(R.color.colorDefaultDrawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(IntegerVersionSignature(getVersionNumber()))

        with (view) {
            iv_user_pic ?: return
            tv_name ?: return
            userbar_container.visibility = View.VISIBLE
            if (photo.photourl == "None" || photo.photourl.contains("/0/")) {
                iv_user_pic.setImageDrawable(view.context.applicationContext
                        .getDrawable(R.drawable.ic_default_profile))
            } else {
                fragment.photoViewerActivity.setImageDraw(iv_user_pic, photo.photourl)
            }

            iv_user_pic.setOnClickListener{
                fragment.photoViewerActivity.user.let {
                    fragment.photoViewerActivity.slidingDrawer.searperProfileDrawerForDownloadViewDrawer?.openDrawer()
                }
            }

            if (photo.realname == "" || photo.realname == "No Real Name") {
                tv_name.text = photo.username
            } else {
                tv_name.text = photo.realname
            }
        }
    }

    internal fun setPagerCurrentPosition(position: Int) {
        this.position = position
    }

    internal fun launchUserInfo(adapter: PhotoViewerAdapter, photo: LocalSavedPhoto, position: Int) {
        showUserInfo(adapter, photo, position)
    }

    private fun showUserInfo(adapter: PhotoViewerAdapter, photo: LocalSavedPhoto, position: Int) {
        val view = adapter.pageArray.get(position)

        view?.let {
            displayUserInfo(view, photo)
        }
    }
}
