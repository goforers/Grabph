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

package com.goforer.grabph.presentation.ui.comment

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import android.view.View
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_ID
import com.goforer.grabph.presentation.ui.comment.fragment.CommentFragment
import kotlinx.android.synthetic.main.activity_photo_comment.*

class CommentActivity : BaseActivity() {
    internal lateinit var photoID: String

    public override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState ?: getIntentData()

        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            photoID = savedInstanceState.getString(EXTRA_PHOTO_ID, "")
        }

        if (!isNetworkAvailable) {
            this@CommentActivity.iv_disconnect_photo_comment.visibility = View.VISIBLE
            this@CommentActivity.tv_notice1_photo_comment.visibility = View.VISIBLE
            this@CommentActivity.tv_notice2_photo_comment.visibility = View.VISIBLE
        }
    }

    override fun setContentView() {
        setContentView(R.layout.activity_photo_comment)
    }

    override fun setActionBar() {
        setSupportActionBar(this@CommentActivity.toolbar)

        val actionBar = supportActionBar

        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.elevation = 0f
            actionBar.title = "Comments of the Photo"
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        toolbar.setNavigationOnClickListener{
            finishAfterTransition()
        }

        toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?)
            = transactFragment(CommentFragment::class.java, R.id.disconnect_container_photo_comment, false)


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(EXTRA_PHOTO_ID, photoID)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        photoID = savedInstanceState.getString(EXTRA_PHOTO_ID, "")
    }

    private fun getIntentData() {
        photoID = intent.getStringExtra(EXTRA_PHOTO_ID)!!
    }
}
