package com.example.underbigtreeapplication.ui.customerActivity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.underbigtreeapp.R
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.model.RewardItem
import com.example.underbigtreeapplication.utils.formatAmount
import com.example.underbigtreeapplication.viewModel.CustomerActivityViewModel

@Composable
fun ActivityDetailScreen(paymentId: String, viewModel: CustomerActivityViewModel, navController: NavController){
    val paymentsWithOrders by viewModel.paymentsWithOrders.observeAsState(emptyList())
    val redeemedRewards by viewModel.redeemedRewardsForPayment.observeAsState(emptyList())

    LaunchedEffect(paymentId) {
        viewModel.fetchRedeemedRewardsForPayment(paymentId)
    }

    val paymentWithOrders = paymentsWithOrders.find { it.payment.paymentId == paymentId }

    if (paymentWithOrders == null){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading...")
        }
    } else {
        ActivityDetailContent(
            paymentId,
            orders = paymentWithOrders.orders,
            subtotal = paymentWithOrders.payment.totalPrice,
            redeemedRewards = redeemedRewards,
            navController = navController,
            viewModel
        )
    }
}

@Composable
fun ActivityDetailContent(paymentId: String, orders: List<CartItem>, subtotal: Double, redeemedRewards: List<RewardItem>, navController: NavController, viewModel: CustomerActivityViewModel) {
    val scrollState = rememberScrollState()

    var autoTriggered by remember { mutableStateOf(false) }

    val hasOrders = orders.isNotEmpty()
    val hasRewards = redeemedRewards.isNotEmpty()

    val completedOrders = hasOrders && orders.any { it.orderStatus == "completed" }
    val completedRewards = hasRewards && redeemedRewards.any { it.status == "completed" }

    val showReceiveButton = when {
        hasOrders && hasRewards -> completedOrders || completedRewards
        hasOrders -> completedOrders
        hasRewards -> completedRewards
        else -> false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Order Details", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Order No: $paymentId",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            if (orders.isEmpty() && redeemedRewards.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.no_order),
                        contentDescription = "No Activity",
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Activity Yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                orders.forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = order.food.imageRes,
                            contentDescription = order.food.name,
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(order.food.name, fontSize = 14.sp)
                                Text(formatAmount(order.totalPrice), fontSize = 14.sp)
                            }

                            if (order.takeAway == true) {
                                Text("(Take Away)", fontSize = 12.sp, color = Color.Gray)
                            }

                            if (order.selectedAddOns.isNotEmpty()) {
                                Text(
                                    "Add-ons: ${order.selectedAddOns.joinToString { it.name }}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                "Qty: ${order.quantity} • Unit: ${formatAmount(order.food.price)}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                if (redeemedRewards.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    redeemedRewards.forEach { reward ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.reward_pic),
                                contentDescription = reward.name,
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(reward.name, fontSize = 14.sp)
                                Text(
                                    text = "Points: ${reward.pointsRequired}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color.LightGray, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal:", fontSize = 16.sp)
                Text(formatAmount(subtotal), fontSize = 16.sp)
            }

            if (showReceiveButton) {
                Button(
                    onClick = {
                        autoTriggered = true
                        viewModel.updatePaymentToReceived(
                            paymentId,
                            orders.map { it.orderId },
                            redeemedRewards.map { it.id }
                        )
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text("Receive")
                }
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}
