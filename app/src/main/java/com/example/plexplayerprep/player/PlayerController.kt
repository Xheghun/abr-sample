package com.example.plexplayerprep.player

import androidx.media3.exoplayer.ExoPlayer
import com.example.plexplayerprep.domain.SampleVideo
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val player: ExoPlayer
    val snapshot: StateFlow<PlayerSnapshot>

    fun load(video: SampleVideo, playWhenReady: Boolean = true)
    fun play()
    fun pause()
    fun retry()
    fun seekTo(positionMs: Long)
    fun setMuted(muted: Boolean)
    fun setPlaybackSpeed(speed: Float)
    fun preload(video: SampleVideo)
    fun release()
}
