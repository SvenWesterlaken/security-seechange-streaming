package com.example.lukab.seechange_streaming.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.service.model.Message
import com.example.lukab.seechange_streaming.ui.adapters.MessageAdapter
import com.example.lukab.seechange_streaming.ui.custom.closeSoftKeyboard
import com.example.lukab.seechange_streaming.viewModel.ChatViewModel
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel
import com.github.nkzawa.emitter.Emitter
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.json.JSONObject


class StreamingActivity : BaseActivity(), View.OnFocusChangeListener {
    private lateinit var panelSlider: SlidingUpPanelLayout
    private lateinit var chatInputText: EditText
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatView: RecyclerView
    private lateinit var chatAdapter: RecyclerView.Adapter<MessageAdapter.ViewHolder>
    private var chatMessages: ArrayList<Message> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streaming)

        this.panelSlider = findViewById(R.id.sliding_layout)
        this.chatInputText = findViewById(R.id.ChatEditText)
        this.chatInputText.onFocusChangeListener = this

        chatAdapter = MessageAdapter(this, chatMessages)

        this.chatView = findViewById(R.id.ChatRecyclerView)
        chatView.layoutManager = LinearLayoutManager(this)
        chatView.adapter = chatAdapter

        initLoginViewModel()
        initChatViewModel()
    }

    private fun initLoginViewModel() {
        this.loginViewModel = LoginViewModel(this.application)
    }

    private fun initChatViewModel() {
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val url = "http://${defaultSharedPreferences.getString("pref_seechange_ip", "145.49.56.174")}:${defaultSharedPreferences.getString("pref_seechange_chat_port", "3000")}"

        val sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val privateKey = sharedPreferences.getString("private_key", null)
        val token = sharedPreferences.getString("token", null)

        this.chatViewModel = ChatViewModel(url, username, privateKey)
        this.chatViewModel.connect()

        this.chatViewModel.addErrorListener(errorListener)
        this.chatViewModel.addMessageListener(newMessageListener)

        this.chatViewModel.authenticate(token, authenticationListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribeFromChat()
        this.chatViewModel.destroy()
    }

    override fun onBackPressed() {
        if (this.panelSlider.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            this.panelSlider.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun onFocusChange(v: View?, isFocused: Boolean) {
        this.panelSlider.isTouchEnabled = !isFocused
    }

    fun sendMessage(v: View?) {
        this.chatViewModel.sendMessage(chatInputText.text.toString())
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

    fun checkSession() {
        val preferences = application.getSharedPreferences("user", Context.MODE_PRIVATE)
        preferences.getString("token", null)
        loginViewModel.checkToken(preferences.getString("token", null), preferences.getString("username", null))
    }

    private fun subscribeToChat() {
        this.chatViewModel.subscribe()
    }

    private fun unsubscribeFromChat() {
        this.chatViewModel.unsubscribe()
    }

    private fun addMessage(message: Message) {
        this.chatMessages.add(message)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        chatView.scrollToPosition(chatAdapter.itemCount - 1)
    }

    // ------------------------------------------------------------------------------
    //
    //    Listeners for Chat (Powered by Socket.io)
    //
    // ------------------------------------------------------------------------------

    private val errorListener = Emitter.Listener { args ->
        this.runOnUiThread {
            val data = args[0] as JSONObject
            val message = data.getString("msg")
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private val newMessageListener = Emitter.Listener { args ->
        this.runOnUiThread {
            val data = args[0] as JSONObject
            val username = data.getString("username")
            val message = data.getString("msg")
            val timestamp = data.getLong("timestamp")
            val hash = data.getString("hash")

            addMessage(Message(this.chatViewModel.isStreamer(username), message, username, timestamp, hash))
        }
    }

    private val authenticationListener = Emitter.Listener { args ->
        this.runOnUiThread {
            val data = args[0] as JSONObject
            val message = data.getString("msg")
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            subscribeToChat()
        }
    }
}
