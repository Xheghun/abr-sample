package com.example.mediaplayerprep.ui.player

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.example.mediaplayerprep.player.PlaybackStatus
import com.example.mediaplayerprep.player.PlayerSnapshot
import org.junit.Test

class PlayerScreenReducerTest {
    @Test
    fun `applySnapshot updates immutable state`() {
        val snapshot = PlayerSnapshot(status = PlaybackStatus.Buffering, positionMs = 3_000)

        val result = PlayerScreenReducer.applySnapshot(PlayerScreenState(), snapshot)

        assertThat(result.snapshot.status).isEqualTo(PlaybackStatus.Buffering)
        assertThat(result.snapshot.positionMs).isEqualTo(3_000)
    }

    @Test
    fun `toggleDebugPanel flips debug visibility`() {
        val result = PlayerScreenReducer.toggleDebugPanel(PlayerScreenState(showDebugPanel = true))

        assertThat(result.showDebugPanel).isFalse()
    }

    @Test
    fun `toggleFullscreen flips fullscreen state`() {
        val result = PlayerScreenReducer.toggleFullscreen(PlayerScreenState(isFullscreen = false))

        assertThat(result.isFullscreen).isTrue()
    }
}
