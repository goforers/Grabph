/*
 * Copyright (C) 2 018 Lukoh Nam, goForer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goforer.grabph.presentation.ui.setting

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.ui.setting.adapter.SettingListAdapter
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.activity_setting.toolbar
import kotlinx.android.synthetic.main.activity_setting.tv_title_name

class SettingListActivity: BaseActivity() {
    private val settingItemName = arrayOf("Push Notification", "Log Out", "Google Account",
                                            "Facebook Account", "Kakao Account", "Line Account")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.TRANSPARENT

        setting_list.adapter = SettingListAdapter(this, settingItemName)
        setFontTypeface(this@SettingListActivity.tv_tutorial, FONT_TYPE_REGULAR)
        this@SettingListActivity.tv_tutorial.text = getString(R.string.tutorial)
        this@SettingListActivity.iv_tutorial.setImageDrawable(getDrawable(R.drawable.ic_arrow_gray))
    }

    override fun setContentView() {
        setContentView(R.layout.activity_setting)
    }

    override fun setActionBar() {
        setSupportActionBar(this@SettingListActivity.toolbar)
        val actionBar= supportActionBar
        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            setFontTypeface(this@SettingListActivity.tv_title_name, FONT_TYPE_BOLD)
            this@SettingListActivity.tv_title_name.text = getString(R.string.setting_title)
        }

        this@SettingListActivity.toolbar?.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@SettingListActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        this@SettingListActivity.appbar_layout.outlineProvider = null
    }
}
