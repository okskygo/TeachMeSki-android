package com.teachmeski.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.teachmeski.app.ui.auth.LoginScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation<Route.AuthGraph>(startDestination = Route.Login) {
        composable<Route.Login> {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Route.Signup) },
            )
        }
        composable<Route.Signup> {
            LoginScreen()
        }
    }
}
