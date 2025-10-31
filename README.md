# Mon Coin - Application de Gestion de Tâches

Application Android native en Kotlin pour la gestion de tâches, alarmes et rappels avec fonctionnement en arrière-plan.

## 🎯 Fonctionnalités Principales

### ✅ Implémenté (Phase 1)

- **Architecture MVVM** avec Hilt pour l'injection de dépendances
- **Base de données Room** avec entités Task, Occurrence, Note, SleepSchedule
- **Système d'alarmes exactes** utilisant AlarmManager
- **Notifications locales** avec canaux séparés (Rappel, Alarme, Système)
- **Persistance après redémarrage** via BroadcastReceiver BOOT_COMPLETED
- **Interface utilisateur** avec Jetpack Compose et Material 3
- **Navigation** entre les écrans principaux
- **Écran d'accueil** avec résumé des tâches du jour
- **Écran d'alarme** plein écran avec son et boutons Stop/Snooze

### 🚧 À Implémenter (Phases suivantes)

- **Création de tâches** avec assistant pas à pas
- **Gestion des répétitions** (ponctuelle, quotidienne, périodique)
- **Détection de chevauchements** et proposition de plages disponibles
- **Gestion du sommeil** avec conflit detection
- **Historique et statistiques** avec graphiques
- **Bloc-note** avec support Markdown et liens vers tâches
- **Export/Import** des données (JSON)
- **Thèmes** clair/sombre
- **Tests unitaires** et d'intégration

## 🏗️ Architecture

```
app/
├── data/
│   ├── local/
│   │   ├── entity/          # Entités Room
│   │   ├── dao/             # Data Access Objects
│   │   ├── converter/       # TypeConverters pour Room
│   │   └── MonCoinDatabase  # Base de données Room
│   ├── model/               # Modèles de données et enums
│   └── repository/          # Repositories (couche d'abstraction)
├── di/                      # Modules Hilt
├── alarm/                   # Système d'alarmes et notifications
│   ├── AlarmScheduler       # Planification des alarmes
│   ├── AlarmReceiver        # BroadcastReceiver
│   ├── AlarmActivity        # UI plein écran pour alarmes
│   ├── NotificationHelper   # Gestion des notifications
│   └── AlarmRescheduleService # Replanification après reboot
├── ui/
│   ├── navigation/          # Navigation Compose
│   ├── screen/              # Écrans de l'application
│   │   ├── home/
│   │   └── settings/
│   └── theme/               # Thème Material 3
└── MonCoinApplication       # Application Hilt
```

## 🔧 Technologies Utilisées

- **Kotlin** - Langage de programmation
- **Jetpack Compose** - UI moderne et déclarative
- **Room Database** - Persistance locale
- **Hilt** - Injection de dépendances
- **Coroutines & Flow** - Programmation asynchrone
- **Navigation Compose** - Navigation entre écrans
- **AlarmManager** - Alarmes exactes en arrière-plan
- **WorkManager** - Tâches différées (à venir)
- **DataStore** - Préférences (à venir)

## 📋 Prérequis

- Android Studio Hedgehog ou supérieur
- SDK Android 26 (Android 8.0) minimum
- SDK Android 35 (Android 15) cible
- Kotlin 2.0+

## 🚀 Installation

1. Cloner le projet
2. Ouvrir dans Android Studio
3. Synchroniser les dépendances Gradle
4. Lancer sur émulateur ou appareil physique

## ⚙️ Permissions Requises

L'application demande les permissions suivantes :

- `POST_NOTIFICATIONS` - Afficher des notifications (Android 13+)
- `SCHEDULE_EXACT_ALARM` - Planifier des alarmes exactes (Android 12+)
- `USE_EXACT_ALARM` - Utiliser des alarmes exactes
- `RECEIVE_BOOT_COMPLETED` - Redémarrer les alarmes après reboot
- `WAKE_LOCK` - Réveiller l'appareil pour les alarmes
- `VIBRATE` - Vibration pour les alarmes
- `USE_FULL_SCREEN_INTENT` - Afficher l'alarme en plein écran

## 📱 Fonctionnement des Alarmes

### Alarmes Exactes
- Utilisation de `AlarmManager.setExactAndAllowWhileIdle()`
- Fonctionne même en mode Doze
- Persiste après redémarrage du téléphone
- Persiste même si l'app est tuée

### Après Redémarrage
1. `AlarmReceiver` reçoit `BOOT_COMPLETED`
2. `AlarmRescheduleService` démarre
3. Toutes les alarmes futures sont replanifiées depuis la base de données

### Alarme Déclenchée
1. `AlarmReceiver` reçoit l'intent
2. `AlarmActivity` s'affiche en plein écran
3. Son d'alarme joué en boucle
4. Options : Arrêter ou Reporter (Snooze)

## 🎨 Design

L'interface utilise Material Design 3 avec :
- Navigation bottom bar
- Floating Action Button pour création rapide
- Cards pour affichage des tâches
- Thème adaptatif (à venir)

## 📊 Modèle de Données

### TaskEntity
- Tâche principale avec titre, description, type, mode, etc.
- Types : PONCTUELLE, QUOTIDIENNE, PERIODIQUE
- Modes : DUREE (durée fixe) ou PLAGE (horaire précis)
- États : SCHEDULED, RUNNING, COMPLETED, MISSED, CANCELLED, SNOOZED

### OccurrenceEntity
- Instance d'une tâche (pour les récurrences)
- Lien vers TaskEntity
- Horaires de début et fin
- État individuel

### NoteEntity
- Note avec contenu, tags
- Peut être liée à une tâche

### SleepScheduleEntity
- Plage de sommeil quotidienne
- Durée cible de sommeil

## 🔄 Prochaines Étapes

1. **Création de tâches** - Formulaire complet avec validation
2. **Moteur de planification** - Détection de chevauchements
3. **Gestion des récurrences** - Génération d'occurrences
4. **Historique** - Filtres et recherche
5. **Statistiques** - Graphiques d'activité
6. **Bloc-note** - Éditeur avec Markdown
7. **Tests** - Unit tests et tests d'intégration

## 📝 Notes de Développement

- Le projet utilise KSP au lieu de KAPT pour de meilleures performances
- Les alarmes sont testées sur émulateur et appareils réels
- Attention aux restrictions OEM (Xiaomi, Samsung, etc.)
- Documentation du code en cours

## 🐛 Problèmes Connus

- Le service de replanification après reboot nécessite des tests approfondis
- Certains fabricants (Xiaomi, Huawei) ont des restrictions supplémentaires
- L'UI d'alarme peut nécessiter des ajustements selon les versions Android

## 📄 Licence

Projet personnel - Tous droits réservés

## 👤 Auteur
NOUBISSIE KAMGA WILFRIED
