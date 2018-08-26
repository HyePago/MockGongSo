package com.emirim.hyejin.mokgongso

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.LogoutResponseCallback
import com.kakao.usermgmt.response.model.UserProfile
import kotlinx.android.synthetic.main.activity_logout.*

class LogoutActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mAuth = FirebaseAuth.getInstance()

        signOutBtn.setOnClickListener {
            Toast.makeText(this, "ID : ${mAuth!!.currentUser?.email}", Toast.LENGTH_SHORT).show()
            signOut()
        }
    }

    private fun signOut() {
        mAuth!!.signOut()
        LoginManager.getInstance().logOut()

        kakaoLogout()

        mGoogleSignInClient!!.signOut().addOnCompleteListener(this) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun kakaoLogout() {
        UserManagement.requestLogout(object: LogoutResponseCallback() {
            override fun onCompleteLogout() {
                intentMain()
            }
        })
    }

    private fun intentMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}