package com.goforer.grabph.di.module.fragment

import com.goforer.grabph.presentation.ui.uploadquest.fragment.QuestPhotosFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class QuestPhotosFragmentBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeQuestPhotosFragment(): QuestPhotosFragment
}