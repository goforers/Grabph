package com.goforer.grabph.di.module.activity

import com.goforer.grabph.presentation.ui.uploadphoto.AuthActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthActivityModule {
    @ContributesAndroidInjector()
    internal abstract fun contributeAuthActivity(): AuthActivity
}
