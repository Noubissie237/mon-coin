# ✅ Fonctionnalité : Marquer Manuellement comme Terminée

## 🎯 Objectif

Permettre à l'utilisateur de marquer manuellement une tâche comme terminée, même si :
- L'heure de début a été manquée
- L'heure de fin n'a pas été respectée
- La tâche a été faite en dehors des horaires prévus
- L'utilisateur veut simplement la marquer comme terminée sans attendre l'alarme

## 📋 Cas d'Usage

### Cas 1 : Tâche Manquée mais Faite Plus Tard
```
Tâche programmée : 14h00 - 16h00
État actuel : MISSED (manquée car non démarrée à 16h00)
Action utilisateur : Clique sur le bouton ✓ à 17h00
Résultat : État passe à COMPLETED
```

### Cas 2 : Tâche en Cours que l'Utilisateur Veut Terminer
```
Tâche en cours : 10h00 - 12h00
État actuel : RUNNING (démarrée à 10h00)
Heure actuelle : 11h30
Action utilisateur : Clique sur le bouton ✓
Résultat : État passe à COMPLETED (sans attendre 12h00)
```

### Cas 3 : Tâche Programmée Terminée Avant l'Heure
```
Tâche programmée : 15h00 - 17h00
État actuel : SCHEDULED
Heure actuelle : 14h30
Action utilisateur : Clique sur le bouton ✓
Résultat : État passe à COMPLETED (tâche faite en avance)
```

### Cas 4 : Tâche DUREE Terminée Sans Timer
```
Tâche DUREE : 2 heures
État actuel : SCHEDULED
Action utilisateur : Clique sur le bouton ✓ (sans démarrer le timer)
Résultat : État passe à COMPLETED (tâche faite sans chronométrer)
```

## 🔧 Modifications Apportées

### 1. **HomeScreen.kt**

#### A. OccurrenceCard
- ✅ Ajout du paramètre `onComplete: () -> Unit`
- ✅ Ajout d'un `IconButton` avec icône `CheckCircle`
- ✅ Le bouton n'apparaît que si la tâche n'est pas déjà terminée ou annulée
- ✅ Positionné entre le timer et la flèche de détails

```kotlin
// Bouton pour marquer comme terminée (si pas déjà terminée ou annulée)
if (!isCompleted && !isCancelled) {
    IconButton(
        onClick = { onComplete() },
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Marquer comme terminée",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}
```

#### B. DurationTaskCard
- ✅ Ajout du paramètre `onComplete: () -> Unit`
- ✅ Ajout d'un `IconButton` avec icône `CheckCircle`
- ✅ Positionné entre le bouton "Démarrer" et le bouton "Info"

```kotlin
IconButton(
    onClick = onComplete,
    modifier = Modifier.size(40.dp)
) {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = "Marquer comme terminée",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
    )
}
```

#### C. Appels des Composants
- ✅ `OccurrenceCard` : `onComplete = { viewModel.completeOccurrence(occurrenceWithTask.occurrence.id) }`
- ✅ `DurationTaskCard` : `onComplete = { viewModel.completeTask(task.id) }`

### 2. **HomeViewModel.kt**

#### Nouvelle Méthode : `completeOccurrence()`
```kotlin
/**
 * Marquer manuellement une occurrence comme terminée
 * Utile quand une tâche a été manquée mais faite plus tard,
 * ou quand l'utilisateur veut marquer comme terminée sans attendre l'alarme
 */
fun completeOccurrence(occurrenceId: String) {
    viewModelScope.launch {
        // Mettre à jour l'état de l'occurrence
        occurrenceRepository.updateOccurrenceState(occurrenceId, TaskState.COMPLETED)
        
        // Récupérer l'occurrence pour obtenir le taskId
        val occurrence = occurrenceRepository.getOccurrenceById(occurrenceId)
        if (occurrence != null) {
            // Mettre à jour l'état de la tâche principale
            taskRepository.updateTaskState(occurrence.taskId, TaskState.COMPLETED)
        }
    }
}
```

**Actions effectuées** :
1. Met à jour l'état de l'`OccurrenceEntity` à `COMPLETED`
2. Récupère l'occurrence pour obtenir le `taskId`
3. Met à jour l'état de la `TaskEntity` à `COMPLETED`

#### Méthode Existante : `completeTask()`
Déjà présente, utilisée pour les tâches DUREE et FLEXIBLE :
```kotlin
fun completeTask(taskId: String) {
    viewModelScope.launch {
        taskRepository.updateTaskState(taskId, TaskState.COMPLETED)
    }
}
```

## 🎨 Interface Utilisateur

### Bouton "Marquer comme Terminée"
- **Icône** : `Icons.Default.CheckCircle` (cercle avec coche)
- **Couleur** : `MaterialTheme.colorScheme.primary` (couleur primaire de l'app)
- **Taille** : 24dp (icône) dans un IconButton de 36dp/40dp
- **Position** : 
  - Pour les occurrences : Entre le timer (si en cours) et la flèche de détails
  - Pour les tâches DUREE : Entre le bouton "Démarrer" et le bouton "Info"

### Visibilité du Bouton
Le bouton apparaît pour :
- ✅ Tâches `SCHEDULED` (programmées)
- ✅ Tâches `RUNNING` (en cours)
- ✅ Tâches `MISSED` (manquées)
- ✅ Tâches `SNOOZED` (reportées)

Le bouton n'apparaît PAS pour :
- ❌ Tâches `COMPLETED` (déjà terminées)
- ❌ Tâches `CANCELLED` (annulées)

## 🔄 Flux de Fonctionnement

### Pour une Occurrence (PLAGE)
```
Utilisateur clique sur le bouton ✓
↓
HomeViewModel.completeOccurrence(occurrenceId)
↓
OccurrenceRepository.updateOccurrenceState(occurrenceId, COMPLETED)
↓
Récupère l'occurrence pour obtenir taskId
↓
TaskRepository.updateTaskState(taskId, COMPLETED)
↓
L'UI se met à jour automatiquement (Flow)
↓
La carte affiche l'icône ✓ verte et le texte barré
```

### Pour une Tâche DUREE
```
Utilisateur clique sur le bouton ✓
↓
HomeViewModel.completeTask(taskId)
↓
TaskRepository.updateTaskState(taskId, COMPLETED)
↓
L'UI se met à jour automatiquement (Flow)
↓
La tâche disparaît de la liste des tâches DUREE
```

### Pour une Tâche FLEXIBLE
```
Utilisateur clique sur le bouton ✓
↓
HomeViewModel.completeTask(taskId)
↓
TaskRepository.updateTaskState(taskId, COMPLETED)
↓
L'UI se met à jour automatiquement (Flow)
↓
La tâche passe en bas de liste avec texte barré
```

## ✅ Avantages

1. **Flexibilité** : L'utilisateur n'est pas contraint par les horaires
2. **Rattrapage** : Permet de marquer les tâches manquées comme terminées
3. **Rapidité** : Pas besoin d'attendre l'alarme de fin
4. **Simplicité** : Un seul clic pour marquer comme terminée
5. **Visibilité** : Bouton clairement identifiable avec icône universelle ✓

## 📱 Comportement Attendu

### Scénario 1 : Tâche Manquée Récupérée
```
1. Tâche "Révision Math" : 14h00 - 16h00
2. État à 16h01 : MISSED (icône ❌ rouge)
3. Utilisateur fait la tâche à 17h00
4. Clique sur le bouton ✓
5. État : COMPLETED (icône ✓ verte, texte barré)
6. La tâche reste visible mais en bas de liste
```

### Scénario 2 : Tâche Terminée en Avance
```
1. Tâche "Réunion" : 15h00 - 17h00
2. État à 14h30 : SCHEDULED
3. Réunion terminée à 16h30 (30 min en avance)
4. Utilisateur clique sur le bouton ✓
5. État : COMPLETED
6. L'alarme de 17h00 ne sonnera pas (tâche déjà terminée)
```

### Scénario 3 : Tâche DUREE Sans Timer
```
1. Tâche "Lecture" : 1 heure
2. Utilisateur lit pendant 1h sans démarrer le timer
3. Clique sur le bouton ✓ au lieu de "Démarrer"
4. État : COMPLETED
5. La tâche disparaît de la liste
```

## 🧪 Tests à Effectuer

### Test 1 : Marquer Occurrence SCHEDULED
1. Créer une tâche PLAGE future
2. Vérifier que le bouton ✓ est visible
3. Cliquer sur le bouton ✓
4. ✅ Vérifier : État passe à COMPLETED
5. ✅ Vérifier : Icône ✓ verte apparaît
6. ✅ Vérifier : Texte est barré

### Test 2 : Marquer Occurrence MISSED
1. Créer une tâche PLAGE dans le passé
2. Attendre qu'elle passe à MISSED
3. Vérifier que le bouton ✓ est visible
4. Cliquer sur le bouton ✓
5. ✅ Vérifier : État passe à COMPLETED

### Test 3 : Marquer Occurrence RUNNING
1. Démarrer une tâche PLAGE
2. Vérifier que le bouton ✓ est visible (à côté du timer)
3. Cliquer sur le bouton ✓
4. ✅ Vérifier : État passe à COMPLETED
5. ✅ Vérifier : Timer disparaît

### Test 4 : Marquer Tâche DUREE
1. Créer une tâche DUREE
2. Vérifier que le bouton ✓ est visible (entre "Démarrer" et "Info")
3. Cliquer sur le bouton ✓
4. ✅ Vérifier : Tâche disparaît de la liste

### Test 5 : Bouton Invisible pour COMPLETED
1. Marquer une tâche comme terminée
2. ✅ Vérifier : Le bouton ✓ n'est plus visible
3. ✅ Vérifier : Seule l'icône ✓ verte est affichée

## 📊 Impact sur les Autres Fonctionnalités

### Alarmes
- ✅ Si une tâche est marquée comme terminée avant l'alarme de fin, l'alarme ne sonnera pas
- ✅ Le `TaskStateChecker` vérifie l'état avant de déclencher l'alarme

### Statistiques
- ✅ Les tâches marquées manuellement comptent comme terminées dans les stats
- ✅ Pas de distinction entre terminée automatiquement ou manuellement

### Historique
- ✅ Les tâches marquées manuellement apparaissent dans l'historique comme COMPLETED
- ✅ Le champ `actualEndTime` pourrait être utilisé pour tracer quand elle a été marquée

## 🔮 Améliorations Futures Possibles

1. **Confirmation** : Ajouter un dialogue de confirmation avant de marquer comme terminée
2. **Annulation** : Permettre d'annuler une complétion manuelle
3. **Horodatage** : Enregistrer l'heure exacte de la complétion manuelle
4. **Raison** : Permettre d'ajouter une note expliquant pourquoi la tâche a été marquée manuellement
5. **Badge** : Afficher un badge différent pour les tâches terminées manuellement vs automatiquement

---

## ✅ Résumé

Cette fonctionnalité ajoute une **flexibilité essentielle** à l'application en permettant à l'utilisateur de gérer manuellement l'état de ses tâches, tout en conservant le système d'alarmes automatiques pour ceux qui préfèrent un suivi strict des horaires.

**Fichiers modifiés** :
- ✅ `HomeScreen.kt` - Ajout des boutons ✓
- ✅ `HomeViewModel.kt` - Ajout de la méthode `completeOccurrence()`

**Aucune modification nécessaire** dans :
- ❌ Repositories (méthodes existantes suffisent)
- ❌ Entités (structure inchangée)
- ❌ Services (fonctionnent avec les états existants)
