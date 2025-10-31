# Analyse : Affichage et Mise à Jour des Tâches Quotidiennes

## 📊 Situation Actuelle

### ❌ PROBLÈMES MAJEURS DÉTECTÉS

---

## 🐛 Problème 1 : Tâches Quotidiennes Non Régénérées

### Comportement Actuel

**Lors de la création d'une tâche QUOTIDIENNE :**
```kotlin
// TaskCreateViewModel.kt ligne 290-299
if (state.taskMode == TaskMode.PLAGE) {
    val occurrence = OccurrenceEntity(
        taskId = task.id,
        startAt = task.startTime!!,
        endAt = task.endTime!!
    )
    occurrenceRepository.insertOccurrence(occurrence)
}
```

**Problème :**
- ✅ Une occurrence est créée pour AUJOURD'HUI
- ❌ **AUCUNE occurrence n'est créée pour DEMAIN**
- ❌ **AUCUNE occurrence n'est créée pour les jours suivants**

### Impact

**Scénario :**
1. Vous créez une tâche QUOTIDIENNE "Faire du sport" à 18h
2. Aujourd'hui : La tâche apparaît ✅
3. Vous la complétez aujourd'hui
4. **DEMAIN : La tâche N'APPARAÎT PAS** ❌
5. **Après-demain : La tâche N'APPARAÎT PAS** ❌

**Résultat :** La tâche "quotidienne" n'apparaît qu'UNE SEULE FOIS !

---

## 🐛 Problème 2 : Pas de Système de Régénération Automatique

### Code Actuel

**SchedulingService.kt** contient la fonction `generateOccurrences()` :
```kotlin
// Ligne 187-250
suspend fun generateOccurrences(
    task: TaskEntity,
    fromDate: LocalDate,
    count: Int = 30
): List<OccurrenceEntity> {
    // ... génère jusqu'à 30 occurrences
}
```

**Problème :**
- ✅ La fonction EXISTE
- ❌ Elle n'est **JAMAIS APPELÉE** nulle part dans le code
- ❌ Pas de job quotidien pour régénérer les occurrences
- ❌ Pas de WorkManager pour planifier la régénération

### Recherche dans le Code

```bash
# Recherche de l'utilisation de generateOccurrences
grep -r "generateOccurrences(" app/src/main/java/
# Résultat : AUCUNE utilisation trouvée !
```

---

## 🐛 Problème 3 : Affichage Basé sur les Occurrences du Jour

### HomeViewModel.kt

```kotlin
// Ligne 57-59
val today = LocalDateTime.now()
val startOfDay = today.toLocalDate().atStartOfDay()
val endOfDay = today.toLocalDate().atTime(23, 59, 59)

// Ligne 63
occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay)
```

**Comportement :**
- Affiche UNIQUEMENT les occurrences d'aujourd'hui
- Si aucune occurrence n'existe pour aujourd'hui → tâche invisible
- Même si la tâche est QUOTIDIENNE

---

## 🐛 Problème 4 : Mise à Jour des États Limitée

### TaskStateChecker.kt

```kotlin
// Ligne 28-59
suspend fun checkAndUpdateStates() {
    val now = LocalDateTime.now()
    
    // Get occurrences from last 24h only
    val pastOccurrences = occurrenceRepository.getOccurrencesBetween(
        LocalDateTime.now().minusDays(1),
        now
    ).first()
    
    // Update SCHEDULED -> MISSED
    // Update RUNNING -> COMPLETED
}
```

**Problème :**
- ✅ Met à jour les états des occurrences passées
- ❌ Ne crée PAS de nouvelles occurrences pour demain
- ❌ Vérifie seulement les dernières 24h

---

## ✅ Solutions Nécessaires

### Solution 1 : Appeler generateOccurrences() à la Création

**Modifier TaskCreateViewModel.kt :**

```kotlin
// Après la ligne 288
taskRepository.insertTask(task)

// AJOUTER : Générer les occurrences pour les tâches récurrentes
if (task.type == TaskType.QUOTIDIENNE || task.type == TaskType.PERIODIQUE) {
    val occurrences = schedulingService.generateOccurrences(
        task = task,
        fromDate = LocalDate.now(),
        count = 30  // Générer 30 jours d'avance
    )
    occurrenceRepository.insertOccurrences(occurrences)
    
    // Planifier les alarmes pour les prochaines occurrences
    if (task.alarmsEnabled) {
        occurrences.take(7).forEach { occurrence ->  // 7 premiers jours
            alarmScheduler.scheduleStartAlarm(occurrence, task.title)
        }
    }
} else if (state.taskMode == TaskMode.PLAGE) {
    // PONCTUELLE avec PLAGE : une seule occurrence
    val occurrence = OccurrenceEntity(
        taskId = task.id,
        startAt = task.startTime!!,
        endAt = task.endTime!!
    )
    occurrenceRepository.insertOccurrence(occurrence)
    
    if (task.alarmsEnabled) {
        alarmScheduler.scheduleStartAlarm(occurrence, task.title)
    }
}
```

### Solution 2 : Job Quotidien de Régénération

**Créer DailyOccurrenceWorker.kt :**

```kotlin
@HiltWorker
class DailyOccurrenceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val occurrenceRepository: OccurrenceRepository,
    private val schedulingService: SchedulingService,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        try {
            // 1. Récupérer toutes les tâches récurrentes
            val recurringTasks = taskRepository.getAllTasks().first()
                .filter { it.type == TaskType.QUOTIDIENNE || it.type == TaskType.PERIODIQUE }
            
            // 2. Pour chaque tâche, vérifier les occurrences futures
            recurringTasks.forEach { task ->
                val tomorrow = LocalDate.now().plusDays(1)
                val in30Days = LocalDate.now().plusDays(30)
                
                // Vérifier les occurrences existantes
                val existingOccurrences = occurrenceRepository
                    .getOccurrencesBetween(
                        tomorrow.atStartOfDay(),
                        in30Days.atTime(23, 59, 59)
                    ).first()
                    .filter { it.taskId == task.id }
                
                // Si moins de 7 jours d'occurrences, en générer plus
                if (existingOccurrences.size < 7) {
                    val newOccurrences = schedulingService.generateOccurrences(
                        task = task,
                        fromDate = tomorrow,
                        count = 30
                    )
                    
                    // Insérer seulement les nouvelles (pas de doublons)
                    val newOccurrencesToInsert = newOccurrences.filter { new ->
                        existingOccurrences.none { existing ->
                            existing.startAt == new.startAt && existing.taskId == new.taskId
                        }
                    }
                    
                    occurrenceRepository.insertOccurrences(newOccurrencesToInsert)
                    
                    // Planifier les alarmes
                    if (task.alarmsEnabled) {
                        newOccurrencesToInsert.take(7).forEach { occurrence ->
                            alarmScheduler.scheduleStartAlarm(occurrence, task.title)
                        }
                    }
                }
            }
            
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
```

**Planifier le Worker dans Application.kt :**

```kotlin
class MonCoinApplication : Application(), Configuration.Provider {
    
    override fun onCreate() {
        super.onCreate()
        
        // Planifier le job quotidien à minuit
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyOccurrenceWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(calculateDelayUntilMidnight(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyOccurrenceGeneration",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
    
    private fun calculateDelayUntilMidnight(): Long {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return Duration.between(now, midnight).toMillis()
    }
}
```

### Solution 3 : Mise à Jour Automatique des États

**Améliorer TaskStateChecker.kt :**

```kotlin
suspend fun checkAndUpdateStates() {
    val now = LocalDateTime.now()
    
    // 1. Mettre à jour les états des occurrences passées
    val pastOccurrences = occurrenceRepository.getOccurrencesBetween(
        LocalDateTime.now().minusDays(7),  // Derniers 7 jours
        now
    ).first()
    
    pastOccurrences.forEach { occurrence ->
        when {
            occurrence.state == TaskState.SCHEDULED && occurrence.endAt.isBefore(now) -> {
                occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.MISSED)
                notificationHelper.showMissedTaskNotification(occurrence.id, "Tâche manquée")
            }
            occurrence.state == TaskState.RUNNING && occurrence.endAt.isBefore(now) -> {
                occurrenceRepository.updateOccurrenceState(occurrence.id, TaskState.COMPLETED)
            }
        }
    }
    
    // 2. NOUVEAU : Vérifier et régénérer les occurrences si nécessaire
    val recurringTasks = taskRepository.getAllTasks().first()
        .filter { it.type == TaskType.QUOTIDIENNE || it.type == TaskType.PERIODIQUE }
    
    recurringTasks.forEach { task ->
        val futureOccurrences = occurrenceRepository.getOccurrencesBetween(
            now,
            now.plusDays(7)
        ).first().filter { it.taskId == task.id }
        
        // Si moins de 3 jours d'occurrences futures, en générer
        if (futureOccurrences.size < 3) {
            val newOccurrences = schedulingService.generateOccurrences(
                task = task,
                fromDate = LocalDate.now(),
                count = 7
            )
            occurrenceRepository.insertOccurrences(newOccurrences)
        }
    }
}
```

### Solution 4 : Affichage Intelligent dans HomeViewModel

**Option A : Afficher les tâches même sans occurrence**

```kotlin
private fun loadHomeData() {
    viewModelScope.launch {
        val today = LocalDateTime.now()
        val startOfDay = today.toLocalDate().atStartOfDay()
        val endOfDay = today.toLocalDate().atTime(23, 59, 59)
        
        combine(
            occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay),
            taskRepository.getAllTasks()
        ) { occurrences, allTasks ->
            
            // NOUVEAU : Pour les tâches QUOTIDIENNES sans occurrence aujourd'hui,
            // créer une occurrence automatiquement
            val quotidienneTasks = allTasks.filter { it.type == TaskType.QUOTIDIENNE }
            quotidienneTasks.forEach { task ->
                val hasOccurrenceToday = occurrences.any { it.taskId == task.id }
                if (!hasOccurrenceToday && task.startTime != null && task.endTime != null) {
                    // Créer l'occurrence manquante
                    val occurrence = OccurrenceEntity(
                        taskId = task.id,
                        startAt = today.toLocalDate().atTime(task.startTime.toLocalTime()),
                        endAt = today.toLocalDate().atTime(task.endTime.toLocalTime()),
                        state = TaskState.SCHEDULED
                    )
                    occurrenceRepository.insertOccurrence(occurrence)
                }
            }
            
            // Recharger les occurrences après création
            val updatedOccurrences = occurrenceRepository
                .getOccurrencesBetween(startOfDay, endOfDay).first()
            
            // ... reste du code
        }
    }
}
```

---

## 📋 Plan d'Action Recommandé

### Phase 1 : Correction Immédiate (Critique)
1. ✅ **Appeler generateOccurrences() à la création** (Solution 1)
2. ✅ **Créer les occurrences manquantes au chargement** (Solution 4)

### Phase 2 : Système Robuste (Important)
3. ✅ **Implémenter DailyOccurrenceWorker** (Solution 2)
4. ✅ **Améliorer TaskStateChecker** (Solution 3)

### Phase 3 : Tests (Essentiel)
5. ✅ **Tester la création de tâche QUOTIDIENNE**
6. ✅ **Vérifier l'affichage le lendemain**
7. ✅ **Tester la régénération automatique**

---

## 🧪 Tests à Créer

```kotlin
@Test
fun `QUOTIDIENNE task creates occurrences for next 30 days`() {
    // Given
    val task = TaskEntity(
        title = "Daily Task",
        type = TaskType.QUOTIDIENNE,
        mode = TaskMode.PLAGE,
        startTime = LocalDateTime.now().withHour(10),
        endTime = LocalDateTime.now().withHour(11)
    )
    
    // When
    taskRepository.insertTask(task)
    val occurrences = schedulingService.generateOccurrences(
        task, LocalDate.now(), 30
    )
    occurrenceRepository.insertOccurrences(occurrences)
    
    // Then
    assertEquals(30, occurrences.size)
    
    // Vérifier que chaque jour a une occurrence
    val tomorrow = LocalDate.now().plusDays(1)
    val tomorrowOccurrence = occurrences.find { 
        it.startAt.toLocalDate() == tomorrow 
    }
    assertNotNull(tomorrowOccurrence)
}

@Test
fun `completed QUOTIDIENNE task appears again tomorrow`() = runTest {
    // Given
    val task = createQuotidienneTask()
    val todayOccurrence = createOccurrence(task, LocalDate.now())
    
    // When - Compléter aujourd'hui
    occurrenceRepository.updateOccurrenceState(
        todayOccurrence.id, 
        TaskState.COMPLETED
    )
    
    // Then - Vérifier que demain a une occurrence
    val tomorrow = LocalDate.now().plusDays(1)
    val tomorrowOccurrences = occurrenceRepository
        .getOccurrencesBetween(
            tomorrow.atStartOfDay(),
            tomorrow.atTime(23, 59, 59)
        ).first()
    
    val tomorrowOccurrence = tomorrowOccurrences.find { 
        it.taskId == task.id 
    }
    assertNotNull("Tomorrow should have an occurrence", tomorrowOccurrence)
    assertEquals(TaskState.SCHEDULED, tomorrowOccurrence?.state)
}
```

---

## ⚠️ Conclusion

**État actuel : CRITIQUE**

Les tâches QUOTIDIENNES ne fonctionnent PAS comme prévu :
- ❌ N'apparaissent qu'une seule fois
- ❌ Pas de régénération automatique
- ❌ Disparaissent après complétion

**Actions requises : URGENTES**

Il faut implémenter les solutions 1 et 4 immédiatement pour que les tâches quotidiennes fonctionnent correctement.

---

**Date d'analyse :** 31 octobre 2025  
**Priorité :** 🔴 CRITIQUE
