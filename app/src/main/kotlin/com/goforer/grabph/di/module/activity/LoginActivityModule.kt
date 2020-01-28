package com.goforer.grabph.di.module.activity

import com.goforer.grabph.di.module.fragment.*
import com.goforer.grabph.presentation.ui.login.LogInActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LoginActivityModule {
    @ContributesAndroidInjector(modules = [
        SignInFragmentBuilderModule::class, SignUpFragmentBuilderModule::class,
        ResetPasswordFragmentBuilderModule::class])
    internal abstract fun contributeLoginActivity(): LogInActivity
}