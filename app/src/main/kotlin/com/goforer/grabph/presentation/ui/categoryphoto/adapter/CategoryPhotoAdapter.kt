package com.goforer.grabph.presentation.ui.categoryphoto.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.view.helper.ItemTouchHelperListener
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.ui.categoryphoto.CategoryPhotoActivity
import com.goforer.grabph.repository.model.cache.data.entity.category.CPhoto
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_category_photo.*
import kotlinx.android.synthetic.main.grid_category_photo_item.*

class CategoryPhotoAdapter(private val activity: CategoryPhotoActivity): PagedListAdapter<CPhoto, CategoryPhotoAdapter.CategoryPhotoViewHolder>(DIFF_CALLBACK),
                                                                         ItemTouchHelperListener {
    companion object {
        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<CPhoto>() {
            override fun areItemsTheSame(oldCPhoto: CPhoto, newCPhoto: CPhoto): Boolean =
                    oldCPhoto.id ==  newCPhoto.id

            override fun areContentsTheSame(oldCPhoto: CPhoto, newCPhoto: CPhoto): Boolean =
                    oldCPhoto ==  newCPhoto

            override fun getChangePayload(oldCPhoto: CPhoto, newCPhoto: CPhoto): Any? {
                return if (sameExceptTitle(oldCPhoto, newCPhoto)) {
                    PAYLOAD_TITLE
                } else {
                    null
                }
            }
        }

        private fun sameExceptTitle(oldCPhoto: CPhoto, newCPhoto: CPhoto): Boolean {
            return oldCPhoto.copy(title = newCPhoto.title) == newCPhoto
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryPhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.grid_category_photo_item, parent, false)

        return CategoryPhotoViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: CategoryPhotoViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<CPhoto>?, currentList: PagedList<CPhoto>?) {
        if (activity.swipe_layout.isRefreshing) {
            activity.swipe_layout?.let {
                if (it.isRefreshing) {
                    it.isRefreshing = false
                }
            }
        }
    }

    override fun onItemDrag(actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            activity.swipe_layout.isRefreshing = false
            activity.swipe_layout.isEnabled = false
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            activity.swipe_layout.isEnabled = true
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return false
    }

    override fun onItemDismiss(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Suppress("NAME_SHADOWING", "DEPRECATION")
    class CategoryPhotoViewHolder(override val containerView: View, private val activity: CategoryPhotoActivity): BaseViewHolder<CPhoto>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: CPhoto, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            iv_category_photo_item_content.requestLayout()
            tv_category_photo_item_title.requestLayout()
            iv_category_photo_item_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
            tv_category_photo_item_title.transitionName = TransitionObject.TRANSITION_NAME_FOR_TITLE + position

            activity.setFixedImageSize(0, 0)
            activity.setImageDraw(iv_category_photo_item_content, categoryPhotoConstraintLayoutContainer,
                    item.path!!, false)
            if (item.title == "" || item.title == " ") {
                item.title = containerView.resources.getString(R.string.no_title)
            }

            tv_category_photo_item_title.text = item.title
            iv_category_photo_item_content.setOnClickListener {
                CommonUtils.showToastMessage(activity, activity.getString(R.string.phrase_view_category_implement), Toast.LENGTH_SHORT)
            }

            /*
            if (activity.swipe_layout.isRefreshing) {
                activity.swipe_layout?.let {
                    if (it.isRefreshing) {
                        it.isRefreshing = false
                    }
                }
            }
            */
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }
    }
}