package com.goforer.grabph.di.module.fragment

import com.goforer.grabph.presentation.ui.uploadquest.fragment.QuestDescFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class QuestDescFragmentBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeQuestDescFragment(): QuestDescFragment
}