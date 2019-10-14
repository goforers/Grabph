/*
 * Copyright 2019 Lukoh Nam, goForer
 *    
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License along with this program.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

@file:Suppress("DEPRECATED_IDENTITY_EQUALS")

package com.goforer.grabph.presentation.ui.login

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import kotlinx.android.synthetic.main.activity_log_in.*
import com.facebook.CallbackManager
import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.utils.SharedPreference
import com.goforer.grabph.presentation.caller.Caller
import java.util.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task

class LogInActivity : BaseActivity() {
    private lateinit var callbackManager: CallbackManager

    private var auth: FirebaseAuth? = null

    /* Client used to interact with Google APIs. */
    private var googleApiClient: GoogleSignInClient? = null

    companion object {
        private const val EMAIL = "email"
        private const val PUBLIC_PROFILE = "public_profile"
        private const val AUTH_TYPE = "rerequest"

        internal const val SNS_NAME_FACEBOOK = "FACEBOOK"
        internal const val SNS_NAME_GOOGLE = "GOOGLE"

        private const val RC_SIGN_IN = 9001
    }

    override fun setContentView() {
        setContentView(R.layout.activity_log_in)

        /* *************************************
         *              GOOGLE                 *
         ***************************************/
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        /* Setup the Google API object to allow Google+ logins */
        googleApiClient = GoogleSignIn.getClient(this, gso)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isNetworkAvailable) {
            this@LogInActivity.disconnect_container.visibility = View.VISIBLE
            this@LogInActivity.iv_disconnect.visibility = View.VISIBLE
            this@LogInActivity.tv_notice1.visibility = View.VISIBLE
            this@LogInActivity.tv_notice2.visibility = View.VISIBLE

            return
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT

        /* *************************************
         *              GOOGLE                 *
         ***************************************/
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()

        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        val accessToken = AccessToken.getCurrentAccessToken()

        accessToken?.let {
            if (!accessToken.isExpired) {
                goToHome()
            }
        }

        /* *************************************
         *              GOOGLE                 *
         ***************************************/
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth?.currentUser

        currentUser?.let {
            goToHome()

            return
        }
    }

    override fun setViews(savedInstanceState: Bundle?) {
        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        callbackManager = CallbackManager.Factory.create()
        this@LogInActivity.login_button.setReadPermissions(Arrays.asList(EMAIL, PUBLIC_PROFILE))
        this@LogInActivity.login_button.authType = AUTH_TYPE
        // Register a callback to respond to the user
        this@LogInActivity.login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                setResult(Activity.RESULT_OK)
                goToHome()
                finish()
            }

            override fun onCancel() {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            override fun onError(e: FacebookException) {
                // Handle exception
                showMessage(e.toString())
            }
        })

        this@LogInActivity.btn_facebook.setOnClickListener {
            SharedPreference.saveSharePreferenceSocialLogin(this, SNS_NAME_FACEBOOK)
            this@LogInActivity.login_button.performClick()
        }

        /* *************************************
         *              GOOGLE                 *
         ***************************************/
        this@LogInActivity.btn_google.setOnClickListener {
            SharedPreference.saveSharePreferenceSocialLogin(this, SNS_NAME_GOOGLE)
            signIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        callbackManager.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)

        /* *************************************
         *              GOOGLE                 *
         ***************************************/
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode === RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            account?.let {
                firebaseAuthWithGoogle(account)
            }
        } catch (e: ApiException) {
            showMessage(e.toString())
        }
    }

    private fun goToHome() {
        Caller.callHome(this)
        finish()
    }

    private fun showMessage(message: String) {
        showToastMessage(this, message, Toast.LENGTH_SHORT)
    }

    /* *************************************
     *              GOOGLE                 *
     ***************************************/
    private fun signIn() {
        val signInIntent = googleApiClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /* *************************************
     *              GOOGLE                 *
     ***************************************/
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        auth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth?.currentUser

                        user?.let {
                            goToHome()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        showToastMessage(this@LogInActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT)

                    }
                }
    }
}
