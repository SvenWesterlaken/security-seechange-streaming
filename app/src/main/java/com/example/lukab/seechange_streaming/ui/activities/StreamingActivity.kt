package com.example.lukab.seechange_streaming.ui.activities

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.service.model.Message
import com.example.lukab.seechange_streaming.ui.adapters.MessageAdapter
import com.example.lukab.seechange_streaming.ui.custom.closeSoftKeyboard
import com.example.lukab.seechange_streaming.viewModel.ChatViewModel
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel
import com.github.nkzawa.emitter.Emitter
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.json.JSONObject
import net.ossrs.rtmp.ConnectCheckerRtmp
import net.ossrs.rtmp.Security
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class StreamingActivity : BaseActivity(), ConnectCheckerRtmp, SurfaceHolder.Callback, View.OnFocusChangeListener {

    lateinit var loginViewModel: LoginViewModel
    lateinit var rtmpCamera1: RtmpCamera1
    lateinit var button: Button
    lateinit var bRecord: Button
    lateinit var etUrl: EditText
    private lateinit var panelSlider: SlidingUpPanelLayout
    private lateinit var chatInputText: EditText
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatView: RecyclerView
    private lateinit var chatAdapter: RecyclerView.Adapter<MessageAdapter.ViewHolder>
    private var chatMessages: ArrayList<Message> = ArrayList()

    private var currentDateAndTime = ""
    private var folder = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rtmp-rtsp-stream-client-java")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_streaming)
        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView);
        button = findViewById(R.id.b_start_stop)
        bRecord = findViewById(R.id.b_record)
        etUrl = findViewById(R.id.et_rtp_url)
        etUrl.setHint(R.string.hint_rtmp)
        rtmpCamera1 = RtmpCamera1(surfaceView, this)
        surfaceView.holder.addCallback(this)

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

        checkSession();
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

        loginViewModel.checkToken(preferences.getString("token", null), preferences.getString("username", null)).observe(this, Observer<Boolean> { validToken ->
            if (validToken == false) {
                val intent = Intent(applicationContext, LoginActivity::class.java);
                startActivity(intent)
                finish()
            }
        })
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

    override fun onConnectionSuccessRtmp() {
        runOnUiThread { Toast.makeText(this@StreamingActivity, "Connection success", Toast.LENGTH_SHORT).show() }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            Toast.makeText(this@StreamingActivity, "Connection failed. $reason", Toast.LENGTH_SHORT)
                    .show()
            rtmpCamera1.stopStream()
            button.setText(R.string.start_button)
        }
    }

    override fun onDisconnectRtmp() {
        runOnUiThread { Toast.makeText(this@StreamingActivity, "Disconnected", Toast.LENGTH_SHORT).show() }
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread { Toast.makeText(this@StreamingActivity, "Auth error", Toast.LENGTH_SHORT).show() }
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread { Toast.makeText(this@StreamingActivity, "Auth success", Toast.LENGTH_SHORT).show() }
    }

    fun startStreamButtonClick(view: View) {
        if (!rtmpCamera1.isStreaming)
        {
            if (rtmpCamera1.isRecording || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                button.setText(R.string.stop_button)
                val preferences = application.getSharedPreferences("user", Context.MODE_PRIVATE)
                val privateKeyString = preferences.getString("private_key", null)
                Log.d("StreamingActivity: ", "message: $privateKeyString")
                val privateKeyFromString = LoginViewModel.getPrivateKeyFromString(privateKeyString)
                Security.setPrivateKey(privateKeyFromString)
                rtmpCamera1.startStream(etUrl.text.toString())
            } else {
                Toast.makeText(this, "Error preparing stream, This device cant do it",
                        Toast.LENGTH_SHORT).show()
            }
        } else
        {
            button.setText(R.string.start_button)
            rtmpCamera1.stopStream()
        }
    }

    fun switchCameraClick(view: View) {
        try {
            rtmpCamera1.switchCamera()
        } catch (e: CameraOpenException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun recordClick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (!rtmpCamera1.isRecording) {
                    try {
                        if (!folder.exists()) {
                            folder.mkdir()
                        }
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        currentDateAndTime = sdf.format(Date())
                        if (!rtmpCamera1.isStreaming) {
                            if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                                rtmpCamera1.startRecord(
                                        folder.absolutePath + "/" + currentDateAndTime + ".mp4")
                                bRecord.setText(R.string.stop_record)
                                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Error preparing stream, This device cant do it",
                                        Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            rtmpCamera1.startRecord(
                                    folder.absolutePath + "/" + currentDateAndTime + ".mp4")
                            bRecord.setText(R.string.stop_record)
                            Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        rtmpCamera1.stopRecord()
                        bRecord.setText(R.string.start_record)
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }

                } else {
                    rtmpCamera1.stopRecord()
                    bRecord.setText(R.string.start_record)
                    Toast.makeText(this,
                            "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
                            Toast.LENGTH_SHORT).show()
                    currentDateAndTime = ""
                }
            } else {
                Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
                        Toast.LENGTH_SHORT).show()
            }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {

    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        rtmpCamera1.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording) {
            rtmpCamera1.stopRecord()
            bRecord.setText(R.string.start_record)
            Toast.makeText(this,
                    "file " + currentDateAndTime + ".mp4 saved in " + folder.absolutePath,
                    Toast.LENGTH_SHORT).show()
            currentDateAndTime = ""
        }
        if (rtmpCamera1.isStreaming) {
            rtmpCamera1.stopStream()
            button.text = resources.getString(R.string.start_button)
        }
        rtmpCamera1.stopPreview()
    }

}

