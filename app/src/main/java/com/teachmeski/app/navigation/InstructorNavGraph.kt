package com.teachmeski.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.teachmeski.app.ui.account.ContactScreen
import com.teachmeski.app.ui.account.InstructorAccountScreen
import com.teachmeski.app.ui.account.LegalScreen
import com.teachmeski.app.ui.chat.ChatRoomListScreen
import com.teachmeski.app.ui.explore.ExploreScreen
import com.teachmeski.app.ui.unlocked.UnlockedScreen

fun NavGraphBuilder.instructorNavGraph(navController: NavHostController) {
    navigation<Route.InstructorGraph>(startDestination = Route.Explore) {
        composable<Route.Explore> {
            ExploreScreen(
                onNavigateToChat = { roomId ->
                    navController.navigate(Route.Chat(roomId))
                },
            )
        }
        composable<Route.Unlocked> {
            UnlockedScreen(
                onNavigateToChat = { roomId ->
                    navController.navigate(Route.Chat(roomId))
                },
            )
        }
        composable<Route.ChatRoomList> { ChatRoomListScreen() }
        composable<Route.InstructorAccount> {
            InstructorAccountScreen(
                onNavigateToWallet = { navController.navigate(Route.Wallet) },
                onNavigateToProfile = { navController.navigate(Route.InstructorProfile) },
                onNavigateToWizard = {
                    navController.navigate(Route.InstructorWizard) {
                        launchSingleTop = true
                    }
                },
                onNavigateToContact = { navController.navigate(Route.Contact) },
                onNavigateToLegal = { navController.navigate(Route.Legal(type = "terms")) },
                onSignedOut = { },
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
