package com.example.underbigtreeapplication.ui.staff

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.model.OptionItem
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodEditScreen(
    navController: NavController,
////    staffViewModel: StaffViewModel,
////    foodId: String,
//    foodType: String // "menu" or "addon"
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var useLink by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<List<String>>(emptyList()) }
//    var availableAddOns by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
//    var selectedAddOns by remember { mutableStateOf<List<String>>(emptyList()) }
//    var availableSauces by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
//    var selectedSauces by remember { mutableStateOf<List<String>>(emptyList()) }
//
//    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
//
//    // 1. Retrieve data from Firebase
//    LaunchedEffect(foodId) {
//        if (foodType == "Menu") {
//            val menu = staffViewModel.getMenuById(foodId)
//            menu?.let {
//                name = it.name
//                desc = it.desc
//                price = it.price.toString()
//                imageUrl = it.imageRes
//                selectedCategory = it.category
//                selectedAddOns = it.addOn
//                selectedSauces = it.sauce
//            }
//            availableAddOns = staffViewModel.getAllAddOnNamesFromFirebase()
//            availableSauces = staffViewModel.getAllSauceNamesFromFirebase()
//        } else if (foodType == "AddOn") {
//            val addon = staffViewModel.getAddOnById(foodId)
//            addon?.let {
//                name = it.name
//                price = it.price.toString()
//            }
//        }
//    }
//
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }
//
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Food") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
//
//            // Only menu has image selection
//            if (foodType == "menu") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = !useLink,
                    onClick = { useLink = false },
                    label = { Text("Upload Image") })
                FilterChip(
                    selected = useLink,
                    onClick = { useLink = true },
                    label = { Text("Use Image Link") })
            }

            if (useLink) {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp, max = 100.dp),
                    maxLines = Int.MAX_VALUE,
                    singleLine = false
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) AsyncImage(
                        model = imageUri,
                        contentDescription = "Food Image"
                    )
                    else AsyncImage(model = imageUrl, contentDescription = "Food Image")
                }
            }
        }
//
            // Name and Price (both menu & addon)
            RequiredTextField("Name", name, { name = it }, showError && name.isBlank())
            RequiredTextField("Price (RM)", price, { price = it.filter { ch -> ch.isDigit() || ch == '.' } }, showError && price.isBlank())

            // Only menu has description and category
//            if (foodType == "menu") {
                RequiredTextField("Description", desc, { desc = it }, showError && desc.isBlank())

                val categoryOptions = listOf("Rice", "Chicken", "Fish", "Spaghetti", "Drinks")
                Text(buildAnnotatedString {
                    append("Select Category")
                    withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                        append(" *")
                    }
                })
                Column {
                    categoryOptions.forEach { category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedCategory = if (selectedCategory.contains(category)) selectedCategory - category else selectedCategory + category
                            }.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedCategory.contains(category),
                                onCheckedChange = { checked ->
                                    selectedCategory = if (checked) selectedCategory + category else selectedCategory - category
                                }
                            )
                            Text(category)
                        }
                    }
                }
//
//                // Add-ons
//                if (availableAddOns.isNotEmpty()) {
//                    Text("Select Add-Ons:")
//                    Column {
//                        availableAddOns.forEach { item ->
//                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
//                                selectedAddOns = if (selectedAddOns.contains(item.id)) selectedAddOns - item.id else selectedAddOns + item.id
//                            }.padding(vertical = 4.dp)) {
//                                Checkbox(
//                                    checked = selectedAddOns.contains(item.id),
//                                    onCheckedChange = { checked ->
//                                        selectedAddOns = if (checked) selectedAddOns + item.id else selectedAddOns - item.id
//                                    }
//                                )
//                                Text(item.name)
//                            }
//                        }
//                    }
//                }
//
//                // Sauces
//                if (availableSauces.isNotEmpty()) {
//                    Text("Select Sauces:")
//                    Column {
//                        availableSauces.forEach { sauce ->
//                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
//                                selectedSauces = if (selectedSauces.contains(sauce.id)) selectedSauces - sauce.id else selectedSauces + sauce.id
//                            }.padding(vertical = 4.dp)) {
//                                Checkbox(
//                                    checked = selectedSauces.contains(sauce.id),
//                                    onCheckedChange = { checked ->
//                                        selectedSauces = if (checked) selectedSauces + sauce.id else selectedSauces - sauce.id
//                                    }
//                                )
//                                Text(sauce.name)
//                            }
//                        }
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            Button(
//                onClick = {
//                    if (name.isBlank() || price.isBlank() || (foodType=="menu" && selectedCategory.isEmpty())) {
//                        showError = true
//                    } else {
//                        coroutineScope.launch {
//                            if (foodType == "menu") {
//                                val finalImageUrl = if (useLink) {
//                                    imageUrl
//                                } else {
//                                    staffViewModel.uploadDrinkImageFromUri(imageUri!!, foodId)
//                                }
//                                if (!finalImageUrl.isNullOrBlank()) {
//                                    val newFood = MenuEntity(
//                                        id = foodId,
//                                        name = name,
//                                        price = price.toDoubleOrNull() ?: 0.0,
//                                        imageRes = finalImageUrl,
//                                        category = selectedCategory,
//                                        desc = desc,
//                                        availability = true,
//                                        addOn = selectedAddOns,
//                                        sauce = selectedSauces
//                                    )
//                                    staffViewModel.addFood(newFood)
//                                    snackbarHostState.showSnackbar("Food added successfully!")
//                                    kotlinx.coroutines.delay(300)
//                                    navController.popBackStack()
//                                } else {
//                                    snackbarHostState.showSnackbar("Failed to upload image. Try again.")
//                                }
//                            } else if (foodType == "addon") {
//                                val updatedAddOn = staffViewModel.getAddOnById(foodId)?.copy(
//                                    name = name,
//                                    price = price.toDoubleOrNull() ?: 0.0
//                                )
//                                updatedAddOn?.let { staffViewModel.updateAddOn(it) }
//                            }
//                            snackbarHostState.showSnackbar("Updated successfully!")
//                            kotlinx.coroutines.delay(300)
//                            navController.popBackStack()
//                        }
//                    }
//                },
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            ) {
//                Text("SAVE CHANGES")
//            }
//        }
//    }
    }
}

// Helper composable for text fields
@Composable
fun RequiredTextField(label: String, value: String, onValueChange: (String) -> Unit, showError: Boolean) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = showError
        )
        if (showError) {
            Text("This field is required", color = Color.Red, fontSize = 12.sp)
        }
    }
}
