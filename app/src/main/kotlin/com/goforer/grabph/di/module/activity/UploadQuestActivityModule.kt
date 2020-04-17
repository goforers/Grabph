package com.goforer.grabph.di.module.activity

import com.goforer.grabph.di.module.fragment.QuestDescFragmentBuilderModule
import com.goforer.grabph.di.module.fragment.QuestInfoFragmentBuilderModule
import com.goforer.grabph.di.module.fragment.QuestPhotosFragmentBuilderModule
import com.goforer.grabph.presentation.ui.uploadquest.UploadQuestActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UploadQuestActivityModule {
    @ContributesAndroidInjector(modules = [
    QuestInfoFragmentBuilderModule::class, QuestPhotosFragmentBuilderModule::class,
    QuestDescFragmentBuilderModule::class])
    internal abstract fun contributeUploadQuestActivity(): UploadQuestActivity
}