package com.example.underbigtreeapplication.ui.customerHomePage

import ads_mobile_sdk.na
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.collections.forEach
import coil.compose.AsyncImage
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.model.CategoryEntity
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.ui.BottomNavigation
import com.example.underbigtreeapplication.ui.SideNavigationBar
import com.example.underbigtreeapplication.viewModel.CartViewModel
import com.example.underbigtreeapplication.viewModel.CustHomeViewModel
import com.example.underbigtreeapplication.viewModel.OrderSummaryViewModel
import com.example.underbigtreeapplication.viewModel.OrderSummaryViewModelFactory
import kotlin.text.category

data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

val navItems = listOf(
    NavItem(Icons.Filled.Home, "Home", "home"),
    NavItem(Icons.Filled.Menu, "Activity","custActivity"),
    NavItem(Icons.Filled.Person, "Profile", "custProfile")
)

@Composable
fun CustHomeScreen(points: Int, modifier: Modifier = Modifier, viewModel: CustHomeViewModel, navController: NavController, cartViewModel: CartViewModel) {
    val menus by viewModel.menus.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by viewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedCategory by viewModel.selectedCategory
    val cartItems by cartViewModel.cartItems.collectAsState()
    var selectedItem by remember { mutableStateOf("home") }
    val desiredOrder = listOf("All", "Rice", "Spaghetti", "Chicken", "Fish", "Drinks")
    val allCategory = categories.find { it.name == "All" } ?: CategoryEntity("all", "All", "")
    val sortedCategories = listOf(allCategory) + categories.filter { it.name != "All" }.sortedBy { desiredOrder.indexOf(it.name).takeIf { it >= 0 } ?: Int.MAX_VALUE }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (!isTablet) {
                BottomNavigation(
                    items = navItems,
                    navController = navController
                )
            }
        },
        floatingActionButton = {
            CartFab(
                navController,
                orderSummaryViewModel = viewModel(factory = OrderSummaryViewModelFactory(cartViewModel))
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (isTablet) {
            Box(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxSize().padding(innerPadding)) {
                    SideNavigationBar(
                        items = navItems,
                        selected = selectedItem,
                        navController = navController,
                        onItemSelected = { newSelection -> selectedItem = newSelection }
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Points(
                                    points = points,
                                    onClick = { navController.navigate("point") })
                            }

                            TopCategory(
                                categories = sortedCategories,
                                selected = selectedCategory,
                                onCategorySelected = { viewModel.selectCategory(it) })

                            val filteredItems = if (selectedCategory == "All") {
                                menus
                            } else {
                                menus.filter { it.category.contains(selectedCategory) }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredItems) { item ->
                                    MenuCard(item, onClick = {
                                        navController.navigate("order/${it.id}")
                                    })
                                }
                            }
                        }
                    }

                }
            }
        } else {
            Row(Modifier.fillMaxSize().padding(innerPadding)) {

                SideNavigation(
                    categories = sortedCategories,
                    selected = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )

                Column(Modifier.fillMaxSize()) {
                    Points(points = points, onClick = {navController.navigate("point")})

                    val filteredItems = if (selectedCategory == "All") {
                        menus
                    } else {
                        menus.filter { it.category.contains(selectedCategory) }
                    }

                    LazyColumn(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredItems) { item ->
                            MenuCard(item = item, onClick = {
                                navController.navigate("order/${it.id}")
                            })
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun SideNavigation(categories: List<CategoryEntity>, selected: String, onCategorySelected: (String) -> Unit) {
    Column(
        Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(Color(0xFFEFEFEF))
            .padding(top = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ){
        categories.forEach { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onCategorySelected(category.name) }
                    .width(70.dp)
            ) {
                Box (
                    modifier = Modifier
                        .background(
                            color = if (category.name == selected) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .size(90.dp),
                    contentAlignment = Alignment.Center
                ){
                    Column (verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = category.imageRes,
                            contentDescription = category.name,
                            Modifier.size(40.dp),
                        )
                        Text(
                            text = category.name,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopCategory(categories: List<CategoryEntity>, selected: String, onCategorySelected: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        categories.forEach { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onCategorySelected(category.name) }
            ) {
                Box (
                    modifier = Modifier
                        .background(
                            color = if (category.name == selected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(60.dp, 8.dp),
                    //.size(100.dp),
                    contentAlignment = Alignment.Center
                ){
                    Row (verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AsyncImage(
                            model = category.imageRes,
                            contentDescription = category.name,
                            Modifier.size(30.dp),
                        )
                        Text(
                            text = category.name,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Points(points: Int, onClick: () -> Unit){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 23.dp, end = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Button(onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
        ){
            Text("Points: $points", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MenuCard(item: MenuEntity, onClick: (MenuEntity) -> Unit){
    val isAvailable = item.availability

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
            if(!isAvailable){
                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
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
fun CartFab(navController: NavController, orderSummaryViewModel: OrderSummaryViewModel) {

    LaunchedEffect(Unit) {
        orderSummaryViewModel.fetchOrders()
    }
    val orderItems by orderSummaryViewModel.orders.collectAsState()

    if (orderItems.isNotEmpty()) {
        FloatingActionButton(
            onClick = { navController.navigate("orderSummaryScreen") },
            containerColor = Color.Black,
            contentColor = Color.White
        ) {
            val totalQuantity = orderItems.sumOf { it.quantity }
            val totalPrice = orderItems.sumOf { it.totalPrice }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cart · $totalQuantity item(s)")
                Text("RM %.2f".format(totalPrice))
            }
        }
    }
}