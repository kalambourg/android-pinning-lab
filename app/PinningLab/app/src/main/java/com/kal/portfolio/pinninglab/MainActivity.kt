package com.kal.portfolio.pinninglab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kal.portfolio.pinninglab.presentation.NetworkScreen
import com.kal.portfolio.pinninglab.ui.theme.PinningLabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PinningLabTheme {
                NetworkScreen()
            }
        }
    }
}