package com.goforer.grabph.di.module.fragment

import com.goforer.grabph.presentation.ui.login.fragments.SignInFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SignInFragmentBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeSignInFragment(): SignInFragment
}