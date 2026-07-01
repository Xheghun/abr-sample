package com.example.mediaplayerprep.player

import android.content.Context
import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import com.example.mediaplayerprep.domain.SampleVideo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class ExoPlayerController(
    context: Context,
    private val mutedState: SharedMutedState,
    defaultTuning: PlaybackTuning = PlaybackTuning.Balanced
) : PlayerController {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var bandwidthMeter = buildBandwidthMeter(defaultTuning)
    private var trackSelector = buildTrackSelector(defaultTuning)
    private var loadControl = buildLoadControl(defaultTuning)
    private var progressJob: Job? = null
    private var currentVideo: SampleVideo? = null
    private var loadStartedAtMs: Long? = null
    private var firstFrameRendered = false
    private var droppedFrames = 0
    private var lastBitrate: Int? = null
    private var tuning = defaultTuning
    private var manualQualityLabel: String? = null
    private var preloader: ExoPlayer? = null
    private var activePlayer: ExoPlayer = buildPlayer()
    private val customCodecInfo = CustomCodecRegistry.probe()

    override val player: ExoPlayer
        get() = activePlayer

    private val _snapshot = MutableStateFlow(PlayerSnapshot(isMuted = mutedState.isMuted, tuning = tuning))
    override val snapshot: StateFlow<PlayerSnapshot> = _snapshot

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) = publishSnapshot()
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) = publishSnapshot()
        override fun onIsPlayingChanged(isPlaying: Boolean) = publishSnapshot()
        override fun onPlayerError(error: PlaybackException) {
            _snapshot.update {
                it.copy(
                    status = PlaybackStatus.Error,
                    userMessage = "Playback failed. Check your connection or retry.",
                    technicalError = "${error.errorCodeName}: ${error.message}"
                )
            }
        }

        override fun onTracksChanged(tracks: Tracks) = publishSnapshot()
    }

    private val analyticsListener = object : AnalyticsListener {
        override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, output: Any, renderTimeMs: Long) {
            if (!firstFrameRendered) {
                firstFrameRendered = true
                val timeToFirstFrameMs = loadStartedAtMs?.let { SystemClock.elapsedRealtime() - it }
                _snapshot.update { it.copy(diagnostics = it.diagnostics.copy(timeToFirstFrameMs = timeToFirstFrameMs)) }
            }
        }

        override fun onDroppedVideoFrames(
            eventTime: AnalyticsListener.EventTime,
            droppedFrames: Int,
            elapsedMs: Long
        ) {
            this@ExoPlayerController.droppedFrames += droppedFrames
            publishSnapshot()
        }

        override fun onVideoInputFormatChanged(
            eventTime: AnalyticsListener.EventTime,
            format: Format,
            decoderReuseEvaluation: DecoderReuseEvaluation?
        ) {
            lastBitrate = format.bitrate.takeIf { it != Format.NO_VALUE }
            publishSnapshot()
        }
    }

    init {
        player.addListener(listener)
        player.addAnalyticsListener(analyticsListener)
        setMuted(mutedState.isMuted)
        startProgressUpdates()
    }

    override fun load(video: SampleVideo, playWhenReady: Boolean) {
        currentVideo = video
        loadStartedAtMs = SystemClock.elapsedRealtime()
        firstFrameRendered = false
        droppedFrames = 0
        _snapshot.update {
            PlayerSnapshot(
                status = PlaybackStatus.Loading,
                isMuted = mutedState.isMuted,
                playbackSpeed = it.playbackSpeed,
                tuning = tuning,
                manualQualityLabel = manualQualityLabel
            )
        }
        player.setMediaItem(video.toMediaItem())
        player.prepare()
        player.playWhenReady = playWhenReady
    }

    override fun play() {
        player.play()
        publishSnapshot()
    }

    override fun pause() {
        player.pause()
        publishSnapshot()
    }

    override fun retry() {
        currentVideo?.let { load(it, playWhenReady = true) }
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0L))
        publishSnapshot()
    }

    override fun setMuted(muted: Boolean) {
        mutedState.isMuted = muted
        player.volume = if (muted) 0f else 1f
        _snapshot.update { it.copy(isMuted = muted) }
    }

    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        _snapshot.update { it.copy(playbackSpeed = speed) }
    }

    override fun setPlaybackTuning(tuning: PlaybackTuning) {
        this.tuning = tuning
        manualQualityLabel = null
        rebuildPlayer(tuning)
        publishSnapshot()
    }

    override fun selectAutoQuality() {
        manualQualityLabel = null
        trackSelector.setParameters(
            trackSelector.buildUponParameters()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
        )
        publishSnapshot()
    }

    override fun selectQuality(optionId: String) {
        val (groupIndex, trackIndex) = optionId.parseQualityId() ?: return
        val group = player.currentTracks.groups.getOrNull(groupIndex) ?: return
        if (group.type != C.TRACK_TYPE_VIDEO || trackIndex !in 0 until group.length) return
        val override = TrackSelectionOverride(group.mediaTrackGroup, trackIndex)
        manualQualityLabel = group.qualityLabel(trackIndex)
        trackSelector.setParameters(
            trackSelector.buildUponParameters()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .addOverride(override)
        )
        publishSnapshot()
    }

    override fun preload(video: SampleVideo) {
        // A lightweight next-item prepare warms manifests, track metadata, and the shared cache.
        preloader?.release()
        preloader = ExoPlayer.Builder(appContext)
            .setBandwidthMeter(bandwidthMeter)
            .setMediaSourceFactory(DefaultMediaSourceFactory(MediaCache.dataSourceFactory(appContext)))
            .build()
            .also {
                it.setMediaItem(video.toMediaItem())
                it.prepare()
            }
    }

    override fun release() {
        progressJob?.cancel()
        player.removeListener(listener)
        player.removeAnalyticsListener(analyticsListener)
        player.release()
        preloader?.release()
    }

    private fun rebuildPlayer(tuning: PlaybackTuning) {
        val oldPlayer = activePlayer
        val restoreVideo = currentVideo
        val restorePositionMs = oldPlayer.currentPosition.coerceAtLeast(0L)
        val restorePlayWhenReady = oldPlayer.playWhenReady
        val restoreSpeed = _snapshot.value.playbackSpeed

        oldPlayer.removeListener(listener)
        oldPlayer.removeAnalyticsListener(analyticsListener)

        bandwidthMeter = buildBandwidthMeter(tuning)
        trackSelector = buildTrackSelector(tuning)
        loadControl = buildLoadControl(tuning)
        activePlayer = buildPlayer()
        activePlayer.addListener(listener)
        activePlayer.addAnalyticsListener(analyticsListener)
        activePlayer.volume = if (mutedState.isMuted) 0f else 1f
        activePlayer.setPlaybackSpeed(restoreSpeed)

        restoreVideo?.let {
            loadStartedAtMs = SystemClock.elapsedRealtime()
            firstFrameRendered = false
            activePlayer.setMediaItem(it.toMediaItem())
            activePlayer.prepare()
            activePlayer.seekTo(restorePositionMs)
            activePlayer.playWhenReady = restorePlayWhenReady
        }

        oldPlayer.release()
    }

    private fun buildPlayer(): ExoPlayer =
        ExoPlayer.Builder(appContext)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .setMediaSourceFactory(DefaultMediaSourceFactory(MediaCache.dataSourceFactory(appContext)))
            .build()

    private fun buildBandwidthMeter(tuning: PlaybackTuning): DefaultBandwidthMeter =
        DefaultBandwidthMeter.Builder(appContext)
            .setInitialBitrateEstimate(tuning.initialBitrateEstimate)
            .setSlidingWindowMaxWeight(tuning.bandwidthSlidingWindowMaxWeight)
            .build()

    private fun buildTrackSelector(tuning: PlaybackTuning): DefaultTrackSelector =
        DefaultTrackSelector(appContext).apply {
            setParameters(buildParameters(tuning))
        }

    private fun buildLoadControl(tuning: PlaybackTuning): DefaultLoadControl =
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                tuning.minBufferMs,
                tuning.maxBufferMs,
                tuning.bufferForPlaybackMs,
                tuning.bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

    private fun startProgressUpdates() {
        progressJob = scope.launch {
            while (isActive) {
                publishSnapshot()
                delay(500)
            }
        }
    }

    private fun publishSnapshot() {
        val duration = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
        val status = PlaybackStateMapper.map(
            playbackState = player.playbackState,
            playWhenReady = player.playWhenReady,
            hasError = player.playerError != null
        )
        _snapshot.update {
            it.copy(
                status = status,
                isPlaying = player.isPlaying,
                durationMs = duration,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
                userMessage = if (status == PlaybackStatus.Error) it.userMessage else null,
                technicalError = if (status == PlaybackStatus.Error) it.technicalError else null,
                diagnostics = PlayerDiagnostics(
                    bitrate = lastBitrate,
                    bandwidthEstimate = bandwidthMeter.bitrateEstimate.takeIf { estimate -> estimate > 0 },
                    droppedFrames = droppedFrames,
                    selectedVideoTrack = player.currentTracks.describe(C.TRACK_TYPE_VIDEO),
                    selectedAudioTrack = player.currentTracks.describe(C.TRACK_TYPE_AUDIO),
                    selectedTextTrack = player.currentTracks.describe(C.TRACK_TYPE_TEXT),
                    customCodec = customCodecInfo,
                    playbackPositionMs = player.currentPosition.coerceAtLeast(0L),
                    bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
                    playerState = status.name,
                    timeToFirstFrameMs = it.diagnostics.timeToFirstFrameMs
                ),
                tuning = tuning,
                qualityOptions = player.currentTracks.videoQualityOptions(),
                manualQualityLabel = manualQualityLabel
            )
        }
    }

    private fun DefaultTrackSelector.buildParameters(tuning: PlaybackTuning): DefaultTrackSelector.Parameters.Builder =
        buildUponParameters()
            .setMinVideoBitrate(tuning.minVideoBitrate)
            .setMaxVideoBitrate(tuning.maxVideoBitrate)
            .setMaxVideoSize(tuning.maxVideoWidth, tuning.maxVideoHeight)
            .setViewportSize(tuning.preferredVideoWidth, tuning.preferredVideoHeight, true)
            .setAllowVideoNonSeamlessAdaptiveness(true)
            .setAllowVideoMixedMimeTypeAdaptiveness(true)
            .setAllowVideoMixedDecoderSupportAdaptiveness(false)

    private fun SampleVideo.toMediaItem(): MediaItem {
        val builder = MediaItem.Builder()
            .setUri(url)
            .setMediaId(id)
        subtitleUrl?.let {
            builder.setSubtitleConfigurations(
                listOf(
                    MediaItem.SubtitleConfiguration.Builder(android.net.Uri.parse(it))
                        .setMimeType(MimeTypes.TEXT_VTT)
                        .setLanguage("en")
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                )
            )
        }
        return builder.build()
    }

    private fun Tracks.describe(type: Int): String {
        val selected = groups
            .filter { it.type == type && it.isSelected }
            .flatMap { group ->
                (0 until group.length)
                    .filter { group.isTrackSelected(it) }
                    .map { group.getTrackFormat(it) }
            }
            .firstOrNull()
        return selected?.let { format ->
            listOfNotNull(
                format.sampleMimeType,
                format.width.takeIf { it > 0 }?.let { "${format.width}x${format.height}" },
                format.bitrate.takeIf { it > 0 }?.let { "${it / 1000} kbps" },
                format.language
            ).joinToString(" | ")
        } ?: if (type == C.TRACK_TYPE_TEXT) "none" else "unknown"
    }

    private fun Tracks.videoQualityOptions(): List<QualityOption> =
        groups
            .mapIndexed { groupIndex, group -> groupIndex to group }
            .filter { (_, group) -> group.type == C.TRACK_TYPE_VIDEO && group.isSupported }
            .flatMap { (groupIndex, group) ->
                (0 until group.length)
                    .filter { group.isTrackSupported(it) }
                    .map { trackIndex ->
                        val format = group.getTrackFormat(trackIndex)
                        QualityOption(
                            id = "$groupIndex:$trackIndex",
                            label = group.qualityLabel(trackIndex),
                            width = format.width.coerceAtLeast(0),
                            height = format.height.coerceAtLeast(0),
                            bitrate = format.bitrate.takeIf { it != Format.NO_VALUE } ?: 0,
                            isSelected = group.isTrackSelected(trackIndex)
                        )
                    }
            }
            .distinctBy { "${it.width}x${it.height}:${it.bitrate}" }
            .sortedWith(compareBy<QualityOption> { it.height }.thenBy { it.bitrate })

    private fun Tracks.Group.qualityLabel(trackIndex: Int): String {
        val format = getTrackFormat(trackIndex)
        val resolution = if (format.width > 0 && format.height > 0) "${format.height}p" else "Video"
        val bitrate = format.bitrate.takeIf { it != Format.NO_VALUE && it > 0 }?.let { " ${it / 1_000} kbps" }.orEmpty()
        return resolution + bitrate
    }

    private fun String.parseQualityId(): Pair<Int, Int>? {
        val parts = split(":")
        if (parts.size != 2) return null
        return parts[0].toIntOrNull()?.let { group -> parts[1].toIntOrNull()?.let { track -> group to track } }
    }
}

class SharedMutedState(var isMuted: Boolean = false)
