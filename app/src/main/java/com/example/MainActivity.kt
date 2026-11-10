package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.DiaryRepository
import com.example.ui.DiaryViewModel
import com.example.ui.screens.FoodSearchScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.WeightScreen
import com.example.ui.theme.FatSecretGreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenTab {
    JOURNAL,
    FOOD_SEARCH,
    STATS,
    WEIGHT,
    SETTINGS
}

class DiaryViewModelFactory(
    private val repository: DiaryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = DiaryRepository(
            foodDao = database.foodDao(),
            mealEntryDao = database.mealEntryDao(),
            settingsDao = database.settingsDao(),
            weightEntryDao = database.weightEntryDao()
        )
        val viewModelFactory = DiaryViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val diaryViewModel: DiaryViewModel = viewModel(factory = viewModelFactory)
                MainAppContainer(viewModel = diaryViewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: DiaryViewModel) {
    var currentTab by remember { mutableStateOf(ScreenTab.JOURNAL) }

    // Predictive / system back handling
    BackHandler(enabled = currentTab != ScreenTab.JOURNAL) {
        currentTab = ScreenTab.JOURNAL
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.JOURNAL,
                    onClick = { currentTab = ScreenTab.JOURNAL },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.JOURNAL) Icons.Filled.Book else Icons.Outlined.Book,
                            contentDescription = "Journal"
                        )
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FatSecretGreen,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_journal")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.FOOD_SEARCH,
                    onClick = { currentTab = ScreenTab.FOOD_SEARCH },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.FOOD_SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                            contentDescription = "Recherche Open Food Facts"
                        )
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FatSecretGreen,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_food_search")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.STATS,
                    onClick = { currentTab = ScreenTab.STATS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.STATS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Statistiques"
                        )
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FatSecretGreen,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_stats")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.WEIGHT,
                    onClick = { currentTab = ScreenTab.WEIGHT },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.WEIGHT) Icons.Filled.MonitorWeight else Icons.Outlined.MonitorWeight,
                            contentDescription = "Suivi du poids"
                        )
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FatSecretGreen,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_weight")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.SETTINGS,
                    onClick = { currentTab = ScreenTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Paramètres"
                        )
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FatSecretGreen,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when (currentTab) {
            ScreenTab.JOURNAL -> {
                JournalScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ScreenTab.FOOD_SEARCH -> {
                FoodSearchScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ScreenTab.STATS -> {
                StatsScreen(
                    viewModel = viewModel,
                    onNavigateToDate = { targetDate ->
                        viewModel.setDate(targetDate)
                        currentTab = ScreenTab.JOURNAL
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ScreenTab.WEIGHT -> {
                WeightScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ScreenTab.SETTINGS -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentTab = ScreenTab.JOURNAL },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
