package com.goforer.grabph.di.module.fragment

import com.goforer.grabph.presentation.ui.login.fragments.SignUpFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SignUpFragmentBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeSignUpFragment(): SignUpFragment
}