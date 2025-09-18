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
import com.example.underbigtreeapplication.model.SauceEntity
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import kotlinx.coroutines.launch

@Composable
fun StaffEditSauceScreen(
    navController: NavController,
    staffViewModel: StaffViewModel,
    sauceId: String
) {
    val sauces by staffViewModel.sauces.collectAsState()
    val sauce = sauces.find { it.id == sauceId }

    if (sauce == null) {
        LaunchedEffect(sauceId) {
            val fetchedSauce = staffViewModel.getSauceById(sauceId)
            if (fetchedSauce != null) {
                staffViewModel.addSauce(fetchedSauce)
            }
        }
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        EditSauceForm(navController, staffViewModel, sauce)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSauceForm(
    navController: NavController,
    staffViewModel: StaffViewModel,
    sauce: SauceEntity
) {
    var name by rememberSaveable { mutableStateOf(sauce.name) }
    var price by rememberSaveable { mutableStateOf(sauce.price.toString()) }
    var showError by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Sauce") },
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
                        val updatedSauce = sauce.copy(
                            name = name,
                            price = price.toDoubleOrNull() ?: sauce.price
                        )

                        coroutineScope.launch {
                            staffViewModel.updateSauce(updatedSauce)
                            snackbarHostState.showSnackbar(
                                message = "Sauce updated successfully!",
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
