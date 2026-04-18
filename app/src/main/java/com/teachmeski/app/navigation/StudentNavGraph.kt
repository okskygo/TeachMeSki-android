package com.teachmeski.app.navigation

import androidx.compose.runtime.key
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.teachmeski.app.domain.model.UserRole
import com.teachmeski.app.ui.account.AccountScreen
import com.teachmeski.app.ui.account.AccountSettingsScreen
import com.teachmeski.app.ui.account.ContactScreen
import com.teachmeski.app.ui.account.LegalScreen
import com.teachmeski.app.ui.chat.ChatRoomListScreen
import com.teachmeski.app.ui.myrequests.MyRequestsScreen
import com.teachmeski.app.ui.myrequests.RequestDetailScreen
import com.teachmeski.app.ui.profile.InstructorDetailScreen
import com.teachmeski.app.ui.wizard.LessonRequestWizardScreen

fun NavGraphBuilder.studentNavGraph(
    navController: NavHostController,
    userRole: UserRole = UserRole.Student,
    onSwitchToInstructor: () -> Unit = {},
) {
    navigation<Route.StudentGraph>(startDestination = Route.MyRequests) {
        composable<Route.MyRequests> {
            MyRequestsScreen(
                onRequestClick = { id ->
                    navController.navigate(Route.RequestDetail(id))
                },
                onNewRequest = {
                    navController.navigate(Route.LessonRequestWizard) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<Route.RequestDetail> { entry ->
            val detailRoute = entry.toRoute<Route.RequestDetail>()
            key(detailRoute.id) {
                RequestDetailScreen(
                    onBack = { navController.popBackStack() },
                    onChatClick = { roomId ->
                        navController.navigate(Route.Chat(roomId))
                    },
                    onInstructorClick = { shortId ->
                        navController.navigate(Route.InstructorDetail(shortId))
                    },
                )
            }
        }
        composable<Route.InstructorDetail> {
            InstructorDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.LessonRequestWizard> {
            LessonRequestWizardScreen(
                onClose = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
            )
        }
        composable<Route.ChatRoomList> { ChatRoomListScreen() }
        composable<Route.Account> {
            AccountScreen(
                userRole = userRole,
                onAccountSettingsClick = { navController.navigate(Route.AccountSettings) },
                onNavigateToWizard = {
                    navController.navigate(Route.InstructorWizard()) {
                        launchSingleTop = true
                    }
                },
                onSwitchToInstructor = onSwitchToInstructor,
                onContactClick = { navController.navigate(Route.Contact) },
                onTermsClick = { navController.navigate(Route.Legal(type = "terms")) },
                onPrivacyClick = { navController.navigate(Route.Legal(type = "privacy")) },
                onSignedOut = { },
            )
        }
        composable<Route.AccountSettings> {
            AccountSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Contact> {
            ContactScreen(onBack = { navController.popBackStack() })
        }
        composable<Route.Legal> { entry ->
            val legalRoute = entry.toRoute<Route.Legal>()
            LegalScreen(
                type = legalRoute.type,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
