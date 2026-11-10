# Journal Alimentaire Personnel (Inspiré de FatSecret)

Application Android épurée dédiée exclusivement au journal alimentaire personnel avec stockage 100% local, respect strict de la confidentialité, recherche Open Food Facts avec cache intelligent, historique rapide et suivi statistique mensuel.

## Décisions Clés & Réponses Utilisateur

> [!IMPORTANT]
> Décisions confirmées suite à votre retour :
> - **Structure des repas** : 4 repas classiques par jour (**Petit-déjeuner**, **Déjeuner**, **Dîner**, **Collations**), reprenant l'ergonomie familière et efficace de FatSecret.
> - **Confidentialité & Hors-ligne** : Stockage 100% local sur SQLite / Room. Aucune création de compte, aucun serveur tiers, aucune fuite de données. Fonctionnement autonome hors-ligne garanti.
> - **Aliments & Cache** : Recherche sur l'API gratuite Open Food Facts mise en cache automatiquement en local dans Room pour minimiser la consommation réseau et batterie. Possibilité d'ajouter des aliments personnalisés manuellement.
> - **Objectif calorique simplifié** : Suivi centré sur les kcal (consommées vs objectif restant), sans surcharge de micro-nutriments.
> - **Design épuré Dark Mode** : Interface sombre moderne, fluide, sobre et sans animations superflues.

---

## 1. Vue d'Ensemble & Concept

L'application élimine tout le superflu de FatSecret (réseau social, recettes sponsorisées, publicités, graphismes lourds) pour se concentrer sur l'essentiel :
1. **Journal Quotidien (Journalier)** : Consultation et saisie rapide des 4 repas du jour avec sélecteur de date (hier, aujourd'hui, calendrier).
2. **Ajout d'Aliment Rapide** :
   - Onglet **Récents / Habituels** : pour ajouter en 1 clic un aliment déjà consommé avec sa portion habituelle.
   - Onglet **Recherche Open Food Facts** : avec mise en cache locale instantanée.
   - Onglet **Création Manuelle** : pour renseigner directement un produit maison ou hors-ligne (nom, kcal/100g ou portion).
3. **Statistiques & Historique** : Graphique mensuel / hebdomadaire sobre montrant les calories consommées jour par jour face à la ligne d'objectif.
4. **Paramètres** : Ajustement direct de l'objectif calorique quotidien (ex: 2000 kcal).

---

## 2. Expérience Utilisateur & Design Visuel

### Parcours Utilisateur
1. **Écran Principal (Journal)** :
   - En-tête : Date actuelle avec flèches précédent/suivant et bouton calendrier.
   - Résumé calorique : Jauge sobre en anneau/barre (`Consommé` / `Objectif` / `Restant`).
   - 4 Cartes de Repas :
     - *Petit-déjeuner* (icône matin, total kcal, liste des aliments saisis avec portions et kcal calculées, bouton "+" pour ajouter).
     - *Déjeuner* (icône midi, total kcal, liste et bouton "+").
     - *Dîner* (icône soir, total kcal, liste et bouton "+").
     - *Collations* (icône pomme/encas, total kcal, liste et bouton "+").
   - Action sur un aliment : Clic pour ajuster la quantité (en grammes ou portions) ou glisser pour supprimer.
2. **Écran d'Ajout d'Aliment** :
   - Barre de recherche en haut.
   - Sélecteur d'onglets rapides : *Récents* (local), *Recherche OFF* (web + cache), *Création rapide*.
   - Fiche portion : saisie du nombre de grammes (ex: 150g) avec calcul en temps réel des kcal correspondantes.
3. **Écran Statistiques (Graphique)** :
   - Sélecteur de mois / 30 derniers jours.
   - Histogramme sobre dessiné en Jetpack Compose Canvas affichant chaque jour avec code couleur (vert sous l'objectif, orange/rouge si dépassé).
   - Moyenne quotidienne du mois et respect de l'objectif en %.

### Identité Visuelle Dark Mode Épurée
- **Couleurs** :
  - Fond principal : `#121214` (Noir profond OLED / gris très sombre).
  - Surfaces / Cartes : `#1C1C20` avec séparateurs subtils `#2C2C34`.
  - Accent primaire : `#4CAF50` (Vert santé / calories FatSecret) et `#64B5F6` (Bleu sobre pour sélecteurs).
  - Alertes / Dépassements : `#FF7043` (Orange corail discret).
  - Typographie : Blanc pur `#F5F5F7` et gris secondaire `#A0A0AB`.
- **Légèreté & Performance** :
  - Pas de transitions lourdes ni de bibliothèques tierces énergivores.
  - Rendu Jetpack Compose optimisé avec `remember`, `derivedStateOf` et Room `Flow`.

---

## 3. Décisions Produit & Architecture Technique

### Persistance Locale (Room Database)
- **Base SQLite Room** :
  - Table `foods` : Aliments en cache (id, code-barres éventuel, nom, marque, kcal pour 100g, portion par défaut, est_personnalisé).
  - Table `meal_entries` : Entrées du journal (id, date au format YYYY-MM-DD, type de repas : BREAKFAST, LUNCH, DINNER, SNACK, food_id, quantité en grammes, total_kcal calculé).
  - Table `user_settings` : Clé/Valeur (ex: `daily_calorie_target` = 2000).

### API Open Food Facts & Cache
- Client Retrofit HTTP léger avec OkHttp (Cache HTTP activé + User-Agent conforme `User-Agent: MonJournalCalorique - Android - 1.0`).
- Recherche textuelle sur `https://world.openfoodfacts.org/cgi/search.pl`.
- Tout résultat sélectionné ou recherché est persisté dans la table `foods`. Les recherches futures consultent d'abord la base locale avant l'API.

---

## 4. Schéma d'Architecture & Flux de Données

```
┌─────────────────────────────────────────────────────────────┐
│                    Jetpack Compose UI                       │
│  ┌──────────────────┐ ┌─────────────────┐ ┌──────────────┐  │
│  │  JournalScreen   │ │   AddFoodScreen │ │ StatsScreen  │  │
│  │ (4 repas + jauge)│ │(Récents/OFF/Perso│ │ (Graphique) │  │
│  └────────┬─────────┘ └────────┬────────┘ └──────┬───────┘  │
└───────────┼────────────────────┼─────────────────┼──────────┘
            │                    │                 │
            ▼                    ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                     DiaryViewModel                          │
│        (StateFlow<DailySummary>, StateFlow<MonthStats>)     │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      DiaryRepository                        │
│     ┌────────────────────────────────┬─────────────────┐    │
│     │                                │                 │    │
│     ▼                                ▼                 ▼    │
│ ┌───────────────┐           ┌──────────────┐     ┌────────┐ │
│ │ MealEntryDao  │           │   FoodDao    │     │ Settings│ │
│ │  (Room SQLite)│           │ (Local Cache)│     │  Dao   │ │
│ └───────┬───────┘           └───────┬──────┘     └────────┘ │
└─────────┼───────────────────────────┼───────────────────────┘
          │                           │
          ▼                           ▼ (Fallback si non en cache)
┌──────────────────┐         ┌────────────────────────────────┐
│  SQLite Local    │         │  Open Food Facts API           │
│  (100% offline)  │         │  (Retrofit REST Client)        │
└──────────────────┘         └────────────────────────────────┘
```

---

## 5. Plan d'Implémentation Étape par Étape

1. **Configuration & Entités Room** :
   - Mise en place du modèle de données Room (`FoodItemEntity`, `MealEntryEntity`, `MealType` enum).
   - Création des DAOs réactifs avec `Flow` (`FoodDao`, `MealEntryDao`).
   - Configuration de `AppDatabase`.
2. **Couche Réseau Open Food Facts** :
   - Définition de l'API Retrofit Open Food Facts avec Moshi pour désérialiser les nutriments (`energy-kcal_100g`, `product_name`).
   - Repository unifié avec stratégie "Cache d'abord" et gestion du mode hors-ligne.
3. **Logique Métier & ViewModel** :
   - Gestion de la date courante, calcul du total quotidien, des calories par repas et de la jauge.
   - Gestion de l'historique des aliments récents.
   - Calcul des agrégats pour les graphiques mensuels.
4. **Interface Graphique Jetpack Compose** :
   - Thème Dark Mode épuré M3 dans `Theme.kt` et `Color.kt`.
   - Écran Journal avec en-tête de date, résumé calorique, 4 sections de repas pliables/dépliables avec suppression par swipe ou bouton.
   - Modal / Écran d'ajout avec recherche rapide, portions personnalisées et création manuelle.
   - Écran de statistiques avec graphique Canvas mensuel / hebdomadaire fluide et sobre.
   - Dialog de réglage de l'objectif calorique journalier.
5. **Vérification & Validation** :
   - Compilation complète via `compile_applet`.
   - Test des scénarios d'utilisation hors-ligne et avec recherche.
