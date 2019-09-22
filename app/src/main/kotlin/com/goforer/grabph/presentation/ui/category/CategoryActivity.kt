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

package com.goforer.grabph.presentation.ui.category

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.Transition
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import androidx.core.app.SharedElementCallback
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.ui.category.fragment.CategoryFragment
import com.goforer.grabph.presentation.vm.category.CategoryViewModel
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.category.CategoryRepository
import com.goforer.grabph.repository.interactor.remote.Repository
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_category.toolbar
import kotlinx.coroutines.*
import javax.inject.Inject

class CategoryActivity: BaseActivity()  {
    private var resultCode: Int = 0

    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    @field:Inject
    internal lateinit var categoryViewModel: CategoryViewModel

    private val sharedExitListener = object : TransitionCallback() {
        override fun onTransitionEnd(transition: Transition) {
            removeCallback()
        }

        override fun onTransitionCancel(transition: Transition) {
            removeCallback()
        }

        private fun removeCallback() {
            window.sharedElementExitTransition.removeListener(this)
            setExitSharedElementCallback(null as SharedElementCallback?)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
            networkStatusVisible(true)
        } else {
            networkStatusVisible(false)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()

        ioScope.cancel()
        job.cancel()
    }

    @SuppressLint("RestrictedApi")
    override fun setActionBar() {
        setSupportActionBar(this@CategoryActivity.toolbar)

        val actionBar = supportActionBar

        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.elevation = 0f
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = getString(R.string.phrase_category)
        }

        this@CategoryActivity.toolbar.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@CategoryActivity.toolbar.hideOverflowMenu()

    }

    override fun setViews(savedInstanceState: Bundle?) {
        removeCache(categoryViewModel.interactor)
        this@CategoryActivity.appbar_layout_category.outlineProvider = null
        transactFragment(CategoryFragment::class.java, R.id.disconnect_container_category, false)
    }

    override fun setContentView() {
        setContentView(R.layout.activity_category)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        this.resultCode = resultCode

        // Listener to reset shared element exit transition callbacks.
        window.sharedElementExitTransition.addListener(sharedExitListener)
        supportPostponeEnterTransition()
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@CategoryActivity.iv_disconnect_category.visibility = View.GONE
        this@CategoryActivity.tv_notice1_category.visibility = View.GONE
        this@CategoryActivity.tv_notice2_category.visibility = View.GONE
    } else {
        this@CategoryActivity.iv_disconnect_category.visibility = View.VISIBLE
        this@CategoryActivity.tv_notice1_category.visibility = View.VISIBLE
        this@CategoryActivity.tv_notice2_category.visibility = View.VISIBLE
    }

    private fun removeCache(repository: Repository) = launchIOWork {
        (repository as CategoryRepository).deleteCategory()
    }

    internal fun showNetworkError(resource: Resource) {
        when(resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(this@CategoryActivity.bottom_navigation_view, getString(R.string.phrase_client_wrong_request), Snackbar.LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(this@CategoryActivity.bottom_navigation_view, getString(R.string.phrase_server_wrong_response), Snackbar.LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(this@CategoryActivity.bottom_navigation_view, resource.getMessage().toString(), Snackbar.LENGTH_LONG).show()
            }
        }
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
    private inline fun launchIOWork(crossinline block: suspend () -> Unit): Job {
        return ioScope.launch {
            block()
        }
    }
}