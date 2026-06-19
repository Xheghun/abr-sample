package com.example.mediaplayerprep.ui.player

import com.example.mediaplayerprep.player.PlayerSnapshot

object PlayerScreenReducer {
    fun applySnapshot(state: PlayerScreenState, snapshot: PlayerSnapshot): PlayerScreenState =
        state.copy(snapshot = snapshot)

    fun toggleDebugPanel(state: PlayerScreenState): PlayerScreenState =
        state.copy(showDebugPanel = !state.showDebugPanel)

    fun toggleFullscreen(state: PlayerScreenState): PlayerScreenState =
        state.copy(isFullscreen = !state.isFullscreen)
}
