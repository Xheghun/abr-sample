package com.example.plexplayerprep.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plexplayerprep.domain.SampleVideo
import com.example.plexplayerprep.domain.VideoCatalog
import com.example.plexplayerprep.player.PlayerController
import com.example.plexplayerprep.player.PlayerSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerScreenState(
    val video: SampleVideo? = null,
    val snapshot: PlayerSnapshot = PlayerSnapshot(),
    val showDebugPanel: Boolean = true,
    val isFullscreen: Boolean = false
)

sealed interface PlayerAction {
    data object PlayPause : PlayerAction
    data object ToggleMute : PlayerAction
    data object Retry : PlayerAction
    data object ToggleDebugPanel : PlayerAction
    data object ToggleFullscreen : PlayerAction
    data class SeekTo(val positionMs: Long) : PlayerAction
    data class SetSpeed(val speed: Float) : PlayerAction
}

class PlayerViewModel(
    videoId: String,
    catalog: VideoCatalog,
    val controller: PlayerController
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerScreenState(video = catalog.video(videoId)))
    val state: StateFlow<PlayerScreenState> = _state.asStateFlow()

    init {
        val videos = catalog.videos()
        val video = _state.value.video
        if (video != null) {
            controller.load(video)
            videos.getOrNull(videos.indexOfFirst { it.id == video.id } + 1)?.let(controller::preload)
        }
        viewModelScope.launch {
            controller.snapshot.collect { snapshot ->
                _state.update { PlayerScreenReducer.applySnapshot(it, snapshot) }
            }
        }
    }

    fun onAction(action: PlayerAction) {
        when (action) {
            PlayerAction.PlayPause -> if (state.value.snapshot.isPlaying) controller.pause() else controller.play()
            PlayerAction.ToggleMute -> controller.setMuted(!state.value.snapshot.isMuted)
            PlayerAction.Retry -> controller.retry()
            PlayerAction.ToggleDebugPanel -> _state.update(PlayerScreenReducer::toggleDebugPanel)
            PlayerAction.ToggleFullscreen -> _state.update(PlayerScreenReducer::toggleFullscreen)
            is PlayerAction.SeekTo -> controller.seekTo(action.positionMs)
            is PlayerAction.SetSpeed -> controller.setPlaybackSpeed(action.speed)
        }
    }

    override fun onCleared() {
        controller.release()
    }
}
