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

package com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.ownerinfo

import com.goforer.base.annotation.MockData
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Owner
import com.goforer.grabph.data.datasource.model.cache.data.mock.entity.OwnerIn
import com.google.gson.GsonBuilder

@MockData
class OwnerDataSource {
    private var ownerIn: OwnerIn? = null

    internal fun setOwner() {
        when(android.os.Build.MANUFACTURER) {
            "samsung" -> {
                ownerIn = if (android.os.Build.MODEL.contains("SM-N92")) {
                    val owner = "{\"owner\":{\"id\":\"156104719@N04\",\"iconserver\":\"7846\",\"iconfarm\":8,\"username\":\"Jason\",\"realname\":\"Jason Kim\",\"location\":\"Korea, Seoul, GangnamGu, SinsaDong\",\"description\":\"I have been in the making strategy of IT company for over 10 years. ambitious and very punctual.\",\"photourl\":{\"_content\":\"http://farm8.staticflickr.com/7846/buddyicons/156104719@N04.jpg\"},\"grade\":\"4\",\"pictures_count\":\"205\",\"pinned_count\":\"52\",\"gallery_count\":\"17\",\"purchased_count\":\"57\",\"follower_count\":\"168\",\"followings_count\":\"115\"}}"

                    GsonBuilder().serializeNulls().create().fromJson(owner, OwnerIn::class.java)
                } else {
                    val owner = "{\"owner\":{\"id\":\"156104719@N04\",\"iconserver\":\"7846\",\"iconfarm\":8,\"username\":\"Cotton\",\"realname\":\"Cotton Lim\",\"location\":\"Korea, Seoul, GangnamGu, SinsaDong\",\"description\":\"I have been in the making strategy of IT company for over 10 years. ambitious and very punctual.\",\"photourl\":{\"_content\":\"http://farm8.staticflickr.com/7846/buddyicons/156104719@N04.jpg\"},\"grade\":\"4\",\"pictures_count\":\"235\",\"pinned_count\":\"32\",\"gallery_count\":\"17\",\"purchased_count\":\"57\",\"follower_count\":\"152\",\"followings_count\":\"102\"}}"

                    GsonBuilder().serializeNulls().create().fromJson(owner, OwnerIn::class.java)
                }
            }
            "LGE" -> {
                val owner = "{\"owner\":{\"id\":\"156104719@N04\",\"iconserver\":\"7846\",\"iconfarm\":8,\"username\":\"Lukoh\",\"realname\":\"Lukoh Nam\",\"location\":\"Korea, Seoul, GangnamGu, SinsaDong\",\"description\":\"I am confident my leadership experience/expertise in SW development is making me a good SW engineer who works with many colleagues.\",\"photourl\":{\"_content\":\"http://farm8.staticflickr.com/7846/buddyicons/156104719@N04.jpg\"},\"grade\":\"4\",\"pictures_count\":\"135\",\"pinned_count\":\"36\",\"gallery_count\":\"17\",\"purchased_count\":\"57\",\"follower_count\":\"122\",\"followings_count\":\"152\"}}"

                ownerIn = GsonBuilder().serializeNulls().create().fromJson(owner, OwnerIn::class.java)
            }
            else -> {
                val owner = "{\"owner\":{\"id\":\"156104719@N04\",\"iconserver\":\"7846\",\"iconfarm\":8,\"username\":\"Allex\",\"realname\":\"Allex Son\",\"location\":\"Korea, Seoul, GangnamGu, SinsaDong\",\"description\":\"I have been in the making strategy of IT company for over 10 years. ambitious and very punctual.\",\"photourl\":{\"_content\":\"http://farm8.staticflickr.com/7846/buddyicons/156104719@N04.jpg\"},\"grade\":\"4\",\"pictures_count\":\"55\",\"pinned_count\":\"42\",\"gallery_count\":\"17\",\"purchased_count\":\"57\",\"follower_count\":\"192\",\"followings_count\":\"132\"}}"

                ownerIn = GsonBuilder().serializeNulls().create().fromJson(owner, OwnerIn::class.java)
            }
        }
    }

    internal fun getOwner(): Owner? {
        ownerIn?.owner?.let {
            return ownerIn?.owner
        }

        throw IllegalArgumentException("QuestInfo have to be not null")
    }
}