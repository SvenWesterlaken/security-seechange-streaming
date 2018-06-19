package com.example.lukab.seechange_streaming.ui.activities

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.viewModel.LoginViewModel
import android.os.Environment.getExternalStorageDirectory
import android.widget.Button
import android.widget.EditText
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import java.io.File
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import android.os.Build
import android.view.SurfaceHolder
import android.system.Os.mkdir
import java.nio.file.Files.exists
import com.pedro.encoder.input.video.CameraOpenException
import com.example.lukab.seechange_streaming.ui.MainActivity
import net.ossrs.rtmp.ConnectCheckerRtmp
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class StreamingActivity : BaseActivity(), ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {

    lateinit var loginViewModel: LoginViewModel
    lateinit var rtmpCamera1: RtmpCamera1
    lateinit var button: Button
    lateinit var bRecord: Button
    lateinit var etUrl: EditText

    private var currentDateAndTime = ""
    private var folder = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rtmp-rtsp-stream-client-java")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_example)
        val surfaceView = findViewById<View>(R.id.surfaceView) as SurfaceView
        button = findViewById(R.id.b_start_stop)
        button.setOnClickListener(this)
        bRecord = findViewById(R.id.b_record)
        bRecord.setOnClickListener(this)
        val switchCamera = findViewById<View>(R.id.switch_camera) as Button
        switchCamera.setOnClickListener(this)
        etUrl = findViewById(R.id.et_rtp_url)
        etUrl.setHint(R.string.hint_rtmp)
        rtmpCamera1 = RtmpCamera1(surfaceView, this)
        surfaceView.holder.addCallback(this)

        loginViewModel = LoginViewModel(this.application)
       // checkSession()
    }

    fun checkSession(){
        val preferences = application.getSharedPreferences("user", Context.MODE_PRIVATE)
        preferences.getString("token", null)

        loginViewModel.checkToken(preferences.getString("token", null), preferences.getString("username", null))

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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.b_start_stop -> if (!rtmpCamera1.isStreaming) {
                if (rtmpCamera1.isRecording || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                    button.setText(R.string.stop_button)
                    rtmpCamera1.startStream(etUrl.text.toString())
                } else {
                    Toast.makeText(this, "Error preparing stream, This device cant do it",
                            Toast.LENGTH_SHORT).show()
                }
            } else {
                button.setText(R.string.start_button)
                rtmpCamera1.stopStream()
            }
            R.id.switch_camera -> try {
                rtmpCamera1.switchCamera()
            } catch (e: CameraOpenException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }

            R.id.b_record -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
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
            else -> {
            }
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
