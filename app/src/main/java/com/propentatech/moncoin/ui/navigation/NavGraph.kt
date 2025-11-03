package com.propentatech.moncoin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.propentatech.moncoin.ui.screen.home.HomeScreen
import com.propentatech.moncoin.ui.screen.settings.SettingsScreen
import com.propentatech.moncoin.ui.screen.task.create.TaskCreateScreen
import com.propentatech.moncoin.ui.screen.history.HistoryScreen
import com.propentatech.moncoin.ui.screen.notes.NotesScreen
import com.propentatech.moncoin.ui.screen.notes.NoteDetailScreen
import com.propentatech.moncoin.ui.screen.statistics.StatisticsScreen
import com.propentatech.moncoin.ui.screen.task.detail.TaskDetailScreen
import com.propentatech.moncoin.ui.screen.sleep.SleepScheduleScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTaskCreate = {
                    navController.navigate(Screen.TaskCreate.route)
                },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToNotes = {
                    navController.navigate(Screen.Notes.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(
            route = Screen.TaskCreate.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            TaskCreateScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) {
            TaskDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { taskId ->
                    navController.navigate(Screen.TaskCreate.createRoute(taskId))
                }
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToNotes = {
                    navController.navigate(Screen.Notes.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }
        
        composable(Screen.Notes.route) {
            NotesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNoteDetail = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                onNavigateToNoteCreate = {
                    navController.navigate("note_detail/new")
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }
        
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) {
            NoteDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToNotes = {
                    navController.navigate(Screen.Notes.route)
                }
            )
        }
        
        composable(Screen.SleepSchedule.route) {
            SleepScheduleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSleepSchedule = {
                    navController.navigate(Screen.SleepSchedule.route)
                },
                onNavigateToThemeSelection = {
                    navController.navigate(Screen.ThemeSelection.route)
                }
            )
        }
        
        composable(Screen.ThemeSelection.route) {
            com.propentatech.moncoin.ui.screen.settings.ThemeSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
