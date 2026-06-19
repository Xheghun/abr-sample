package com.example.mediaplayerprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mediaplayerprep.ui.MediaPrepApp
import com.example.mediaplayerprep.ui.theme.MediaPrepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaPrepTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MediaPrepApp()
                }
            }
        }
    }
}
