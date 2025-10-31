package com.propentatech.moncoin.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object TaskCreate : Screen("task_create")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object History : Screen("history")
    object Notes : Screen("notes")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}
