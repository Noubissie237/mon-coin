package com.propentatech.moncoin

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.propentatech.moncoin.data.repository.OccurrenceRepository
import com.propentatech.moncoin.data.repository.TaskRepository
import com.propentatech.moncoin.data.model.TaskState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Test du flux complet d'une tâche :
 * Création -> Démarrage -> Exécution -> Alarme -> Complétion
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskCompleteFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var occurrenceRepository: OccurrenceRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun completeDureeTaskFlow_createStartAndComplete() = runBlocking {
        val taskTitle = "Test Complete Flow ${System.currentTimeMillis()}"

        // 1. Créer une tâche DUREE
        composeTestRule.onNodeWithContentDescription("Créer une tâche")
            .performClick()

        composeTestRule.onNodeWithText("Titre *")
            .performTextInput(taskTitle)

        composeTestRule.onNodeWithText("Durée fixe")
            .performClick()

        composeTestRule.onNodeWithContentDescription("Sauvegarder")
            .performClick()

        // Attendre la création
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText(taskTitle)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Vérifier que la tâche est créée dans la base de données
        val tasks = taskRepository.getAllTasks().first()
        val createdTask = tasks.find { it.title == taskTitle }
        assertNotNull("La tâche devrait être créée", createdTask)
        assertEquals("La tâche devrait être en état SCHEDULED", TaskState.SCHEDULED, createdTask?.state)

        // 3. Démarrer la tâche
        composeTestRule.onNodeWithText(taskTitle)
            .performClick()

        composeTestRule.onNodeWithText("Démarrer")
            .performClick()

        // Attendre le démarrage
        Thread.sleep(1000)

        // 4. Vérifier que l'occurrence est créée
        val today = java.time.LocalDateTime.now().toLocalDate()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.atTime(23, 59, 59)
        val occurrences = occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay).first()
        val taskOccurrence = occurrences.find { it.taskId == createdTask?.id }
        assertNotNull("Une occurrence devrait être créée", taskOccurrence)
        assertEquals("L'occurrence devrait être en état RUNNING", TaskState.RUNNING, taskOccurrence?.state)

        // 5. Vérifier que la tâche apparaît dans "Tâches en cours" ou "Tâches du jour"
        composeTestRule.onNodeWithContentDescription("Retour")
            .performClick()

        composeTestRule.onNodeWithText(taskTitle)
            .assertIsDisplayed()

        // 6. Compléter la tâche
        composeTestRule.onNodeWithText(taskTitle)
            .performClick()

        composeTestRule.onNodeWithText("Terminer")
            .performClick()

        // Attendre la complétion
        Thread.sleep(1000)

        // 7. Vérifier que la tâche est marquée comme COMPLETED
        val updatedTask = taskRepository.getTaskById(createdTask!!.id)
        assertEquals("La tâche devrait être COMPLETED", TaskState.COMPLETED, updatedTask?.state)
    }

    @Test
    fun plageTaskFlow_automaticStart() = runBlocking {
        val taskTitle = "Test Plage Flow ${System.currentTimeMillis()}"

        // 1. Créer une tâche PLAGE pour dans 1 minute
        composeTestRule.onNodeWithContentDescription("Créer une tâche")
            .performClick()

        composeTestRule.onNodeWithText("Titre *")
            .performTextInput(taskTitle)

        composeTestRule.onNodeWithText("Plage horaire")
            .performClick()

        // Note: Configuration des horaires nécessite interaction avec TimePickerDialog
        // Pour un test complet, il faudrait simuler la sélection d'horaires

        composeTestRule.onNodeWithContentDescription("Sauvegarder")
            .performClick()

        // Attendre la création
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText(taskTitle)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Vérifier que la tâche est créée
        val tasks = taskRepository.getAllTasks().first()
        val createdTask = tasks.find { it.title == taskTitle }
        assertNotNull("La tâche devrait être créée", createdTask)

        // 3. Vérifier qu'une occurrence est créée automatiquement
        val today = java.time.LocalDateTime.now().toLocalDate()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.atTime(23, 59, 59)
        val occurrences = occurrenceRepository.getOccurrencesBetween(startOfDay, endOfDay).first()
        val taskOccurrence = occurrences.find { it.taskId == createdTask?.id }
        assertNotNull("Une occurrence devrait être créée automatiquement", taskOccurrence)
    }

    @Test
    fun taskEdition_preservesData() = runBlocking {
        val originalTitle = "Original Task ${System.currentTimeMillis()}"
        val updatedTitle = "Updated Task ${System.currentTimeMillis()}"

        // 1. Créer une tâche
        composeTestRule.onNodeWithContentDescription("Créer une tâche")
            .performClick()

        composeTestRule.onNodeWithText("Titre *")
            .performTextInput(originalTitle)

        composeTestRule.onNodeWithContentDescription("Sauvegarder")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText(originalTitle)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Ouvrir les détails de la tâche
        composeTestRule.onNodeWithText(originalTitle)
            .performClick()

        // 3. Cliquer sur Modifier
        composeTestRule.onNodeWithContentDescription("Modifier")
            .performClick()

        // 4. Vérifier que le formulaire est prérempli
        composeTestRule.onNodeWithText(originalTitle)
            .assertIsDisplayed()

        // 5. Modifier le titre
        composeTestRule.onNodeWithText(originalTitle)
            .performTextClearance()
        composeTestRule.onNodeWithText("Titre *")
            .performTextInput(updatedTitle)

        // 6. Sauvegarder
        composeTestRule.onNodeWithContentDescription("Sauvegarder")
            .performClick()

        // 7. Vérifier que la modification est enregistrée
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText(updatedTitle)
                .fetchSemanticsNodes().isNotEmpty()
        }

        val tasks = taskRepository.getAllTasks().first()
        val updatedTask = tasks.find { it.title == updatedTitle }
        assertNotNull("La tâche modifiée devrait exister", updatedTask)
        assertNull("L'ancienne tâche ne devrait plus exister", tasks.find { it.title == originalTitle })
    }
}
