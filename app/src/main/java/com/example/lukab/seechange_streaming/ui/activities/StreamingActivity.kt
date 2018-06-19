package com.example.lukab.seechange_streaming.ui.activities

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel

class StreamingActivity : BaseActivity() {

    lateinit var loginViewModel: LoginViewModel;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming)

        loginViewModel = LoginViewModel(this.application);
        checkSession()
    }

    private fun checkSession(){
        val preferences = application.getSharedPreferences("user", Context.MODE_PRIVATE)

        loginViewModel.checkToken(preferences.getString("token", null), preferences.getString("username",null)).observe(this, Observer<Boolean> { validToken ->
            if (validToken == false) {
                val intent = Intent(applicationContext, LoginActivity::class.java);
                startActivity(intent)
                finish()
            }
        })
    }
}