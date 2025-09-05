package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.model.SauceEntity
import com.example.underbigtreeapplication.ui.customerHomePage.MenuCard
import com.example.underbigtreeapplication.ui.profile.StaffBottomNavigation
import com.example.underbigtreeapplication.ui.profile.StaffSideNavigationBar
import com.example.underbigtreeapplication.ui.profile.staffNavItems
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import com.example.underbigtreeapplication.viewModel.StaffViewModelFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

@Composable
fun StaffFoodMenuScreen(
    navController: NavController,
    staffViewModel: StaffViewModel,
    onMenuClick: (MenuEntity) -> Unit
) {
    val menuList by staffViewModel.menus.collectAsState()
    val addon by staffViewModel.addons.collectAsState()
    var showChooseScreen by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600
    var selectedItem by remember { mutableStateOf("staffFood") }

    Box(Modifier.fillMaxSize()) {
        if (isTablet) {
            StaffSideNavigationBar(
                items = staffNavItems,
                selected = selectedItem,
                navController = navController,
                onItemSelected = { newSelection -> selectedItem = newSelection },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Menu",
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { AddMenuCard { showChooseScreen = true } }
                        items(menuList) { menu -> StaffMenuCard(menu) { onMenuClick(menu) } }
                        items(addon) { add ->
                            StaffAddOnCard(add) { selectedAddOn ->
                                navController.navigate("editAddOnMenu/${selectedAddOn.id}")
                            }
                        }
                    }
                }
            }
        } else {
            Scaffold(
                containerColor = Color.White,
                bottomBar = {
                    StaffBottomNavigation(
                        items = staffNavItems,
                        navController = navController
                    )
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Menu",
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { AddMenuCard { showChooseScreen = true } }
                        items(menuList) { menu -> StaffMenuCard(menu) { onMenuClick(menu) } }
                        items(addon) { add ->
                            StaffAddOnCard(add) { selectedAddOn ->
                                navController.navigate("editAddOnMenu/${selectedAddOn.id}")
                            }
                        }
                    }
                }

                if (showChooseScreen) {
                    StaffChooseScreen(
                        navController = navController,
                        staffViewModel = staffViewModel,
                        onDismiss = { showChooseScreen = false }
                    )
                }
            }
        }
    }
}

@Composable
fun AddMenuCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(230.dp).clickable { onClick() },
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
fun StaffMenuCard(item: MenuEntity, onClick: (MenuEntity) -> Unit) {
    val isAvailable = item.availability

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .then(if (isAvailable) Modifier.clickable { onClick(item) } else Modifier),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFEFEF),
            contentColor = Color.Black
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(6.dp)){
                if (!isAvailable) {
                    Text(
                        text = "*Unavailable",
                        color = Color.Red,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(if (!isAvailable) 8.dp else 0.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isAvailable) 1f else 0.3f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = item.imageRes,
                    contentDescription = item.name,
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("RM %.2f".format(item.price))
            }
        }
    }
}

@Composable
fun StaffAddOnCard(item: AddOnEntity, onClick: (AddOnEntity) -> Unit) {
    val isAvailable = item.availability

    Card(
        modifier = Modifier.fillMaxWidth().height(230.dp)
            .then(
                if (isAvailable) Modifier.clickable { onClick(item) }
                else Modifier
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFEFEF),
            contentColor = Color.Black
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center)
        {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!isAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "*Unavailable",
                            color = Color.Red,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }
                Column(
                    modifier = Modifier.alpha(if (isAvailable) 1f else 0.3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
