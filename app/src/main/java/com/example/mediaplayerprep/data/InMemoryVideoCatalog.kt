package com.example.mediaplayerprep.data

import com.example.mediaplayerprep.domain.SampleVideo
import com.example.mediaplayerprep.domain.StreamType
import com.example.mediaplayerprep.domain.VideoCatalog

class InMemoryVideoCatalog : VideoCatalog {
    private val items = listOf(
        SampleVideo(
            id = "bbb-hls",
            title = "Big Buck Bunny HLS",
            description = "Adaptive HLS VOD stream commonly used for playback validation.",
            streamType = StreamType.Hls,
            url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            subtitleUrl = "https://raw.githubusercontent.com/videojs/video.js/main/docs/examples/shared/example-captions.vtt",
            posterHint = "HLS"
        ),
        SampleVideo(
            id = "sintel-dash",
            title = "Sintel DASH",
            description = "MPEG-DASH adaptive stream with multiple video representations.",
            streamType = StreamType.Dash,
            url = "https://storage.googleapis.com/shaka-demo-assets/sintel/dash.mpd",
            posterHint = "DASH"
        ),
        SampleVideo(
            id = "tears-hls",
            title = "Tears of Steel HLS",
            description = "HLS stream useful for checking startup, buffering, and seeks.",
            streamType = StreamType.Hls,
            url = "https://test-streams.mux.dev/test_001/stream.m3u8",
            posterHint = "HLS"
        ),
        SampleVideo(
            id = "angel-one-dash",
            title = "Angel One DASH",
            description = "Shaka test asset for track selection and adaptive bitrate changes.",
            streamType = StreamType.Dash,
            url = "https://storage.googleapis.com/shaka-demo-assets/angel-one/dash.mpd",
            posterHint = "DASH"
        )
    )

    override fun videos(): List<SampleVideo> = items

    override fun video(id: String): SampleVideo? = items.firstOrNull { it.id == id }
}
