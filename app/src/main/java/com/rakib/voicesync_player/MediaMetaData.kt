package com.rakib.voicesync_player

import android.net.Uri

object MediaMetaData {
    lateinit var sourceVideoUri: Uri
    lateinit var outputAudioUri: Uri
    var videoDurationInSec: Int = 0
    var videoSizeInKB: Int = 0
}