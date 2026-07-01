package com.example.mediaplayerprep.player

data class PlaybackTuning(
    val minVideoBitrate: Int = 0,
    val maxVideoBitrate: Int = Int.MAX_VALUE,
    val maxVideoWidth: Int = Int.MAX_VALUE,
    val maxVideoHeight: Int = Int.MAX_VALUE,
    val preferredVideoWidth: Int = 1280,
    val preferredVideoHeight: Int = 720,
    val minBufferMs: Int = 15_000,
    val maxBufferMs: Int = 50_000,
    val bufferForPlaybackMs: Int = 1_500,
    val bufferForPlaybackAfterRebufferMs: Int = 3_000,
    val initialBitrateEstimate: Long = 2_000_000L,
    val bandwidthSlidingWindowMaxWeight: Int = 2_000
) {
    val summary: String
        get() = "min=${minVideoBitrate.kbpsLabel()}, max=${maxVideoBitrate.kbpsLabel()}, " +
            "preferred=${preferredVideoWidth}x$preferredVideoHeight, " +
            "buffer=$minBufferMs-$maxBufferMs ms, initialBw=${initialBitrateEstimate.kbpsLabel()}"

    companion object {
        val Balanced = PlaybackTuning()

        val DataSaver = PlaybackTuning(
            maxVideoBitrate = 1_500_000,
            maxVideoWidth = 854,
            maxVideoHeight = 480,
            preferredVideoWidth = 854,
            preferredVideoHeight = 480,
            minBufferMs = 10_000,
            maxBufferMs = 30_000,
            initialBitrateEstimate = 900_000L
        )

        val HighQuality = PlaybackTuning(
            minVideoBitrate = 1_500_000,
            maxVideoBitrate = 8_000_000,
            maxVideoWidth = 1920,
            maxVideoHeight = 1080,
            preferredVideoWidth = 1920,
            preferredVideoHeight = 1080,
            minBufferMs = 25_000,
            maxBufferMs = 70_000,
            bufferForPlaybackMs = 2_500,
            bufferForPlaybackAfterRebufferMs = 5_000,
            initialBitrateEstimate = 5_000_000L
        )
    }
}

data class QualityOption(
    val id: String,
    val label: String,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val isSelected: Boolean
)

private fun Int.kbpsLabel(): String =
    if (this == Int.MAX_VALUE) "none" else "${this / 1_000} kbps"

private fun Long.kbpsLabel(): String = "${this / 1_000} kbps"
