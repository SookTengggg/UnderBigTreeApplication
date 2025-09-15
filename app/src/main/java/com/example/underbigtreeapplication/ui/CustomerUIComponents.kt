package com.example.underbigtreeapplication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.underbigtreeapplication.ui.customerHomePage.NavItem
import kotlin.collections.forEach

@Composable
fun BottomNavigation(items: List<NavItem>, navController: NavController, modifier: Modifier = Modifier) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar (
        modifier = modifier.fillMaxWidth(),
        containerColor = Color(0xFFF2F2F2),
        contentColor = Color.Black
    ) {
        items.forEach { item ->
            val selected = currentDestination == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentDestination != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {saveState = true}
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                    )
                }
            )
        }
    }
}

@Composable
fun SideNavigationBar(items: List<NavItem>, navController: NavController, content: @Composable () -> Unit) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.width(150.dp),
                drawerContainerColor = Color.White,
                drawerContentColor = Color.Black
            ) {
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        selected = currentDestination == item.route,
                        onClick = {
                            if(navController.currentBackStackEntry?.destination?.route != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {saveState = true}
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            content()
        }
    }
}