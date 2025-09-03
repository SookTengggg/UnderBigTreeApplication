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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.underbigtreeapp.R
import com.example.underbigtreeapplication.repository.Profile
import com.example.underbigtreeapplication.ui.BottomNavigation
import com.example.underbigtreeapplication.ui.SideNavigationBar
import com.example.underbigtreeapplication.ui.customerHomePage.SideNavigation
import com.example.underbigtreeapplication.ui.customerHomePage.navItems
import com.example.underbigtreeapplication.viewModel.ProfileUiState
import com.example.underbigtreeapplication.viewModel.ProfileViewModel
import com.example.underbigtreeapplication.viewModel.ProfileViewModelFactory

data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

val navItems = listOf(
    NavItem(Icons.Filled.Home, "Home", "home"),
    NavItem(Icons.Filled.Menu, "Activity","activityScreen"),
    NavItem(Icons.Filled.Person, "Profile", "profile")
)

@Composable
fun ProfileScreen (navController: NavController, viewModel: ProfileViewModel) {
    val uiState by viewModel.profileState.collectAsStateWithLifecycle()
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val email = firebaseUser?.email

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600
    var selectedItem by remember { mutableStateOf("profile") }

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
            SideNavigationBar(
                items = navItems,
                selected = selectedItem,
                onItemSelected = { route ->
                    selectedItem = route
                    if (navController.currentBackStackEntry?.destination?.route != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    ProfileContentWrapper(uiState, navController)
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    BottomNavigation(items = navItems, navController = navController)
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    ProfileContentWrapper(uiState, navController)
                }
            }
        }
    }
}

@Composable
private fun ProfileContentWrapper(uiState: ProfileUiState, navController: NavController) {
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
                ProfileContent(profile = profile, navController)
            } else {
                Text("No Profile found")
            }
        }
    }
}

@Composable
fun ProfileContent(profile: Profile, navController: NavController) {
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
            ProfileField(label = "Phone", value = profile.phone)
            ProfileField(label = "Email", value = profile.email)
            ProfileField(label = "Gender", value = profile.gender)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("editProfile") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Profile")
        }

    }
}


@Composable
fun ProfileField(label: String, value: String){
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