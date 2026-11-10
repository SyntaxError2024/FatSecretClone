package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import android.app.DatePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WeightEntryEntity
import com.example.ui.DiaryViewModel
import com.example.ui.WeightEvolution
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CalorieRed
import com.example.ui.theme.FatSecretGreen
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class ChartRange(val label: String, val days: Int) {
    SEVEN_DAYS("7 jours", 7),
    THIRTY_DAYS("30 jours", 30),
    THREE_MONTHS("3 mois", 90),
    ALL("Tout", 0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    viewModel: DiaryViewModel,
    modifier: Modifier = Modifier
) {
    val weightEntriesDesc by viewModel.weightEntriesDesc.collectAsState()
    val weightEntriesAsc by viewModel.weightEntriesAsc.collectAsState()
    val latestEntry by viewModel.latestWeightEntry.collectAsState()
    val targetWeight by viewModel.targetWeight.collectAsState()
    val userHeightCm by viewModel.userHeightCm.collectAsState()
    val evolution by viewModel.weightEvolution.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<WeightEntryEntity?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(ChartRange.THIRTY_DAYS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Suivi du poids",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showGoalDialog = true },
                        modifier = Modifier.testTag("weight_goals_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Objectif et taille",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            entryToEdit = null
                            showAddDialog = true
                        },
                        modifier = Modifier.testTag("weight_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter une pesée",
                            tint = FatSecretGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Overview card with latest weight, evolution, BMI and Goal
            item {
                WeightOverviewCard(
                    latestEntry = latestEntry,
                    evolution = evolution,
                    targetWeight = targetWeight,
                    userHeightCm = userHeightCm,
                    onOpenGoals = { showGoalDialog = true }
                )
            }

            // 2. Graphical canvas chart
            item {
                WeightChartCard(
                    entriesAsc = weightEntriesAsc,
                    targetWeight = targetWeight,
                    selectedRange = selectedRange,
                    onRangeSelected = { selectedRange = it }
                )
            }

            // 3. Entries history header without cramped "Nouvelle pesée" button
            item {
                Text(
                    text = "HISTORIQUE DES PESÉES (${weightEntriesDesc.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // 4. List of entries
            if (weightEntriesDesc.isEmpty()) {
                item {
                    EmptyWeightPlaceholder(
                        onAddClick = {
                            entryToEdit = null
                            showAddDialog = true
                        }
                    )
                }
            } else {
                itemsIndexed(
                    items = weightEntriesDesc,
                    key = { _, item -> item.id }
                ) { index, entry ->
                    // Calculate difference compared to the next chronological entry (which is at index + 1 in DESC order)
                    val previousEntry = weightEntriesDesc.getOrNull(index + 1)
                    val diffFromPrev = previousEntry?.let { entry.weightKg - it.weightKg }

                    WeightEntryRow(
                        entry = entry,
                        diffFromPrev = diffFromPrev,
                        onEdit = {
                            entryToEdit = entry
                            showAddDialog = true
                        },
                        onDelete = {
                            viewModel.deleteWeight(entry.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddDialog) {
        AddOrEditWeightDialog(
            initialEntry = entryToEdit,
            defaultWeight = latestEntry?.weightKg ?: 70.0,
            onDismiss = { showAddDialog = false },
            onConfirm = { date, weight, note ->
                viewModel.logWeight(date, weight, note)
                showAddDialog = false
            }
        )
    }

    if (showGoalDialog) {
        GoalAndHeightDialog(
            currentTarget = targetWeight,
            currentHeight = userHeightCm,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newTarget, newHeight ->
                viewModel.updateTargetWeight(newTarget)
                viewModel.updateUserHeight(newHeight)
                showGoalDialog = false
            }
        )
    }
}

@Composable
fun WeightOverviewCard(
    latestEntry: WeightEntryEntity?,
    evolution: WeightEvolution,
    targetWeight: Double?,
    userHeightCm: Int?,
    onOpenGoals: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weight_overview_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DERNIÈRE PESÉE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (latestEntry != null) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%.1f".format(latestEntry.weightKg),
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                            )
                        }
                        Text(
                            text = DateUtils.formatFriendlyDate(latestEntry.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Aucune pesée",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Commencez par enregistrer votre poids",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (evolution.totalDiff != null) {
                    EvolutionBadge(
                        diff = evolution.totalDiff,
                        label = "Total"
                    )
                }
            }

            // Variations grid (7 days, 30 days, Min, Max)
            if (evolution.entriesCount > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    evolution.diff7Days?.let { diff7 ->
                        VariationSmallCard(
                            title = "7 jours",
                            diff = diff7,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    evolution.diff30Days?.let { diff30 ->
                        VariationSmallCard(
                            title = "30 jours",
                            diff = diff30,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (evolution.minWeight != null && evolution.maxWeight != null) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Min / Max",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${"%.1f".format(evolution.minWeight)} / ${"%.1f".format(evolution.maxWeight)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // BMI (IMC) and Target Goal section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // BMI Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenGoals),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "IMC",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (evolution.bmi != null) {
                            val bmiValue = evolution.bmi
                            val (category, catColor) = getBmiCategory(bmiValue)
                            Text(
                                text = "%.1f".format(bmiValue),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = catColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "Définir taille",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = FatSecretGreen
                            )
                            Text(
                                text = "Pour calculer l'IMC",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Target weight Card
                Surface(
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable(onClick = onOpenGoals),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "OBJECTIF",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = CalorieOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (targetWeight != null && latestEntry != null) {
                            val diff = latestEntry.weightKg - targetWeight
                            val diffText = if (abs(diff) < 0.05) {
                                "Atteint !"
                            } else if (diff > 0) {
                                "-${"%.1f".format(diff)} kg à perdre"
                            } else {
                                "+${"%.1f".format(-diff)} kg à prendre"
                            }
                            Text(
                                text = "${"%.1f".format(targetWeight)} kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = diffText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (abs(diff) < 0.05) FatSecretGreen else CalorieOrange
                            )
                        } else if (targetWeight != null) {
                            Text(
                                text = "${"%.1f".format(targetWeight)} kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cible fixée",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Fixer un objectif",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = FatSecretGreen
                            )
                            Text(
                                text = "Poids cible souhaité",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvolutionBadge(
    diff: Double,
    label: String
) {
    val isLoss = diff < -0.05
    val isGain = diff > 0.05
    val color = when {
        isLoss -> FatSecretGreen
        isGain -> CalorieOrange
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = when {
                    isLoss -> Icons.Default.ArrowDownward
                    isGain -> Icons.Default.ArrowUpward
                    else -> Icons.AutoMirrored.Filled.TrendingFlat
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (diff > 0) "+" else ""}${"%.1f".format(diff)} kg",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = color
                )
            }
        }
    }
}

@Composable
fun VariationSmallCard(
    title: String,
    diff: Double,
    modifier: Modifier = Modifier
) {
    val isLoss = diff < -0.05
    val isGain = diff > 0.05
    val color = when {
        isLoss -> FatSecretGreen
        isGain -> CalorieOrange
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${if (diff > 0) "+" else ""}${"%.1f".format(diff)} kg",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
fun WeightChartCard(
    entriesAsc: List<WeightEntryEntity>,
    targetWeight: Double?,
    selectedRange: ChartRange,
    onRangeSelected: (ChartRange) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weight_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with range filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ÉVOLUTION DU POIDS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                // Range pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ChartRange.values().forEach { range ->
                        val isSelected = range == selectedRange
                        Surface(
                            modifier = Modifier
                                .clickable { onRangeSelected(range) }
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FatSecretGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = range.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter entries by range
            val filteredEntries = remember(entriesAsc, selectedRange) {
                if (selectedRange.days == 0 || entriesAsc.isEmpty()) {
                    entriesAsc
                } else {
                    val cutoffDate = DateUtils.shiftDays(DateUtils.getTodayIso(), -selectedRange.days)
                    val result = entriesAsc.filter { it.date >= cutoffDate }
                    // If filtered is empty or only 1, fallback to at least last few entries
                    if (result.isEmpty()) entriesAsc.takeLast(selectedRange.days) else result
                }
            }

            if (filteredEntries.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (filteredEntries.isEmpty()) {
                                "Enregistrez au moins 2 pesées pour tracer la courbe"
                            } else {
                                "Ajoutez une autre pesée pour visualiser l'évolution"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                WeightCanvasChart(
                    entries = filteredEntries,
                    targetWeight = targetWeight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
fun WeightCanvasChart(
    entries: List<WeightEntryEntity>,
    targetWeight: Double?,
    modifier: Modifier = Modifier
) {
    val primaryColor = FatSecretGreen
    val targetColor = CalorieOrange
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val textPaintColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f).hashCode()

    Canvas(modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
        val width = size.width
        val height = size.height

        val weights = entries.map { it.weightKg }
        var rawMin = weights.minOrNull() ?: 60.0
        var rawMax = weights.maxOrNull() ?: 80.0

        if (targetWeight != null) {
            rawMin = min(rawMin, targetWeight)
            rawMax = max(rawMax, targetWeight)
        }

        // Expand bounds slightly for margin
        val span = max(rawMax - rawMin, 1.0)
        val padding = span * 0.15
        val minY = rawMin - padding
        val maxY = rawMax + padding
        val totalYRange = max(maxY - minY, 0.5)

        val paddingLeft = 36.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 24.dp.toPx()

        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        fun getX(index: Int): Float {
            return if (entries.size == 1) {
                paddingLeft + plotWidth / 2f
            } else {
                paddingLeft + (index.toFloat() / (entries.size - 1)) * plotWidth
            }
        }

        fun getY(weight: Double): Float {
            val ratio = ((weight - minY) / totalYRange).toFloat()
            return paddingTop + plotHeight * (1f - ratio)
        }

        // 1. Draw horizontal grid lines & labels (3 steps)
        val gridSteps = 3
        val paint = android.graphics.Paint().apply {
            color = textPaintColor
            textSize = 24f
            isAntiAlias = true
        }

        for (i in 0..gridSteps) {
            val stepWeight = minY + (totalYRange * (i.toFloat() / gridSteps))
            val lineY = getY(stepWeight)

            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, lineY),
                end = Offset(width - paddingRight, lineY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // Weight label
            val label = "%.1f".format(stepWeight)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                4f,
                lineY + 8f,
                paint
            )
        }

        // 2. Target weight line if within range
        if (targetWeight != null) {
            val targetY = getY(targetWeight)
            if (targetY in paddingTop..(paddingTop + plotHeight)) {
                drawLine(
                    color = targetColor.copy(alpha = 0.8f),
                    start = Offset(paddingLeft, targetY),
                    end = Offset(width - paddingRight, targetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                )
            }
        }

        // 3. Curve Path and Fill Path
        val linePath = Path()
        val fillPath = Path()

        val points = entries.mapIndexed { index, item ->
            Offset(getX(index), getY(item.weightKg))
        }

        if (points.isNotEmpty()) {
            linePath.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, paddingTop + plotHeight)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                val cx = (p0.x + p1.x) / 2f
                linePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, paddingTop + plotHeight)
            fillPath.close()

            // Draw area gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.30f),
                        primaryColor.copy(alpha = 0.02f)
                    ),
                    startY = paddingTop,
                    endY = paddingTop + plotHeight
                )
            )

            // Draw line
            drawPath(
                path = linePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw point dots
            points.forEachIndexed { idx, pt ->
                val isLast = idx == points.size - 1

                // Outer circle
                drawCircle(
                    color = Color.White,
                    radius = if (isLast) 5.dp.toPx() else 3.5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = primaryColor,
                    radius = if (isLast) 3.5.dp.toPx() else 2.5.dp.toPx(),
                    center = pt
                )

                // Label on the latest point
                if (isLast) {
                    val lastWeightLabel = "%.1f".format(entries[idx].weightKg)
                    drawContext.canvas.nativeCanvas.drawText(
                        "$lastWeightLabel kg",
                        pt.x - 30f,
                        pt.y - 12f,
                        paint.apply { isFakeBoldText = true }
                    )
                }
            }
        }

        // 4. Draw X-axis date labels (start, end)
        if (entries.isNotEmpty()) {
            val firstDateText = formatShortDate(entries.first().date)
            val lastDateText = formatShortDate(entries.last().date)

            drawContext.canvas.nativeCanvas.drawText(
                firstDateText,
                paddingLeft,
                height - 4f,
                paint.apply { isFakeBoldText = false }
            )

            if (entries.size > 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    lastDateText,
                    width - paddingRight - 60f,
                    height - 4f,
                    paint
                )
            }
        }
    }
}

fun formatShortDate(isoDate: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
        val d = sdf.parse(isoDate) ?: return isoDate
        SimpleDateFormat("d MMM", Locale.FRANCE).format(d)
    } catch (e: Exception) {
        isoDate
    }
}

@Composable
fun WeightEntryRow(
    entry: WeightEntryEntity,
    diffFromPrev: Double?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("weight_entry_row_${entry.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = DateUtils.formatFriendlyDate(entry.date),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (entry.note.isNotBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Weight and diff
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${"%.1f".format(entry.weightKg)} kg",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (diffFromPrev != null) {
                        val isLoss = diffFromPrev < -0.05
                        val isGain = diffFromPrev > 0.05
                        val diffColor = when {
                            isLoss -> FatSecretGreen
                            isGain -> CalorieOrange
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val prefix = if (diffFromPrev > 0) "+" else ""
                        Text(
                            text = "$prefix${"%.1f".format(diffFromPrev)} kg",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = diffColor
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyWeightPlaceholder(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MonitorWeight,
                contentDescription = null,
                tint = FatSecretGreen.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Aucune pesée enregistrée",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Suivez régulièrement votre progression en ajoutant votre poids.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = FatSecretGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enregistrer une pesée", maxLines = 1, softWrap = false)
            }
        }
    }
}

@Composable
fun AddOrEditWeightDialog(
    initialEntry: WeightEntryEntity?,
    defaultWeight: Double,
    onDismiss: () -> Unit,
    onConfirm: (date: String, weight: Double, note: String) -> Unit
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(initialEntry?.date ?: DateUtils.getTodayIso()) }
    var weightStr by remember {
        mutableStateOf(
            initialEntry?.let { "%.1f".format(it.weightKg) } ?: "%.1f".format(defaultWeight)
        )
    }
    var note by remember { mutableStateOf(initialEntry?.note ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun adjustWeight(delta: Double) {
        val current = weightStr.replace(',', '.').toDoubleOrNull() ?: defaultWeight
        val updated = max(20.0, min(300.0, current + delta))
        weightStr = "%.1f".format(updated).replace(',', '.')
    }

    fun openDatePicker() {
        val cal = Calendar.getInstance().apply {
            try {
                val parts = date.split("-")
                if (parts.size == 3) {
                    set(Calendar.YEAR, parts[0].toInt())
                    set(Calendar.MONTH, parts[1].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                }
            } catch (_: Exception) {}
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                date = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialEntry != null) "Modifier la pesée" else "Enregistrer une pesée",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick date selector (chips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val today = DateUtils.getTodayIso()
                    val yesterday = DateUtils.shiftDays(today, -1)

                    FilterChip(
                        selected = date == today,
                        onClick = { date = today },
                        label = { Text("Aujourd'hui") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FatSecretGreen.copy(alpha = 0.2f),
                            selectedLabelColor = FatSecretGreen
                        )
                    )
                    FilterChip(
                        selected = date == yesterday,
                        onClick = { date = yesterday },
                        label = { Text("Hier") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FatSecretGreen.copy(alpha = 0.2f),
                            selectedLabelColor = FatSecretGreen
                        )
                    )
                }

                // Date Picker Card replacing raw inputbox
                OutlinedCard(
                    onClick = { openDatePicker() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_date_picker_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date de la pesée",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = DateUtils.formatFriendlyDate(date),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FatSecretGreen.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Choisir la date dans le calendrier",
                                    tint = FatSecretGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Weight input
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = {
                        weightStr = it
                        errorMsg = null
                    },
                    label = { Text("Poids en kg") },
                    placeholder = { Text("ex: 72.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    trailingIcon = { Text("kg", modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_input_field")
                )

                // Quick increment / decrement buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { adjustWeight(-0.5) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("-0.5")
                    }
                    OutlinedButton(
                        onClick = { adjustWeight(-0.1) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("-0.1")
                    }
                    OutlinedButton(
                        onClick = { adjustWeight(+0.1) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("+0.1")
                    }
                    OutlinedButton(
                        onClick = { adjustWeight(+0.5) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("+0.5")
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note facultative") },
                    placeholder = { Text("ex: À jeun, après le sport...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = weightStr.replace(',', '.').toDoubleOrNull()
                    if (parsed == null || parsed <= 0 || parsed > 400) {
                        errorMsg = "Veuillez entrer un poids valide (ex: 72.5)"
                        return@Button
                    }
                    onConfirm(date.trim(), parsed, note.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = FatSecretGreen),
                modifier = Modifier.testTag("weight_confirm_button")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun GoalAndHeightDialog(
    currentTarget: Double?,
    currentHeight: Int?,
    onDismiss: () -> Unit,
    onConfirm: (targetKg: Double?, heightCm: Int?) -> Unit
) {
    var targetStr by remember {
        mutableStateOf(currentTarget?.let { "%.1f".format(it) } ?: "")
    }
    var heightStr by remember {
        mutableStateOf(currentHeight?.toString() ?: "")
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Objectif & Données IMC",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Configurez votre objectif de poids et votre taille pour calculer automatiquement l'indice de masse corporelle (IMC).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Poids cible (kg)") },
                    placeholder = { Text("ex: 68.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    trailingIcon = { Text("kg", modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it },
                    label = { Text("Taille (cm)") },
                    placeholder = { Text("ex: 175") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    trailingIcon = { Text("cm", modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedTarget = if (targetStr.isBlank()) null else targetStr.replace(',', '.').toDoubleOrNull()
                    val parsedHeight = if (heightStr.isBlank()) null else heightStr.trim().toIntOrNull()

                    if (targetStr.isNotBlank() && (parsedTarget == null || parsedTarget <= 20 || parsedTarget > 350)) {
                        errorMsg = "Poids cible invalide"
                        return@Button
                    }
                    if (heightStr.isNotBlank() && (parsedHeight == null || parsedHeight < 50 || parsedHeight > 250)) {
                        errorMsg = "Taille invalide (en cm, ex: 175)"
                        return@Button
                    }

                    onConfirm(parsedTarget, parsedHeight)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FatSecretGreen)
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

fun getBmiCategory(bmi: Double): Pair<String, Color> {
    return when {
        bmi < 18.5 -> Pair("Insuffisance pondérale", AccentBlue)
        bmi in 18.5..24.9 -> Pair("Poids normal", FatSecretGreen)
        bmi in 25.0..29.9 -> Pair("Surpoids", CalorieOrange)
        else -> Pair("Obésité", CalorieRed)
    }
}
