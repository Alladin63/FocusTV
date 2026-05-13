package com.alladin63.maxtv.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.alladin63.maxtv.presentation.navigation.MaxTvNavHost
import com.alladin63.maxtv.presentation.theme.MaxBackground
import com.alladin63.maxtv.presentation.theme.MaxTvTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MaxTvTheme {
                val navController = rememberNavController()
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaxBackground)
                ) {
                    MaxTvNavHost(navController = navController)
                }
            }
        }
    }
}
