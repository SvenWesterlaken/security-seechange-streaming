package com.example.lukab.seechange_streaming.ui.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.lukab.seechange_streaming.R

class StreamingActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming)

        val SettingsButton: View = findViewById(R.id.SettingsImageView)
        SettingsButton.setOnClickListener(this)

    }

    override fun onClick(btn: View?) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
