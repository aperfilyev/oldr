package com.aperfilyev.oldr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aperfilyev.oldr.ui.theme.OldrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OldrTheme {
                MainScreen()
            }
        }
    }
}
