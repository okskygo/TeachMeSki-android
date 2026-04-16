package com.teachmeski.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.teachmeski.app.ui.component.ActiveRole

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isAuthenticated: Boolean,
    activeRole: ActiveRole,
    modifier: Modifier = Modifier,
) {
    val startDestination: Route = if (!isAuthenticated) {
        Route.AuthGraph
    } else {
        when (activeRole) {
            ActiveRole.Student -> Route.StudentGraph
            ActiveRole.Instructor -> Route.InstructorGraph
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        authNavGraph(navController)
        studentNavGraph(navController)
        instructorNavGraph(navController)
    }
}
