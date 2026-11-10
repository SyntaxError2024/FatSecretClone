package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FoodEntity::class, MealEntryEntity::class, UserSettingsEntity::class, WeightEntryEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun weightEntryDao(): WeightEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add macro & nutrient columns to foods table
                db.execSQL("ALTER TABLE foods ADD COLUMN proteinsPer100g REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE foods ADD COLUMN carbsPer100g REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE foods ADD COLUMN fatPer100g REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE foods ADD COLUMN fiberPer100g REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE foods ADD COLUMN saltPer100g REAL NOT NULL DEFAULT 0.0")

                // Add macro & nutrient columns to meal_entries table
                db.execSQL("ALTER TABLE meal_entries ADD COLUMN proteins REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meal_entries ADD COLUMN carbs REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meal_entries ADD COLUMN fat REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meal_entries ADD COLUMN fiber REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meal_entries ADD COLUMN salt REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `weight_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `weightKg` REAL NOT NULL,
                        `note` TEXT NOT NULL DEFAULT '',
                        `timestamp` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_weight_entries_date` ON `weight_entries` (`date`)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE meal_entries ADD COLUMN unit TEXT NOT NULL DEFAULT 'g'")
                db.execSQL("ALTER TABLE meal_entries ADD COLUMN unitQuantity REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fat_secret_diary.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialCommonFoods(database.foodDao(), database.settingsDao())
                    }
                }
            }
        }

        private suspend fun populateInitialCommonFoods(foodDao: FoodDao, settingsDao: SettingsDao) {
            // Default calorie target
            settingsDao.setSetting(UserSettingsEntity("daily_calorie_target", "2000"))

            // Pre-seed common reference foods with nutritional macros
            val commonFoods = listOf(
                FoodEntity(name = "Pomme", caloriesPer100g = 52.0, proteinsPer100g = 0.3, carbsPer100g = 13.8, fatPer100g = 0.2, fiberPer100g = 2.4, saltPer100g = 0.0, defaultServingGrams = 150.0, servingUnitName = "g"),
                FoodEntity(name = "Banane", caloriesPer100g = 89.0, proteinsPer100g = 1.1, carbsPer100g = 22.8, fatPer100g = 0.3, fiberPer100g = 2.6, saltPer100g = 0.0, defaultServingGrams = 120.0, servingUnitName = "g"),
                FoodEntity(name = "Blanc de poulet cuit", caloriesPer100g = 165.0, proteinsPer100g = 31.0, carbsPer100g = 0.0, fatPer100g = 3.6, fiberPer100g = 0.0, saltPer100g = 0.2, defaultServingGrams = 120.0, servingUnitName = "g"),
                FoodEntity(name = "Riz basmati cuit", caloriesPer100g = 130.0, proteinsPer100g = 2.7, carbsPer100g = 28.0, fatPer100g = 0.3, fiberPer100g = 0.4, saltPer100g = 0.0, defaultServingGrams = 150.0, servingUnitName = "g"),
                FoodEntity(name = "Pâtes cuites", caloriesPer100g = 131.0, proteinsPer100g = 5.0, carbsPer100g = 25.0, fatPer100g = 1.1, fiberPer100g = 1.8, saltPer100g = 0.0, defaultServingGrams = 180.0, servingUnitName = "g"),
                FoodEntity(name = "Oeuf entier dur", caloriesPer100g = 155.0, proteinsPer100g = 13.0, carbsPer100g = 1.1, fatPer100g = 11.0, fiberPer100g = 0.0, saltPer100g = 0.3, defaultServingGrams = 60.0, servingUnitName = "g"),
                FoodEntity(name = "Pain complet", caloriesPer100g = 247.0, proteinsPer100g = 13.0, carbsPer100g = 41.0, fatPer100g = 3.4, fiberPer100g = 7.0, saltPer100g = 1.0, defaultServingGrams = 40.0, servingUnitName = "g"),
                FoodEntity(name = "Flocons d'avoine", caloriesPer100g = 389.0, proteinsPer100g = 16.9, carbsPer100g = 66.3, fatPer100g = 6.9, fiberPer100g = 10.6, saltPer100g = 0.0, defaultServingGrams = 40.0, servingUnitName = "g"),
                FoodEntity(name = "Lait demi-écrémé", caloriesPer100g = 46.0, proteinsPer100g = 3.2, carbsPer100g = 4.8, fatPer100g = 1.5, fiberPer100g = 0.0, saltPer100g = 0.1, defaultServingGrams = 200.0, servingUnitName = "ml"),
                FoodEntity(name = "Yaourt nature", caloriesPer100g = 61.0, proteinsPer100g = 3.5, carbsPer100g = 4.7, fatPer100g = 3.3, fiberPer100g = 0.0, saltPer100g = 0.1, defaultServingGrams = 125.0, servingUnitName = "g"),
                FoodEntity(name = "Huile d'olive", caloriesPer100g = 884.0, proteinsPer100g = 0.0, carbsPer100g = 0.0, fatPer100g = 100.0, fiberPer100g = 0.0, saltPer100g = 0.0, defaultServingGrams = 10.0, servingUnitName = "g"),
                FoodEntity(name = "Amandes", caloriesPer100g = 579.0, proteinsPer100g = 21.2, carbsPer100g = 21.6, fatPer100g = 49.9, fiberPer100g = 12.5, saltPer100g = 0.0, defaultServingGrams = 30.0, servingUnitName = "g"),
                FoodEntity(name = "Café noir (sans sucre)", caloriesPer100g = 2.0, proteinsPer100g = 0.1, carbsPer100g = 0.0, fatPer100g = 0.0, fiberPer100g = 0.0, saltPer100g = 0.0, defaultServingGrams = 150.0, servingUnitName = "ml"),
                FoodEntity(name = "Chocolat noir 70%", caloriesPer100g = 598.0, proteinsPer100g = 7.8, carbsPer100g = 34.0, fatPer100g = 43.0, fiberPer100g = 10.9, saltPer100g = 0.0, defaultServingGrams = 20.0, servingUnitName = "g")
            )
            for (food in commonFoods) {
                foodDao.insertFood(food)
            }
        }
    }
}
