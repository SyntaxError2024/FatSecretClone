package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDateUtils_shiftDays() {
    val date = "2026-10-30"
    val next = com.example.util.DateUtils.shiftDays(date, 1)
    val prev = com.example.util.DateUtils.shiftDays(date, -1)
    assertEquals("2026-10-31", next)
    assertEquals("2026-10-29", prev)
  }

  @Test
  fun testCalorieCalculation() {
    val kcalPer100g = 165.0 // Poulet
    val grams = 150.0
    val total = (kcalPer100g * grams / 100.0)
    assertEquals(247.5, total, 0.01)
  }

  @Test
  fun testOpenFoodFactsParsing() {
    val product = com.example.data.remote.OffProductDto(
      code = "3760049798609",
      productName = "Pomme",
      productNameFr = "Pomme Gala",
      brands = "Bio",
      servingSize = "150g",
      nutriments = mapOf(
        "energy-kcal_100g" to 52.0,
        "energy-kcal_value" to 52,
        "proteins_100g" to 0.3,
        "carbohydrates_100g" to 13.8,
        "fat_100g" to 0.2,
        "fiber_100g" to 2.4,
        "salt_100g" to 0.01
      )
    )
    assertEquals("Pomme Gala", product.getDisplayName())
    assertEquals(52.0, product.getCaloriesPer100g() ?: 0.0, 0.01)
    assertEquals(150.0, product.parseServingGrams(), 0.01)
    assertEquals(0.3, product.getProteinsPer100g(), 0.01)
    assertEquals(13.8, product.getCarbsPer100g(), 0.01)
    assertEquals(0.2, product.getFatPer100g(), 0.01)
    assertEquals(2.4, product.getFiberPer100g(), 0.01)
    assertEquals(0.01, product.getSaltPer100g(), 0.001)
  }

  @Test
  fun testLastUsedPortionCalculation() {
    val food = com.example.data.local.FoodEntity(
      name = "Pâtes",
      caloriesPer100g = 131.0,
      proteinsPer100g = 5.0,
      defaultServingGrams = 220.0
    )
    assertEquals(220.0, food.defaultServingGrams, 0.01)
    val totalKcal = food.caloriesPer100g * food.defaultServingGrams / 100.0
    val totalProteins = food.proteinsPer100g * food.defaultServingGrams / 100.0
    assertEquals(288.2, totalKcal, 0.01)
    assertEquals(11.0, totalProteins, 0.01)
  }

  @Test
  fun testStoreSubtitle() {
    val prodWithStore = com.example.data.remote.OffProductDto(
      productName = "Yaourt",
      brands = "Carrefour Bio",
      stores = "Carrefour"
    )
    assertEquals("Carrefour Bio • Enseigne: Carrefour", prodWithStore.getSubtitle())
  }

  @Test
  fun testBackupSerializerRoundTrip() {
    val settings = listOf(
      com.example.data.local.UserSettingsEntity("daily_calorie_target", "2200"),
      com.example.data.local.UserSettingsEntity("off_search_country", "de"),
      com.example.data.local.UserSettingsEntity("off_search_store", "Aldi")
    )
    val foods = listOf(
      com.example.data.local.FoodEntity(
        id = 1,
        name = "Pain de seigle",
        brand = "Bio",
        caloriesPer100g = 210.0,
        proteinsPer100g = 6.0,
        carbsPer100g = 40.0,
        fatPer100g = 1.2,
        defaultServingGrams = 60.0
      )
    )
    val entries = listOf(
      com.example.data.local.MealEntryEntity(
        id = 10,
        date = "2026-11-01",
        mealType = com.example.data.model.MealType.BREAKFAST,
        foodId = 1,
        foodName = "Pain de seigle",
        quantityGrams = 60.0,
        caloriesPer100g = 210.0,
        totalCalories = 126.0,
        proteins = 3.6,
        carbs = 24.0,
        fat = 0.72
      )
    )
    val payload = com.example.data.model.BackupPayload(
      exportDate = "2026-11-01",
      appVersion = "1.1",
      settings = settings,
      foods = foods,
      mealEntries = entries
    )

    val json = com.example.data.model.BackupSerializer.toJson(payload)
    assertTrue(json.contains("\"version\": 3"))
    assertTrue(json.contains("\"off_search_country\""))
    assertTrue(json.contains("\"Pain de seigle\""))

    val restored = com.example.data.model.BackupSerializer.fromJson(json)
    assertEquals(3, restored.settings.size)
    assertEquals(1, restored.foods.size)
    assertEquals(1, restored.mealEntries.size)
    assertEquals("Aldi", restored.settings.first { it.key == "off_search_store" }.value)
    assertEquals("Pain de seigle", restored.foods[0].name)
    assertEquals(60.0, restored.foods[0].defaultServingGrams, 0.01)
    assertEquals(com.example.data.model.MealType.BREAKFAST, restored.mealEntries[0].mealType)

    // Test backwards compatibility with version 2 JSON
    val v2Json = json.replace("\"version\": 3", "\"version\": 2")
    val restoredV2 = com.example.data.model.BackupSerializer.fromJson(v2Json)
    assertEquals(3, restoredV2.settings.size)
    assertEquals(1, restoredV2.foods.size)
    assertEquals(0, restoredV2.weightEntries.size)
  }

  @Test
  fun testPastaTargetingAndAccentRemoval() {
    assertEquals("pates", com.example.data.repository.DiaryRepository.removeAccents("pâtes"))
    assertEquals("emince", com.example.data.repository.DiaryRepository.removeAccents("émincé"))

    assertTrue(com.example.data.repository.DiaryRepository.isQueryTargetingPasta("pâtes"))
    assertTrue(com.example.data.repository.DiaryRepository.isQueryTargetingPasta("pates"))
    assertTrue(com.example.data.repository.DiaryRepository.isQueryTargetingPasta("pasta"))
    assertFalse(com.example.data.repository.DiaryRepository.isQueryTargetingPasta("pâte à tartiner"))
    assertFalse(com.example.data.repository.DiaryRepository.isQueryTargetingPasta("pâte feuilletée"))
    assertFalse(com.example.data.repository.DiaryRepository.isQueryTargetingPasta("sauce pâtes"))

    // Verify ranking prioritizes real pasta from Carrefour
    val items = listOf(
      com.example.data.remote.OffProductDto(
        code = "1",
        productName = "Pâte à tartiner noisettes",
        brands = "Poulain",
        stores = "carrefour",
        nutriments = mapOf("energy-kcal_100g" to 530.0)
      ),
      com.example.data.remote.OffProductDto(
        code = "2",
        productName = "Penne Rigate",
        productNameFr = "Pâtes Penne Rigate",
        brands = "Barilla",
        stores = "carrefour",
        categoriesTags = listOf("en:pastas"),
        nutriments = mapOf("energy-kcal_100g" to 359.0)
      ),
      com.example.data.remote.OffProductDto(
        code = "3",
        productName = "Sauce pesto basilic pour pâtes",
        brands = "Panzani",
        stores = "carrefour",
        nutriments = mapOf("energy-kcal_100g" to 420.0)
      ),
      com.example.data.remote.OffProductDto(
        code = "4",
        productName = "Coquillettes",
        brands = "Carrefour",
        stores = "carrefour",
        categoriesTags = listOf("en:pastas"),
        nutriments = mapOf("energy-kcal_100g" to 350.0)
      )
    )

    val ranked = com.example.data.repository.DiaryRepository.rankAndFilterProducts(items, "pâtes", "carrefour")
    // Top 2 items MUST be actual pasta (Barilla Penne and Carrefour Coquillettes)
    assertEquals("Pâtes Penne Rigate", ranked[0].getDisplayName())
    assertEquals("Coquillettes", ranked[1].getDisplayName())
    // Pâte à tartiner and sauce must be ranked at the bottom
    assertEquals("Sauce pesto basilic pour pâtes", ranked[2].getDisplayName())
    assertEquals("Pâte à tartiner noisettes", ranked[3].getDisplayName())
  }

  @Test
  fun testBackupSerializerV3WithWeights() {
    val weights = listOf(
      com.example.data.local.WeightEntryEntity(
        id = 1,
        date = "2026-10-15",
        weightKg = 76.5,
        note = "Début"
      ),
      com.example.data.local.WeightEntryEntity(
        id = 2,
        date = "2026-11-01",
        weightKg = 74.2,
        note = "Après séance"
      )
    )
    val payload = com.example.data.model.BackupPayload(
      exportDate = "2026-11-02",
      appVersion = "1.2",
      settings = listOf(com.example.data.local.UserSettingsEntity("target_weight_kg", "70.0")),
      foods = emptyList(),
      mealEntries = emptyList(),
      weightEntries = weights
    )

    val json = com.example.data.model.BackupSerializer.toJson(payload)
    assertTrue(json.contains("\"version\": 3"))
    assertTrue(json.contains("\"weightEntries\""))
    assertTrue(json.contains("76.5"))

    val restored = com.example.data.model.BackupSerializer.fromJson(json)
    assertEquals(2, restored.weightEntries.size)
    assertEquals(76.5, restored.weightEntries[0].weightKg, 0.01)
    assertEquals("Après séance", restored.weightEntries[1].note)
  }

  @Test
  fun testBMICalculation() {
    val heightCm = 175
    val weightKg = 74.0
    val heightM = heightCm / 100.0
    val bmi = weightKg / (heightM * heightM)
    assertEquals(24.16, bmi, 0.05)

    val (catNormal, _) = com.example.ui.screens.getBmiCategory(24.16)
    assertEquals("Poids normal", catNormal)

    val (catOverweight, _) = com.example.ui.screens.getBmiCategory(27.5)
    assertEquals("Surpoids", catOverweight)
  }

  @Test
  fun testCollapsedMealsSerialization() {
    val collapsed = setOf(com.example.data.model.MealType.BREAKFAST, com.example.data.model.MealType.SNACK)
    val serialized = collapsed.joinToString(",") { it.name }
    assertEquals("BREAKFAST,SNACK", serialized)

    val deserialized = serialized.split(",").mapNotNull {
      try { com.example.data.model.MealType.valueOf(it.trim()) } catch (e: Exception) { null }
    }.toSet()
    assertEquals(collapsed, deserialized)
  }

  @Test
  fun testBlankProductFiltering() {
    val items = listOf(
      com.example.data.remote.OffProductDto(
        code = "1",
        productName = "   ",
        productNameFr = ""
      ),
      com.example.data.remote.OffProductDto(
        code = "2",
        productName = "Flocons d'avoine",
        nutriments = mapOf("energy-kcal_100g" to 365.0)
      )
    )
    val filtered = com.example.data.repository.DiaryRepository.rankAndFilterProducts(items, "avoine", "")
    assertEquals(1, filtered.size)
    assertEquals("Flocons d'avoine", filtered[0].getDisplayName())
  }
}
