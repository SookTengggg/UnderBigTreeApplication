package com.example.underbigtreeapplication.ui.profile

import ads_mobile_sdk.na
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.underbigtreeapp.R
import com.example.underbigtreeapplication.repository.Profile
import com.example.underbigtreeapplication.ui.SideNavigationBar
import com.example.underbigtreeapplication.ui.customerHomePage.NavItem
import com.example.underbigtreeapplication.viewModel.ProfileUiState
import com.example.underbigtreeapplication.viewModel.ProfileViewModel
import com.example.underbigtreeapplication.viewModel.ProfileViewModelFactory

data class StaffNavItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

val staffNavItems = listOf(
    StaffNavItem(Icons.Filled.Home, "Home", "staffFood"),
    StaffNavItem(Icons.Filled.Edit, "Edit", "staffEditProfile"),
    StaffNavItem(Icons.Filled.Menu, "Activity","staffActivityScreen"),
    StaffNavItem(Icons.Filled.Person, "Profile", "staffProfile")
)

@Composable
fun StaffProfileScreen (navController: NavController, viewModel: ProfileViewModel) {
    val uiState by viewModel.profileState.collectAsStateWithLifecycle()
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val email = firebaseUser?.email

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600
    var selectedItem by remember { mutableStateOf("staffProfile") }

    LaunchedEffect(email) {
        if (email != null) {
            viewModel.loadProfileByEmail(email)
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isTablet) {
            StaffSideNavigationBar(
                items = staffNavItems,
                selected = selectedItem,
                navController = navController,
                onItemSelected = { newSelection -> selectedItem = newSelection }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    StaffProfileContentWrapper(uiState, navController)
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    StaffBottomNavigation(
                        items = staffNavItems,
                        navController = navController,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    StaffProfileContentWrapper(uiState, navController)
                }
            }
        }
    }
}

@Composable
private fun StaffProfileContentWrapper(uiState: ProfileUiState, navController: NavController) {
    when (uiState) {
        is ProfileUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
        is ProfileUiState.Error -> Text (
            text = uiState.message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
        is ProfileUiState.Success -> {
            val profile = uiState.profile
            if (profile != null){
                StaffProfileContent(profile = profile, navController)
            } else {
                Text("No Profile found")
            }
        }
    }
}

@Composable
fun StaffProfileContent(profile: Profile, navController: NavController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(scrollState)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = R.drawable.profile,
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StaffProfileField(label = "Phone", value = profile.phone)
            StaffProfileField(label = "Email", value = profile.email)
            StaffProfileField(label = "Gender", value = profile.gender)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("staffEditProfile") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Profile")
        }

    }
}


@Composable
fun StaffProfileField(label: String, value: String){
    Column(modifier = Modifier.fillMaxWidth()){
        Text (
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun StaffBottomNavigation(items: List<StaffNavItem>, navController: NavController, modifier: Modifier = Modifier) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(modifier = modifier.fillMaxWidth(), containerColor = Color.White) {
        items.forEach { item ->
            val selected = currentDestination == item.route
            NavigationBarItem(
                selected = selected, onClick = {
                    if (currentDestination != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) }
            )
        }
    }
}

@Composable
fun StaffSideNavigationBar(items: List<StaffNavItem>, selected: String, navController: NavController, onItemSelected: (String) -> Unit, content: @Composable () -> Unit) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet (modifier = Modifier.width(150.dp)) {
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        selected = selected == item.route,
                        onClick = {
                            if(navController.currentBackStackEntry?.destination?.route != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {saveState = true}
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            onItemSelected(item.route)
                        }
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            content()
        }
    }
}