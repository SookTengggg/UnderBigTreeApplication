package com.example.underbigtreeapplication.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.underbigtreeapplication.repository.Profile
import com.example.underbigtreeapplication.repository.ProfileRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success (val profile: Profile?) : ProfileUiState()
    data class Error (val message: String) : ProfileUiState()
}

class ProfileViewModel (private  val repository: ProfileRepository) : ViewModel() {
    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()
    private val storage = FirebaseStorage.getInstance().reference

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            try {
                repository.refreshFromFirebase(uid)
                repository.observeProfile(uid).collect { profile ->
                    _profileState.value = ProfileUiState.Success(profile)
                }
            } catch (e: Exception) {
                _profileState.value = ProfileUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun loadProfileByEmail(email: String) {
        viewModelScope.launch {
            try {
                repository.observeProfileByEmail(email).collect { profile ->
                    _profileState.value = ProfileUiState.Success(profile)
                }
            } catch (e: Exception) {
                _profileState.value = ProfileUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun createProfile(name: String, phone: String, email: String, gender: String) {
        viewModelScope.launch {
            try {
                val profile = repository.createProfile(name, phone, email, gender)
                _profileState.value = ProfileUiState.Success(profile)
            } catch (e: Exception) {
                _profileState.value = ProfileUiState.Error(e.message ?: "Failed to create profile")
            }
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                repository.updateProfile(profile)
                _profileState.value = ProfileUiState.Success(profile)
            } catch (e: Exception) {
                _profileState.value = ProfileUiState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun uploadPhoto(uid: String, uri: Uri, updatedProfile: Profile, onComplete: (Profile) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
        Log.d("UploadDebug", "Uploading to: ${storageRef.path}")
        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                Log.d("UploadDebug", "Download URL: $downloadUri")
                val newProfile = updatedProfile.copy(photoUrl = downloadUri.toString())
                updateProfile(newProfile)
                onComplete(newProfile)
            }
        }
            .addOnFailureListener { e ->
                Log.e("UploadDebug", "Upload failed", e)
            }
    }
}
