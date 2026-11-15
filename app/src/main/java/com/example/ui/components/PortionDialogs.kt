package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.MealEntryEntity
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CalorieRed
import com.example.ui.theme.FatSecretGreen
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun UnitSelectorRow(
    selectedUnit: String,
    onSelectUnit: (String) -> Unit
) {
    Column {
        Text(
            text = "Unité de mesure :",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val units = listOf(
                Triple("g", "g", "Grammes"),
                Triple("ml", "ml", "Millilitres"),
                Triple("unité", "Unité", "À la pièce")
            )
            units.forEach { (unitKey, symbol, label) ->
                val isSelected = selectedUnit == unitKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectUnit(unitKey) }
                        .padding(vertical = 6.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PieceWeightSelector(
    pieceWeightGrams: Double,
    effectiveGrams: Double,
    onSelectWeight: (Double) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1 unité = ${pieceWeightGrams.roundToInt()}g",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "Total : ${effectiveGrams.roundToInt()}g",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = FatSecretGreen,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(30, 50, 60, 100, 120, 150, 200).forEach { weight ->
                    val isSelected = pieceWeightGrams.roundToInt() == weight
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable { onSelectWeight(weight.toDouble()) }
                    ) {
                        Text(
                            text = "${weight}g",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortionQuantityDialog(
    foodName: String,
    foodBrand: String,
    caloriesPer100g: Double,
    proteinsPer100g: Double = 0.0,
    carbsPer100g: Double = 0.0,
    fatPer100g: Double = 0.0,
    fiberPer100g: Double = 0.0,
    saltPer100g: Double = 0.0,
    initialQuantityGrams: Double = 100.0,
    initialUnit: String = "g",
    initialUnitQuantity: Double = initialQuantityGrams,
    defaultServingGrams: Double = 100.0,
    onDismiss: () -> Unit,
    onConfirm: (quantityGrams: Double, unit: String, unitQuantity: Double) -> Unit
) {
    var selectedUnit by remember(initialUnit) {
        mutableStateOf(
            when (initialUnit.lowercase()) {
                "ml", "millilitre", "millilitres" -> "ml"
                "unité", "unite", "pièce", "piece", "portion" -> "unité"
                else -> "g"
            }
        )
    }

    var pieceWeightGrams by remember(defaultServingGrams) {
        mutableDoubleStateOf(if (defaultServingGrams > 0) defaultServingGrams else 100.0)
    }

    val initialText = remember(selectedUnit, initialUnit, initialUnitQuantity, initialQuantityGrams) {
        when (selectedUnit) {
            "unité" -> {
                val count = if (initialUnit == "unité" && initialUnitQuantity > 0) initialUnitQuantity else 1.0
                if (count % 1.0 == 0.0) count.toInt().toString() else count.toString()
            }
            "ml" -> {
                val ml = if (initialUnit == "ml" && initialUnitQuantity > 0) initialUnitQuantity else initialQuantityGrams
                ml.roundToInt().toString()
            }
            else -> {
                val grams = if (initialQuantityGrams > 0) initialQuantityGrams else 100.0
                grams.roundToInt().toString()
            }
        }
    }

    var quantityField by remember(selectedUnit) {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(0, initialText.length)
            )
        )
    }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    var isQtyFieldFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
        scrollState.scrollTo(0)
    }

    LaunchedEffect(isQtyFieldFocused) {
        if (isQtyFieldFocused) {
            delay(40)
            quantityField = quantityField.copy(
                selection = TextRange(0, quantityField.text.length)
            )
        }
    }

    val parsedQty = quantityField.text.toDoubleOrNull() ?: 0.0
    val effectiveGrams = when (selectedUnit) {
        "unité" -> parsedQty * pieceWeightGrams
        "ml" -> parsedQty
        else -> parsedQty
    }

    val calculatedKcal = (caloriesPer100g * effectiveGrams / 100.0).roundToInt()
    val calculatedP = (proteinsPer100g * effectiveGrams / 100.0)
    val calculatedC = (carbsPer100g * effectiveGrams / 100.0)
    val calculatedF = (fatPer100g * effectiveGrams / 100.0)
    val calculatedFiber = (fiberPer100g * effectiveGrams / 100.0)
    val calculatedSalt = (saltPer100g * effectiveGrams / 100.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // FIXED HEADER: Title, brand, base calories (never cut off!)
                Column {
                    Text(
                        text = foodName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (foodBrand.isNotBlank()) {
                        Text(
                            text = foodBrand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val baseLabel = if (selectedUnit == "ml") "100ml" else "100g"
                    Text(
                        text = "${caloriesPer100g.roundToInt()} kcal pour $baseLabel",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = FatSecretGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // SCROLLABLE BODY (Scrolls only if screen is constrained or keyboard is open)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Unit Selector
                    UnitSelectorRow(
                        selectedUnit = selectedUnit,
                        onSelectUnit = {
                            focusManager.clearFocus()
                            selectedUnit = it
                        }
                    )

                    // Quantity Input
                    OutlinedTextField(
                        value = quantityField,
                        onValueChange = { newValue ->
                            val cleaned = newValue.text.replace(',', '.')
                            if (cleaned.all { it.isDigit() || it == '.' }) {
                                quantityField = newValue.copy(text = cleaned)
                            }
                        },
                        label = {
                            Text(
                                when (selectedUnit) {
                                    "unité" -> "Nombre d'unités (ex: 1, 2, 0.5)"
                                    "ml" -> "Volume en millilitres (ml)"
                                    else -> "Quantité en grammes (g)"
                                },
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        trailingIcon = {
                            if (quantityField.text.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        quantityField = TextFieldValue(text = "", selection = TextRange.Zero)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Effacer la quantité",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isQtyFieldFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    quantityField = quantityField.copy(
                                        selection = TextRange(0, quantityField.text.length)
                                    )
                                }
                            }
                            .testTag("portion_input")
                    )

                    // If "À l'unité", show piece weight selector with non-wrapping flow chips
                    if (selectedUnit == "unité") {
                        PieceWeightSelector(
                            pieceWeightGrams = pieceWeightGrams,
                            effectiveGrams = effectiveGrams,
                            onSelectWeight = {
                                focusManager.clearFocus()
                                pieceWeightGrams = it
                            }
                        )
                    }

                    // Quick portions chips
                    Column {
                        Text(
                            text = when (selectedUnit) {
                                "unité" -> "Portions rapides (unités) :"
                                "ml" -> "Volumes rapides :"
                                else -> "Portions rapides :"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val quickValues = when (selectedUnit) {
                            "unité" -> listOf("0.5", "1", "1.5", "2", "3", "4", "5")
                            "ml" -> listOf("50", "100", "150", "200", "250", "330", "500")
                            else -> {
                                val baseGrams = listOf("30", "50", "100", "150", "200", "250")
                                val lastInt = initialQuantityGrams.roundToInt().toString()
                                if (initialQuantityGrams > 0 && lastInt !in baseGrams) {
                                    (baseGrams + lastInt).sortedBy { it.toIntOrNull() ?: 0 }
                                } else baseGrams
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickValues.forEach { chipText ->
                                val isSelected = quantityField.text == chipText
                                val chipLabel = when (selectedUnit) {
                                    "unité" -> "$chipText unité${if (chipText != "1" && chipText != "0.5") "s" else ""}"
                                    "ml" -> "${chipText}ml"
                                    else -> "${chipText}g"
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        focusManager.clearFocus()
                                        quantityField = TextFieldValue(
                                            text = chipText,
                                            selection = TextRange(0, chipText.length)
                                        )
                                    },
                                    label = { Text(chipLabel, fontSize = 12.sp, maxLines = 1, softWrap = false) }
                                )
                            }
                        }
                    }

                    // Calorie Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Énergie calculée :",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "$calculatedKcal kcal",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Macros breakdown
                    val hasMacros = proteinsPer100g > 0 || carbsPer100g > 0 || fatPer100g > 0 || fiberPer100g > 0 || saltPer100g > 0
                    if (hasMacros) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "P: ${"%.1f".format(calculatedP)}g",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = AccentBlue,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = "G: ${"%.1f".format(calculatedC)}g",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = CalorieOrange,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = "L: ${"%.1f".format(calculatedF)}g",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = CalorieRed,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                if (fiberPer100g > 0 || saltPer100g > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (fiberPer100g > 0) {
                                            Text(
                                                text = "Fibres: ${"%.1f".format(calculatedFiber)}g",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                        if (saltPer100g > 0) {
                                            Text(
                                                text = "Sel: ${"%.2f".format(calculatedSalt)}g",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // FIXED FOOTER BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (parsedQty > 0 && effectiveGrams > 0) {
                                onConfirm(effectiveGrams, selectedUnit, parsedQty)
                            }
                        },
                        enabled = parsedQty > 0 && effectiveGrams > 0,
                        modifier = Modifier.testTag("confirm_portion_button")
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditQuantityDialog(
    entry: MealEntryEntity,
    onDismiss: () -> Unit,
    onSave: (newQuantityGrams: Double, unit: String, unitQuantity: Double) -> Unit,
    onDelete: () -> Unit
) {
    var selectedUnit by remember(entry.unit) {
        mutableStateOf(
            when (entry.unit.lowercase()) {
                "ml", "millilitre" -> "ml"
                "unité", "unite", "portion" -> "unité"
                else -> "g"
            }
        )
    }

    val initialPieceWeight = remember(entry) {
        if (entry.unit == "unité" && entry.unitQuantity > 0) {
            entry.quantityGrams / entry.unitQuantity
        } else 100.0
    }
    var pieceWeightGrams by remember { mutableDoubleStateOf(if (initialPieceWeight > 0) initialPieceWeight else 100.0) }

    val initialQtyString = remember(selectedUnit, entry) {
        when (selectedUnit) {
            "unité" -> {
                val count = if (entry.unit == "unité" && entry.unitQuantity > 0) entry.unitQuantity else 1.0
                if (count % 1.0 == 0.0) count.toInt().toString() else count.toString()
            }
            "ml" -> {
                val ml = if (entry.unit == "ml" && entry.unitQuantity > 0) entry.unitQuantity else entry.quantityGrams
                ml.roundToInt().toString()
            }
            else -> entry.quantityGrams.roundToInt().toString()
        }
    }

    var quantityField by remember(selectedUnit) {
        mutableStateOf(
            TextFieldValue(
                text = initialQtyString,
                selection = TextRange(0, initialQtyString.length)
            )
        )
    }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    var isQtyFieldFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
        scrollState.scrollTo(0)
    }

    LaunchedEffect(isQtyFieldFocused) {
        if (isQtyFieldFocused) {
            delay(40)
            quantityField = quantityField.copy(
                selection = TextRange(0, quantityField.text.length)
            )
        }
    }

    val parsedQty = quantityField.text.toDoubleOrNull() ?: 0.0
    val effectiveGrams = when (selectedUnit) {
        "unité" -> parsedQty * pieceWeightGrams
        "ml" -> parsedQty
        else -> parsedQty
    }

    val calculatedKcal = (entry.caloriesPer100g * effectiveGrams / 100.0).roundToInt()
    val ratio = if (entry.quantityGrams > 0) effectiveGrams / entry.quantityGrams else 1.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // FIXED HEADER
                Column {
                    Text(
                        text = "Modifier la portion",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = entry.foodName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entry.foodBrand.isNotBlank()) {
                        Text(
                            text = entry.foodBrand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // SCROLLABLE BODY
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Unit Selector
                    UnitSelectorRow(
                        selectedUnit = selectedUnit,
                        onSelectUnit = {
                            focusManager.clearFocus()
                            selectedUnit = it
                        }
                    )

                    // Quantity Input
                    OutlinedTextField(
                        value = quantityField,
                        onValueChange = { newValue ->
                            val cleaned = newValue.text.replace(',', '.')
                            if (cleaned.all { it.isDigit() || it == '.' }) {
                                quantityField = newValue.copy(text = cleaned)
                            }
                        },
                        label = {
                            Text(
                                when (selectedUnit) {
                                    "unité" -> "Nombre d'unités (ex: 1, 2, 0.5)"
                                    "ml" -> "Volume en millilitres (ml)"
                                    else -> "Quantité en grammes (g)"
                                },
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        trailingIcon = {
                            if (quantityField.text.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        quantityField = TextFieldValue(text = "", selection = TextRange.Zero)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Effacer la quantité",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isQtyFieldFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    quantityField = quantityField.copy(
                                        selection = TextRange(0, quantityField.text.length)
                                    )
                                }
                            }
                    )

                    // If "À l'unité", show piece weight selector with non-wrapping flow chips
                    if (selectedUnit == "unité") {
                        PieceWeightSelector(
                            pieceWeightGrams = pieceWeightGrams,
                            effectiveGrams = effectiveGrams,
                            onSelectWeight = {
                                focusManager.clearFocus()
                                pieceWeightGrams = it
                            }
                        )
                    }

                    // Quick portions chips
                    Column {
                        val quickValues = when (selectedUnit) {
                            "unité" -> listOf("0.5", "1", "1.5", "2", "3", "4", "5")
                            "ml" -> listOf("50", "100", "150", "200", "250", "330", "500")
                            else -> listOf("30", "50", "100", "150", "200", "250")
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickValues.forEach { chipText ->
                                val isSelected = quantityField.text == chipText
                                val chipLabel = when (selectedUnit) {
                                    "unité" -> "$chipText unité${if (chipText != "1" && chipText != "0.5") "s" else ""}"
                                    "ml" -> "${chipText}ml"
                                    else -> "${chipText}g"
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        focusManager.clearFocus()
                                        quantityField = TextFieldValue(
                                            text = chipText,
                                            selection = TextRange(0, chipText.length)
                                        )
                                    },
                                    label = { Text(chipLabel, fontSize = 11.sp, maxLines = 1, softWrap = false) }
                                )
                            }
                        }
                    }

                    // Calorie total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total :",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "$calculatedKcal kcal",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Macros
                    if (entry.proteins > 0 || entry.carbs > 0 || entry.fat > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "P: ${"%.1f".format(entry.proteins * ratio)}g",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentBlue,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "G: ${"%.1f".format(entry.carbs * ratio)}g",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CalorieOrange,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "L: ${"%.1f".format(entry.fat * ratio)}g",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CalorieRed,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Delete button
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CalorieRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Supprimer cet aliment")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // FIXED FOOTER BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fermer")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (parsedQty > 0 && effectiveGrams > 0) {
                                onSave(effectiveGrams, selectedUnit, parsedQty)
                            }
                        },
                        enabled = parsedQty > 0 && effectiveGrams > 0
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}
