package com.goforer.grabph.repository.model.cache.data.entity.ranking

import androidx.room.*
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.repository.model.cache.data.entity.profile.Searper
import com.google.gson.reflect.TypeToken

@Entity(tableName = "Ranking")
data class Ranking (@field:PrimaryKey val itemCount: Int,
                    @field:Embedded(prefix = "photoRanking_") val photo_rank: RankList,
                    @field:Embedded(prefix = "profitRanking_") val profit_rank: RankList,
                    @field:Embedded(prefix = "likesRanking_") val likes_rank: RankList): BaseModel() {

    data class RankList(@field:ColumnInfo(name = "count") val count: Int): BaseModel() {
        @TypeConverters(RankingTypeConverters::class)
        lateinit var ranking: List<Rank>
    }

    class RankingTypeConverters {
        @TypeConverter
        fun stringToMeasurements(json: String): List<Rank>? {
            val type = object : TypeToken<List<Rank>>() {}.type

            return gson().fromJson<List<Rank>>(json, type)
        }

        @TypeConverter
        fun measurementsToString(list: List<Rank>): String {
            val type = object : TypeToken<List<Rank>>() {}.type

            return gson().toJson(list, type)
        }
    }

    data class Rank(@field:ColumnInfo(name = "id") val id: String,
                    @field:ColumnInfo(name = "rank") val rank: Int,
                    @field:ColumnInfo(name = "username") val username: String,
                    @field:ColumnInfo(name = "name") val name: String,
                    @field:ColumnInfo(name = "point") val point: Int,
                    @field:ColumnInfo(name = "rank_color") val rank_color: Int,
                    @field:Embedded(prefix = "profile_image") val profile_image: Searper.ProfileUrls
    )
}