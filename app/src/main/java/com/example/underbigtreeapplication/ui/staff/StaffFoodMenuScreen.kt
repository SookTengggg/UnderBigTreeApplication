package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.ui.profile.StaffBottomNavigation
import com.example.underbigtreeapplication.ui.profile.StaffSideNavigationBar
import com.example.underbigtreeapplication.ui.profile.staffNavItems
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight

@Composable
fun StaffFoodMenuScreen(
    navController: NavController,
    staffViewModel: StaffViewModel,
    onMenuClick: (MenuEntity) -> Unit
) {
    val menuList by staffViewModel.menus.collectAsState()
    val addon by staffViewModel.addons.collectAsState()
    var showChooseScreen by rememberSaveable { mutableStateOf(false) }
    var selectedOption by rememberSaveable { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600
    var selectedItem by remember { mutableStateOf("staffFood") }

    // Global dialog states
    var deleteTargetMenuId by rememberSaveable { mutableStateOf<String?>(null) }
    val deleteTargetMenu = menuList.find { it.id == deleteTargetMenuId }

    var deleteTargetAddonId by rememberSaveable { mutableStateOf<String?>(null) }
    val deleteTargetAddon = addon.find { it.id == deleteTargetAddonId }


    Box(Modifier.fillMaxSize()) {
        if (isTablet) {
            StaffSideNavigationBar(
                items = staffNavItems,
                selected = selectedItem,
                navController = navController,
                onItemSelected = { newSelection -> selectedItem = newSelection },
            ) {
                MenuContent(
                    menuList = menuList,
                    addon = addon,
                    onMenuClick = { selectedMenu ->
                        val encodedId = URLEncoder.encode(selectedMenu.id, StandardCharsets.UTF_8.toString())
                        navController.navigate("editFoodMenu/$encodedId")
                    },
                    onAddOnClick = { selectedAddOn ->
                        val encodedId = URLEncoder.encode(selectedAddOn.id, StandardCharsets.UTF_8.toString())
                        navController.navigate("editAddOnMenu/$encodedId")
                    },
                    onMenuDelete = { deleteTargetMenuId = it.id },
                    onAddOnDelete = { deleteTargetAddonId = it.id },
                    showChooseScreen = showChooseScreen,
                    onShowChooseScreen = { showChooseScreen = it }
                )
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
                MenuContent(
                    menuList = menuList,
                    addon = addon,
                    onMenuClick = { selectedMenu ->
                        val encodedId = URLEncoder.encode(selectedMenu.id, StandardCharsets.UTF_8.toString())
                        navController.navigate("editFoodMenu/$encodedId")
                    },
                    onAddOnClick = { selectedAddOn ->
                        val encodedId = URLEncoder.encode(selectedAddOn.id, StandardCharsets.UTF_8.toString())
                        navController.navigate("editAddOnMenu/$encodedId")
                    },
                    onMenuDelete = { deleteTargetMenuId = it.id },
                    onAddOnDelete = { deleteTargetAddonId = it.id },
                    showChooseScreen = showChooseScreen,
                    onShowChooseScreen = { showChooseScreen = it },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Global Delete Dialogs
        deleteTargetMenu?.let { menu ->
            AlertDialog(
                onDismissRequest = { deleteTargetMenuId = null },
                title = { Text("Delete Menu") },
                text = { Text("Are you sure you want to delete '${menu.name}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        staffViewModel.deleteMenu(menu.id)
                        deleteTargetMenuId = null
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTargetMenuId = null }) { Text("Cancel") }
                }
            )
        }

        deleteTargetAddon?.let { add ->
            AlertDialog(
                onDismissRequest = { deleteTargetAddonId = null },
                title = { Text("Delete Add-On") },
                text = { Text("Are you sure you want to delete '${add.name}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        staffViewModel.deleteAddOn(add.id)
                        deleteTargetAddonId = null
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTargetAddonId = null }) { Text("Cancel") }
                }
            )
        }

        // Choose screen overlay
        if (showChooseScreen) {
            StaffChooseScreen(
                navController = navController,
                staffViewModel = staffViewModel,
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it },
                onDismiss = { showChooseScreen = false }
            )
        }
    }
}

@Composable
private fun MenuContent(
    menuList: List<MenuEntity>,
    addon: List<AddOnEntity>,
    onMenuClick: (MenuEntity) -> Unit,
    onAddOnClick: (AddOnEntity) -> Unit,
    onMenuDelete: (MenuEntity) -> Unit,
    onAddOnDelete: (AddOnEntity) -> Unit,
    showChooseScreen: Boolean,
    onShowChooseScreen: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
            item { AddMenuCard { onShowChooseScreen(true) } }
            items(menuList) { menu ->
                Box {
                    StaffMenuCard(item = menu, onClick = { onMenuClick(menu) })
                    IconButton(
                        onClick = { onMenuDelete(menu) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Menu", tint = Color.Red)
                    }
                }
            }
            items(addon) { add ->
                Box {
                    StaffAddOnCard(item = add, onClick = { onAddOnClick(add) })
                    IconButton(
                        onClick = { onAddOnDelete(add) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete AddOn", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun AddMenuCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Add, contentDescription = "Add Menu", tint = Color.Gray, modifier = Modifier.size(40.dp))
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF), contentColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                if (!isAvailable) {
                    Text("*Unavailable", color = Color.Red, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Column(
                modifier = Modifier.fillMaxSize().alpha(if (isAvailable) 1f else 0.3f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(model = item.imageRes, contentDescription = item.name, modifier = Modifier.size(120.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("RM %.2f".format(item.price))
            }
        }
    }
}

@Composable
fun StaffAddOnCard(item: AddOnEntity, onClick: (AddOnEntity) -> Unit) {
    val isAvailable = item.availability
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .then(if (isAvailable) Modifier.clickable { onClick(item) } else Modifier),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF), contentColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).height(IntrinsicSize.Min), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (!isAvailable) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text("*Unavailable", color = Color.Red, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                    }
                }
                Column(modifier = Modifier.alpha(if (isAvailable) 1f else 0.3f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    Text("RM %.2f".format(item.price))
                }
            }
        }
    }
}
