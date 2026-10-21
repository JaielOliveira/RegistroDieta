package com.dieta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.dieta.app.ui.screens.HomeScreen
import com.dieta.app.ui.theme.DietaTheme
import com.dieta.app.ui.viewmodel.DietViewModel
import com.dieta.app.ui.viewmodel.DietViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: DietViewModel by viewModels {
        val app = application as DietaApp
        DietViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DietaTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
