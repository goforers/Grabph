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

package com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.category

import com.goforer.base.annotation.MockData
import com.goforer.grabph.data.datasource.model.cache.data.mock.entity.CategoryIn
import com.goforer.grabph.data.datasource.model.cache.data.entity.category.Category
import com.google.gson.GsonBuilder

@MockData
class CategoryDataSource {
    private var categoryIn: CategoryIn? = null

    internal fun setCategories() {
        val categoryg = "{\"categoryg\":{\"stat\":\"OK\"," +
                "\"categories\":[{\"id\":\"240272134@ANIMAL\",\"type\":0,\"title\":\"Animal\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/animal.jpg\"}}," +
                "{\"id\":\"140272037@ARCHITECTURE\",\"type\":1,\"title\":\"Architecture\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/architecture.jpg\"}}," +
                "{\"id\":\"550272028@ART\",\"type\":2,\"title\":\"Art\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/art.jpg\"}}," +
                "{\"id\":\"180277037@CAR\",\"type\":3,\"title\":\"Car\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/car.jpg\"}}," +
                "{\"id\":\"747272047@FASHION\",\"type\":4,\"title\":\"Fashion\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/fashion.jpg\"}}," +
                "{\"id\":\"570772037@FOOD\",\"type\":5,\"title\":\"Food\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/food.jpg\"}}," +
                "{\"id\":\"940346037@PEOPLE\",\"type\":6,\"title\":\"People\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/people.jpg\"}}," +
                "{\"id\":\"270772077@SPORT\",\"type\":7,\"title\":\"Sport\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/sport.jpg\"}}," +
                "{\"id\":\"540772777@TRAVEL\",\"type\":8,\"title\":\"Travel\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/travel.jpg\"}}," +
                "{\"id\":\"640543777@HUMOR\",\"type\":9,\"title\":\"Humor\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/humor.jpg\"}}," +
                "{\"id\":\"721772667@BEAUTY\",\"type\":10,\"title\":\"Beauty\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/beauty.jpg\"}}," +
                "{\"id\":\"729772777@DESIGN\",\"type\":11,\"title\":\"Design\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/design.jpg\"}}," +
                "{\"id\":\"740772234@EDUCATION\",\"type\":12,\"title\":\"Education\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/education.jpg\"}}," +
                "{\"id\":\"755772777@SCENERY\",\"type\":13,\"title\":\"Scenery\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/scenery.jpg\"}}," +
                "{\"id\":\"768772567@HEALTH\",\"type\":14,\"title\":\"Health\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/health.jpg\"}}," +
                "{\"id\":\"780772177@HISTORY\",\"type\":15,\"title\":\"History\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/history.jpg\"}}," +
                "{\"id\":\"348077257@LIFE\",\"type\":16,\"title\":\"Life\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/life.jpg\"}}," +
                "{\"id\":\"270772546@TECH\",\"type\":17,\"title\":\"Tech\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/tech.jpg\"}}," +
                "{\"id\":\"670672877@IT\",\"type\":18,\"title\":\"IT\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/it.jpg\"}}," +
                "{\"id\":\"377722178@DIET\",\"type\":19,\"title\":\"Diet\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/diet.jpg\"}}," +
                "{\"id\":\"572725628@RELIGION\",\"type\":20,\"title\":\"Religion\",\"photo\":{\"m\":\"https://raw.githubusercontent.com/Lukoh/Grabph_Category/master/home/religion.jpg\"}}]}}"

        categoryIn = GsonBuilder().serializeNulls().create().fromJson(categoryg, CategoryIn::class.java)
    }

    internal fun getCategories(): List<Category>? {
        this.categoryIn?.categoryg?.categories?.let {
            return this.categoryIn?.categoryg?.categories
        }

        throw IllegalArgumentException("Category have to be not null")
    }
}