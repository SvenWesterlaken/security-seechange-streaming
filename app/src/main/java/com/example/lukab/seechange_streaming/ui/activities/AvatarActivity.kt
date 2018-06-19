package com.example.lukab.seechange_streaming.ui.activities

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.StringSignature
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.app.util.avatarUrl
import com.example.lukab.seechange_streaming.viewModel.UserSettingsViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class AvatarActivity : AppCompatActivity(), PermissionListener {
    private lateinit var userSettingsViewModel: UserSettingsViewModel
    private lateinit var profilePicture: CircleImageView
    private lateinit var avatarImageUrl: String
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avatar)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val username = "svenwesterlaken"
        this.avatarImageUrl = "http://${sharedPreferences.getString("pref_seechange_ip", "145.49.56.174")}:${sharedPreferences.getString("pref_seechange_api_port", "8081")}$avatarUrl?username=$username"

        this.userSettingsViewModel = UserSettingsViewModel(application, username)

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(this)
                .check()

        this.profilePicture = findViewById(com.example.lukab.seechange_streaming.R.id.profile_image)
        Glide.with(this).load(avatarImageUrl).signature(StringSignature(sharedPreferences.getLong("pref_avatar", 0).toString())).into(profilePicture)
    }

    fun onUploadButtonClick(v: View?) {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            uploadImage(File(getRealPathFromUri(data!!.data)))
        }
    }

    private fun uploadImage(image: File) {
        this.userSettingsViewModel.uploadAvatar(image).observe(this, Observer<Boolean> { succeeded ->
            if(succeeded!!) {
                Toast.makeText(this, "Upload Successful", Toast.LENGTH_SHORT).show()
                val lastModified = System.currentTimeMillis()
                sharedPreferences.edit().putLong("pref_avatar", lastModified).apply()

                Glide.with(this).load(this.avatarImageUrl).signature(StringSignature(lastModified.toString())).into(profilePicture)
            } else {
                Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getRealPathFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

        cursor.moveToFirst()

        val picturePath = cursor.getString(columnIndex)
        cursor.close()

        return picturePath
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        Toast.makeText(this, "Permission Successful", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
    }



}
