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

package com.goforer.grabph.presentation.ui.setting.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_REGULAR
import com.goforer.grabph.R
import com.goforer.grabph.presentation.ui.setting.SettingListActivity
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.view_setting_item.view.*

class SettingListAdapter(private val activity: SettingListActivity, private val itemName: Array<String>):
                            ArrayAdapter<String>(activity, R.layout.view_setting_item, itemName) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val settingView = activity.layoutInflater
                                    .inflate(R.layout.view_setting_item, parent, false)
        activity.setFontTypeface(settingView.tv_setting_item, FONT_TYPE_REGULAR)
        settingView.tv_setting_item.text = itemName[position]


        return settingView
    }
}
