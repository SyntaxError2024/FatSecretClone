package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DayStat
import com.example.ui.DiaryViewModel
import com.example.ui.WeeklyStatsSummary
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.CalorieRed
import com.example.ui.theme.FatSecretGreen
import com.example.util.DateUtils
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: DiaryViewModel,
    onNavigateToDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statsSummary by viewModel.weeklyStats.collectAsStateWithLifecycle()
    var selectedDayStat by remember { mutableStateOf<DayStat?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Clean Header: "Statistiques"
        TopAppBar(
            title = {
                Text(
                    text = "Statistiques",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Dedicated Full-Width Week Navigator (never truncated)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.goToPreviousWeek() },
                    modifier = Modifier.testTag("prev_week_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Semaine précédente"
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = statsSummary.weekTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (statsSummary.isCurrentWeek) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FatSecretGreen.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Cette semaine",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FatSecretGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Toucher pour revenir à cette semaine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { viewModel.goToCurrentWeek() }
                                .padding(top = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.goToNextWeek() },
                    modifier = Modifier.testTag("next_week_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Semaine suivante"
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2 Spacious Summary Cards: strictly for the week viewed!
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Moyenne / jour",
                        value = if (statsSummary.recordedDaysCount > 0) "${statsSummary.averageCalories.roundToInt()} kcal" else "—",
                        subtitle = if (statsSummary.recordedDaysCount > 0) "${statsSummary.recordedDaysCount} j renseignés" else "Aucune mesure",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Total semaine",
                        value = "${statsSummary.totalCalories.roundToInt()} kcal",
                        subtitle = "${statsSummary.daysOnTargetCount} / ${statsSummary.recordedDaysCount.coerceAtLeast(1)} j sous l'objectif",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Weekly Chart Card: clean header and dedicated legend row (no vertical wrapping!)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_chart_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        // Title
                        Text(
                            text = "RÉPARTITION DE LA SEMAINE",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Dedicated Legend Row: ample space, will NEVER wrap into a vertical column!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(FatSecretGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Objectif respecté",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(CalorieRed, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dépassement",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        WeeklyBarChart(
                            dailyStats = statsSummary.dailyStats,
                            targetCalories = statsSummary.targetCalories,
                            selectedStat = selectedDayStat,
                            onSelectDay = { selectedDayStat = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Touchez une colonne pour afficher le détail du jour.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Selected Day Inspection Card
            selectedDayStat?.let { day ->
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = DateUtils.formatFriendlyDate(day.dateIso),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val diff = day.totalCalories.roundToInt() - day.targetCalories
                                val statusText = when {
                                    !day.isRecorded -> "Aucune entrée"
                                    diff <= 0 -> "${day.totalCalories.roundToInt()} kcal (${-diff} kcal restantes)"
                                    else -> "${day.totalCalories.roundToInt()} kcal (+$diff kcal de dépassement)"
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (diff > 0) CalorieRed else FatSecretGreen
                                )
                            }

                            Button(
                                onClick = { onNavigateToDate(day.dateIso) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voir le journal")
                            }
                        }
                    }
                }
            }

            // All 7 Days of the week in chronological order (Lun -> Dim)
            item {
                Text(
                    text = "DÉTAIL DES 7 JOURS DE LA SEMAINE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(statsSummary.dailyStats, key = { it.dateIso }) { day ->
                RecordedDayRow(
                    day = day,
                    isSelected = selectedDayStat?.dateIso == day.dateIso,
                    onClick = {
                        selectedDayStat = day
                        onNavigateToDate(day.dateIso)
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(
    dailyStats: List<DayStat>,
    targetCalories: Int,
    selectedStat: DayStat?,
    onSelectDay: (DayStat) -> Unit
) {
    if (dailyStats.isEmpty()) return

    val todayIso = remember { DateUtils.getTodayIso() }
    val maxVal = max(
        (dailyStats.maxOfOrNull { it.totalCalories } ?: 0.0),
        targetCalories.toDouble() * 1.25
    ).coerceAtLeast(1000.0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dailyStats) {
                        detectTapGestures { offset ->
                            val count = dailyStats.size
                            if (count > 0) {
                                val slotWidth = size.width / count
                                val index = (offset.x / slotWidth).toInt().coerceIn(0, count - 1)
                                onSelectDay(dailyStats[index])
                            }
                        }
                    }
            ) {
                val chartWidth = size.width
                val chartHeight = size.height - 16.dp.toPx()
                val count = dailyStats.size
                val slotWidth = chartWidth / count
                val barWidth = (slotWidth * 0.45f).coerceIn(16.dp.toPx(), 36.dp.toPx())

                // Target dashed line
                val targetY = chartHeight - (targetCalories / maxVal.toFloat() * chartHeight)
                drawLine(
                    color = Color(0xFF6B7280),
                    start = Offset(0f, targetY),
                    end = Offset(chartWidth, targetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Draw each day's bar
                dailyStats.forEachIndexed { index, day ->
                    val centerX = index * slotWidth + slotWidth / 2f
                    val left = centerX - barWidth / 2f
                    val isSelected = selectedStat?.dateIso == day.dateIso

                    if (isSelected) {
                        drawRoundRect(
                            color = Color(0xFF3B82F6).copy(alpha = 0.12f),
                            topLeft = Offset(centerX - slotWidth * 0.45f, 0f),
                            size = Size(slotWidth * 0.9f, chartHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }

                    if (day.isRecorded && day.totalCalories > 0) {
                        val barHeight = (day.totalCalories / maxVal * chartHeight).toFloat().coerceAtLeast(6.dp.toPx())
                        val top = chartHeight - barHeight

                        val barColor = when {
                            day.totalCalories <= targetCalories -> FatSecretGreen
                            day.totalCalories <= targetCalories * 1.1 -> CalorieOrange
                            else -> CalorieRed
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    } else {
                        drawRoundRect(
                            color = Color(0xFF262933),
                            topLeft = Offset(left, chartHeight - 4.dp.toPx()),
                            size = Size(barWidth, 4.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Days labels below the bars (Lun, Mar, Mer, Jeu, Ven, Sam, Dim + day number)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dailyStats.forEach { day ->
                val isSelected = selectedStat?.dateIso == day.dateIso
                val isToday = day.dateIso == todayIso
                val dayName = day.dayLabel.ifBlank { DateUtils.getDayOfWeekShort(day.dateIso) }

                Column(
                    modifier = Modifier
                        .clickable { onSelectDay(day) }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = when {
                            isToday -> FatSecretGreen
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "${day.dayNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal
                        ),
                        color = if (isToday) FatSecretGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordedDayRow(
    day: DayStat,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val dayName = day.dayLabel.ifBlank { DateUtils.getDayOfWeekShort(day.dateIso) }
            Text(
                text = "$dayName • ${DateUtils.formatFriendlyDate(day.dateIso)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (day.isRecorded) {
                val isUnder = day.totalCalories <= day.targetCalories
                val diff = (day.totalCalories - day.targetCalories).roundToInt()
                Text(
                    text = if (isUnder) "Objectif respecté" else "Dépassement de $diff kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUnder) FatSecretGreen else CalorieRed
                )
            } else {
                Text(
                    text = "Aucune entrée enregistrée",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (day.isRecorded) "${day.totalCalories.roundToInt()} kcal" else "—",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (day.isRecorded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 8.dp)
            )

            IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Ouvrir",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
