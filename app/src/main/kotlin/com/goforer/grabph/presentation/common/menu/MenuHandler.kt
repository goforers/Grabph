/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.goforer.grabph.presentation.common.menu

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import androidx.appcompat.widget.PopupMenu
import com.goforer.grabph.presentation.common.typefacespan.CustomTypeFaceSpan

class MenuHandler {
    internal fun applyFontToMenuItem(popup: PopupMenu, font: Typeface, foregroundColor: Int) {
        val menu = popup.menu

        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val title = SpannableString(item.title)

            title.setSpan(CustomTypeFaceSpan("", font, foregroundColor), 0, title.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            item.title = title
        }
    }
}