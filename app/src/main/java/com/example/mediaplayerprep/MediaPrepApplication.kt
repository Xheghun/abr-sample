package com.example.mediaplayerprep

import android.app.Application
import com.example.mediaplayerprep.player.MediaCache

class MediaPrepApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MediaCache.initialize(this)
    }
}
