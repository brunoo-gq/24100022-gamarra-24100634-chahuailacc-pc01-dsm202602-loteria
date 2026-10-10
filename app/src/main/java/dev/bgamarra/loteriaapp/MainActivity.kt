package dev.bgamarra.loteriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.bgamarra.loteriaapp.presentation.navigation.AppNavGraph
import dev.bgamarra.loteriaapp.ui.theme.LoteriaAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            LoteriaAppTheme {

                AppNavGraph()

            }
        }
    }
}