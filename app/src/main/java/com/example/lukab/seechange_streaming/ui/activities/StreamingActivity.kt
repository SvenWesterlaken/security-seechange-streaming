package com.example.lukab.seechange_streaming.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.EditText
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.ui.custom.closeSoftKeyboard
import com.example.lukab.seechange_streaming.viewModel.ChatViewModel
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel
import com.sothree.slidinguppanel.SlidingUpPanelLayout


class StreamingActivity : BaseActivity(), View.OnFocusChangeListener {
    private lateinit var panelSlider: SlidingUpPanelLayout
    private lateinit var chatInputText: EditText
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming)

        this.panelSlider = findViewById(R.id.sliding_layout)
        this.chatInputText = findViewById(R.id.ChatEditText)
        this.chatInputText.onFocusChangeListener = this
        this.loginViewModel = LoginViewModel(this.application)

        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val url = "http://${defaultSharedPreferences.getString("pref_seechange_ip", "145.49.56.174")}:${defaultSharedPreferences.getString("pref_seechange_chat_port", "3000")}"

        val sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val privatekey = sharedPreferences.getString("private_key", null)

        this.chatViewModel = ChatViewModel(url, username, privatekey)
        this.chatViewModel.connect()

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

    fun checkSession() {
        val preferences = application.getSharedPreferences("user", Context.MODE_PRIVATE)
        preferences.getString("token", null)
        loginViewModel.checkToken(preferences.getString("token", null), preferences.getString("username", null))
    }
}
