package com.example.underbigtreeapplication.ui.staff

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    var imageUrl by remember { mutableStateOf("") }
    var useLink by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var newId by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val lastId = staffViewModel.getLastDrinkIdFromFirebase()
        val nextIdNumber = lastId?.removePrefix("M")?.toIntOrNull()?.plus(1) ?: 1
        newId = "M%04d".format(nextIdNumber)
    }

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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = !useLink,
                    onClick = { useLink = false },
                    label = { Text("Upload Image") }
                )
                FilterChip(
                    selected = useLink,
                    onClick = { useLink = true },
                    label = { Text("Use Image Link") }
                )
            }
            if (useLink) {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp, max = 100.dp)
                        .verticalScroll(rememberScrollState()),
                    maxLines = Int.MAX_VALUE,
                    singleLine = false,
                    isError = showError && imageUrl.isBlank()
                )
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
        } else {
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
                    if (name.isBlank() || price.isBlank() ||
                        (!useLink && imageUri == null) ||
                        (useLink && imageUrl.isBlank())
                    ) {
                        showError = true
                    } else {
                        coroutineScope.launch {
                            val finalImageUrl = if (useLink) {
                                imageUrl
                            } else {
                                staffViewModel.uploadDrinkImageFromUri(imageUri!!, newId)
                            }

                            if (!finalImageUrl.isNullOrBlank()) {
                                val newDrink = DrinkEntity(
                                    id = newId,
                                    name = name,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    imageRes = finalImageUrl,
                                    category = listOf("Drinks"),
                                    desc = desc,
                                    availability = true
                                )
                                staffViewModel.addDrink(newDrink)
                                snackbarHostState.showSnackbar("Drink added successfully!")
                                kotlinx.coroutines.delay(300)
                                navController.popBackStack()
                            } else {
                                snackbarHostState.showSnackbar("Your imageRes is Failed to upload image. Try again."+finalImageUrl)
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