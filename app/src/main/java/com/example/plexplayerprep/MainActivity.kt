package com.example.plexplayerprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.plexplayerprep.ui.PlexPrepApp
import com.example.plexplayerprep.ui.theme.PlexPrepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlexPrepTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlexPrepApp()
                }
            }
        }
    }
}
