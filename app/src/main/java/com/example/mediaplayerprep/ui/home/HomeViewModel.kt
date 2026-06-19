package com.example.mediaplayerprep.ui.home

import androidx.lifecycle.ViewModel
import com.example.mediaplayerprep.domain.SampleVideo
import com.example.mediaplayerprep.domain.VideoCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeState(val videos: List<SampleVideo> = emptyList())

class HomeViewModel(catalog: VideoCatalog) : ViewModel() {
    private val _state = MutableStateFlow(HomeState(videos = catalog.videos()))
    val state = _state.asStateFlow()
}
