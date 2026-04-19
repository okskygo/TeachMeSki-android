package com.teachmeski.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.UserRole
import com.teachmeski.app.ui.account.InstructorAccountSettingsScreen
import com.teachmeski.app.ui.component.ActiveRole
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.instructorwizard.InstructorWizardScreen
import com.teachmeski.app.ui.profile.InstructorDetailScreen
import com.teachmeski.app.ui.profile.InstructorProfileScreen
import com.teachmeski.app.ui.chat.ChatScreen
import com.teachmeski.app.ui.wallet.CreditHistoryScreen
import com.teachmeski.app.ui.wallet.WalletScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isAuthenticated: Boolean,
    activeRole: ActiveRole,
    userRole: UserRole = UserRole.Student,
    onSwitchToStudent: () -> Unit,
    onSwitchToInstructor: () -> Unit,
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
        studentNavGraph(navController, userRole = userRole, onSwitchToInstructor = onSwitchToInstructor)
        instructorNavGraph(navController, onSwitchToStudent = onSwitchToStudent)

        composable<Route.InstructorAccountSettings> {
            InstructorAccountSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.InstructorProfile> {
            InstructorProfileScreen(
                onBack = { navController.popBackStack() },
                onPreview = { shortId ->
                    navController.navigate(Route.InstructorDetail(shortId))
                },
            )
        }
        composable<Route.InstructorDetail> {
            InstructorDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Wallet> {
            WalletScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCreditHistory = { navController.navigate(Route.CreditHistory) },
            )
        }
        composable<Route.CreditHistory> {
            CreditHistoryScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.InstructorWizard> { entry ->
            val wizardRoute = entry.toRoute<Route.InstructorWizard>()
            InstructorWizardScreen(
                isGuestMode = wizardRoute.isGuestMode,
                onClose = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack(Route.InstructorGraph, inclusive = false)
                },
            )
        }
        composable<Route.Chat> {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInstructor = { shortId ->
                    navController.navigate(Route.InstructorDetail(shortId))
                },
            )
        }
    }
}
