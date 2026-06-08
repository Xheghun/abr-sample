package com.example.plexplayerprep.ui.home

import androidx.lifecycle.ViewModel
import com.example.plexplayerprep.domain.SampleVideo
import com.example.plexplayerprep.domain.VideoCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeState(val videos: List<SampleVideo> = emptyList())

class HomeViewModel(catalog: VideoCatalog) : ViewModel() {
    private val _state = MutableStateFlow(HomeState(videos = catalog.videos()))
    val state = _state.asStateFlow()
}
