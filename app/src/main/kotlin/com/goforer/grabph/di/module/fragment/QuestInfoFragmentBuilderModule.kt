package com.goforer.grabph.di.module.fragment

import com.goforer.grabph.presentation.ui.uploadquest.fragment.QuestInfoFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class QuestInfoFragmentBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeQuestInfoFragment(): QuestInfoFragment
}