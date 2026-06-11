package com.example.plexplayerprep.ui.feed

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.plexplayerprep.data.InMemoryVideoCatalog
import com.example.plexplayerprep.player.PlaybackStatus
import com.example.plexplayerprep.player.PlayerControllerFactory

@OptIn(UnstableApi::class)
@Composable
fun FeedRoot(
    catalog: InMemoryVideoCatalog,
    controllerFactory: PlayerControllerFactory,
    onBack: () -> Unit
) {
    val viewModel = remember { FeedViewModel(catalog, controllerFactory) }
    val state = viewModel.state.collectAsStateWithLifecycle().value
    FeedScreen(
        state = state,
        activePlayer = viewModel.activeController,
        onAction = viewModel::onAction,
        onBack = onBack
    )
}

@Composable
fun FeedScreen(
    state: FeedState,
    activePlayer: com.example.plexplayerprep.player.PlayerController,
    onAction: (FeedAction) -> Unit,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { state.videos.size })
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { onAction(FeedAction.ActivePageChanged(it)) }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val video = state.videos[page]
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                if (page == state.activeIndex) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            PlayerView(context).apply {
                                useController = false
                                player = activePlayer.player
                            }
                        },
                        update = { it.player = activePlayer.player }
                    )
                } else {
                    Text(
                        text = video.posterHint,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(video.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(video.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("State: ${if (page == state.activeIndex) state.snapshot.status.name else "Inactive"}")
                    if (state.snapshot.status == PlaybackStatus.Error && page == state.activeIndex) {
                        Text(state.snapshot.userMessage.orEmpty(), color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Row {
                IconButton(onClick = { onAction(FeedAction.PlayPause) }) {
                    Icon(if (state.snapshot.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play or pause")
                }
                IconButton(onClick = { onAction(FeedAction.ToggleMute) }) {
                    Icon(if (state.snapshot.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = "Mute or unmute")
                }
                IconButton(onClick = { onAction(FeedAction.Retry) }) {
                    Icon(Icons.Default.Replay, contentDescription = "Retry")
                }
            }
        }
    }
}
