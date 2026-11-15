package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import com.example.data.ciqual.CiqualFood
import com.example.data.ciqual.CiqualGenericFoods
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FoodEntity
import com.example.data.model.MealType
import com.example.data.remote.OffProductDto
import com.example.ui.DiaryViewModel
import com.example.ui.components.PortionQuantityDialog
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CalorieRed
import com.example.ui.theme.FatSecretGreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    viewModel: DiaryViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearchingRemote.collectAsState()
    val searchResults by viewModel.remoteSearchResults.collectAsState()
    val searchError by viewModel.remoteSearchError.collectAsState()
    val recentFoods by viewModel.recentFoods.collectAsState()
    val savedCodes by viewModel.savedToHistoryCodes.collectAsState()
    val offCountry by viewModel.offCountry.collectAsState()
    val offStore by viewModel.offStore.collectAsState()
    val activePortionDialog by viewModel.activePortionDialog.collectAsState()

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var productForMealSelect by remember { mutableStateOf<OffProductDto?>(null) }
    var localFoodForMealSelect by remember { mutableStateOf<FoodEntity?>(null) }
    var ciqualFoodForMealSelect by remember { mutableStateOf<CiqualFood?>(null) }

    var idleTab by remember { mutableStateOf(0) } // 0: CIQUAL, 1: Historique
    var selectedCiqualCat by remember { mutableStateOf("Tous") }

    val quickSuggestions = listOf("Pomme", "Banane", "Oeuf", "Poulet", "Riz", "Pâtes", "Avoine", "Yaourt", "Chocolat")

    val matchingCiqual = remember(searchQuery) {
        if (searchQuery.isNotBlank()) {
            CiqualGenericFoods.search(searchQuery)
        } else {
            emptyList()
        }
    }

    val idleCiqualFoods = remember(selectedCiqualCat) {
        CiqualGenericFoods.filter("", selectedCiqualCat)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Recherche d'aliments",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        val countryLabel = when (offCountry.lowercase()) {
                            "fr", "france" -> "🇫🇷 France"
                            "de", "allemagne", "germany" -> "🇩🇪 Allemagne"
                            "be", "belgique" -> "🇧🇪 Belgique"
                            "ch", "suisse" -> "🇨🇭 Suisse"
                            "es", "espagne" -> "🇪🇸 Espagne"
                            "it", "italie" -> "🇮🇹 Italie"
                            "uk" -> "🇬🇧 Royaume-Uni"
                            else -> "🌍 International"
                        }
                        val storeInfo = if (offStore.isNotBlank()) " • $offStore" else ""
                        Text(
                            text = "CIQUAL (Base officielle) & OFF ($countryLabel$storeInfo)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input Row with fixed size and search icon button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Pomme, oeuf, banane, marque...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Effacer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.searchOpenFoodFactsNow()
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("explore_search_input")
                )

                FilledIconButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.searchOpenFoodFactsNow()
                    },
                    enabled = searchQuery.isNotBlank() && !isSearching,
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = FatSecretGreen,
                        contentColor = Color.White,
                        disabledContainerColor = FatSecretGreen.copy(alpha = 0.35f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("explore_search_button")
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Rechercher",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Quick suggestion chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickSuggestions.forEach { suggestion ->
                    Surface(
                        modifier = Modifier.clickable {
                            focusManager.clearFocus()
                            viewModel.searchQuery.value = suggestion
                            viewModel.searchOpenFoodFactsNow()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Content Area
            if (searchQuery.isNotBlank()) {
                // Search Mode: Display CIQUAL Matches first, then Open Food Facts Results
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Section 1: CIQUAL Matches (if any)
                    if (matchingCiqual.isNotEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FatSecretGreen.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ALIMENTS DE BASE CIQUAL (${matchingCiqual.size})",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = FatSecretGreen
                                        )
                                        Text(
                                            text = "Produits bruts officiels ANSES (sans additifs industriels)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        items(matchingCiqual, key = { "ciqual_match_${it.id}" }) { ciqual ->
                            val isSaved = savedCodes.contains(ciqual.id) || recentFoods.any { it.name.equals(ciqual.name, ignoreCase = true) }
                            CiqualProductCard(
                                food = ciqual,
                                isSaved = isSaved,
                                onSaveToHistory = {
                                    viewModel.saveFoodToHistory(ciqual.toFoodEntity())
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Aliment de base \"${ciqual.name}\" mémorisé !")
                                    }
                                },
                                onAddToMeal = {
                                    ciqualFoodForMealSelect = ciqual
                                }
                            )
                        }
                    }

                    // Section 2: Open Food Facts Results
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PRODUITS DU COMMERCE (OPEN FOOD FACTS)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (searchResults.isNotEmpty()) {
                                Text(
                                    text = "${searchResults.size} trouvés",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isSearching) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = FatSecretGreen, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Recherche en ligne Open Food Facts...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else if (searchResults.isNotEmpty()) {
                        itemsIndexed(
                            items = searchResults,
                            key = { index, product -> "search_off_${product.code ?: "item"}_$index" }
                        ) { _, product ->
                            val isSaved = product.code != null && (savedCodes.contains(product.code) || recentFoods.any { it.barcode == product.code })
                            ProductDetailCard(
                                product = product,
                                isSavedToHistory = isSaved,
                                onSaveToHistory = {
                                    viewModel.saveProductToHistory(product)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Aliment \"${product.getDisplayName()}\" ajouté à l'historique !")
                                    }
                                },
                                onAddToMeal = {
                                    productForMealSelect = product
                                }
                            )
                        }
                    } else if (searchError != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = searchError ?: "Aucun résultat trouvé sur Open Food Facts",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // When idle: Tabs between "Aliments de base (CIQUAL)" and "Historique & Mémoire"
                TabRow(
                    selectedTabIndex = idleTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = FatSecretGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[idleTab]),
                            color = FatSecretGreen
                        )
                    }
                ) {
                    Tab(
                        selected = idleTab == 0,
                        onClick = { idleTab = 0 },
                        text = {
                            Text(
                                text = "Aliments de base (${CiqualGenericFoods.ALL_FOODS.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (idleTab == 0) FontWeight.Bold else FontWeight.Normal
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                    Tab(
                        selected = idleTab == 1,
                        onClick = { idleTab = 1 },
                        text = {
                            Text(
                                text = "Historique (${recentFoods.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (idleTab == 1) FontWeight.Bold else FontWeight.Normal
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (idleTab == 0) {
                    // CIQUAL Category selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CiqualGenericFoods.CATEGORIES.forEach { category ->
                            val isSelected = selectedCiqualCat == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCiqualCat = category },
                                label = {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(idleCiqualFoods, key = { "ciqual_idle_${it.id}" }) { ciqual ->
                            val isSaved = savedCodes.contains(ciqual.id) || recentFoods.any { it.name.equals(ciqual.name, ignoreCase = true) }
                            CiqualProductCard(
                                food = ciqual,
                                isSaved = isSaved,
                                onSaveToHistory = {
                                    viewModel.saveFoodToHistory(ciqual.toFoodEntity())
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Aliment \"${ciqual.name}\" mémorisé !")
                                    }
                                },
                                onAddToMeal = {
                                    ciqualFoodForMealSelect = ciqual
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                } else {
                    // Recent & Saved Foods
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (recentFoods.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = FatSecretGreen.copy(alpha = 0.6f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Aucun aliment en mémoire",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Recherchez un aliment ou parcourez les aliments de base pour les ajouter à votre historique.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(recentFoods, key = { it.id }) { food ->
                                SavedFoodItemCard(
                                    food = food,
                                    onAddToMeal = { localFoodForMealSelect = food },
                                    onDelete = { viewModel.deleteRecentFood(food) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialog: Select Meal for CIQUAL food
    ciqualFoodForMealSelect?.let { ciqual ->
        MealSelectDialog(
            foodTitle = ciqual.name,
            onDismiss = { ciqualFoodForMealSelect = null },
            onSelectMeal = { mealType ->
                ciqualFoodForMealSelect = null
                viewModel.openPortionDialogForCiqual(ciqual, mealType)
            }
        )
    }

    // Dialog: Select Meal for Open Food Facts product
    productForMealSelect?.let { product ->
        MealSelectDialog(
            foodTitle = product.getDisplayName(),
            onDismiss = { productForMealSelect = null },
            onSelectMeal = { mealType ->
                productForMealSelect = null
                viewModel.openPortionDialogForRemote(product, mealType)
            }
        )
    }

    // Dialog: Select Meal for Saved / Local food
    localFoodForMealSelect?.let { food ->
        MealSelectDialog(
            foodTitle = food.name,
            onDismiss = { localFoodForMealSelect = null },
            onSelectMeal = { mealType ->
                localFoodForMealSelect = null
                viewModel.openPortionDialogForLocal(food, mealType)
            }
        )
    }

    // Portion Quantity Dialog if triggered
    activePortionDialog?.let { portionState ->
        PortionQuantityDialog(
            foodName = portionState.foodName,
            foodBrand = portionState.foodBrand,
            caloriesPer100g = portionState.caloriesPer100g,
            proteinsPer100g = portionState.proteinsPer100g,
            carbsPer100g = portionState.carbsPer100g,
            fatPer100g = portionState.fatPer100g,
            fiberPer100g = portionState.fiberPer100g,
            saltPer100g = portionState.saltPer100g,
            initialQuantityGrams = portionState.initialQuantityGrams,
            initialUnit = portionState.initialUnit,
            initialUnitQuantity = portionState.initialUnitQuantity,
            defaultServingGrams = portionState.defaultServingGrams,
            onDismiss = { viewModel.dismissPortionDialog() },
            onConfirm = { grams, unit, unitQuantity ->
                viewModel.confirmPortionDialog(grams, unit, unitQuantity)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Ajouté à ${portionState.mealType.titleFr} !")
                }
            }
        )
    }
}

@Composable
fun CiqualProductCard(
    food: CiqualFood,
    isSaved: Boolean,
    onSaveToHistory: () -> Unit,
    onAddToMeal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FatSecretGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "CIQUAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = FatSecretGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = food.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Portion usuelle : ${food.defaultServingDescription}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FatSecretGreen.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${food.caloriesPer100g.roundToInt()} kcal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = FatSecretGreen
                        )
                        Text(
                            text = "pour 100g",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = FatSecretGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nutrients row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MacroPill("P", "${"%.1f".format(food.proteinsPer100g)}g", AccentBlue, Modifier.weight(1f))
                MacroPill("G", "${"%.1f".format(food.carbsPer100g)}g", CalorieOrange, Modifier.weight(1f))
                MacroPill("L", "${"%.1f".format(food.fatPer100g)}g", CalorieRed, Modifier.weight(1f))
                if (food.fiberPer100g > 0) {
                    MacroPill("Fib", "${"%.1f".format(food.fiberPer100g)}g", MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
                }
                if (food.saltPer100g > 0) {
                    MacroPill("Sel", "${"%.2f".format(food.saltPer100g)}g", MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSaveToHistory,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSaved) FatSecretGreen else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSaved) "En mémoire" else "Mémoriser",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isSaved) FatSecretGreen else MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = onAddToMeal,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FatSecretGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Au menu",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailCard(
    product: OffProductDto,
    isSavedToHistory: Boolean,
    onSaveToHistory: () -> Unit,
    onAddToMeal: () -> Unit
) {
    val cleanTitle = product.getDisplayName()
    val cleanSubtitle = product.getSubtitle()
    val kcal = product.getCaloriesPer100g() ?: 0.0
    val proteins = product.getProteinsPer100g()
    val carbs = product.getCarbsPer100g()
    val fat = product.getFatPer100g()
    val fiber = product.getFiberPer100g()
    val salt = product.getSaltPer100g()
    val cleanServing = product.getCleanServingDisplay()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cleanTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (cleanSubtitle.isNotBlank()) {
                        Text(
                            text = cleanSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FatSecretGreen.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${kcal.roundToInt()} kcal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = FatSecretGreen
                        )
                        Text(
                            text = "pour 100g",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = FatSecretGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nutrients row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MacroPill("P", "${"%.1f".format(proteins)}g", AccentBlue, Modifier.weight(1f))
                MacroPill("G", "${"%.1f".format(carbs)}g", CalorieOrange, Modifier.weight(1f))
                MacroPill("L", "${"%.1f".format(fat)}g", CalorieRed, Modifier.weight(1f))
                if (fiber > 0) {
                    MacroPill("Fib", "${"%.1f".format(fiber)}g", MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
                }
                if (salt > 0) {
                    MacroPill("Sel", "${"%.2f".format(salt)}g", MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
                }
            }

            if (cleanServing != null) {
                Text(
                    text = cleanServing,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Save to history
                OutlinedButton(
                    onClick = onSaveToHistory,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isSavedToHistory) Icons.Default.Check else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSavedToHistory) FatSecretGreen else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSavedToHistory) "En mémoire" else "Mémoriser",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isSavedToHistory) FatSecretGreen else MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Button 2: Add directly to a meal
                Button(
                    onClick = onAddToMeal,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FatSecretGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Au menu",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun SavedFoodItemCard(
    food: FoodEntity,
    onAddToMeal: () -> Unit,
    onDelete: () -> Unit
) {
    val cleanName = food.name.replace(Regex("""[\r\n\t]+"""), " ").trim().ifBlank { "Aliment" }
    val cleanBrand = food.brand.replace(Regex("""[\r\n\t]+"""), " ").trim()
    val portionInfo = if (food.defaultServingGrams > 0) "${food.defaultServingGrams.roundToInt()}g" else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Calories (2 lines for title, NO truncated text!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cleanName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (cleanBrand.isNotBlank()) {
                        Text(
                            text = cleanBrand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FatSecretGreen.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${food.caloriesPer100g.roundToInt()} kcal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = FatSecretGreen
                        )
                        Text(
                            text = "pour 100g",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = FatSecretGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nutrients row with MacroPills (Identical to search result card!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MacroPill("P", "${"%.1f".format(food.proteinsPer100g)}g", AccentBlue, Modifier.weight(1f))
                MacroPill("G", "${"%.1f".format(food.carbsPer100g)}g", CalorieOrange, Modifier.weight(1f))
                MacroPill("L", "${"%.1f".format(food.fatPer100g)}g", CalorieRed, Modifier.weight(1f))
                if (food.fiberPer100g > 0) {
                    MacroPill("Fib", "${"%.1f".format(food.fiberPer100g)}g", MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
                }
                if (food.saltPer100g > 0) {
                    MacroPill("Sel", "${"%.2f".format(food.saltPer100g)}g", MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
                }
            }

            if (portionInfo != null) {
                Text(
                    text = "Dernière portion mémorisée : $portionInfo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Supprimer & Au menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Supprimer de l'historique
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Supprimer de l'historique",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Supprimer",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Button 2: Add directly to a meal
                Button(
                    onClick = onAddToMeal,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FatSecretGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Au menu",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun MacroPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = color,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = color,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun MealSelectDialog(
    foodTitle: String,
    onDismiss: () -> Unit,
    onSelectMeal: (MealType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ajouter à quel repas ?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = foodTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                listOf(
                    Pair(MealType.BREAKFAST, Icons.Default.WbSunny),
                    Pair(MealType.LUNCH, Icons.Default.Restaurant),
                    Pair(MealType.DINNER, Icons.Default.NightsStay),
                    Pair(MealType.SNACK, Icons.Default.Fastfood)
                ).forEach { (meal, icon) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMeal(meal) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = FatSecretGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = meal.titleFr,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
