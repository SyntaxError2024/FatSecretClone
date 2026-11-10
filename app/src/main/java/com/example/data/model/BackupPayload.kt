package com.example.data.model

import com.example.data.local.FoodEntity
import com.example.data.local.MealEntryEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.local.WeightEntryEntity
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
    val exportDate: String,
    val appVersion: String,
    val settings: List<UserSettingsEntity>,
    val foods: List<FoodEntity>,
    val mealEntries: List<MealEntryEntity>,
    val weightEntries: List<WeightEntryEntity> = emptyList()
)

data class ImportResult(
    val settingsCount: Int,
    val foodsCount: Int,
    val entriesCount: Int,
    val weightEntriesCount: Int = 0
)

data class DatabaseStats(
    val foodsCount: Int,
    val entriesCount: Int,
    val weightEntriesCount: Int = 0
)

object BackupSerializer {
    fun toJson(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("version", 3)
        root.put("exportDate", payload.exportDate)
        root.put("appVersion", payload.appVersion)

        val settingsArray = JSONArray()
        payload.settings.forEach { s ->
            val obj = JSONObject()
            obj.put("key", s.key)
            obj.put("value", s.value)
            settingsArray.put(obj)
        }
        root.put("settings", settingsArray)

        val foodsArray = JSONArray()
        payload.foods.forEach { f ->
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("name", f.name)
            obj.put("brand", f.brand)
            obj.put("caloriesPer100g", f.caloriesPer100g)
            obj.put("proteinsPer100g", f.proteinsPer100g)
            obj.put("carbsPer100g", f.carbsPer100g)
            obj.put("fatPer100g", f.fatPer100g)
            obj.put("fiberPer100g", f.fiberPer100g)
            obj.put("saltPer100g", f.saltPer100g)
            obj.put("defaultServingGrams", f.defaultServingGrams)
            obj.put("servingUnitName", f.servingUnitName)
            if (f.barcode != null) {
                obj.put("barcode", f.barcode)
            } else {
                obj.put("barcode", JSONObject.NULL)
            }
            obj.put("isCustom", f.isCustom)
            obj.put("lastUsedTimestamp", f.lastUsedTimestamp)
            obj.put("usageCount", f.usageCount)
            foodsArray.put(obj)
        }
        root.put("foods", foodsArray)

        val entriesArray = JSONArray()
        payload.mealEntries.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("date", e.date)
            obj.put("mealType", e.mealType.name)
            obj.put("foodId", e.foodId)
            obj.put("foodName", e.foodName)
            obj.put("foodBrand", e.foodBrand)
            obj.put("quantityGrams", e.quantityGrams)
            obj.put("caloriesPer100g", e.caloriesPer100g)
            obj.put("totalCalories", e.totalCalories)
            obj.put("proteins", e.proteins)
            obj.put("carbs", e.carbs)
            obj.put("fat", e.fat)
            obj.put("fiber", e.fiber)
            obj.put("salt", e.salt)
            obj.put("timestamp", e.timestamp)
            obj.put("unit", e.unit)
            obj.put("unitQuantity", e.unitQuantity)
            entriesArray.put(obj)
        }
        root.put("mealEntries", entriesArray)

        val weightArray = JSONArray()
        payload.weightEntries.forEach { w ->
            val obj = JSONObject()
            obj.put("id", w.id)
            obj.put("date", w.date)
            obj.put("weightKg", w.weightKg)
            obj.put("note", w.note)
            obj.put("timestamp", w.timestamp)
            weightArray.put(obj)
        }
        root.put("weightEntries", weightArray)

        return root.toString(2)
    }

    fun fromJson(jsonStr: String): BackupPayload {
        val root = JSONObject(jsonStr)
        val exportDate = root.optString("exportDate", "")
        val appVersion = root.optString("appVersion", "1.1")

        val settingsList = mutableListOf<UserSettingsEntity>()
        val settingsArray = root.optJSONArray("settings") ?: JSONArray()
        for (i in 0 until settingsArray.length()) {
            val obj = settingsArray.getJSONObject(i)
            val key = obj.getString("key")
            val value = obj.getString("value")
            settingsList.add(UserSettingsEntity(key, value))
        }

        val foodsList = mutableListOf<FoodEntity>()
        val foodsArray = root.optJSONArray("foods") ?: JSONArray()
        for (i in 0 until foodsArray.length()) {
            val obj = foodsArray.getJSONObject(i)
            val barcode = if (obj.isNull("barcode")) null else obj.optString("barcode", null)
            foodsList.add(
                FoodEntity(
                    id = obj.optLong("id", 0),
                    name = obj.getString("name"),
                    brand = obj.optString("brand", ""),
                    caloriesPer100g = obj.getDouble("caloriesPer100g"),
                    proteinsPer100g = obj.optDouble("proteinsPer100g", 0.0),
                    carbsPer100g = obj.optDouble("carbsPer100g", 0.0),
                    fatPer100g = obj.optDouble("fatPer100g", 0.0),
                    fiberPer100g = obj.optDouble("fiberPer100g", 0.0),
                    saltPer100g = obj.optDouble("saltPer100g", 0.0),
                    defaultServingGrams = obj.optDouble("defaultServingGrams", 100.0),
                    servingUnitName = obj.optString("servingUnitName", "g"),
                    barcode = barcode,
                    isCustom = obj.optBoolean("isCustom", false),
                    lastUsedTimestamp = obj.optLong("lastUsedTimestamp", System.currentTimeMillis()),
                    usageCount = obj.optInt("usageCount", 1)
                )
            )
        }

        val entriesList = mutableListOf<MealEntryEntity>()
        val entriesArray = root.optJSONArray("mealEntries") ?: JSONArray()
        for (i in 0 until entriesArray.length()) {
            val obj = entriesArray.getJSONObject(i)
            val mealTypeName = obj.optString("mealType", "SNACK")
            val mealType = try {
                MealType.valueOf(mealTypeName)
            } catch (_: Exception) {
                MealType.SNACK
            }
            val cal100g = obj.getDouble("caloriesPer100g")
            val qty = obj.getDouble("quantityGrams")
            entriesList.add(
                MealEntryEntity(
                    id = obj.optLong("id", 0),
                    date = obj.getString("date"),
                    mealType = mealType,
                    foodId = obj.optLong("foodId", 0),
                    foodName = obj.getString("foodName"),
                    foodBrand = obj.optString("foodBrand", ""),
                    quantityGrams = qty,
                    caloriesPer100g = cal100g,
                    totalCalories = obj.optDouble("totalCalories", cal100g * qty / 100.0),
                    proteins = obj.optDouble("proteins", 0.0),
                    carbs = obj.optDouble("carbs", 0.0),
                    fat = obj.optDouble("fat", 0.0),
                    fiber = obj.optDouble("fiber", 0.0),
                    salt = obj.optDouble("salt", 0.0),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    unit = obj.optString("unit", "g"),
                    unitQuantity = obj.optDouble("unitQuantity", qty)
                )
            )
        }

        val weightList = mutableListOf<WeightEntryEntity>()
        val weightArray = root.optJSONArray("weightEntries") ?: JSONArray()
        for (i in 0 until weightArray.length()) {
            val obj = weightArray.getJSONObject(i)
            weightList.add(
                WeightEntryEntity(
                    id = obj.optLong("id", 0),
                    date = obj.getString("date"),
                    weightKg = obj.getDouble("weightKg"),
                    note = obj.optString("note", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }

        return BackupPayload(
            exportDate = exportDate,
            appVersion = appVersion,
            settings = settingsList,
            foods = foodsList,
            mealEntries = entriesList,
            weightEntries = weightList
        )
    }
}
