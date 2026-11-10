package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.ui.screens.MacroPill
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.FoodEntity
import com.example.data.model.MealType
import com.example.data.remote.OffProductDto
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CalorieRed
import com.example.ui.theme.FatSecretGreen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodDialog(
    mealType: MealType,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchOpenFoodFacts: () -> Unit,
    isSearchingRemote: Boolean,
    remoteResults: List<OffProductDto>,
    remoteError: String?,
    localFoods: List<FoodEntity>,
    recentFoods: List<FoodEntity>,
    onSelectLocalFood: (FoodEntity) -> Unit,
    onSelectRemoteProduct: (OffProductDto) -> Unit,
    onCreateCustomFood: (
        name: String,
        brand: String,
        kcal100g: Double,
        quantityGrams: Double,
        proteins: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        salt: Double
    ) -> Unit,
    onDeleteRecentFood: (FoodEntity) -> Unit,
    offCountry: String = "fr",
    offStore: String = "",
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Récents", "Open Food Facts", "Création manuelle")
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Ajouter à ${mealType.titleFr}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                        }
                    },
                    actions = {
                        // Prominent "Terminé" button allowing the user to finish adding multiple foods
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("finish_add_food_button")
                        ) {
                            Text(
                                text = "Terminé",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = FatSecretGreen
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab Row
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> {
                        // Récents & Habituels tab
                        RecentsTabContent(
                            recentFoods = recentFoods,
                            onSelect = onSelectLocalFood,
                            onDelete = onDeleteRecentFood
                        )
                    }
                    1 -> {
                        // Open Food Facts tab
                        OffSearchTabContent(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange,
                            onSearch = {
                                focusManager.clearFocus()
                                onSearchOpenFoodFacts()
                            },
                            isLoading = isSearchingRemote,
                            results = remoteResults,
                            errorMessage = remoteError,
                            localMatches = localFoods,
                            offCountry = offCountry,
                            offStore = offStore,
                            onSelectLocal = onSelectLocalFood,
                            onSelectRemote = onSelectRemoteProduct
                        )
                    }
                    2 -> {
                        // Custom Food Creation tab
                        CustomFoodTabContent(
                            onCreate = onCreateCustomFood
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentsTabContent(
    recentFoods: List<FoodEntity>,
    onSelect: (FoodEntity) -> Unit,
    onDelete: (FoodEntity) -> Unit
) {
    if (recentFoods.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Aucun aliment récent",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Recherchez un aliment sur Open Food Facts pour le mettre en cache.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "ALIMENTS RÉCENTS & FAVORIS EN CACHE (${recentFoods.size})",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            items(recentFoods, key = { it.id }) { food ->
                val portionLabel = "Dernière portion : ${food.defaultServingGrams.roundToInt()}g"
                FoodCardItem(
                    title = food.name,
                    subtitle = if (food.brand.isNotBlank()) food.brand else "",
                    caloriesPer100g = food.caloriesPer100g,
                    proteins = food.proteinsPer100g,
                    carbs = food.carbsPer100g,
                    fat = food.fatPer100g,
                    fiber = food.fiberPer100g,
                    salt = food.saltPer100g,
                    servingDisplay = portionLabel,
                    onClick = { onSelect(food) },
                    onDelete = { onDelete(food) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun OffSearchTabContent(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isLoading: Boolean,
    results: List<OffProductDto>,
    errorMessage: String?,
    localMatches: List<FoodEntity>,
    offCountry: String,
    offStore: String,
    onSelectLocal: (FoodEntity) -> Unit,
    onSelectRemote: (OffProductDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Search Bar with fixed size and search icon button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Rechercher (ex: Pomme, Riz, Pâtes...)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Effacer",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_food_input")
            )

            FilledIconButton(
                onClick = onSearch,
                enabled = query.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = FatSecretGreen,
                    contentColor = Color.White,
                    disabledContainerColor = FatSecretGreen.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .size(52.dp)
                    .testTag("search_button")
            ) {
                if (isLoading) {
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

        Spacer(modifier = Modifier.height(6.dp))

        // Active country / store indicator
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
        val storeInfo = if (offStore.isNotBlank()) " • Enseigne: $offStore" else ""
        Text(
            text = "Base : $countryLabel$storeInfo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp,
                        color = FatSecretGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Recherche en cours sur Open Food Facts...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (query.isBlank()) "Rechercher sur Open Food Facts" else "Aucun résultat trouvé",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (query.isBlank()) {
                            "Tapez le nom d'un aliment (ex: Pâtes, Riz, Yaourt) puis validez."
                        } else {
                            "Vérifiez l'orthographe ou essayez un mot-clé plus simple."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "${results.size} RÉSULTAT${if (results.size > 1) "S" else ""} OPEN FOOD FACTS",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(results, key = { index, product -> "off_${product.code ?: "item"}_$index" }) { _, product ->
                    val kcal = product.getCaloriesPer100g() ?: 0.0
                    FoodCardItem(
                        title = product.getDisplayName(),
                        subtitle = product.getCleanBrand(),
                        caloriesPer100g = kcal,
                        proteins = product.getProteinsPer100g(),
                        carbs = product.getCarbsPer100g(),
                        fat = product.getFatPer100g(),
                        fiber = product.getFiberPer100g(),
                        salt = product.getSaltPer100g(),
                        servingDisplay = product.getCleanServingDisplay(),
                        onClick = { onSelectRemote(product) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CustomFoodTabContent(
    onCreate: (
        name: String,
        brand: String,
        kcal100g: Double,
        quantityGrams: Double,
        proteins: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        salt: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var kcalText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("g") }
    var pieceWeightGrams by remember { mutableDoubleStateOf(100.0) }
    var quantityField by remember(selectedUnit) {
        val initialStr = if (selectedUnit == "unité") "1" else "100"
        mutableStateOf(
            TextFieldValue(
                text = initialStr,
                selection = TextRange(0, initialStr.length)
            )
        )
    }
    var proteinsText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var fiberText by remember { mutableStateOf("") }
    var saltText by remember { mutableStateOf("") }

    val kcal = kcalText.toDoubleOrNull() ?: 0.0
    val rawQty = quantityField.text.toDoubleOrNull() ?: 0.0
    val effectiveGrams = when (selectedUnit) {
        "unité" -> rawQty * pieceWeightGrams
        else -> rawQty
    }
    val proteins = proteinsText.toDoubleOrNull() ?: 0.0
    val carbs = carbsText.toDoubleOrNull() ?: 0.0
    val fat = fatText.toDoubleOrNull() ?: 0.0
    val fiber = fiberText.toDoubleOrNull() ?: 0.0
    val salt = saltText.toDoubleOrNull() ?: 0.0
    val isValid = name.isNotBlank() && kcal > 0 && effectiveGrams > 0

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Créer un aliment personnalisé",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Enregistré 100% sur votre appareil pour réutilisation future.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom de l'aliment *") },
            placeholder = { Text("ex: Salade maison, Gâteau au chocolat") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("custom_food_name")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Marque ou origine (optionnel)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = kcalText,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) kcalText = it },
            label = { Text("Calories pour ${if (selectedUnit == "ml") "100ml" else "100g"} (kcal) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("custom_food_kcal")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Unit selector
        Text(
            text = "Unité de mesure :",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedUnit == "g",
                onClick = { selectedUnit = "g" },
                label = { Text("Au gramme (g)") }
            )
            FilterChip(
                selected = selectedUnit == "ml",
                onClick = { selectedUnit = "ml" },
                label = { Text("Au millilitre (ml)") }
            )
            FilterChip(
                selected = selectedUnit == "unité",
                onClick = { selectedUnit = "unité" },
                label = { Text("À l'unité") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = quantityField,
            onValueChange = { newValue ->
                val cleaned = newValue.text.replace(',', '.')
                if (cleaned.all { c -> c.isDigit() || c == '.' }) {
                    quantityField = newValue.copy(text = cleaned)
                }
            },
            label = {
                Text(
                    when (selectedUnit) {
                        "unité" -> "Portion consommée (unités) *"
                        "ml" -> "Portion consommée (ml) *"
                        else -> "Portion consommée (g) *"
                    }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        quantityField = quantityField.copy(
                            selection = TextRange(0, quantityField.text.length)
                        )
                    }
                }
                .testTag("custom_food_quantity")
        )

        if (selectedUnit == "unité") {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Poids d'1 unité : ${pieceWeightGrams.roundToInt()}g",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total : ${effectiveGrams.roundToInt()}g",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = FatSecretGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Valeurs nutritionnelles pour ${if (selectedUnit == "ml") "100ml" else "100g"} (optionnel) :",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 3 macros on one row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = proteinsText,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) proteinsText = it },
                label = { Text("Prot (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = carbsText,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) carbsText = it },
                label = { Text("Gluc (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = fatText,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) fatText = it },
                label = { Text("Lip (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fiber and Salt row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fiberText,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) fiberText = it },
                label = { Text("Fibres (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = saltText,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) saltText = it },
                label = { Text("Sel (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val calculatedTotal = (kcal * effectiveGrams / 100.0).roundToInt()
        Text(
            text = "Total pour ce repas : $calculatedTotal kcal",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (isValid) {
                    onCreate(name, brand, kcal, effectiveGrams, proteins, carbs, fat, fiber, salt)
                }
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().testTag("save_custom_food_button")
        ) {
            Icon(imageVector = Icons.Default.PostAdd, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Créer et ajouter au repas", maxLines = 1, softWrap = false)
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun FoodCardItem(
    title: String,
    subtitle: String,
    caloriesPer100g: Double,
    proteins: Double = 0.0,
    carbs: Double = 0.0,
    fat: Double = 0.0,
    fiber: Double = 0.0,
    salt: Double = 0.0,
    servingDisplay: String? = null,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val cleanTitle = title.replace(Regex("""[\r\n\t]+"""), " ").trim().ifBlank { "Aliment" }
    val cleanSubtitle = subtitle.replace(Regex("""[\r\n\t]+"""), " ").trim()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Subtitle on left, Calories badge on right (+ Delete button if present)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FatSecretGreen.copy(alpha = 0.15f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${caloriesPer100g.roundToInt()} kcal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = FatSecretGreen,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "pour 100g",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = FatSecretGreen,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer de l'historique",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.65f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nutrients row spanning full width with proportional MacroPill
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

            if (!servingDisplay.isNullOrBlank()) {
                Text(
                    text = servingDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tap hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Toucher pour choisir la portion ›",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = FatSecretGreen
                )
            }
        }
    }
}

@Composable
fun FoodRowItem(
    title: String,
    subtitle: String,
    caloriesPer100g: Double,
    proteins: Double = 0.0,
    carbs: Double = 0.0,
    fat: Double = 0.0,
    fiber: Double = 0.0,
    salt: Double = 0.0,
    servingDisplay: String? = null,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    FoodCardItem(
        title = title,
        subtitle = subtitle,
        caloriesPer100g = caloriesPer100g,
        proteins = proteins,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        salt = salt,
        servingDisplay = servingDisplay,
        onClick = onClick,
        onDelete = onDelete
    )
}
