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

package com.goforer.grabph.presentation.ui.home.profile.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ProfilePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragments = ArrayList<Fragment>()
    private val titles = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // return titles[position]
        return null
    }

    internal fun addFragment(fragment: Fragment, title: String){
        fragments.add(fragment)
        titles.add(title)
    }
}