package com.example.lukab.seechange_streaming.ui.activities

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle

import android.view.View
import android.widget.EditText
import com.example.lukab.seechange_streaming.R
import android.widget.Toast
import com.example.lukab.seechange_streaming.app.util.HexConverter
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel
import java.security.MessageDigest

class LoginActivity : BaseActivity() {


    private lateinit var loginViewModel: LoginViewModel;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginViewModel = LoginViewModel(this.application)
        setContentView(R.layout.activity_login)

        val hash = MessageDigest.getInstance("SHA-256").digest("henk".toByteArray())
        Log.d("Hash", HexConverter.bytesToHex(hash).toString())

    }

    fun login(v: View?) {
        val username =  this.findViewById (R.id.NameInput) as EditText
        val password = this.findViewById(R.id.PasswordInput) as EditText

        if (loginViewModel.isUsernameAndPasswordValid(username.text.toString(), password.text.toString())) {

            loginViewModel.login(username.text.toString(), password.text.toString()).observe(this, Observer<Boolean> { isLoggedIn ->
                if (isLoggedIn == true) {
                    openStreamingActivity()
                } else {
                    Toast.makeText(applicationContext, getString(R.string.login_mismatch), Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, getString(R.string.login_mismatch), Toast.LENGTH_SHORT).show()
        }
    }


    fun openStreamingActivity(){
        val intent = Intent(this.applicationContext, StreamingActivity::class.java);
        startActivity(intent)
        finish()
    }



}
