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

package com.goforer.grabph.presentation.ui.people

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.goforer.base.annotation.MockData
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PEOPLE_TYPE
import com.goforer.grabph.presentation.ui.people.adapter.PeopleAdapter
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.presentation.vm.people.PeopleViewModel
import com.goforer.grabph.repository.model.cache.data.mock.datasource.people.PeopleDataSource
import com.goforer.grabph.repository.model.cache.data.mock.datasource.people.SearperDataSource
import com.goforer.grabph.repository.model.cache.data.entity.profile.Searper
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.LOAD_PEOPLE
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.response.Status
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_people.*
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.reflect.full.findAnnotation

@SuppressLint("Registered")
@RunWithMockData(true)
class PeopleActivity: BaseActivity() {
    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private var layoutType: Int = 0

    private lateinit var adapter: PeopleAdapter

    @field:Inject
    internal lateinit var peopleViewModel: PeopleViewModel

    private val job = Job()

    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        const val LAYOUT_SEARPER = 1
        const val LAYOUT_SEARPLE = 2
        const val LAYOUT_LIKE = 3
        const val LAYOUT_SELL = 4
    }

    override fun setContentView() { setContentView(R.layout.activity_people) }

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

    override fun setViews(savedInstanceState: Bundle?) {
        super.setViews(savedInstanceState)
        getIntentData(savedInstanceState)
        createAdapter()
        observePeopleLiveData()
        if (savedInstanceState == null) getPeople() else {
            peopleViewModel.loadPeopleFromCache()
        }
    }

    override fun setActionBar() {
        setSupportActionBar(this@PeopleActivity.toolbar_people)

        val actionBar = supportActionBar

        actionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            setDisplayShowTitleEnabled(true)
            elevation = 0f
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        this@PeopleActivity.toolbar_people.setNavigationOnClickListener { finishAfterTransition() }
        this@PeopleActivity.toolbar_people.hideOverflowMenu()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run { putInt(EXTRA_PEOPLE_TYPE, layoutType) }
        super.onSaveInstanceState(outState)
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()
        ioScope.cancel()
        job.cancel()
    }

    private fun getPeople() {
        when (mock) {
            @MockData
            true -> transactMockData()
            false -> transactRealData()
        }
    }

    private fun observePeopleLiveData() {
        peopleViewModel.getPeopleLiveData().observe(this, Observer {

            val config = PagedList.Config.Builder()
                    .setInitialLoadSizeHint(10)
                    .setPageSize(20)
                    .setPrefetchDistance(10)
                    .build()

            LivePagedListBuilder(object : DataSource.Factory<Int, Searper>() {
                override fun create(): DataSource<Int, Searper> {
                    return SearperDataSource(it)
                }
            }, config).build().observe(this, Observer { pagedList ->
                adapter.submitList(pagedList)
                adapter.setFollowListStatus(pagedList)
            })
        })
    }

    @MockData
    private fun transactMockData() {
        val people = PeopleDataSource()

        when (layoutType) {
            LAYOUT_SEARPER -> {
                people.setFollowings()
                people.getFollowings()?.result?.let {
                    peopleViewModel.setPeople(it)
                    peopleViewModel.setPeopleLiveData(it)
                }
            }
            LAYOUT_SEARPLE -> {
                people.setFollowers()
                people.getFollowers()?.result?.let {
                    peopleViewModel.setPeople(it)
                    peopleViewModel.setPeopleLiveData(it)
                }
            }

            //LAYOUT_LIKE -> json = "my_followers.json"

            LAYOUT_SELL -> {
                people.setFollowers()
                people.getFollowers()?.result?.let {
                    peopleViewModel.setPeople(it)
                    peopleViewModel.setPeopleLiveData(it)
                }
            }
        }
    }

    private fun transactRealData() {
        // "id" is needed to be inserted
        peopleViewModel.setParameters(Parameters("", -1, LOAD_PEOPLE, BOUND_FROM_LOCAL), NONE_TYPE)
        val liveData = peopleViewModel.people
        liveData.observe(this, Observer { resource ->
            when(resource?.getStatus()) {

                Status.SUCCESS -> {

                    resource.getData()?.let {
                        val people = it as List<Searper>

                        when {
                            people.isNotEmpty() -> peopleViewModel.setPeopleLiveData(people)

                            resource.getMessage() != null -> {
                                showNetworkError(resource)
                                liveData.removeObservers(this)
                            }

                            else -> {
                                showNetworkError(resource)
                                liveData.removeObservers(this)
                            }
                        }
                    }
                }
                Status.LOADING -> { /*로딩 이미지 구현 필요*/ }

                Status.ERROR -> {
                    showNetworkError(resource)
                    liveData.removeObservers(this)
                }

                else -> {
                    showNetworkError(resource)
                    liveData.removeObservers(this)
                }
            }
        })
    }

    private fun createAdapter() {
        adapter = PeopleAdapter(this, layoutType)
        recycler_people_layout.layoutManager = LinearLayoutManager(applicationContext)

        val divider = DividerItemDecoration(applicationContext, LinearLayoutManager(applicationContext).orientation)

        getDrawable(R.drawable.recycer_divider)?.let { divider.setDrawable(it) }
        recycler_people_layout.addItemDecoration(divider)
        recycler_people_layout.adapter = adapter
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@PeopleActivity.disconnect_container_pinned_people.visibility = View.GONE
        this@PeopleActivity.appbar_layout_people.visibility = View.VISIBLE
        this@PeopleActivity.toolbar_people.visibility = View.VISIBLE
        this@PeopleActivity.tv_people_title.visibility = View.VISIBLE
    } else {
        this@PeopleActivity.disconnect_container_pinned_people.visibility = View.VISIBLE
        this@PeopleActivity.appbar_layout_people.visibility = View.GONE
        this@PeopleActivity.toolbar_people.visibility = View.GONE
        this@PeopleActivity.tv_people_title.visibility = View.GONE
    }

    private fun getIntentData(savedInstanceState: Bundle?) {
        layoutType = savedInstanceState?.getInt(EXTRA_PEOPLE_TYPE) ?: intent.getIntExtra(EXTRA_PEOPLE_TYPE, -1)
        setTitle()
    }

    private fun setTitle() {
        when (layoutType) {
            LAYOUT_SEARPER -> tv_people_title.text = getString(R.string.people_title_searper)
            LAYOUT_SEARPLE -> tv_people_title.text = getString(R.string.people_title_searple)
            LAYOUT_LIKE -> tv_people_title.text = getString(R.string.people_title_like)
            LAYOUT_SELL -> tv_people_title.text = getString(R.string.people_title_buyer)
        }

        setFontTypeface(tv_people_title, FONT_TYPE_MEDIUM)
    }

    private fun showNetworkError(resource: Resource) {
        when(resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(this@PeopleActivity.people_constraint_layout, getString(R.string.phrase_client_wrong_request), Snackbar.LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(this@PeopleActivity.people_constraint_layout, getString(R.string.phrase_server_wrong_response), Snackbar.LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(this@PeopleActivity.people_constraint_layout, resource.getMessage().toString(), Snackbar.LENGTH_LONG).show()
            }
        }
    }
}