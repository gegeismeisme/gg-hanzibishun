package com.yourstudio.hskstroke.bishun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yourstudio.hskstroke.bishun.ads.AdsInitializer
import com.yourstudio.hskstroke.bishun.ui.navigation.BishunApp
import com.yourstudio.hskstroke.bishun.ui.theme.BishunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsInitializer.initialize(applicationContext)
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
