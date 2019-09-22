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

package com.goforer.grabph.presentation.ui.ranking.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.EXTRA_RANKING_TYPE
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.ranking.RankingActivity
import com.goforer.grabph.presentation.ui.ranking.RankingActivity.Companion.RANK_TYPE_PHOTO
import com.goforer.grabph.presentation.ui.ranking.RankingActivity.Companion.RANK_TYPE_PROFIT
import com.goforer.grabph.presentation.ui.ranking.adapter.RankingAdapter
import com.goforer.grabph.presentation.vm.ranking.RankingViewModel
import kotlinx.android.synthetic.main.fragment_ranking.*
import javax.inject.Inject

class RankingFragment: BaseFragment() {
    private lateinit var acvRankingAdapter: AutoClearedValue<RankingAdapter>

    private var adapter: RankingAdapter? = null

    private var rankType: Int = 0

    @field:Inject
    internal lateinit var rankingViewModel: RankingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this, inflater.inflate(R.layout.fragment_ranking, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.getInt(EXTRA_RANKING_TYPE)?.let { rankType = it }

        observeRankingLiveData()

        createAdapter()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_RANKING_TYPE, rankType)
    }

    internal fun setRankType(type: Int) { rankType = type }

    private fun observeRankingLiveData() {
        rankingViewModel.getRankingLiveData().observe(this, Observer {
            when (rankType) {
                RANK_TYPE_PHOTO -> adapter?.addList(it.photo_rank.ranking)

                RANK_TYPE_PROFIT -> adapter?.addList(it.profit_rank.ranking)

                else -> adapter?.addList(it.likes_rank.ranking)
            }
        })
    }

    private fun createAdapter() {
        adapter = adapter ?: RankingAdapter((baseActivity as RankingActivity), rankType)
        acvRankingAdapter = AutoClearedValue(this, adapter)

        this@RankingFragment.recycler_ranking.adapter = acvRankingAdapter.get()
        this@RankingFragment.recycler_ranking.layoutManager = LinearLayoutManager(activity)

        val divider = DividerItemDecoration(activity, LinearLayoutManager(activity).orientation)
        activity?.getDrawable(R.drawable.recycer_divider)?.let { divider.setDrawable(it) }
        this@RankingFragment.recycler_ranking.addItemDecoration(divider)
    }
}