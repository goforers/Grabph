package com.goforer.grabph.presentation.ui.uploadquest

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import com.goforer.base.presentation.utils.CommonUtils.hideKeyboard
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.presentation.ui.uploadquest.fragment.QuestDescFragment
import com.goforer.grabph.presentation.ui.uploadquest.fragment.QuestInfoFragment
import com.goforer.grabph.presentation.vm.uploadquest.UploadQuestViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_upload_quest.*
import kotlinx.android.synthetic.main.layout_disconnection.*
import javax.inject.Inject

class UploadQuestActivity : BaseActivity() {

    private val fragmentManager = supportFragmentManager

    private var questInfoFragment: QuestInfoFragment? = null
    private var questDescriptionFragment: QuestDescFragment? = null

    private var currentFragment = 0

    @field:Inject
    lateinit var viewModel: UploadQuestViewModel

    companion object {
        internal const val CURRENT_FRAGMENT_QUEST_INFO = 0
        internal const val CURRENT_FRAGMENT_QUEST_DESC = 1
        internal const val CURRENT_FRAGMENT_QUEST_REVIEW = 2
    }

    override fun setContentView() { setContentView(R.layout.activity_upload_quest) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
            transactQuestInfoFragment()
            setFontType()

            networkStatusVisible(true)
        } else {
            networkStatusVisible(false)
        }
    }

    override fun setActionBar() {
        super.setActionBar()
        setSupportActionBar(this.toolbar_upload_quest)
        val actionBar = supportActionBar
        actionBar?.let {
            it.setHomeAsUpIndicator(R.drawable.ic_back)
            it.displayOptions = ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_USE_LOGO
            it.setDisplayShowTitleEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
        this.toolbar_upload_quest.hideOverflowMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }


    override fun onBackPressed() {
        hideKeyboard(this)

        when (currentFragment) {
            CURRENT_FRAGMENT_QUEST_INFO -> questInfoFragment?.onBackPressed()
            CURRENT_FRAGMENT_QUEST_DESC -> questDescriptionFragment?.onBackPressed()
            else -> goBackStack()
        }
    }

    internal fun goBackStack() {
        hideKeyboard(this)
        super.onBackPressed()
    }

    internal fun setCurrentFragment(fragment: Int) {
        currentFragment = fragment
    }

    private fun transactQuestInfoFragment() {
        hideKeyboard(this)

        questInfoFragment = questInfoFragment ?: QuestInfoFragment()
        val fragment = questInfoFragment

        fragmentManager.beginTransaction()
            .add(R.id.container_quest_upload, fragment as Fragment)
            .commit()
    }

    internal fun transactQuestDesc() {
        hideKeyboard(this)

        questDescriptionFragment = questDescriptionFragment ?: QuestDescFragment()
        val fragment = questDescriptionFragment

        fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right)
            .replace(R.id.container_quest_upload, fragment as Fragment)
            .commit()
    }

    private fun setFontType() {
        FONT_TYPE_BOLD.let {
            setFontTypeface(this.tv_upload_quest_title, it)
        }
    }

    private fun showLoadingProgressBar(isLoading: Boolean, comment: String? = null) {
        this.container_progress_bar_upload_quest.visibility = if (isLoading) View.VISIBLE else View.GONE
        comment?.let { this.tv_comment_upload_quest.text = it }
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this.container_quest_upload.visibility = View.VISIBLE
        this.disconnect_container_pinned.visibility = View.GONE
    } else {
        this.container_quest_upload.visibility = View.GONE
        this.disconnect_container_pinned.visibility = View.VISIBLE
    }

    private fun showNetworkError(resource: Resource) = when(resource.errorCode) {
        in 400..499 -> showSnackBar(getString(R.string.phrase_client_wrong_request))
        in 500..599 -> showSnackBar(getString(R.string.phrase_server_wrong_response))
        else -> showSnackBar(resource.getMessage().toString())
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(this.container_quest_upload, msg, Snackbar.LENGTH_LONG).show()
    }

}