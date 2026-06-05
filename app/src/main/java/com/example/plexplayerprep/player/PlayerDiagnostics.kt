package com.example.plexplayerprep.player

data class PlayerDiagnostics(
    val bitrate: Int? = null,
    val droppedFrames: Int = 0,
    val selectedVideoTrack: String = "unknown",
    val selectedAudioTrack: String = "unknown",
    val selectedTextTrack: String = "none",
    val playbackPositionMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playerState: String = "Idle",
    val timeToFirstFrameMs: Long? = null
)
