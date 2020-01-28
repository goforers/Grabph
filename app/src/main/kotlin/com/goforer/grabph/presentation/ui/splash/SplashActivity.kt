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

package com.goforer.grabph.presentation.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.facebook.AccessToken
import com.goforer.base.presentation.utils.CommonUtils.getVersionNumber
import com.goforer.base.presentation.utils.DisplayUtils
import com.goforer.base.presentation.utils.SharedPreference
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.Grabph
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.utils.cache.IntegerVersionSignature
import com.goforer.grabph.presentation.ui.login.LogInActivity.Companion.SNS_NAME_FACEBOOK
import com.goforer.grabph.presentation.ui.login.LogInActivity.Companion.SNS_NAME_GOOGLE
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.*

@SuppressLint("Registered")
class SplashActivity : BaseActivity() {
    private var splashStart: Long = 0

    private var auth: FirebaseAuth? = null

    private var accessToken: AccessToken? = null

    private val job = Job()

    private val defaultScope = CoroutineScope(Dispatchers.Default + job)

    private val permissions = arrayOf(Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)

    companion object {
        private const val MIN_SPLASH_TIME = 1500
        private const val REQUEST_ID_MULTIPLE_PERMISSIONS = 1

        fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && context != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }

            return true
        }
    }

    override fun setContentView() {
        setContentView(R.layout.activity_splash)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // if (!isNetworkAvailable) {
        //     this@SplashActivity.iv_background.visibility = View.GONE
        //     this@SplashActivity.disconnect_container.visibility = View.VISIBLE
        //
        //     return
        // }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT

        /* *************************************
         *              GOOGLE                 *
         ***************************************/
        auth = FirebaseAuth.getInstance()

        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        accessToken = AccessToken.getCurrentAccessToken()

        DisplayUtils.setSlidingDrawerWidth(DisplayUtils.getSlidingDrawerWidth(this))

        splashStart = System.currentTimeMillis()

        setFontTypeface()
    }

    override fun onResume() {
        super.onResume()

        if (!isNetworkAvailable) {
            return
        }

        if (!hasPermissions(this, *permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_ID_MULTIPLE_PERMISSIONS)
        } else {
            onWait(SharedPreference.getSharedPreferenceSocialLogin(this))
        }
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()

        defaultScope.cancel()
        job.cancel()
    }

    @SuppressLint("CheckResult")
    override fun setViews(savedInstanceState: Bundle?) {
        Glide.with(this)
            .asBitmap()
            .load(Grabph.splashUrl)
            .apply(RequestOptions.fitCenterTransform())
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .apply(RequestOptions().signature(IntegerVersionSignature(getVersionNumber())))
            .listener(object: RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                    isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                    dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    val set = ConstraintSet()
                    val ratio = String.format("%d:%d", resource?.width, resource?.height)

                    set.clone(splashConstraintLayoutContainer)
                    set.setDimensionRatio(iv_background.id, ratio)
                    set.applyTo(splashConstraintLayoutContainer)
                    iv_background.setImageBitmap(resource)

                    return false
                }
            })
        // .submit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Note: If request is cancelled, the result arrays are empty.
        if (grantResults.isNotEmpty()) {
            when (requestCode) {
                REQUEST_ID_MULTIPLE_PERMISSIONS -> {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        onWait(SharedPreference.getSharedPreferenceSocialLogin(this))
                    }
                }
            }
        } else {
            Toast.makeText(applicationContext, getString(R.string.phrase_permission_cancel), Toast.LENGTH_SHORT).show()
            launchWork {
                delay(Toast.LENGTH_SHORT.toLong())
                this.close()
            }
        }
    }

    private fun setFontTypeface() {
        val krRegularTypeface = Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_MEDIUM)

        tv_copyrights_splash.typeface = krRegularTypeface
    }

    private fun onWait(snsName: String) = launchWork {
        val elapsed = System.currentTimeMillis() - splashStart
        val moreSplash = if (MIN_SPLASH_TIME <= elapsed) 0 else MIN_SPLASH_TIME - elapsed

        delay(moreSplash)
        when(snsName) {
            SNS_NAME_FACEBOOK -> {
                accessToken ?: moveToLogIn()
                accessToken?.let {
                    goToHome()

                    return@launchWork
                }
            }

            SNS_NAME_GOOGLE -> {
                auth?.currentUser ?: moveToLogIn()
                auth?.currentUser?.let {
                    goToHome()

                    return@launchWork
                }
            }

            else -> {
                moveToLogIn()
                // goToHome()

                return@launchWork
            }
        }
    }

    private fun moveToLogIn() {
        Caller.callLogIn(this)
        this@SplashActivity.finish()
    }

    private fun goToHome() {
        Caller.callHome(this)
        this@SplashActivity.finish()
    }

    private fun close() {
        Grabph.exitApplication(this)
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
}