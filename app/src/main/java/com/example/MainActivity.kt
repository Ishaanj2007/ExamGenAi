package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.database.AppDatabase
import com.example.data.repository.QuestionPaperRepository
import com.example.ui.screens.CreatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QuestionPaperViewModel
import com.example.ui.viewmodel.QuestionPaperViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize local Room Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = QuestionPaperRepository(database.questionPaperDao())

        setContent {
            // Fetch/Provide ViewModel first to drive theme dynamically
            val viewModel: QuestionPaperViewModel = viewModel(
                factory = QuestionPaperViewModelFactory(application, repository)
            )

            val darkMode by viewModel.darkModePref.collectAsState()
            val dynamicColorEnabled by viewModel.dynamicColorPref.collectAsState()
            
            val isDark = when (darkMode) {
                "dark" -> true
                "light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(
                darkTheme = isDark,
                dynamicColor = dynamicColorEnabled
            ) {
                // Set up standard Compose Navigation NavHost
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToCreator = { navController.navigate("creator") },
                                onNavigateToPreview = { navController.navigate("preview") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        composable("creator") {
                            CreatorScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onGenerationSuccess = {
                                    // Navigate to preview, but clear the backstack entry of creator
                                    // so back-press from preview goes directly to home screen
                                    navController.navigate("preview") {
                                        popUpTo("home") { saveState = false }
                                    }
                                }
                            )
                        }
                        composable("preview") {
                            PreviewScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }}
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
