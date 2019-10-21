/*
 * Copyright 2019 Lukoh Nam, goForer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goforer.grabph.data.datasource.model.cache.data.entity.media

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.goforer.base.presentation.model.BaseModel

data class Media(@field:ColumnInfo(name = "type") val type: String,
                 @field:Embedded(prefix = "urls") val urls: Urls): BaseModel() {
    data class Urls(@field:ColumnInfo(name = "full") val full: String?,
                    @field:ColumnInfo(name = "regular") val regular: String?,
                    @field:ColumnInfo(name = "small") val small: String?,
                    @field:ColumnInfo(name = "thumb") val thumb: String?): BaseModel()
}