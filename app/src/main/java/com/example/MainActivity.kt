package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.LauncherDatabase
import com.example.data.LauncherRepository
import com.example.ui.LauncherHomeScreen
import com.example.ui.LauncherViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: LauncherDatabase
    private lateinit var repository: LauncherRepository
    private lateinit var viewModel: LauncherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Instantiate the Room Database with destructive migration fallback for version upgrades
        database = Room.databaseBuilder(
            applicationContext,
            LauncherDatabase::class.java,
            "tv_launcher_parental_db"
        ).fallbackToDestructiveMigration().build()

        // 2. Instantiate repository and view model
        repository = LauncherRepository(applicationContext, database.launcherDao())

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return LauncherViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        viewModel = ViewModelProvider(this, factory)[LauncherViewModel::class.java]

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(darkTheme = themeMode != "LIGHT") {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LauncherHomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh installed apps and reset active tracking state if bypassed
        if (::viewModel.isInitialized) {
            viewModel.lockParentSession()
        }
    }
}
