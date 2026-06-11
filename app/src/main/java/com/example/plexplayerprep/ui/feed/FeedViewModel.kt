package com.example.plexplayerprep.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plexplayerprep.domain.SampleVideo
import com.example.plexplayerprep.domain.VideoCatalog
import com.example.plexplayerprep.player.PlayerController
import com.example.plexplayerprep.player.PlayerControllerFactory
import com.example.plexplayerprep.player.PlayerSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedState(
    val videos: List<SampleVideo> = emptyList(),
    val activeIndex: Int = 0,
    val snapshot: PlayerSnapshot = PlayerSnapshot()
)

sealed interface FeedAction {
    data class ActivePageChanged(val index: Int) : FeedAction
    data object PlayPause : FeedAction
    data object ToggleMute : FeedAction
    data object Retry : FeedAction
}

class FeedViewModel(
    catalog: VideoCatalog,
    private val controllerFactory: PlayerControllerFactory
) : ViewModel() {
    private val _state = MutableStateFlow(FeedState(videos = catalog.videos()))
    val state: StateFlow<FeedState> = _state.asStateFlow()

    var activeController: PlayerController = controllerFactory.create()
        private set

    private var collectJob: Job? = null

    init {
        loadActive(index = 0)
    }

    fun onAction(action: FeedAction) {
        when (action) {
            is FeedAction.ActivePageChanged -> loadActive(action.index)
            FeedAction.PlayPause -> if (state.value.snapshot.isPlaying) activeController.pause() else activeController.play()
            FeedAction.ToggleMute -> activeController.setMuted(!state.value.snapshot.isMuted)
            FeedAction.Retry -> activeController.retry()
        }
    }

    private fun loadActive(index: Int) {
        val video = state.value.videos.getOrNull(index) ?: return
        if (index == state.value.activeIndex && state.value.snapshot.durationMs > 0) return
        collectJob?.cancel()
        activeController.release()
        activeController = controllerFactory.create()
        activeController.load(video, playWhenReady = true)
        state.value.videos.getOrNull(index + 1)?.let(activeController::preload)
        _state.update { it.copy(activeIndex = index, snapshot = PlayerSnapshot(isMuted = it.snapshot.isMuted)) }
        collectJob = viewModelScope.launch {
            activeController.snapshot.collect { snapshot ->
                _state.update { it.copy(snapshot = snapshot) }
            }
        }
    }

    override fun onCleared() {
        collectJob?.cancel()
        activeController.release()
    }
}
