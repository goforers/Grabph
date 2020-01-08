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

package com.goforer.grabph.presentation.ui.questinfo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.transition.Transition
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.utils.CommonUtils.setTextViewGradient
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.SORT_QUEST_CLOSED
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.SORT_QUEST_ONGOING
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest.Companion.SORT_QUEST_UNDER_EXAMINATION
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FORM_HOME_FAVORITE_QUEST
import com.goforer.grabph.presentation.caller.Caller.CALLED_FORM_HOME_HOT_QUEST
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_BEST_PICK_QUEST
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_MAIN_QUEST
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_CALLED_FROM
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_DESCRIPTION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_DURATION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_OWNER_IMAGE
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_OWNER_LOGO
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_OWNER_NAME
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_REWARD
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_STATE
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_TITLE
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.common.menu.MenuHandler
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.questinfo.sharedelementcallback.QuestInfoCallback
import com.goforer.grabph.presentation.vm.quest.info.QuestInfoViewModel
import com.goforer.grabph.presentation.ui.photoviewer.sharedelementcallback.PhotoViewerCallback
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.info.QuestInfo
import com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.questinfo.QuestInfoDataSource
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_FAVORITE_QUEST_INFO
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_MEDIA_TYPE
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_NUMBER_OF_CONTENTS
import com.goforer.grabph.presentation.ui.questinfo.adapter.QuestInspirationAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_quest_info.*
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs
import kotlin.reflect.full.findAnnotation

@Suppress("SameParameterValue", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@RunWithMockData(true)
class QuestInfoActivity: BaseActivity() {
    private lateinit var sharedElementCallback: QuestInfoCallback

    private lateinit var questInfo: QuestInfo

    private lateinit var ownerName: String
    private lateinit var ownerLogo: String
    private lateinit var ownerImage: String
    private lateinit var title: String
    private lateinit var description: String
    private lateinit var questState: String
    private lateinit var reward: String
    private lateinit var numberOfContents: String
    private lateinit var mediaType: String

    private var duration: Int = 0
    private var calledFrom: Int = 0
    private var position: Int = 0

    private var isAppBarLayoutExpanded = false
    private var isAppBarLayoutCollapsed = false

    private val job = Job()

    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val defaultScope = CoroutineScope(Dispatchers.Default + job)
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    @field:Inject
    lateinit var questInfoViewModel: QuestInfoViewModel

    @field:Inject
    lateinit var workHandler: CommonWorkHandler

    @field:Inject
    lateinit var waterMarkHandler: WatermarkHandler

    companion object {
        private const val DELAY_COLLAPSED_TIMER_INTERVAL = 300
    }

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

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let { getIntentData() }

        val krBoldTypeface = Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_BOLD)
        this@QuestInfoActivity.collapsing_layout.setCollapsedTitleTypeface(krBoldTypeface)
        this@QuestInfoActivity.collapsing_layout.setExpandedTitleTypeface(krBoldTypeface)

        getQuestInfo()
        initAppBarLayout()
        initSwipeLayout()
    }

    override fun setContentView() {
        setContentView(R.layout.activity_quest_info)
    }

    override fun setActionBar() {
        setSupportActionBar(this@QuestInfoActivity.toolbar)
        val actionBar= supportActionBar
        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        this@QuestInfoActivity.toolbar?.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@QuestInfoActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        savedInstanceState ?: getIntentData()
        if (!isNetworkAvailable) {
            this@QuestInfoActivity.disconnect_container_quest_info.visibility = View.VISIBLE
            return
        }

        launchIOWork {
            questInfoViewModel.deleteQuestInfo()
        }

        window.sharedElementEnterTransition.addListener(sharedEnterListener)
        supportPostponeEnterTransition()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        if (resultCode == Caller.SELECTED_FEED_INFO_PHOTO_VIEW) {
            // Listener to reset shared element exit transition callbacks.
            window.sharedElementExitTransition.addListener(sharedExitListener)
            supportPostponeEnterTransition()

            val callback = PhotoViewerCallback()

            callback.setViewBinding(this@QuestInfoActivity.iv_quest_info_photo)
            setEnterSharedElementCallback(callback)
            supportStartPostponedEnterTransition()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_normal_item, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                @SuppressLint("PrivateApi")
                val method= menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", java.lang.Boolean.TYPE)
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
                        R.id.menu_share_facebook ->
                            callShareToFacebook(this@QuestInfoActivity.iv_quest_info_photo.drawable as BitmapDrawable)
                        R.id.menu_share_ect -> {
                            waterMarkHandler.putWatermark(this.applicationContext, workHandler,
                                (this@QuestInfoActivity.iv_quest_info_photo.drawable as BitmapDrawable).bitmap, title, description)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(EXTRA_QUEST_OWNER_NAME, ownerName)
        outState.putString(EXTRA_QUEST_OWNER_LOGO, ownerLogo)
        outState.putString(EXTRA_QUEST_OWNER_IMAGE, ownerImage)
        outState.putString(EXTRA_QUEST_TITLE, title)
        outState.putString(EXTRA_QUEST_DESCRIPTION, description)
        outState.putString(EXTRA_QUEST_STATE, questState)
        outState.putString(EXTRA_QUEST_REWARD, reward)
        outState.putString(EXTRA_QUEST_NUMBER_OF_CONTENTS, numberOfContents)
        outState.putString(EXTRA_QUEST_MEDIA_TYPE, mediaType)
        outState.putInt(EXTRA_QUEST_DURATION, duration)
        outState.putInt(EXTRA_QUEST_POSITION, position)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        ownerName = savedInstanceState.getString(EXTRA_QUEST_OWNER_NAME, "")
        ownerLogo = savedInstanceState.getString(EXTRA_QUEST_OWNER_LOGO, "")
        ownerImage = savedInstanceState.getString(EXTRA_QUEST_OWNER_IMAGE, "")
        title = savedInstanceState.getString(EXTRA_QUEST_TITLE, "")
        description = savedInstanceState.getString(EXTRA_QUEST_DESCRIPTION, "")
        questState = savedInstanceState.getString(EXTRA_QUEST_STATE, "")
        reward = savedInstanceState.getString(EXTRA_QUEST_REWARD, "")
        numberOfContents = savedInstanceState.getString(EXTRA_QUEST_NUMBER_OF_CONTENTS, "")
        mediaType = savedInstanceState.getString(EXTRA_QUEST_MEDIA_TYPE, "")
        duration = savedInstanceState.getInt(EXTRA_QUEST_DURATION, 0)
        position = savedInstanceState.getInt(EXTRA_QUEST_POSITION, 0)
    }

    override fun onBackPressed() {
        setActivityResult(calledFrom)

        super.onBackPressed()
    }

    override fun finishAfterTransition() {
        this@QuestInfoActivity.collapsing_layout.title = ""
        supportPostponeEnterTransition()
        setViewBind()
        setActivityResult(calledFrom)

        super.finishAfterTransition()
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()

        uiScope.cancel()
        defaultScope.cancel()
        ioScope.cancel()
        job.cancel()
    }

    private fun getIntentData() {
        ownerName = intent.getStringExtra(EXTRA_QUEST_OWNER_NAME)
        ownerLogo = intent.getStringExtra(EXTRA_QUEST_OWNER_LOGO)
        ownerImage = intent.getStringExtra(EXTRA_QUEST_OWNER_IMAGE)
        title = intent.getStringExtra(EXTRA_QUEST_TITLE)
        description = intent.getStringExtra(EXTRA_QUEST_DESCRIPTION)
        questState = intent.getStringExtra(EXTRA_QUEST_STATE)
        reward = intent.getStringExtra(EXTRA_QUEST_REWARD)
        numberOfContents = intent.getStringExtra(EXTRA_QUEST_NUMBER_OF_CONTENTS)
        mediaType = intent.getStringExtra(EXTRA_QUEST_MEDIA_TYPE)
        duration = intent.getIntExtra(EXTRA_QUEST_DURATION, -1)
        position = intent.getIntExtra(EXTRA_QUEST_POSITION, -1)
        calledFrom = intent.getIntExtra(EXTRA_QUEST_CALLED_FROM, -1)
    }

    private fun getQuestInfo() {
        when(mock) {
            @MockData
            true -> transactMockData()
            false -> transactRealData()
        }
    }

    @MockData
    private fun transactMockData() {
        val mission = QuestInfoDataSource()

        launchIOWork {
            mission.setQuest(position)
            questInfoViewModel.setQuestInfo(mission.getQuest()!!)
        }

        questInfoViewModel.getQuestInfo().observe(this, Observer {
            it?.let { questInfo ->
                this.questInfo = questInfo
                launchMainWork {
                    displayQuestInfo(questInfo)
                }
            }
        })
    }

    private fun transactRealData() {
        questInfoViewModel.setParameters(
            Parameters(
                "",
                -1,
                LOAD_FAVORITE_QUEST_INFO,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)

        questInfoViewModel.mission.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { questInfo ->
                        this.questInfo = questInfo as QuestInfo
                        launchMainWork {
                            displayQuestInfo(questInfo)
                        }
                    }

                    resource.getMessage()?.let {
                        showNetworkError(resource.errorCode)
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    showNetworkError(resource.errorCode)
                }

                else -> {
                    showNetworkError(resource.errorCode)
                }
            }
        })
    }

    private fun showNetworkError(errorCode: Int) {
        when(errorCode) {
            in 400..499 -> {
                Snackbar.make(this@QuestInfoActivity.coordinator_quest_info_layout, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(this@QuestInfoActivity.coordinator_quest_info_layout, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
            }
        }
    }

    private suspend fun displayQuestInfo(questInfo: QuestInfo) {
        val workQuestInfo = uiScope.async {
            loadQuestInfo()
        }

        val workExtra = uiScope.async {
            fillExtraQuestInfo(questInfo)
        }

        workQuestInfo.await()
        workExtra.await()
    }

    private fun loadQuestInfo() {
        setImageDraw(this.iv_quest_owner_logo, ownerLogo)
        this.iv_quest_info_photo.setColorFilter(getColor(R.color.colorQuestInfoMask),
            PorterDuff.Mode.DST_IN)
        setFixedImageSize(0, 0)
        setImageDraw(this.iv_quest_info_photo, this.backdrop_container, ownerImage, false)

        if (mediaType == "photo") this.iv_quest_type.setImageResource(R.drawable.ic_photo_size)
        else this.iv_quest_type.setImageResource(R.drawable.ic_video)

        showQuestState()

        withDelay(50L) {
            setViewBind()
            setEnterSharedElementCallback(sharedElementCallback)
            supportStartPostponedEnterTransition()
        }

        this.tv_quest_owner_name.text = ownerName

        this.appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
                _, verticalOffset ->
            if (this.collapsing_layout.height + verticalOffset < 2
                * ViewCompat.getMinimumHeight(this.collapsing_layout)) {
                // collapsed
                // this.iv_quest_info_photo.animate().alpha(1.0f).duration = 600
            } else {
                // extended
                // this.iv_quest_info_photo.animate().alpha(1.0f).duration = 1000    // 1.0f means opaque
            }
        })
    }

    private fun showQuestState() {
        when (questState) {
            SORT_QUEST_ONGOING -> showOngoingUI()
            SORT_QUEST_UNDER_EXAMINATION -> showExaminationUI()
            SORT_QUEST_CLOSED -> showClosedUI()
        }
    }

    private fun showOngoingUI() {
        this.tv_quest_state.visibility = View.GONE
        this.iv_quest_winner_crown.visibility = View.GONE
        this.iv_quest_info_photo.alpha = 1f
    }


    private fun showExaminationUI() {
        this.iv_quest_winner_crown.visibility = View.GONE
        this.tv_quest_state.visibility = View.VISIBLE
        this.tv_quest_state.text = getString(R.string.quest_state_examination_kr)
    }

    private fun showClosedUI() {
        this.tv_quest_state.visibility = View.VISIBLE
        this.tv_quest_state.text = getString(R.string.quest_winner_eng)
        this.iv_quest_winner_crown.visibility = View.VISIBLE
        setTextViewGradient(this, this.tv_quest_state)
    }

    private fun fillExtraQuestInfo(questInfo: QuestInfo) {
        fillExtraWithText(questInfo)
        createAdapter(questInfo)
    }

    private fun createAdapter(questInfo: QuestInfo) {
        this.rv_quest_inspiration.setHasFixedSize(false)
        this.rv_quest_inspiration.setItemViewCacheSize(20)
        this.rv_quest_inspiration.isVerticalScrollBarEnabled = false

        val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        this.rv_quest_inspiration.layoutManager = gridLayoutManager
        this.rv_quest_inspiration.addItemDecoration(createItemDecoration())

        gridLayoutManager.isItemPrefetchEnabled = true
        val adapter = QuestInspirationAdapter(this)
        this.rv_quest_inspiration.adapter = adapter

        adapter.addList(questInfo.photos.photo!!)
    }

    private fun createItemDecoration(): RecyclerView.ItemDecoration {
        return object : GapItemDecoration(VERTICAL_LIST, resources.getDimensionPixelSize(R.dimen.space_4)) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = 4
                outRect.right = 4
                outRect.bottom = 4
                outRect.top = 4
            }
        }
    }

    private fun fillExtraWithText(questInfo: QuestInfo) {
        setFontTypeface(this.tv_quest_info_explanation, FONT_TYPE_REGULAR)
        setFontTypeface(this.tv_quest_info_rules_title, FONT_TYPE_REGULAR)
        setFontTypeface(this.tv_quest_info_rules_first_rule, FONT_TYPE_REGULAR)
        setFontTypeface(this.tv_quest_info_rules_second_rule, FONT_TYPE_REGULAR)
        setFontTypeface(this.tv_quest_info_bonus_rules_title, FONT_TYPE_REGULAR)
        setFontTypeface(this.tv_quest_info_bonus_first_rule, FONT_TYPE_REGULAR)
        setFontTypeface(this.tv_quest_info_bonus_second_rule, FONT_TYPE_REGULAR)
        setFontTypeface(this.tv_quest_info_important_title, FONT_TYPE_REGULAR)
        setFontTypeface(this.tv_quest_info_important_notice, FONT_TYPE_REGULAR)
        setFontTypeface(this.tv_quest_contents, FONT_TYPE_MEDIUM)
        setFontTypeface(this.tv_quest_type, FONT_TYPE_MEDIUM)
        setFontTypeface(this.tv_duration, FONT_TYPE_MEDIUM)
        setFontTypeface(this.tv_quest_reward_price, FONT_TYPE_MEDIUM)
        this.tv_quest_info_explanation.text = description
        this.tv_quest_contents.text = numberOfContents
        this.tv_quest_type.text = mediaType
        this.tv_duration.text = if (duration == 0) "종료" else (getString(R.string.snap_quest_duration_day_phrase) + duration)

        this.tv_quest_reward_price.text = "$ $reward"
        this.tv_quest_info_rules_title.text = questInfo.rules.title
        this.tv_quest_info_rules_first_rule.text = questInfo.rules.firstRule
        this.tv_quest_info_rules_second_rule.text = questInfo.rules.secondRule
        this.tv_quest_info_bonus_rules_title.text = questInfo.bonus.title
        this.tv_quest_info_bonus_first_rule.text = questInfo.bonus.firstBonus
        this.tv_quest_info_bonus_second_rule.text = questInfo.bonus.secondBonus
        this.tv_quest_info_important_title.text = this.applicationContext.getString(R.string.phrase_important_notice)
        this.tv_quest_info_important_notice.text = questInfo.importantNotice
    }

    private fun initAppBarLayout() {
        this@QuestInfoActivity.appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
                appBarLayout, verticalOffset ->
            this@QuestInfoActivity.collapsing_layout.title = title
            when {
                abs(verticalOffset) == appBarLayout.totalScrollRange -> {
                    isAppBarLayoutCollapsed = true
                    isAppBarLayoutExpanded = false
                }
                verticalOffset == 0 -> {
                    isAppBarLayoutExpanded = true
                    launchWork {
                        delay(DELAY_COLLAPSED_TIMER_INTERVAL.toLong())
                        isAppBarLayoutCollapsed = false
                    }
                }
                else -> {
                    isAppBarLayoutExpanded = false
                    isAppBarLayoutCollapsed = true
                }
            }
        })
    }

    private fun initSwipeLayout() {
        this@QuestInfoActivity.coordinator_quest_info_layout.setOnSwipeOutListener(this, object : OnSwipeOutListener {
            override fun onSwipeLeft(x: Float, y: Float) {
                Timber.d("onSwipeLeft")

                finishAfterTransition()
            }

            override fun onSwipeRight(x: Float, y: Float) {
                Timber.d("onSwipeRight")

            }

            override fun onSwipeDown(x: Float, y: Float) {
                Timber.d( "onSwipeDown")

                if (!isAppBarLayoutCollapsed && isAppBarLayoutExpanded) {
                    finishAfterTransition()
                }
            }

            override fun onSwipeUp(x: Float, y: Float) {
                Timber.d("onSwipeUp")
            }

            override fun onSwipeDone() {
                Timber.d("onSwipeDone")
            }
        })
    }

    private fun callShareToFacebook(drawable: BitmapDrawable) {
        workHandler.shareToFacebook(drawable.bitmap, this)
    }

    private fun setViewBind() {
        this.iv_quest_info_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
        this.iv_quest_owner_logo.transitionName = TransitionObject.TRANSITION_NAME_FOR_LOGO + position
        this.tv_quest_info_explanation.transitionName = TransitionObject.TRANSITION_NAME_FOR_EXPLANATION + position
        this.tv_quest_owner_name.transitionName = TransitionObject.TRANSITION_NAME_FOR_OWNER_NAME + position
        sharedElementCallback = QuestInfoCallback(intent)
        sharedElementCallback.setViewBinding(this.iv_quest_info_photo, this.iv_quest_owner_logo,
            this.tv_quest_info_explanation, this.tv_quest_owner_name)
    }

    private fun setActivityResult(calledFrom: Int) {
        when(calledFrom) {
            CALLED_FROM_HOME_MAIN_QUEST -> {
                val intent = Intent(this, HomeActivity::class.java)

                intent.putExtra(EXTRA_QUEST_POSITION, position)
                setResult(Caller.SELECTED_QUEST_INFO_ITEM_FROM_HOME_MAIN_POSITION, intent)
            }

            CALLED_FROM_HOME_BEST_PICK_QUEST -> {
                val intent = Intent(this, HomeActivity::class.java)

                intent.putExtra(EXTRA_QUEST_POSITION, position)
                setResult(Caller.SELECTED_BEST_PICK_QUEST_POSITION, intent)
            }

            CALLED_FORM_HOME_HOT_QUEST -> {
                intent.putExtra(EXTRA_QUEST_POSITION, position)
                setResult(Caller.SELECTED_QUEST_INFO_ITEM_FROM_HOT_QUEST_POSITION, intent)
            }

            CALLED_FORM_HOME_FAVORITE_QUEST -> {
                intent.putExtra(EXTRA_QUEST_POSITION, position)
                setResult(Caller.SELECTED_QUEST_INFO_ITEM_FROM_FAVORITE_QUEST_POSITION, intent)
            }

            else -> {}
        }
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the MainScope.
     *              lambda the some work will do
     */
    private inline fun launchMainWork(crossinline block: suspend () -> Unit): Job {
        return uiScope.launch {
            block()
        }
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the defaultScope.
     *              lambda the some work will do
     */
    private inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return defaultScope.launch {
            block()
        }
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the ioScope.
     *              lambda the some work will do
     */
    private inline fun launchIOWork(crossinline block: suspend () -> Unit): Job {
        return ioScope.launch {
            block()
        }
    }
}