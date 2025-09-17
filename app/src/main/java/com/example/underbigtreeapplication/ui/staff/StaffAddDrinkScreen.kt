package com.example.underbigtreeapplication.ui.staff

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.underbigtreeapplication.model.DrinkEntity
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffAddDrinkScreen(
    navController: NavController,
    staffViewModel: StaffViewModel
) {
    var name by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var newId by rememberSaveable { mutableStateOf("") }

    var isImageLoaded by remember { mutableStateOf(false) } // track if image is loaded successfully

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val lastId = staffViewModel.getLastDrinkIdFromFirebase()
        val nextIdNumber = lastId?.removePrefix("M")?.toIntOrNull()?.plus(1) ?: 1
        newId = "M%04d".format(nextIdNumber)
    }

    Scaffold(
        containerColor = Color(0xFFF2F2F2),
        contentColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Add New Drink") },
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
                .background(Color.White)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = {
                    imageUrl = it
                    isImageLoaded = false // reset on change
                },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth(),
                isError = showError && imageUrl.isBlank()
            )
            if (showError && imageUrl.isBlank()) {
                Text(
                    text = "Image URL is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (imageUrl.isNotBlank()) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    onSuccess = { isImageLoaded = true },
                    onError = { isImageLoaded = false }
                )
                Image(
                    painter = painter,
                    contentDescription = "Drink Image Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.LightGray)
                )
                if (!isImageLoaded) {
                    Text(
                        text = "Failed to load image",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
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
                    if (name.isBlank() || price.isBlank() || imageUrl.isBlank() || !isImageLoaded) {
                        showError = true
                        if (!isImageLoaded) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please provide a valid image URL that can load.")
                            }
                        }
                    } else {
                        coroutineScope.launch {
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