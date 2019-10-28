package com.goforer.grabph.presentation.ui.upload

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.goforer.base.presentation.utils.CALLBACK_URL
import com.goforer.base.presentation.utils.CONSUMER_KEY
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.utils.OAUTH_VERSION
import com.goforer.base.presentation.utils.SIGN_METHOD
import com.goforer.base.presentation.utils.SharedPreference
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.network.api.SearpService
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_AUTH_ACTIVITY
import com.goforer.grabph.presentation.vm.upload.AuthViewModel
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AuthActivity : BaseActivity() {
    @field:Inject lateinit var searpService: SearpService
    @field:Inject internal lateinit var viewModel: AuthViewModel

    companion object {
        const val REQUEST_CODE_IMAGE = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasIntentData()) getAccessToken() else getRequestToken()
    }

    override fun setContentView() { setContentView(R.layout.activity_auth) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_IMAGE) {
                data?.let { handleImageRequestResult(it) }
            }
        }
    }

    private fun handleImageRequestResult(intent: Intent) {
        val imageUri: Uri? = intent.clipData?.getItemAt(0)?.uri ?: intent.data

        if (imageUri == null) {
            Timber.e("Invalid input image Uri.")
            return
        }

        Caller.callUploadPhoto(this, imageUri.toString(), CALLED_FROM_AUTH_ACTIVITY)
    }

    private fun hasIntentData(): Boolean = intent.data != null

    private fun getRequestToken() {
        val params = CommonUtils.getParamsForRequestToken()

        GlobalScope.launch {
            showStat("getting request token..")
            try {
                val response = searpService.getRequestToken(
                    params.nonce,
                    params.timeStamp,
                    CONSUMER_KEY,
                    SIGN_METHOD,
                    OAUTH_VERSION,
                    CALLBACK_URL,
                    params.signature
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    println("woogear@MainActivity...requestToken response: $responseBody")
                    responseBody?.let { result ->
                        val requestToken = getTokenFromCallback(result)
                        requestToken?.let { getVerifier(it) }
                    }
                } else {
                    val msg = response.errorBody()?.string() ?: "unknown error"
                    Log.e("woogear@MainActivity", "requestToken error: $msg")
                    showStat("got error.. please try again")
                    showErrorMsg(msg, true)
                }
            } catch (e: Exception) {
                println("woogear@MainActivity....exception: ${e.printStackTrace()}")
            }
        }
    }

    private fun getVerifier(token: String) {
        showStat("get verifier...")
        Caller.callVerifierUrl(this, token, CALLED_FROM_AUTH_ACTIVITY)
    }

    private fun getAccessToken() {
        showStat("getting access token..")
        Log.d("woogear@OauthActivity", "msg of intent: ${intent.toUri(Intent.URI_INTENT_SCHEME)}")
        intent.data?.let { uri ->
            val requestToken = uri.getQueryParameter("oauth_token")
            val verifier = uri.getQueryParameter("oauth_verifier")

            val secret = SharedPreference.getTokenSecret(this)
            val params = CommonUtils.getParamsForAccessToken(requestToken!!, verifier!!, secret)

            Log.i("woogear@authActivity", "uri=${uri}")
            Log.i("woogear@authActivity", "verifier=$verifier")

            GlobalScope.launch {
                try {
                    val response = searpService.getAccessToken(
                        params.nonce, params.timeStamp, verifier!!, CONSUMER_KEY,
                        SIGN_METHOD, OAUTH_VERSION, requestToken, params.signature
                    )

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        Log.d("woogear@OauthActivity", "accessToken result: $responseBody")
                        val uri = Uri.parse("text.com?$responseBody")
                        val accessToken = uri.getQueryParameter("oauth_token")
                        val accessSecret = uri.getQueryParameter("oauth_token_secret")
                        val userId = uri.getQueryParameter("user_nsid")

                        SharedPreference.saveAccessToken(applicationContext, accessToken!!, accessSecret!!, userId!!)
                        go()

                    } else {
                        val msg = response.errorBody()?.string() ?: "unknown error"
                        showStat("got error.. please try again")
                        Log.e("woogear@OauthActivity", "network error: $msg")
                    }
                } catch (e: java.lang.Exception) {
                    println("woogear@OauthActivity..exception: ${e.printStackTrace()}")
                }
            }
        }
    }

    private fun go() {
        Caller.callPhotoGallery(this, CALLED_FROM_AUTH_ACTIVITY, REQUEST_CODE_IMAGE)
    }

    private fun showStat(msg: String) {
        runOnUiThread { this.tv_auth_stat.text = msg }
    }

    private fun showErrorMsg(msg: String, isFailed: Boolean) {

    }

    private fun getTokenFromCallback(result: String): String? {
        val uri = Uri.parse("test.com?$result")
        val isConfirmed = uri.getBooleanQueryParameter("oauth_callback_confirmed", false)

        return if (isConfirmed) {
            val token = uri.getQueryParameter("oauth_token")
            val tokenSecret = uri.getQueryParameter("oauth_token_secret")
            SharedPreference.saveTokenSecret(this, tokenSecret!!)
            token
        } else {
            null
        }
    }
}
