package com.rakib.voicesync_player

import android.net.Uri

object MediaMetaData {
    lateinit var sourceVideoUri: Uri
    var outputAudioPath: String = ""
    var videoDurationInSec: Int = 0
    var videoSizeInKB: Int = 0
    var audioStartTime: Int = 0
    var audioEndTime: Int = 0
}