package com.example.plexplayerprep.ui.player

import androidx.media3.exoplayer.ExoPlayer
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.plexplayerprep.domain.SampleVideo
import com.example.plexplayerprep.domain.StreamType
import com.example.plexplayerprep.domain.VideoCatalog
import com.example.plexplayerprep.player.PlaybackStatus
import com.example.plexplayerprep.player.PlayerController
import com.example.plexplayerprep.player.PlayerSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `retry delegates to controller after error snapshot`() = runTest {
        val controller = FakePlayerController()
        val viewModel = PlayerViewModel("video-1", FakeCatalog, controller)

        viewModel.state.test {
            awaitItem()
            controller.emit(PlayerSnapshot(status = PlaybackStatus.Error, technicalError = "404"))
            assertThat(awaitItem().snapshot.status).isEqualTo(PlaybackStatus.Error)

            viewModel.onAction(PlayerAction.Retry)

            assertThat(controller.retryCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `play pause action pauses when currently playing`() = runTest {
        val controller = FakePlayerController()
        val viewModel = PlayerViewModel("video-1", FakeCatalog, controller)
        controller.emit(PlayerSnapshot(status = PlaybackStatus.Playing, isPlaying = true))

        viewModel.onAction(PlayerAction.PlayPause)

        assertThat(controller.pauseCount).isEqualTo(1)
    }
}

private object FakeCatalog : VideoCatalog {
    private val video = SampleVideo(
        id = "video-1",
        title = "Test",
        description = "Test video",
        streamType = StreamType.Hls,
        url = "https://example.com/stream.m3u8",
        posterHint = "HLS"
    )

    override fun videos(): List<SampleVideo> = listOf(video)
    override fun video(id: String): SampleVideo? = video.takeIf { it.id == id }
}

private class FakePlayerController : PlayerController {
    private val mutableSnapshot = MutableStateFlow(PlayerSnapshot())
    var retryCount = 0
    var pauseCount = 0

    override val player: ExoPlayer
        get() = error("Fake does not expose an ExoPlayer")
    override val snapshot: StateFlow<PlayerSnapshot> = mutableSnapshot

    fun emit(snapshot: PlayerSnapshot) {
        mutableSnapshot.value = snapshot
    }

    override fun load(video: SampleVideo, playWhenReady: Boolean) = Unit
    override fun play() = Unit
    override fun pause() {
        pauseCount++
    }
    override fun retry() {
        retryCount++
    }
    override fun seekTo(positionMs: Long) = Unit
    override fun setMuted(muted: Boolean) = Unit
    override fun setPlaybackSpeed(speed: Float) = Unit
    override fun preload(video: SampleVideo) = Unit
    override fun release() = Unit
}
