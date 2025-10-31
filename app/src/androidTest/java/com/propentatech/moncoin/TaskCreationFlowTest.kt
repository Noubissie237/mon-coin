package com.propentatech.moncoin

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test du flux complet de création de tâche
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskCreationFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun createPonctuelleTask_withPlageMode_success() {
        // Naviguer vers la création de tâche
        composeTestRule.onNodeWithContentDescription("Créer une tâche")
            .performClick()

        // Remplir le titre
        composeTestRule.onNodeWithText("Titre *")
            .performTextInput("Rendez-vous médecin")

        // Sélectionner type PONCTUELLE (déjà sélectionné par défaut)
        composeTestRule.onNodeWithText("Ponctuelle")
            .assertIsSelected()

        // Sélectionner la date (Aujourd'hui)
        composeTestRule.onNodeWithText("Aujourd'hui")
            .performClick()

        // Sélectionner mode PLAGE
        composeTestRule.onNodeWithText("Plage horaire")
            .performClick()

        // Définir les horaires (simulé - nécessite interaction avec TimePickerDialog)
        // Note: Les TimePickers nécessitent une interaction plus complexe

        // Sauvegarder
        composeTestRule.onNodeWithContentDescription("Sauvegarder")
            .performClick()

        // Vérifier le retour à l'écran d'accueil
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Tâches du jour")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun createDureeTask_success() {
        // Naviguer vers la création de tâche
        composeTestRule.onNodeWithContentDescription("Créer une tâche")
            .performClick()

        // Remplir le titre
        composeTestRule.onNodeWithText("Titre *")
            .performTextInput("Session de sport")

        // Sélectionner mode DUREE
        composeTestRule.onNodeWithText("Durée fixe")
            .performClick()

        // Ajuster la durée avec le slider
        composeTestRule.onNode(hasTestTag("duration_slider") or hasContentDescription("Durée"))
            .performTouchInput { swipeRight() }

        // Sauvegarder
        composeTestRule.onNodeWithContentDescription("Sauvegarder")
            .performClick()

        // Vérifier la création
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Session de sport")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun createTask_withoutTitle_showsError() {
        // Naviguer vers la création de tâche
        composeTestRule.onNodeWithContentDescription("Créer une tâche")
            .performClick()

        // Ne pas remplir le titre

        // Essayer de sauvegarder
        composeTestRule.onNodeWithContentDescription("Sauvegarder")
            .performClick()

        // Vérifier l'affichage du message d'erreur
        composeTestRule.onNodeWithText("Le titre est obligatoire")
            .assertIsDisplayed()
    }

    @Test
    fun createPeriodiqueTask_withDaysSelection_success() {
        // Naviguer vers la création de tâche
        composeTestRule.onNodeWithContentDescription("Créer une tâche")
            .performClick()

        // Remplir le titre
        composeTestRule.onNodeWithText("Titre *")
            .performTextInput("Sport hebdomadaire")

        // Sélectionner type PERIODIQUE
        composeTestRule.onNodeWithText("Périodique")
            .performClick()

        // Sélectionner des jours (Lundi, Mercredi, Vendredi)
        composeTestRule.onNodeWithText("LUN")
            .performClick()
        composeTestRule.onNodeWithText("MER")
            .performClick()
        composeTestRule.onNodeWithText("VEN")
            .performClick()

        // Sélectionner mode DUREE
        composeTestRule.onNodeWithText("Durée fixe")
            .performClick()

        // Sauvegarder
        composeTestRule.onNodeWithContentDescription("Sauvegarder")
            .performClick()

        // Vérifier la création
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Sport hebdomadaire")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
