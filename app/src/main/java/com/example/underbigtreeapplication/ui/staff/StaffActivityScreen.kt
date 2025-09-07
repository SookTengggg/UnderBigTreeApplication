package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.underbigtreeapp.R
import com.example.underbigtreeapplication.ui.BottomNavigation
import com.example.underbigtreeapplication.ui.SideNavigationBar
import com.example.underbigtreeapplication.ui.customerHomePage.navItems
import com.example.underbigtreeapplication.ui.profile.StaffBottomNavigation
import com.example.underbigtreeapplication.ui.profile.StaffSideNavigationBar
import com.example.underbigtreeapplication.ui.profile.staffNavItems
import com.example.underbigtreeapplication.viewModel.StaffActivityViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffActivityScreen(
    navController: NavController,
    viewModel: StaffActivityViewModel = viewModel()
) {
    val paymentsWithOrders by viewModel.paymentsWithOrders.observeAsState(emptyList())

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidthDp >= 600
    var selectedItem by remember { mutableStateOf("staffActivity") }

    LaunchedEffect(Unit) {
        viewModel.fetchAllPayments()
    }

    if (isTablet) {
        StaffSideNavigationBar(
            items = staffNavItems,
            selected = selectedItem,
            navController = navController,
            onItemSelected = { newSelection -> selectedItem = newSelection }
        ) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Staff Activity") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFF2F2F2),
                            titleContentColor = Color.Black
                        )
                    )
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    StaffActivityContent(paymentsWithOrders, viewModel)
                }
            }
        }
    } else {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Staff Activity") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF2F2F2),
                        titleContentColor = Color.Black
                    )
                )
            },
            bottomBar = {
                StaffBottomNavigation(
                    items = staffNavItems,
                    navController = navController
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                StaffActivityContent(paymentsWithOrders, viewModel)
            }
        }
    }
}

@Composable
private fun StaffActivityContent(
    paymentWithOrders: List<StaffActivityViewModel.PaymentWithOrders>,
    viewModel: StaffActivityViewModel
) {
    if (paymentWithOrders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.no_order),
                    contentDescription = "No Orders",
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Orders Yet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            val grouped = paymentWithOrders.groupBy { formatDate(it.payment.transactionDate) }
            grouped.forEach { (date, groups) ->
                item {
                    Text(
                        text = date,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                items(groups.size) { index ->
                    StaffActivityCard(groups[index], viewModel)
                }
            }
        }
    }
}

@Composable
fun StaffActivityCard(
    group: StaffActivityViewModel.PaymentWithOrders,
    viewModel: StaffActivityViewModel
) {
    val allCompleted = group.orders.all { viewModel.isOrderCompleted(it.orderId) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2), contentColor = Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.order),
                contentDescription = "Order",
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                group.orders.forEach { order ->
                    Text(
                        text = "${order.food.name} (User: ${group.payment.userId.take(6)})",
                        maxLines = 1,
                        fontSize = 14.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "RM ${String.format("%.2f", group.payment.totalPrice)}")

                if (!allCompleted) {
                    Button(
                        onClick = { viewModel.updateOrdersToComplete(group.orders.map { it.orderId }) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Mark Complete")
                    }
                }
            }
        }
    }
}

// Utility function
private fun formatDate(timeStamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MM yyyy", Locale.getDefault())
    return sdf.format(Date(timeStamp))
}
