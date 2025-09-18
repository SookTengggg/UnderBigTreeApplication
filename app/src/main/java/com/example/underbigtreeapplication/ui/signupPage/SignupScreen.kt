package com.example.underbigtreeapplication.ui.signupPage

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.underbigtreeapp.R
import com.example.underbigtreeapplication.data.local.AppDatabase
import com.example.underbigtreeapplication.data.remote.FirebaseService
import com.example.underbigtreeapplication.repository.ProfileRepository
import com.example.underbigtreeapplication.viewModel.SignupViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    signupViewModel: SignupViewModel = viewModel()
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val database = AppDatabase.getDatabase(context)
    val profileDao = database.profileDao()
    val profileRepository = ProfileRepository(dao = profileDao)
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Female", "Male", "Prefer not to disclose")
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ubt_logo),
                contentDescription = "Under Big Tree Logo",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 24.dp)
            )

            Text(text = "Sign Up", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = signupViewModel.name,
                onValueChange = { signupViewModel.onNameChange(it) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )


            OutlinedTextField(
                value = signupViewModel.phone,
                onValueChange = { signupViewModel.onPhoneChange(it) },
                label = { Text("Phone Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = signupViewModel.email,
                onValueChange = { signupViewModel.onEmailChange(it) },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {expanded = !expanded},
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = signupViewModel.selectedGender,
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
                                signupViewModel.onGenderSelected(gender)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = signupViewModel.password,
                onValueChange = { signupViewModel.onPasswordChange(it) },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = signupViewModel.confirmPassword,
                onValueChange = { signupViewModel.onConfirmPasswordChange(it) },
                label = { Text("Confirm Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )


            Spacer(modifier = Modifier.height(20.dp))

            val scope = rememberCoroutineScope()

            Button(
                onClick = {
                    if (signupViewModel.name.isBlank() || signupViewModel.phone.isBlank() || signupViewModel.email.isBlank()) {
                        errorMessage = "Please fill in all required fields"
                        return@Button
                    }

                    if (signupViewModel.password != signupViewModel.confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        val result = FirebaseService.registerUser(signupViewModel.email, signupViewModel.password)

                        result.onSuccess { authResult ->
                            val authUid = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
                            try {
                                profileRepository.createProfile(
                                    authUid = authUid,
                                    name = signupViewModel.name,
                                    phone = signupViewModel.phone,
                                    email = signupViewModel.email,
                                    gender = signupViewModel.selectedGender
                                )
                                Toast.makeText(context, "Signup successful!", Toast.LENGTH_SHORT).show()
                                onRegisterSuccess()
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Profile Creation failed"
                            }
                            isLoading = false
                        }.onFailure {
                            isLoading = false
                            errorMessage = it.message ?: "Signup failed"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Register")
                }
            }

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSignupScreen() {
    SignupScreen(
        onRegisterSuccess = {},
        onNavigateToLogin = {}
    )
}