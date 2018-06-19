package com.example.lukab.seechange_streaming.ui.activities

import com.example.lukab.seechange_streaming.viewModel.LoginViewModel
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.ui.custom.closeSoftKeyboard
import com.sothree.slidinguppanel.SlidingUpPanelLayout


class StreamingActivity : BaseActivity(), View.OnFocusChangeListener {
    private lateinit var panelSlider: SlidingUpPanelLayout
    private lateinit var chatInputText: EditText
    private lateinit var loginViewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming)

        this.panelSlider = findViewById(R.id.sliding_layout)
        this.chatInputText = findViewById(R.id.ChatEditText)
        this.chatInputText.onFocusChangeListener = this
        this.loginViewModel = LoginViewModel(this.application)

        checkSession();
    }

    override fun onBackPressed() {
        if (this.panelSlider.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            this.panelSlider.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }


    fun settingsButtonClick(btn: View?) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun outsideEditTextClick(v: View?) {
        if (this.chatInputText.hasFocus()) {
            this.chatInputText.clearFocus()
            closeSoftKeyboard(this)
        }
    }

    override fun onFocusChange(v: View?, isFocused: Boolean) {
        this.panelSlider.isTouchEnabled = !isFocused

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

