package com.example.data.repository

import com.example.data.local.FoodDao
import com.example.data.local.FoodEntity
import com.example.data.local.MealEntryDao
import com.example.data.local.MealEntryEntity
import com.example.data.local.SettingsDao
import com.example.data.local.UserSettingsEntity
import com.example.data.model.BackupPayload
import com.example.data.model.BackupSerializer
import com.example.data.model.DatabaseStats
import com.example.data.model.ImportResult
import com.example.data.model.MealType
import com.example.data.remote.NetworkClient
import com.example.data.remote.OffProductDto
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.data.local.WeightEntryDao
import com.example.data.local.WeightEntryEntity
import kotlinx.coroutines.withContext

data class LastUsedPortion(
    val quantityGrams: Double,
    val unit: String = "g",
    val unitQuantity: Double = quantityGrams
)

class DiaryRepository(
    private val foodDao: FoodDao,
    private val mealEntryDao: MealEntryDao,
    private val settingsDao: SettingsDao,
    private val weightEntryDao: WeightEntryDao? = null
) {
    companion object {
        const val KEY_DAILY_CALORIE_TARGET = "daily_calorie_target"
        const val KEY_OFF_COUNTRY = "off_search_country"
        const val KEY_OFF_STORE = "off_search_store"
        const val KEY_TARGET_WEIGHT = "target_weight_kg"
        const val KEY_USER_HEIGHT_CM = "user_height_cm"
        const val KEY_COLLAPSED_MEALS = "collapsed_meals"
        const val DEFAULT_CALORIE_TARGET = 2000
        const val DEFAULT_OFF_COUNTRY = "fr"
        const val DEFAULT_OFF_STORE = ""

        fun isQueryTargetingPasta(query: String): Boolean {
            val q = query.lowercase().trim()
            val hasPastaToken = q.contains("pate") || q.contains("pâte") || q.contains("pasta")
            val isNonPasta = q.contains("tartiner") || q.contains("feuillet") || q.contains("brise") ||
                    q.contains("brisé") || q.contains("sable") || q.contains("sablé") ||
                    q.contains("pain") || q.contains("gateau") || q.contains("gâteau") ||
                    q.contains("pâté") || q.contains("sauce") || q.contains("confiture") ||
                    q.contains("puree") || q.contains("purée") || q.contains("fromage")
            return hasPastaToken && !isNonPasta
        }

        fun removeAccents(str: String): String {
            val normalized = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
            return Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalized, "")
        }

        fun rankAndFilterProducts(
            products: List<OffProductDto>,
            query: String,
            store: String?
        ): List<OffProductDto> {
            val valid = products.filter {
                val name = it.getDisplayName().trim()
                it.getCaloriesPer100g() != null && name.isNotBlank() && name != "Aliment sans nom"
            }

            val seenCodes = mutableSetOf<String>()
            val seenSignatures = mutableSetOf<String>()
            val uniqueProducts = mutableListOf<OffProductDto>()

            for (p in valid) {
                val code = p.code?.trim()
                val sig = (p.getDisplayName().lowercase().trim() + "|" + (p.brands ?: "").lowercase().trim())
                if (!code.isNullOrBlank()) {
                    if (seenCodes.add(code)) {
                        seenSignatures.add(sig)
                        uniqueProducts.add(p)
                    }
                } else if (seenSignatures.add(sig)) {
                    uniqueProducts.add(p)
                }
            }

            val qLower = query.lowercase().trim()
            val qClean = removeAccents(qLower)
            val isPastaTarget = isQueryTargetingPasta(query)
            val storeLower = store?.lowercase()?.trim()

            val pastaKeywords = listOf(
                "pate", "pates", "pâte", "pâtes", "spaghetti", "coquillette", "coquillettes",
                "penne", "tagliatelle", "farfalle", "fusilli", "tortellini", "ravioli",
                "macaroni", "lasagne", "gnocchi", "nouille", "nouilles", "ramen"
            )
            val offTopicPateWords = listOf(
                "tartiner", "purée", "puree", "confiture", "sauce", "fromage",
                "salade", "feuillet", "brisé", "brise", "sablé", "sable", "pâté", "pate de"
            )

            return uniqueProducts.sortedByDescending { p ->
                var score = 0
                val name = p.getDisplayName().lowercase()
                val nameClean = removeAccents(name)
                val cats = p.categoriesTags?.map { it.lowercase() } ?: emptyList()
                val brand = (p.brands ?: "").lowercase()
                val pStores = (p.stores ?: "").lowercase()

                if (isPastaTarget) {
                    if (offTopicPateWords.any { nameClean.contains(it) || name.contains(it) }) {
                        score -= 150
                    }
                    if (pastaKeywords.any { nameClean.contains(it) || name.contains(it) }) {
                        score += 120
                    }
                    if (cats.any { it.contains("pasta") || it.contains("pate") || it.contains("pâte") }) {
                        score += 80
                    }
                }

                // Title relevance
                when {
                    nameClean == qClean || name == qLower -> score += 100
                    nameClean.startsWith(qClean) || name.startsWith(qLower) -> score += 60
                    " $qClean " in " $nameClean " || " $qLower " in " $name " -> score += 40
                    nameClean.contains(qClean) || name.contains(qLower) -> score += 25
                }

                // Brand match
                if (brand.contains(qClean) || brand.contains(qLower)) {
                    score += 20
                }

                // Store preference
                if (!storeLower.isNullOrBlank()) {
                    if (pStores.contains(storeLower)) {
                        score += 35
                    }
                    if (brand.contains(storeLower)) {
                        score += 30
                    }
                }

                score
            }
        }
    }

    // Daily meal entries
    fun getEntriesForDate(date: String): Flow<List<MealEntryEntity>> {
        return mealEntryDao.getEntriesForDate(date)
    }

    // Date range entries for monthly statistics
    fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<MealEntryEntity>> {
        return mealEntryDao.getEntriesBetweenDates(startDate, endDate)
    }

    // Calorie target flow
    fun getDailyCalorieTarget(): Flow<Int> {
        return settingsDao.getSetting(KEY_DAILY_CALORIE_TARGET).map { value ->
            value?.toIntOrNull() ?: DEFAULT_CALORIE_TARGET
        }
    }

    suspend fun setDailyCalorieTarget(target: Int) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(UserSettingsEntity(KEY_DAILY_CALORIE_TARGET, target.toString()))
    }

    // Open Food Facts Country setting
    fun getOffCountry(): Flow<String> {
        return settingsDao.getSetting(KEY_OFF_COUNTRY).map { it ?: DEFAULT_OFF_COUNTRY }
    }

    suspend fun setOffCountry(country: String) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(UserSettingsEntity(KEY_OFF_COUNTRY, country.trim().lowercase()))
    }

    // Open Food Facts Store / Enseigne setting
    fun getOffStore(): Flow<String> {
        return settingsDao.getSetting(KEY_OFF_STORE).map { it ?: DEFAULT_OFF_STORE }
    }

    suspend fun setOffStore(store: String) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(UserSettingsEntity(KEY_OFF_STORE, store.trim()))
    }

    // Local foods search & recent history
    fun searchLocalFoods(query: String): Flow<List<FoodEntity>> {
        return foodDao.searchFoods(query)
    }

    fun getRecentFoods(limit: Int = 30): Flow<List<FoodEntity>> {
        return foodDao.getRecentFoods(limit)
    }

    // Remote search on Open Food Facts with configured country & store filter
    suspend fun searchOpenFoodFacts(
        query: String,
        country: String? = null,
        store: String? = null
    ): Result<List<OffProductDto>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext Result.success(emptyList())
        val trimmed = query.trim()
        val activeCountry = (country ?: settingsDao.getSettingValue(KEY_OFF_COUNTRY) ?: DEFAULT_OFF_COUNTRY).lowercase().trim()
        val activeStore = (store ?: settingsDao.getSettingValue(KEY_OFF_STORE) ?: DEFAULT_OFF_STORE).trim()

        val canonicalCountry = when (activeCountry) {
            "fr", "france" -> "en:france"
            "de", "allemagne", "germany" -> "en:germany"
            "be", "belgique", "belgium" -> "en:belgium"
            "ch", "suisse", "switzerland" -> "en:switzerland"
            "es", "espagne", "spain" -> "en:spain"
            "it", "italie", "italy" -> "en:italy"
            "uk", "gb", "royaume-uni", "united kingdom", "united-kingdom" -> "en:united-kingdom"
            "world", "monde", "" -> null
            else -> "en:$activeCountry"
        }

        val endpointUrl = when (activeCountry) {
            "fr", "france" -> "https://fr.openfoodfacts.org/cgi/search.pl"
            "de", "allemagne", "germany" -> "https://de.openfoodfacts.org/cgi/search.pl"
            "be", "belgique", "belgium" -> "https://be.openfoodfacts.org/cgi/search.pl"
            "ch", "suisse", "switzerland" -> "https://ch.openfoodfacts.org/cgi/search.pl"
            "es", "espagne", "spain" -> "https://es.openfoodfacts.org/cgi/search.pl"
            "it", "italie", "italy" -> "https://it.openfoodfacts.org/cgi/search.pl"
            "uk", "royaume-uni" -> "https://uk.openfoodfacts.org/cgi/search.pl"
            else -> "https://world.openfoodfacts.org/cgi/search.pl"
        }

        val cleanStore = activeStore.lowercase().trim().takeIf {
            it.isNotBlank() && it != "toutes les enseignes" && it != "toutes"
        }

        // Unaccented search term to prevent Open Food Facts Perl 503 encoding errors
        val searchClean = removeAccents(trimmed)

        val t0Type = if (cleanStore != null) "stores" else if (canonicalCountry != null) "countries" else null
        val t0Contains = if (t0Type != null) "contains" else null
        val t0Val = cleanStore ?: canonicalCountry

        val t1Type = if (cleanStore != null && canonicalCountry != null) "countries" else null
        val t1Contains = if (t1Type != null) "contains" else null
        val t1Val = if (cleanStore != null && canonicalCountry != null) canonicalCountry else null

        val collectedProducts = mutableListOf<OffProductDto>()

        try {
            // 1. Primary text search with store and country tags
            val response = NetworkClient.api.searchProductsWithUrl(
                url = endpointUrl,
                searchTerms = searchClean,
                tagtype0 = t0Type,
                tagContains0 = t0Contains,
                tag0 = t0Val,
                tagtype1 = t1Type,
                tagContains1 = t1Contains,
                tag1 = t1Val
            )
            collectedProducts.addAll(response.products)

            // 2. High-precision category query for pasta if search term is "pâtes" / "pates" / "pasta"
            val isPastaSearch = isQueryTargetingPasta(trimmed)
            if (isPastaSearch) {
                try {
                    val pastaCategoryResponse = NetworkClient.api.searchProductsWithUrl(
                        url = endpointUrl,
                        tagtype0 = "categories",
                        tagContains0 = "contains",
                        tag0 = "en:pastas",
                        tagtype1 = if (cleanStore != null) "stores" else if (canonicalCountry != null) "countries" else null,
                        tagContains1 = if (cleanStore != null || canonicalCountry != null) "contains" else null,
                        tag1 = cleanStore ?: canonicalCountry,
                        tagtype2 = if (cleanStore != null && canonicalCountry != null) "countries" else null,
                        tagContains2 = if (cleanStore != null && canonicalCountry != null) "contains" else null,
                        tag2 = if (cleanStore != null && canonicalCountry != null) canonicalCountry else null
                    )
                    collectedProducts.addAll(pastaCategoryResponse.products)
                } catch (eCat: Exception) {
                    android.util.Log.w("OFF_SEARCH", "Category query fallback ignored: ${eCat.message}")
                }
            }

            // 3. Fallback if 0 results and store was specified
            if (collectedProducts.isEmpty() && cleanStore != null) {
                val fallbackResponse = NetworkClient.api.searchProductsWithUrl(
                    url = endpointUrl,
                    searchTerms = "$searchClean $cleanStore",
                    tagtype0 = if (canonicalCountry != null) "countries" else null,
                    tagContains0 = if (canonicalCountry != null) "contains" else null,
                    tag0 = canonicalCountry
                )
                collectedProducts.addAll(fallbackResponse.products)
            }

            val ranked = rankAndFilterProducts(collectedProducts, trimmed, cleanStore)
            Result.success(ranked)
        } catch (e: Exception) {
            android.util.Log.w("OFF_SEARCH", "Search failed on $endpointUrl: ${e.message}, attempting world fallback")
            try {
                val worldUrl = "https://world.openfoodfacts.org/cgi/search.pl"
                val fallbackResponse = NetworkClient.api.searchProductsWithUrl(
                    url = worldUrl,
                    searchTerms = if (cleanStore != null) "$searchClean $cleanStore" else searchClean,
                    tagtype0 = if (canonicalCountry != null) "countries" else null,
                    tagContains0 = if (canonicalCountry != null) "contains" else null,
                    tag0 = canonicalCountry
                )
                val ranked = rankAndFilterProducts(fallbackResponse.products, trimmed, cleanStore)
                Result.success(ranked)
            } catch (e2: Exception) {
                android.util.Log.e("OFF_SEARCH", "World fallback also failed: ${e2.message}", e2)
                Result.failure(e2)
            }
        }
    }

    // Add entry to diary with automatic local caching
    suspend fun addMealEntry(
        date: String,
        mealType: MealType,
        foodName: String,
        foodBrand: String,
        quantityGrams: Double,
        caloriesPer100g: Double,
        proteinsPer100g: Double = 0.0,
        carbsPer100g: Double = 0.0,
        fatPer100g: Double = 0.0,
        fiberPer100g: Double = 0.0,
        saltPer100g: Double = 0.0,
        existingFoodId: Long? = null,
        barcode: String? = null,
        isCustom: Boolean = false,
        defaultServingGrams: Double = 100.0,
        servingUnitName: String = "g",
        unit: String = servingUnitName,
        unitQuantity: Double = quantityGrams
    ): Long = withContext(Dispatchers.IO) {
        val resolvedFoodId: Long = if (existingFoodId != null && existingFoodId > 0) {
            foodDao.recordFoodUsage(existingFoodId, quantityGrams)
            existingFoodId
        } else {
            // Check if already in cache by name + brand
            val existing = foodDao.findByNameAndBrand(foodName, foodBrand)
            if (existing != null) {
                foodDao.recordFoodUsage(existing.id, quantityGrams)
                existing.id
            } else {
                // Cache into local database with the chosen portion as default
                val newFood = FoodEntity(
                    name = foodName.trim(),
                    brand = foodBrand.trim(),
                    caloriesPer100g = caloriesPer100g,
                    proteinsPer100g = proteinsPer100g,
                    carbsPer100g = carbsPer100g,
                    fatPer100g = fatPer100g,
                    fiberPer100g = fiberPer100g,
                    saltPer100g = saltPer100g,
                    defaultServingGrams = quantityGrams, // Remember user's chosen quantity
                    servingUnitName = unit,
                    barcode = barcode,
                    isCustom = isCustom,
                    lastUsedTimestamp = System.currentTimeMillis(),
                    usageCount = 1
                )
                foodDao.insertFood(newFood)
            }
        }

        val totalCalories = (caloriesPer100g * quantityGrams / 100.0)
        val totalProteins = (proteinsPer100g * quantityGrams / 100.0)
        val totalCarbs = (carbsPer100g * quantityGrams / 100.0)
        val totalFat = (fatPer100g * quantityGrams / 100.0)
        val totalFiber = (fiberPer100g * quantityGrams / 100.0)
        val totalSalt = (saltPer100g * quantityGrams / 100.0)

        val entry = MealEntryEntity(
            date = date,
            mealType = mealType,
            foodId = resolvedFoodId,
            foodName = foodName.trim(),
            foodBrand = foodBrand.trim(),
            quantityGrams = quantityGrams,
            caloriesPer100g = caloriesPer100g,
            totalCalories = totalCalories,
            proteins = totalProteins,
            carbs = totalCarbs,
            fat = totalFat,
            fiber = totalFiber,
            salt = totalSalt,
            timestamp = System.currentTimeMillis(),
            unit = unit,
            unitQuantity = unitQuantity
        )
        mealEntryDao.insertEntry(entry)
    }

    suspend fun updateEntryQuantity(
        entry: MealEntryEntity,
        newQuantityGrams: Double,
        unit: String = entry.unit,
        unitQuantity: Double = newQuantityGrams
    ) = withContext(Dispatchers.IO) {
        val ratio = if (entry.quantityGrams > 0) newQuantityGrams / entry.quantityGrams else 1.0
        val updated = entry.copy(
            quantityGrams = newQuantityGrams,
            totalCalories = (entry.caloriesPer100g * newQuantityGrams / 100.0),
            proteins = entry.proteins * ratio,
            carbs = entry.carbs * ratio,
            fat = entry.fat * ratio,
            fiber = entry.fiber * ratio,
            salt = entry.salt * ratio,
            unit = unit,
            unitQuantity = unitQuantity
        )
        mealEntryDao.updateEntry(updated)
        if (entry.foodId > 0) {
            foodDao.updateLastQuantity(entry.foodId, newQuantityGrams)
        }
    }

    suspend fun getLastUsedPortion(
        foodId: Long = 0,
        foodName: String = "",
        foodBrand: String = "",
        barcode: String? = null
    ): LastUsedPortion? = withContext(Dispatchers.IO) {
        // 1. Try finding by food in meal entries
        val lastEntry = mealEntryDao.getLastEntryForFood(foodId, foodName.trim(), foodBrand.trim())
        if (lastEntry != null && lastEntry.quantityGrams > 0) {
            return@withContext LastUsedPortion(
                quantityGrams = lastEntry.quantityGrams,
                unit = lastEntry.unit,
                unitQuantity = if (lastEntry.unitQuantity > 0) lastEntry.unitQuantity else lastEntry.quantityGrams
            )
        }
        // 2. Try finding by barcode in cached foods
        if (!barcode.isNullOrBlank()) {
            val cached = foodDao.findByBarcode(barcode)
            if (cached != null && cached.defaultServingGrams > 0) {
                return@withContext LastUsedPortion(
                    quantityGrams = cached.defaultServingGrams,
                    unit = cached.servingUnitName,
                    unitQuantity = if (cached.servingUnitName == "unité") 1.0 else cached.defaultServingGrams
                )
            }
        }
        // 3. Try finding by foodName and foodBrand in foods table
        val cachedByName = foodDao.findByNameAndBrand(foodName.trim(), foodBrand.trim())
        if (cachedByName != null && cachedByName.defaultServingGrams > 0) {
            return@withContext LastUsedPortion(
                quantityGrams = cachedByName.defaultServingGrams,
                unit = cachedByName.servingUnitName,
                unitQuantity = if (cachedByName.servingUnitName == "unité") 1.0 else cachedByName.defaultServingGrams
            )
        }
        null
    }

    suspend fun getLastUsedQuantity(
        foodId: Long = 0,
        foodName: String = "",
        foodBrand: String = "",
        barcode: String? = null
    ): Double? = getLastUsedPortion(foodId, foodName, foodBrand, barcode)?.quantityGrams


    suspend fun syncLastQuantities() = withContext(Dispatchers.IO) {
        try {
            foodDao.syncLastQuantitiesFromMealHistory()
        } catch (e: Exception) {
            android.util.Log.w("DiaryRepository", "Could not sync last quantities from history: ${e.message}")
        }
    }

    suspend fun deleteEntry(entry: MealEntryEntity) = withContext(Dispatchers.IO) {
        mealEntryDao.deleteEntry(entry)
    }

    suspend fun deleteEntryById(id: Long) = withContext(Dispatchers.IO) {
        mealEntryDao.deleteEntryById(id)
    }

    suspend fun deleteFood(id: Long) = withContext(Dispatchers.IO) {
        foodDao.deleteFood(id)
    }

    suspend fun saveProductToLocalFoods(product: OffProductDto): Long = withContext(Dispatchers.IO) {
        val name = product.getDisplayName().trim()
        val brand = product.getCleanBrand().trim()
        val kcal = product.getCaloriesPer100g() ?: 0.0
        val prot = product.getProteinsPer100g()
        val carbs = product.getCarbsPer100g()
        val fat = product.getFatPer100g()
        val fiber = product.getFiberPer100g()
        val salt = product.getSaltPer100g()
        val servingGrams = product.parseServingGrams()

        val existing = foodDao.findByNameAndBrand(name, brand)
        if (existing != null) {
            foodDao.recordFoodUsage(existing.id, servingGrams)
            existing.id
        } else {
            val newFood = FoodEntity(
                name = name,
                brand = brand,
                caloriesPer100g = kcal,
                proteinsPer100g = prot,
                carbsPer100g = carbs,
                fatPer100g = fat,
                fiberPer100g = fiber,
                saltPer100g = salt,
                defaultServingGrams = servingGrams,
                barcode = product.code,
                isCustom = false,
                lastUsedTimestamp = System.currentTimeMillis(),
                usageCount = 1
            )
            foodDao.insertFood(newFood)
        }
    }

    suspend fun saveFoodToHistory(food: FoodEntity): Long = withContext(Dispatchers.IO) {
        val existing = foodDao.findByNameAndBrand(food.name.trim(), food.brand.trim())
        if (existing != null) {
            foodDao.recordFoodUsage(existing.id, food.defaultServingGrams)
            existing.id
        } else {
            val toInsert = food.copy(
                id = 0,
                lastUsedTimestamp = System.currentTimeMillis(),
                usageCount = 1
            )
            foodDao.insertFood(toInsert)
        }
    }

    // Weight tracking flows & operations
    fun getAllWeightEntriesDesc(): Flow<List<WeightEntryEntity>> {
        return weightEntryDao?.getAllEntriesDesc() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    fun getAllWeightEntriesAsc(): Flow<List<WeightEntryEntity>> {
        return weightEntryDao?.getAllEntriesAsc() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    fun getLatestWeight(): Flow<WeightEntryEntity?> {
        return weightEntryDao?.getLatestEntry() ?: kotlinx.coroutines.flow.flowOf(null)
    }

    fun getTargetWeight(): Flow<Double?> {
        return settingsDao.getSetting(KEY_TARGET_WEIGHT).map { it?.toDoubleOrNull() }
    }

    suspend fun setTargetWeight(targetKg: Double?) = withContext(Dispatchers.IO) {
        if (targetKg != null && targetKg > 0) {
            settingsDao.setSetting(UserSettingsEntity(KEY_TARGET_WEIGHT, targetKg.toString()))
        } else {
            settingsDao.setSetting(UserSettingsEntity(KEY_TARGET_WEIGHT, ""))
        }
    }

    fun getUserHeightCm(): Flow<Int?> {
        return settingsDao.getSetting(KEY_USER_HEIGHT_CM).map { it?.toIntOrNull() }
    }

    suspend fun setUserHeightCm(heightCm: Int?) = withContext(Dispatchers.IO) {
        if (heightCm != null && heightCm > 0) {
            settingsDao.setSetting(UserSettingsEntity(KEY_USER_HEIGHT_CM, heightCm.toString()))
        } else {
            settingsDao.setSetting(UserSettingsEntity(KEY_USER_HEIGHT_CM, ""))
        }
    }

    fun getCollapsedMeals(): Flow<Set<MealType>> {
        return settingsDao.getSetting(KEY_COLLAPSED_MEALS).map { raw ->
            if (raw.isNullOrBlank()) {
                emptySet()
            } else {
                raw.split(",")
                    .mapNotNull { name ->
                        try {
                            com.example.data.model.MealType.valueOf(name.trim())
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .toSet()
            }
        }
    }

    suspend fun setCollapsedMeals(collapsed: Set<com.example.data.model.MealType>) = withContext(Dispatchers.IO) {
        val str = collapsed.joinToString(",") { it.name }
        settingsDao.setSetting(UserSettingsEntity(KEY_COLLAPSED_MEALS, str))
    }

    suspend fun logWeight(date: String, weightKg: Double, note: String = "") = withContext(Dispatchers.IO) {
        val entry = WeightEntryEntity(
            date = date,
            weightKg = weightKg,
            note = note.trim(),
            timestamp = System.currentTimeMillis()
        )
        weightEntryDao?.insertOrUpdate(entry)
    }

    suspend fun deleteWeight(id: Long) = withContext(Dispatchers.IO) {
        weightEntryDao?.deleteEntryById(id)
    }

    suspend fun getWeightForDate(date: String): WeightEntryEntity? = withContext(Dispatchers.IO) {
        weightEntryDao?.getEntryForDate(date)
    }

    suspend fun exportBackupJson(appVersion: String): String = withContext(Dispatchers.IO) {
        val settings = settingsDao.getAllSettingsSync()
        val foods = foodDao.getAllFoodsSync()
        val entries = mealEntryDao.getAllEntriesSync()
        val weights = weightEntryDao?.getAllEntriesSync() ?: emptyList()
        val payload = BackupPayload(
            exportDate = DateUtils.getTodayIso(),
            appVersion = appVersion,
            settings = settings,
            foods = foods,
            mealEntries = entries,
            weightEntries = weights
        )
        BackupSerializer.toJson(payload)
    }

    suspend fun importBackupJson(
        jsonString: String,
        overwrite: Boolean = false
    ): ImportResult = withContext(Dispatchers.IO) {
        val payload = BackupSerializer.fromJson(jsonString)
        if (overwrite) {
            mealEntryDao.clearAllEntries()
            foodDao.clearAllFoods()
            settingsDao.clearAllSettings()
            weightEntryDao?.clearAll()
        }
        settingsDao.insertSettings(payload.settings)
        foodDao.insertFoods(payload.foods)
        mealEntryDao.insertEntries(payload.mealEntries)
        if (payload.weightEntries.isNotEmpty()) {
            weightEntryDao?.insertAll(payload.weightEntries)
        }

        ImportResult(
            settingsCount = payload.settings.size,
            foodsCount = payload.foods.size,
            entriesCount = payload.mealEntries.size,
            weightEntriesCount = payload.weightEntries.size
        )
    }

    suspend fun getDatabaseStats(): DatabaseStats = withContext(Dispatchers.IO) {
        DatabaseStats(
            foodsCount = foodDao.getFoodsCount(),
            entriesCount = mealEntryDao.getEntriesCount(),
            weightEntriesCount = weightEntryDao?.getCount() ?: 0
        )
    }
}
