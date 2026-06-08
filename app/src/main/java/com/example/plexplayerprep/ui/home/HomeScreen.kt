package com.example.plexplayerprep.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.plexplayerprep.data.InMemoryVideoCatalog
import com.example.plexplayerprep.domain.SampleVideo
import com.example.plexplayerprep.ui.theme.PlexPrepTheme

@Composable
fun HomeRoot(
    catalog: InMemoryVideoCatalog,
    onOpenVideo: (String) -> Unit,
    onOpenFeed: () -> Unit
) {
    val viewModel = remember(catalog) { HomeViewModel(catalog) }
    val state = viewModel.state.collectAsStateWithLifecycle().value
    HomeScreen(state = state, onOpenVideo = onOpenVideo, onOpenFeed = onOpenFeed)
}

@Composable
fun HomeScreen(
    state: HomeState,
    onOpenVideo: (String) -> Unit,
    onOpenFeed: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Plex Player Prep", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Media3 streaming, diagnostics, cache, and feed playback", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onOpenFeed) {
                    Icon(Icons.Default.ViewCarousel, contentDescription = null)
                    Text("Feed")
                }
            }
        }
        items(state.videos, key = { it.id }) { video ->
            VideoRow(video = video, onClick = { onOpenVideo(video.id) })
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun VideoRow(video: SampleVideo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 128.dp, height = 72.dp)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFE5A00D), Color(0xFF2A9D8F), Color(0xFF264653))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(video.posterHint, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(video.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(video.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Icons.Default.PlayArrow, contentDescription = "Play ${video.title}")
    }
}

@Preview
@Composable
private fun HomePreview() {
    PlexPrepTheme {
        HomeScreen(HomeState(InMemoryVideoCatalog().videos()), onOpenVideo = {}, onOpenFeed = {})
    }
}
