package com.goforer.base.presentation.utils

const val SHARED_PREF_ACCESS_TOKEN = "access_token_in_shared_preferences"
const val SHARED_PREF_ACCESS_TOKEN_SECRET = "access_token_secret_in_shared_preferences"
const val SHARED_PREF_FLICKR_USER_ID = "flickr_user_id_in_shared_preferences"
const val SHARED_PREF_REQUEST_TOKEN_SECRET = "request_token_secret_in_shared_preferences"

const val KEY_UPLOAD_IMAGE_URI = "KEY_UPLOAD_IMAGE_URI"
const val KEY_UPLOAD_IMAGE_TITLE = "KEY_UPLOAD_IMAGE_TITLE"
const val KEY_UPLOAD_IMAGE_DESC = "KEY_UPLOAD_IMAGE_DESC"

const val KEY_UPLOAD_RESPONSE_STAT = "KEY_UPLOAD_STAT"
const val KEY_UPLOAD_RESPONSE_ERROR_CODE = "KEY_UPLOAD_ERROR_CODE"
const val KEY_UPLOAD_RESPONSE_ERROR_MSG = "KEY_UPLOAD_RESPONSE"
const val KEY_UPLOAD_RESPONSE_PHOTO_ID = "KEY_UPLOAD_PHOTO_ID"

const val KEY_SELECTED_IMAGE_URI = "KEY_SELECTED_IMAGE_URI"

const val UPLOADING_WORK_NAME = "uploading_image_work"
const val TAG_OUTPUT = "OUTPUT"
const val MAX_NUMBER_REQUEST_PERMISSIONS = 2
const val REQUEST_CODE_PERMISSIONS = 101

const val BASE_URL_FOR_TOKEN = "https://www.flickr.com/"
const val BASE_URL_FOR_SERVICE = "https://up.flickr.com/"

const val CONSUMER_KEY = "69d74fda16e013f3d72f9b8757806bab"
const val CONSUMER_SECRET = "f5530a2eceb06f1c"

const val ACCESS_TOKEN = "72157711389285633-c59795423f8e2dd8"
const val ACCESS_SECRET = "3053ba899da2eb6d"

const val SIGN_METHOD = "HMAC-SHA1"
const val REQUEST_TOKEN_URL = "https://www.flickr.com/services/oauth/request_token"
const val ACCESS_TOKEN_URL = "https://www.flickr.com/services/oauth/access_token"
const val AUTHORIZE_URL = "https://www.flickr.com/services/oauth/authorize"
const val TEST_LOGIN_URL = "https://www.flickr.com/services/rest"
const val UPLOAD_URL = "https://up.flickr.com/services/upload/"
const val OAUTH_VERSION = "1.0"

const val CALLBACK_URL = "searp%3A%2F%2Fsearp.io%2Fsearpapp%2F"

@JvmField val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence = "Verbose WorkManager Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Shows notifications whenever work starts"
@JvmField val NOTIFICATION_TITLE: CharSequence = "Searp Upload Status"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1