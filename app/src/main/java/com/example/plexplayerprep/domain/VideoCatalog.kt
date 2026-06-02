package com.example.plexplayerprep.domain

interface VideoCatalog {
    fun videos(): List<SampleVideo>
    fun video(id: String): SampleVideo?
}
