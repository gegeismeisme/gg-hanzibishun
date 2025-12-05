package com.example.bishun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.bishun.ui.navigation.BishunApp
import com.example.bishun.ui.theme.BishunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BishunTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BishunApp()
                }
            }
        }
    }
}
