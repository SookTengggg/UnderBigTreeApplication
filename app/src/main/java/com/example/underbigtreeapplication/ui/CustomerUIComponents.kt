package com.example.underbigtreeapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.underbigtreeapplication.ui.customerHomePage.NavItem
import com.example.underbigtreeapplication.ui.signupPage.PreviewSignupScreen
import kotlin.collections.forEach

@Composable
fun BottomNavigation(items: List<NavItem>, navController: NavController, modifier: Modifier = Modifier) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar (
        modifier = modifier.fillMaxWidth(),
        containerColor = Color(0xFFEFEFEF),
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
fun SideNavigationBar(items: List<NavItem>, selected: String, onItemSelected: (String) -> Unit, content: @Composable () -> Unit) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet {
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        selected = selected == item.route,
                        onClick = { onItemSelected(item.route) }
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                //.weight(1f)
                .fillMaxHeight()
        ) {
            content()
        }
    }


//    Column (modifier = Modifier
//        .fillMaxHeight()
//        .width(200.dp)
//        .background(Color(0xFFEFEFEF)),
//        verticalArrangement = Arrangement.Top
//    ) {
//        items.forEach { item ->
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable {onItemSelected(item.route)}
//                    .background(if (selected == item.route) Color.White else Color.Transparent)
//                    .padding(12.dp),
//                verticalAlignment = Alignment.CenterVertically,
//
//                ) {
//                Icon(
//                    item.icon,
//                    contentDescription = item.label
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = item.label,
//                    fontSize = 12.sp,
//                    fontWeight = if (selected == item.route) FontWeight.Bold else FontWeight.Normal,
//                    color = Color.Black
//                )
//            }
//        }
//    }
}