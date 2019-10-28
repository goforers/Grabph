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

    fun hasAccessToken(context: Context): Boolean {
        val pref = context.getSharedPreferences("pref_login", MODE_PRIVATE)
        val token = pref.getString(SHARED_PREF_ACCESS_TOKEN, "null") ?: "null"
        return token != "null"
    }

    /**
     * SharedPreferences Key should be modified to unique value like User's Id.
     * */
    fun saveAccessToken(context: Context, token: String, secret: String, userId: String) {
        val edit = context.getSharedPreferences("pref_login", MODE_PRIVATE).edit()
        edit.putString(SHARED_PREF_ACCESS_TOKEN, token)
        edit.putString(SHARED_PREF_ACCESS_TOKEN_SECRET, secret)
        edit.putString(SHARED_PREF_FLICKR_USER_ID, userId)
        edit.apply()
    }

    fun removeAllTokenInfo(context: Context) {
        val edit = context.getSharedPreferences("pref_login", MODE_PRIVATE).edit()
        edit.clear()
        edit.apply()
    }

    fun getAccessToken(context: Context): String {
        val pref = context.getSharedPreferences("pref_login", MODE_PRIVATE)
        return pref.getString(SHARED_PREF_ACCESS_TOKEN, "null") ?: "null"
    }

    fun getAccessTokenSecret(context: Context): String {
        val pref = context.getSharedPreferences("pref_login", MODE_PRIVATE)
        return pref.getString(SHARED_PREF_ACCESS_TOKEN_SECRET, "null") ?: "null"
    }

    fun getUserId(context: Context): String {
        val pref = context.getSharedPreferences("pref_login", MODE_PRIVATE)
        return pref.getString(SHARED_PREF_FLICKR_USER_ID, "null") ?: "null"
    }

    fun saveTokenSecret(context: Context, secret: String) {
        val edit = context.getSharedPreferences("pref_login", MODE_PRIVATE).edit()
        edit.putString(SHARED_PREF_REQUEST_TOKEN_SECRET, secret)
        edit.apply()
    }

    fun getTokenSecret(context: Context): String {
        val pref = context.getSharedPreferences("pref_login", MODE_PRIVATE)
        val secret = pref.getString(SHARED_PREF_REQUEST_TOKEN_SECRET, "null") ?: "null"
        val editor = pref.edit()

        editor.remove(SHARED_PREF_REQUEST_TOKEN_SECRET)
        editor.apply()

        return secret
    }
}