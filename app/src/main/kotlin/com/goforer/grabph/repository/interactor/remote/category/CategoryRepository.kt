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

package com.goforer.grabph.repository.interactor.remote.category

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.presentation.vm.category.CategoryViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.category.Category
import com.goforer.grabph.repository.model.cache.data.entity.category.Categoryg
import com.goforer.grabph.repository.model.dao.remote.category.CategoryDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.paging.boundarycallback.PagedListCategoryBoundaryCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository
@Inject
constructor(private val dao: CategoryDao): Repository() {
    companion object {
        const val METHOD = "searp.category.getList"
    }

    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int,
                              boundType: Int, calledFrom: Int): LiveData<Resource> {
        return object : NetworkBoundResource<MutableList<Category>, PagedList<Category>, Categoryg>(loadType, boundType) {
            override suspend fun saveToCache(item:  MutableList<Category>) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int,
                                               pages: Int): LiveData<PagedList<Category>> {
                val config = PagedList.Config.Builder()
                        .setInitialLoadSizeHint(10)
                        .setPageSize(itemCount)
                        .setPrefetchDistance(5)
                        .setEnablePlaceholders(true)
                        .build()

                return withContext(Dispatchers.IO) {
                    if (isLatest) {
                        LivePagedListBuilder(dao.getLatestCategories(itemCount), /* PageList Config */ config)
                                .setBoundaryCallback(PagedListCategoryBoundaryCallback<Category>(
                                        viewModel as CategoryViewModel, query1, pages, calledFrom)).build()
                    } else {
                        LivePagedListBuilder(dao.getCategories(), /* PageList Config */ config)
                                .setBoundaryCallback(PagedListCategoryBoundaryCallback<Category>(
                                        viewModel as CategoryViewModel, query1, pages, calledFrom)).build()
                    }
                }
            }

            override suspend fun loadFromNetwork() = searpService.getCategories(KEY, METHOD, FORMAT_JSON, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(query1)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    @MockData
    internal fun loadCategories(): LiveData<List<Category>> = dao.getCategoryList()

    @WorkerThread
    @MockData
    internal suspend fun setCategory(categories: MutableList<Category>) = insert(categories)

    @WorkerThread
    internal fun deleteCategory() = delete()

    @MockData
    internal suspend fun insert(categories: MutableList<Category>) = dao.insert(categories)

    internal fun delete() = dao.clearAll()
}