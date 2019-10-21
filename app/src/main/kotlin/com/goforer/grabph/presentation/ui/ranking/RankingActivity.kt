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

package com.goforer.grabph.presentation.ui.ranking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.Transition
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.Observer
import com.goforer.base.annotation.MockData
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.ui.ranking.adapter.RankingPagerAdapter
import com.goforer.grabph.presentation.ui.ranking.fragments.RankingFragment
import com.goforer.grabph.presentation.vm.ranking.RankingViewModel
import com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.ranking.RankingDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.ranking.Ranking
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_RANKING
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.datasource.network.response.Status
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_ranking.*
import javax.inject.Inject
import kotlin.reflect.full.findAnnotation

@RunWithMockData(true)
class RankingActivity : BaseActivity() {
    private var resultCode: Int = 0

    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock

    private var pagerAdapter: RankingPagerAdapter? = null

    private var photoRankFragment: RankingFragment? = null
    private var profitRankFragment: RankingFragment? = null
    private var likesRankFragment: RankingFragment? = null

    @field:Inject
    internal lateinit var rankingViewModel: RankingViewModel

    private val sharedExitListener = object : TransitionCallback() {
        override fun onTransitionEnd(transition: Transition) { removeCallback() }

        override fun onTransitionCancel(transition: Transition) { removeCallback() }

        private fun removeCallback() {
            window.sharedElementExitTransition.removeListener(this)
            setExitSharedElementCallback(null as SharedElementCallback?)
        }
    }

    companion object {
        const val RANK_TYPE_PHOTO = 1
        const val RANK_TYPE_PROFIT = 2
        const val RANK_TYPE_LIKE = 3

        const val FRAGMENT_KEY_PHOTO = "searp:fragment_photo"
        const val FRAGMENT_KEY_PROFIT = "searp:fragment_profit"
        const val FRAGMENT_KEY_LIKES = "searp:fragment_likes"

        const val VIEW_TYPE_TOP_RANK = 1
    }

    override fun setContentView() { setContentView(R.layout.activity_ranking) }

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
        setPagerAdapter(savedInstanceState)
        if (savedInstanceState == null) getRanking() else rankingViewModel.loadRankingFromCache()
    }

    override fun setActionBar() {
        setSupportActionBar(this@RankingActivity.toolbar_ranking)

        val actionBar = supportActionBar

        actionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            setDisplayShowTitleEnabled(true)
            elevation = 0f
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        this@RankingActivity.toolbar_ranking.setNavigationOnClickListener { finishAfterTransition() }
        this@RankingActivity.toolbar_ranking.hideOverflowMenu()
        setFontTypeface(this@RankingActivity.tv_ranking_title, FONT_TYPE_BOLD)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        this.resultCode = resultCode

        // Listener to reset shared element exit transition callbacks.
        window.sharedElementExitTransition.addListener(sharedExitListener)
        supportPostponeEnterTransition()
    }

    /*  Saves instances of the fragments by their key/value pairs, and use them after this activity unexpectedly closed */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        photoRankFragment?.let { if (it.isAdded) supportFragmentManager.putFragment(outState, FRAGMENT_KEY_PHOTO, it) }
        profitRankFragment?.let { if (it.isAdded) supportFragmentManager.putFragment(outState, FRAGMENT_KEY_PROFIT, it) }
        likesRankFragment?.let{ if (it.isAdded) supportFragmentManager.putFragment(outState, FRAGMENT_KEY_LIKES, it) }
    }

    private fun setPagerAdapter(savedInstanceState: Bundle?) {
        savedInstanceState?.let { getRankingFragmentInstance(it) }

        photoRankFragment = photoRankFragment ?: RankingFragment()
        photoRankFragment?.setRankType(RANK_TYPE_PHOTO)
        profitRankFragment = profitRankFragment ?: RankingFragment()
        profitRankFragment?.setRankType(RANK_TYPE_PROFIT)
        likesRankFragment = likesRankFragment ?: RankingFragment()
        likesRankFragment?.setRankType(RANK_TYPE_LIKE)

        pagerAdapter = pagerAdapter ?: RankingPagerAdapter(supportFragmentManager)

        photoRankFragment?.let { pagerAdapter?.addFragment(it, getString(R.string.ranking_tab_photo)) }
        profitRankFragment?.let { pagerAdapter?.addFragment(it, getString(R.string.ranking_tab_reward)) }
        likesRankFragment?.let { pagerAdapter?.addFragment(it, getString(R.string.ranking_tab_like)) }

        this@RankingActivity.viewPager_ranking.adapter = pagerAdapter
        this@RankingActivity.tabLayout_ranking.setupWithViewPager(viewPager_ranking)
        this@RankingActivity.tabLayout_ranking.tabRippleColor
    }

    private fun getRankingFragmentInstance(savedInstanceState: Bundle) {
        photoRankFragment = supportFragmentManager.getFragment(savedInstanceState, FRAGMENT_KEY_PHOTO)?.let { it as RankingFragment }
        profitRankFragment = supportFragmentManager.getFragment(savedInstanceState, FRAGMENT_KEY_PROFIT)?.let { it as RankingFragment }
        likesRankFragment = supportFragmentManager.getFragment(savedInstanceState, FRAGMENT_KEY_LIKES)?.let { it as RankingFragment }
    }

    private fun getRanking() {
        when(mock) {
            @MockData
            true -> transactMockData()
            false -> transactRealData()
        }
    }

    @MockData
    private fun transactMockData() {
        val ranking = RankingDataSource()

        ranking.setRanking()
        ranking.getRanking()?.let {
            rankingViewModel.setRanking(it)
            rankingViewModel.setRankingLiveData(it)
        }
    }

    private fun transactRealData() {
        val liveData = rankingViewModel.ranking

        rankingViewModel.setParameters(
            Parameters(
                "",
                -1,
                LOAD_RANKING,
                BOUND_FROM_LOCAL
            ), -1)
        liveData.observe(this, Observer { resource ->
            when(resource?.getStatus()) {

                Status.SUCCESS -> {

                    resource.getData() ?: return@Observer
                    val ranking = resource.getData() as? Ranking?

                    ranking?.let {
                        when {
                            it.likes_rank.ranking.isNotEmpty() -> rankingViewModel.setRankingLiveData(it)

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

                Status.LOADING -> { /* Loading Image Should be Implemented*/ }

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

    internal fun moveToOthersProfileActivity(userId: String, userName: String, ranking: Int, photoUrl: String) {
        Caller.callOtherUserProfile(this, Caller.CALLED_FROM_RANKING, userId, userName, ranking, photoUrl)
    }

    internal fun setRankColor(rank: Int, view: View, rankType: Int) {
        if(rankType == VIEW_TYPE_TOP_RANK) {
            when (rank) {
                PEOPLE_RANK_BEGINNER -> view.setBackgroundResource(R.drawable.border_rounded_rank_yellow)
                PEOPLE_RANK_FIRST -> view.setBackgroundResource(R.drawable.border_rounded_rank_blue)
                PEOPLE_RANK_SECOND -> view.setBackgroundResource(R.drawable.border_rounded_rank_orange)
                PEOPLE_RANK_THIRD -> view.setBackgroundResource(R.drawable.border_rounded_rank_purple)
                PEOPLE_RANK_FOURTH -> view.setBackgroundResource(R.drawable.border_rounded_rank_red)
            }
        } else { // rankType == VIEW_TYPE_BOTTOM_RANK
            when (rank) {
                PEOPLE_RANK_BEGINNER -> view.setBackgroundResource(R.drawable.border_rounded_rank_yellow_thin)
                PEOPLE_RANK_FIRST -> view.setBackgroundResource(R.drawable.border_rounded_rank_blue_thin)
                PEOPLE_RANK_SECOND -> view.setBackgroundResource(R.drawable.border_rounded_rank_orange_thin)
                PEOPLE_RANK_THIRD -> view.setBackgroundResource(R.drawable.border_rounded_rank_purple_thin)
                PEOPLE_RANK_FOURTH -> view.setBackgroundResource(R.drawable.border_rounded_rank_red_thin)
            }
        }
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@RankingActivity.disconnect_container_pinned_ranking.visibility = View.GONE
        this@RankingActivity.iv_disconnect_pinned_ranking.visibility = View.GONE
        this@RankingActivity.tv_notice1_pinned_ranking.visibility = View.GONE
        this@RankingActivity.appBar_ranking.visibility = View.VISIBLE
        this@RankingActivity.toolbar_ranking.visibility = View.VISIBLE
        this@RankingActivity.tv_ranking_title.visibility = View.VISIBLE
    } else {
        this@RankingActivity.disconnect_container_pinned_ranking.visibility = View.VISIBLE
        this@RankingActivity.iv_disconnect_pinned_ranking.visibility = View.VISIBLE
        this@RankingActivity.tv_notice1_pinned_ranking.visibility = View.VISIBLE
        this@RankingActivity.appBar_ranking.visibility = View.GONE
        this@RankingActivity.toolbar_ranking.visibility = View.GONE
        this@RankingActivity.tv_ranking_title.visibility = View.GONE
    }

    private fun showNetworkError(resource: Resource) {
        when (resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(this@RankingActivity.ranking_constraint_layout,
                        getString(R.string.phrase_client_wrong_request), Snackbar.LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(this@RankingActivity.ranking_constraint_layout,
                        getString(R.string.phrase_server_wrong_response), Snackbar.LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(this@RankingActivity.ranking_constraint_layout,
                        resource.getMessage().toString(), Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
