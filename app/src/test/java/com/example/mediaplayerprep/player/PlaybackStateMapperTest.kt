package com.example.mediaplayerprep.player

import androidx.media3.common.Player
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class PlaybackStateMapperTest {
    @Test
    fun `ready maps to playing when playWhenReady is true`() {
        val result = PlaybackStateMapper.map(Player.STATE_READY, playWhenReady = true, hasError = false)

        assertThat(result).isEqualTo(PlaybackStatus.Playing)
    }

    @Test
    fun `ready maps to paused when playWhenReady is false`() {
        val result = PlaybackStateMapper.map(Player.STATE_READY, playWhenReady = false, hasError = false)

        assertThat(result).isEqualTo(PlaybackStatus.Paused)
    }

    @Test
    fun `error wins over raw player state`() {
        val result = PlaybackStateMapper.map(Player.STATE_READY, playWhenReady = true, hasError = true)

        assertThat(result).isEqualTo(PlaybackStatus.Error)
    }
}
