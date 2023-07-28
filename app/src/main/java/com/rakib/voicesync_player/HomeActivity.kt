package com.rakib.voicesync_player

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rakib.voicesync_player.databinding.ActivityHomeBinding
import java.io.File

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var mediaController: MediaController
    private lateinit var mediaRecorder: MediaRecorder
    private var isVideoSelected = false
    private var isAudioRecorded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Request all permissions
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this@HomeActivity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.addVideoAnimationView.setOnClickListener {
            pickVideoFromStorage()
        }
    }

    private fun controlViewVisibility() {
        if (isVideoSelected) {
            binding.emptyVideoAnimationView.visibility = View.INVISIBLE
            binding.videoView.visibility = View.VISIBLE
            binding.startTimeFilledTextField.visibility = View.VISIBLE
            binding.endTimeFilledTextField.visibility = View.VISIBLE
            binding.recordingAnimationView.visibility = View.VISIBLE

            if (isAudioRecorded) {
                binding.playButton.visibility = View.VISIBLE
            }
        }
    }

    private fun pickVideoFromStorage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_VIDEO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_VIDEO && resultCode == Activity.RESULT_OK) {
            MediaMetaData.sourceVideoUri = data?.data!!

            getVideoStats(MediaMetaData.sourceVideoUri)

            isVideoSelected = true
            controlViewVisibility()
        }
    }

    private fun getVideoStats(videoUri: Uri) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this@HomeActivity, videoUri)

        MediaMetaData.videoDurationInSec =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt() / 1000
        MediaMetaData.videoSizeInKB = File(videoUri.path!!).length().toInt() / 1024

        playVideo(MediaMetaData.sourceVideoUri)

        Toast.makeText(
            this@HomeActivity, "Duration: ${MediaMetaData.videoDurationInSec} s\n" +
                    "Size: ${MediaMetaData.videoSizeInKB} KB", Toast.LENGTH_LONG
        ).show()
    }

    private fun playVideo(videoUri: Uri) {
        mediaController = MediaController(this@HomeActivity)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)

        // Set the video URI to the VideoView
        binding.videoView.setVideoURI(videoUri)

        // Start video playback
        binding.videoView.start()
    }

    // Process result from permission request dialog box
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            Toast.makeText(this@HomeActivity, "Permission Allowed", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@HomeActivity, "Permission Denied", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Check if all permissions specified in the manifest have been granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this@HomeActivity,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val REQUEST_CODE_PICK_VIDEO = 11

        // An array of all the permissions specified in the manifest
        private val REQUIRED_PERMISSIONS = mutableListOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}