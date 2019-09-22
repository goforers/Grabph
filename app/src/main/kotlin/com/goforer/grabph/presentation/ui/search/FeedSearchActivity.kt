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

@file:Suppress("UNREACHABLE_CODE", "CAST_NEVER_SUCCEEDS", "DEPRECATION")

package com.goforer.grabph.presentation.ui.search

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SEARCH_COLLAPSED
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SEARCH_QUERY
import com.goforer.grabph.presentation.caller.Caller.SELECTED_SEARCH_ITEM_POSITION
import com.goforer.grabph.presentation.event.action.FeedUpdateAction
import com.goforer.grabph.presentation.event.action.SearchKeywordSubmitAction
import com.goforer.grabph.presentation.ui.search.fragment.FeedSearchFragment
import com.goforer.grabph.presentation.ui.search.fragment.RecentKeywordFragment
import kotlinx.android.synthetic.main.activity_feed_search.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FeedSearchActivity: BaseActivity() {
    private lateinit var searchView: SearchView

    internal lateinit var queryKeyword: String

    private var isCollapsed: Boolean = false

    private val job = Job()

    private val defaultScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        private const val DELAY_TIMER_INTERVAL = 400
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            networkStatusVisible(true)
            savedInstanceState ?: getData()
            savedInstanceState?.let {
                queryKeyword = savedInstanceState.getString(EXTRA_SEARCH_QUERY, "")
                isCollapsed = savedInstanceState.getBoolean(EXTRA_SEARCH_COLLAPSED)
            }
        } else {
            networkStatusVisible(false)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()

        defaultScope.cancel()
        job.cancel()
    }

    override fun setContentView() {
        setContentView(R.layout.activity_feed_search)
    }

    @SuppressLint("RestrictedApi")
    override fun setActionBar() {
        setSupportActionBar(this@FeedSearchActivity.toolbar)

        val actionBar = supportActionBar

        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.elevation = 0f
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        toolbar.setNavigationOnClickListener {
            finishAfterTransition()
        }

        toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) =
            transactFragment(RecentKeywordFragment::class.java, R.id.disconnect_container_feed_search, false)


    override fun onNewIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)

            queryKeyword = query
            searchView.setQuery(query, true)
        }

        super.onNewIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(EXTRA_SEARCH_QUERY, queryKeyword)
        outState.putBoolean(EXTRA_SEARCH_COLLAPSED, isCollapsed)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        queryKeyword = savedInstanceState.getString(EXTRA_SEARCH_QUERY, "")
        isCollapsed = savedInstanceState.getBoolean(EXTRA_SEARCH_COLLAPSED)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchItem = menu.findItem(R.id.action_search)

        searchView = searchItem.actionView as SearchView

        val searchKeyword: TextView
                = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as TextView

        searchKeyword.setHintTextColor(Color.WHITE)
        searchKeyword.setTextColor(Color.WHITE)

        val searchCloseIcon
                = searchView.findViewById(androidx.appcompat.R.id.search_close_btn) as AppCompatImageView

        searchCloseIcon.setColorFilter(Color.WHITE)

        val voiceIcon
                = searchView.findViewById(androidx.appcompat.R.id.search_voice_btn) as AppCompatImageView

        voiceIcon.setColorFilter(Color.WHITE)

        searchItem.expandActionView()
        searchView.queryHint = resources.getString(R.string.search_hint)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val submitAction = SearchKeywordSubmitAction()

                submitAction.keyword = query
                EventBus.getDefault().post(submitAction)

                queryKeyword = query
                launchWork {
                    delay(DELAY_TIMER_INTERVAL.toLong())
                }

                if (getFragment(RecentKeywordFragment::class.java)?.isVisible!!) {
                    transactFragment(FeedSearchFragment::class.java, R.id.disconnect_container_feed_search, false)
                } else {
                    val fragment = getFragment(FeedSearchFragment::class.java) as FeedSearchFragment
                    fragment.searchKeyword(queryKeyword)
                }

                // workaround to avoid issues with some emulators and keyboard devices
                // firing twice if a keyboard enter is used see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus()

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (searchView.query.toString().isEmpty() && !isCollapsed) {
                    if (getFragment(FeedSearchFragment::class.java) != null && getFragment(FeedSearchFragment::class.java)!!.isVisible) {
                        transactFragment(RecentKeywordFragment::class.java, R.id.disconnect_container_feed_search, false)
                    }
                }

                isCollapsed = false

                return false
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if (!searchView.isIconified) {
                    isCollapsed = true
                    searchView.isIconified = true
                }

                onBackPressed()

                return false  // Return true to collapse action view
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                isCollapsed = false

                return true
            }
        })

        searchView.setOnCloseListener {
            onBackPressed()

            val action = FeedUpdateAction()

            action.position = 0
            EventBus.getDefault().post(action)
            setActivityResult()
            finishAfterTransition()

            false // Don't consume event, will dismiss search.
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                @SuppressLint("PrivateApi")
                val method = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", java.lang.Boolean.TYPE)
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: NoSuchMethodException) {
                System.err.println(e.message)
                e.printStackTrace()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }

        return super.onPreparePanel(featureId, view, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search ->

                true
            android.R.id.home -> {
                onBackPressed()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    internal fun doTransactFragment(keyword: String) {
        queryKeyword = keyword
        searchView.setQuery(keyword, true)
    }

    internal fun getKeyword(): String {
        return queryKeyword
    }

    internal fun getSearchView(): SearchView {
        return searchView
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@FeedSearchActivity.iv_disconnect_feed_search.visibility = View.GONE
        this@FeedSearchActivity.tv_notice1_feed_search.visibility = View.GONE
        this@FeedSearchActivity.tv_notice2_feed_search.visibility = View.GONE
        this@FeedSearchActivity.appbar_layout_feed_search.visibility = View.VISIBLE
    } else {
        this@FeedSearchActivity.iv_disconnect_feed_search.visibility = View.VISIBLE
        this@FeedSearchActivity.tv_notice1_feed_search.visibility = View.VISIBLE
        this@FeedSearchActivity.tv_notice2_feed_search.visibility = View.VISIBLE
        this@FeedSearchActivity.appbar_layout_feed_search.visibility = View.GONE
    }

    private fun setActivityResult() {
        setResult(SELECTED_SEARCH_ITEM_POSITION)
    }

    private fun getData() {
        queryKeyword = ""
        isCollapsed = false
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the defaultScope.
     *              lambda the some work will do
     */
    private inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return defaultScope.launch {
            block()
        }
    }
}
