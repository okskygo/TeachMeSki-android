package com.teachmeski.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teachmeski.app.navigation.AppNavGraph
import com.teachmeski.app.navigation.Route
import com.teachmeski.app.ui.component.ActiveRole
import com.teachmeski.app.ui.component.TmsBottomBar
import com.teachmeski.app.ui.theme.TeachMeSkiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeachMeSkiTheme {
                TeachMeSkiRoot()
            }
        }
    }
}

@Composable
private fun TeachMeSkiRoot() {
    val navController = rememberNavController()
    var activeRole by rememberSaveable { mutableStateOf(ActiveRole.Student) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestRoute = navBackStackEntry?.destination?.route

    val currentTabRoute: Route? =
        when {
            currentDestRoute?.contains("MyRequests") == true -> Route.MyRequests
            currentDestRoute?.contains("Explore") == true -> Route.Explore
            currentDestRoute?.contains("Unlocked") == true -> Route.Unlocked
            currentDestRoute?.contains("ChatRoomList") == true -> Route.ChatRoomList
            currentDestRoute?.contains("Account") == true && activeRole == ActiveRole.Student -> Route.Account
            currentDestRoute?.contains("InstructorAccount") == true -> Route.InstructorAccount
            else -> null
        }

    Scaffold(
        bottomBar = {
            TmsBottomBar(
                activeRole = activeRole,
                currentRoute = currentTabRoute,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            activeRole = activeRole,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
