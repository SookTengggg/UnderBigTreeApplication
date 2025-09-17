package com.example.underbigtreeapplication.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.underbigtreeapplication.model.SauceEntity
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffAddSauceScreen(
    navController: NavController,
    staffViewModel: StaffViewModel
) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var newId by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val lastId = staffViewModel.getLastSauceIdFromFirebase()
        val nextIdNumber = lastId?.removePrefix("SM")?.toIntOrNull()?.plus(1) ?: 1
        newId = "SM%04d".format(nextIdNumber)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Add New Sauce") },
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
                        val newSauce = SauceEntity(
                            id = newId,
                            name = name,
                            price = price.toDoubleOrNull() ?: 0.0,
                            availability = true
                        )
                        coroutineScope.launch {
                            staffViewModel.addSauce(newSauce)
                            snackbarHostState.showSnackbar(
                                message = "Sauce added successfully!",
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

@Composable
fun RequiredTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    singleLine: Boolean = true
) {
    Column {
        Row {
            Text(label)
            Text("*", color = Color.Red)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
            isError = showError
        )
        if (showError) {
            Text("This field is required", color = Color.Red, fontSize = 12.sp)
        }
    }
}