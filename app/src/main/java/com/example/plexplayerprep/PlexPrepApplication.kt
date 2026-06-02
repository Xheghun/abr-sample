package com.example.plexplayerprep

import android.app.Application
import com.example.plexplayerprep.player.MediaCache

class PlexPrepApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MediaCache.initialize(this)
    }
}
