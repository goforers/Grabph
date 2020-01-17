package com.goforer.grabph.di.module.fragment

import com.goforer.grabph.presentation.ui.othersprofile.fragments.OthersProfilePinFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OthersProfilePinFragmentBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeOthersProfilePinFragment(): OthersProfilePinFragment
}