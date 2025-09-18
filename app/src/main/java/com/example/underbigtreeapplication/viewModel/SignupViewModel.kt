package com.example.underbigtreeapplication.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.example.underbigtreeapplication.repository.UserRepository
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var selectedGender by mutableStateOf("")

    private val _signupStatus = MutableLiveData<Result<Boolean>>()
    val signupStatus: LiveData<Result<Boolean>> = _signupStatus

    fun onNameChange(newName: String) {
        name = newName
    }

    fun onPhoneChange(newPhone: String) {
        phone = newPhone
    }

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
    }

    fun onGenderSelected(gender: String) {
        selectedGender = gender
    }


    fun signup() {
        viewModelScope.launch {
            try {
                val result = repository.registerUser(email, password, name, phone)
                _signupStatus.value = Result.success(result)
            } catch (e: Exception) {
                _signupStatus.value = Result.failure(e)
            }
        }
    }
}