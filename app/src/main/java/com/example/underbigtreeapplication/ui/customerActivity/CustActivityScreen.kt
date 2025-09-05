package com.example.underbigtreeapplication.ui.customerActivity

import ads_mobile_sdk.gr
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.underbigtreeapp.R
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.ui.BottomNavigation
import com.example.underbigtreeapplication.ui.SideNavigationBar
import com.example.underbigtreeapplication.ui.customerHomePage.navItems
import com.example.underbigtreeapplication.viewModel.CustomerActivityViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustActivityScreen(navController: NavController, viewModel: CustomerActivityViewModel) {
    val paymentWithOrders by viewModel.paymentsWithOrders.observeAsState(emptyList())

    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidthDp >= 600
    var selectedItem by remember { mutableStateOf("activityScreen") }

    LaunchedEffect(Unit) {
        viewModel.fetchUserPayments()
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (isTablet) {
            SideNavigationBar(
                items = navItems,
                selected = selectedItem,
                navController = navController,
                onItemSelected = { newSelection -> selectedItem = newSelection }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Activity") })
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        ActivityContent(paymentWithOrders = paymentWithOrders, viewModel)
                    }
                }
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Activity") })
                },
                bottomBar = {
                    BottomNavigation(items = navItems, navController = navController)
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    ActivityContent(paymentWithOrders = paymentWithOrders, viewModel)
                }
            }
        }
    }
}


private fun formatDate(timeStamp: Long): String {
    val sdf = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
    return sdf.format(Date(timeStamp))
}

@Composable
private fun ActivityContent(paymentWithOrders: List<CustomerActivityViewModel.PaymentWithOrders>, viewModel: CustomerActivityViewModel){
    if (paymentWithOrders.isEmpty()){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally){
                Image(
                    painter = painterResource(R.drawable.no_order),
                    contentDescription = "No Order History",
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Activity Yet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            val grouped = paymentWithOrders.groupBy {
                formatDate(it.payment.transactionDate)
            }

            grouped.forEach { (date, groups) ->
                item {
                    Text(
                        text = date,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                items(groups.size) { index ->
                    val group = groups[index]
                    ActivityCard(group, viewModel)
                }
            }
        }
    }
}

@Composable
fun ActivityCard(group: CustomerActivityViewModel.PaymentWithOrders, viewModel: CustomerActivityViewModel) {
    val allCompleted = group.orders.all { viewModel.isOrderCompleted(it.orderId) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
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

            Column(modifier = Modifier.weight(1f)){
                group.orders.forEach { order ->
                    Text(
                        text = order.food.name,
                        maxLines = 1,
                        fontSize = 14.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End){
                Text(text = "RM ${String.format("%.2f", group.payment.totalPrice)}")

                if(!allCompleted){
                        Button(
                            onClick = {
                                viewModel.updateOrdersToComplete(group.orders.map {it.orderId})
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Order Receive")
                        }
                    }
                }

        }
    }
}