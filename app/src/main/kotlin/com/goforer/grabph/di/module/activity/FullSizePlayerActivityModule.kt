package com.goforer.grabph.di.module.activity

import com.goforer.grabph.presentation.ui.player.FullSizePlayerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FullSizePlayerActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeFullSizePlayerActivity(): FullSizePlayerActivity
}