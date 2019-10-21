/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.goforer.grabph.presentation.common.utils.handler.exif

import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.EXIF
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EXIFHandler @Inject constructor() {
    private var exifNoneItemCount: Int = 0

    fun putManualEXIF(label: String): EXIF {
        val raw = EXIF.Raw("None")

        exifNoneItemCount++

        return EXIF("", "", label, raw)
    }

    fun getEXIFByLabel(listEXIF: List<EXIF>, label: String): EXIF? {
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            return if (listEXIF.stream()
                            .filter { (_, _, label1) -> label == label1 }
                            .findFirst()
                            .orElse(null) == null) {
                null
            } else {
                listEXIF.stream()
                        .filter { (_, _, label1) -> label == label1 }
                        .findFirst()
                        .orElse(null)
            }
        } else {
            for (exif in listEXIF) {
                if (label == exif.label) {
                    return exif
                }
            }

            return null
        }
    }

    fun getEXIFNoneItemCount(): Int {
        return exifNoneItemCount
    }

    fun resetEXIFNoneItemCount() {
        exifNoneItemCount = 0
    }
}