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
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teachmeski.app.auth.LineBindResultBus
import com.teachmeski.app.auth.LineBindResultUi
import com.teachmeski.app.auth.LineCallbackActivity
import com.teachmeski.app.domain.model.UserRole
import com.teachmeski.app.navigation.AppNavGraph
import com.teachmeski.app.navigation.Route
import com.teachmeski.app.notifications.NotificationDeepLinkEvent
import com.teachmeski.app.notifications.NotificationEvents
import com.teachmeski.app.notifications.NotificationIntentExtras
import android.util.Log
import com.teachmeski.app.ui.MainUiState
import com.teachmeski.app.ui.MainViewModel
import com.teachmeski.app.ui.component.ActiveRole
import com.teachmeski.app.ui.component.OfflineBanner
import com.teachmeski.app.ui.component.TmsBottomBar
import com.teachmeski.app.ui.theme.TeachMeSkiTheme
import com.teachmeski.app.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import com.teachmeski.app.notifications.NotificationDeepLinkBus

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationDeepLinkBus: NotificationDeepLinkBus

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)
        handleLineBindIntent(intent)
        setContent {
            TeachMeSkiTheme {
                TeachMeSkiRoot(networkMonitor = networkMonitor)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
        handleLineBindIntent(intent)
    }

    private fun handleLineBindIntent(intent: Intent?) {
        val kind = intent?.getStringExtra(LineCallbackActivity.EXTRA_LINE_RESULT) ?: return
        val errorCode = intent.getStringExtra(LineCallbackActivity.EXTRA_LINE_ERROR_CODE)
        lifecycleScope.launch {
            LineBindResultBus.emit(LineBindResultUi(kind = kind, errorCode = errorCode))
        }
        intent.removeExtra(LineCallbackActivity.EXTRA_LINE_RESULT)
        intent.removeExtra(LineCallbackActivity.EXTRA_LINE_ERROR_CODE)
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
private fun TeachMeSkiRoot(
    networkMonitor: NetworkMonitor,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()

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
                isOffline = !isOnline,
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
                isOffline = !isOnline,
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
    isOffline: Boolean,
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
        Log.d("TMS_NAV", "graphLE fired: current=$current lastHandled=$lastHandled suppressFlag=${mainViewModel.suppressGraphNavOnRoleChange}")
        if (lastHandled == current) {
            Log.d("TMS_NAV", "graphLE: same as lastHandled, skip")
            return@LaunchedEffect
        }
        lastHandled = current

        // Notification deep-link handlers set this flag before calling switchRole
        // and manually build the correct back stack (CorrectGraph → Chat).
        // Consuming the flag here suppresses the graph re-root so it doesn't
        // wipe Chat off the back stack after the async role change resolves.
        if (mainViewModel.suppressGraphNavOnRoleChange) {
            Log.d("TMS_NAV", "graphLE: suppressFlag=true, skip graph re-root")
            mainViewModel.suppressGraphNavOnRoleChange = false
            return@LaunchedEffect
        }

        if (isAuthenticated) {
            val startRoute: Route = when (activeRole) {
                ActiveRole.Student -> Route.StudentGraph
                ActiveRole.Instructor -> Route.InstructorGraph
            }
            Log.d("TMS_NAV", "graphLE: navigating to $startRoute (popUpTo 0)")
            navController.navigate(startRoute) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            Log.d("TMS_NAV", "graphLE: navigating to AuthGraph")
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
            onRefreshUnreadCount = onRefreshUnreadCount,
            mainViewModel = mainViewModel,
        )
    }

    // F-113 FR-113-007: re-sync the bottom-tab badge whenever the app
    // returns to foreground. The MainViewModel inbox subscription handles
    // realtime updates while the app is alive, but a websocket that
    // reconnected after a long doze can miss intermediate `room_updated`
    // broadcasts — so we additionally re-fetch on every ON_START.
    if (isAuthenticated) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    onRefreshUnreadCount()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    Scaffold(
        contentWindowInsets = if (isFullscreenRoute) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            OfflineBanner(isOffline = isOffline)
        },
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
            onWizardCompleted = { mainViewModel.refreshRole() },
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
    onRefreshUnreadCount: () -> Unit,
    mainViewModel: MainViewModel,
) {
    // Use rememberUpdatedState so the long-lived LaunchedEffect(Unit) sees fresh
    // values on every recomposition (activeRole flips, callbacks change identity, etc.).
    val currentRole = rememberUpdatedState(activeRole)
    val switchToStudent = rememberUpdatedState(onSwitchToStudent)
    val switchToInstructor = rememberUpdatedState(onSwitchToInstructor)
    val refreshUnreadCount = rememberUpdatedState(onRefreshUnreadCount)

    // Collect directly from the channel-backed flow rather than via collectAsState().
    // collectAsState uses structuralEqualityPolicy by default — two consecutive
    // emissions with identical NotificationDeepLinkEvent data (same room+event)
    // would be silently de-duped, swallowing repeat notification taps for the
    // same chat room while the app is in foreground. Direct collect processes
    // every emission.
    LaunchedEffect(Unit) {
        mainViewModel.notificationDeepLinkBus.events.collect { e ->
            val role = currentRole.value
            when (e.event) {
                NotificationEvents.N_001 -> {
                    if (role != ActiveRole.Instructor) switchToInstructor.value()
                    navController.navigate(Route.Explore) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                }
                NotificationEvents.N_002,
                NotificationEvents.N_003,
                NotificationEvents.N_004 -> {
                    // F-113 FR-113-007: when a chat-notification tap brings
                    // the app to foreground, the realtime subscription may
                    // have missed the `room_updated` broadcast while the
                    // websocket was disconnected — re-fetch immediately so
                    // the bottom-tab badge reflects the new unread count
                    // even if the user does not navigate to ChatRoomList.
                    refreshUnreadCount.value()
                    val roomId = e.roomId
                    if (roomId != null) {
                        when (e.event) {
                            NotificationEvents.N_002,
                            NotificationEvents.N_004 -> {
                                if (role != ActiveRole.Instructor) {
                                    mainViewModel.suppressGraphNavOnRoleChange = true
                                    switchToInstructor.value()
                                    navController.navigate(Route.InstructorGraph) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                            NotificationEvents.N_003 -> {
                                if (role != ActiveRole.Student) {
                                    mainViewModel.suppressGraphNavOnRoleChange = true
                                    switchToStudent.value()
                                    navController.navigate(Route.StudentGraph) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
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
                    if (role != ActiveRole.Instructor) switchToInstructor.value()
                    navController.navigate(Route.Wallet) {
                        launchSingleTop = true
                    }
                }
                NotificationEvents.N_007 -> {
                    // F-113 FR-113-018 #3 + AC-113-011: quota-expansion push is
                    // an instructor-side event; switch panel first so the back
                    // stack stays consistent with FR-113-016. Android does not
                    // yet have a standalone request-detail route reachable
                    // outside MyRequests; landing on Explore lets the
                    // instructor see the expanded request via the Explore feed.
                    if (role != ActiveRole.Instructor) switchToInstructor.value()
                    navController.navigate(Route.Explore) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                }
                // TODO(F-113 FR-113-018 #4): IAP-success push case. Add a
                // `NotificationEvents.<IAP_EVENT>` branch here once the Edge
                // Function emits one (see TODO in NotificationConstants.kt).
            }
        }
    }
}
