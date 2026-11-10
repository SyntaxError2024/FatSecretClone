package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.data.model.ImportResult
import com.example.ui.DiaryViewModel
import com.example.ui.components.CalorieTargetDialog
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.CalorieOrange
import com.example.ui.theme.FatSecretGreen
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class CountryOption(val code: String, val label: String, val flag: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: DiaryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val calorieTarget by viewModel.calorieTarget.collectAsStateWithLifecycle()
    val offCountry by viewModel.offCountry.collectAsStateWithLifecycle()
    val offStore by viewModel.offStore.collectAsStateWithLifecycle()
    val dbStats by viewModel.databaseStats.collectAsStateWithLifecycle()

    var showCalorieDialog by remember { mutableStateOf(false) }
    var storeInputValue by remember(offStore) { mutableStateOf(offStore) }

    // Import / Export state
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var importOverwriteOption by remember { mutableStateOf(false) }
    var importSuccessResult by remember { mutableStateOf<ImportResult?>(null) }
    var exportJsonContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshDatabaseStats()
    }

    // Save Document launcher (Export to file)
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && exportJsonContent != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(exportJsonContent!!.toByteArray(Charsets.UTF_8))
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Sauvegarde enregistrée avec succès !", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Erreur d'écriture : ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Open Document launcher (Import from file)
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val json = inputStream.bufferedReader().use { it.readText() }
                        withContext(Dispatchers.Main) {
                            pendingImportJson = json
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Erreur de lecture du fichier : ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paramètres",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. OPEN FOOD FACTS SETTINGS
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = FatSecretGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Recherche Open Food Facts",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pays d'origine & enseignes pour les requêtes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Country Selection
                    Text(
                        text = "PAYS DE RECHERCHE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sélectionnez le pays pour interroger sa base nationale de produits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val countries = listOf(
                        CountryOption("fr", "France", "🇫🇷"),
                        CountryOption("de", "Allemagne", "🇩🇪"),
                        CountryOption("be", "Belgique", "🇧🇪"),
                        CountryOption("ch", "Suisse", "🇨🇭"),
                        CountryOption("es", "Espagne", "🇪🇸"),
                        CountryOption("it", "Italie", "🇮🇹"),
                        CountryOption("uk", "Royaume-Uni", "🇬🇧"),
                        CountryOption("world", "Monde entier", "🌍")
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        countries.forEach { item ->
                            val isSelected = offCountry.equals(item.code, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setOffCountry(item.code) },
                                label = { Text("${item.flag} ${item.label}") },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FatSecretGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = FatSecretGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Store / Enseigne Filter
                    Text(
                        text = "ENSEIGNE / MAGASIN (OPTIONNEL)",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Filtre les résultats sur une marque de distributeur ou magasin (ex: Carrefour, Aldi, Lidl...).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = storeInputValue,
                        onValueChange = {
                            storeInputValue = it
                            viewModel.setOffStore(it)
                        },
                        label = { Text("Nom de l'enseigne") },
                        placeholder = { Text("ex: Carrefour, Aldi, Lidl...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                        },
                        trailingIcon = {
                            if (storeInputValue.isNotBlank()) {
                                IconButton(onClick = {
                                    storeInputValue = ""
                                    viewModel.setOffStore("")
                                }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_filter_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Store Chips
                    val popularStores = listOf(
                        "Toutes",
                        "Carrefour",
                        "Aldi",
                        "Lidl",
                        "E.Leclerc",
                        "Auchan",
                        "Intermarché",
                        "Monoprix",
                        "Super U",
                        "Rewe",
                        "Edeka"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularStores.forEach { storeName ->
                            val isSelected = if (storeName == "Toutes") {
                                storeInputValue.isBlank()
                            } else {
                                storeInputValue.equals(storeName, ignoreCase = true)
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val newVal = if (storeName == "Toutes") "" else storeName
                                    storeInputValue = newVal
                                    viewModel.setOffStore(newVal)
                                },
                                label = { Text(storeName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = AccentBlue
                                )
                            )
                        }
                    }
                }
            }

            // 2. DAILY CALORIE TARGET
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = CalorieOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Objectif Calorique",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Cible journalière : $calorieTarget kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showCalorieDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("edit_target_button")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modifier", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            // 3. IMPORT / EXPORT DATA & SETTINGS
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sauvegarde & Restauration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Importer ou exporter vos données & réglages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Exportez l'intégralité de vos repas, portions mémorisées, aliments personnalisés et réglages dans un fichier JSON pour changer de smartphone ou créer une sauvegarde.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Export Button
                        Button(
                            onClick = {
                                viewModel.exportBackup(BuildConfig.VERSION_NAME) { result ->
                                    result.onSuccess { json ->
                                        exportJsonContent = json
                                        val fileName = "journal_calorie_backup_${DateUtils.getTodayIso()}.json"
                                        saveFileLauncher.launch(fileName)
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Erreur d'exportation : ${err.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("export_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exporter les données (JSON)", maxLines = 1, softWrap = false)
                        }

                        // Share Export Button
                        OutlinedButton(
                            onClick = {
                                viewModel.exportBackup(BuildConfig.VERSION_NAME) { result ->
                                    result.onSuccess { json ->
                                        shareBackupFile(context, json)
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Erreur de partage : ${err.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("share_export_button")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Partager la sauvegarde", maxLines = 1, softWrap = false)
                        }

                        // Import Button
                        OutlinedButton(
                            onClick = { openFileLauncher.launch("application/json") },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("import_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Importer une sauvegarde (JSON)", maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            // 4. ABOUT (À PROPOS)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = FatSecretGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "À propos de l'application",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Journal Calorie • Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Database local stats
                    Text(
                        text = "BASE DE DONNÉES LOCALE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Repas enregistrés dans l'historique :", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${dbStats.entriesCount}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Aliments en mémoire / cache :", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${dbStats.foodsCount}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Privacy & Open Food Facts details
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = FatSecretGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "100% Hors-Ligne & Privé",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Vos entrées journalières, macros, portions et poids sont conservés exclusivement dans la mémoire SQLite de votre téléphone. Aucun compte n'est requis et aucune donnée n'est transmise.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Données nutritionnelles : Open Food Facts (licence ODbL). Projet mondial et collaboratif libre.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Calorie Target Dialog
    if (showCalorieDialog) {
        CalorieTargetDialog(
            currentTarget = calorieTarget,
            onDismiss = { showCalorieDialog = false },
            onSaveTarget = { newTarget ->
                viewModel.updateCalorieTarget(newTarget)
                showCalorieDialog = false
            }
        )
    }

    // Confirmation Dialog for Importing JSON
    pendingImportJson?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingImportJson = null },
            title = {
                Text(
                    text = "Confirmer l'importation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Une sauvegarde a été chargée. Comment souhaitez-vous appliquer les données ?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = !importOverwriteOption,
                            onClick = { importOverwriteOption = false }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fusionner avec les données actuelles (recommandé)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = importOverwriteOption,
                            onClick = { importOverwriteOption = true }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Remplacer entièrement la base de données",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toImport = json
                        val overwrite = importOverwriteOption
                        pendingImportJson = null
                        viewModel.importBackup(toImport, overwrite) { result ->
                            result.onSuccess { res ->
                                importSuccessResult = res
                            }.onFailure { err ->
                                Toast.makeText(context, "Échec de l'import : ${err.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Importer")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportJson = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Import Success Dialog
    importSuccessResult?.let { res ->
        AlertDialog(
            onDismissRequest = { importSuccessResult = null },
            title = {
                Text(
                    text = "Importation réussie !",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FatSecretGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Les données ont été restaurées :", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "• ${res.entriesCount} entrées de repas restaurées", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• ${res.foodsCount} aliments en cache / favoris", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• ${res.settingsCount} réglages appliqués", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { importSuccessResult = null }) {
                    Text("Super")
                }
            }
        )
    }
}

private fun shareBackupFile(context: Context, jsonContent: String) {
    try {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, "journal_calorie_backup_${DateUtils.getTodayIso()}.json")
        FileOutputStream(file).use { it.write(jsonContent.toByteArray(Charsets.UTF_8)) }

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde Journal Calorie")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Partager la sauvegarde"))
    } catch (e: Exception) {
        Toast.makeText(context, "Impossible de partager le fichier : ${e.message}", Toast.LENGTH_LONG).show()
    }
}
