package com.goforer.grabph.di.module.activity

import com.goforer.grabph.presentation.ui.upload.UploadPhotoActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UploadPhotoActivityModule {
    @ContributesAndroidInjector()
    internal abstract fun contributeUploadPhotoActivity(): UploadPhotoActivity
}