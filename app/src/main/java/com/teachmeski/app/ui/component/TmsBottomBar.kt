package com.teachmeski.app.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.teachmeski.app.R
import com.teachmeski.app.navigation.Route

enum class ActiveRole { Student, Instructor }

data class BottomNavItem(
    val route: Route,
    val labelResId: Int,
    val icon: ImageVector,
)

val studentTabs =
    listOf(
        BottomNavItem(Route.MyRequests, R.string.nav_my_requests, Icons.Outlined.Description),
        BottomNavItem(Route.ChatRoomList, R.string.nav_inbox, Icons.AutoMirrored.Outlined.Message),
        BottomNavItem(Route.Account, R.string.nav_account, Icons.Outlined.Person),
    )

val instructorTabs =
    listOf(
        BottomNavItem(Route.Explore, R.string.nav_explore, Icons.Outlined.Description),
        BottomNavItem(Route.Unlocked, R.string.nav_unlocked, Icons.Outlined.LockOpen),
        BottomNavItem(Route.ChatRoomList, R.string.nav_conversation, Icons.AutoMirrored.Outlined.Message),
        BottomNavItem(Route.InstructorAccount, R.string.nav_instructor_account, Icons.Outlined.Person),
    )

@Composable
fun TmsBottomBar(
    activeRole: ActiveRole,
    currentRoute: Route?,
    onTabSelected: (Route) -> Unit,
) {
    val tabs =
        when (activeRole) {
            ActiveRole.Student -> studentTabs
            ActiveRole.Instructor -> instructorTabs
        }

    NavigationBar {
        tabs.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onTabSelected(item.route) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.labelResId)) },
            )
        }
    }
}
