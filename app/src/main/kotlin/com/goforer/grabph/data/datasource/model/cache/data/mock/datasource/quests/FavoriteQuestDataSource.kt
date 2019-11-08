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

package com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.quests

import androidx.paging.PageKeyedDataSource
import com.goforer.base.annotation.MockData
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.Quest
import com.goforer.grabph.data.datasource.model.cache.data.mock.entity.FavoriteQuestIn
import com.google.gson.GsonBuilder

@MockData
class FavoriteQuestDataSource: PageKeyedDataSource<Int, Quest>() {
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Quest>) {
        val quest = "{\"quest\":[{\"idx\":1,\"id\":\"A24343_1@N01\",\"owner\":\"1@N01\",\"ownerName\":\"Searp\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/searp_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/searp_travel.jpg\",\"favoriteCategory\":\"Travel\",\"title\":\"Cool! Let's shoot a Surfing\",\"state\":\"On a questInfo\",\"description\":\"Show us surfing photo. Cool surfing scenery! Decided to surf with your friends in this year? Awesome. Want to spend more time on surfing with your friends in this year?\",\"rewards\":\"2,000\",\"duration\":25}," +
            "{\"idx\":2,\"id\":\"M34343_2@N01\",\"owner\":\"2@N01\",\"ownerName\":\"Graph\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/grabph_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/grabph_tarvel.jpg\",\"favoriteCategory\":\"Travel\",\"title\":\"Fresh & beautiful forest.\",\"state\":\"On a questInfo\",\"description\":\"Show us beautiful forest photo. Great scenery! Decided to spend awesome time with your friends in this year? Awesome. Want to spend more time on climbing with your friends in this year?\",\"rewards\":\"2,000\",\"duration\":15}," +
            "{\"idx\":3,\"id\":\"N34343_3@N01\",\"owner\":\"3@N01\",\"ownerName\":\"Ford\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/ford_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/ford_travel.jpg\",\"favoriteCategory\":\"Car\",\"title\":\"Great travel season.\",\"state\":\"On a questInfo\",\"description\":\"Show us great travel photo that is taken by you in this winter. Great snow-fall scenery with Ford SUV! Decided to travel with family and Ford SUV in this winter? Even better. Want to spend more time with your family in this winter?\",\"rewards\":\"1,500\",\"duration\":14}," +
            "{\"idx\":4,\"id\":\"P34342_4@N01\",\"owner\":\"4@N01\",\"ownerName\":\"Nikon\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/nikon_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/nikon_travel.jpg\",\"favoriteCategory\":\"Travel\",\"title\":\"Beautiful spring  scenery!\",\"state\":\"On a questInfo\",\"description\":\"Take your beautiful photo that is taken by you in this spring.  Great forest scenery! Decided to travel with family in this spring? Even better. Want to spend more time with your family in this spring?\",\"rewards\":\"2,500\",\"duration\":7}," +
            "{\"idx\":5,\"id\":\"D74342_5@N01\",\"owner\":\"5@N01\",\"ownerName\":\"Sony\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/sony_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/sony_travel.jpg\",\"favoriteCategory\":\"Travel\",\"title\":\"Beautiful spring scenery!\",\"state\":\"On a questInfo\",\"description\":\"Take photo that is taken by you in this spring. Great spring scenery! Decided to travel alone in this spring? Even better. Want to spend more time with yourself in this spring?\",\"rewards\":\"3,000\",\"duration\":7}," +
            "{\"idx\":6,\"id\":\"F14342_6@N01\",\"owner\":\"6@N01\",\"ownerName\":\"Cannon\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/cannon_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/cannon_travel.jpg\",\"favoriteCategory\":\"Travel\",\"title\":\"Beautiful beach scenery!\",\"state\":\"On a questInfo\",\"description\":\"Show us your beautiful photo that is taken by you in beautiful beach. Great cool scenery! Decided to travel with family in this summer? Even better. Want to spend more time with your family in this summer?\",\"rewards\":\"2,500\",\"duration\":24}," +
            "{\"idx\":7,\"id\":\"E62342_7@N01\",\"owner\":\"7@N01\",\"ownerName\":\"BMW\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/bmw_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/bmw_travel.jpg\",\"favoriteCategory\":\"Car\",\"title\":\"Drive on the road\",\"state\":\"On a quest\",\"description\":\"Show us BMW SUV photo that is taken by you in this winter. Great snow-fall scenery! Decided to travel with family in this winter? Even better. Want to spend more time with your family in this winter?\",\"rewards\":\"4,000\",\"duration\":9}," +
            "{\"idx\":8,\"id\":\"G94342_8@N01\",\"owner\":\"8@N01\",\"ownerName\":\"TripAdvisor\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/tripadvisor_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/tripadvisor_travel.jpg\",\"favoriteCategory\":\"Travel\",\"title\":\"Awesome winter season\",\"state\":\"On a questInfo\",\"description\":\"Show us your snow-fall photo that is taken by you in this winter. Great snow-fall scenery! Decided to travel with your honey in this winter? Even better. Want to spend more time with your honey in this winter?\",\"rewards\":\"2,000\",\"duration\":15}," +
            "{\"idx\":9,\"id\":\"P34342_9@N01\",\"owner\":\"9N0@1\",\"ownerName\":\"GoEuro\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/goeuro_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/goeuro_travel.jpg\",\"favoriteCategory\":\"Travel\",\"title\":\"Europe in spring season\",\"state\":\"On a questInfo\",\"description\":\"Show us your beautiful photo that is taken by you in Europe. Great beautiful Europe scenery! Decided to travel with family in this winter? Even better. Want to spend more time with your family in Europe?\",\"rewards\":\"2,500\",\"duration\":10}," +
            "{\"idx\":10,\"id\":\"S34342_10@N01\",\"owner\":\"10@N01\",\"ownerName\":\"Mercedes - Benz\",\"ownerLogo\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/Logo/benz_logo.png\",\"ownerImage\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Mission/master/Provider/MainImage/benz_travel.jpg\",\"favoriteCategory\":\"Car\",\"title\":\"Drive on the way\",\"state\":\"On a questInfo\",\"description\":\"Show us your SUV video that is taken by you in this winter. Great snow-fall scenery! Decided to travel with family in this winter? Even better. Want to spend more time with your family in this winter?\",\"rewards\":\"4,500\",\"duration\":6}]}"
        val quests = GsonBuilder().serializeNulls().create().fromJson(quest, FavoriteQuestIn::class.java)

        callback.onResult(quests.quest as MutableList<Quest>, null, null)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Quest>) {
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Quest>) {
    }
}