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

package com.goforer.grabph.presentation.ui.home.profile.fragment.sales

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SAVED_SALES_TAB_INDEX
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.profile.adapter.sales.SalePagerAdapter
import com.goforer.grabph.presentation.vm.profile.HomeProfileViewModel
import com.goforer.grabph.repository.model.cache.data.entity.profile.MyPhoto
import kotlinx.android.synthetic.main.fragment_home_profile_sale.*
import javax.inject.Inject

/**
 * This Fragment is a fragment inside HomeProfileFragment
 * This is one of the two fragments that can be accessed through tab layout.
 * */

class HomeProfileSalesFragment: BaseFragment(), View.OnClickListener {
    private var pagerAdapter: SalePagerAdapter? = null

    private lateinit var acvPagerAdapter: AutoClearedValue<SalePagerAdapter>

    private var salesAllFragment: HomeProfileSaleStatusFragment? = null
    private var salesVerifyingFragment: HomeProfileSaleStatusFragment? = null
    private var salesApprovedFragment: HomeProfileSaleStatusFragment? = null
    private var salesInvalidFragment: HomeProfileSaleStatusFragment? = null

    private val statusNumbers: MutableList<TextView> by lazy { ArrayList<TextView>() }
    private val statusTexts: MutableList<TextView> by lazy { ArrayList<TextView>() }

    @field:Inject
    lateinit var homeProfileViewModel: HomeProfileViewModel

    private var currentTabIndex: Int = 0

    companion object {
        internal const val SALES_ALL_INDEX = 0
        internal const val SALES_VERIFYING_INDEX = 1
        internal const val SALES_APPROVED_INDEX = 2
        internal const val SALES_INVALID_INDEX = 3

        const val FRAGMENT_KEY_SALES_ALL = "searp:fragment_home_all"
        const val FRAGMENT_KEY_SALES_VERIFYING = "searp:fragment_home_verifying"
        const val FRAGMENT_KEY_SALES_APPROVED = "searp:fragment_home_approved"
        const val FRAGMENT_KEY_SALES_INVALID = "searp:fragment_home_invalid"
    }

    private val homeActivity: HomeActivity by lazy { activity as HomeActivity }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_profile_sale, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPagerAdapter(savedInstanceState)
        initTabs(savedInstanceState)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.constraint_profile_sale_containerAll -> setTabButtonColor(SALES_ALL_INDEX)
            R.id.constraint_profile_sale_containerOnInspection -> setTabButtonColor(SALES_VERIFYING_INDEX)
            R.id.constraint_profile_sale_containerApproved -> setTabButtonColor(SALES_APPROVED_INDEX)
            R.id.constraint_profile_sale_containerFail -> setTabButtonColor(SALES_INVALID_INDEX)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        salesAllFragment?.let { if (it.isAdded) fragmentManager?.putFragment(outState, FRAGMENT_KEY_SALES_ALL, it) }
        salesVerifyingFragment?.let { if (it.isAdded) fragmentManager?.putFragment(outState, FRAGMENT_KEY_SALES_VERIFYING, it) }
        salesApprovedFragment?.let { if (it.isAdded) fragmentManager?.putFragment(outState, FRAGMENT_KEY_SALES_APPROVED, it) }
        salesInvalidFragment?.let { if (it.isAdded) fragmentManager?.putFragment(outState, FRAGMENT_KEY_SALES_INVALID, it) }

        outState.putInt(EXTRA_SAVED_SALES_TAB_INDEX, currentTabIndex)
    }

    private fun initTabs(savedInstanceState: Bundle?) {
        setTabViewList()
        initStatusClickListener()
        observeHomeProfileLiveData()
        setTabButtonColor(savedInstanceState?.getInt(EXTRA_SAVED_SALES_TAB_INDEX) ?: 0)
        setFontType()
    }

    private fun setPagerAdapter(savedInstanceState: Bundle?) {
        savedInstanceState?.let { getViewInstance(it) }

        salesAllFragment = salesAllFragment ?: HomeProfileSaleStatusFragment() //1
        salesAllFragment?.setStatusType(SALES_ALL_INDEX)
        salesVerifyingFragment = salesVerifyingFragment ?: HomeProfileSaleStatusFragment() //2
        salesVerifyingFragment?.setStatusType(SALES_VERIFYING_INDEX)
        salesApprovedFragment = salesApprovedFragment ?: HomeProfileSaleStatusFragment() //3
        salesApprovedFragment?.setStatusType(SALES_APPROVED_INDEX)
        salesInvalidFragment = salesInvalidFragment ?: HomeProfileSaleStatusFragment() //4
        salesInvalidFragment?.setStatusType(SALES_INVALID_INDEX)

        pagerAdapter = pagerAdapter ?: SalePagerAdapter(requireFragmentManager())
        acvPagerAdapter = AutoClearedValue(this, pagerAdapter)

        salesAllFragment?.let { pagerAdapter?.addFragment(it) }
        salesVerifyingFragment?.let { pagerAdapter?.addFragment(it) }
        salesApprovedFragment?.let { pagerAdapter?.addFragment(it) }
        salesInvalidFragment?.let { pagerAdapter?.addFragment(it) }

        this@HomeProfileSalesFragment.viewPager_profile_sale.adapter = acvPagerAdapter.get()
        this@HomeProfileSalesFragment.viewPager_profile_sale.disableSwipe(true)
        this@HomeProfileSalesFragment.tv_profile_sale_number_all.setBackgroundResource(R.drawable.border_circle_of_button_white)
        this@HomeProfileSalesFragment.tv_profile_sale_number_all.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        this@HomeProfileSalesFragment.tv_profile_sale_text_all.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun getViewInstance(savedInstanceState: Bundle) {
        salesAllFragment = fragmentManager?.getFragment(savedInstanceState, FRAGMENT_KEY_SALES_ALL)?.let { it as HomeProfileSaleStatusFragment }
        salesVerifyingFragment = fragmentManager?.getFragment(savedInstanceState, FRAGMENT_KEY_SALES_VERIFYING)?.let { it as HomeProfileSaleStatusFragment }
        salesApprovedFragment = fragmentManager?.getFragment(savedInstanceState, FRAGMENT_KEY_SALES_APPROVED)?.let { it as HomeProfileSaleStatusFragment }
        salesInvalidFragment = fragmentManager?.getFragment(savedInstanceState, FRAGMENT_KEY_SALES_INVALID)?.let { it as HomeProfileSaleStatusFragment }
    }

    private fun observeHomeProfileLiveData() {
        homeProfileViewModel.getHomeProfileLiveData().observe(this, Observer {
            setSalesStatusNumber(it.sellPhotos.photos)
            homeProfileViewModel.setSalesStatusLiveData(it.sellPhotos.photos)
        })
    }

    private fun setTabButtonColor(index: Int) {
        setButtonsUnchecked()
        currentTabIndex = index

        val statusNumber: TextView = statusNumbers[index]
        val statusText: TextView = statusTexts[index]

        viewPager_profile_sale.currentItem = index

        statusNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        statusNumber.setBackgroundResource(R.drawable.border_circle_of_button_white)
        statusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun setSalesStatusNumber(mySales: List<MyPhoto>) {
        val salesAll = mySales.size
        val salesVerifying = mySales.filter { it.status == "verifying" }.count()
        val salesApproved = mySales.filter { it.status == "approved" }.count()
        val salesFailed = mySales.filter { it.status == "invalid" }.count()

        this@HomeProfileSalesFragment.tv_profile_sale_number_all.text = "$salesAll"
        this@HomeProfileSalesFragment.tv_profile_sale_number_OnInspection.text = "$salesVerifying"
        this@HomeProfileSalesFragment.tv_profile_sale_number_approved.text = "$salesApproved"
        this@HomeProfileSalesFragment.tv_profile_sale_number_containerFail.text = "$salesFailed"
    }

    private fun initStatusClickListener() {
        this@HomeProfileSalesFragment.constraint_profile_sale_containerAll.setOnClickListener(this)
        this@HomeProfileSalesFragment.constraint_profile_sale_containerOnInspection.setOnClickListener(this)
        this@HomeProfileSalesFragment.constraint_profile_sale_containerApproved.setOnClickListener(this)
        this@HomeProfileSalesFragment.constraint_profile_sale_containerFail.setOnClickListener(this)
    }

    private fun setTabViewList() {
        statusNumbers.add(this@HomeProfileSalesFragment.tv_profile_sale_number_all)
        statusNumbers.add(this@HomeProfileSalesFragment.tv_profile_sale_number_OnInspection)
        statusNumbers.add(this@HomeProfileSalesFragment.tv_profile_sale_number_approved)
        statusNumbers.add(this@HomeProfileSalesFragment.tv_profile_sale_number_containerFail)

        statusTexts.add(this@HomeProfileSalesFragment.tv_profile_sale_text_all)
        statusTexts.add(this@HomeProfileSalesFragment.tv_profile_sale_text_OnInspection)
        statusTexts.add(this@HomeProfileSalesFragment.tv_profile_sale_text_approved)
        statusTexts.add(this@HomeProfileSalesFragment.tv_profile_sale_text_containerFail)
    }

    private fun setButtonsUnchecked() {
        val color: Int = ContextCompat.getColor(requireContext(), R.color.uncheckedSalesStatusColor)
        val background: Int = R.drawable.border_circle_of_button_grey

        for(i in statusNumbers.indices){
            statusNumbers[i].setBackgroundResource(background)
            statusNumbers[i].setTextColor(color)
            statusTexts[i].setTextColor(color)
        }
    }

    private fun setFontType() {
        val krMediumTypeface = Typeface.createFromAsset(homeActivity.applicationContext?.assets, BaseActivity.NOTO_SANS_KR_MEDIUM)

        krMediumTypeface.let {
            tv_profile_sale_number_all.typeface = it
            tv_profile_sale_number_OnInspection.typeface = it
            tv_profile_sale_number_approved.typeface = it
            tv_profile_sale_number_containerFail.typeface = it
            tv_profile_sale_text_all.typeface = it
            tv_profile_sale_text_OnInspection.typeface = it
            tv_profile_sale_text_approved.typeface = it
            tv_profile_sale_text_containerFail.typeface = it
        }
    }
}

