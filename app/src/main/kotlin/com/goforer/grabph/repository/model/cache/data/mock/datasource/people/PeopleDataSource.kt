package com.goforer.grabph.repository.model.cache.data.mock.datasource.people

import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.model.BaseModel
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.grabph.repository.model.cache.data.entity.profile.People
import java.lang.IllegalArgumentException

@MockData
class PeopleDataSource {
    private var followers: People? = null
    private var followings: People? = null

    internal fun setFollowers() {
        val json = CommonUtils.getJson("mock/my_followers.json")
        followers = BaseModel.gson().fromJson(json, People::class.java)
    }

    internal fun getFollowers():People? {
        this.followers?.let {
            return it
        }

        throw IllegalArgumentException("Followers should be not null")
    }


    internal fun setFollowings() {
        val json = CommonUtils.getJson("mock/my_following.json")
        followings = BaseModel.gson().fromJson(json, People::class.java)
    }

    internal fun getFollowings(): People? {
        this.followings?.let {
            return it
        }

        throw IllegalArgumentException("Followings should be not null")
    }
}