package com.rakib.voicesync_player.utils

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import com.rakib.voicesync_player.AppStatsData.isAudioRecorded
import com.rakib.voicesync_player.AppStatsData.isAudioRecording
import com.rakib.voicesync_player.MediaMetaData
import java.io.IOException

private const val LOG_TAG = "AudioRecorder"

class AudioRecorder(context: Context) {
    private val mContext = context
    private var mediaRecorder: MediaRecorder? = null

    fun startRecording() {
        // Record at external cache directory for visibility
        MediaMetaData.outputAudioPath =
            "${mContext.externalCacheDir?.absolutePath}/audio_recording.mp3"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(MediaMetaData.outputAudioPath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
                isAudioRecording = true
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }

        mediaRecorder = null

        isAudioRecording = false
        isAudioRecorded = true
    }

    fun compareAudioWithVideo(startTime: Int, endTime: Int): Boolean {
        var isRecordable = false

        if (startTime > endTime) {
            Toast.makeText(
                mContext,
                "Start time can't be longer than end time.",
                Toast.LENGTH_LONG
            ).show()
        } else if (startTime == endTime) {
            Toast.makeText(
                mContext,
                "Start time and end time can't be same.",
                Toast.LENGTH_LONG
            ).show()
        } else if (endTime > MediaMetaData.videoDurationInSec) {
            Toast.makeText(
                mContext,
                "End time can't be longer than video duration.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            isRecordable = true
        }

        return isRecordable
    }
}