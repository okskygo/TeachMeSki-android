package com.teachmeski.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.teachmeski.app.R
import com.teachmeski.app.ui.account.AccountScreen
import com.teachmeski.app.ui.account.ContactScreen
import com.teachmeski.app.ui.account.LegalScreen
import com.teachmeski.app.ui.chat.ChatRoomListScreen
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.myrequests.MyRequestsScreen
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.ui.wizard.LessonRequestWizardScreen

fun NavGraphBuilder.studentNavGraph(navController: NavHostController) {
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
                RequestDetailPlaceholderScreen(onBack = { navController.popBackStack() })
            }
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
                onContactClick = { navController.navigate(Route.Contact) },
                onTermsClick = { navController.navigate(Route.Legal(type = "terms")) },
                onPrivacyClick = { navController.navigate(Route.Legal(type = "privacy")) },
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

@Composable
private fun RequestDetailPlaceholderScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.request_detail_screen_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = TmsColor.Primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.common_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
