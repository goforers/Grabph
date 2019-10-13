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

@file:Suppress("NAME_SHADOWING", "DEPRECATION")

package com.goforer.grabph.presentation.ui.photog.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import androidx.paging.PagedListAdapter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.paging.PagedList
import com.goforer.base.presentation.model.BaseModel
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.NOTO_SANS_KR_MEDIUM
import com.goforer.base.presentation.view.customs.imageview.ThreeTwoImageView
import com.goforer.base.presentation.view.helper.ItemTouchHelperListener
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_PHOTOG_PHOTO
import com.goforer.grabph.presentation.caller.Caller.SELECTED_PHOTOG_PHOTO_VIEW
import com.goforer.grabph.presentation.caller.Caller.SELECTED_PHOTO_INFO_ITEM_POSITION
import com.goforer.grabph.presentation.common.menu.MenuHandler
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.presentation.ui.photog.fragment.PhotogPhotoFragment
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photo
import com.goforer.grabph.repository.interactor.remote.feed.photo.PhotoRepository
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_photog_photo.*
import kotlinx.android.synthetic.main.list_photo_item.*
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PhotogPhotoAdapter(private val fragment: PhotogPhotoFragment, private val waterMarkHandler: WatermarkHandler,
                         private val workHandler: CommonWorkHandler): PagedListAdapter<Photo, PhotogPhotoAdapter.PhotogPhotoViewHolder>(DIFF_CALLBACK),
                                                                      ItemTouchHelperListener {
    companion object {
        private const val TRANSITION_NAME_FOR_IMAGE = "Image "
        private const val TRANSITION_NAME_FOR_TITLE = "Title "

        private const val STOP_LOADING_TIME_OUT = 50L

        private const val LIKE_NORMAL = 0
        private const val LIKE_GOOD = 1
        private const val LIKE_BAD = 2

        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldPhoto: Photo, newPhoto: Photo): Boolean =
                    oldPhoto.id == newPhoto.id

            override fun areContentsTheSame(oldPhoto: Photo, newPhoto: Photo): Boolean =
                    oldPhoto == newPhoto

            override fun getChangePayload(oldPhoto: Photo, newPhoto: Photo): Any? {
                return if (sameExceptTitle(oldPhoto, newPhoto)) {
                    PAYLOAD_TITLE
                } else {
                    null
                }
            }
        }

        private fun sameExceptTitle(oldPhoto: Photo, newPhoto: Photo): Boolean {
            return oldPhoto.copy(title = newPhoto.title) == newPhoto
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotogPhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.list_photo_item, parent, false)
        return PhotogPhotoViewHolder(view, fragment, waterMarkHandler, workHandler)
    }

    override fun onBindViewHolder(holder: PhotogPhotoViewHolder, position: Int) {
        val item: Photo? = getItem(position)

        if(item != null) {
            holder.bindItemHolder(holder, item, position)
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Timber.i("onItemMove : From  %d - To  %d", fromPosition, toPosition)

        if (fromPosition == -1 || toPosition == -1) {
            return false
        }

        swap(getItem(fromPosition)!!, getItem(toPosition)!!)

        return true
    }

    override fun onCurrentListChanged(previousList: PagedList<Photo>?, currentList: PagedList<Photo>?) {
        if (fragment.swipe_layout.isRefreshing) {
            runBlocking {
                fragment.stopMoreLoading(STOP_LOADING_TIME_OUT)
            }
        }

        withDelay(STOP_LOADING_TIME_OUT) {
            previousList?.let {
                fragment.recycler_view.scrollToPosition(previousList.size - PhotoRepository.PREFETCH_DISTANCE)
            }
        }
    }

    override fun onItemDismiss(position: Int) {
        val type = fragment.photogPhotoActivity.type

        when(type) {
            Caller.PHOTOG_PHOTO_GENERAL_TYPE -> {
                delete(currentList!![position]?.id!!, type)
            }

            Caller.PHOTOG_PHOTO_POPULAR_TYPE -> {
                delete(currentList!![position]?.id!!, type)
            }

            Caller.PHOTOG_PHOTO_FAVORITE_TYPE -> {
                delete(currentList!![position]?.id!!, type)
            }
        }
    }

    override fun onItemDrag(actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            fragment.swipe_layout.isRefreshing = false
            fragment.swipe_layout.isEnabled = false
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            fragment.swipe_layout.isEnabled = true
        }
    }

    private fun swap(fromPhoto: Photo, toPhoto: Photo) {
        var fromPhoto = fromPhoto
        var toPhoto = toPhoto
        val photo = BaseModel.deepCopy(fromPhoto, Photo::class.java)

        fromPhoto = passByValue(fromPhoto, toPhoto)
        assert(photo != null)
        toPhoto = passByValue(toPhoto, photo!!)
        move(fromPhoto, toPhoto, fragment.photogPhotoActivity.type)
    }

    private fun passByValue(copyPhoto: Photo, sourcePhoto: Photo): Photo {
        copyPhoto.id = sourcePhoto.id
        copyPhoto.secret = sourcePhoto.secret
        copyPhoto.server = sourcePhoto.server
        copyPhoto.farm = sourcePhoto.farm
        copyPhoto.title = sourcePhoto.title
        copyPhoto.ispublic = sourcePhoto.ispublic
        copyPhoto.isfriend = sourcePhoto.isfriend
        copyPhoto.isfamily = sourcePhoto.isfamily

        return copyPhoto
    }

    private fun move(fromPhoto: Photo, toPhoto: Photo, type: Int) {
        fragment.launchWork {
            when(type) {
                Caller.PHOTOG_PHOTO_GENERAL_TYPE -> {
                    fragment.photoViewModel.update(fromPhoto)
                    fragment.photoViewModel.update(toPhoto)
                }

                Caller.PHOTOG_PHOTO_POPULAR_TYPE -> {
                    fragment.popularPhotoViewModel.update(fromPhoto)
                    fragment.popularPhotoViewModel.update(toPhoto)
                }

                Caller.PHOTOG_PHOTO_FAVORITE_TYPE -> {
                    fragment.favoritePhotoViewModel.update(fromPhoto)
                    fragment.favoritePhotoViewModel.update(toPhoto)
                }
            }
        }
    }

    private fun delete(id: String, type: Int) {
        fragment.launchWork {
            when(type) {
                Caller.PHOTOG_PHOTO_GENERAL_TYPE -> {
                    fragment.photoViewModel.deleteByPhotoId(id)
                }

                Caller.PHOTOG_PHOTO_POPULAR_TYPE -> {
                    fragment.popularPhotoViewModel.deleteByPhotoId(id)
                }

                Caller.PHOTOG_PHOTO_FAVORITE_TYPE -> {
                    fragment.favoritePhotoViewModel.deleteByPhotoId(id)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    class PhotogPhotoViewHolder(override val containerView: View, private val fragment: PhotogPhotoFragment,
                                private val waterMarkHandler: WatermarkHandler,
                                private val workHandler: CommonWorkHandler): BaseViewHolder<Photo>(containerView), LayoutContainer {
        // In case of viewing an item in the list, the photo couldn't be downloaded(Concept get changed...)
        /*
        private val currentDateAndTime: String
            get() {
                @SuppressLint("SimpleDateFormat")
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                return format.format(Date())
            }
        */

        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Photo, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            iv_photo_item_content.requestLayout()
            tv_photo_item_title.requestLayout()
            ib_viewer.requestLayout()
            ib_share.requestLayout()
            iv_photo_item_content.transitionName = TRANSITION_NAME_FOR_IMAGE + position
            tv_photo_item_title.transitionName = TRANSITION_NAME_FOR_TITLE + position

            val path = fragment.photogPhotoActivity.searperPhotoPath

            path?.let {
                if (path.contains("/0/")) {
                    iv_searper_pic.setImageDrawable(fragment.context.applicationContext
                            .getDrawable(R.drawable.ic_default_profile))
                } else {
                    fragment.photogPhotoActivity.setImageDraw(iv_searper_pic, path)
                    iv_searper_pic.setOnClickListener {
                        fragment.photogPhotoActivity.slidingDrawer.searperProfileDrawerForFeedViewDrawer?.openDrawer()
                    }
                }
            }

            path ?: iv_searper_pic.setImageDrawable(fragment.context.applicationContext
                    .getDrawable(R.drawable.ic_default_profile))
            tv_searper_name.text = fragment.photogPhotoActivity.searperName
            tv_searper_posted_date.text = fragment.context.applicationContext.getString(R.string.photo_safe)

            val url = ("https://farm" + item.farm + ".staticflickr.com/" + item.server
                    + "/" + item.id + "_" + item.secret + ".jpg")

            fragment.photogPhotoActivity.setFixedImageSize(0, 0)
            fragment.photogPhotoActivity.setImageDraw(iv_photo_item_content, pinnedConstraintLayoutContainer, url, false)
            photo_item_holder.visibility = View.VISIBLE
            card_holder.visibility = View.VISIBLE

            iv_photo_item_content.setOnClickListener {
                Caller.callPhotoInfo(fragment.photogPhotoActivity,
                        iv_photo_item_content, tv_photo_item_title,
                        item.id, item.owner!!, holder.adapterPosition,
                        SELECTED_PHOTO_INFO_ITEM_POSITION)
            }

            if (item.title == "") {
                tv_photo_item_title.text = fragment.context.applicationContext.getString(R.string.no_title)
            } else {
                tv_photo_item_title.text = item.title
            }

            val drawable: Drawable = when (item.like) {
                LIKE_NORMAL -> {
                    ContextCompat.getDrawable(fragment.context.applicationContext, R.drawable.ic_fab_normal)!!
                }

                LIKE_GOOD -> {
                    ContextCompat.getDrawable(fragment.context.applicationContext, R.drawable.ic_fab_good)!!
                }

                LIKE_BAD -> {
                    ContextCompat.getDrawable(fragment.context.applicationContext, R.drawable.ic_fab_bad)!!
                }

                else -> {
                    ContextCompat.getDrawable(fragment.context.applicationContext, R.drawable.ic_fab_normal)!!
                }
            }

            ib_like.setImageDrawable(drawable)
            ib_like.backgroundTintList = when (item.like) {
                LIKE_NORMAL -> {
                    ColorStateList.valueOf(Color.WHITE)
                }

                LIKE_GOOD -> {
                    ColorStateList.valueOf(Color.RED)
                }

                LIKE_BAD -> {
                    ColorStateList.valueOf(Color.BLUE)
                }
                else -> {
                    ColorStateList.valueOf(Color.WHITE)
                }
            }

            ib_like.setOnClickListener { view -> doLike(view as AppCompatImageButton, item) }

            ib_viewer.setOnClickListener {
                Caller.callViewer(fragment.photogPhotoActivity, iv_photo_item_content, position,
                        CALLED_FROM_PHOTOG_PHOTO, (iv_photo_item_content.drawable as BitmapDrawable).bitmap,
                        url, SELECTED_PHOTOG_PHOTO_VIEW)
            }

            ib_share.setOnClickListener { doShare(ib_share, iv_photo_item_content, item) }
        }

        private fun callShareToFacebook(drawable: BitmapDrawable, caption: String) {
            workHandler.shareToFacebook(drawable.bitmap, fragment)
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }

        // In case of viewing an item in the list, the photo couldn't be downloaded(Concept get changed...)
        /*
        @SuppressLint("DefaultLocale")
        private fun doDownload(id: String, publishedDate: String, photoView: ThreeTwoImageView) {
            val action = SavePhotogAction()

            val dateTime: Long
            try {
                dateTime = convertDateToLong(publishedDate)
            } catch (e: ParseException) {
                e.printStackTrace()
                Timber.d(e)

                return
            }

            val photoURL = if (mGrapher.iconserver == "0") {
                "None"
            } else {
                ("http://farm" + mGrapher.iconfarm + ".staticflickr.com/"
                        + mGrapher.iconserver
                        + "/buddyicons/" + mGrapher.id + ".jpg")
            }

            action.fileName = String.format("%s.jpg", id + "_" + dateTime.toString())
            action.bitmapDrawable = photoView.drawable as BitmapDrawable
            action.userInfo = mGrapher
            action.grapherPhotoUrl = photoURL
            EventBus.getDefault().post(action)
        }
        */

        private fun doLike(view: View, photo: Photo) {
            val wrapper = ContextThemeWrapper(fragment.photogPhotoActivity, R.style.PopupMenu)
            val popup = PopupMenu(wrapper, view, Gravity.CENTER)

            popup.menuInflater.inflate(R.menu.menu_like_popup, popup.menu)
            MenuHandler().applyFontToMenuItem(popup,
                    Typeface.createFromAsset(fragment.photogPhotoActivity.applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                    fragment.photogPhotoActivity.resources.getColor(R.color.colorHomeQuestFavoriteKeyword, fragment.photogPhotoActivity.theme))
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_like_good -> {
                        (view as AppCompatImageButton).setImageResource(R.drawable.ic_fab_good)
                        view.backgroundTintList = ColorStateList.valueOf(Color.RED)
                        photo.like = 1
                    }
                    R.id.menu_like_normal -> {
                        (view as AppCompatImageButton).setImageResource(R.drawable.ic_fab_normal)
                        view.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                        photo.like = 0
                    }
                    R.id.menu_like_bad -> {
                        (view as AppCompatImageButton).setImageResource(R.drawable.ic_fab_bad)
                        view.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
                        photo.like = 2
                    }
                    else -> {
                    }
                }

                true
            }

            val menuHelper: Any
            val argTypes: Array<Class<*>>

            try {
                val fMenuHelper= PopupMenu::class.java.getDeclaredField("mPopup")
                fMenuHelper.isAccessible = true
                menuHelper=fMenuHelper.get(popup)
                argTypes=arrayOf(Boolean::class.javaPrimitiveType!!)
                menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes).invoke(menuHelper, true)
            } catch (e: Exception) {
                // Possible exceptions are NoSuchMethodError and NoSuchFieldError
                //
                // In either case, an exception indicates something is wrong with the reflection code, or the
                // structure of the PopupMenu class or its dependencies has changed.
                //
                // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
                // but in the case that they do, we simply can't force icons to display, so log the error and
                // show the menu normally.
            }

            popup.show()
        }

        private fun doShare(view: View, photoView: ThreeTwoImageView, photo: Photo) {
            val popup = PopupMenu(fragment.photogPhotoActivity, view, Gravity.CENTER)

            popup.menuInflater.inflate(R.menu.menu_share_popup, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_share_facebook -> callShareToFacebook(
                            photoView.drawable as BitmapDrawable,
                            fragment.photogPhotoActivity.applicationContext.getString(
                                    R.string.phrase_title) + "\n\n"
                                    + item.title + "\n\n"
                                    + fragment.context.applicationContext.getString(
                                    R.string.phrase_description) + "\n\n")
                    R.id.menu_share_ect -> waterMarkHandler.putWatermark(fragment.context.applicationContext, workHandler,
                            (photoView.drawable as BitmapDrawable).bitmap, photo.title, "")
                    else -> {
                    }
                }

                true
            }

            popup.show()
        }
    }
}