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

import android.os.Bundle
import android.view.View
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import kotlinx.android.synthetic.main.activity_log_in.*
import com.facebook.CallbackManager
import android.content.Intent
import android.graphics.Color
import android.view.WindowManager
import android.widget.Toast
import com.facebook.AccessToken
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.ui.login.fragments.ResetPasswordFragment
import com.goforer.grabph.presentation.ui.login.fragments.SignInFragment
import com.goforer.grabph.presentation.ui.login.fragments.SignUpFragment
import com.goforer.grabph.presentation.vm.login.LoginViewModel
import javax.inject.Inject

class LogInActivity : BaseActivity() {
    private lateinit var signInFragment: SignInFragment
    private lateinit var signUpFragment: SignUpFragment
    private lateinit var currentFragment: BaseFragment

    internal lateinit var callbackManager: CallbackManager

    private val fragmentManager = supportFragmentManager

    @field:Inject
    lateinit var viewModel: LoginViewModel

    companion object {
        internal const val SNS_NAME_FACEBOOK = "FACEBOOK"
        internal const val SNS_NAME_GOOGLE = "GOOGLE"
    }

    override fun setContentView() { setContentView(R.layout.activity_log_in) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
            setFaceBookAuth()
            savedInstanceState ?: showLogin()

            networkStatusVisible(true)
        } else {
            networkStatusVisible(false)
        }
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
    }

    override fun setViews(savedInstanceState: Bundle?) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showLogin() {
        val ft = fragmentManager.beginTransaction()
        ft.add(R.id.login_container, SignInFragment())
            .commit()
    }

    internal fun showSignUp() {
        fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
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
        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        callbackManager = CallbackManager.Factory.create()
        val accessToken = AccessToken.getCurrentAccessToken()

        accessToken?.let {
            if (!accessToken.isExpired) {
                goToHome()
            }
        }
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this.login_container.visibility = View.VISIBLE
        this.disconnect_container.visibility = View.GONE
    } else {
        this.login_container.visibility = View.GONE
        this.disconnect_container.visibility = View.VISIBLE
    }

}
