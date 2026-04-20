package com.teachmeski.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teachmeski.app.domain.model.UserRole
import com.teachmeski.app.navigation.AppNavGraph
import com.teachmeski.app.navigation.Route
import com.teachmeski.app.notifications.NotificationDeepLinkEvent
import com.teachmeski.app.notifications.NotificationEvents
import com.teachmeski.app.notifications.NotificationIntentExtras
import com.teachmeski.app.ui.MainUiState
import com.teachmeski.app.ui.MainViewModel
import com.teachmeski.app.ui.component.ActiveRole
import com.teachmeski.app.ui.component.TmsBottomBar
import com.teachmeski.app.ui.theme.TeachMeSkiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.teachmeski.app.notifications.NotificationDeepLinkBus

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationDeepLinkBus: NotificationDeepLinkBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)
        setContent {
            TeachMeSkiTheme {
                TeachMeSkiRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val event = intent?.getStringExtra(NotificationIntentExtras.EVENT) ?: return
        notificationDeepLinkBus.emit(
            NotificationDeepLinkEvent(
                event = event,
                roomId = intent.getStringExtra(NotificationIntentExtras.ROOM_ID),
                requestId = intent.getStringExtra(NotificationIntentExtras.REQUEST_ID),
                transactionId = intent.getStringExtra(NotificationIntentExtras.TRANSACTION_ID),
            ),
        )
        // Consume so a configuration change doesn't re-fire the deep link
        intent.removeExtra(NotificationIntentExtras.EVENT)
    }
}

@Composable
private fun TeachMeSkiRoot(mainViewModel: MainViewModel = hiltViewModel()) {
    val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()

    var lastResolved by remember { mutableStateOf<MainUiState?>(null) }
    LaunchedEffect(mainState) {
        if (mainState !is MainUiState.Loading) {
            lastResolved = mainState
        }
    }

    val resolved = lastResolved
    if (resolved == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    when (resolved) {
        is MainUiState.Loading -> Unit
        is MainUiState.Unauthenticated -> {
            AuthenticatedApp(
                isAuthenticated = false,
                activeRole = ActiveRole.Student,
                userRole = UserRole.Student,
                unreadCount = 0,
                onSwitchToStudent = { mainViewModel.switchRole(ActiveRole.Student) },
                onSwitchToInstructor = { mainViewModel.switchRole(ActiveRole.Instructor) },
                onRefreshUnreadCount = { mainViewModel.refreshUnreadCount() },
                mainViewModel = mainViewModel,
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
                mainViewModel = mainViewModel,
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
    mainViewModel: MainViewModel,
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

    // Request POST_NOTIFICATIONS on Android 13+ once authenticated
    if (isAuthenticated) {
        RequestNotificationPermissionOnce()
    }

    // Handle incoming notification deep links once we are authenticated
    if (isAuthenticated) {
        HandleNotificationDeepLinks(
            navController = navController,
            activeRole = activeRole,
            onSwitchToStudent = onSwitchToStudent,
            onSwitchToInstructor = onSwitchToInstructor,
            mainViewModel = mainViewModel,
        )
    }

    Scaffold(
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

@Composable
private fun RequestNotificationPermissionOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = androidx.compose.ui.platform.LocalContext.current
    var requested by remember { mutableStateOf(false) }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* ignore result — user can re-enable from Settings */ }

    LaunchedEffect(Unit) {
        if (requested) return@LaunchedEffect
        requested = true
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun HandleNotificationDeepLinks(
    navController: NavHostController,
    activeRole: ActiveRole,
    onSwitchToStudent: () -> Unit,
    onSwitchToInstructor: () -> Unit,
    mainViewModel: MainViewModel,
) {
    val event = mainViewModel.notificationDeepLinkBus.events.collectAsState(initial = null).value
    LaunchedEffect(event) {
        val e = event ?: return@LaunchedEffect
        when (e.event) {
            NotificationEvents.N_001 -> {
                if (activeRole != ActiveRole.Instructor) onSwitchToInstructor()
                navController.navigate(Route.Explore) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                }
            }
            NotificationEvents.N_002,
            NotificationEvents.N_003,
            NotificationEvents.N_004 -> {
                val roomId = e.roomId
                if (roomId != null) {
                    // N-002 / N-004 => instructor-side message; N-003 => student-side message
                    when (e.event) {
                        NotificationEvents.N_002,
                        NotificationEvents.N_004 -> {
                            if (activeRole != ActiveRole.Instructor) onSwitchToInstructor()
                        }
                        NotificationEvents.N_003 -> {
                            if (activeRole != ActiveRole.Student) onSwitchToStudent()
                        }
                    }
                    navController.navigate(Route.Chat(roomId)) {
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate(Route.ChatRoomList) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                }
            }
            NotificationEvents.N_005 -> {
                if (activeRole != ActiveRole.Instructor) onSwitchToInstructor()
                navController.navigate(Route.Wallet) {
                    launchSingleTop = true
                }
            }
        }
    }
}
