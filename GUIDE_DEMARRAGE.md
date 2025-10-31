# Guide de Démarrage - Mon Coin

## 🚀 Démarrage Rapide

### 1. Premier Lancement

Au premier lancement, l'application va :
1. Demander la permission de notifications (Android 13+)
2. Demander la permission d'alarmes exactes (Android 12+)
3. Créer les canaux de notification
4. Initialiser la base de données Room

**Important** : Acceptez toutes les permissions pour un fonctionnement optimal.

### 2. Structure Actuelle

L'application dispose actuellement de :
- ✅ **Écran d'accueil** - Vue d'ensemble des tâches
- ✅ **Écran de paramètres** - Configuration de base
- ✅ **Système d'alarmes** - Infrastructure complète
- ✅ **Base de données** - Prête à stocker les données

### 3. Fonctionnalités Disponibles

#### Écran d'Accueil
- Résumé du jour avec compteurs
- Liste des tâches en cours
- Liste des tâches programmées pour aujourd'hui
- Bouton flottant pour créer une tâche (à implémenter)

#### Système d'Alarmes
- Alarmes exactes qui fonctionnent en arrière-plan
- Persistance après redémarrage
- Interface plein écran avec son
- Boutons Stop et Snooze

## 📝 Prochaines Étapes de Développement

### Phase 2 : Création de Tâches (Prioritaire)

Créer l'écran de création de tâches avec :
1. **Formulaire de base**
   - Titre (obligatoire)
   - Description
   - Tags/Catégories

2. **Configuration du type**
   - Ponctuelle (une seule fois)
   - Quotidienne (tous les jours)
   - Périodique (jours spécifiques de la semaine)

3. **Configuration du mode**
   - Durée fixe (ex: 2 heures)
   - Plage horaire (ex: 14:00 - 16:00)

4. **Options avancées**
   - Rappels (10, 15, 30 minutes avant)
   - Son d'alarme personnalisé
   - Priorité
   - Couleur/Étiquette

### Phase 3 : Moteur de Planification

Implémenter la logique de :
1. **Détection de chevauchements**
   - Vérifier les conflits avec tâches existantes
   - Vérifier les conflits avec plage de sommeil

2. **Proposition de plages disponibles**
   - Calculer les créneaux libres
   - Afficher dans une timeline visuelle
   - Permettre la sélection

3. **Génération d'occurrences**
   - Pour les tâches récurrentes
   - Générer N occurrences futures
   - Planifier les alarmes correspondantes

### Phase 4 : Historique et Statistiques

1. **Écran Historique**
   - Liste de toutes les occurrences passées
   - Filtres par date, état, catégorie
   - Recherche

2. **Écran Statistiques**
   - Temps total par jour/semaine/mois
   - Taux d'accomplissement
   - Graphiques (barres, lignes)
   - Tendances

### Phase 5 : Bloc-Note

1. **Liste des notes**
   - Affichage chronologique
   - Recherche et filtres
   - Tags

2. **Éditeur de notes**
   - Support Markdown basique
   - Lien vers tâches
   - Timestamps automatiques

## 🔧 Configuration Recommandée

### Paramètres Android

Pour un fonctionnement optimal sur tous les appareils :

#### Samsung
1. Paramètres → Applications → Mon Coin
2. Batterie → Non optimisé
3. Autorisations → Toutes acceptées

#### Xiaomi/Redmi
1. Paramètres → Applications → Gérer les applications → Mon Coin
2. Démarrage automatique → Activé
3. Économie d'énergie → Pas de restrictions
4. Autres autorisations → Afficher en arrière-plan → Activé

#### Huawei
1. Paramètres → Applications → Mon Coin
2. Lancement → Gérer manuellement
3. Démarrage automatique → Activé
4. Activité secondaire → Activé
5. Exécuter en arrière-plan → Activé

### Paramètres de l'Application

1. **Notifications**
   - Activer toutes les notifications
   - Vérifier que les canaux sont activés dans les paramètres Android

2. **Alarmes**
   - Activer les alarmes
   - Choisir un son audible
   - Tester avec une alarme de test

3. **Sommeil**
   - Configurer votre plage de sommeil habituelle
   - Définir la durée cible (ex: 7-8h)

## 🧪 Tests Recommandés

### Test 1 : Alarme Simple
1. Créer une tâche ponctuelle dans 2 minutes
2. Attendre que l'alarme se déclenche
3. Vérifier le son et l'interface
4. Tester les boutons Stop et Snooze

### Test 2 : Persistance
1. Créer une tâche pour dans 10 minutes
2. Forcer l'arrêt de l'application
3. Attendre que l'alarme se déclenche
4. Vérifier qu'elle fonctionne toujours

### Test 3 : Redémarrage
1. Créer une tâche pour après le redémarrage
2. Redémarrer l'appareil
3. Attendre que l'alarme se déclenche
4. Vérifier qu'elle a été replanifiée

## 📚 Ressources

### Documentation
- [Cahier des charges](TODO.md) - Spécifications complètes
- [README](README.md) - Documentation technique
- [Architecture Android](https://developer.android.com/topic/architecture)

### APIs Utilisées
- [AlarmManager](https://developer.android.com/reference/android/app/AlarmManager)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)

## 🐛 Dépannage

### L'alarme ne se déclenche pas
- Vérifier les permissions
- Vérifier que l'app n'est pas optimisée pour la batterie
- Vérifier les paramètres du fabricant (Xiaomi, etc.)

### L'application crash au démarrage
- Vérifier les logs Android Studio
- Synchroniser les dépendances Gradle
- Nettoyer et rebuilder le projet

### Les notifications n'apparaissent pas
- Vérifier la permission POST_NOTIFICATIONS
- Vérifier les canaux de notification dans les paramètres
- Vérifier le mode Ne pas déranger

## 💡 Conseils de Développement

1. **Tester sur appareil réel** - Les alarmes peuvent se comporter différemment sur émulateur
2. **Logs détaillés** - Utiliser Logcat pour suivre le cycle de vie des alarmes
3. **Tests progressifs** - Tester chaque fonctionnalité individuellement
4. **Backup régulier** - Exporter les données régulièrement (quand implémenté)

## 📞 Support

Pour toute question ou problème :
- Consulter les logs dans Android Studio
- Vérifier le cahier des charges pour les spécifications
- Documenter les bugs rencontrés

---

**Version actuelle** : 1.0 (Phase 1 - Infrastructure de base)
**Dernière mise à jour** : Octobre 2024
