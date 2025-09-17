package com.example.underbigtreeapplication.ui.staff


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.model.OptionItem
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import kotlinx.coroutines.launch

@Composable
fun FoodEditScreen(
    navController: NavController,
    staffViewModel: StaffViewModel,
    menuId: String
) {
    val menuList by staffViewModel.menus.collectAsState()
    val menu = menuList.find { it.id == menuId }

    if (menu == null) {
        LaunchedEffect(menuId) {
            val fetchedMenu = staffViewModel.getMenuById(menuId)
            if (fetchedMenu != null) {
                staffViewModel.addMenu(fetchedMenu)
            }
        }
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        EditFoodForm(navController, staffViewModel, menu)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodForm(
    navController: NavController,
    staffViewModel: StaffViewModel,
    menu: MenuEntity
) {
    var imageUrl by rememberSaveable { mutableStateOf(menu.imageRes ?: "") }
    var name by rememberSaveable { mutableStateOf(menu.name) }
    var desc by rememberSaveable { mutableStateOf(menu.desc) }
    var price by rememberSaveable { mutableStateOf(menu.price.toString()) }
    var selectedCategory by rememberSaveable { mutableStateOf(menu.category) }
    var selectedAddOns by rememberSaveable { mutableStateOf(menu.addOn) }
    var selectedSauces by rememberSaveable { mutableStateOf(menu.sauce) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var isImageLoaded by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isDrink = menu.category.any { it.equals("Drinks", ignoreCase = true) }

    var availableAddOns by rememberSaveable { mutableStateOf<List<OptionItem>>(emptyList()) }
    var availableSauces by rememberSaveable { mutableStateOf<List<OptionItem>>(emptyList()) }
    val categoryOptions = listOf("Rice", "Chicken", "Fish", "Spaghetti")

    LaunchedEffect(Unit) {
        if (!isDrink) {
            availableAddOns = staffViewModel.getAllAddOnNamesFromFirebase()
            availableSauces = staffViewModel.getAllSauceNamesFromFirebase()
        }
    }

    val painter = rememberAsyncImagePainter(
        model = imageUrl,
        onSuccess = { isImageLoaded = true },
        onError = { isImageLoaded = false },
        onLoading = { isImageLoaded = false }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Menu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                isError = showError && imageUrl.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            if (showError && imageUrl.isBlank()) {
                Text(
                    text = "Image URL is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (imageUrl.isNotBlank()) {
                Image(
                    painter = painter,
                    contentDescription = "Food Image",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
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

            if (!isDrink) {
                Text("Select Category", style = MaterialTheme.typography.titleMedium)
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
                            Text(category)
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
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank() || imageUrl.isBlank() || (!isDrink && selectedCategory.isEmpty()) || !isImageLoaded) {
                        showError = true
                        if (!isImageLoaded) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please provide a valid image URL that can load.")
                            }
                        }
                    } else {
                        val updatedMenu = menu.copy(
                            name = name,
                            desc = desc,
                            price = price.toDoubleOrNull() ?: menu.price,
                            category = if (!isDrink) selectedCategory else menu.category,
                            addOn = if (!isDrink) selectedAddOns else menu.addOn,
                            sauce = if (!isDrink) selectedSauces else menu.sauce,
                            imageRes = imageUrl
                        )

                        coroutineScope.launch {
                            staffViewModel.updateMenu(updatedMenu)
                            snackbarHostState.showSnackbar(
                                "Food updated successfully!",
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
