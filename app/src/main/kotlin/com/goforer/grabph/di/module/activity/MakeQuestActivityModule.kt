package com.goforer.grabph.di.module.activity

import com.goforer.grabph.presentation.ui.makequest.MakeQuestActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MakeQuestActivityModule {
    @ContributesAndroidInjector()
    internal abstract fun contributeMakeQuestActivity(): MakeQuestActivity
}