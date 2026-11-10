package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FoodEntity
import com.example.data.local.MealEntryEntity
import com.example.data.local.WeightEntryEntity
import com.example.data.model.DatabaseStats
import com.example.data.model.ImportResult
import com.example.data.model.MealType
import com.example.data.remote.OffProductDto
import com.example.data.repository.DiaryRepository
import com.example.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeightEvolution(
    val latestWeight: Double?,
    val startingWeight: Double?,
    val totalDiff: Double?,
    val diff7Days: Double?,
    val diff30Days: Double?,
    val minWeight: Double?,
    val maxWeight: Double?,
    val targetWeight: Double?,
    val userHeightCm: Int?,
    val bmi: Double?,
    val entriesCount: Int
)

data class DayStat(
    val dateIso: String,
    val dayNumber: Int,
    val dayLabel: String = "",
    val totalCalories: Double,
    val targetCalories: Int,
    val isRecorded: Boolean
)

data class WeeklyStatsSummary(
    val weekMondayIso: String,
    val weekTitle: String,
    val isCurrentWeek: Boolean,
    val dailyStats: List<DayStat>,
    val averageCalories: Double,
    val totalCalories: Double,
    val recordedDaysCount: Int,
    val targetCalories: Int,
    val daysOnTargetCount: Int
)

data class MonthlyStatsSummary(
    val yearMonth: String,
    val monthTitle: String,
    val dailyStats: List<DayStat>,
    val averageCalories: Double,
    val recordedDaysCount: Int,
    val targetCalories: Int,
    val daysOnTargetCount: Int
)

sealed interface SearchItemUi {
    data class LocalFood(val food: FoodEntity) : SearchItemUi
    data class RemoteFood(val product: OffProductDto) : SearchItemUi
}

data class PortionDialogState(
    val foodName: String,
    val foodBrand: String,
    val caloriesPer100g: Double,
    val proteinsPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val fiberPer100g: Double = 0.0,
    val saltPer100g: Double = 0.0,
    val initialQuantityGrams: Double = 100.0,
    val initialUnit: String = "g",
    val initialUnitQuantity: Double = initialQuantityGrams,
    val defaultServingGrams: Double = 100.0,
    val mealType: MealType,
    val localFood: FoodEntity? = null,
    val remoteProduct: OffProductDto? = null
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class DiaryViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.syncLastQuantities()
            refreshDatabaseStats()
        }
    }

    // Open Food Facts Country & Store Settings
    val offCountry: StateFlow<String> = repository.getOffCountry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryRepository.DEFAULT_OFF_COUNTRY)

    val offStore: StateFlow<String> = repository.getOffStore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryRepository.DEFAULT_OFF_STORE)

    // Database stats for About section
    private val _databaseStats = MutableStateFlow(DatabaseStats(0, 0))
    val databaseStats: StateFlow<DatabaseStats> = _databaseStats.asStateFlow()

    // Portion dialog state (remembers last used quantity)
    private val _activePortionDialog = MutableStateFlow<PortionDialogState?>(null)
    val activePortionDialog: StateFlow<PortionDialogState?> = _activePortionDialog.asStateFlow()

    // Current selected date for the diary
    private val _selectedDate = MutableStateFlow(DateUtils.getTodayIso())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Daily entries for selected date
    val currentEntries: StateFlow<List<MealEntryEntity>> = _selectedDate
        .flatMapLatest { date -> repository.getEntriesForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calorie target
    val calorieTarget: StateFlow<Int> = repository.getDailyCalorieTarget()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryRepository.DEFAULT_CALORIE_TARGET)

    // Recent foods from local history / cache
    val recentFoods: StateFlow<List<FoodEntity>> = repository.getRecentFoods(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state
    val searchQuery = MutableStateFlow("")
    private val _isSearchingRemote = MutableStateFlow(false)
    val isSearchingRemote: StateFlow<Boolean> = _isSearchingRemote.asStateFlow()

    private val _remoteSearchResults = MutableStateFlow<List<OffProductDto>>(emptyList())
    val remoteSearchResults: StateFlow<List<OffProductDto>> = _remoteSearchResults.asStateFlow()

    private val _remoteSearchError = MutableStateFlow<String?>(null)
    val remoteSearchError: StateFlow<String?> = _remoteSearchError.asStateFlow()

    private val _savedToHistoryCodes = MutableStateFlow<Set<String>>(emptySet())
    val savedToHistoryCodes: StateFlow<Set<String>> = _savedToHistoryCodes.asStateFlow()

    // Combined local search
    val localSearchResults: StateFlow<List<FoodEntity>> = searchQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getRecentFoods(20)
            } else {
                repository.searchLocalFoods(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected week for statistics (Monday ISO) - defaults to the current week
    private val _selectedWeek = MutableStateFlow(DateUtils.getCurrentWeekStartIso())
    val selectedWeek: StateFlow<String> = _selectedWeek.asStateFlow()

    // Weekly stats: only takes into account the days of the week currently viewed
    val weeklyStats: StateFlow<WeeklyStatsSummary> = combine(
        _selectedWeek,
        _selectedWeek.flatMapLatest { mondayIso ->
            val sundayIso = DateUtils.shiftDays(mondayIso, 6)
            repository.getEntriesBetweenDates(mondayIso, sundayIso)
        },
        calorieTarget
    ) { mondayIso, entries, target ->
        val daysInWeek = DateUtils.getDaysInWeek(mondayIso)
        val entriesByDate = entries.groupBy { it.date }

        var totalKcalSum = 0.0
        var recordedDays = 0
        var daysOnTarget = 0

        val dailyStats = daysInWeek.mapIndexed { index, dateIso ->
            val dayEntries = entriesByDate[dateIso] ?: emptyList()
            val totalKcal = dayEntries.sumOf { it.totalCalories }
            val hasEntries = dayEntries.isNotEmpty()
            if (hasEntries) {
                totalKcalSum += totalKcal
                recordedDays++
                if (totalKcal <= target) {
                    daysOnTarget++
                }
            }
            DayStat(
                dateIso = dateIso,
                dayNumber = try { dateIso.split("-")[2].toInt() } catch (_: Exception) { index + 1 },
                dayLabel = DateUtils.getDayOfWeekShort(dateIso),
                totalCalories = totalKcal,
                targetCalories = target,
                isRecorded = hasEntries
            )
        }

        val avg = if (recordedDays > 0) totalKcalSum / recordedDays else 0.0

        WeeklyStatsSummary(
            weekMondayIso = mondayIso,
            weekTitle = DateUtils.formatWeekTitle(mondayIso),
            isCurrentWeek = DateUtils.isCurrentWeek(mondayIso),
            dailyStats = dailyStats,
            averageCalories = avg,
            totalCalories = totalKcalSum,
            recordedDaysCount = recordedDays,
            targetCalories = target,
            daysOnTargetCount = daysOnTarget
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeeklyStatsSummary(
            weekMondayIso = DateUtils.getCurrentWeekStartIso(),
            weekTitle = DateUtils.formatWeekTitle(DateUtils.getCurrentWeekStartIso()),
            isCurrentWeek = true,
            dailyStats = emptyList(),
            averageCalories = 0.0,
            totalCalories = 0.0,
            recordedDaysCount = 0,
            targetCalories = DiaryRepository.DEFAULT_CALORIE_TARGET,
            daysOnTargetCount = 0
        )
    )

    // Selected month for statistics
    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonthIso())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // Monthly stats
    val monthlyStats: StateFlow<MonthlyStatsSummary> = combine(
        _selectedMonth,
        _selectedMonth.flatMapLatest { ym ->
            val range = DateUtils.getMonthDateRange(ym)
            repository.getEntriesBetweenDates(range.first, range.second)
        },
        calorieTarget
    ) { ym, entries, target ->
        val daysInMonth = DateUtils.getAllDaysInMonth(ym)
        val entriesByDate = entries.groupBy { it.date }

        var totalKcalSum = 0.0
        var recordedDays = 0
        var daysOnTarget = 0

        val dailyStats = daysInMonth.mapIndexed { index, dateIso ->
            val dayEntries = entriesByDate[dateIso] ?: emptyList()
            val totalKcal = dayEntries.sumOf { it.totalCalories }
            val hasEntries = dayEntries.isNotEmpty()
            if (hasEntries) {
                totalKcalSum += totalKcal
                recordedDays++
                if (totalKcal <= target) {
                    daysOnTarget++
                }
            }
            DayStat(
                dateIso = dateIso,
                dayNumber = index + 1,
                totalCalories = totalKcal,
                targetCalories = target,
                isRecorded = hasEntries
            )
        }

        val avg = if (recordedDays > 0) totalKcalSum / recordedDays else 0.0

        MonthlyStatsSummary(
            yearMonth = ym,
            monthTitle = DateUtils.formatMonthTitle(ym),
            dailyStats = dailyStats,
            averageCalories = avg,
            recordedDaysCount = recordedDays,
            targetCalories = target,
            daysOnTargetCount = daysOnTarget
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyStatsSummary(
            yearMonth = DateUtils.getCurrentMonthIso(),
            monthTitle = DateUtils.formatMonthTitle(DateUtils.getCurrentMonthIso()),
            dailyStats = emptyList(),
            averageCalories = 0.0,
            recordedDaysCount = 0,
            targetCalories = DiaryRepository.DEFAULT_CALORIE_TARGET,
            daysOnTargetCount = 0
        )
    )

    // Navigation between days
    fun goToPreviousDay() {
        _selectedDate.value = DateUtils.shiftDays(_selectedDate.value, -1)
    }

    fun goToNextDay() {
        _selectedDate.value = DateUtils.shiftDays(_selectedDate.value, 1)
    }

    fun goToToday() {
        _selectedDate.value = DateUtils.getTodayIso()
    }

    fun setDate(date: String) {
        _selectedDate.value = date
    }

    // Navigation between weeks
    fun goToPreviousWeek() {
        _selectedWeek.value = DateUtils.shiftWeek(_selectedWeek.value, -1)
    }

    fun goToNextWeek() {
        _selectedWeek.value = DateUtils.shiftWeek(_selectedWeek.value, 1)
    }

    fun goToCurrentWeek() {
        _selectedWeek.value = DateUtils.getCurrentWeekStartIso()
    }

    // Navigation between months
    fun goToPreviousMonth() {
        _selectedMonth.value = DateUtils.shiftMonth(_selectedMonth.value, -1)
    }

    fun goToNextMonth() {
        _selectedMonth.value = DateUtils.shiftMonth(_selectedMonth.value, 1)
    }

    // Calorie Target
    fun updateCalorieTarget(newTarget: Int) {
        if (newTarget in 500..10000) {
            viewModelScope.launch {
                repository.setDailyCalorieTarget(newTarget)
            }
        }
    }

    // Search Open Food Facts
    fun searchOpenFoodFactsNow() {
        val q = searchQuery.value.trim()
        if (q.isBlank()) {
            _remoteSearchResults.value = emptyList()
            _remoteSearchError.value = null
            return
        }

        viewModelScope.launch {
            _isSearchingRemote.value = true
            _remoteSearchError.value = null
            val result = repository.searchOpenFoodFacts(
                query = q,
                country = offCountry.value,
                store = offStore.value
            )
            result.onSuccess { list ->
                _remoteSearchResults.value = list
                if (list.isEmpty()) {
                    val storeMsg = if (offStore.value.isNotBlank()) " pour l'enseigne \"${offStore.value}\"" else ""
                    _remoteSearchError.value = "Aucun aliment trouvé sur Open Food Facts pour \"$q\"$storeMsg"
                }
            }.onFailure { err ->
                _remoteSearchResults.value = emptyList()
                val detail = when (err) {
                    is java.net.UnknownHostException -> "Pas de connexion Internet (vérifiez le réseau de l'appareil)"
                    is java.net.SocketTimeoutException -> "Délai dépassé (serveur Open Food Facts lent, réessayez)"
                    else -> err.localizedMessage ?: "Erreur réseau"
                }
                _remoteSearchError.value = "Recherche en ligne indisponible : $detail"
            }
            _isSearchingRemote.value = false
        }
    }

    fun clearSearch() {
        searchQuery.value = ""
        _remoteSearchResults.value = emptyList()
        _remoteSearchError.value = null
    }

    // Portion Dialog management remembering last quantity
    fun openPortionDialogForLocal(food: FoodEntity, mealType: MealType) {
        viewModelScope.launch {
            val lastPortion = repository.getLastUsedPortion(
                foodId = food.id,
                foodName = food.name,
                foodBrand = food.brand,
                barcode = food.barcode
            )
            val lastQty = lastPortion?.quantityGrams ?: food.defaultServingGrams
            val resolvedQty = if (lastQty > 0) lastQty else (if (food.defaultServingGrams > 0) food.defaultServingGrams else 100.0)
            val resolvedUnit = lastPortion?.unit ?: food.servingUnitName
            val resolvedUnitQty = lastPortion?.unitQuantity ?: (if (resolvedUnit == "unité") 1.0 else resolvedQty)

            _activePortionDialog.value = PortionDialogState(
                foodName = food.name,
                foodBrand = food.brand,
                caloriesPer100g = food.caloriesPer100g,
                proteinsPer100g = food.proteinsPer100g,
                carbsPer100g = food.carbsPer100g,
                fatPer100g = food.fatPer100g,
                fiberPer100g = food.fiberPer100g,
                saltPer100g = food.saltPer100g,
                initialQuantityGrams = resolvedQty,
                initialUnit = resolvedUnit,
                initialUnitQuantity = resolvedUnitQty,
                defaultServingGrams = if (food.defaultServingGrams > 0) food.defaultServingGrams else 100.0,
                mealType = mealType,
                localFood = food
            )
        }
    }

    fun openPortionDialogForRemote(product: OffProductDto, mealType: MealType) {
        viewModelScope.launch {
            val name = product.getDisplayName()
            val brand = product.getCleanBrand()
            val lastPortion = repository.getLastUsedPortion(
                foodId = 0,
                foodName = name,
                foodBrand = brand,
                barcode = product.code
            )
            val parsedServing = product.parseServingGrams()
            val lastQty = lastPortion?.quantityGrams ?: parsedServing
            val resolvedQty = if (lastQty > 0) lastQty else 100.0
            val isBeverage = product.categoriesTags?.any { it.contains("beverage", ignoreCase = true) || it.contains("boisson", ignoreCase = true) } == true
            val detectedUnit = when {
                product.servingSize?.contains("ml", ignoreCase = true) == true -> "ml"
                product.servingSize?.contains("cl", ignoreCase = true) == true -> "ml"
                product.servingSize?.contains("dl", ignoreCase = true) == true -> "ml"
                isBeverage -> "ml"
                else -> "g"
            }
            val resolvedUnit = lastPortion?.unit ?: detectedUnit
            val resolvedUnitQty = lastPortion?.unitQuantity ?: (if (resolvedUnit == "unité") 1.0 else resolvedQty)

            _activePortionDialog.value = PortionDialogState(
                foodName = name,
                foodBrand = brand,
                caloriesPer100g = product.getCaloriesPer100g() ?: 0.0,
                proteinsPer100g = product.getProteinsPer100g(),
                carbsPer100g = product.getCarbsPer100g(),
                fatPer100g = product.getFatPer100g(),
                fiberPer100g = product.getFiberPer100g(),
                saltPer100g = product.getSaltPer100g(),
                initialQuantityGrams = resolvedQty,
                initialUnit = resolvedUnit,
                initialUnitQuantity = resolvedUnitQty,
                defaultServingGrams = if (parsedServing > 0) parsedServing else 100.0,
                mealType = mealType,
                remoteProduct = product
            )
        }
    }

    fun dismissPortionDialog() {
        _activePortionDialog.value = null
    }

    fun confirmPortionDialog(
        quantityGrams: Double,
        unit: String = "g",
        unitQuantity: Double = quantityGrams
    ) {
        val state = _activePortionDialog.value ?: return
        if (state.localFood != null) {
            addLocalFoodToMeal(state.mealType, state.localFood, quantityGrams, unit, unitQuantity)
        } else if (state.remoteProduct != null) {
            addRemoteProductToMeal(state.mealType, state.remoteProduct, quantityGrams, unit, unitQuantity)
        }
        _activePortionDialog.value = null
    }

    fun saveProductToHistory(product: OffProductDto) {
        viewModelScope.launch {
            repository.saveProductToLocalFoods(product)
            product.code?.let { code ->
                _savedToHistoryCodes.value = _savedToHistoryCodes.value + code
            }
        }
    }

    // Diary actions
    fun addLocalFoodToMeal(
        mealType: MealType,
        food: FoodEntity,
        quantityGrams: Double,
        unit: String = food.servingUnitName,
        unitQuantity: Double = quantityGrams
    ) {
        viewModelScope.launch {
            repository.addMealEntry(
                date = _selectedDate.value,
                mealType = mealType,
                foodName = food.name,
                foodBrand = food.brand,
                quantityGrams = quantityGrams,
                caloriesPer100g = food.caloriesPer100g,
                proteinsPer100g = food.proteinsPer100g,
                carbsPer100g = food.carbsPer100g,
                fatPer100g = food.fatPer100g,
                fiberPer100g = food.fiberPer100g,
                saltPer100g = food.saltPer100g,
                existingFoodId = food.id,
                barcode = food.barcode,
                isCustom = food.isCustom,
                defaultServingGrams = quantityGrams,
                servingUnitName = unit,
                unit = unit,
                unitQuantity = unitQuantity
            )
        }
    }

    fun addRemoteProductToMeal(
        mealType: MealType,
        product: OffProductDto,
        quantityGrams: Double,
        unit: String = "g",
        unitQuantity: Double = quantityGrams
    ) {
        val kcal100g = product.getCaloriesPer100g() ?: 0.0
        viewModelScope.launch {
            repository.addMealEntry(
                date = _selectedDate.value,
                mealType = mealType,
                foodName = product.getDisplayName(),
                foodBrand = product.getCleanBrand(),
                quantityGrams = quantityGrams,
                caloriesPer100g = kcal100g,
                proteinsPer100g = product.getProteinsPer100g(),
                carbsPer100g = product.getCarbsPer100g(),
                fatPer100g = product.getFatPer100g(),
                fiberPer100g = product.getFiberPer100g(),
                saltPer100g = product.getSaltPer100g(),
                barcode = product.code,
                isCustom = false,
                defaultServingGrams = quantityGrams,
                servingUnitName = unit,
                unit = unit,
                unitQuantity = unitQuantity
            )
        }
    }

    fun addCustomFoodToMeal(
        mealType: MealType,
        name: String,
        brand: String,
        caloriesPer100g: Double,
        quantityGrams: Double,
        proteinsPer100g: Double = 0.0,
        carbsPer100g: Double = 0.0,
        fatPer100g: Double = 0.0,
        fiberPer100g: Double = 0.0,
        saltPer100g: Double = 0.0,
        servingUnitName: String = "g",
        unit: String = servingUnitName,
        unitQuantity: Double = quantityGrams
    ) {
        viewModelScope.launch {
            repository.addMealEntry(
                date = _selectedDate.value,
                mealType = mealType,
                foodName = name.trim(),
                foodBrand = brand.trim(),
                quantityGrams = quantityGrams,
                caloriesPer100g = caloriesPer100g,
                proteinsPer100g = proteinsPer100g,
                carbsPer100g = carbsPer100g,
                fatPer100g = fatPer100g,
                fiberPer100g = fiberPer100g,
                saltPer100g = saltPer100g,
                isCustom = true,
                defaultServingGrams = quantityGrams,
                servingUnitName = servingUnitName,
                unit = unit,
                unitQuantity = unitQuantity
            )
        }
    }

    fun updateEntryQuantity(
        entry: MealEntryEntity,
        newQuantity: Double,
        unit: String = entry.unit,
        unitQuantity: Double = newQuantity
    ) {
        viewModelScope.launch {
            repository.updateEntryQuantity(entry, newQuantity, unit, unitQuantity)
        }
    }

    fun deleteEntry(entry: MealEntryEntity) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun deleteRecentFood(food: FoodEntity) {
        viewModelScope.launch {
            repository.deleteFood(food.id)
            refreshDatabaseStats()
        }
    }

    // Settings actions
    fun setOffCountry(country: String) {
        viewModelScope.launch {
            repository.setOffCountry(country)
        }
    }

    fun setOffStore(store: String) {
        viewModelScope.launch {
            repository.setOffStore(store)
        }
    }

    fun refreshDatabaseStats() {
        viewModelScope.launch {
            _databaseStats.value = repository.getDatabaseStats()
        }
    }

    fun exportBackup(appVersion: String, onComplete: (Result<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportBackupJson(appVersion)
                onComplete(Result.success(json))
            } catch (e: Exception) {
                onComplete(Result.failure(e))
            }
        }
    }

    fun importBackup(
        jsonString: String,
        overwrite: Boolean,
        onComplete: (Result<ImportResult>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.importBackupJson(jsonString, overwrite)
                refreshDatabaseStats()
                onComplete(Result.success(result))
            } catch (e: Exception) {
                onComplete(Result.failure(e))
            }
        }
    }

    // Weight tracking flows & operations
    val weightEntriesDesc: StateFlow<List<WeightEntryEntity>> = repository.getAllWeightEntriesDesc()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightEntriesAsc: StateFlow<List<WeightEntryEntity>> = repository.getAllWeightEntriesAsc()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestWeightEntry: StateFlow<WeightEntryEntity?> = repository.getLatestWeight()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val targetWeight: StateFlow<Double?> = repository.getTargetWeight()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userHeightCm: StateFlow<Int?> = repository.getUserHeightCm()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weightEvolution: StateFlow<WeightEvolution> = combine(
        weightEntriesAsc,
        targetWeight,
        userHeightCm
    ) { entries, target, height ->
        if (entries.isEmpty()) {
            WeightEvolution(
                latestWeight = null,
                startingWeight = null,
                totalDiff = null,
                diff7Days = null,
                diff30Days = null,
                minWeight = null,
                maxWeight = null,
                targetWeight = target,
                userHeightCm = height,
                bmi = null,
                entriesCount = 0
            )
        } else {
            val latest = entries.last().weightKg
            val start = entries.first().weightKg
            val totalDiff = latest - start
            val min = entries.minOf { it.weightKg }
            val max = entries.maxOf { it.weightKg }

            val todayIso = DateUtils.getTodayIso()
            val date7DaysAgo = DateUtils.shiftDays(todayIso, -7)
            val entry7Days = entries.lastOrNull { it.date <= date7DaysAgo }
            val diff7 = if (entry7Days != null) latest - entry7Days.weightKg else null

            val date30DaysAgo = DateUtils.shiftDays(todayIso, -30)
            val entry30Days = entries.lastOrNull { it.date <= date30DaysAgo }
            val diff30 = if (entry30Days != null) latest - entry30Days.weightKg else null

            val bmi = if (height != null && height > 50) {
                val heightM = height / 100.0
                latest / (heightM * heightM)
            } else null

            WeightEvolution(
                latestWeight = latest,
                startingWeight = start,
                totalDiff = totalDiff,
                diff7Days = diff7,
                diff30Days = diff30,
                minWeight = min,
                maxWeight = max,
                targetWeight = target,
                userHeightCm = height,
                bmi = bmi,
                entriesCount = entries.size
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeightEvolution(null, null, null, null, null, null, null, null, null, null, 0)
    )

    fun logWeight(date: String, weightKg: Double, note: String = "") {
        viewModelScope.launch {
            repository.logWeight(date, weightKg, note)
            refreshDatabaseStats()
        }
    }

    fun deleteWeight(id: Long) {
        viewModelScope.launch {
            repository.deleteWeight(id)
            refreshDatabaseStats()
        }
    }

    fun updateTargetWeight(targetKg: Double?) {
        viewModelScope.launch {
            repository.setTargetWeight(targetKg)
        }
    }

    fun updateUserHeight(heightCm: Int?) {
        viewModelScope.launch {
            repository.setUserHeightCm(heightCm)
        }
    }

    val collapsedMeals: StateFlow<Set<MealType>> = repository.getCollapsedMeals()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptySet()
        )

    fun toggleMealCollapsed(mealType: MealType) {
        viewModelScope.launch {
            val current = collapsedMeals.value
            val updated = if (current.contains(mealType)) {
                current - mealType
            } else {
                current + mealType
            }
            repository.setCollapsedMeals(updated)
        }
    }
}
