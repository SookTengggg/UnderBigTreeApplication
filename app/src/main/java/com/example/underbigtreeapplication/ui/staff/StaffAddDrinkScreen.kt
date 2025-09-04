package com.example.underbigtreeapplication.ui.staff

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.underbigtreeapplication.model.DrinkEntity
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffAddDrinkScreen(
    navController: NavController,
    staffViewModel: StaffViewModel
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showError by remember { mutableStateOf(false) }
    var newId by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Generate next Drink ID from Firebase
    LaunchedEffect(Unit) {
        val lastId = staffViewModel.getLastDrinkIdFromFirebase()
        val nextIdNumber = lastId?.removePrefix("M")?.toIntOrNull()?.plus(1) ?: 1
        newId = "M%04d".format(nextIdNumber)
    }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Drink") },
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
            // Image upload field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = "Drink Image")
                } else {
                    Text(
                        "Click to upload image",
                        color = if (showError && imageUri == null) Color.Red else Color.Gray
                    )
                }
            }

            RequiredTextField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                showError = showError && name.isBlank()
            )

            RequiredTextField(
                label = "Description",
                value = desc,
                onValueChange = { desc = it },
                showError = showError && desc.isBlank()
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
                    if (name.isBlank() || price.isBlank() || imageUri == null) {
                        showError = true
                    } else {
                        coroutineScope.launch {
                            val imageUrl = staffViewModel.uploadDrinkImageToFirebase(imageUri!!, newId)
                            if (imageUrl != null) {
                                val newDrink = DrinkEntity(
                                    id = newId,
                                    name = name,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    imageRes = imageUrl,
                                    category = listOf("Drinks"),
                                    desc = desc,
                                    availability = true
                                )
                                staffViewModel.addDrink(newDrink)
                                snackbarHostState.showSnackbar("Drink added successfully!")
                                kotlinx.coroutines.delay(300)
                                navController.popBackStack()
                            } else {
                                snackbarHostState.showSnackbar("Failed to upload image. Try again.")
                            }
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