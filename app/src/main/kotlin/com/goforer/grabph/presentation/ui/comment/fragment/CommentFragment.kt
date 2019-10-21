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

package com.goforer.grabph.presentation.ui.comment.fragment

import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.view.decoration.RemoverItemDecoration
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.grabph.R
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.ui.comment.CommentActivity
import com.goforer.grabph.presentation.ui.comment.adapter.CommentAdapter
import com.goforer.grabph.presentation.vm.comment.CommentViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.comments.Comment
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_COMMENTS
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.remote.Repository.Companion.BOUND_FROM_BACKEND
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.fragment_photo_comment.*
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class CommentFragment : RecyclerFragment<Comment>() {
    private lateinit var adapter: CommentAdapter

    private lateinit var acvAdapter: AutoClearedValue<CommentAdapter>

    private lateinit var photoId: String

    private val job = Job()

    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    internal val commentActivity: CommentActivity by lazy {
        activity as CommentActivity
    }

    @field:Inject
    lateinit var commentViewModel: CommentViewModel

    @field:Inject
    lateinit var workHandler: CommonWorkHandler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_photo_comment, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoId = commentActivity.photoID

        refresh(true)
        setItemHasFixedSize(true)

        // The cache should be removed whenever App is started again and then
        // the data are fetched from the Back-end.
        // The Cache has to be light-weight.
        launchWork { commentViewModel.removeComments() }
        commentViewModel.loadType = LOAD_COMMENTS
        commentViewModel.boundType = BOUND_FROM_BACKEND
    }

    @ExperimentalCoroutinesApi
    override fun onDestroyView() {
        super.onDestroyView()

        ioScope.cancel()
        job.cancel()
    }

    override fun onDetach() {
        super.onDetach()

        CommentAdapter.getCommenter().clear()
    }

    override fun createLayoutManager(): RecyclerView.LayoutManager {
        super.setOnProcessListener(object : OnProcessListener {
            override fun onScrolledToLast(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolledToLast")
            }

            override fun onScrolling(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolling")
            }

            override fun onScrollIdle(position: Int) {}

            override fun onScrollSetting() {
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolled")
            }

            override fun onError(message: String) {
                showToastMessage(baseActivity, message, Toast.LENGTH_SHORT)
            }
        })

        recycler_view.setHasFixedSize(true)
        recycler_view.setItemViewCacheSize(20)
        recycler_view.isDrawingCacheEnabled = true
        recycler_view.isNestedScrollingEnabled = false
        recycler_view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        recycler_view.isVerticalScrollBarEnabled = false

        return androidx.recyclerview.widget.LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        adapter = CommentAdapter(this, workHandler)
        acvAdapter = AutoClearedValue(this, adapter)
        recycler_view.adapter = acvAdapter.get()

        return adapter
    }

    override fun createItemDecoration(): RecyclerView.ItemDecoration {
        return RemoverItemDecoration(Color.TRANSPARENT)
    }

    override fun createItemTouchHelper(): ItemTouchHelper.Callback? {
        return null
    }

    override fun isItemDecorationVisible(): Boolean {
        return true
    }

    override fun requestData(isNew: Boolean) {
        commentViewModel.setParameters(
            Parameters(
                photoId,
                -1,
                LOAD_COMMENTS,
                BOUND_FROM_BACKEND
            ), -1)
        getComments()

        Timber.i("requestData")
    }

    override fun updateData() {
        getComments()
    }

    override fun reachToLastPage() {
        Timber.i("reachToLastPage")
    }

    override fun onSorted(items: List<Comment>) {
    }

    override fun onFirstVisibleItem(position: Int) {
    }

    override fun onLastVisibleItem(position: Int) {

    }

    private fun getComments() = commentViewModel.comments.observe(this, Observer { resource ->
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                @Suppress("UNCHECKED_CAST")
                val comments = resource.getData() as PagedList<Comment>
                if (comments.size > 0) {
                    acvAdapter.get()?.submitList(comments)
                } else {
                    resource.getMessage() ?: requestData(false)
                    stopMoreLoading(STOP_REFRESHING_TIMEOUT)
                    stopComment(resource)
                }
            }

            Status.LOADING -> {
            }

            Status.ERROR -> {
                stopMoreLoading(STOP_REFRESHING_TIMEOUT)
                stopComment(resource)
            }

            else -> {
                stopMoreLoading(STOP_REFRESHING_TIMEOUT)
                stopComment(resource)
            }
        }
    })

    @SuppressLint("SetTextI18n")
    private fun stopComment(resource: Resource) = when(resource.errorCode) {
        in 400..499 -> {
            Snackbar.make(commentActivity.constraint_photo_comment_layout, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
        }

        in 500..599 -> {
            Snackbar.make(commentActivity.constraint_photo_comment_layout, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
        }

        else -> {}
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the ioScope.
     *              lambda the some work will do
     */
    private inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return ioScope.launch {
            block()
        }
    }
}


