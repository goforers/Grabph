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

package com.goforer.grabph.presentation.ui.hottopic

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.transition.Transition
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.annotation.MockData
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.presentation.caller.Caller.EXTRA_HOP_TOPIC_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_HOT_TOPIC_CONTENT_ID
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_HOT_TOPIC_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.common.menu.MenuHandler
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.hottopic.adapter.HotTopicContentAdapter
import com.goforer.grabph.presentation.ui.hottopic.sharedelementcallback.HotTopicContentCallback
import com.goforer.grabph.presentation.vm.hottopic.HotTopicContentViewModel
import com.goforer.grabph.repository.model.cache.data.mock.datasource.hottopic.HotTopicContentDataSource
import com.goforer.grabph.repository.model.cache.data.entity.hottopic.HotTopicContent
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.LOAD_HOT_TOPIC_CONTENT
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.response.Status
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_hot_topic.*
import kotlinx.android.synthetic.main.activity_hot_topic.appbar_layout
import kotlinx.android.synthetic.main.activity_hot_topic.toolbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.full.findAnnotation

@RunWithMockData(true)
class HotTopicContentActivity: BaseActivity() {
    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private var adapter: HotTopicContentAdapter? = null

    private lateinit var hotTopicContentId: String

    private lateinit var sharedElementCallback: HotTopicContentCallback

    @field:Inject
    lateinit var hotTopicContentViewModel: HotTopicContentViewModel

    private val sharedEnterListener = object : TransitionCallback() {
        override fun onTransitionEnd(transition: Transition) {
            removeCallback()
        }

        override fun onTransitionCancel(transition: Transition) {
            removeCallback()
        }

        private fun removeCallback() {
            window.sharedElementEnterTransition.removeListener(this)
            setEnterSharedElementCallback(null as SharedElementCallback?)
        }
    }

    private val sharedExitListener = object : TransitionCallback() {
        override fun onTransitionEnd(transition: Transition) {
            removeCallback()
        }

        override fun onTransitionCancel(transition: Transition) {
            removeCallback()
        }

        private fun removeCallback() {
            window.sharedElementExitTransition.removeListener(this)
            setExitSharedElementCallback(null as SharedElementCallback?)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
            networkStatusVisible(true)
            getIntentData()
            getHotTopicContent()

            this@HotTopicContentActivity.coordinator_hot_topic_content_layout.setOnSwipeOutListener(this, object : OnSwipeOutListener {
                override fun onSwipeLeft(x: Float, y: Float) {
                    Timber.d("onSwipeLeft")

                    finishAfterTransition()
                }

                override fun onSwipeRight(x: Float, y: Float) {
                    Timber.d("onSwipeRight")
                }

                override fun onSwipeDown(x: Float, y: Float) {
                    Timber.d( "onSwipeDown")
                }

                override fun onSwipeUp(x: Float, y: Float) {
                    Timber.d("onSwipeUp")
                }

                override fun onSwipeDone() {
                    Timber.d("onSwipeDone")
                }
            })
        } else {
            networkStatusVisible(false)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        setActivityResult()

        super.onBackPressed()
    }

    @SuppressLint("RestrictedApi")
    override fun setActionBar() {
        setSupportActionBar(this@HotTopicContentActivity.toolbar)
        val actionBar= supportActionBar
        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            setFontTypeface(this@HotTopicContentActivity.tv_title_name, FONT_TYPE_BOLD)
            this@HotTopicContentActivity.tv_title_name.text = getString(R.string.hot_topic)
        }

        this@HotTopicContentActivity.toolbar?.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@HotTopicContentActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        this@HotTopicContentActivity.appbar_layout.outlineProvider = null
        window.sharedElementEnterTransition.addListener(sharedEnterListener)
        supportPostponeEnterTransition()
        this@HotTopicContentActivity.recycler_hot_topic_view.setHasFixedSize(true)
        this@HotTopicContentActivity.recycler_hot_topic_view.setItemViewCacheSize(10)
        this@HotTopicContentActivity.recycler_hot_topic_view.isVerticalScrollBarEnabled = false
        this@HotTopicContentActivity.recycler_hot_topic_view.layoutManager = LinearLayoutManager(this.applicationContext, RecyclerView.VERTICAL, false)

        window.sharedElementEnterTransition.addListener(sharedEnterListener)
        supportPostponeEnterTransition()
    }

    override fun setContentView() {
        setContentView(R.layout.activity_hot_topic)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(EXTRA_HOT_TOPIC_CONTENT_ID, hotTopicContentId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        hotTopicContentId = savedInstanceState.getString(EXTRA_HOT_TOPIC_CONTENT_ID).toString()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        window.sharedElementExitTransition.addListener(sharedExitListener)
        supportPostponeEnterTransition()

        val callback = HotTopicContentCallback()

        callback.setViewBinding(this@HotTopicContentActivity.iv_hot_topic_content)
        setEnterSharedElementCallback(callback)
        supportStartPostponedEnterTransition()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_normal_item, menu)

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
        when (item.itemId) {
            R.id.action_photo_share -> {
                val wrapper = ContextThemeWrapper(this, R.style.PopupMenu)
                val sharePopup = PopupMenu(wrapper, findViewById(R.id.action_photo_share), Gravity.CENTER)

                sharePopup.menuInflater.inflate(R.menu.menu_share_popup, sharePopup.menu)
                MenuHandler().applyFontToMenuItem(sharePopup, Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                        resources.getColor(R.color.colorHomeQuestFavoriteKeyword, theme))
                sharePopup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_share_facebook -> {

                        }

                        R.id.menu_share_ect -> {
                        }

                        else -> {
                        }
                    }

                    true
                }

                sharePopup.show()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun finishAfterTransition() {
        supportPostponeEnterTransition()
        setViewBind()
        setActivityResult()

        super.finishAfterTransition()
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@HotTopicContentActivity.disconnect_container_hot_topic_content.visibility = View.GONE
        this@HotTopicContentActivity.iv_disconnect_hot_topic_content.visibility = View.GONE
        this@HotTopicContentActivity.tv_notice1_hot_topic_content.visibility = View.GONE
        this@HotTopicContentActivity.tv_notice2_hot_topic_content.visibility = View.GONE
        this@HotTopicContentActivity.appbar_layout.visibility = View.VISIBLE
    } else {
        this@HotTopicContentActivity.disconnect_container_hot_topic_content.visibility = View.VISIBLE
        this@HotTopicContentActivity.iv_disconnect_hot_topic_content.visibility = View.VISIBLE
        this@HotTopicContentActivity.tv_notice1_hot_topic_content.visibility = View.VISIBLE
        this@HotTopicContentActivity.tv_notice2_hot_topic_content.visibility = View.VISIBLE
        this@HotTopicContentActivity.appbar_layout.visibility = View.GONE
    }

    private fun getHotTopicContent() {
        when(mock) {
            @MockData
            true -> transactMockData()
            false -> transactRealData()
        }
    }

    @MockData
    private fun transactMockData() {
        val hotTopicContent = HotTopicContentDataSource()

        hotTopicContentViewModel.loadHotTopicContent.observe(this, Observer {
            it?.let { content ->
                displayHotTopicContent(content)
                createAdapter()
                adapter?.addItem(content.topicContent.contents)
            }
        })

        hotTopicContent.setHotTopicContent()
        hotTopicContentViewModel.setHotTopicContent(hotTopicContent.getHotTopicContent()!!)
    }

    private fun getIntentData() {
        hotTopicContentId = intent.getStringExtra(EXTRA_HOT_TOPIC_CONTENT_ID)!!
    }

    private fun transactRealData() {
        val liveData =  hotTopicContentViewModel.hotTopicContent

        hotTopicContentViewModel.setParameters(Parameters(hotTopicContentId, -1, LOAD_HOT_TOPIC_CONTENT, BOUND_FROM_LOCAL), -1)
        liveData.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { content ->
                        displayHotTopicContent(content as HotTopicContent)
                        createAdapter()
                        adapter?.addItem(content.topicContent.contents)
                    }

                    resource.getMessage()?.let {
                        showNetworkError(resource)
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    showNetworkError(resource)
                    liveData.removeObservers(this)
                }

                else -> {
                    showNetworkError(resource)
                }
            }
        })
    }

    private fun createAdapter() {
        adapter = HotTopicContentAdapter(this)
        this@HotTopicContentActivity.recycler_hot_topic_view.adapter = adapter
    }

    private fun displayHotTopicContent(hotTopicContent: HotTopicContent) {
        setFontTypeface()
        this@HotTopicContentActivity.iv_hot_topic_content.isFocusableInTouchMode = true
        this@HotTopicContentActivity.iv_hot_topic_content.requestFocus()
        setFixedImageSize(0, 0)
        setImageDraw(this@HotTopicContentActivity.iv_hot_topic_content,
                     this@HotTopicContentActivity.hot_topic_constraintLayoutContainer, hotTopicContent.media.urls.regular!!, true)
        this@HotTopicContentActivity.tv_hot_topic_tip_phrase.text = getString(R.string.snap_home_main_hot_topic_photo_phrase)
        this@HotTopicContentActivity.tv_hot_topic_title.text = hotTopicContent.title
        this@HotTopicContentActivity.tv_sub_title.text = hotTopicContent.subTitle
        withDelay(50L) {
            setViewBind()
            setEnterSharedElementCallback(sharedElementCallback)
            supportStartPostponedEnterTransition()
        }
    }

    private fun setViewBind() {
        this@HotTopicContentActivity.iv_hot_topic_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + 0
        sharedElementCallback = HotTopicContentCallback()
        sharedElementCallback.setViewBinding(this@HotTopicContentActivity.iv_hot_topic_content)
    }

    private fun setActivityResult() {
        val intent = Intent(this, HomeActivity::class.java)

        intent.putExtra(EXTRA_HOP_TOPIC_POSITION, 0)
        setResult(SELECTED_BEST_PICK_HOT_TOPIC_POSITION, intent)
    }

    private fun setFontTypeface() {
        val krRegularTypeface = Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_MEDIUM)
        val krBoldTypeface = Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_BOLD)

        this@HotTopicContentActivity.tv_title_name.typeface = krRegularTypeface
        this@HotTopicContentActivity.tv_sub_title.typeface = krBoldTypeface
    }

    private fun showNetworkError(resource: Resource) {
        when(resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(this@HotTopicContentActivity.coordinator_hot_topic_content_layout,
                                getString(R.string.phrase_client_wrong_request), Snackbar.LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(this@HotTopicContentActivity.coordinator_hot_topic_content_layout,
                                getString(R.string.phrase_server_wrong_response), Snackbar.LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(this@HotTopicContentActivity.coordinator_hot_topic_content_layout,
                                resource.getMessage().toString(), Snackbar.LENGTH_LONG).show()
            }
        }
    }

}