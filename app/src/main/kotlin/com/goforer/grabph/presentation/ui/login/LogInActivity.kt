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
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.utils.SharedPreference
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.ui.login.fragments.ResetPasswordFragment
import com.goforer.grabph.presentation.ui.login.fragments.SignInFragment
import com.goforer.grabph.presentation.ui.login.fragments.SignUpFragment
import com.goforer.grabph.presentation.vm.login.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.layout_disconnection.*
import java.util.Arrays
import javax.inject.Inject

class LogInActivity : BaseActivity() {
    private lateinit var signInFragment: SignInFragment
    private lateinit var signUpFragment: SignUpFragment
    private lateinit var currentFragment: BaseFragment

    private lateinit var callbackManager: CallbackManager

    private var auth: FirebaseAuth? = null

    /* Client used to interact with Google APIs. */
    private var googleApiClient: GoogleSignInClient? = null

    private val fragmentManager = supportFragmentManager

    @field:Inject
    lateinit var viewModel: LoginViewModel

    companion object {
        private const val EMAIL = "email"
        private const val PUBLIC_PROFILE = "public_profile"
        private const val AUTH_TYPE = "rerequest"

        internal const val SNS_NAME_FACEBOOK = "FACEBOOK"
        internal const val SNS_NAME_GOOGLE = "GOOGLE"

        private const val RC_SIGN_IN = 9001
    }

    override fun setContentView() { setContentView(R.layout.activity_log_in) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
            showLogin()
            setFaceBookAuth()
            setGoogleAuth()
            networkStatusVisible(true)
        } else {
            networkStatusVisible(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // FACEBOOK
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // GOOGLE : Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            showLoadingProgressBar(true, getString(R.string.loading_now))
            account?.let { firebaseAuthWithGoogle(account) }
        } catch (e: ApiException) {
            showMessage(e.toString())
        }
    }

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
                    showToastMessage(this@LogInActivity,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT)
                    showLoadingProgressBar(false)
                }
            }
    }

    private fun showLogin() {
        val ft = fragmentManager.beginTransaction()
        ft.add(R.id.login_container, SignInFragment()).commit()
    }

    internal fun showSignUp() {
        fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right)
            .replace(R.id.login_container, SignUpFragment())
            .commit()
    }

    internal fun goToResetPassword() {
        fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
            .replace(R.id.login_container, ResetPasswordFragment())
            .commit()
    }

    internal fun showMessage(message: String) {
        showToastMessage(this, message, Toast.LENGTH_SHORT)
    }

    internal fun goToHome() {
        Caller.callHome(this)
        finish()
    }

    private fun setFaceBookAuth() {
        callbackManager = CallbackManager.Factory.create()
        this.login_button.setReadPermissions(
            Arrays.asList(EMAIL, PUBLIC_PROFILE))
        this.login_button.authType = AUTH_TYPE
        // Register a callback to respond to the user
        this.login_button.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                setResult(Activity.RESULT_OK)
                goToHome()
            }

            override fun onCancel() {
                setResult(Activity.RESULT_CANCELED)
            }

            override fun onError(e: FacebookException) {
                // Handle exception
                showMessage(e.toString())
            }
        })
    }

    private fun setGoogleAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        /* Setup the Google API object to allow Google+ logins */
        googleApiClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
    }

    internal fun signInWithGoogle() {
        SharedPreference.saveSharePreferenceSocialLogin(this, SNS_NAME_GOOGLE)
        val signInIntent = googleApiClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    internal fun signInWithFacebook() {
        SharedPreference.saveSharePreferenceSocialLogin(this, SNS_NAME_FACEBOOK)
        this.login_button.performClick()
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this.login_container.visibility = View.VISIBLE
        this.disconnect_container_pinned.visibility = View.GONE
    } else {
        this.login_container.visibility = View.GONE
        this.disconnect_container_pinned.visibility = View.VISIBLE
    }

    private fun showNetworkError(resource: Resource) = when(resource.errorCode) {
        in 400..499 -> showSnackBar(getString(R.string.phrase_client_wrong_request))
        in 500..599 -> showSnackBar(getString(R.string.phrase_server_wrong_response))
        else -> showSnackBar(resource.getMessage().toString())
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(this.login_container, msg, LENGTH_LONG).show()
    }

    internal fun showLoadingProgressBar(isLoading: Boolean, comment: String? = null) {
        this.container_progress_bar_logging_in.visibility = if (isLoading) View.VISIBLE else View.GONE
        comment?.let { this.tv_comment_login.text = it }
    }
}
