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

package com.goforer.grabph.data.datasource.model.cache.data

import androidx.lifecycle.MutableLiveData

/**
 * A LiveData class that has `null` value.
 */
class AbsentLiveData<T> private constructor() : MutableLiveData<T>() {
    init {
        postValue(null)
    }

    companion object {
        fun <T> create(): MutableLiveData<T> {
            return AbsentLiveData()
        }
    }
}