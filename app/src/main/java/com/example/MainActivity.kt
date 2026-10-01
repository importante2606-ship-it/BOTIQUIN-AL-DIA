package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.BotiquinMainApp
import com.example.ui.BotiquinViewModel
import com.example.ui.theme.BotiquinTheme

/**
 * Actividad Principal de "BOTIQUÍN AL DÍA".
 *
 * Configura Edge-to-Edge obligatorio y conecta el ViewModel con la jerarquía de Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: BotiquinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BotiquinTheme {
                BotiquinMainApp(viewModel = viewModel)
            }
        }
    }
}
