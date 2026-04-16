package com.teachmeski.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.teachmeski.app.R
import com.teachmeski.app.ui.auth.LoginScreen
import com.teachmeski.app.ui.auth.SignupScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation<Route.AuthGraph>(startDestination = Route.Login) {
        composable<Route.Login> {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Route.Signup) },
                onNavigateToForgotPassword = { navController.navigate(Route.ForgotPassword) },
                onNavigateToVerifyEmail = { navController.navigate(Route.VerifyEmail) },
            )
        }
        composable<Route.Signup> {
            SignupScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToVerifyEmail = { navController.navigate(Route.VerifyEmail) },
            )
        }
        composable<Route.ForgotPassword> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.auth_forgot_password_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(24.dp),
                )
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                ) {
                    Text(stringResource(R.string.auth_forgot_password_back_to_login))
                }
            }
        }
        composable<Route.VerifyEmail> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.auth_verify_email_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(24.dp),
                )
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                ) {
                    Text(stringResource(R.string.auth_verify_email_back))
                }
            }
        }
    }
}
