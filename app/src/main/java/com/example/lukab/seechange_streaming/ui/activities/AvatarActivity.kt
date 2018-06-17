package com.example.lukab.seechange_streaming.ui.activities

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.lukab.seechange_streaming.R
import com.example.lukab.seechange_streaming.viewModel.UserSettingsViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.net.URI

class AvatarActivity : AppCompatActivity(), PermissionListener {
    private lateinit var userSettingsViewModel: UserSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avatar)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        this.userSettingsViewModel = UserSettingsViewModel(application, "svenwesterlaken")

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(this)
                .check()

        val profilePicture: CircleImageView = findViewById(R.id.profile_image)
        Glide.with(this).load("http://goo.gl/gEgYUd").into(profilePicture)
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
