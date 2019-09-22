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

package com.goforer.base.presentation.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE

object SharedPreference {
    fun saveSharePreferenceItemPosition(context: Context, itemPosition: Int) {
        val editor = context.getSharedPreferences("pref_item", MODE_PRIVATE).edit()

        editor.putInt("item_position", itemPosition)
        editor.apply()
    }

    fun saveSharePreferenceTabPosition(context: Context, tabPosition: Int) {
        val editor = context.getSharedPreferences("pref_tab", MODE_PRIVATE).edit()

        editor.putInt("tab_position", tabPosition)
        editor.apply()
    }

    fun saveSharePreferenceSocialLogin(context: Context, snsName: String) {
        val editor = context.getSharedPreferences("pref_login", MODE_PRIVATE).edit()

        editor.putString("pref_login", snsName)
        editor.apply()
    }

    fun getSharedPreferenceItemPosition(context: Context): Int {
        val pref = context.getSharedPreferences("pref_item", MODE_PRIVATE)

        return pref.getInt("item_position", 0)
    }

    fun getSharedPreferenceTabPosition(context: Context): Int {
        val pref = context.getSharedPreferences("pref_tab", MODE_PRIVATE)

        return pref.getInt("tab_position", 0)
    }

    fun getSharedPreferenceSocialLogin(context: Context): String {
        val pref = context.getSharedPreferences("pref_login", MODE_PRIVATE)

        return pref.getString("pref_login", "").toString()
    }
}