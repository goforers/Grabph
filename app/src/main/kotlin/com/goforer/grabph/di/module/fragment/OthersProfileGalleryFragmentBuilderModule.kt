package com.goforer.grabph.di.module.fragment

import com.goforer.grabph.presentation.ui.othersprofile.fragments.OthersProfileGalleryFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OthersProfileGalleryFragmentBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeOthersProfileGalleryFragment(): OthersProfileGalleryFragment
}