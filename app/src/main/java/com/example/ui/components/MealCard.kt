package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.MealEntryEntity
import com.example.data.model.MealType
import com.example.ui.theme.FatSecretGreen
import kotlin.math.roundToInt

@Composable
fun MealCard(
    mealType: MealType,
    entries: List<MealEntryEntity>,
    onAddFoodClick: () -> Unit,
    onEntryClick: (MealEntryEntity) -> Unit,
    onDeleteEntryClick: (MealEntryEntity) -> Unit,
    isExpanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalKcal = entries.sumOf { it.totalCalories }.roundToInt()
    val icon: ImageVector = when (mealType) {
        MealType.BREAKFAST -> Icons.Default.WbSunny
        MealType.LUNCH -> Icons.Default.Restaurant
        MealType.DINNER -> Icons.Default.NightsStay
        MealType.SNACK -> Icons.Default.Fastfood
    }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("meal_card_${mealType.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Header: Click to collapse / expand category
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onToggleExpand
                    )
                    .testTag("meal_header_${mealType.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = mealType.titleFr,
                                tint = FatSecretGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = mealType.titleFr,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "$totalKcal kcal",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (totalKcal > 0) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (totalKcal > 0) FatSecretGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chevron indicating collapse/expand + Add button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Replier la catégorie" else "Déplier la catégorie",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(rotationAngle)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Explicit Add button
                    IconButton(
                        onClick = onAddFoodClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("add_food_${mealType.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter à ${mealType.titleFr}",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Collapsible Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    if (entries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        entries.forEachIndexed { index, entry ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            MealItemRow(
                                entry = entry,
                                onClick = { onEntryClick(entry) },
                                onDeleteClick = { onDeleteEntryClick(entry) }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Aucun aliment ajouté.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 36.dp, top = 2.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealItemRow(
    entry: MealEntryEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.foodName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val quantityFormatted = when (entry.unit) {
                "ml" -> {
                    val ml = if (entry.unitQuantity > 0) entry.unitQuantity else entry.quantityGrams
                    "${ml.roundToInt()} ml"
                }
                "unité" -> {
                    val count = if (entry.unitQuantity > 0) entry.unitQuantity else 1.0
                    val countStr = if (count % 1.0 == 0.0) count.toInt().toString() else "%.1f".format(count)
                    val unitLabel = if (count > 1.0) "unités" else "unité"
                    "$countStr $unitLabel (${entry.quantityGrams.roundToInt()}g)"
                }
                else -> "${entry.quantityGrams.roundToInt()}g"
            }

            val detailsText = buildString {
                if (entry.foodBrand.isNotBlank()) {
                    append(entry.foodBrand)
                    append(" • ")
                }
                append(quantityFormatted)
                if (entry.proteins > 0 || entry.carbs > 0 || entry.fat > 0) {
                    append(" • P:${"%.1f".format(entry.proteins)}g G:${"%.1f".format(entry.carbs)}g L:${"%.1f".format(entry.fat)}g")
                }
                if (entry.fiber > 0) {
                    append(" • Fib:${"%.1f".format(entry.fiber)}g")
                }
                if (entry.salt > 0) {
                    append(" • Sel:${"%.2f".format(entry.salt)}g")
                }
            }

            Text(
                text = detailsText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 6.dp)
        ) {
            Text(
                text = "${entry.totalCalories.roundToInt()} kcal",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 4.dp)
            )

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
