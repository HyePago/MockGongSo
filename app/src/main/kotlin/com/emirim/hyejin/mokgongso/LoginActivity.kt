package com.emirim.hyejin.mokgongso

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.emirim.hyejin.mokgongso.api.API
import com.emirim.hyejin.mokgongso.api.APIRequestManager
import com.emirim.hyejin.mokgongso.model.*
import com.emirim.hyejin.mokgongso.tutorial.MainAcitivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_signup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    private lateinit var gso: GoogleSignInOptions

    companion object {
        const val TAG: String = "LoginActivity"
        const val RC_SIGN_IN: Int = 9001 // ?
        var appData: SharedPreferences? = null
        var boolean = false
    }

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mCallbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // local login _ SharedPreferences
        appData = this.getSharedPreferences("Mandalart", 0)

        // 설정값 불러옴
        var token: String = appData!!.getString("ID", "")

        Log.d("Login token" ,token)

        if(token.isNotEmpty()) {
            // auto

            var callAuto: Call<SignInMessage> = APIRequestManager.getInstance().requestServer().auto(token)

            callAuto.enqueue(object: Callback<SignInMessage> {
                override fun onResponse(call: Call<SignInMessage>, response: Response<SignInMessage>) {
                    when(response.code()) {
                        200 -> {
                            val message: SignInMessage = response.body() as SignInMessage
                            val editor = appData!!.edit()

                            editor.putString("ID", message.data.token.trim())
                            editor.putString("name", message.data.name.trim())
                            User.startDay = message.data.startDay.trim()

                            Log.d("Login" ,message.data.toString())

                            editor.apply()
                        }
                    }
                }
                override fun onFailure(call: Call<SignInMessage>, t: Throwable) {
                    Log.e("Page4", "실패: " + t.message)
                    t.printStackTrace()
                }
            })

            intentMandalart()
        }

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

        // 회원가입
        join.setOnClickListener {
            var intent = Intent(this, Terms::class.java)
            intent.putExtra("bool", false)
            startActivity(intent)
        }

        // local 로그인
        loginBtn.setOnClickListener {
            User.signIn = Signin(email.text.toString(), password.text.toString())

            var call: Call<SignInMessage> = APIRequestManager.getInstance().requestServer().signIn(User.signIn)

            call.enqueue(object: Callback<SignInMessage> {
                override fun onResponse(call: Call<SignInMessage>, response: Response<SignInMessage>) {
                    when(response.code()) {
                        200 -> {
                            val message: SignInMessage = response.body() as SignInMessage
                            val editor = appData!!.edit()

                            Log.d(TAG, "Login Success" + response.body().toString())

                            editor.putString("ID", message.data.token.trim())
                            editor.putString("name", message.data.name.trim())
                            User.startDay = message.data.startDay.trim()

                            Log.d("Login" ,message.data.toString())

                            editor.apply()

                            // 튜토리얼을 봐야하는 지 확인
                            intentChk()
                        }
                        404 -> {
                            Log.d(TAG, "Login Fail")
                            password.requestFocus()
                            password.setText("")
                        }
                    }
                }

                override fun onFailure(call: Call<SignInMessage>, t: Throwable) {
                    Log.e("SignUpActivity", "에러: " + t.message)
                    t.printStackTrace()
                }
            })
        }
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
            // Firebase Login 성공 - Server에 값 전송
            User.fb = Fb(user.uid, user.displayName)

            var callFb: Call<SignInMessage> = APIRequestManager.getInstance().requestServer().fb(User.fb)

            callFb.enqueue(object: Callback<SignInMessage> {
                override fun onResponse(call: Call<SignInMessage>, response: Response<SignInMessage>) {
                    when (response.code()) {
                        200 -> {
                            // 로그인 성공
                            val message: SignInMessage = response.body() as SignInMessage
                            val editor = appData!!.edit()

                            editor.putString("ID", message.data.token.trim())
                            editor.putString("name", message.data.name.trim())
                            User.startDay = message.data.startDay.trim()

                            editor.apply()

                            intentMandalart()
                        }
                        201 -> {
                            // 회원가입 성공
                            val message: SignInMessage = response.body() as SignInMessage
                            val editor = appData!!.edit()

                            editor.putString("ID", message.data.token.trim())
                            editor.putString("name", message.data.name.trim())
                            User.startDay = message.data.startDay.trim()

                            editor.apply()

                            intentTutorial()
                        }
                        500 -> {
                            Log.d(TAG, "Login Fail")
                        }
                    }
                }

                override fun onFailure(call: Call<SignInMessage>, t: Throwable) {
                    Log.e("SignUpActivity", "에러: " + t.message)
                    t.printStackTrace()
                }
            })
        }
    }


    private var callbackManager: CallbackManager? = null
    // Facebook
    private fun handleFacebookAccessToken(token: AccessToken) {
        var btnLoginFacebook = findViewById<Button>(R.id.facebookSignInBtn)

        btnLoginFacebook.setOnClickListener(View.OnClickListener {
            // Login
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
            LoginManager.getInstance().registerCallback(callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onSuccess(loginResult: LoginResult) {
                            Log.d("MainActivity", "Facebook token: " + loginResult.accessToken.token)
                            startActivity(Intent(applicationContext, MandalartActivity::class.java))
                        }

                        override fun onCancel() {
                            Log.d("MainActivity", "Login Fail")

                        }

                        override fun onError(error: FacebookException) {
                            Log.d("MainActivity", "FaceBook Server is gone")

                        }
                    })
        })
    }

    fun intentMandalart() {
        var intent = Intent(this, MandalartActivity::class.java)

        startActivity(intent)
        finish()
    }

    fun intentTutorial() {
        var intent = Intent(this, MainAcitivity::class.java)

        startActivity(intent)
        finish()
    }

    fun intentChk() {
        Mandalart.mandalChk = MandalChk(appData!!.getString("ID", ""))

        var callMandal: Call<Re> = APIRequestManager.getInstance().requestServer().getMandal(Mandalart.mandalChk)

        callMandal.enqueue(object: Callback<Re> {
            override fun onResponse(call: Call<Re>, response: Response<Re>) {
                when(response.code()) {
                    200 -> {
                        // 만다라트 존재
                        val re: Re = response.body() as Re
                        com.emirim.hyejin.mokgongso.Mandalart.re = re
                        com.emirim.hyejin.mokgongso.mandalart.Mandalart.title = re.re.title
                        com.emirim.hyejin.mokgongso.mandalart.Mandalart.achievement = re.re.achievement.toInt()

                        Log.d("getMandal", re.toString())

                        for(i in 0..7){
                            com.emirim.hyejin.mokgongso.mandalart.Mandalart.subMandalartTitle[re.re.mandal[i].order] = re.re.mandal[i].middleTitle

                            var strArray = Array<String>(8, {""})
                            strArray[0] = re.re.mandal[i].smallMandalArt1.title
                            strArray[1] = re.re.mandal[i].smallMandalArt2.title
                            strArray[2] = re.re.mandal[i].smallMandalArt3.title
                            strArray[3] = re.re.mandal[i].smallMandalArt4.title
                            strArray[4] = re.re.mandal[i].smallMandalArt5.title
                            strArray[5] = re.re.mandal[i].smallMandalArt6.title
                            strArray[6] = re.re.mandal[i].smallMandalArt7.title
                            strArray[7] = re.re.mandal[i].smallMandalArt8.title

                            var intArray = IntArray(8, {0})
                            intArray[0] = re.re.mandal[i].smallMandalArt1.achievement
                            intArray[1] = re.re.mandal[i].smallMandalArt2.achievement
                            intArray[2] = re.re.mandal[i].smallMandalArt3.achievement
                            intArray[3] = re.re.mandal[i].smallMandalArt4.achievement
                            intArray[4] = re.re.mandal[i].smallMandalArt5.achievement
                            intArray[5] = re.re.mandal[i].smallMandalArt6.achievement
                            intArray[6] = re.re.mandal[i].smallMandalArt7.achievement
                            intArray[7] = re.re.mandal[i].smallMandalArt8.achievement

                            com.emirim.hyejin.mokgongso.mandalart.Mandalart.thirdContent[i] = strArray
                            com.emirim.hyejin.mokgongso.mandalart.Mandalart.thirdAchievement[i] = intArray
                        }

                        MandalartActivity.mandalBoolean = true
                        Log.d("ServerMandal", response.body().toString())

                        intentMandalart()
                    }
                    401 -> {
                        // 존재 X
                        intentTutorial()
                    }
                    404 -> {
                        // 존재 X
                        intentTutorial()
                    }
                }
            }

            override fun onFailure(call: Call<Re>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }
}