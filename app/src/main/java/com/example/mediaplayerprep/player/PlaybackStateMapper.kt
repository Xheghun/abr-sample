package com.example.mediaplayerprep.player

import androidx.media3.common.Player

object PlaybackStateMapper {
    fun map(playbackState: Int, playWhenReady: Boolean, hasError: Boolean): PlaybackStatus {
        if (hasError) return PlaybackStatus.Error
        return when (playbackState) {
            Player.STATE_IDLE -> PlaybackStatus.Idle
            Player.STATE_BUFFERING -> if (playWhenReady) PlaybackStatus.Buffering else PlaybackStatus.Loading
            Player.STATE_READY -> if (playWhenReady) PlaybackStatus.Playing else PlaybackStatus.Paused
            Player.STATE_ENDED -> PlaybackStatus.Ended
            else -> PlaybackStatus.Idle
        }
    }
}
