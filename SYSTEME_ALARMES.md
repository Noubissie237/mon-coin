# 🔔 Système d'Alarmes - Style Clock Android

## 📋 Vue d'Ensemble

Le système d'alarmes a été complètement refondu pour reproduire le comportement de l'application **Clock (Horloge)** d'Android.

---

## 🎯 Comportements Implémentés

### 1. Début de Tâche (START_TRIGGER)

**Quand** : À l'heure de début programmée

**Condition** : État = `SCHEDULED`

**Actions** :
1. ✅ **Écran plein écran** s'affiche (`TaskStartActivity`)
2. ✅ **Sonnerie courte** en boucle (TYPE_ALARM)
3. ✅ **Vibration** en pattern
4. ✅ **3 boutons disponibles** :
   - **"Commencer"** → État passe à `RUNNING`
   - **"Annuler"** → État passe à `CANCELLED`
   - **"Fermer"** → Ferme l'écran, état reste `SCHEDULED`

**Fichiers** :
- `TaskStartActivity.kt` - Écran avec sonnerie
- `AlarmReceiver.kt` - Vérifie l'état avant déclenchement

---

### 2. Fin de Tâche (ALARM_TRIGGER)

**Quand** : À l'heure de fin programmée

**Conditions et Actions** :

#### Si État = `RUNNING` ✅
1. **Écran plein écran** s'affiche (`AlarmActivity`)
2. **Sonnerie** en boucle
3. **Vibration**
4. **2 boutons** :
   - **"Stop"** → État passe à `COMPLETED`
   - **"Snooze"** → État passe à `SNOOZED` (reporte de 10 min)

#### Si État = `SCHEDULED` ⚠️
1. **Pas d'écran**
2. État passe automatiquement à `MISSED`
3. **Notification push** "Tâche manquée"

#### Si État = `CANCELLED` ❌
1. **Aucune action**
2. L'alarme ne se déclenche pas

**Fichiers** :
- `AlarmActivity.kt` - Écran de fin avec sonnerie
- `AlarmReceiver.kt` - Logique conditionnelle
- `TaskStateChecker.kt` - Vérification des états

---

## 🔄 Transitions d'États

```
SCHEDULED
    ↓ [Début: Clic "Commencer"]
RUNNING
    ↓ [Fin: Clic "Stop"]
COMPLETED

SCHEDULED
    ↓ [Début: Clic "Annuler"]
CANCELLED
    (Aucune alarme de fin)

SCHEDULED
    ↓ [Fin atteinte sans démarrage]
MISSED
    + Notification push
```

---

## 🎵 Sonneries

### Début de Tâche
- **Type** : `RingtoneManager.TYPE_ALARM`
- **Durée** : En boucle jusqu'à action
- **Volume** : `USAGE_ALARM`
- **Vibration** : Pattern 500ms ON / 500ms OFF

### Fin de Tâche
- **Type** : `RingtoneManager.TYPE_ALARM`
- **Durée** : En boucle jusqu'à action
- **Volume** : `USAGE_ALARM`
- **Vibration** : Pattern continu

---

## 📱 Écrans Plein Écran

### TaskStartActivity
```kotlin
- Titre de la tâche
- "Il est temps de commencer"
- Bouton "Commencer" (bleu, grand)
- Bouton "Annuler" (rouge, outlined)
- Bouton "Fermer" (texte)
```

### AlarmActivity
```kotlin
- Titre de la tâche
- "Temps écoulé !"
- Bouton "Stop" (grand)
- Bouton "Snooze" (outlined)
```

---

## 🔐 Persistance

### Après Fermeture de l'App
✅ **Fonctionne** - Les alarmes sont gérées par `AlarmManager`

### Après Redémarrage
✅ **Fonctionne** - `AlarmRescheduleService` reprogramme tout au boot

**Permissions nécessaires** :
- `RECEIVE_BOOT_COMPLETED`
- `SCHEDULE_EXACT_ALARM`
- `USE_EXACT_ALARM`
- `WAKE_LOCK`
- `USE_FULL_SCREEN_INTENT`

---

## 🛠️ Architecture

### Composants Principaux

#### 1. AlarmScheduler
```kotlin
- scheduleStartAlarm()  // Alarme de début
- scheduleAlarm()       // Alarme de fin
- scheduleReminder()    // Rappels avant début
```

#### 2. AlarmReceiver
```kotlin
- ACTION_START_TRIGGER   // Gère le début
- ACTION_ALARM_TRIGGER   // Gère la fin
- ACTION_REMINDER_TRIGGER // Gère les rappels
```

#### 3. TaskStateChecker
```kotlin
- shouldTriggerStartAlarm()  // Vérifie si SCHEDULED
- shouldTriggerEndAlarm()    // Vérifie si RUNNING
- checkAndUpdateStates()     // Mise à jour périodique
```

#### 4. Activities
```kotlin
- TaskStartActivity  // Écran de début
- AlarmActivity      // Écran de fin
```

---

## 📊 Flux Complet

### Scénario 1 : Tâche Démarrée et Terminée
```
1. Création → État: SCHEDULED
2. Alarmes programmées (début + fin)
3. [Heure début] → TaskStartActivity + sonnerie
4. Clic "Commencer" → État: RUNNING
5. [Heure fin] → AlarmActivity + sonnerie
6. Clic "Stop" → État: COMPLETED
```

### Scénario 2 : Tâche Annulée
```
1. Création → État: SCHEDULED
2. [Heure début] → TaskStartActivity + sonnerie
3. Clic "Annuler" → État: CANCELLED
4. [Heure fin] → Rien (alarme ne se déclenche pas)
```

### Scénario 3 : Tâche Manquée
```
1. Création → État: SCHEDULED
2. [Heure début] → TaskStartActivity + sonnerie
3. Clic "Fermer" → État: SCHEDULED
4. [Heure fin] → Pas d'écran
5. État: MISSED + Notification push
```

---

## 🔍 Vérifications de Sécurité

### Avant Déclenchement START
```kotlin
if (occurrence.state == SCHEDULED) {
    // Déclencher TaskStartActivity
} else {
    // Ne rien faire
}
```

### Avant Déclenchement END
```kotlin
when (occurrence.state) {
    RUNNING -> {
        // Déclencher AlarmActivity
        // Puis marquer COMPLETED
    }
    SCHEDULED -> {
        // Marquer MISSED
        // Envoyer notification
    }
    CANCELLED -> {
        // Ne rien faire
    }
}
```

---

## 🧪 Tests à Effectuer

### Test 1 : Cycle Complet
1. Créer une tâche dans 2 minutes
2. Attendre le début → Vérifier sonnerie
3. Cliquer "Commencer"
4. Attendre la fin → Vérifier sonnerie
5. Cliquer "Stop"
6. Vérifier état = COMPLETED

### Test 2 : Annulation
1. Créer une tâche dans 2 minutes
2. Attendre le début
3. Cliquer "Annuler"
4. Attendre l'heure de fin
5. Vérifier qu'aucune alarme ne sonne

### Test 3 : Tâche Manquée
1. Créer une tâche dans 2 minutes
2. Attendre le début
3. Cliquer "Fermer"
4. Attendre l'heure de fin
5. Vérifier notification "Tâche manquée"

### Test 4 : Persistance
1. Créer une tâche dans 5 minutes
2. Fermer l'application (swipe)
3. Attendre → Vérifier que l'alarme sonne quand même

### Test 5 : Reboot
1. Créer une tâche dans 10 minutes
2. Redémarrer le téléphone
3. Attendre → Vérifier que l'alarme sonne

---

## 📝 Notes Techniques

### MediaPlayer
- Utilise `AudioAttributes.USAGE_ALARM`
- Mode `isLooping = true`
- Libéré dans `onDestroy()`

### Vibration
- Pattern : `longArrayOf(0, 500, 500)`
- Index de répétition : 0 (boucle infinie)
- Annulée avec `vibrator.cancel()`

### Écrans Lock Screen
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
    setShowWhenLocked(true)
    setTurnScreenOn(true)
} else {
    window.addFlags(
        FLAG_SHOW_WHEN_LOCKED or
        FLAG_TURN_SCREEN_ON
    )
}
```

---

## 🎨 UI/UX

### Boutons
- **Primaire** : Bleu, 64dp de hauteur
- **Secondaire** : Outlined, 64dp
- **Tertiaire** : TextButton

### Icônes
- ▶️ PlayArrow - Commencer
- ❌ Cancel - Annuler
- ✖️ Close - Fermer
- ⏹️ Stop - Arrêter

### Couleurs
- **Commencer** : Primary
- **Annuler** : Error
- **Fermer** : OnSurfaceVariant

---

## ✅ Checklist d'Implémentation

- [x] TaskStartActivity avec sonnerie
- [x] 3 boutons (Commencer, Annuler, Fermer)
- [x] Transitions d'états correctes
- [x] AlarmActivity vérifie l'état RUNNING
- [x] Notification pour tâches MISSED
- [x] TaskStateChecker pour vérifications
- [x] Pas d'alarme si CANCELLED
- [x] Persistance après fermeture app
- [x] Persistance après reboot
- [ ] Tests sur appareil réel

---

**Version** : 1.2.0  
**Date** : Octobre 2024  
**Statut** : ✅ Implémenté - En attente de tests
