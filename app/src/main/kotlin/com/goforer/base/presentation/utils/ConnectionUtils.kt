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

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

object ConnectionUtils {
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                                                                            as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            return capabilities != null || capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)!!
        } else {
            connectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ObsoleteSdkInt")
    @SuppressWarnings("unused")
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                                                                            as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networks = connectivityManager.allNetworks
            for (mNetwork in networks) {
                val networkInfo = connectivityManager.getNetworkInfo(mNetwork)
                if (networkInfo?.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        } else {
            val info = connectivityManager.allNetworkInfo ?: return false
            for (anInfo in info) {
                if (anInfo.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        }

        return false
    }
}