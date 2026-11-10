package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.FoodEntity
import com.example.data.local.MealEntryEntity
import com.example.data.model.MealType
import com.example.data.remote.OffProductDto
import com.example.ui.DiaryViewModel
import com.example.ui.components.AddFoodDialog
import com.example.ui.components.CalorieMeterCard
import com.example.ui.components.CalorieTargetDialog
import com.example.ui.components.EditQuantityDialog
import com.example.ui.components.MealCard
import com.example.ui.components.PortionQuantityDialog
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: DiaryViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val entries by viewModel.currentEntries.collectAsStateWithLifecycle()
    val calorieTarget by viewModel.calorieTarget.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchingRemote by viewModel.isSearchingRemote.collectAsStateWithLifecycle()
    val remoteResults by viewModel.remoteSearchResults.collectAsStateWithLifecycle()
    val remoteError by viewModel.remoteSearchError.collectAsStateWithLifecycle()
    val localFoods by viewModel.localSearchResults.collectAsStateWithLifecycle()
    val recentFoods by viewModel.recentFoods.collectAsStateWithLifecycle()
    val activePortionDialog by viewModel.activePortionDialog.collectAsStateWithLifecycle()
    val offCountry by viewModel.offCountry.collectAsStateWithLifecycle()
    val offStore by viewModel.offStore.collectAsStateWithLifecycle()
    val collapsedMeals by viewModel.collapsedMeals.collectAsStateWithLifecycle()

    // Dialog states
    var activeAddMealType by remember { mutableStateOf<MealType?>(null) }
    var selectedEntryForEdit by remember { mutableStateOf<MealEntryEntity?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }

    val isToday = selectedDate == DateUtils.getTodayIso()

    Column(modifier = modifier.fillMaxSize()) {
        // Date Navigation Top Bar
        TopAppBar(
            title = {
                Text(
                    text = DateUtils.formatFriendlyDate(selectedDate),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            },
            actions = {
                IconButton(
                    onClick = { viewModel.goToPreviousDay() },
                    modifier = Modifier.testTag("prev_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Jour précédent"
                    )
                }

                if (!isToday) {
                    IconButton(
                        onClick = { viewModel.goToToday() },
                        modifier = Modifier.testTag("today_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Aujourd'hui",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.goToNextDay() },
                    modifier = Modifier.testTag("next_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Jour suivant"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Diary Content: Compact Summary Gauge + 4 Meals in one view
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalorieMeterCard(
                targetCalories = calorieTarget,
                entries = entries,
                onEditTargetClick = { showTargetDialog = true }
            )

            // 4 Meals in standard order
            listOf(
                MealType.BREAKFAST,
                MealType.LUNCH,
                MealType.DINNER,
                MealType.SNACK
            ).forEach { mealType ->
                val mealEntries = entries.filter { it.mealType == mealType }
                val isExpanded = !collapsedMeals.contains(mealType)
                MealCard(
                    mealType = mealType,
                    entries = mealEntries,
                    isExpanded = isExpanded,
                    onToggleExpand = { viewModel.toggleMealCollapsed(mealType) },
                    onAddFoodClick = {
                        viewModel.clearSearch()
                        activeAddMealType = mealType
                    },
                    onEntryClick = { entry ->
                        selectedEntryForEdit = entry
                    },
                    onDeleteEntryClick = { entry ->
                        viewModel.deleteEntry(entry)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Add Food Dialog
    activeAddMealType?.let { mealType ->
        AddFoodDialog(
            mealType = mealType,
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.searchQuery.value = it },
            onSearchOpenFoodFacts = { viewModel.searchOpenFoodFactsNow() },
            isSearchingRemote = isSearchingRemote,
            remoteResults = remoteResults,
            remoteError = remoteError,
            localFoods = localFoods,
            recentFoods = recentFoods,
            onSelectLocalFood = { food ->
                viewModel.openPortionDialogForLocal(food, mealType)
            },
            onSelectRemoteProduct = { product ->
                viewModel.openPortionDialogForRemote(product, mealType)
            },
            onCreateCustomFood = { name, brand, kcal100g, quantityGrams, prot, carbs, fat, fiber, salt ->
                viewModel.addCustomFoodToMeal(
                    mealType = mealType,
                    name = name,
                    brand = brand,
                    caloriesPer100g = kcal100g,
                    quantityGrams = quantityGrams,
                    proteinsPer100g = prot,
                    carbsPer100g = carbs,
                    fatPer100g = fat,
                    fiberPer100g = fiber,
                    saltPer100g = salt
                )
            },
            onDeleteRecentFood = { food ->
                viewModel.deleteRecentFood(food)
            },
            offCountry = offCountry,
            offStore = offStore,
            onDismiss = {
                activeAddMealType = null
            }
        )
    }

    // Portion Dialog with memory of last used quantity and unit
    activePortionDialog?.let { dialogState ->
        PortionQuantityDialog(
            foodName = dialogState.foodName,
            foodBrand = dialogState.foodBrand,
            caloriesPer100g = dialogState.caloriesPer100g,
            proteinsPer100g = dialogState.proteinsPer100g,
            carbsPer100g = dialogState.carbsPer100g,
            fatPer100g = dialogState.fatPer100g,
            fiberPer100g = dialogState.fiberPer100g,
            saltPer100g = dialogState.saltPer100g,
            initialQuantityGrams = dialogState.initialQuantityGrams,
            initialUnit = dialogState.initialUnit,
            initialUnitQuantity = dialogState.initialUnitQuantity,
            defaultServingGrams = dialogState.defaultServingGrams,
            onDismiss = { viewModel.dismissPortionDialog() },
            onConfirm = { quantityGrams, unit, unitQuantity ->
                viewModel.confirmPortionDialog(quantityGrams, unit, unitQuantity)
            }
        )
    }

    // Edit Existing Entry Dialog
    selectedEntryForEdit?.let { entry ->
        EditQuantityDialog(
            entry = entry,
            onDismiss = { selectedEntryForEdit = null },
            onSave = { newQuantityGrams, unit, unitQuantity ->
                viewModel.updateEntryQuantity(entry, newQuantityGrams, unit, unitQuantity)
                selectedEntryForEdit = null
            },
            onDelete = {
                viewModel.deleteEntry(entry)
                selectedEntryForEdit = null
            }
        )
    }

    // Calorie Target Dialog
    if (showTargetDialog) {
        CalorieTargetDialog(
            currentTarget = calorieTarget,
            onDismiss = { showTargetDialog = false },
            onSaveTarget = { newTarget ->
                viewModel.updateCalorieTarget(newTarget)
                showTargetDialog = false
            }
        )
    }
}
