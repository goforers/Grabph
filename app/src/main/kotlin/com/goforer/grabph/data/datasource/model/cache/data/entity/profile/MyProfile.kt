package com.goforer.grabph.data.datasource.model.cache.data.entity.profile

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.DataColumns

@Entity(tableName = "MyProfile")
data class MyProfile(@field:PrimaryKey
    @field:ColumnInfo(index = true, name = COLUMN_ID) val id: String,
    @field:ColumnInfo(name = "nsid") val nsid: String,
    @field:ColumnInfo(name = "iconserver") val iconserver: String,
    @field:ColumnInfo(name = "iconfarm") val iconfarm: Int,
    @field:Embedded(prefix = "username_") val username: Username?,
    @field:Embedded(prefix = "realname_") val realname: Realname?,
    @field:Embedded(prefix = "location_") val location: Location?,
    @field:Embedded(prefix = "description_") val description: Description?,
    @field:Embedded(prefix = "photosurl_") val photosurl: Photosurl?,
    @field:Embedded(prefix = "profileurl_") val profileurl: Profileurl?,
    @field:Embedded(prefix = "mobileurl_") val mobileurl: Mobileurl?,
    @field:Embedded(prefix = "photos_") val photos: Photos?): BaseModel() {

    companion object {
        private const val COLUMN_ID = DataColumns.ID
    }

    @ColumnInfo(name = "grade")
    var grade: String? = null

    @ColumnInfo(name = "followers")
    var followers: String? = null

    @ColumnInfo(name = "followings")
    var followings: String? = null

    data class Username(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Realname(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Location(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Description(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Photosurl(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Profileurl(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Mobileurl(@field:ColumnInfo(name = "_content") val _content: String?)

    data class Photos(@Embedded(prefix = "firstdatetaken_") val firstdatetaken: Firstdatetaken?,
        @Embedded(prefix = "firstdate_") val firstdate: Firstdate?,
        @Embedded(prefix = "count_") val count: Count?) {
        data class Firstdatetaken(@field:ColumnInfo(name = "_content") val _content: String?)

        data class Firstdate(@field:ColumnInfo(name = "_content") val _content: String?)

        data class Count(@field:ColumnInfo(name = "_content") val _content: String?)
    }
}