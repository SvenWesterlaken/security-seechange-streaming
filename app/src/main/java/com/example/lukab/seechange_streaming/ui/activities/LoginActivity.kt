package com.example.lukab.seechange_streaming.ui.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import com.example.lukab.seechange_streaming.R
import android.widget.Toast
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel
import javax.inject.Inject;
import kotlin.math.log

class LoginActivity : BaseActivity() {


    lateinit var loginViewModel: LoginViewModel;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginViewModel = LoginViewModel(this.application);
        setContentView(R.layout.activity_login)

    }

    fun login(view: View) {
        val username =  this.findViewById<EditText>(R.id.editText)
        val password = this.findViewById<EditText>(R.id.editText2)
        if (loginViewModel.isUsernameAndPasswordValid(username.text.toString(), password.text.toString())) {
            loginViewModel.login(username.text.toString(), password.text.toString())
                        openStreamingActivity();

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
