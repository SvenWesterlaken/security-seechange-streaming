package com.example.lukab.seechange_streaming.ui.activities

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel

class StreamingActivity : BaseActivity() {

    lateinit var loginViewModel: LoginViewModel;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming)

        loginViewModel = LoginViewModel(this.application);
       // checkSession()
    }

    fun checkSession(){
        val preferences = application.getSharedPreferences("user", Context.MODE_PRIVATE)
        preferences.getString("token", null);

        loginViewModel.checkToken(preferences.getString("token", null), preferences.getString("username", null))

    }

}
