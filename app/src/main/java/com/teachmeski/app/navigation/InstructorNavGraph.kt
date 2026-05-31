package com.teachmeski.app.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.teachmeski.app.ui.account.ContactScreen
import com.teachmeski.app.ui.account.InstructorAccountScreen
import com.teachmeski.app.ui.account.LegalScreen
import com.teachmeski.app.ui.chat.ChatRoomListScreen
import com.teachmeski.app.ui.explore.ExploreDetailScreen
import com.teachmeski.app.ui.explore.ExploreScreen
import com.teachmeski.app.ui.explore.ExploreViewModel
import com.teachmeski.app.ui.profile.detail.InstructorDetailScreen
import com.teachmeski.app.ui.unlocked.UnlockedScreen

fun NavGraphBuilder.instructorNavGraph(
    navController: NavHostController,
    onSwitchToStudent: () -> Unit,
) {
    navigation<Route.InstructorGraph>(startDestination = Route.Explore) {
        composable<Route.Explore> { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(Route.InstructorGraph)
            }
            val sharedViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
            ExploreScreen(
                viewModel = sharedViewModel,
                onNavigateToChat = { roomId ->
                    navController.navigate(Route.Chat(roomId))
                },
                onNavigateToWallet = { navController.navigate(Route.Wallet) },
                onNavigateToAccountSettings = {
                    navController.navigate(Route.InstructorAccountSettings)
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Route.ExploreDetail(id))
                },
            )
        }
        composable<Route.ExploreDetail> { entry ->
            val detailRoute = entry.toRoute<Route.ExploreDetail>()
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(Route.InstructorGraph)
            }
            val sharedViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
            ExploreDetailScreen(
                requestId = detailRoute.id,
                viewModel = sharedViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToChat = { roomId ->
                    navController.navigate(Route.Chat(roomId))
                },
                onNavigateToWallet = { navController.navigate(Route.Wallet) },
                onNavigateToAccountSettings = {
                    navController.navigate(Route.InstructorAccountSettings)
                },
            )
        }
        composable<Route.Unlocked> {
            UnlockedScreen(
                onNavigateToChat = { roomId ->
                    navController.navigate(Route.Chat(roomId))
                },
                onNavigateToExplore = {
                    navController.navigate(Route.Explore) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        composable<Route.ChatRoomList> {
            ChatRoomListScreen(
                isInstructorView = true,
                onRoomClick = { roomId ->
                    navController.navigate(Route.Chat(roomId))
                },
                onEmptyCtaClick = {
                    navController.navigate(Route.Explore) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        composable<Route.InstructorAccount> {
            InstructorAccountScreen(
                onSwitchToStudent = onSwitchToStudent,
                onNavigateToAccountSettings = {
                    navController.navigate(Route.InstructorAccountSettings)
                },
                onNavigateToWallet = { navController.navigate(Route.Wallet) },
                onNavigateToProfile = { navController.navigate(Route.InstructorProfile) },
                onNavigateToContact = { navController.navigate(Route.Contact) },
                onNavigateToTerms = { navController.navigate(Route.Legal(type = "terms")) },
                onNavigateToPrivacy = { navController.navigate(Route.Legal(type = "privacy")) },
                onSignedOut = { },
            )
        }
        composable<Route.InstructorDetail> {
            InstructorDetailScreen(
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
