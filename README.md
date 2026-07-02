# M Player

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


## Custom Codec Extension

Android apps cannot globally register arbitrary app-local codecs into the platform `MediaCodec` list. For a real custom software codec with Media3, you normally add a custom renderer/decoder extension that recognizes a custom MIME type and feeds decoded frames into the playback pipeline.

This sample adds the lower-level native bridge for that path:

- `app/src/main/cpp/native_codec_bridge.cpp`: toy C++ RLE decoder/probe.
- `CustomCodecRegistry`: declares the sample MIME type `video/x-mediaprep-rle`.
- `NativeCodecBridge`: loads `libmediaprep_custom_codec.so` and calls the native probe.
- Debug panel: shows whether the native decoder bridge loaded and decoded the probe frame.

The included C++ code intentionally decodes only a tiny synthetic probe frame. Turning it into real playback support would require a Media3 custom renderer that consumes samples for the custom MIME type, calls the native decoder per access unit, queues decoded frames, handles timestamps, flush/seek, backpressure, and renders to a surface.

## React Native Integration Sketch

A React Native app would usually expose this native layer as a view manager plus an event bridge:

- Native view wraps `PlayerView` and a `PlayerController`.
- JS props provide media URL, stream type, autoplay, muted, speed, and selected text/audio track.
- JS commands call play, pause, seek, retry, setMuted, and setPlaybackSpeed.
- Native events emit playback state, errors, progress, buffering, diagnostics, and track changes.
- The native layer should keep ExoPlayer ownership and lifecycle rules on Android; JS should not directly manage player instances.

For a larger app, create a player service/factory owned by the native module and use stable player IDs so JS can coordinate full-screen, mini-player, casting, and feed cells without leaking native players.

## Study Next

- Media3 offline downloads and production DRM flows.
- ABR internals: how buffer health, bandwidth, viewport size, and track selector constraints influence variant selection.
- DRM with Widevine, license renewal, offline licenses, and failure modes.
- Subtitle formats: WebVTT, TTML, CEA-608/708, styling, and accessibility.
- Player lifecycle in feeds, background playback, audio focus, PiP, casting, and full-screen handoff.
- Observability: structured playback telemetry, startup funnels, CDN correlation, and device-specific codec analytics.
