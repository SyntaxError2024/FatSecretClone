package com.example.data.ciqual

import com.example.data.local.FoodEntity

data class CiqualFood(
    val id: String,
    val name: String,
    val category: String,
    val caloriesPer100g: Double,
    val proteinsPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val fiberPer100g: Double = 0.0,
    val saltPer100g: Double = 0.0,
    val defaultServingGrams: Double = 100.0,
    val servingUnitName: String = "g", // "g", "ml", "unité"
    val defaultServingDescription: String = "100g"
) {
    fun toFoodEntity(): FoodEntity {
        return FoodEntity(
            name = name,
            brand = "Aliment de base (CIQUAL)",
            caloriesPer100g = caloriesPer100g,
            proteinsPer100g = proteinsPer100g,
            carbsPer100g = carbsPer100g,
            fatPer100g = fatPer100g,
            fiberPer100g = fiberPer100g,
            saltPer100g = saltPer100g,
            defaultServingGrams = defaultServingGrams,
            servingUnitName = servingUnitName,
            isCustom = false
        )
    }
}

object CiqualGenericFoods {

    val CATEGORIES = listOf(
        "Tous",
        "Fruits",
        "Légumes",
        "Féculents & Pains",
        "Viandes & Volailles",
        "Poissons & Fruits de mer",
        "Oeufs",
        "Laitages & Végétaux",
        "Matières grasses",
        "Douceurs & Boissons"
    )

    val ALL_FOODS: List<CiqualFood> = listOf(
        // === FRUITS ===
        CiqualFood("fr_pomme", "Pomme (crue avec peau)", "Fruits", 52.0, 0.3, 13.8, 0.2, 2.4, 0.0, 150.0, "unité", "1 pomme moyenne (~150g)"),
        CiqualFood("fr_banane", "Banane (fraîche)", "Fruits", 89.0, 1.1, 22.8, 0.3, 2.6, 0.0, 120.0, "unité", "1 banane moyenne (~120g)"),
        CiqualFood("fr_orange", "Orange (fraîche)", "Fruits", 47.0, 0.9, 11.8, 0.1, 2.4, 0.0, 130.0, "unité", "1 orange (~130g)"),
        CiqualFood("fr_clementine", "Clémentine / Mandarine", "Fruits", 47.0, 0.8, 12.0, 0.2, 1.8, 0.0, 70.0, "unité", "1 clémentine (~70g)"),
        CiqualFood("fr_fraise", "Fraises (fraîches)", "Fruits", 32.0, 0.7, 7.7, 0.3, 2.0, 0.0, 150.0, "g", "1 barquette (~150g)"),
        CiqualFood("fr_poire", "Poire (fraîche)", "Fruits", 57.0, 0.4, 15.2, 0.1, 3.1, 0.0, 160.0, "unité", "1 poire (~160g)"),
        CiqualFood("fr_raisin", "Raisin (blanc ou noir)", "Fruits", 69.0, 0.7, 18.1, 0.2, 0.9, 0.0, 120.0, "g", "1 petite grappe (~120g)"),
        CiqualFood("fr_peche", "Pêche / Nectarine", "Fruits", 39.0, 0.9, 9.5, 0.3, 1.5, 0.0, 130.0, "unité", "1 pêche (~130g)"),
        CiqualFood("fr_abricot", "Abricot (frais)", "Fruits", 48.0, 1.4, 11.1, 0.4, 2.0, 0.0, 45.0, "unité", "1 abricot (~45g)"),
        CiqualFood("fr_kiwi", "Kiwi", "Fruits", 61.0, 1.1, 14.7, 0.5, 3.0, 0.0, 100.0, "unité", "1 kiwi (~100g)"),
        CiqualFood("fr_ananas", "Ananas frais", "Fruits", 50.0, 0.5, 13.1, 0.1, 1.4, 0.0, 150.0, "g", "1 tranche (~150g)"),
        CiqualFood("fr_melon", "Melon (chair)", "Fruits", 34.0, 0.8, 8.2, 0.2, 0.9, 0.0, 200.0, "g", "1/4 de melon (~200g)"),
        CiqualFood("fr_pasteque", "Pastèque (chair)", "Fruits", 30.0, 0.6, 7.6, 0.2, 0.4, 0.0, 250.0, "g", "1 tranche (~250g)"),
        CiqualFood("fr_myrtille", "Myrtilles fraîches", "Fruits", 57.0, 0.7, 14.5, 0.3, 2.4, 0.0, 125.0, "g", "1 barquette (~125g)"),
        CiqualFood("fr_framboise", "Framboises fraîches", "Fruits", 52.0, 1.2, 11.9, 0.7, 6.5, 0.0, 125.0, "g", "1 barquette (~125g)"),
        CiqualFood("fr_mangue", "Mangue (chair)", "Fruits", 60.0, 0.8, 15.0, 0.4, 1.6, 0.0, 200.0, "g", "1/2 mangue (~200g)"),
        CiqualFood("fr_avocat", "Avocat (chair)", "Fruits", 160.0, 2.0, 8.5, 14.7, 6.7, 0.0, 150.0, "unité", "1 avocat moyen (~150g)"),
        CiqualFood("fr_citron", "Jus de citron pressé", "Fruits", 29.0, 1.1, 9.3, 0.3, 0.3, 0.0, 50.0, "ml", "1 citron pressé (~50ml)"),

        // === LÉGUMES ===
        CiqualFood("leg_tomate", "Tomate crue", "Légumes", 18.0, 0.9, 3.9, 0.2, 1.2, 0.0, 120.0, "unité", "1 tomate moyenne (~120g)"),
        CiqualFood("leg_tomate_cerise", "Tomates cerises", "Légumes", 18.0, 0.9, 3.9, 0.2, 1.2, 0.0, 100.0, "g", "1 poignée (~100g)"),
        CiqualFood("leg_concombre", "Concombre (avec peau)", "Légumes", 15.0, 0.7, 3.6, 0.1, 0.5, 0.0, 150.0, "g", "1/2 concombre (~150g)"),
        CiqualFood("leg_carotte_crue", "Carotte crue", "Légumes", 41.0, 0.9, 9.6, 0.2, 2.8, 0.0, 80.0, "unité", "1 carotte moyenne (~80g)"),
        CiqualFood("leg_carotte_cuite", "Carottes cuites", "Légumes", 35.0, 0.8, 8.0, 0.2, 2.5, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("leg_courgette", "Courgette cuite vapeur", "Légumes", 17.0, 1.2, 3.1, 0.3, 1.0, 0.0, 200.0, "g", "1 courgette (~200g)"),
        CiqualFood("leg_brocoli", "Brocoli cuit vapeur", "Légumes", 35.0, 2.4, 7.0, 0.4, 2.6, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("leg_haricot_vert", "Haricots verts cuits", "Légumes", 31.0, 1.8, 7.0, 0.2, 2.7, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("leg_salade", "Salade verte (Laitue / Mâche)", "Légumes", 15.0, 1.4, 2.9, 0.2, 1.3, 0.0, 50.0, "g", "1 bol (~50g)"),
        CiqualFood("leg_epinards", "Épinards cuits", "Légumes", 23.0, 3.0, 3.8, 0.4, 2.2, 0.1, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("leg_champignon", "Champignons de Paris cuits", "Légumes", 28.0, 3.1, 3.3, 0.3, 1.0, 0.0, 100.0, "g", "1 portion (~100g)"),
        CiqualFood("leg_poivron_rouge", "Poivron rouge (cru ou cuit)", "Légumes", 31.0, 1.0, 6.0, 0.3, 2.1, 0.0, 120.0, "g", "1 poivron (~120g)"),
        CiqualFood("leg_poivron_vert", "Poivron vert", "Légumes", 20.0, 0.9, 4.6, 0.2, 1.7, 0.0, 120.0, "g", "1 poivron (~120g)"),
        CiqualFood("leg_oignon", "Oignon cuit", "Légumes", 40.0, 1.1, 9.3, 0.1, 1.7, 0.0, 80.0, "g", "1 oignon moyen (~80g)"),
        CiqualFood("leg_poireau", "Poireau cuit", "Légumes", 31.0, 1.5, 7.0, 0.3, 2.0, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("leg_choufleur", "Chou-fleur cuit", "Légumes", 25.0, 1.9, 5.0, 0.3, 2.0, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("leg_petit_pois", "Petits pois cuits", "Légumes", 81.0, 5.4, 14.5, 0.4, 5.7, 0.0, 120.0, "g", "1 portion (~120g)"),
        CiqualFood("leg_asperge", "Asperges cuites", "Légumes", 20.0, 2.2, 3.9, 0.1, 2.1, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("leg_betterave", "Betterave rouge cuite", "Légumes", 43.0, 1.6, 9.6, 0.2, 2.8, 0.1, 100.0, "g", "1 portion (~100g)"),

        // === FÉCULENTS & PAINS ===
        CiqualFood("fec_pdt_vapeur", "Pomme de terre vapeur", "Féculents & Pains", 87.0, 1.9, 20.1, 0.1, 1.8, 0.0, 150.0, "g", "2 pommes de terre moyennes (~150g)"),
        CiqualFood("fec_pdt_four", "Pomme de terre au four", "Féculents & Pains", 93.0, 2.5, 21.2, 0.1, 2.2, 0.0, 150.0, "g", "1 pomme de terre au four (~150g)"),
        CiqualFood("fec_puree", "Purée de pommes de terre maison", "Féculents & Pains", 110.0, 2.0, 15.0, 4.5, 1.2, 0.3, 200.0, "g", "1 portion (~200g)"),
        CiqualFood("fec_frites", "Frites cuites maison", "Féculents & Pains", 260.0, 3.4, 35.0, 12.0, 3.0, 0.4, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("fec_riz_blanc", "Riz blanc cuit", "Féculents & Pains", 130.0, 2.7, 28.2, 0.3, 0.4, 0.0, 150.0, "g", "1 portion moyenne (~150g)"),
        CiqualFood("fec_riz_basmati", "Riz basmati cuit", "Féculents & Pains", 120.0, 2.5, 26.0, 0.3, 0.5, 0.0, 150.0, "g", "1 portion moyenne (~150g)"),
        CiqualFood("fec_riz_complet", "Riz complet cuit", "Féculents & Pains", 112.0, 2.6, 23.5, 0.9, 1.8, 0.0, 150.0, "g", "1 portion moyenne (~150g)"),
        CiqualFood("fec_pates_blanches", "Pâtes blanches cuites", "Féculents & Pains", 131.0, 5.0, 25.0, 1.1, 1.8, 0.0, 180.0, "g", "1 assiette moyenne (~180g)"),
        CiqualFood("fec_pates_completes", "Pâtes complètes cuites", "Féculents & Pains", 124.0, 5.3, 26.5, 0.5, 3.9, 0.0, 180.0, "g", "1 assiette moyenne (~180g)"),
        CiqualFood("fec_pain_baguette", "Pain baguette blanche", "Féculents & Pains", 274.0, 8.8, 56.0, 1.2, 3.2, 1.4, 50.0, "g", "1/5 de baguette (~50g)"),
        CiqualFood("fec_pain_complet", "Pain complet / céréales", "Féculents & Pains", 247.0, 13.0, 41.0, 3.4, 7.0, 1.0, 40.0, "unité", "1 tranche (~40g)"),
        CiqualFood("fec_pain_mie_blanc", "Pain de mie blanc", "Féculents & Pains", 265.0, 8.0, 49.0, 3.5, 2.7, 1.1, 35.0, "unité", "1 tranche (~35g)"),
        CiqualFood("fec_pain_mie_complet", "Pain de mie complet", "Féculents & Pains", 250.0, 9.0, 44.0, 3.0, 5.5, 1.0, 35.0, "unité", "1 tranche (~35g)"),
        CiqualFood("fec_flocons_avoine", "Flocons d'avoine", "Féculents & Pains", 389.0, 16.9, 66.3, 6.9, 10.6, 0.0, 40.0, "g", "1 bol petit-déjeuner (~40g)"),
        CiqualFood("fec_semoule", "Semoule de blé cuite (Couscous)", "Féculents & Pains", 112.0, 3.8, 23.2, 0.2, 1.4, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("fec_quinoa", "Quinoa cuit", "Féculents & Pains", 120.0, 4.4, 21.3, 1.9, 2.8, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("fec_lentilles", "Lentilles vertes/corail cuites", "Féculents & Pains", 116.0, 9.0, 20.1, 0.4, 7.9, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("fec_pois_chiches", "Pois chiches cuits", "Féculents & Pains", 164.0, 8.9, 27.4, 2.6, 7.6, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("fec_haricots_rouges", "Haricots rouges cuits", "Féculents & Pains", 127.0, 8.7, 22.8, 0.5, 6.4, 0.0, 150.0, "g", "1 portion (~150g)"),
        CiqualFood("fec_mais", "Maïs doux en grains", "Féculents & Pains", 96.0, 3.4, 19.0, 1.2, 2.0, 0.2, 100.0, "g", "1 petite boîte (~100g)"),

        // === VIANDES & VOLAILLES ===
        CiqualFood("via_blanc_poulet", "Blanc de poulet cuit", "Viandes & Volailles", 165.0, 31.0, 0.0, 3.6, 0.0, 0.2, 120.0, "g", "1 filet (~120g)"),
        CiqualFood("via_cuisse_poulet", "Cuisse de poulet rôtie (sans peau)", "Viandes & Volailles", 180.0, 27.0, 0.0, 8.0, 0.0, 0.3, 130.0, "g", "1 cuisse (~130g)"),
        CiqualFood("via_filet_dinde", "Filet de dinde cuit", "Viandes & Volailles", 147.0, 30.0, 0.0, 2.0, 0.0, 0.2, 120.0, "g", "1 escalope (~120g)"),
        CiqualFood("via_steak_5", "Steak haché pur boeuf 5% MG cuit", "Viandes & Volailles", 130.0, 21.0, 0.0, 5.0, 0.0, 0.2, 100.0, "unité", "1 steak haché (~100g)"),
        CiqualFood("via_steak_15", "Steak haché pur boeuf 15% MG cuit", "Viandes & Volailles", 205.0, 19.0, 0.0, 14.5, 0.0, 0.2, 100.0, "unité", "1 steak haché (~100g)"),
        CiqualFood("via_bavette_boeuf", "Bavette / Steak de boeuf grillé", "Viandes & Volailles", 190.0, 26.0, 0.0, 9.5, 0.0, 0.2, 130.0, "g", "1 pièce de boeuf (~130g)"),
        CiqualFood("via_roti_porc", "Rôti de porc cuit", "Viandes & Volailles", 175.0, 26.0, 0.0, 7.8, 0.0, 0.2, 120.0, "g", "1 tranche épaisse (~120g)"),
        CiqualFood("via_cote_porc", "Côte de porc grillée", "Viandes & Volailles", 210.0, 24.0, 0.0, 12.5, 0.0, 0.2, 130.0, "g", "1 côte (~130g)"),
        CiqualFood("via_jambon_blanc", "Jambon blanc découenné dégraissé", "Viandes & Volailles", 110.0, 21.0, 1.0, 2.5, 0.0, 1.8, 45.0, "unité", "1 tranche (~45g)"),
        CiqualFood("via_jambon_cru", "Jambon cru (Serrano / Parme)", "Viandes & Volailles", 230.0, 28.0, 0.5, 13.0, 0.0, 4.5, 30.0, "unité", "1 tranche (~30g)"),
        CiqualFood("via_bacon", "Bacon / Poitrine fumée grillée", "Viandes & Volailles", 400.0, 37.0, 1.4, 28.0, 0.0, 3.8, 20.0, "g", "2 tranches fines (~20g)"),

        // === POISSONS & FRUITS DE MER ===
        CiqualFood("poi_saumon", "Pavé de saumon cuit au four", "Poissons & Fruits de mer", 208.0, 20.0, 0.0, 13.0, 0.0, 0.1, 120.0, "g", "1 pavé (~120g)"),
        CiqualFood("poi_cabillaud", "Filet de cabillaud / morue cuit", "Poissons & Fruits de mer", 82.0, 18.0, 0.0, 0.7, 0.0, 0.2, 120.0, "g", "1 filet (~120g)"),
        CiqualFood("poi_thon_boite", "Thon au naturel en boîte (égoutté)", "Poissons & Fruits de mer", 116.0, 26.0, 0.0, 1.0, 0.0, 0.9, 90.0, "g", "1 boîte égouttée (~90g)"),
        CiqualFood("poi_crevettes", "Crevettes roses cuites décortiquées", "Poissons & Fruits de mer", 99.0, 21.0, 0.2, 1.5, 0.0, 1.2, 100.0, "g", "1 portion (~100g)"),
        CiqualFood("poi_colin", "Filet de colin d'Alaska / merlu cuit", "Poissons & Fruits de mer", 85.0, 19.0, 0.0, 0.9, 0.0, 0.2, 120.0, "g", "1 filet (~120g)"),
        CiqualFood("poi_sardines", "Sardines à l'huile d'olive (égouttées)", "Poissons & Fruits de mer", 208.0, 24.6, 0.0, 11.5, 0.0, 1.1, 65.0, "g", "1 boîte égouttée (~65g)"),
        CiqualFood("poi_maquereau", "Filet de maquereau cuit", "Poissons & Fruits de mer", 262.0, 23.8, 0.0, 18.0, 0.0, 0.2, 100.0, "g", "1 filet (~100g)"),

        // === OEUFS ===
        CiqualFood("oeu_oeuf_dur", "Oeuf entier dur / mollet", "Oeufs", 155.0, 13.0, 1.1, 11.0, 0.0, 0.3, 60.0, "unité", "1 oeuf moyen (~60g)"),
        CiqualFood("oeu_oeuf_plat", "Oeuf au plat (avec filet d'huile)", "Oeufs", 190.0, 13.5, 0.8, 15.0, 0.0, 0.4, 65.0, "unité", "1 oeuf au plat (~65g)"),
        CiqualFood("oeu_oeuf_coque", "Oeuf à la coque", "Oeufs", 155.0, 13.0, 1.1, 11.0, 0.0, 0.3, 60.0, "unité", "1 oeuf à la coque (~60g)"),
        CiqualFood("oeu_blanc", "Blanc d'oeuf cuit", "Oeufs", 52.0, 11.0, 0.7, 0.2, 0.0, 0.4, 35.0, "unité", "1 blanc d'oeuf (~35g)"),
        CiqualFood("oeu_jaune", "Jaune d'oeuf cuit", "Oeufs", 322.0, 16.0, 0.6, 27.0, 0.0, 0.1, 20.0, "unité", "1 jaune d'oeuf (~20g)"),
        CiqualFood("oeu_omelette", "Omelette nature (2 oeufs)", "Oeufs", 170.0, 12.0, 1.0, 13.0, 0.0, 0.5, 120.0, "g", "1 omelette 2 oeufs (~120g)"),

        // === LAITAGES & VÉGÉTAUX ===
        CiqualFood("lai_demi_ecreme", "Lait demi-écrémé", "Laitages & Végétaux", 46.0, 3.2, 4.8, 1.5, 0.0, 0.1, 200.0, "ml", "1 verre de lait (~200ml)"),
        CiqualFood("lai_ecreme", "Lait écrémé", "Laitages & Végétaux", 34.0, 3.4, 5.0, 0.1, 0.0, 0.1, 200.0, "ml", "1 verre de lait (~200ml)"),
        CiqualFood("lai_entier", "Lait entier", "Laitages & Végétaux", 64.0, 3.2, 4.7, 3.6, 0.0, 0.1, 200.0, "ml", "1 verre de lait (~200ml)"),
        CiqualFood("lai_soja", "Boisson au soja nature", "Laitages & Végétaux", 43.0, 3.8, 1.7, 2.0, 0.6, 0.1, 200.0, "ml", "1 verre (~200ml)"),
        CiqualFood("lai_amande", "Boisson à l'amande sans sucre", "Laitages & Végétaux", 15.0, 0.5, 0.3, 1.2, 0.3, 0.1, 200.0, "ml", "1 verre (~200ml)"),
        CiqualFood("lai_yaourt_nature", "Yaourt nature classique", "Laitages & Végétaux", 61.0, 3.5, 4.7, 3.3, 0.0, 0.1, 125.0, "unité", "1 pot de yaourt (~125g)"),
        CiqualFood("lai_yaourt_grec", "Yaourt à la grecque", "Laitages & Végétaux", 120.0, 4.5, 4.0, 9.5, 0.0, 0.1, 125.0, "unité", "1 pot (~125g)"),
        CiqualFood("lai_fromage_blanc_0", "Fromage blanc 0% MG", "Laitages & Végétaux", 48.0, 8.0, 4.0, 0.1, 0.0, 0.1, 100.0, "g", "1 ramequin (~100g)"),
        CiqualFood("lai_fromage_blanc_3", "Fromage blanc 3% MG", "Laitages & Végétaux", 75.0, 7.5, 4.0, 3.2, 0.0, 0.1, 100.0, "g", "1 ramequin (~100g)"),
        CiqualFood("lai_skyr", "Skyr nature 0% MG", "Laitages & Végétaux", 57.0, 10.0, 4.0, 0.2, 0.0, 0.1, 140.0, "unité", "1 pot de skyr (~140g)"),
        CiqualFood("lai_petit_suisse", "Petit-suisse 40% MG", "Laitages & Végétaux", 135.0, 9.0, 4.0, 9.0, 0.0, 0.1, 60.0, "unité", "1 petit-suisse (~60g)"),
        CiqualFood("lai_emmental", "Emmental / Gruyère râpé", "Laitages & Végétaux", 380.0, 28.0, 0.5, 30.0, 0.0, 0.7, 30.0, "g", "1 poignée (~30g)"),
        CiqualFood("lai_mozzarella", "Mozzarella au lait de vache", "Laitages & Végétaux", 280.0, 18.0, 1.5, 22.0, 0.0, 0.5, 125.0, "unité", "1 boule (~125g)"),
        CiqualFood("lai_parmesan", "Parmesan râpé", "Laitages & Végétaux", 431.0, 38.0, 4.0, 29.0, 0.0, 1.5, 15.0, "g", "1 cuillère à soupe (~15g)"),
        CiqualFood("lai_camembert", "Camembert au lait de vache", "Laitages & Végétaux", 295.0, 20.0, 0.5, 23.5, 0.0, 1.4, 30.0, "g", "1 portion (~30g)"),
        CiqualFood("lai_chevre_frais", "Fromage de chèvre frais", "Laitages & Végétaux", 170.0, 11.0, 2.0, 13.0, 0.0, 0.9, 40.0, "g", "1 rondin (~40g)"),

        // === MATIÈRES GRASSES ===
        CiqualFood("mat_huile_olive", "Huile d'olive", "Matières grasses", 884.0, 0.0, 0.0, 100.0, 0.0, 0.0, 10.0, "g", "1 cuillère à soupe (~10g)"),
        CiqualFood("mat_huile_colza", "Huile de colza", "Matières grasses", 884.0, 0.0, 0.0, 100.0, 0.0, 0.0, 10.0, "g", "1 cuillère à soupe (~10g)"),
        CiqualFood("mat_huile_tournesol", "Huile de tournesol", "Matières grasses", 884.0, 0.0, 0.0, 100.0, 0.0, 0.0, 10.0, "g", "1 cuillère à soupe (~10g)"),
        CiqualFood("mat_beurre_doux", "Beurre doux classique", "Matières grasses", 717.0, 0.8, 0.7, 81.0, 0.0, 0.0, 10.0, "g", "1 noisette de beurre (~10g)"),
        CiqualFood("mat_beurre_allege", "Beurre allégé (40% MG)", "Matières grasses", 380.0, 2.0, 2.0, 40.0, 0.0, 0.3, 10.0, "g", "1 noisette (~10g)"),
        CiqualFood("mat_amandes", "Amandes entières", "Matières grasses", 579.0, 21.2, 21.6, 49.9, 12.5, 0.0, 30.0, "g", "1 poignée (~30g)"),
        CiqualFood("mat_noix", "Noix de Grenoble", "Matières grasses", 654.0, 15.2, 13.7, 65.2, 6.7, 0.0, 30.0, "g", "1 poignée (~30g)"),
        CiqualFood("mat_noisettes", "Noisettes", "Matières grasses", 628.0, 15.0, 16.7, 60.8, 9.7, 0.0, 30.0, "g", "1 poignée (~30g)"),
        CiqualFood("mat_cajou", "Noix de cajou", "Matières grasses", 553.0, 18.2, 30.2, 43.8, 3.3, 0.0, 30.0, "g", "1 poignée (~30g)"),
        CiqualFood("mat_chia", "Graines de chia", "Matières grasses", 486.0, 16.5, 42.1, 30.7, 34.4, 0.0, 15.0, "g", "1 cuillère à soupe (~15g)"),
        CiqualFood("mat_beurre_cacahuete", "Beurre de cacahuète pur", "Matières grasses", 588.0, 25.0, 20.0, 50.0, 6.0, 0.0, 20.0, "g", "1 cuillère à soupe (~20g)"),

        // === DOUCEURS & BOISSONS ===
        CiqualFood("dou_miel", "Miel toutes fleurs", "Douceurs & Boissons", 304.0, 0.3, 82.0, 0.0, 0.2, 0.0, 20.0, "g", "1 cuillère à café bombée (~20g)"),
        CiqualFood("dou_confiture", "Confiture de fruits (fraise / abricot)", "Douceurs & Boissons", 250.0, 0.5, 60.0, 0.1, 1.2, 0.0, 20.0, "g", "1 cuillère à café bombée (~20g)"),
        CiqualFood("dou_sucre", "Sucre blanc en poudre / morceau", "Douceurs & Boissons", 400.0, 0.0, 100.0, 0.0, 0.0, 0.0, 5.0, "g", "1 morceau / 1 c. à café (~5g)"),
        CiqualFood("dou_chocolat_noir_70", "Chocolat noir 70% cacao", "Douceurs & Boissons", 598.0, 7.8, 34.0, 43.0, 10.9, 0.0, 20.0, "g", "2 carrés (~20g)"),
        CiqualFood("dou_chocolat_lait", "Chocolat au lait", "Douceurs & Boissons", 535.0, 7.6, 59.4, 29.7, 2.0, 0.2, 20.0, "g", "2 carrés (~20g)"),
        CiqualFood("dou_compote_sans_sucre", "Compote de pomme sans sucre ajouté", "Douceurs & Boissons", 52.0, 0.4, 12.0, 0.2, 1.5, 0.0, 100.0, "unité", "1 gourde / pot (~100g)"),
        CiqualFood("dou_galette_riz", "Galette de riz soufflé", "Douceurs & Boissons", 380.0, 8.0, 80.0, 2.8, 3.1, 0.1, 8.0, "unité", "1 galette (~8g)"),
        CiqualFood("boi_cafe_noir", "Café noir (sans sucre)", "Douceurs & Boissons", 2.0, 0.1, 0.0, 0.0, 0.0, 0.0, 150.0, "ml", "1 tasse (~150ml)"),
        CiqualFood("boi_the_sans_sucre", "Thé vert / noir infusé (sans sucre)", "Douceurs & Boissons", 1.0, 0.0, 0.2, 0.0, 0.0, 0.0, 200.0, "ml", "1 mug (~200ml)"),
        CiqualFood("boi_jus_orange", "Jus d'orange pur jus (sans sucre ajouté)", "Douceurs & Boissons", 45.0, 0.7, 10.0, 0.1, 0.4, 0.0, 150.0, "ml", "1 verre moyen (~150ml)")
    )

    fun filter(query: String, category: String = "Tous"): List<CiqualFood> {
        val cleanQuery = normalize(query.trim())
        return ALL_FOODS.filter { food ->
            val matchesCategory = (category == "Tous" || food.category.equals(category, ignoreCase = true))
            val matchesQuery = if (cleanQuery.isBlank()) {
                true
            } else {
                val cleanName = normalize(food.name)
                val cleanCat = normalize(food.category)
                cleanName.contains(cleanQuery) || cleanCat.contains(cleanQuery)
            }
            matchesCategory && matchesQuery
        }
    }

    fun search(query: String): List<CiqualFood> = filter(query, "Tous")

    private fun normalize(str: String): String {
        return str.lowercase()
            .replace('é', 'e')
            .replace('è', 'e')
            .replace('ê', 'e')
            .replace('ë', 'e')
            .replace('à', 'a')
            .replace('â', 'a')
            .replace('ä', 'a')
            .replace('î', 'i')
            .replace('ï', 'i')
            .replace('ô', 'o')
            .replace('ö', 'o')
            .replace('ù', 'u')
            .replace('û', 'u')
            .replace('ü', 'u')
            .replace('ç', 'c')
            .replace('-', ' ')
            .replace('\'', ' ')
    }
}
