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

package com.goforer.grabph.presentation.common.utils.cache

import com.bumptech.glide.load.Key

import java.nio.ByteBuffer
import java.security.MessageDigest

class IntegerVersionSignature(private val currentVersion: Int): Key {
    override fun equals(any: Any?): Boolean {
        if (any is IntegerVersionSignature) {
            val other = any as IntegerVersionSignature?
            return currentVersion == other!!.currentVersion
        }

        return false
    }

    override fun hashCode(): Int {
        return currentVersion
    }

    override fun updateDiskCacheKey(md: MessageDigest) {
        md.update(ByteBuffer.allocate(Integer.SIZE).putInt(currentVersion).array())
    }
}