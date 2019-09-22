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

@file:Suppress("DEPRECATION", "NAME_SHADOWING")

package com.goforer.grabph.presentation.ui.photoviewer.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goforer.base.presentation.view.customs.viewpager.SwipeCoolViewPager
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_SEARPLE_GALLERY_PHOTO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_FEED_ITEM
import com.goforer.grabph.presentation.common.effect.transformer.CubeOutTransformer
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.photoviewer.PhotoViewerActivity
import com.goforer.grabph.presentation.ui.photoviewer.adapter.PhotoViewerAdapter
import com.huanhailiuxin.coolviewpager.CoolViewPager
import kotlinx.android.synthetic.main.fragment_viewer_photo.view.*
import kotlinx.android.synthetic.main.view_searple_gallery_photo.view.*

class PhotoViewerFragment : BaseFragment() {
    internal val photoViewerActivity: PhotoViewerActivity by lazy {
        activity as PhotoViewerActivity
    }

    internal lateinit var adapter: PhotoViewerAdapter

    private var calledFrom: Int = 0

    private var currentPosition: Int = 0

    private lateinit var imageList: List<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_viewer_photo, container, false))

        setHasOptionsMenu(true)

        return acvView.get()?.rootView
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calledFrom = photoViewerActivity.calledFrom
        currentPosition = photoViewerActivity.getCurrentImagePosition()
        if (calledFrom == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
            imageList = photoViewerActivity.photoPathList
            view.tv_number.visibility = View.VISIBLE
        } else {
            view.tv_number.visibility = View.GONE
        }

        setUpViewPager(this, view, currentPosition)
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        if (calledFrom == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
            view?.tv_number?.text = ((currentPosition + 1).toString() + "/" + imageList.size)
        }
    }

    private fun setUpViewPager(fragment: PhotoViewerFragment, view: View, position: Int) = with (view) {
        if (calledFrom == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
            adapter = PhotoViewerAdapter(fragment.context.applicationContext, fragment, imageList,
                    CALLED_FROM_SEARPLE_GALLERY_PHOTO)
            pager_browse.adapter = adapter
            photoViewerActivity.getData(currentPosition)
        } else {
            adapter = PhotoViewerAdapter(fragment.context.applicationContext, fragment, photoViewerActivity.photoUrl, CALLED_FROM_FEED_ITEM)
            pager_browse.adapter = adapter
        }

        adapter.setPagerCurrentPosition(position)

        pager_browse.isPageScrolled = true
        pager_browse.setCurrentItem(position, false)
        pager_browse.setViewPagerType(SwipeCoolViewPager.PAGER_PHOTO_VIEWER_TYPE)
        pager_browse.pageMargin = resources.getDimensionPixelSize(R.dimen.padding_1)
        pager_browse.isInfiniteLoop = false
        pager_browse.setScrollMode(CoolViewPager.ScrollMode.HORIZONTAL)
        pager_browse.setDrawEdgeEffect(true)
        pager_browse.setPageTransformer(true, CubeOutTransformer())
        pager_browse.setEdgeEffectColor(resources.getColor(R.color.colorPrimary, null))
        pager_browse.setOnSwipeOutListener(object : SwipeCoolViewPager.OnSwipeOutListener {
            override fun onSwipeOutAtStart() {
                if (calledFrom == CALLED_FROM_FEED_ITEM) {
                    baseActivity.onBackPressed()
                }
            }

            override fun onSwipeOutAtEnd() {}

            override fun onSwipeLeft(x: Float, y: Float) {}

            override fun onSwipeRight(x: Float, y: Float) {}

            override fun onSwipeDown(x: Float, y: Float) {
                photoViewerActivity.finishAfterTransition()
            }

            override fun onSwipeUp(x: Float, y: Float) {}
        })

        pager_browse.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int,
                                        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                if (pager_browse.childCount > 0) {
                    pager_browse.removeOnLayoutChangeListener(this)
                    if (calledFrom == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
                        photoViewerActivity.supportStartPostponedEnterTransition()
                    }
                }
            }
        })

        pager_browse.addOnPageChangeListener(object : CoolViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                pager_browse.isPageScrolled = true
            }

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                if (calledFrom == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
                    adapter.setPagerCurrentPosition(position)
                    userbar_container.visibility = View.GONE
                    photoViewerActivity.setCurrentImagePosition(position)
                    photoViewerActivity.getData(position)
                    tv_number.text = (position + 1).toString() + "/" + imageList.size
                }

                pager_browse.setCurrentItem(position, false)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        pager_browse.pageMargin = resources.getDimensionPixelSize(R.dimen.padding_1)
    }
}