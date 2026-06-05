package com.example.plexplayerprep.player

import android.content.Context
import androidx.media3.common.util.UnstableApi

@UnstableApi
class PlayerControllerFactory(
    private val context: Context,
    private val sharedMutedState: SharedMutedState = SharedMutedState()
) {
    fun create(): PlayerController = ExoPlayerController(context, sharedMutedState)
}
