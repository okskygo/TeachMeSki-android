package com.teachmeski.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.teachmeski.app.ui.account.AccountScreen
import com.teachmeski.app.ui.chat.ChatRoomListScreen
import com.teachmeski.app.ui.myrequests.MyRequestsScreen

fun NavGraphBuilder.studentNavGraph(navController: NavHostController) {
    navigation<Route.StudentGraph>(startDestination = Route.MyRequests) {
        composable<Route.MyRequests> { MyRequestsScreen() }
        composable<Route.ChatRoomList> { ChatRoomListScreen() }
        composable<Route.Account> { AccountScreen() }
    }
}
