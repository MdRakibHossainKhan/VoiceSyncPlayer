package com.rakib.voicesync_player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rakib.voicesync_player.AppStatsData.isAudioRecorded
import com.rakib.voicesync_player.AppStatsData.isAudioRecording
import com.rakib.voicesync_player.AppStatsData.isVideoSelected
import com.rakib.voicesync_player.MediaMetaData.audioEndTime
import com.rakib.voicesync_player.MediaMetaData.audioStartTime
import com.rakib.voicesync_player.MediaMetaData.outputAudioPath
import com.rakib.voicesync_player.MediaMetaData.sourceVideoUri
import com.rakib.voicesync_player.databinding.ActivityHomeBinding
import com.rakib.voicesync_player.utils.AudioRecorder
import com.rakib.voicesync_player.utils.MediaUtils

class HomeActivity : AppCompatActivity() {
    private val context = this@HomeActivity
    private var mediaUtils = MediaUtils(context)
    private lateinit var binding: ActivityHomeBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Request all permissions
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                context,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.addVideoAnimationView.setOnClickListener {
            pickVideoFromStorage()
        }

        val audioRecorder = AudioRecorder(context)

        binding.recordingAnimationView.setOnClickListener {
            if (isAudioRecording) {
                audioRecorder.stopRecording()
                controlViewVisibility()
            } else {
                // Get start and end time
                val startTimeString = binding.startTimeFilledTextField.editText?.text.toString()
                val endTimeString = binding.endTimeFilledTextField.editText?.text.toString()

                if (startTimeString.isNotEmpty() && endTimeString.isNotEmpty()) {
                    audioStartTime = startTimeString.toInt()
                    audioEndTime = endTimeString.toInt()

                    if (audioRecorder.compareAudioWithVideo(audioStartTime, audioEndTime)) {
                        audioRecorder.startRecording()
                        controlViewVisibility()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Provide valid start and end time.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        binding.playButton.setOnClickListener {
            val delayInMillis = audioStartTime * 1000
            playVideoWithVoiceOver(sourceVideoUri, outputAudioPath, delayInMillis.toLong())
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            currentFocus?.applicationWindowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    private fun controlViewVisibility() {
        if (isVideoSelected) {
            binding.emptyVideoAnimationView.visibility = View.INVISIBLE
            binding.videoView.visibility = View.VISIBLE
            binding.startTimeFilledTextField.visibility = View.VISIBLE
            binding.endTimeFilledTextField.visibility = View.VISIBLE
            binding.recordingAnimationView.visibility = View.VISIBLE

            if (isAudioRecording) {
                hideKeyboard()
                binding.recordingAnimationView.playAnimation()
            } else {
                binding.recordingAnimationView.pauseAnimation()

                if (isAudioRecorded) {
                    binding.playButton.visibility = View.VISIBLE
                }
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
            sourceVideoUri = data?.data!!
            mediaUtils.getVideoStats(sourceVideoUri)
            isVideoSelected = true
            controlViewVisibility()
            playVideo(sourceVideoUri)
        }
    }

    private fun playVideo(videoUri: Uri) {
        val mediaController = MediaController(this)
        mediaController.apply {
            setAnchorView(binding.videoView)
            binding.videoView.setMediaController(mediaController)
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.start()
        }
    }

    private fun playVideoWithVoiceOver(videoPath: Uri, voiceOverPath: String, delayInMillis: Long) {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(context, videoPath)
            setOnPreparedListener {
                binding.videoView.start()
            }
            prepareAsync()
        }

        mediaPlayer.setOnSeekCompleteListener {
            mediaPlayer.apply {
                reset()
                setDataSource(voiceOverPath)
                setOnPreparedListener {
                    start()
                }
                prepareAsync()
            }
        }

        // Calculate the delay based on user input (start and end time) and adjust playback accordingly
        Handler().postDelayed({
            mediaPlayer.seekTo(0) // Reset the video playback to the beginning before playing the voice-over
            mediaPlayer.start()
        }, delayInMillis)
    }

    // Process result from permission request dialog box
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            Toast.makeText(context, "Permission Allowed", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Check if all permissions specified in the manifest have been granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            context,
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