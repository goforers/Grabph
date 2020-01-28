package com.goforer.grabph.presentation.ui.uploadphoto.data

data class FlickrLoginResult(val user: User, val stat: String) {
    data class User(val id: String, val username: UserInfo) {
        data class UserInfo(val _content: String)
    }
}