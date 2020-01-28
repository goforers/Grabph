package com.goforer.grabph.di.module.fragment

import com.goforer.grabph.presentation.ui.login.fragments.ResetPasswordFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ResetPasswordFragmentBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeResetPasswordFragment(): ResetPasswordFragment
}