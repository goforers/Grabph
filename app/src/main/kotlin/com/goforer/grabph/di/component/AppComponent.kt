/*
 * Copyright 2019 Lukoh Nam, goForer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.goforer.grabph.di.component

import android.app.Application
import com.goforer.grabph.Grabph
import com.goforer.grabph.di.module.AppModule
import com.goforer.grabph.di.module.activity.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
                AndroidSupportInjectionModule::class, AppModule::class,
                FeedItemActivityModule::class, PhotoViewerActivityModule::class,
                FeedSearchActivityModule::class, PhotogPhotoActivityModule::class,
                PhotoInfoActivityModule::class, CommentActivityModule::class,
                QuestInfoActivityModule::class, PinnedFeedsActivityModule::class,
                HomeActivityModule::class, CategoryPhotoActivityModule::class,
                SearpleGalleryActivityModule::class, PeopleActivityModule::class,
                CategoryActivityModule::class, HotTopicContentActivityModule::class,
                RankingActivityModule::class, OthersProfileActivityModule::class
            ])

interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: Application): Builder

        fun build(): AppComponent
    }

    fun inject(grabph: Grabph)
}
