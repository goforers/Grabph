package com.goforer.grabph.presentation.ui.uploadphoto.data

data class RequestParams(
    val nonce: String,
    val timeStamp: String,
    val signature: String
)