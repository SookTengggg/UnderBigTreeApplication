package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffAddOnScreen(
    navController: NavController,
    staffViewModel: StaffViewModel
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var newId by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Generate next Add-On ID from Firebase
    LaunchedEffect(Unit) {
        val lastId = staffViewModel.getLastAddOnIdFromFirebase()
        val nextIdNumber = lastId?.removePrefix("AM")?.toIntOrNull()?.plus(1) ?: 1
        newId = "AM%04d".format(nextIdNumber)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Add New Add-On") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2F2F2),
                    titleContentColor = Color.Black
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Name
            RequiredTextField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                showError = showError && name.isBlank()
            )

            // Price
            RequiredTextField(
                label = "Price (RM)",
                value = price,
                onValueChange = { price = it.filter { ch -> ch.isDigit() || ch == '.' } },
                showError = showError && price.isBlank()
            )

            Spacer(Modifier.height(24.dp))

            // Confirm button
            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank()) {
                        showError = true
                    } else {
                        val newAddOn = AddOnEntity(
                            id = newId,
                            name = name,
                            price = price.toDoubleOrNull() ?: 0.0,
                            availability = true,
                        )

                        coroutineScope.launch {
                            staffViewModel.addAddOn(newAddOn)
                            snackbarHostState.showSnackbar(
                                message = "Add-On added successfully!",
                                duration = SnackbarDuration.Short
                            )
                            kotlinx.coroutines.delay(300)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("CONFIRM")
            }
        }
    }
}