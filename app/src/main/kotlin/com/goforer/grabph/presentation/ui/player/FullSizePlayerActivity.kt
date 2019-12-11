package com.goforer.grabph.presentation.ui.player

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.EXTRA_VIDEO_SOURCE_URL
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.activity_full_size_player.*

class FullSizePlayerActivity : BaseActivity() {

    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var playWhenReady = false

    private var player: SimpleExoPlayer? = null
    private var videoUrl: String? = null

    private lateinit var playerView: PlayerView

    companion object {
        const val EXTRA_PLAYER_POSITION = "fullsize_player_position"
        const val EXTRA_PLAY_WHEN_READY = "fullsize_play_when_ready"
    }

    override fun setContentView() { setContentView(R.layout.activity_full_size_player) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playerView = this.video_view_fullsize
        getIntentData(savedInstanceState )
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Build.VERSION.SDK_INT < 24) {
            videoUrl?.let { initializePlayer() }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= 24) {
            videoUrl?.let { initializePlayer() }
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        videoUrl?.let { url ->
            outState.putString(EXTRA_VIDEO_SOURCE_URL, url)
            outState.putBoolean(EXTRA_PLAY_WHEN_READY, true)
            outState.putLong(EXTRA_PLAYER_POSITION, player?.currentPosition ?: playbackPosition)
        }
    }

    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector()
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())

        player = ExoPlayerFactory.newSimpleInstance(this)
        playerView.player = player

        val uri = Uri.parse(videoUrl)
        val mediaSource = buildMediaSource(uri)

        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare(mediaSource, false, false)
        player?.volume = 0f
    }

    private fun releasePlayer() {
        player?.let {
            currentWindow = it.currentWindowIndex
            playbackPosition = it.currentPosition

            it.release()
            player = null
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this, "exoplayer_searp")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun hideSystemUi() {
        playerView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private fun getIntentData(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            videoUrl = intent.getStringExtra(EXTRA_VIDEO_SOURCE_URL)
        } else {
            videoUrl = savedInstanceState.getString(EXTRA_VIDEO_SOURCE_URL)
            playbackPosition = savedInstanceState.getLong(EXTRA_PLAYER_POSITION)
            playWhenReady = savedInstanceState.getBoolean(EXTRA_PLAY_WHEN_READY)
        }
    }
}
