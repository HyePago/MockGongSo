package com.emirim.hyejin.mokgongso

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult

import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    private lateinit var gso: GoogleSignInOptions

    companion object {
        const val TAG: String = "LoginActivity"
        const val RC_SIGN_IN: Int = 9001 // ?
    }

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mCallbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()

        googleSignInBtn.setOnClickListener {
            signIn()
        }
        /* googleSignInIcon.setOnClickListener{
            Toast.makeText(this, "Google Login", Toast.LENGTH_SHORT).show()
            googleSignInBtn.callOnClick()
            //signIn()
        } */

        // Facebook
        mCallbackManager = CallbackManager.Factory.create()
        facebookSignInBtn.setReadPermissions("email", "public_profile")
        facebookSignInBtn.registerCallback(mCallbackManager, object: FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                Log.d(TAG, "facebook:onSuccess:$result")
                handleFacebookAccessToken(result!!.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException?) {
                Log.d(TAG, "facebook:onError", error)
            }
        })
    }

    override fun onStart() {
        super.onStart()

        // 사용자가 로그인 되어 있는 지 확인
        updateUI(mAuth!!.currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)

        // 서버의 클라이언트 ID를 requestIdToken 메소드에 전달
        if(requestCode == RC_SIGN_IN) {
            var task = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if(task.isSuccess) {
                val account = task.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                updateUI(null)
                Toast.makeText(applicationContext, "SignIn: failed!",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 사용자가 정상적으로 로그인 한 후, Firebase에 인증
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)

        // showProgressDialog()

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.let { it ->
            it.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            // 로그인 성공
                            val user = it.currentUser
                            updateUI(user)
                        } else {
                            updateUI(null)
                        }
                    }
        }
    }

    private fun signIn() {
        var signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            var intent = Intent(this, MandalartActivity::class.java)
            startActivity(intent)
        }
    }

    // Facebook
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)

        mAuth?.let {
            it.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            Log.d(TAG, "signInWithCredential:success")
                            var user = mAuth!!.currentUser
                            updateUI(user)
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.exception)
                        }
                    }
        }
    }
}

