package com.teachmeski.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teachmeski.app.navigation.AppNavGraph
import com.teachmeski.app.navigation.Route
import com.teachmeski.app.ui.MainUiState
import com.teachmeski.app.ui.MainViewModel
import com.teachmeski.app.domain.model.UserRole
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
private fun TeachMeSkiRoot(mainViewModel: MainViewModel = hiltViewModel()) {
    val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()

    // Keep the last resolved (non-Loading) state so that transient Loading
    // emissions (e.g. Supabase re-emitting Initializing after the app returns
    // from background) do NOT tear down AuthenticatedApp and rebuild the
    // NavController, which would reset the user's current screen.
    var lastResolved by remember { mutableStateOf<MainUiState?>(null) }
    LaunchedEffect(mainState) {
        if (mainState !is MainUiState.Loading) {
            lastResolved = mainState
        }
    }

    val resolved = lastResolved
    if (resolved == null) {
        // Initial cold start — no state resolved yet.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    when (resolved) {
        is MainUiState.Loading -> Unit // unreachable by construction
        is MainUiState.Unauthenticated -> {
            AuthenticatedApp(
                isAuthenticated = false,
                activeRole = ActiveRole.Student,
                userRole = UserRole.Student,
                unreadCount = 0,
                onSwitchToStudent = { mainViewModel.switchRole(ActiveRole.Student) },
                onSwitchToInstructor = { mainViewModel.switchRole(ActiveRole.Instructor) },
                onRefreshUnreadCount = { mainViewModel.refreshUnreadCount() },
            )
        }
        is MainUiState.Authenticated -> {
            AuthenticatedApp(
                isAuthenticated = true,
                activeRole = resolved.activeRole,
                userRole = resolved.userRole,
                unreadCount = resolved.unreadCount,
                onSwitchToStudent = { mainViewModel.switchRole(ActiveRole.Student) },
                onSwitchToInstructor = { mainViewModel.switchRole(ActiveRole.Instructor) },
                onRefreshUnreadCount = { mainViewModel.refreshUnreadCount() },
            )
        }
    }
}

@Composable
private fun AuthenticatedApp(
    isAuthenticated: Boolean,
    activeRole: ActiveRole,
    userRole: UserRole,
    unreadCount: Int,
    onSwitchToStudent: () -> Unit,
    onSwitchToInstructor: () -> Unit,
    onRefreshUnreadCount: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestRoute = navBackStackEntry?.destination?.route

    val isOnAuthScreen = currentDestRoute?.let { route ->
        route.contains("Login") || route.contains("Signup") || route.contains("Register") ||
            route.contains("ForgotPassword") || route.contains("VerifyEmail")
    } ?: !isAuthenticated

    val fullscreenRoutes =
        setOf(
            Route.LessonRequestWizard::class,
            Route.InstructorWizard::class,
            Route.Chat::class,
        )
    val isFullscreenRoute =
        currentDestRoute?.let { route ->
            fullscreenRoutes.any { kClass ->
                val simple = kClass.simpleName ?: return@any false
                // Match Route.X exactly OR Route.X/... OR Route.X? (arg separators)
                // to avoid false-positives like "ChatRoomList" matching "Chat".
                route.endsWith(".$simple") ||
                    route.contains(".$simple/") ||
                    route.contains(".$simple?")
            }
        } == true

    val showBottomBar = isAuthenticated && !isOnAuthScreen && !isFullscreenRoute

    fun String.matchesRoute(simpleName: String): Boolean =
        endsWith(".$simpleName") ||
            contains(".$simpleName/") ||
            contains(".$simpleName?")

    val currentTabRoute: Route? = when {
        currentDestRoute?.matchesRoute("MyRequests") == true -> Route.MyRequests
        currentDestRoute?.matchesRoute("Explore") == true -> Route.Explore
        currentDestRoute?.matchesRoute("Unlocked") == true -> Route.Unlocked
        currentDestRoute?.matchesRoute("ChatRoomList") == true -> Route.ChatRoomList
        currentDestRoute?.matchesRoute("InstructorAccount") == true -> Route.InstructorAccount
        currentDestRoute?.matchesRoute("Account") == true &&
            activeRole == ActiveRole.Student -> Route.Account
        else -> null
    }

    // Only reset the nav back stack when the (isAuthenticated, activeRole)
    // tuple ACTUALLY changes — not on every recomposition. Without this guard,
    // returning from background can re-run this effect and pop the user back
    // to the home tab, losing their current screen.
    var lastHandled by remember {
        mutableStateOf<Pair<Boolean, ActiveRole>?>(null)
    }
    LaunchedEffect(isAuthenticated, activeRole) {
        val current = isAuthenticated to activeRole
        if (lastHandled == current) return@LaunchedEffect
        lastHandled = current

        if (isAuthenticated) {
            val startRoute: Route = when (activeRole) {
                ActiveRole.Student -> Route.StudentGraph
                ActiveRole.Instructor -> Route.InstructorGraph
            }
            navController.navigate(startRoute) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate(Route.AuthGraph) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        // Fullscreen routes (Chat, wizards) handle system bar insets themselves
        // via their own TopAppBar / bottom bars. Zero out here so the outer
        // Scaffold does NOT reserve status/nav bar space that would leak the
        // default background color behind inner content.
        contentWindowInsets = if (isFullscreenRoute) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
        bottomBar = {
            if (showBottomBar) {
                TmsBottomBar(
                    activeRole = activeRole,
                    currentRoute = currentTabRoute,
                    unreadCount = unreadCount,
                    onTabSelected = { route ->
                        if (route == Route.ChatRoomList) {
                            onRefreshUnreadCount()
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            isAuthenticated = isAuthenticated,
            activeRole = activeRole,
            userRole = userRole,
            onSwitchToStudent = onSwitchToStudent,
            onSwitchToInstructor = onSwitchToInstructor,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
