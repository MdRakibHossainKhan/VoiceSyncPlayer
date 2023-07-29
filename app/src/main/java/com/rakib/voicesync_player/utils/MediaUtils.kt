package com.rakib.voicesync_player.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import com.rakib.voicesync_player.MediaMetaData
import java.io.File

class MediaUtils(context: Context) {
    private val mContext = context

    fun getVideoStats(videoUri: Uri) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(mContext, videoUri)

        MediaMetaData.videoDurationInSec =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt() / 1000
        MediaMetaData.videoSizeInKB = File(videoUri.path!!).length().toInt() / 1024

        Toast.makeText(
            mContext, "Duration: ${MediaMetaData.videoDurationInSec} s\n" +
                    "Size: ${MediaMetaData.videoSizeInKB} KB", Toast.LENGTH_LONG
        ).show()
    }
}