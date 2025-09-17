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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.model.OptionItem
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffAddFoodScreen(
    navController: NavController,
    staffViewModel: StaffViewModel
) {
    var name by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var newId by rememberSaveable { mutableStateOf("") }
    var isImageLoaded by rememberSaveable { mutableStateOf(false) }

    val categoryOptions = listOf("Rice", "Chicken", "Fish", "Spaghetti")
    var selectedCategory by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    var availableAddOns by rememberSaveable { mutableStateOf<List<OptionItem>>(emptyList()) }
    var selectedAddOns by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var availableSauces by rememberSaveable { mutableStateOf<List<OptionItem>>(emptyList()) }
    var selectedSauces by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val lastId = staffViewModel.getLastDrinkIdFromFirebase()
        val nextIdNumber = lastId?.removePrefix("M")?.toIntOrNull()?.plus(1) ?: 1
        newId = "M%04d".format(nextIdNumber)
        availableAddOns = staffViewModel.getAllAddOnNamesFromFirebase()
        availableSauces = staffViewModel.getAllSauceNamesFromFirebase()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Add New Food") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = {
                    imageUrl = it
                    isImageLoaded = false},
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
                    model = imageUrl,
                    onSuccess = { isImageLoaded = true },
                    onError = { isImageLoaded = false },
                    onLoading = { isImageLoaded = false }
                )
                Image(
                    painter = painter,
                    contentDescription = "Food Image Preview",
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

            Text(
                buildAnnotatedString {
                    append("Select Category")
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" *")
                    }
                },
                style = MaterialTheme.typography.titleMedium
            )

            Column {
                categoryOptions.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategory = if (selectedCategory.contains(category)) {
                                    selectedCategory - category
                                } else {
                                    selectedCategory + category
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedCategory.contains(category),
                            onCheckedChange = { checked ->
                                selectedCategory = if (checked) {
                                    selectedCategory + category
                                } else {
                                    selectedCategory - category
                                }
                            }
                        )
                        Text(
                            category,
                            color = if (showError && selectedCategory.isEmpty()) Color.Red else Color.Unspecified
                        )
                    }
                }
            }

            if (availableAddOns.isNotEmpty()) {
                Text("Select Add-Ons:", style = MaterialTheme.typography.titleMedium)
                Column {
                    availableAddOns.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAddOns = if (selectedAddOns.contains(item.id)) {
                                        selectedAddOns - item.id
                                    } else {
                                        selectedAddOns + item.id
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedAddOns.contains(item.id),
                                onCheckedChange = { checked ->
                                    selectedAddOns = if (checked) {
                                        selectedAddOns + item.id
                                    } else {
                                        selectedAddOns - item.id
                                    }
                                }
                            )
                            Text(item.name)
                        }
                    }
                }
            }

            if (availableSauces.isNotEmpty()) {
                Text("Select Sauces:", style = MaterialTheme.typography.titleMedium)
                Column {
                    availableSauces.forEach { sauce ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSauces = if (selectedSauces.contains(sauce.id)) {
                                        selectedSauces - sauce.id
                                    } else {
                                        selectedSauces + sauce.id
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedSauces.contains(sauce.id),
                                onCheckedChange = { checked ->
                                    selectedSauces = if (checked) {
                                        selectedSauces + sauce.id
                                    } else {
                                        selectedSauces - sauce.id
                                    }
                                }
                            )
                            Text(sauce.name)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank() || imageUrl.isBlank() ||
                        selectedCategory.isEmpty() || !isImageLoaded
                    ) {
                        showError = true
                        if (!isImageLoaded) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please provide a valid image URL that can load.")
                            }
                        }
                    } else {
                        coroutineScope.launch {

                            val newFood = MenuEntity(
                                id = newId,
                                name = name,
                                price = price.toDoubleOrNull() ?: 0.0,
                                imageRes = imageUrl,
                                category = selectedCategory,
                                desc = desc,
                                availability = true,
                                addOn = selectedAddOns,
                                sauce = selectedSauces
                            )
                            staffViewModel.addFood(newFood)
                            snackbarHostState.showSnackbar("Food added successfully!")
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