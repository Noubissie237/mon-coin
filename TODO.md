# Cahier des charges — Mon Coin

**But**
Créer une application Android native (Kotlin) de gestion de tâches, alarmes et rappels qui fonctionne en arrière-plan même si l'application est tuée, gère les répétitions, évite les chevauchements, propose une planification intelligente (gestion du sommeil), fournit historique + statistiques et un bloc‑note intégré.

---

## 1. Contexte et enjeux

L'utilisateur a des difficultés d'organisation quotidiennes : grande quantité de tâches, besoin de prises de notes, planification flexible (ponctuelle, périodique, quotidienne), et d'alertes fiables qui continuent de fonctionner même après redémarrage du téléphone ou suppression de l'application de la RAM.

Objectifs clés :

* Notifications préalables aux tâches (avertissement), alarmes sonores à la fin des tâches (comme l'app Clock), gestion des tâches en durée ou en intervalle, prévention des chevauchements, gestion du sommeil et offre d'alternatives intelligentes.

---

## 2. Périmètre fonctionnel

### 2.1. Gestion des tâches

* Création d'une tâche avec : titre, description, tags/catégorie, type de répétition (ponctuelle, quotidienne permanente, périodique par jours de semaine), mode de programmation (durée fixe ou intervalle horaire), durée (ex. 5h) ou plage (ex. 12:00–14:30), priorité, notifications (on/off), alarmes (on/off), sonnerie/son par défaut ou personnalisée, rappel avant tâche (par ex. 10 min, 15 min), couleur/étiquette.
* États d'une tâche : Programmé, En cours, Terminée, Manquée, Annulée, Snoozée.
* Démarrer/arrêter une tâche manuellement depuis l'UI.
* Si tâche non démarrée et heure de fin passée → basculer en Manquée et notifier.

### 2.2. Rappels et notifications

* Notification préalable configurable (ex: 10 min avant).
* Alarme sonore à la fin de la tâche (exacte et audible même si app supprimée).
* Notifications push locales (pas nécessaire un serveur distant) — affichées même après redémarrage.
* Canaux de notification séparés : Rappel, Alarme, Système.

### 2.3. Planification du sommeil

* L'utilisateur définit une plage de sommeil quotidienne (ex. 23:00–05:00) et une durée cible (ex. 6h).
* Lors de la création d'une tâche qui chevauche la plage de sommeil, l'application empêche la sauvegarde et propose : — refuser, — déplacer la tâche, — décaler la plage de sommeil (avec prévisualisation des conséquences).

### 2.4. Gestion des chevauchements et plages disponibles

* L'utilisateur ne saisit pas d'horaires manuellement mais choisit parmi des plages proposées générées par le moteur d'occupation (plages libres selon agenda interne + sommeil + tâches récurrentes).
* Règle simple : aucune plage ne doit se chevaucher. Les tasks adjacentes peuvent être séparées d'au moins 1 minute.
* Option avancée : mode semi-manuel (l'utilisateur peut forcer chevauchement avec avertissement et option de prioriser une tâche).

### 2.5. Historique et statistiques

* Historique complet : programmées, accomplies, manquées, annulées — avec filtres date/matière/etat.
* Statistiques : temps total passé sur tâches par jour/semaine/mois, taux d'accomplissement, graphique d'activité (barres/line).

### 2.6. Bloc‑note

* Bloc‑note journalier (ou global) avec timestamps, support du markdown léger, recherche, tags et lien vers tâches (attacher note à une tâche).

### 2.7. Paramètres et préférences

* Sons et volumes, mode Ne pas déranger (intégration DND Android), autorisations (notifications, alarmes exactes, démarrage au boot), thèmes (clair/sombre), sauvegarde/restauration (export JSON, sauvegarde automatique sur stockage local ou cloud si l'utilisateur connecte un service).

---

## 3. Exigences non fonctionnelles

* Performance : timers et alarmes précis (<1s d'erreur acceptable), faible consommation batterie.
* Fiabilité : résilience aux redémarrages, aux kills par OS et aux mises à jour.
* Sécurité & vie privée : données locales chiffrées (option), permissions minimales, pas de collecte externe par défaut.
* Accessibilité : tailles de police réglables, labels pour lecteur d’écran.
* Support Android : API niveau ciblé (p.ex. minSdk 26 ou 28) — décider selon besoin des APIs modernes.

---

## 4. Contraintes techniques et solutions proposées

### 4.1. Architecture générale

* Architecture client-only (app standalone) pour MVP ; option d'intégration serveur ultérieure pour multi‑device et push distants.
* Pattern : MVVM + Repository + Room (Local DB) + Kotlin Coroutines + Flow/LiveData.
* UI : Jetpack Compose pour interface fluide et moderne.

### 4.2. Persistance

* Room DB : entités Task, Occurrence (instance d'une tâche récurrente), Note, Settings, AlarmSchedule.
* Migrations gérées.

### 4.3. Alarmes et timers en arrière-plan (comportement critique)

* Pour alarmes exactes et sonores similaires à l'app Clock : utiliser `AlarmManager.setExactAndAllowWhileIdle()` couplé à un `BroadcastReceiver` pour déclencher un `ForegroundService` qui affichera l'UI d'alarme et jouera la sonnerie.
* Pour assurer le fonctionnement après redémarrage : implémenter `BOOT_COMPLETED` receiver pour rescheduler toutes les occurrences futures depuis la DB.
* Si l'OS tue l'app : les alarmes programmées via AlarmManager persistent (si utilisées correctement) ; pour minuteries de durée (ex. démarrer et mesurer 5h) : démarrer un ForegroundService au moment du démarrage de la tâche (notification persistante) — le service gère le compteur et l'alarme finale.
* Gérer `SCHEDULE_EXACT_ALARM` (Android 12+) et expliquer à l'utilisateur pourquoi la permission est nécessaire pour alarmes exactes.

### 4.4. Notifications locales

* Utiliser `NotificationManager` avec canaux. Notifications préalables programmées via AlarmManager ou WorkManager selon priorité / exactitude.

### 4.5. Redémarrage et persistance des timers

* Deux cas :

  1. Tâches planifiées par plage horaire → rescheduler via AlarmManager au BOOT.
  2. Tâches en cours (démarrées et mesurant une durée) → stocker l'instant de début dans DB ; au BOOT, lancer un ForegroundService qui calcule le temps restant et continue le timer (ou déclenche immédiatement l'alarme si expiré).

### 4.6. Gestion du Doze et optimisation batterie

* Utiliser `setExactAndAllowWhileIdle` pour événements critiques ; limiter wakeups fréquents ; regrouper tâches non critiques via WorkManager (API différée) pour économiser batterie.

### 4.7. Sonnerie et UI d'alarme

* ForegroundActivity/AlarmActivity qui prend le focus, bouton Snooze/Stop, répétitions, volume/Do Not Disturb handling (demander l'exception DND si nécessaire).

---

## 5. Modèle de données (proposition simplifiée)

### Entité Task

* id: UUID
* titre: String
* description: String
* type: ENUM{PONCTUELLE, QUOTIDIENNE, PERIODIQUE}
* recurrence: structure (jours de semaine, intervalle, fin éventuelle)
* mode: ENUM{DUREE, PLAGE}
* duree_minutes: Int? (si DUREE)
* start_time: LocalDateTime? (si PLAGE ou occurrence calculée)
* end_time: LocalDateTime?
* sleep_conflict_policy: ENUM{BLOCK, PROPOSE_SHIFT, FORCE}
* reminders: List (minutes avant)
* alarm_sound_uri: String
* priority: Int
* state: ENUM{SCHEDULED, RUNNING, COMPLETED, MISSED, CANCELLED}
* created_at, updated_at

### Entité Occurrence

* id, task_id, start_at, end_at, state

### Note

* id, date, content, related_task_id?

---

## 6. UI / UX — parcours utilisateur & écrans clés

* Écran d’accueil : résumé du jour (tâches à venir, tâches en cours, bouton + rapide), accès bloc‑note, statistiques rapide.
* Création tâche : assistant pas à pas (choix type → choisir plage depuis plages disponibles générées → config reminders/son/labels) — interface centrée sur boutons et sélecteurs, pas d’input horaire libre.
* Sélecteur de plages disponibles : vue calendrier / timeline montrant plages occupées et plages libres ; tap pour choisir.
* Écran tâche en cours : minuterie active, bouton Pause/Stop, note rapide.
* Écran alarme/sonnerie : interface plein écran (Stop / Snooze), information tâche.
* Historique : filtres et détail d’occurrence.
* Paramètres : sommeil, notifications, sauvegarde.

---

## 7. Règles métier et cas d’usage détaillés

* Règle de non-chevauchement : création validée uniquement si la plage choisie est libre. Sinon proposer décalage automatique minimal (ou proposer décaler sommeil).
* Début automatique vs manuel : l’app demande à l’utilisateur de marquer "débuté" (optionnel) ; si non marqué et fin passée → état Manquée.
* Répétition : les occurrences répétitives se génèrent à la fréquence choisie ; possibilité de génération à long terme (next N occurrences) ou on‑demand.

---

## 8. Critères d’acceptation / Tests

* Alarmes sonnent après expiration d’une durée même si app tuée ou redémarrée.
* Notifications préalables apparaissent conformément aux paramètres.
* Chevauchements impossibles en mode normal; proposition de correction si conflit.
* Historique enregistre correctement les états (Completed vs Missed).

---

## 10. Risques & mitigations

* **Doze / restrictions OEM** : risque d’OS qui bloque alarmes exactes. Mitigation : documenter comportements par OEM, proposer exiger permission d'alarme exacte et instructions UX pour autorisations d’économies d'énergie.
* **Consommation batterie (ForegroundService)** : minimiser temps en foreground, n'utiliser le service que quand tâche respecte durée et que la précision est requise.
* **Permissions utilisateurs** : UX pour expliquer pourquoi les permissions sont nécessaires.

---

## 11. Tests et QA

* Unit tests pour la logique de planification (non-chevauchement, occurrence generation).
* Tests d’intégration pour rescheduling BOOT et alarm triggers (sur émulateur + vrai appareil).
* Tests manuels multi‑OEM pour s’assurer du comportement (Samsung, Xiaomi, etc.).

---

## 13. Annexes — décisions techniques recommandées

* Kotlin + Jetpack Compose, Room, Coroutines, WorkManager (pour tâches non-exactes), AlarmManager + ForegroundService pour alarmes exactes, MediaPlayer ou SoundPool pour sonneries, Notification channels, Request SCHEDULE_EXACT_ALARM.

---
