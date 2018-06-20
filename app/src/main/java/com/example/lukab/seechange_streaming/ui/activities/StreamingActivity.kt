package com.example.lukab.seechange_streaming.ui.activities

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.service.model.Message
import com.example.lukab.seechange_streaming.ui.adapters.MessageAdapter
import com.example.lukab.seechange_streaming.ui.custom.closeSoftKeyboard
import com.example.lukab.seechange_streaming.viewModel.ChatViewModel
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel
import com.github.nkzawa.emitter.Emitter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import net.ossrs.rtmp.ConnectCheckerRtmp
import net.ossrs.rtmp.Security
import org.json.JSONObject
import java.util.*


class StreamingActivity : BaseActivity(), ConnectCheckerRtmp, SurfaceHolder.Callback, View.OnFocusChangeListener, MultiplePermissionsListener {
    private lateinit var camera: RtmpCamera1
    private lateinit var streamingButton: ImageView
    private lateinit var cameraSwitchButton: ImageView
    private lateinit var panelSlider: SlidingUpPanelLayout
    private lateinit var chatInputText: EditText
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatView: RecyclerView
    private lateinit var chatAdapter: RecyclerView.Adapter<MessageAdapter.ViewHolder>
    private lateinit var surfaceView: SurfaceView

    private var chatMessages: ArrayList<Message> = ArrayList()

    private var isFrontCamera = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_streaming)

        streamingButton = findViewById(R.id.camera_button)

        this.panelSlider = findViewById(R.id.sliding_layout)
        this.chatInputText = findViewById(R.id.ChatEditText)
        this.cameraSwitchButton = findViewById(R.id.switch_camera)
        this.chatInputText.onFocusChangeListener = this

        this.surfaceView = findViewById(R.id.surfaceView)

        chatAdapter = MessageAdapter(this, chatMessages)

        this.chatView = findViewById(R.id.ChatRecyclerView)
        chatView.layoutManager = LinearLayoutManager(this)
        chatView.adapter = chatAdapter

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                )
                .withListener(this)
                .check()

        initLoginViewModel()
        initChatViewModel()
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        this.camera = RtmpCamera1(this.surfaceView, this)
        this.surfaceView.holder.addCallback(this)
    }

    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {}

    private fun initLoginViewModel() {
        this.loginViewModel = LoginViewModel(this.application)
        checkSession()
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

    private fun checkSession() {
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
        runOnUiThread { Toast.makeText(this, "Connection success", Toast.LENGTH_SHORT).show() }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            Toast.makeText(this, "Connection failed. $reason", Toast.LENGTH_SHORT).show()
            camera.stopStream()
            streamingButton.background = ContextCompat.getDrawable(applicationContext,R.drawable.camera_button)
        }
    }

    override fun onDisconnectRtmp() {
        runOnUiThread { Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show() }
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread { Toast.makeText(this, "Auth error", Toast.LENGTH_SHORT).show() }
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread { Toast.makeText(this, "Auth success", Toast.LENGTH_SHORT).show() }
    }

    fun startStreamButtonClick(view: View?) {
        if (!camera.isStreaming) {
            if (camera.isRecording || camera.prepareAudio() && camera.prepareVideo()) {
                streamingButton.background = ContextCompat.getDrawable(applicationContext,R.drawable.camera_button_active)
                val preferences = application.getSharedPreferences("user", Context.MODE_PRIVATE)
                val privateKeyString = preferences.getString("private_key", null)
                Log.d("StreamingActivity: ", "message: $privateKeyString")
                val privateKeyFromString = LoginViewModel.getPrivateKeyFromString(privateKeyString)
                Security.setPrivateKey(privateKeyFromString)

                val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val streamUrl = "http://${defaultSharedPreferences.getString("pref_seechange_ip", "145.49.56.174")}:${defaultSharedPreferences.getString("pref_seechange_stream_port", "1935")}"

                camera.startStream(streamUrl)
            } else {
                Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show()
            }
        } else {
            streamingButton.background = ContextCompat.getDrawable(applicationContext,R.drawable.camera_button)
            camera.stopStream()
        }
    }

    fun switchCameraClick(view: View?) {
        try {
            camera.switchCamera()


            if (isFrontCamera) {
                this.cameraSwitchButton.background = ContextCompat.getDrawable(applicationContext,R.drawable.camera_front_variant)
                this.isFrontCamera = false
            } else {
                this.cameraSwitchButton.background = ContextCompat.getDrawable(applicationContext,R.drawable.camera_rear_variant)
                this.isFrontCamera = true
            }

        } catch (e: CameraOpenException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {}

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        camera.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && camera.isRecording) {
            camera.stopRecord()
        }

        if (camera.isStreaming) {
            camera.stopStream()
        }

        camera.stopPreview()
    }

}

