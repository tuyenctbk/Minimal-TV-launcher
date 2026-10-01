package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
    private var packageChangeReceiver: BroadcastReceiver? = null

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

        // 3. Register package receiver to immediately detect newly installed or deleted apps
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (::viewModel.isInitialized) {
                    viewModel.refreshApps()
                }
            }
        }
        packageChangeReceiver = receiver
        registerReceiver(receiver, filter)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(darkTheme = themeMode != "LIGHT") {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        LauncherHomeScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh installed apps and reset active tracking state if bypassed
        if (::viewModel.isInitialized) {
            viewModel.refreshApps()
            viewModel.lockParentSession()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        packageChangeReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {
            }
        }
    }
}
