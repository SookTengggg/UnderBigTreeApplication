package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.underbigtreeapplication.model.MenuEntity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.underbigtreeapplication.ui.customerHomePage.MenuCard
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import com.example.underbigtreeapplication.viewModel.StaffViewModelFactory


data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

val staffNavItems = listOf(
    NavItem(Icons.Filled.Home, "Home", "staffHome"),
    NavItem(Icons.Filled.Edit, "Edit", "staffEdit"),
    NavItem(Icons.Filled.Menu, "Activity","staffActivityScreen"),
    NavItem(Icons.Filled.Person, "Profile", "staffProfile")
)

@Composable
fun StaffFoodMenuScreen(navController: NavController, staffViewModel: StaffViewModel, onAddMenu: () -> Unit, onMenuClick: (MenuEntity) -> Unit) {
    val menuList by staffViewModel.menus.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Menu",
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    AddMenuCard(onAddMenu)
                }
                items(menuList) { menu ->
                    MenuCard(menu, onMenuClick)
                }
            }
        }
        StaffBottomNavigation(
            items = staffNavItems,
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun AddMenuCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(250.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Menu",
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun MenuCard(menu: MenuEntity, onClick: (MenuEntity) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { onClick(menu) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = menu.imageRes,
                contentDescription = menu.name,
                modifier = Modifier.size(80.dp)
            )
            Text(menu.name, fontSize = 14.sp)
            Text("RM %.2f".format(menu.price), fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StaffBottomNavigation(items: List<NavItem>, navController: NavController, modifier: Modifier = Modifier) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(modifier = modifier.fillMaxWidth(), containerColor = Color.White) {
        items.forEach { item ->
            val selected = currentDestination == item.route
            NavigationBarItem(
                selected = selected, onClick = {
                    if (currentDestination != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) }
            )
        }
    }
}
