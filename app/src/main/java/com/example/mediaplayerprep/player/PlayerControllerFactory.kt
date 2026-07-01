package com.example.mediaplayerprep.player

import android.content.Context
import androidx.media3.common.util.UnstableApi

@UnstableApi
class PlayerControllerFactory(
    private val context: Context,
    private val sharedMutedState: SharedMutedState = SharedMutedState(),
    private val defaultTuning: PlaybackTuning = PlaybackTuning.Balanced
) {
    fun create(): PlayerController = ExoPlayerController(context, sharedMutedState, defaultTuning)
}
