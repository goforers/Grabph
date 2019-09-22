package com.goforer.grabph.di.module.activity

import com.goforer.grabph.presentation.ui.categoryphoto.CategoryPhotoActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CategoryPhotoActivityModule {
    @ContributesAndroidInjector()
    internal abstract fun contributeCategoryPhotoActivity(): CategoryPhotoActivity
}