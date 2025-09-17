package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import com.example.underbigtreeapplication.ui.profile.StaffBottomNavigation
import com.example.underbigtreeapplication.ui.profile.StaffSideNavigationBar
import com.example.underbigtreeapplication.ui.profile.staffNavItems
import com.example.underbigtreeapplication.viewModel.StaffActivityViewModel
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
                StaffActivityContent(
                    paymentsWithOrders,
                    viewModel,
                    Modifier.padding(innerPadding).padding(16.dp),
                    navController
                )
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
            StaffActivityContent(
                paymentsWithOrders,
                viewModel,
                Modifier.padding(innerPadding).padding(16.dp),
                navController
            )
        }
    }
}

@Composable
private fun StaffActivityContent(
    paymentWithOrders: List<StaffActivityViewModel.PaymentWithOrders>,
    viewModel: StaffActivityViewModel,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    if (paymentWithOrders.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
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
        LazyColumn(modifier = modifier.fillMaxSize()) {
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
                    StaffActivityCard(groups[index], viewModel, navController)
                }
            }
        }
    }
}

@Composable
fun StaffActivityCard(
    group: StaffActivityViewModel.PaymentWithOrders,
    viewModel: StaffActivityViewModel,
    navController: NavController
) {
    val allCompleted = group.orders.all { viewModel.isOrderCompleted(it.orderId) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable{
                navController.navigate("staffActivityDetail/${group.payment.paymentId}")
            },
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

                Text(
                    text = "Order #${group.payment.paymentId.takeLast(6)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                group.orders.forEach { order ->
                    Text(
                        text = "${order.quantity} ${order.food.name}",
                        maxLines = 1,
                        fontSize = 14.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                    if(!order.selectedSauces.isNullOrEmpty()){
                        Text(
                            text = "  - Sauce: ${order.selectedSauces.joinToString { it.name }}",
                            maxLines = 1,
                            fontSize = 10.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if(!order.selectedAddOns.isNullOrEmpty()){
                        Text(
                            text = "  - Add-ons: ${order.selectedAddOns.joinToString { it.name }}",
                            maxLines = 1,
                            fontSize = 10.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if(!order.remarks.isNullOrEmpty()){
                        Text(
                            text = "  - Remarks: ${order.remarks}",
                            maxLines = 1,
                            fontSize = 10.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (order.takeAway) {
                        Text(
                            text = "  - Take Away",
                            fontSize = 10.sp
                        )
                    }
                }

                group.redeemedRewards.forEach { reward ->
                    Text(
                        text = "🎁 ${reward.name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF388E3C)
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
                        Text("Complete")
                    }
                }
            }
        }
    }
}

private fun formatDate(timeStamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timeStamp))
}
