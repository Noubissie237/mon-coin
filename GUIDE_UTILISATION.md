# 📱 Guide d'Utilisation - Mon Coin

## 🏠 Écran d'Accueil (HomeScreen)

### Comment modifier ou supprimer une tâche ?

#### Étape 1 : Cliquer sur la tâche
Sur l'écran d'accueil, vous verrez vos tâches affichées sous forme de cartes. Chaque carte de tâche contient maintenant :
- Le titre de la tâche
- La description
- Un texte bleu : **"Appuyez pour voir les détails"**
- Une flèche → à droite

**👆 Cliquez simplement sur la carte de la tâche** pour ouvrir l'écran de détail.

#### Étape 2 : Écran de Détail
Une fois dans l'écran de détail, vous avez **3 façons** d'accéder aux actions :

##### Option A : Boutons dans la barre du haut
- **Icône crayon (✏️)** : Modifier la tâche
- **Icône poubelle (🗑️)** : Supprimer la tâche

##### Option B : Section "Actions" en bas
Faites défiler vers le bas de l'écran pour voir une carte "Actions" avec :
- **Bouton bleu "Modifier la tâche"** : Ouvre l'éditeur
- **Bouton rouge "Supprimer la tâche"** : Supprime la tâche

### Flux Complet

```
HomeScreen (Liste des tâches)
    ↓ [Cliquer sur une tâche]
TaskDetailScreen (Détails + Occurrences)
    ↓ [Cliquer sur "Modifier"]
TaskEditScreen (Édition)
    OU
    ↓ [Cliquer sur "Supprimer"]
Confirmation → Suppression → Retour à HomeScreen
```

---

## 🎨 Interface Améliorée

### Titre "Mon Coin"
- **"Mon"** : Couleur bleu clair (#38B6FF)
- **"Coin"** : Couleur blanche

### Cartes de Tâches
- **Tâches en cours** : Fond bleu clair avec icône ▶️
- **Tâches programmées** : Fond normal
- **Indicateur visuel** : Texte "Appuyez pour voir les détails" en bleu
- **Flèche** : → à droite pour indiquer que c'est cliquable

---

## 📋 Actions Disponibles

### Sur une Tâche

#### 1. Voir les Détails
- **Où** : HomeScreen
- **Comment** : Cliquer sur la carte de tâche
- **Résultat** : Affiche tous les détails et occurrences

#### 2. Modifier une Tâche
- **Où** : TaskDetailScreen
- **Comment** : 
  - Cliquer sur l'icône ✏️ en haut
  - OU cliquer sur le bouton "Modifier la tâche" en bas
- **Résultat** : Ouvre l'éditeur (à implémenter)

#### 3. Supprimer une Tâche
- **Où** : TaskDetailScreen
- **Comment** : 
  - Cliquer sur l'icône 🗑️ en haut
  - OU cliquer sur le bouton "Supprimer la tâche" en bas
- **Résultat** : Demande confirmation puis supprime

#### 4. Gérer les Occurrences
- **Où** : TaskDetailScreen
- **Comment** : Utiliser les boutons sur chaque occurrence
- **Actions** :
  - ✅ Terminer
  - ❌ Annuler

---

## 🔔 Cycle de Vie d'une Tâche

### 1. Création
- Cliquer sur le bouton **+** flottant
- Remplir le formulaire
- Sauvegarder

### 2. Rappel (X minutes avant)
- Notification push
- Cliquer dessus ouvre l'application

### 3. Début de la Tâche
- **Écran plein écran** s'affiche automatiquement
- 2 options :
  - **"Démarrer la tâche"** : La tâche passe en cours
  - **"Plus tard"** : Ferme l'écran sans démarrer

### 4. Pendant la Tâche
- La tâche apparaît dans "Tâches en cours"
- Fond bleu clair avec icône ▶️
- Cliquable pour voir les détails

### 5. Fin de la Tâche
- **Alarme sonore** se déclenche
- Écran plein écran avec son
- 2 options :
  - **"Stop"** : Marque comme terminée
  - **"Snooze"** : Reporte de 10 minutes

### 6. Après la Tâche
- Visible dans l'historique
- État : Terminée, Manquée ou Annulée

---

## 🎯 Raccourcis Rapides

### Depuis HomeScreen
| Action | Comment |
|--------|---------|
| Créer une tâche | Bouton **+** flottant |
| Voir détails | Cliquer sur la carte |
| Aller à l'historique | Onglet "Historique" en bas |
| Voir les notes | Onglet "Notes" en bas |
| Voir les stats | Onglet "Statistiques" en bas |
| Paramètres | Icône ⚙️ en haut à droite |

### Depuis TaskDetailScreen
| Action | Comment |
|--------|---------|
| Retour | Flèche ← en haut à gauche |
| Modifier | Icône ✏️ OU bouton "Modifier" |
| Supprimer | Icône 🗑️ OU bouton "Supprimer" |
| Terminer occurrence | Bouton "Terminer" sur l'occurrence |
| Annuler occurrence | Bouton "Annuler" sur l'occurrence |

---

## 💡 Conseils d'Utilisation

### Pour Modifier une Tâche
1. Trouvez la tâche sur l'écran d'accueil
2. **Appuyez sur la carte** (vous verrez le texte bleu "Appuyez pour voir les détails")
3. Dans l'écran de détail, **faites défiler vers le bas**
4. Cliquez sur le **bouton bleu "Modifier la tâche"**

### Pour Supprimer une Tâche
1. Trouvez la tâche sur l'écran d'accueil
2. **Appuyez sur la carte**
3. Dans l'écran de détail, **faites défiler vers le bas**
4. Cliquez sur le **bouton rouge "Supprimer la tâche"**
5. Confirmez la suppression

### Pour Voir l'État d'une Tâche
- **Fond bleu clair** = Tâche en cours
- **Fond normal** = Tâche programmée
- **Icône ▶️** = Tâche en cours d'exécution

---

## ❓ Questions Fréquentes

### Q : Comment savoir qu'une carte est cliquable ?
**R :** Vous verrez :
- Un texte bleu "Appuyez pour voir les détails"
- Une flèche → à droite de la carte
- Les tâches en cours ont un fond bleu clair

### Q : Où sont les boutons Modifier et Supprimer ?
**R :** Dans l'écran de détail de la tâche :
- En haut : icônes ✏️ et 🗑️
- En bas : boutons "Modifier la tâche" et "Supprimer la tâche"

### Q : Puis-je modifier une tâche en cours ?
**R :** Oui ! Cliquez sur la tâche, puis sur "Modifier la tâche"

### Q : Que se passe-t-il si je supprime une tâche ?
**R :** Toutes les occurrences (passées et futures) sont supprimées. Une confirmation est demandée avant la suppression définitive.

### Q : Comment annuler une occurrence sans supprimer la tâche ?
**R :** Dans l'écran de détail, cliquez sur "Annuler" pour l'occurrence spécifique. La tâche reste active pour les autres occurrences.

---

## 🎨 Personnalisation

### Couleurs de l'Application
- **Bleu principal** : #38B6FF (titre "Mon")
- **Blanc** : Titre "Coin"
- **Bleu clair** : Tâches en cours
- **Rouge** : Actions de suppression

### Icônes
- ➕ Créer
- ✏️ Modifier
- 🗑️ Supprimer
- ▶️ En cours
- ✅ Terminer
- ❌ Annuler
- ⚙️ Paramètres
- ← Retour

---

## 📞 Support

Si vous ne trouvez pas comment faire quelque chose :
1. Cherchez l'icône correspondante en haut de l'écran
2. Faites défiler vers le bas pour voir toutes les options
3. Consultez ce guide

**Astuce** : Presque tout dans l'application est cliquable ! Si vous voyez une carte ou un bouton, essayez de cliquer dessus.

---

**Version** : 1.1.0  
**Dernière mise à jour** : Octobre 2024
