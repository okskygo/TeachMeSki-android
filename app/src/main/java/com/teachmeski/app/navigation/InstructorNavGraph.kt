package com.teachmeski.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.teachmeski.app.ui.account.InstructorAccountScreen
import com.teachmeski.app.ui.chat.ChatRoomListScreen
import com.teachmeski.app.ui.explore.ExploreScreen
import com.teachmeski.app.ui.unlocked.UnlockedScreen

fun NavGraphBuilder.instructorNavGraph(navController: NavHostController) {
    navigation<Route.InstructorGraph>(startDestination = Route.Explore) {
        composable<Route.Explore> { ExploreScreen() }
        composable<Route.Unlocked> { UnlockedScreen() }
        composable<Route.ChatRoomList> { ChatRoomListScreen() }
        composable<Route.InstructorAccount> { InstructorAccountScreen() }
    }
}
