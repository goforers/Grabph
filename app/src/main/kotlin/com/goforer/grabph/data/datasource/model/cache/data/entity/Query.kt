package com.goforer.grabph.data.datasource.model.cache.data.entity

import com.goforer.base.presentation.model.BaseModel
import javax.inject.Singleton

@Singleton
class Query: BaseModel() {
    lateinit var query: String
    var pages: Int = 0
    var loadType: Int = 0
    var boundType: Int = 0
}