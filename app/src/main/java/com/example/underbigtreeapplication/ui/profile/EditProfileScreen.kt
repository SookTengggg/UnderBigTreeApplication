package com.example.underbigtreeapplication.ui.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.underbigtreeapp.R
import com.example.underbigtreeapplication.data.local.AppDatabase
import com.example.underbigtreeapplication.repository.Profile
import com.example.underbigtreeapplication.repository.ProfileRepository
import com.example.underbigtreeapplication.viewModel.ProfileUiState
import com.example.underbigtreeapplication.viewModel.ProfileViewModel
import com.example.underbigtreeapplication.viewModel.ProfileViewModelFactory
import com.google.common.collect.Table

@Composable
fun EditProfileScreen(navController: NavController, viewModel: ProfileViewModel){
    val uiState by viewModel.profileState.collectAsStateWithLifecycle()

    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val email = firebaseUser?.email

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    LaunchedEffect(email) {
        if (email != null) {
            viewModel.loadProfileByEmail(email)
        }
    }

    when (uiState) {
        is ProfileUiState.Loading -> {
            Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ProfileUiState.Error -> {
            Text(
                text = (uiState as ProfileUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp).background(Color.White)
            )
        }
        is ProfileUiState.Success -> {
            val profile = (uiState as ProfileUiState.Success).profile
            if (profile != null) {
                EditProfileContent(
                    profile = profile,
                    onBackClick = {navController.popBackStack()},
                    viewModel = viewModel,
                    onSaveClick = { updated ->
                        navController.popBackStack()
                    },
                    isTablet = isTablet
                )
            }


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(profile: Profile, viewModel: ProfileViewModel, onBackClick: () -> Unit, onSaveClick: (Profile) -> Unit, isTablet: Boolean = false){
    val name by viewModel.name.collectAsStateWithLifecycle()
    val phone by viewModel.phone.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Female", "Male", "Prefer not to disclose")
    val selectedGender by viewModel.gender.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold (
        containerColor = Color.White,
        contentColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {Text("Edit Profile", color = Color.Black)},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2F2F2),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ){ innerPadding  ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(if (isTablet) 64.dp else 24.dp)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            AsyncImage(
                model = R.drawable.profile,
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.name.value = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.phone.value = it },
                label = { Text("Phone Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {expanded = !expanded},
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedGender,
                    onValueChange = {  },
                    label = { Text("Gender (Optional)") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    genderOptions.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender) },
                            onClick = {
                                viewModel.gender.value = gender
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val updatedProfile = profile.copy(
                        name = name,
                        phone = phone,
                        gender = selectedGender,
                    )
                    viewModel.updateProfile(updatedProfile)
                    onSaveClick(updatedProfile)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }

    }
}