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
import com.teachmeski.app.R
import com.teachmeski.app.ui.account.InstructorAccountScreen
import com.teachmeski.app.ui.chat.ChatRoomListScreen
import com.teachmeski.app.ui.component.TmsTopBar
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
        composable<Route.Chat> {
            InstructorChatPlaceholderScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.InstructorAccount> { InstructorAccountScreen() }
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
