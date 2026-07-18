package com.jaylizapp.dimetu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jaylizapp.dimetu.ui.TrackerControl
import com.jaylizapp.dimetu.ui.theme.DemoniacTheme

class MainActivity : ComponentActivity() {
    @androidx.compose.material3.ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by TrackerRepository.isDarkMode.collectAsState()
            
            DemoniacTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDarkMode) Color.Black else Color(0xFFD1D5D8)
                ) {
                    TrackerControl()
                }
            }
        }
    }
}
