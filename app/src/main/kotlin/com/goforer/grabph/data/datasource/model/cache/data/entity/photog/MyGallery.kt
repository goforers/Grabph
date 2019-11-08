package com.goforer.grabph.data.datasource.model.cache.data.entity.photog

import androidx.room.*
import com.goforer.base.presentation.model.BaseModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.DataColumns
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.EXIF

@Entity(tableName = "MyGallery")
data class MyGallery(@field:PrimaryKey
                    @field:ColumnInfo(name = COLUMN_ID) var id: String,
                    @field:ColumnInfo(name = "secret") var secret: String?,
                    @field:ColumnInfo(name = "server") var server: String?,
                    @field:ColumnInfo(name = "farm") var farm: Int,
                    @field:ColumnInfo(name = "camera") var camera: String?,
                    @field:ColumnInfo(name = "owner") var owner: String?,
                    @field:ColumnInfo(name = "title") var title: String?,
                    @field:ColumnInfo(name = "ispublic") var ispublic: Int,
                    @field:ColumnInfo(name = "isfriend") var isfriend: Int,
                    @field:ColumnInfo(name = "isfamily") var isfamily: Int) : BaseModel() {
    companion object {
        private const val COLUMN_ID = DataColumns.ID
    }

    @ColumnInfo(name = "like")
    var like: Int = 0

    @Ignore
    val exif: List<EXIF>? = null

}
