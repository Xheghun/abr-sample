package com.example.plexplayerprep.domain

data class SampleVideo(
    val id: String,
    val title: String,
    val description: String,
    val streamType: StreamType,
    val url: String,
    val subtitleUrl: String? = null,
    val posterHint: String
)
