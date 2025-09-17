package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.underbigtreeapplication.model.AddOnEntity
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import kotlinx.coroutines.launch

@Composable
fun StaffEditAddOnScreen(
    navController: NavController,
    staffViewModel: StaffViewModel,
    addOnId: String
) {
    val addons by staffViewModel.addons.collectAsState()
    val addOn = addons.find { it.id == addOnId }

    if (addOn == null) {
        LaunchedEffect(addOnId) {
            val fetchedAddOn = staffViewModel.getAddOnById(addOnId)
            if (fetchedAddOn != null) {
                staffViewModel.addAddOn(fetchedAddOn)
            }
        }
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        EditAddOnForm(navController, staffViewModel, addOn)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddOnForm(
    navController: NavController,
    staffViewModel: StaffViewModel,
    addOn: AddOnEntity
) {
    var name by rememberSaveable { mutableStateOf(addOn.name) }
    var price by rememberSaveable { mutableStateOf(addOn.price.toString()) }
    var showError by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Add-On") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            RequiredTextField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                showError = showError && name.isBlank()
            )

            RequiredTextField(
                label = "Price (RM)",
                value = price,
                onValueChange = { price = it.filter { ch -> ch.isDigit() || ch == '.' } },
                showError = showError && price.isBlank()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank()) {
                        showError = true
                    } else {
                        val updatedAddOn = addOn.copy(
                            name = name,
                            price = price.toDoubleOrNull() ?: addOn.price
                        )

                        coroutineScope.launch {
                            staffViewModel.updateAddOn(updatedAddOn)
                            snackbarHostState.showSnackbar(
                                message = "Add-On updated successfully!",
                                duration = SnackbarDuration.Short
                            )
                            kotlinx.coroutines.delay(300)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("UPDATE")
            }
        }
    }
}
