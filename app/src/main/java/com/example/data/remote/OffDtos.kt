package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Locale
import kotlin.math.roundToInt

@JsonClass(generateAdapter = true)
data class OffSearchResponse(
    @Json(name = "products") val products: List<OffProductDto> = emptyList(),
    @Json(name = "count") val count: Any? = null
)

@JsonClass(generateAdapter = true)
data class OffProductDto(
    @Json(name = "code") val code: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_name_fr") val productNameFr: String? = null,
    @Json(name = "brands") val brands: String? = null,
    @Json(name = "stores") val stores: String? = null,
    @Json(name = "serving_size") val servingSize: String? = null,
    @Json(name = "categories_tags") val categoriesTags: List<String>? = null,
    @Json(name = "nutriments") val nutriments: Map<String, Any?>? = null
) {
    /**
     * Extracts only the clean, human-readable product name without HTML, linebreaks,
     * barcode artifacts, or all-caps shouting.
     */
    fun getDisplayName(): String {
        val raw = productNameFr?.takeIf { it.isNotBlank() }
            ?: productName?.takeIf { it.isNotBlank() }
            ?: return "Aliment sans nom"

        return cleanRawText(raw)
    }

    /**
     * Extracts only the primary, clean brand name (e.g. "Barilla" instead of
     * "Barilla, Barilla France, Barilla G. e R. Fratelli S.p.A.").
     */
    fun getCleanBrand(): String {
        val raw = brands ?: return ""
        val cleaned = cleanRawText(raw)
        if (cleaned.isBlank()) return ""

        // Split by comma / semicolon to isolate primary brand
        val parts = cleaned.split(',', ';')
        for (part in parts) {
            val candidate = part.trim()
                .removePrefix("fr:")
                .removePrefix("en:")
                .removePrefix("de:")
                .removePrefix("es:")
                .removePrefix("it:")
                .trim()
            if (candidate.isNotBlank() && !candidate.startsWith("http", ignoreCase = true)) {
                return formatCapitalization(candidate)
            }
        }
        return ""
    }

    /**
     * Subtitle with only useful information: primary brand and optional standard serving size.
     * Never dumps raw store slugs or JSON fragments.
     */
    fun getSubtitle(): String {
        val brand = getCleanBrand()
        val grams = parseServingGrams()
        val serving = if (grams > 0.0 && grams != 100.0) "Portion : ${grams.roundToInt()}g" else ""

        return when {
            brand.isNotBlank() && serving.isNotBlank() -> "$brand • $serving"
            brand.isNotBlank() -> brand
            serving.isNotBlank() -> serving
            else -> ""
        }
    }

    /**
     * Clean standard portion display (e.g. "Portion standard : 80g").
     */
    fun getCleanServingDisplay(): String? {
        val grams = parseServingGrams()
        return if (grams > 0.0 && grams != 100.0) {
            "Portion recommandée : ${grams.roundToInt()}g"
        } else null
    }

    private fun toDouble(v: Any?): Double? {
        return when (v) {
            is Number -> v.toDouble()
            is String -> {
                val s = v.trim()
                    .replace('<', ' ')
                    .replace('>', ' ')
                    .replace('~', ' ')
                    .replace("g", "", ignoreCase = true)
                    .replace("ml", "", ignoreCase = true)
                    .trim()
                    .replace(',', '.')
                s.toDoubleOrNull()
            }
            else -> null
        }
    }

    /**
     * Reliably extracts calories per 100g, converting from kJ / Joules when needed,
     * and strictly validating the range [0..950] kcal per 100g.
     */
    fun getCaloriesPer100g(): Double? {
        val n = nutriments ?: return null

        // 1. Direct kcal per 100g
        val kcal100 = toDouble(n["energy-kcal_100g"])
        if (kcal100 != null && kcal100 in 0.0..950.0) return kcal100

        val kcalVal = toDouble(n["energy-kcal_value"])
        if (kcalVal != null && kcalVal in 0.0..950.0) return kcalVal

        val kcal = toDouble(n["energy-kcal"])
        if (kcal != null && kcal in 0.0..950.0) return kcal

        // 2. Conversion from kJ per 100g (1 kcal = 4.184 kJ)
        val kj100 = toDouble(n["energy-kj_100g"]) ?: toDouble(n["energy-kj_value"]) ?: toDouble(n["energy-kj"])
        if (kj100 != null && kj100 > 0.0) {
            val converted = kj100 / 4.184
            if (converted in 0.0..950.0) return converted
        }

        // 3. Generic energy_100g field
        val energyRaw = toDouble(n["energy_100g"]) ?: toDouble(n["energy_value"]) ?: toDouble(n["energy"])
        if (energyRaw != null && energyRaw > 0.0) {
            when {
                energyRaw in 0.0..950.0 -> return energyRaw
                energyRaw in 950.0..4200.0 -> {
                    val fromKj = energyRaw / 4.184
                    if (fromKj in 0.0..950.0) return fromKj
                }
                energyRaw > 4200.0 -> {
                    val fromJ = energyRaw / 4184.0
                    if (fromJ in 0.0..950.0) return fromJ
                }
            }
        }

        return null
    }

    fun getProteinsPer100g(): Double {
        val n = nutriments ?: return 0.0
        val v = toDouble(n["proteins_100g"]) ?: toDouble(n["proteins_value"]) ?: toDouble(n["proteins"]) ?: 0.0
        return v.coerceIn(0.0, 100.0)
    }

    fun getCarbsPer100g(): Double {
        val n = nutriments ?: return 0.0
        val v = toDouble(n["carbohydrates_100g"]) ?: toDouble(n["carbohydrates_value"]) ?: toDouble(n["carbohydrates"]) ?: 0.0
        return v.coerceIn(0.0, 100.0)
    }

    fun getFatPer100g(): Double {
        val n = nutriments ?: return 0.0
        val v = toDouble(n["fat_100g"]) ?: toDouble(n["fat_value"]) ?: toDouble(n["fat"]) ?: 0.0
        return v.coerceIn(0.0, 100.0)
    }

    fun getFiberPer100g(): Double {
        val n = nutriments ?: return 0.0
        val v = toDouble(n["fiber_100g"]) ?: toDouble(n["fiber_value"]) ?: toDouble(n["fiber"]) ?: 0.0
        return v.coerceIn(0.0, 100.0)
    }

    fun getSaltPer100g(): Double {
        val n = nutriments ?: return 0.0
        val salt = toDouble(n["salt_100g"]) ?: toDouble(n["salt_value"]) ?: toDouble(n["salt"])
        if (salt != null) return salt.coerceIn(0.0, 100.0)

        // Sodium fallback: salt = sodium * 2.54
        val sodium = toDouble(n["sodium_100g"]) ?: toDouble(n["sodium_value"]) ?: toDouble(n["sodium"])
        if (sodium != null) {
            return (sodium * 2.54).coerceIn(0.0, 100.0)
        }
        return 0.0
    }

    /**
     * Parses standard serving size in grams, handling units like g, ml, cl.
     */
    fun parseServingGrams(): Double {
        val raw = servingSize ?: return 100.0
        val clean = raw.trim()

        // Match cl (e.g. 33 cl -> 330g)
        val matchCl = Regex("""(\d+(?:[.,]\d+)?)\s*cl\b""", RegexOption.IGNORE_CASE).find(clean)
        if (matchCl != null) {
            val num = matchCl.groupValues[1].replace(',', '.').toDoubleOrNull()
            if (num != null && num > 0) return (num * 10.0).coerceIn(10.0, 2000.0)
        }

        // Match g or ml (e.g. 80 g, 250ml)
        val matchGrams = Regex("""(\d+(?:[.,]\d+)?)\s*(?:g|ml)\b""", RegexOption.IGNORE_CASE).find(clean)
        if (matchGrams != null) {
            val num = matchGrams.groupValues[1].replace(',', '.').toDoubleOrNull()
            if (num != null && num > 0) return num.coerceIn(10.0, 2000.0)
        }

        // Generic number
        val matchGeneric = Regex("""\b(\d+(?:[.,]\d+)?)\b""").find(clean)
        if (matchGeneric != null) {
            val num = matchGeneric.groupValues[1].replace(',', '.').toDoubleOrNull()
            if (num != null && num in 10.0..1000.0) return num
        }

        return 100.0
    }

    companion object {
        fun cleanRawText(str: String): String {
            var text = str
                .replace(Regex("<[^>]*>"), " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&eacute;", "é")
                .replace("&egrave;", "è")
                .replace("&agrave;", "à")
                .replace("&ccedil;", "ç")
                .replace("&ecirc;", "ê")
                .replace(Regex("[\\r\\n\\t]+"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

            // Remove trailing/leading garbage punctuation
            text = text.trim(',', ';', '-', '_', '•', ' ', '.')

            return formatCapitalization(text)
        }

        fun formatCapitalization(text: String): String {
            if (text.isBlank()) return text
            // If ALL CAPS with letters, format to sentence case
            val hasLetters = text.any { it.isLetter() }
            val isAllCaps = hasLetters && text.none { it.isLowerCase() }
            return if (isAllCaps && text.length > 3) {
                text.lowercase(Locale.FRENCH).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString()
                }
            } else {
                text.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString()
                }
            }
        }
    }
}
