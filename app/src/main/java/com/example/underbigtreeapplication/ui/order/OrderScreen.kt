package com.example.underbigtreeapplication.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.model.Option
import com.example.underbigtreeapplication.utils.formatAmount
import com.example.underbigtreeapplication.utils.isOnline
import com.example.underbigtreeapplication.viewModel.OrderViewModel
import com.example.underbigtreeapplication.viewModel.OrderViewModelFactory

@Composable
fun OrderScreen(
    foodId: String,
    onBackClick: () -> Unit = {},
    onPlaceOrder: (CartItem) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showOfflineDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isOnline(context)) {
            showOfflineDialog = true
        }
    }

    if (showOfflineDialog) {
        AlertDialog(
            onDismissRequest = {
                showOfflineDialog = false
                onBackClick()
            },
            title = { Text("Ooops") },
            text = { Text("You are offline. Please ensure your connection to proceed to order and payment.") },
            confirmButton = {
                TextButton(onClick = {
                    showOfflineDialog = false
                    onBackClick()
                }) {
                    Text("OK")
                }
            }
        )
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val viewModel: OrderViewModel = viewModel(
        factory = OrderViewModelFactory(foodId)
    )
    val food by viewModel.food.collectAsState()

    val selectedSauces by viewModel.selectedSauces.collectAsState()
    val selectedAddOns by viewModel.selectedAddOns.collectAsState()
    val takeAway by viewModel.takeAway.collectAsState()
    val remarks by viewModel.remarks.collectAsState()
    val quantity by viewModel.quantity.collectAsState()

    var showSauceWarning by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    if (isTablet) { //tablet view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
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
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = food.imageRes,
                        contentDescription = food.name,
                        modifier = Modifier.size(140.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        food.name,
                        fontSize = 18.sp
                    )
                    Text(formatAmount(food.price), fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = food.desc,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        style = TextStyle(
                            lineHeight = 14.sp
                        )
                    )
                }

                Divider(
                    color = Color.LightGray,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(2f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!food.category.contains("Drinks")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sauce")
                            Text("*Select one or more", color = Color.Red, fontSize = 14.sp)
                        }
                        food.sauceIds.forEach { sauce: Option ->
                            val isAvailable = sauce.availability
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedSauces.contains(sauce),
                                        onCheckedChange = { viewModel.toggleSauce(sauce, it) },
                                        enabled = isAvailable
                                    )
                                    Text(
                                        text = sauce.name,
                                        fontSize = 14.sp,
                                        color = if (isAvailable) Color.Black else Color.Gray
                                    )
                                }
                                Text(
                                    text = if (isAvailable) "+ ${formatAmount(sauce.price)}" else "(Unavailable)",
                                    fontSize = 14.sp,
                                    color = if (isAvailable) Color.Black else Color.Gray
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Add On")
                            Text("*Optional", color = Color.Red, fontSize = 14.sp)
                        }
                        food.addOnIds.forEach { addOn: Option ->
                            val isAvailable = addOn.availability
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedAddOns.contains(addOn),
                                        onCheckedChange = { viewModel.toggleAddOn(addOn, it) },
                                        enabled = isAvailable
                                    )
                                    Text(
                                        text = addOn.name,
                                        fontSize = 14.sp,
                                        color = if (isAvailable) Color.Black else Color.Gray
                                    )
                                }
                                Text(
                                    text = if (isAvailable) "+ ${formatAmount(addOn.price)}" else "(Unavailable)",
                                    fontSize = 14.sp,
                                    color = if (isAvailable) Color.Black else Color.Gray
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Take Away")
                            Text("*Optional", color = Color.Red, fontSize = 14.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = takeAway,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleTakeAway(checked)
                                    }
                                )
                                Text("Packaging Fee", fontSize = 14.sp)
                            }
                            Text("+ RM 0.50", fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remarks")
                            Text("*Optional", color = Color.Red, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { viewModel.setRemarks(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.decreaseQuantity() }) { Text("-") }
                        Text(quantity.toString())
                        IconButton(onClick = { viewModel.increaseQuantity() }) { Text("+") }
                    }

                    Button(
                        onClick = {
                            if (!food.category.any {
                                    it.equals(
                                        "Drinks",
                                        ignoreCase = true
                                    )
                                } && selectedSauces.isEmpty()) {
                                showSauceWarning = true
                            } else {
                                val cartItem = viewModel.buildCartItem()
                                viewModel.saveOrder(
                                    cartItem,
                                    onSuccess = {
                                        onPlaceOrder(cartItem)
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Cart")
                    }

                    if (showSauceWarning) {
                        AlertDialog(
                            onDismissRequest = { showSauceWarning = false },
                            title = { Text("Selection Required") },
                            text = { Text("Please select at least one sauce before placing your order.") },
                            confirmButton = {
                                TextButton(onClick = { showSauceWarning = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    } else { //phone view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 25.dp),
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
                Spacer(modifier = Modifier.height(10.dp))

                AsyncImage(
                    model = food.imageRes,
                    contentDescription = food.name,
                    modifier = Modifier.size(140.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            food.name,
                            fontSize = 18.sp
                        )
                        Text(formatAmount(food.price), fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = food.desc,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        style = TextStyle(
                            lineHeight = 14.sp
                        )
                    )

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )

                    if (!food.category.contains("Drinks")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sauce")
                            Text("*Select one or more", color = Color.Red, fontSize = 14.sp)
                        }
                        food.sauceIds.forEach { sauce: Option ->
                            val isAvailable = sauce.availability
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedSauces.contains(sauce),
                                        onCheckedChange = { viewModel.toggleSauce(sauce, it) },
                                        enabled = isAvailable
                                    )
                                    Text(
                                        text = sauce.name,
                                        fontSize = 14.sp,
                                        color = if (isAvailable) Color.Black else Color.Gray
                                    )
                                }
                                Text(
                                    text = if (isAvailable) "+ ${formatAmount(sauce.price)}" else "(Unavailable)",
                                    fontSize = 14.sp,
                                    color = if (isAvailable) Color.Black else Color.Gray
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Add On")
                            Text("*Optional", color = Color.Red, fontSize = 14.sp)
                        }
                        food.addOnIds.forEach { addOn: Option ->
                            val isAvailable = addOn.availability
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedAddOns.contains(addOn),
                                        onCheckedChange = { viewModel.toggleAddOn(addOn, it) },
                                        enabled = isAvailable
                                    )
                                    Text(
                                        text = addOn.name,
                                        fontSize = 14.sp,
                                        color = if (isAvailable) Color.Black else Color.Gray
                                    )
                                }
                                Text(
                                    text = if (isAvailable) "+ ${formatAmount(addOn.price)}" else "(Unavailable)",
                                    fontSize = 14.sp,
                                    color = if (isAvailable) Color.Black else Color.Gray
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Take Away")
                            Text("*Optional", color = Color.Red, fontSize = 14.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = takeAway,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleTakeAway(checked)
                                    }
                                )
                                Text("Packaging Fee", fontSize = 14.sp)
                            }
                            Text("+ RM 0.50", fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remarks")
                            Text("*Optional", color = Color.Red, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { viewModel.setRemarks(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.decreaseQuantity() }) { Text("-") }
                    Text(quantity.toString())
                    IconButton(onClick = { viewModel.increaseQuantity() }) { Text("+") }
                }

                Button(
                    onClick = {
                        if (!food.category.any {
                                it.equals(
                                    "Drinks",
                                    ignoreCase = true
                                )
                            } && selectedSauces.isEmpty()) {
                            showSauceWarning = true
                        } else {
                            val cartItem = viewModel.buildCartItem()
                            viewModel.saveOrder(
                                cartItem,
                                onSuccess = {
                                    onPlaceOrder(cartItem)
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add to Cart")
                }

                if (showSauceWarning) {
                    AlertDialog(
                        onDismissRequest = { showSauceWarning = false },
                        title = { Text("Selection Required") },
                        text = { Text("Please select at least one sauce before placing your order.") },
                        confirmButton = {
                            TextButton(onClick = { showSauceWarning = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}