/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.goforer.grabph

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.*
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.facebook.FacebookSdk
import com.facebook.FacebookSdk.getApplicationContext
import com.facebook.appevents.AppEventsLogger
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.utils.CommonUtils.getVersionNumber
import com.goforer.grabph.di.helper.AppInjector
import com.goforer.grabph.presentation.common.utils.cache.IntegerVersionSignature
import com.goforer.grabph.presentation.common.view.drawer.loader.AbstractSlidingDrawerImageLoader
import com.goforer.grabph.presentation.common.view.drawer.loader.SlidingDrawerImageLoader
import com.goforer.grabph.presentation.ui.splash.SplashActivity
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

class Grabph: Application(), LifecycleObserver, HasAndroidInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    companion object {
        private var appMemoryStatus = AppMemoryStatus.SUFFICIENT_MEMORY

        private var appScreenStatus = AppScreenStatus.SCREEN_ON

        internal lateinit var splashUrl: String

        private const val PROFILE_PIC_HEIGHT = 48
        private const val PROFILE_PIC_WIDTH = 48
        private const val GRABPH_PENDING_ID = 10000

        fun restartApp() {
            val intent = Intent(getApplicationContext(), SplashActivity::class.java)
            val pendingIntentId = GRABPH_PENDING_ID

            val mPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    pendingIntentId, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT)
            val alarmManager = getApplicationContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            exit()
        }

        fun exitApplication(activity: Activity) {
            ActivityCompat.finishAffinity(activity)
            exit()
        }

        private fun exit() {
            android.os.Process.killProcess(android.os.Process.myPid())
            System.runFinalizersOnExit(true)
            exitProcess(0)
        }
    }

    override fun onCreate() {
        Timber.e("onCreate Crashed!!")
        super.onCreate()

        splashUrl = GeneralFunctions.getSplashBackgroundUrl()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerComponentCallbacks(GrabphComponentCallback(this))
        checkForScreenTurningOn(this)
        checkForScreenTurningOff(this)

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        AppInjector.init(this)

        //initialize and create the image loader logic
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView?, uri: Uri?, placeholder: Drawable?, tag: String) {
                val options = RequestOptions()
                        .override(PROFILE_PIC_WIDTH, PROFILE_PIC_HEIGHT)
                        .placeholder(placeholder)

                Glide.with(applicationContext).asBitmap().load(uri)
                        .apply(options)
                        .apply(RequestOptions.fitCenterTransform())
                        .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .apply(RequestOptions().signature(IntegerVersionSignature(getVersionNumber())))
                        .thumbnail(0.5f)
                        .listener(object: RequestListener<Bitmap> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                                      isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                         dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                imageView?.setImageBitmap(resource)

                                return false
                            }
                        })
                        .submit()
            }

            override fun cancel(imageView: ImageView?) {
                Glide.with(applicationContext).clear(imageView!!)
            }

            override fun placeholder(context: Context, tag: String?): Drawable {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                return when (tag) {
                    DrawerImageLoader.Tags.PROFILE.name -> DrawerUIUtils.getPlaceHolder(context)
                    DrawerImageLoader.Tags.ACCOUNT_HEADER.name -> IconicsDrawable(context).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56)
                    "customUrlItem" -> IconicsDrawable(context).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56)

                    //we use the default one for
                    //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()
                    else -> super.placeholder(context, tag)
                }

            }
        })

        SlidingDrawerImageLoader.init(object : AbstractSlidingDrawerImageLoader() {
            @SuppressLint("NewApi", "CheckResult")
            override fun set(imageView: AppCompatImageView, uri: Uri, placeholder: Drawable) {
                val options = RequestOptions
                        .fitCenterTransform()
                        .override(PROFILE_PIC_WIDTH, PROFILE_PIC_HEIGHT)
                        .placeholder(placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(IntegerVersionSignature(getVersionNumber()))


                Glide.with(applicationContext).asBitmap().load(uri)
                        .apply(options)
                        .thumbnail(0.5f)
                        .listener(object: RequestListener<Bitmap> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                                                      isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                                         dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                imageView.setImageBitmap(resource)

                                return false
                            }
                        })
                        .submit()
            }

            override fun cancel(imageView: AppCompatImageView) {
                Glide.with(applicationContext).clear(imageView)
            }

        })
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        MultiDex.install(this)
    }

    override fun androidInjector() = dispatchingAndroidInjector

    private fun checkForScreenTurningOff(grabphApp: Grabph) {
        val screenStateFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                unregisterComponentCallbacks(GrabphComponentCallback(grabphApp))
                appScreenStatus = AppScreenStatus.SCREEN_OFF
            }
        }, screenStateFilter)
    }

    private fun checkForScreenTurningOn(grabphApp: Grabph) {
        val screenStateFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                registerComponentCallbacks(GrabphComponentCallback(grabphApp))
                appScreenStatus = AppScreenStatus.SCREEN_ON
            }
        }, screenStateFilter)
    }

    class GrabphComponentCallback(private val grabphApp: Grabph): ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration?) {}

        override fun onTrimMemory(level: Int) {
            Glide.get(grabphApp).trimMemory(level)
            appMemoryStatus = when {
                level == TRIM_MEMORY_UI_HIDDEN -> {
                    AppMemoryStatus.UI_HIDDEN_MEMORY
                }

                level >= TRIM_MEMORY_COMPLETE -> {
                    AppMemoryStatus.LOW_MEMORY
                }

                level in TRIM_MEMORY_COMPLETE downTo TRIM_MEMORY_MODERATE -> {
                    AppMemoryStatus.MODERATE_MEMORY
                }

                else -> {
                    AppMemoryStatus.SUFFICIENT_MEMORY
                }
            }
        }

        override fun onLowMemory() {
            Timber.d("onLowMemory")

            Glide.get(grabphApp).clearMemory()
            appMemoryStatus = AppMemoryStatus.LOW_MEMORY
        }
    }

    enum class AppMemoryStatus {
        LOW_MEMORY,
        UI_HIDDEN_MEMORY,
        MODERATE_MEMORY,
        SUFFICIENT_MEMORY
    }

    enum class AppScreenStatus {
        SCREEN_ON,
        SCREEN_OFF
    }
}