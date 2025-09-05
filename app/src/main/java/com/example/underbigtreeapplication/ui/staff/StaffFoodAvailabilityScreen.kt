package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.model.OptionItem
import com.example.underbigtreeapplication.model.SauceEntity
import com.example.underbigtreeapplication.viewModel.StaffViewModel

@Composable
fun StaffFoodAvailabilityScreen(
    navController: NavController,
    staffViewModel: StaffViewModel,
    onMenuClick: (MenuEntity) -> Unit
) {
    val menuList by staffViewModel.menus.collectAsState()
    val sauce by staffViewModel.sauces.collectAsState()
    val addon by staffViewModel.addons.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(32.dp)) {
            Text(
                text = "Food Availability",
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "* Click button to toggle available or unavailable",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)
            ) {
                items(menuList) { menu ->
                    FoodAvailabilityCard(menu = menu, onToggle = {
                        staffViewModel.updateMenuAvailability(menu.id, !menu.availability)
                    })
                }
                items(sauce) { sauce ->
                    SauceAvailabilityCard(sauce = sauce, onToggle = {
                        staffViewModel.updateSauceAvailability(sauce.id, !sauce.availability)
                    })
                }
                items(addon) { addon ->
                    AddOnAvailabilityCard(addon = addon, onToggle = {
                        staffViewModel.updateAddOnAvailability(addon.id, !addon.availability)
                    })
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
fun FoodAvailabilityCard(menu: MenuEntity, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFEFEF),
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).height(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = menu.imageRes,
                contentDescription = menu.name,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(menu.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { onToggle() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (menu.availability) Color.Gray else Color.Red
                )
            ) {
                Text(
                    if (menu.availability) "Available" else "Unavailable",
                    color = Color.White,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun SauceAvailabilityCard(sauce: SauceEntity, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFEFEF),
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    sauce.name, fontSize = 14.sp, fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { onToggle() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sauce.availability) Color.Gray else Color.Red
                    )
                ) {
                    Text(
                        if (sauce.availability) "Available" else "Unavailable",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun AddOnAvailabilityCard(addon: AddOnEntity, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFEFEF),
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center)
        {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(addon.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = { onToggle() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (addon.availability) Color.Gray else Color.Red
                    )
                ) {
                    Text(
                        if (addon.availability) "Available" else "Unavailable",
                        color = Color.White,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    }
}