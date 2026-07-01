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
- ABR tuning: explicit `DefaultTrackSelector`, max/min bitrate constraints, preferred resolution/viewport, custom `LoadControl`, custom `DefaultBandwidthMeter`, and manual quality overrides.
- Custom codec extension point: a small NDK/C++ demo decoder is built with CMake, loaded through JNI, and surfaced in the debug panel as a native codec probe.
- Media3 `SimpleCache` through `CacheDataSource.Factory`, capped with LRU eviction.
- Preloading demo: the next item is prepared with a secondary player to warm manifests, track metadata, and cache.
- Vertical feed mode using a single active player; inactive pages release player resources. Mute state is preserved by the shared controller factory inside the feed.
- Captions: one HLS sample attaches a sidecar WebVTT subtitle. Production apps should expose text track selection from Media3 `Tracks`.
- tests for playback state mapping, reducer behavior, and retry/play-pause ViewModel behavior.

## HLS And DASH

HLS uses an `.m3u8` manifest and segments that can be selected adaptively based on bandwidth and buffer health. DASH uses an `.mpd` manifest with similar adaptive representations. In both cases the player loads a manifest, selects tracks/variants, fetches segments, decodes audio/video, and adapts as network and decoder conditions change.

The important client responsibilities are startup behavior, seek behavior, error handling, buffering policy, track selection, cache policy, lifecycle cleanup, and surfacing enough diagnostics to debug real playback failures.