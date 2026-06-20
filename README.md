# Media Player Prep

An Android media streaming sample built to practice adaptive streaming, Media3/ExoPlayer integration, custom playback state, diagnostics, caching, feed playback, lifecycle cleanup, and testable presentation logic.

## How To Run

Open the project in Android Studio and run the `app` configuration on an emulator or device with internet access.

## Architecture

This is intentionally a single-module sample, with layered packages instead of heavy modularization:

- `domain`: `SampleVideo`, stream type, and catalog interface.
- `data`: in-memory catalog of public HLS and DASH streams.
- `player`: `PlayerController` abstraction, Media3/ExoPlayer implementation, cache setup, diagnostics, and playback state mapping.
- `ui`: Compose screens, type-safe navigation routes, ViewModels, and a small reducer for immutable player screen state.

The UI never owns raw player behavior. It renders immutable state from ViewModels and sends actions back up. `ExoPlayerController` owns the Media3 player instance and releases it from `ViewModel.onCleared()`.

## Features Demonstrated

- Home screen listing public HLS and DASH samples.
- Media3/ExoPlayer playback with HLS and DASH modules.
- Custom controls: play/pause, mute/unmute, seek, speed, fullscreen orientation toggle, retry.
- Playback state model: idle, loading, buffering, ready/paused, playing, ended, error.
- User-friendly error message plus technical details in the debug panel.
- Diagnostics: bitrate when available, dropped frames, selected audio/video/text track, position, buffered position, player state, and time-to-first-frame.
- Media3 `SimpleCache` through `CacheDataSource.Factory`, capped with LRU eviction.
- Preloading demo: the next item is prepared with a secondary player to warm manifests, track metadata, and cache.
- Vertical feed mode using a single active player; inactive pages release player resources. Mute state is preserved by the shared controller factory inside the feed.
- Captions: one HLS sample attaches a sidecar WebVTT subtitle. Production apps should expose text track selection from Media3 `Tracks`.
- JVM tests for playback state mapping, reducer behavior, and retry/play-pause ViewModel behavior.

## HLS And DASH

HLS uses an `.m3u8` manifest and segments that can be selected adaptively based on bandwidth and buffer health. DASH uses an `.mpd` manifest with similar adaptive representations. In both cases the player loads a manifest, selects tracks/variants, fetches segments, decodes audio/video, and adapts as network and decoder conditions change.

The important client responsibilities are startup behavior, seek behavior, error handling, buffering policy, track selection, cache policy, lifecycle cleanup, and surfacing enough diagnostics to debug real playback failures.

## Debugging Latency, Buffering, And Codec Issues

For startup latency, measure time from `load()` to first rendered frame, then split the problem into DNS/TLS, manifest load, playlist updates, segment download, decoder init, and first frame render.

For buffering, inspect selected bitrate, buffered duration, bandwidth estimates, dropped frames, CDN errors, segment size, and whether the ABR algorithm selected too aggressively.

For codec issues, compare the selected track format against device decoder support, DRM requirements, profile/level, resolution, HDR format, and audio channel layout. In production, persist structured playback sessions so failures can be grouped by device, stream, CDN, codec, and app version.

## React Native Integration Sketch

A React Native app would usually expose this native layer as a view manager plus an event bridge:

- Native view wraps `PlayerView` and a `PlayerController`.
- JS props provide media URL, stream type, autoplay, muted, speed, and selected text/audio track.
- JS commands call play, pause, seek, retry, setMuted, and setPlaybackSpeed.
- Native events emit playback state, errors, progress, buffering, diagnostics, and track changes.
- The native layer should keep ExoPlayer ownership and lifecycle rules on Android; JS should not directly manage player instances.

