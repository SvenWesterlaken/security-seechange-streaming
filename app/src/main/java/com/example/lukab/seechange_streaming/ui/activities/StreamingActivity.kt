package com.example.lukab.seechange_streaming.ui.activities

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming)

        this.panelSlider = findViewById(R.id.sliding_layout)
        this.chatInputText = findViewById(R.id.ChatEditText)
        this.chatInputText.onFocusChangeListener = this
    }

    override fun onBackPressed() {
        if (this.panelSlider.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            this.panelSlider.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    fun checkSession(){
        val preferences = application.getSharedPreferences("user", Context.MODE_PRIVATE)
        preferences.getString("token", null);

    }
        loginViewModel.checkToken(preferences.getString("token", null), preferences.getString("username", null))

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
}
