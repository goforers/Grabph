package com.goforer.grabph.di.module.activity

import com.goforer.grabph.presentation.ui.feed.feedinfo.FeedItemActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FeedItemActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeFeedItemActivity(): FeedItemActivity
}