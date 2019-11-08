package com.goforer.grabph.di.module.fragment

import com.goforer.grabph.presentation.ui.home.profile.fragment.pin.HomeProfilePinFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeProfilePinFragmentBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeHomeProfilePinFragment(): HomeProfilePinFragment
}