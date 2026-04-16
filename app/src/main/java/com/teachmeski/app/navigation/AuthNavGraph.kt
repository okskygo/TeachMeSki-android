package com.teachmeski.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.teachmeski.app.ui.auth.ForgotPasswordScreen
import com.teachmeski.app.ui.auth.LoginScreen
import com.teachmeski.app.ui.auth.SignupScreen
import com.teachmeski.app.ui.auth.VerifyEmailScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation<Route.AuthGraph>(startDestination = Route.Login) {
        composable<Route.Login> {
            LoginScreen(
                onNavigateToSignup = {
                    navController.navigate(Route.Signup) {
                        launchSingleTop = true
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Route.ForgotPassword) {
                        launchSingleTop = true
                    }
                },
                onNavigateToVerifyEmail = { email ->
                    navController.navigate(Route.VerifyEmail(email)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<Route.Signup> {
            SignupScreen(
                onNavigateToLogin = {
                    navController.popBackStack(Route.Login, inclusive = false)
                },
                onNavigateToVerifyEmail = { email ->
                    navController.navigate(Route.VerifyEmail(email)) {
                        popUpTo(Route.Login) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<Route.ForgotPassword> {
            ForgotPasswordScreen(
                onNavigateToLogin = {
                    navController.popBackStack(Route.Login, inclusive = false)
                },
            )
        }
        composable<Route.VerifyEmail> {
            VerifyEmailScreen(
                onBack = {
                    navController.popBackStack(Route.Login, inclusive = false)
                },
            )
        }
    }
}
