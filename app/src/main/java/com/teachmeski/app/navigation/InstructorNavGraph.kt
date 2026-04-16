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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.teachmeski.app.R
import com.teachmeski.app.ui.account.ContactScreen
import com.teachmeski.app.ui.account.InstructorAccountScreen
import com.teachmeski.app.ui.account.LegalScreen
import com.teachmeski.app.ui.chat.ChatRoomListScreen
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.explore.ExploreScreen
import com.teachmeski.app.ui.profile.InstructorProfileScreen
import com.teachmeski.app.ui.unlocked.UnlockedScreen
import com.teachmeski.app.ui.wallet.CreditHistoryScreen
import com.teachmeski.app.ui.wallet.WalletScreen

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
        composable<Route.Chat> {
            InstructorChatPlaceholderScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.InstructorAccount> {
            InstructorAccountScreen(
                onNavigateToWallet = { navController.navigate(Route.Wallet) },
                onNavigateToProfile = { navController.navigate(Route.InstructorProfile) },
                onNavigateToContact = { navController.navigate(Route.Contact) },
                onNavigateToLegal = { navController.navigate(Route.Legal(type = "terms")) },
                onSignedOut = { },
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
        composable<Route.InstructorProfile> {
            InstructorProfileScreen(
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

@Composable
private fun InstructorChatPlaceholderScreen(onBack: () -> Unit) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.nav_inbox),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.placeholder_inbox),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
