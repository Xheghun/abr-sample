# Interview Notes

## Likely Questions And Strong Answers

### How did you separate player logic from UI?

The UI consumes immutable state and sends actions. `PlayerController` hides ExoPlayer details behind a small contract, while `ExoPlayerController` owns Media3 setup, listeners, diagnostics, cache, preloading, and release. This makes ViewModels testable and prevents Compose from becoming the owner of playback infrastructure.

### How do HLS and DASH work?

Both use manifests that describe available tracks and segment URLs. The player chooses an initial representation, downloads media segments, fills the buffer, and adapts representation based on network and playback conditions. HLS uses `.m3u8`; DASH uses `.mpd`. Most client complexity is around manifests, track selection, buffering, seeking, errors, and codec compatibility.

### What would you log for a playback failure?

Stream URL/media ID, manifest type, player state timeline, error code, HTTP response if available, selected track format, bandwidth estimate, buffered duration, position, dropped frames, decoder name, DRM status, device model, OS version, app version, CDN edge if exposed, and retry outcome.

### How would you debug slow startup?

Start with time-to-first-frame and break it into manifest load, license load if DRM exists, first segment load, decoder init, and render. Then compare by network type, CDN, device, codec, stream type, and cold/warm cache. If the first segment is too large or the initial bitrate is too high, tune encoding ladders or startup track selection.

### How would you avoid leaks in a vertical video feed?

Keep one active player or a very small bounded pool. Release or pause inactive players as pages leave the active window. Preserve user-level state, such as mute, outside individual player instances. Avoid storing ExoPlayer in composables; tie release to ViewModel or lifecycle ownership.

### Why use a controller abstraction?

It creates a stable seam between app state and Media3 APIs. The app can test retry, mute, seek, and state transitions with fakes. It also lets a team evolve caching, analytics, DRM, downloads, or a React Native bridge without changing every screen.

### How would captions be handled in production?

Attach sidecar subtitle configurations when the content service provides external text tracks. For embedded tracks, read Media3 `Tracks`, expose selected text languages/styles to UI, and route selection back through player track parameters. Accessibility settings should influence defaults.

### What are the caching tradeoffs?

Cache improves repeat playback and can reduce startup latency, but it consumes disk and can hide CDN/network issues during testing. Use bounded eviction, cache only eligible content, respect DRM/license constraints, and instrument cache hit rates separately from network fetches.

### How would you expose this to React Native?

Expose a native player view plus commands and events. JS owns intent and layout, while Android owns ExoPlayer instances, lifecycle, and diagnostics. Events should be structured and throttled for progress updates. Commands should be idempotent where possible.

## Concepts Demonstrated

- Media3 ExoPlayer setup for HLS and DASH.
- `CacheDataSource` and `SimpleCache` configuration.
- Custom playback state mapping from Media3 state.
- `AnalyticsListener` diagnostics: first frame and dropped frames.
- Track inspection through Media3 `Tracks`.
- MVI-style actions and immutable state.
- ViewModel lifecycle ownership and player release.
- Feed playback with one active player.
- Retry and technical error surfaces.
- Unit testing with fake player controllers.

## Limitations

- No DRM or license flow.
- No real backend catalog, auth, resume position sync, or watch history.
- No production-grade ABR tuning or custom `LoadControl`.
- Feed mode uses one active player rather than a tuned pre-warmed player pool.
- Subtitle UI is diagnostic only; it does not yet expose user track selection.
- Fullscreen is a simple orientation toggle, not a complete immersive full-screen player shell.
- Diagnostics are displayed locally but not persisted as telemetry.

## Future Improvements

- Add Widevine DRM samples and license diagnostics.
- Add track selection UI for audio, video quality, and text tracks.
- Persist playback sessions and resume positions.
- Add configurable `LoadControl` and startup bitrate strategy.
- Add offline downloads with Media3 download service.
- Add PiP, audio focus, noisy-audio handling, and media session controls.
- Add Compose UI tests for home, player controls, and feed active-item behavior.
- Add a small React Native module sketch with TypeScript API definitions.
