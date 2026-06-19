package com.example.mediaplayerprep.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.mediaplayerprep.data.InMemoryVideoCatalog
import com.example.mediaplayerprep.player.PlaybackStatus
import com.example.mediaplayerprep.player.PlayerControllerFactory
import com.example.mediaplayerprep.player.PlayerSnapshot

@OptIn(UnstableApi::class)
@Composable
fun PlayerRoot(
    videoId: String,
    catalog: InMemoryVideoCatalog,
    controllerFactory: PlayerControllerFactory,
    onBack: () -> Unit
) {
    val viewModel = remember(videoId) {
        PlayerViewModel(videoId, catalog, controllerFactory.create())
    }
    val state = viewModel.state.collectAsStateWithLifecycle().value
    FullscreenOrientationEffect(state.isFullscreen)
    PlayerScreen(
        state = state,
        player = viewModel.controller,
        onAction = viewModel::onAction,
        onBack = onBack
    )
}

@Composable
fun PlayerScreen(
    state: PlayerScreenState,
    player: com.example.mediaplayerprep.player.PlayerController,
    onAction: (PlayerAction) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(state.video?.title.orEmpty(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f).background(MaterialTheme.colorScheme.surfaceVariant)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PlayerView(context).apply {
                        useController = false
                        this.player = player.player
                    }
                },
                update = { it.player = player.player }
            )
            if (state.snapshot.status == PlaybackStatus.Loading || state.snapshot.status == PlaybackStatus.Buffering) {
                LinearProgressIndicator(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth())
            }
        }
        PlayerControls(state.snapshot, onAction)
        state.snapshot.userMessage?.let {
            ErrorPanel(message = it, technical = state.snapshot.technicalError, onRetry = { onAction(PlayerAction.Retry) })
        }
        if (state.showDebugPanel) {
            DebugPanel(state.snapshot)
        }
    }
}

@Composable
fun PlayerControls(snapshot: PlayerSnapshot, onAction: (PlayerAction) -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onAction(PlayerAction.PlayPause) }) {
                Icon(if (snapshot.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play or pause")
            }
            IconButton(onClick = { onAction(PlayerAction.ToggleMute) }) {
                Icon(if (snapshot.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = "Mute or unmute")
            }
            IconButton(onClick = { onAction(PlayerAction.ToggleDebugPanel) }) {
                Icon(Icons.Default.BugReport, contentDescription = "Toggle debug panel")
            }
            IconButton(onClick = { onAction(PlayerAction.ToggleFullscreen) }) {
                Icon(Icons.Default.Fullscreen, contentDescription = "Toggle fullscreen")
            }
            Text(snapshot.status.name, color = MaterialTheme.colorScheme.secondary)
        }
        Slider(
            value = snapshot.positionMs.toFloat(),
            onValueChange = { onAction(PlayerAction.SeekTo(it.toLong())) },
            valueRange = 0f..snapshot.durationMs.coerceAtLeast(1L).toFloat()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMs(snapshot.positionMs))
            Text(formatMs(snapshot.bufferedPositionMs) + " buffered")
            Text(formatMs(snapshot.durationMs))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                AssistChip(
                    onClick = { onAction(PlayerAction.SetSpeed(speed)) },
                    label = { Text("${speed}x") }
                )
            }
        }
    }
}

@Composable
private fun ErrorPanel(message: String, technical: String?, onRetry: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(message, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
        technical?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Button(onClick = onRetry) {
            Icon(Icons.Default.Replay, contentDescription = null)
            Text("Retry")
        }
    }
}

@Composable
fun DebugPanel(snapshot: PlayerSnapshot) {
    val d = snapshot.diagnostics
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Diagnostics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Bitrate: ${d.bitrate?.let { "${it / 1000} kbps" } ?: "unknown"}")
        Text("Dropped frames: ${d.droppedFrames}")
        Text("Video: ${d.selectedVideoTrack}")
        Text("Audio: ${d.selectedAudioTrack}")
        Text("Text: ${d.selectedTextTrack}")
        Text("Position: ${d.playbackPositionMs} ms")
        Text("Buffered: ${d.bufferedPositionMs} ms")
        Text("Player state: ${d.playerState}")
        Text("Time to first frame: ${d.timeToFirstFrameMs?.let { "$it ms" } ?: "pending"}")
        Text("Captions: one HLS item attaches a WebVTT sidecar; production apps should surface Media3 text track selection.")
    }
}

@Composable
private fun FullscreenOrientationEffect(isFullscreen: Boolean) {
    val activity = LocalContext.current as? Activity
    DisposableEffect(isFullscreen) {
        val previous = activity?.requestedOrientation
        activity?.requestedOrientation =
            if (isFullscreen) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {
            if (previous != null) activity.requestedOrientation = previous
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
