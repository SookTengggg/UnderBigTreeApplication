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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.underbigtreeapplication.model.MenuEntity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.model.SauceEntity
import com.example.underbigtreeapplication.ui.customerHomePage.MenuCard
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import com.example.underbigtreeapplication.viewModel.StaffViewModelFactory


data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

val staffNavItems = listOf(
    NavItem(Icons.Filled.Home, "Home", "staffActivity"),
    NavItem(Icons.Filled.Edit, "Edit", "staffAvailability"),
    NavItem(Icons.Filled.Menu, "Activity","staffFood"),
    NavItem(Icons.Filled.Person, "Profile", "staffProfile")
)

@Composable
fun StaffFoodMenuScreen(navController: NavController, staffViewModel: StaffViewModel, onMenuClick: (MenuEntity) -> Unit) {
    val menuList by staffViewModel.menus.collectAsState()
    val addon by staffViewModel.addons.collectAsState()
    var showChooseScreen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(32.dp)) {
            Text(
                text = "Menu",
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)
            ) {
                item {
                    AddMenuCard(onClick = { showChooseScreen = true })
                }
                items(menuList) { menu ->
                    StaffMenuCard(menu) { selectedMenu ->
                        navController.navigate("foodEdit/${selectedMenu.id}/menu")
                    }
                }
                items(addon) { addon ->
                    StaffAddOnCard(addon) { selectedAddOn ->
                        navController.navigate("foodEdit/${selectedAddOn.id}/addon")
                    }
                }
            }
        }
        StaffBottomNavigation(
            items = staffNavItems,
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        if (showChooseScreen) {
            StaffChooseScreen(
                navController = navController,
                staffViewModel = staffViewModel,
                onDismiss = { showChooseScreen = false }
            )
        }
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
fun StaffMenuCard(item: MenuEntity, onClick: (MenuEntity) -> Unit){
    val isAvailable = item.availability

    Card(
        modifier = Modifier.fillMaxWidth().height(250.dp)
            .then(
                if (isAvailable) Modifier.clickable { onClick(item) }
                else Modifier
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFEFEF),
            contentColor = Color.Black
        )
    ){
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
            if(!isAvailable){
                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text(
                        text = "*Unavailable", color = Color.Red, fontWeight = FontWeight.SemiBold, fontSize = 10.sp
                    )
                }
            }
            Column(
                modifier = Modifier.alpha(if (isAvailable) 1f else 0.3f),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                AsyncImage(
                    model = item.imageRes,
                    contentDescription = item.name,
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text("RM %.2f".format(item.price))
            }
        }
    }
}

@Composable
fun StaffAddOnCard(item: AddOnEntity, onClick: (AddOnEntity) -> Unit){
    val isAvailable = item.availability

    Card(
        modifier = Modifier.fillMaxWidth().height(250.dp)
            .then(
                if (isAvailable) Modifier.clickable { onClick(item) }
                else Modifier
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFEFEF),
            contentColor = Color.Black
        )
    ){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center)
        {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                if(!isAvailable){
                    Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text(
                            text = "*Unavailable", color = Color.Red, fontWeight = FontWeight.SemiBold, fontSize = 10.sp
                        )
                    }
                }
                Column(
                    modifier = Modifier.alpha(if (isAvailable) 1f else 0.3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text("RM %.2f".format(item.price))
                }
            }
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
