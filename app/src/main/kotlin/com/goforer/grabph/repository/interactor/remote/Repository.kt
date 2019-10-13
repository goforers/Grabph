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

package com.goforer.grabph.repository.interactor.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.presentation.common.utils.RateLimiter
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.network.api.SearpService
import com.goforer.grabph.repository.network.response.Resource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
abstract class Repository<T> {
    @field:Inject lateinit var searpService: SearpService

    abstract suspend fun load(liveData: MutableLiveData<T>, parameters: Parameters): LiveData<Resource>

    companion object {
        const val KEY = "6d00237df3d2cefa70ea2c68579f9c0b"//"0214354eb90a5f1be88a9658fb66ec52"
        const val FORMAT_JSON = "json"
        const val PER_PAGE = 20
        const val MISSION_PER_PAGE = 10
        const val INDEX = 1

        const val BOUND_TO_BACKEND = 0
        const val BOUND_FROM_BACKEND = 1
        const val BOUND_FROM_LOCAL = 2

        const val LANG_KOREAN = "ko-kr"
        const val LANG_ENGLISH = "en-us"
        const val LANG_GERMAN = "de-de"
        const val LANG_SPANISH = "es-us"
        const val LANG_FRANCE = "fr-fr"
        const val LANG_ITALY = "it-it"

        const val FEED_ITEM_CACHE_SIZE = 10000
        const val FEED_ITEM_LAST_SEEN_COUNT = 1000

        var repoRateLimit = RateLimiter<String>(RateLimiter.RATE_LIMIT_FETCH_DATA, TimeUnit.MINUTES)
    }
}