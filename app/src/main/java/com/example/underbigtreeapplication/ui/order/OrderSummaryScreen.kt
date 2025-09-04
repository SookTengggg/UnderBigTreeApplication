package com.example.underbigtreeapplication.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.underbigtreeapplication.utils.formatAmount
import com.example.underbigtreeapplication.viewModel.CartViewModel
import com.example.underbigtreeapplication.viewModel.OrderSummaryViewModel
import com.example.underbigtreeapplication.viewModel.OrderViewModel
import kotlin.toString

@Composable
fun OrderSummaryScreen(
    viewModel: OrderSummaryViewModel,
    navController: NavController,
    onBackClick: () -> Unit = {}
) {
    val orders by viewModel.orders.collectAsState()
    val scrollState = rememberScrollState()
    var selectedPayment by remember { mutableStateOf("tng") }

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    val groupedOrders = orders.groupBy { it.food.id to it.takeAway }
        .map { (_, group) ->
            val first = group.first()
            first.copy(
                quantity = group.sumOf { it.quantity },
                totalPrice = group.sumOf { it.totalPrice }
            )
        }

    val subtotal = groupedOrders.sumOf { it.totalPrice }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))

        Text("Order Summary", fontSize = 28.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (groupedOrders.isEmpty()) {
            Text("No orders yet", fontSize = 16.sp)
        } else {
            groupedOrders.forEach { order ->
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

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(order.food.name, fontSize = 14.sp)
                            Text("${formatAmount(order.totalPrice)}", fontSize = 14.sp)
                        }
                        if (order.takeAway == true) {
                            Text("(Take Away)", fontSize = 12.sp, color = Color.Gray)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {

                            IconButton(onClick = { viewModel.decreaseQuantity(order) }) {
                                Text("-", fontSize = 12.sp, color = Color.Gray)
                            }

                            Text("Qty: ${order.quantity}", fontSize = 12.sp, color = Color.Gray)

                            IconButton(onClick = { viewModel.increaseQuantity(order) }) {
                                Text("+", fontSize = 12.sp, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = { viewModel.removeItem(order) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove item",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color.LightGray, thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal:", fontSize = 16.sp)
                Text("${formatAmount(subtotal)}", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text("Payment Option ", fontSize = 16.sp)
                    Text("*", fontSize = 16.sp, color = Color.Red)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedPayment == "tng",
                        onClick = { selectedPayment = "tng" }
                    )
                    Text("Touch n Go")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedPayment == "bank",
                        onClick = { selectedPayment = "bank" }
                    )
                    Text("Credit/Debit Card")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedPayment == "tng") {
                        navController.navigate("tngPayment/${subtotal}")
                    } else {
                        navController.navigate("bankPayment/${subtotal}")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pay")
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}
